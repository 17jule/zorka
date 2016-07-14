/**
 * Copyright 2012-2016 Rafal Lewczuk <rafal.lewczuk@jitlogic.com>
 * <p/>
 * This is free software. You can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p/>
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this software. If not, see <http://www.gnu.org/licenses/>.
 */

package com.jitlogic.zorka.lisp;

import com.jitlogic.zorka.util.ObjectInspector;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Map;

import static com.jitlogic.zorka.lisp.Symbol.symbol;

/**
 * LISP context object holds all data for particular instance of LISP:
 * - symbol dictionary;
 * - root environment (TBD);
 */
public class Interpreter {

    private Environment env = new Environment(null);

    public final Symbol BEGIN = Symbol.symbol("begin");
    public final Symbol COND = Symbol.symbol("cond");
    public final Symbol DEF = Symbol.symbol("def");
    public final Symbol ELSE = Symbol.symbol("else");
    public final Symbol GE = Symbol.symbol("=>");
    public final Symbol FN = Symbol.symbol("fn");
    public final Symbol MACRO = Symbol.symbol("macro");
    public final Symbol QUOTE = Symbol.symbol("quote");
    public final Symbol QUASIQUOTE = Symbol.symbol("quasiquote");
    public final Symbol SET = Symbol.symbol("set!");
    public final Symbol UNQUOTE = Symbol.symbol("unquote");
    public final Symbol UNQUOTE_SPLICING = Symbol.symbol("unquote-splicing");


    public void install(Object lib) {
            install(lib.getClass(), lib);
    }

    private void install(Class<?> clazz, Object lib) {
        Namespace ns = clazz.getAnnotation(Namespace.class);
        for (Method m : clazz.getDeclaredMethods()) {
            Primitive ann = m.getAnnotation(Primitive.class);
            if (ann != null) {
                Symbol sym = Symbol.symbol(
                    ns != null ? ns.value() : null,
                    ann.value().equals("") ? m.getName() : ann.value());
                env.define(sym, ann.isNative() ? new NativeMethod(m, lib) : new JavaMethod(m, lib));
            }
        }
        if (clazz.getSuperclass() != Object.class) {
            install(clazz.getSuperclass(), lib);
        }
    }

    public Environment env() {
        return env;
    }

    private Object quasiquote(Object x, Environment env) {
        if (StandardLibrary.car(x) == UNQUOTE || StandardLibrary.car(x) == UNQUOTE_SPLICING) {
            return eval(StandardLibrary.cadr(x), env);
        } else if (x instanceof Seq) {
            Pair head = new Pair(), tail = head;
            for (Object c = x; c instanceof Seq; c = StandardLibrary.cdr(c)) {
                Object o = quasiquote(StandardLibrary.car(c), env);
                if (StandardLibrary.caar(c) == UNQUOTE_SPLICING) {
                    for (Object s = o; s instanceof Seq; s = StandardLibrary.cdr(s)) {
                        tail.setRest(new Pair(StandardLibrary.car(s)));
                        tail = (Pair) StandardLibrary.cdr(tail);
                    }
                } else {
                    tail.setRest(new Pair(o));
                    tail = (Pair) StandardLibrary.cdr(tail);
                }
            }
            return StandardLibrary.cdr(head);
        } else {
            return x;
        }
    }

    public Object cond(Seq clauses, Environment env) {
        for (Seq cur = clauses; cur != null; cur = Utils.next(cur)) {
            Object cl = StandardLibrary.car(cur), c = StandardLibrary.car(cl);
            if (c == ELSE || Utils.isTrue(c = eval(c, env))) {
                return StandardLibrary.cdr(cl) == null ? c : eval(StandardLibrary.cadr(cl) == GE ?  StandardLibrary.caddr(cl)
                    : StandardLibrary.cddr(cl) != null ? StandardLibrary.cons(BEGIN, StandardLibrary.cdr(cl)) : StandardLibrary.cadr(cl), env);
            }
        }
        return false;
    }

    public Object eval(Object x, Environment env) {
        while (true) {
            try {
                if (x == null || x instanceof Number || x instanceof String || x instanceof Boolean
                    || x instanceof Keyword || x instanceof Character
                    || x.getClass().isArray() || x.getClass().isEnum()) {
                    return x;
                } else if (x instanceof Symbol) {
                    return env.lookup((Symbol) x);
                } else if (x instanceof LispMap) {
                    LispMap m = LispSMap.EMPTY;
                    for (Map.Entry e : (LispMap)x) {
                        m = m.assoc(eval(e.getKey(), env), eval(e.getValue(), env));
                    }
                    return m;
                } else if (x instanceof LispVector) {
                    LispVector v = LispVector.EMPTY;
                    for (Object o : (LispVector)x) {
                        v = v.append(eval(o,env));
                    }
                    return v;
                } else if (x instanceof Pair) {
                    Object f = StandardLibrary.car(x);
                    Seq n = Utils.next(x);
                    if (f == QUOTE) {
                        return StandardLibrary.car(n);
                    } else if (f == QUASIQUOTE) {
                        return quasiquote(StandardLibrary.car(n), env);
                    } else if (f == BEGIN) {
                        while (n != null && n.rest() != null) {
                            eval(n.first(), env);
                            n = Utils.next(n);
                        }
                        return n != null ? eval(n.first(), env) : null;
                    } else if (f == DEF) {
                        Object sym = StandardLibrary.car(n);
                        if (sym instanceof Symbol) {
                            return env.define((Symbol) sym, eval(StandardLibrary.cadr(n), env));
                        } else if (sym instanceof Seq) {
                            if (StandardLibrary.car(sym) instanceof Symbol) {
                                Closure c = new Closure(this, env, Utils.next(sym), Utils.next(n), false);
                                return env.define((Symbol) StandardLibrary.car(sym), c);
                            }
                        }
                        throw new LispException("Defined name must be a symbol.");
                    } else if (f == SET) {
                        Object sym = StandardLibrary.car(n);
                        if (sym instanceof Symbol) {
                            return env.set((Symbol) sym, eval(StandardLibrary.cadr(n), env));
                        } else {
                            throw new LispException("First argument in set! must be a symbol.");
                        }
                    } else if (f == FN || f == MACRO) {
                        Object args = StandardLibrary.car(n);
                        if (args == null || args instanceof Seq || args instanceof Symbol) {
                            return new Closure(this, env, args, Utils.next(n), f == MACRO);
                        } else {
                            throw new LispException("First argument of (lambda...) needs to be list of symbols or symbol.");
                        }
                    } else if (f == COND) {
                        return cond(n, env);
                    } else if (f instanceof Symbol && ((Symbol)f).getName().startsWith(".")) {
                        Object obj = eval(StandardLibrary.car(n), env);
                        if (obj != null) {
                            for (Method m : ObjectInspector.lookupMethods(obj.getClass(), ((Symbol) f).getName().substring(1), false)) {
                                if (m.getParameterTypes().length == StandardLibrary.length(n) - 1) {
                                    Object[] args = StandardLibrary.toArray(StandardLibrary.cdr(n));
                                    for (int i = 0; i < args.length; i++) {
                                        args[i] = eval(args[i],env);
                                    }
                                    return m.invoke(obj,args);
                                }
                            }
                        } else {
                            throw new LispException("Cannot call a method on ");
                        }

                    } else {
                        Object obj = eval(f, env);
                        if (!(obj instanceof Fn)) {
                            throw new LispException("Object " + obj + " is not callable.");
                        }
                        Fn fn = (Fn) obj;
                        if (fn.isMacro()) {
                            Object expansion = fn.apply(this, env, n);
                            Pair p = (Pair) x;
                            if (expansion instanceof Seq) {
                                p.setFirst(StandardLibrary.car(expansion));
                                p.setRest(StandardLibrary.cdr(expansion));
                            } else {
                                p.setFirst(BEGIN);
                                p.setRest(StandardLibrary.cons(expansion, null));
                            }
                        } else {
                            Pair head = new Pair(), tail = head;
                            for (Seq cur = n; cur != null; cur = Utils.next(cur)) {
                                Pair p = new Pair(eval(StandardLibrary.car(cur), env));
                                tail.setRest(p);
                                tail = p;
                            }
                            return fn.apply(this, env, Utils.next(head));
                        }
                    }
                } else {
                    throw new LispException("Cannot evaluate: " + x);
                }
            } catch (LispException e) {
                e.addEvalItem(x);
                throw e;
            } catch (Exception e) {
                LispException le = new LispException("Error evaluating expr", e);
                le.addEvalItem(x);
                throw le;
            }
        }
    }

    public Object eval(Object x) {
        return eval(x, env);
    }

    public Object evalStr(String s) {
        return eval(new Reader(s).read());
    }

    public Seq readScript(String path) {
        InputStream is = null;
        try {
            is = Utils.open(path);
            return new Reader(new File(path).getName(), is).readAll();
        } catch (Exception e) {
            throw new LispException("Cannot open script: " + path, e);
        } finally {
            if (is != null) {
                try {is.close();} catch (IOException e) { }
            }
        }
    }

    public Object evalScript(String path) {
        Object rslt = null;
        for (Seq seq = readScript(path); seq != null; seq = Utils.next(seq)) {
            rslt = eval(seq.first());
        }
        return rslt;
    }

}

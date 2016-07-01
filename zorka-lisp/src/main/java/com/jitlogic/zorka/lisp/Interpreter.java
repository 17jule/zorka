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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import static com.jitlogic.zorka.lisp.StandardLibrary.*;
import static com.jitlogic.zorka.lisp.Utils.*;

import static com.jitlogic.zorka.lisp.Symbol.symbol;

/**
 * LISP context object holds all data for particular instance of LISP:
 * - symbol dictionary;
 * - root environment (TBD);
 */
public class Interpreter {

    private Environment env = new Environment(null);

    public final Symbol BEGIN = symbol("begin");
    public final Symbol COND = symbol("cond");
    public final Symbol DEF = symbol("def");
    public final Symbol ELSE = symbol("else");
    public final Symbol GE = symbol("=>");
    public final Symbol FN = symbol("fn");
    public final Symbol MACRO = symbol("macro");
    public final Symbol QUOTE = symbol("quote");
    public final Symbol QUASIQUOTE = symbol("quasiquote");
    public final Symbol SET = symbol("set!");
    public final Symbol UNQUOTE = symbol("unquote");
    public final Symbol UNQUOTE_SPLICING = symbol("unquote-splicing");


    public void install(Object lib) {
            install(lib.getClass(), lib);
    }

    private void install(Class<?> clazz, Object lib) {
        Namespace ns = clazz.getAnnotation(Namespace.class);
        for (Method m : clazz.getDeclaredMethods()) {
            Primitive ann = m.getAnnotation(Primitive.class);
            if (ann != null) {
                Symbol sym = symbol(
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
        if (car(x) == UNQUOTE || car(x) == UNQUOTE_SPLICING) {
            return eval(cadr(x), env);
        } else if (x instanceof Seq) {
            Pair head = new Pair(), tail = head;
            for (Object c = x; c instanceof Seq; c = cdr(c)) {
                Object o = quasiquote(car(c), env);
                if (caar(c) == UNQUOTE_SPLICING) {
                    for (Object s = o; s instanceof Seq; s = cdr(s)) {
                        tail.setRest(new Pair(car(s)));
                        tail = (Pair) cdr(tail);
                    }
                } else {
                    tail.setRest(new Pair(o));
                    tail = (Pair) cdr(tail);
                }
            }
            return cdr(head);
        } else {
            return x;
        }
    }

    public Object cond(Seq clauses, Environment env) {
        for (Seq cur = clauses; cur != null; cur = next(cur)) {
            Object cl = car(cur), c = car(cl);
            if (c == ELSE || isTrue(c = eval(c, env))) {
                return cdr(cl) == null ? c : eval(cadr(cl) == GE ?  caddr(cl)
                    : cddr(cl) != null ? cons(BEGIN, cdr(cl)) : cadr(cl), env);
            }
        }
        return false;
    }

    public Object eval(Object x, Environment env) {
        while (true) {
            try {
                if (x == null || x instanceof Number || x instanceof String || x instanceof Boolean
                    || x instanceof Keyword || x instanceof Character || x.getClass().isArray() || x.getClass().isEnum()) {
                    return x;
                } else if (x instanceof Symbol) {
                    return env.lookup((Symbol) x);
                } else if (x instanceof Pair) {
                    Object f = car(x);
                    Seq n = next(x);
                    if (f == QUOTE) {
                        return car(n);
                    } else if (f == QUASIQUOTE) {
                        return quasiquote(car(n), env);
                    } else if (f == BEGIN) {
                        while (n != null && n.rest() != null) {
                            eval(n.first(), env);
                            n = next(n);
                        }
                        return n != null ? eval(n.first(), env) : null;
                    } else if (f == DEF) {
                        Object sym = car(n);
                        if (sym instanceof Symbol) {
                            return env.define((Symbol) sym, eval(cadr(n), env));
                        } else if (sym instanceof Seq) {
                            if (car(sym) instanceof Symbol) {
                                Closure c = new Closure(this, env, next(sym), next(n), false);
                                return env.define((Symbol) car(sym), c);
                            }
                        }
                        throw new LispException("Defined name must be a symbol.");
                    } else if (f == SET) {
                        Object sym = car(n);
                        if (sym instanceof Symbol) {
                            return env.set((Symbol) sym, eval(cadr(n), env));
                        } else {
                            throw new LispException("First argument in set! must be a symbol.");
                        }
                    } else if (f == FN || f == MACRO) {
                        Object args = car(n);
                        if (args == null || args instanceof Seq || args instanceof Symbol) {
                            return new Closure(this, env, args, next(n), f == MACRO);
                        } else {
                            throw new LispException("First argument of (lambda...) needs to be list of symbols or symbol.");
                        }
                    } else if (f == COND) {
                        return cond(n, env);
                    } else if (f instanceof Symbol && ((Symbol)f).getName().startsWith(".")) {
                        Object obj = eval(car(n), env);
                        if (obj != null) {
                            for (Method m : ObjectInspector.lookupMethods(obj.getClass(), ((Symbol) f).getName().substring(1), false)) {
                                if (m.getParameterTypes().length == length(n) - 1) {
                                    Object[] args = toArray(cdr(n));
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
                                p.setFirst(car(expansion));
                                p.setRest(cdr(expansion));
                            } else {
                                p.setFirst(BEGIN);
                                p.setRest(cons(expansion, null));
                            }
                        } else {
                            Pair head = new Pair(), tail = head;
                            for (Seq cur = n; cur != null; cur = next(cur)) {
                                Pair p = new Pair(eval(car(cur), env));
                                tail.setRest(p);
                                tail = p;
                            }
                            return fn.apply(this, env, next(head));
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
            is = open(path);
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
        for (Seq seq = readScript(path); seq != null; seq = next(seq)) {
            rslt = eval(seq.first());
        }
        return rslt;
    }

}

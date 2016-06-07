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

import java.io.InputStream;
import java.security.Key;
import java.util.*;

import static com.jitlogic.zorka.lisp.Utils.next;

public class StandardLibrary {

    private Interpreter ctx;

    public StandardLibrary(Interpreter ctx) {
        this.ctx = ctx;
    }

    // Implementation of basic primitives

    @Primitive("+")
    public static Number add(Number...nums) {

        if (nums.length == 0) {
            return 0;
        }

        Number rslt = nums[0];

        for (int i = 1; i < nums.length; i++) {
            if (rslt == null) {
                rslt = nums[i];
            } else if (nums[i] != null) {
                rslt = Utils.add(rslt, nums[i]);
            }
        }

        return rslt != null ? rslt : 0;
    }

    @Primitive("-")
    public static Number sub(Number...nums) {

        if (nums.length == 0) {
            return 0;
        }

        Number rslt = nums[0];

        for (int i = 1; i < nums.length; i++) {
            if (rslt == null) {
                rslt = nums[i];
            } else if (nums[i] != null) {
                rslt = Utils.sub(rslt, nums[i]);
            }
        }

        return rslt != null ? rslt : 0;
    }

    @Primitive("*")
    public static Number mul(Number...nums) {

        if (nums.length == 0) {
            return 1;
        }

        Number rslt = nums[0];

        for (int i = 1; i < nums.length; i++) {
            if (rslt == null) {
                rslt = nums[i];
            } else if (nums[i] != null) {
                rslt = Utils.mul(rslt, nums[i]);
            }
        }

        return rslt != null ? rslt : 0;
    }

    @Primitive("/")
    public static Number div(Number...nums) {

        if (nums.length == 0) {
            return 1;
        }

        Number rslt = null;

        for (Number num : nums) {
            if (rslt == null) {
                rslt = num;
            } else if (num != null) {
                rslt = Utils.div(rslt, num);
            }
        }

        return rslt != null ? rslt : 0;
    }

    @Primitive("=")
    public static boolean isEqualNum(Number...nums) {
        for (int i = 1; i < nums.length; i++) {
            if (Utils.cmp(nums[i-1],nums[i]) != 0) {
                return false;
            }
        }

        return true;
    }


    @Primitive("!=")
    public static boolean isNotEquals(Number...nums) {
        return !isEqualNum(nums);
    }

    @Primitive("<")
    public static boolean isLessThan(Number...nums) {
        for (int i = 1; i < nums.length; i++) {
            if (Utils.cmp(nums[i-1],nums[i]) >= 0) {
                return false;
            }
        }

        return true;
    }

    @Primitive("<=")
    public static boolean isLessOrEqual(Number...nums) {
        for (int i = 1; i < nums.length; i++) {
            if (Utils.cmp(nums[i-1],nums[i]) > 0) {
                return false;
            }
        }

        return true;
    }

    @Primitive(">")
    public static boolean isGreaterThan(Number...nums) {
        for (int i = 1; i < nums.length; i++) {
            if (Utils.cmp(nums[i-1],nums[i]) <= 0) {
                return false;
            }
        }

        return true;
    }

    @Primitive(">=")
    public static boolean isGreaterOrEqual(Number...nums) {
        for (int i = 1; i < nums.length; i++) {
            if (Utils.cmp(nums[i-1],nums[i]) < 0) {
                return false;
            }
        }

        return true;
    }

    @Primitive("_lazy-seq")
    public Object lazySeqNew(Fn fn) {
        return new LazySeq(ctx, fn);
    }

    @Primitive("abs")
    public static Number abs(Number n) {
        if (n != null) {
            Class cl = n.getClass();
            if (cl == Double.class) {
                return Math.abs(n.doubleValue());
            } else if (cl == Float.class) {
                return Math.abs(n.floatValue());
            } else if (cl == Long.class) {
                return Math.abs(n.longValue());
            } else {
                return Math.abs(n.intValue());
            }
        } else {
            return null;
        }
    }

    @Primitive("array-list")
    public static List arrayList(Object...objs) {
        List<Object> rslt = new ArrayList<Object>(objs.length);
        for (int i = 0; i < objs.length; i++) {
            rslt.add(objs[i]);
        }
        return rslt;
    }

    @Primitive("apply")
    public Object apply(Fn fn, Object...args) {
        if (args.length > 0) {
            Seq head = (Seq)args[args.length-1];
            for (int i = args.length-2; i >= 0; i--) {
                head = new Pair(args[i], head);
            }
            return fn.apply(ctx, ctx.env(), head);
        } else {
            return fn.apply(ctx, ctx.env(), null);
        }
    }

    @Primitive("atom?")
    public static boolean isAtom(Object o) { return !isList(o); }

    @Primitive("byte?")
    public static boolean isByte(Object o) { return o instanceof Byte; }

    @Primitive("car")
    public static Object car(Object o) { return o instanceof Seq ? ((Seq)o).first() : null; }

    @Primitive("cdr")
    public static Object cdr(Object o) { return o instanceof Seq ? ((Seq)o).rest() : null; }

    @Primitive("caar")
    public static Object caar(Object o) { return car(car(o)); }

    @Primitive("cadr")
    public static Object cadr(Object o) { return car(cdr(o)); }

    @Primitive("cdar")
    public static Object cdar(Object o) { return cdr(car(o)); }

    @Primitive("cddr")
    public static Object cddr(Object o) { return cdr(cdr(o)); }

    @Primitive("caaar")
    public static Object caaar(Object o) { return car(caar(o)); }

    @Primitive("caadr")
    public static Object caadr(Object o) { return car(cadr(o)); }

    @Primitive("cadar")
    public static Object cadar(Object o) { return car(cdar(o)); }

    @Primitive("caddr")
    public static Object caddr(Object o) { return car(cddr(o)); }

    @Primitive("cdaar")
    public static Object cdaar(Object o) { return cdr(caar(o)); }

    @Primitive("cdadr")
    public static Object cdadr(Object o) { return cdr(cadr(o)); }

    @Primitive("cddar")
    public static Object cddar(Object o) { return cdr(cdar(o)); }

    @Primitive("cdddr")
    public static Object cdddr(Object o) { return cdr(cddr(o)); }

    @Primitive("caaaar")
    public static Object caaaar(Object o) { return car(caaar(o)); }

    @Primitive("caaadr")
    public static Object caaadr(Object o) { return car(caadr(o)); }

    @Primitive("caadar")
    public static Object caadar(Object o) { return car(cadar(o)); }

    @Primitive("caaddr")
    public static Object caaddr(Object o) { return car(caddr(o)); }

    @Primitive("cadaar")
    public static Object cadaar(Object o) { return car(cdaar(o)); }

    @Primitive("cadadr")
    public static Object cadadr(Object o) { return car(cdadr(o)); }

    @Primitive("caddar")
    public static Object caddar(Object o) { return car(cddar(o)); }

    @Primitive("cadddr")
    public static Object cadddr(Object o) { return car(cdddr(o)); }

    @Primitive("cdaaar")
    public static Object cdaaar(Object o) { return cdr(caaar(o)); }

    @Primitive("cdaadr")
    public static Object cdaadr(Object o) { return cdr(caadr(o)); }

    @Primitive("cdadar")
    public static Object cdadar(Object o) { return cdr(cadar(o)); }

    @Primitive("cdaddr")
    public static Object cdaddr(Object o) { return cdr(caddr(o)); }

    @Primitive("cddaar")
    public static Object cddaar(Object o) { return cdr(cdaar(o)); }

    @Primitive("cddadr")
    public static Object cddadr(Object o) { return cdr(cdadr(o)); }

    @Primitive("cdddar")
    public static Object cdddar(Object o) { return cdr(cddar(o)); }

    @Primitive("cddddr")
    public static Object cddddr(Object o) { return cdr(cdddr(o)); }

    @Primitive(value = "call-with-current-continuation", isNative = true)
    public static Object callWithCurrentContinuation(Interpreter ctx, Environment env, Seq args) {
        if (car(args) instanceof Fn) {
            LispException jmp = new LispException();
            Continuation cont = new Continuation(jmp);
            try {
                return ((Fn)car(args)).apply(ctx, env, cons(cont, null));
            } catch (LispException e) {
                if (e == jmp) {
                    return cont.getValue();
                } else {
                    throw e;
                }
            }
        } else {
            throw new LispException("Expected lambda as an argument.");
        }
    }

    @Primitive("cons")
    public static Seq cons(Object obj, Object seq) { return new Pair(obj, seq); }

    @Primitive("dec")
    public static Number dec(Number n) {
        return Utils.sub(n, (byte)1);
    }

    @Primitive("eq?")
    public static boolean isSame(Object obj1, Object obj2) { return obj1 == obj2; }

    @Primitive("eqv?")
    public static boolean isEquivalent(Object obj1, Object obj2) {
        return obj1 == obj2 || (isAtom(obj1) && obj1.equals(obj2));
    }

    @Primitive("equal?")
    public static boolean isEqual(Object obj1, Object obj2) { return Utils.objEquals(obj1, obj2); }

    @Primitive("eval")
    public Object eval(Object x) { return ctx.eval(x); }

    @Primitive("even?")
    public static boolean isEven(Number n) { return isZero(modulo(n,2)); }

    @Primitive("exact?")
    public static boolean isExact(Object n) { return isInteger(n); }

    @Primitive("fn?")
    public static boolean isFn(Object o) {
        return o instanceof Fn;
    }

    @Primitive("get")
    public static Object get(Object...args) {
        Object rslt = ObjectInspector.get(args[0], args[1]);
        return rslt == null && args.length > 2 ? args[2] : rslt;
    }

    @Primitive("hash-map")
    public static Map hashMap(Object...objs) {

        if (objs.length % 2 != 0) {
            throw new LispException("Uneven number of arguments passed to hash-map function.");
        }

        Map<Object,Object> m = new HashMap<Object,Object>();

        for (int i = 1; i < objs.length; i += 2) {
            m.put(objs[i-1], objs[i]);
        }

        return m;
    }

    @Primitive("inc")
    public static Number inc(Number n) {
        return Utils.add(n, (byte)1);
    }

    @Primitive("inexact?")
    public static boolean isInexact(Object n) {
        return n instanceof Double || n instanceof Float;
    }

    @Primitive("int?")
    public static boolean isInt(Object o) { return o instanceof Integer; }

    @Primitive("integer?")
    public static boolean isInteger(Object o) { return isInt(o) || isLong(o) || isByte(o) || isShort(o); }

    @Primitive("keyword")
    public static Keyword keyword(String name) {
        return Keyword.keyword(name);
    }

    @Primitive("keyword?")
    public static boolean isKeyword(Object o) {
        return o instanceof Keyword;
    }

    @Primitive("length")
    public static int length(Seq lst) {
        int len = 0;
        for (Seq cur = lst; cur != null; cur = next(cur)) {
            len++;
        }
        return len;
    }

    @Primitive(value = "list", isNative = true)
    public static Seq list(Interpreter ctx, Environment env, Seq args) {
        return args;
    }

    @Primitive("list?")
    public static boolean isList(Object obj) {
        return obj == null || obj instanceof Seq;
    }

    @Primitive("long?")
    public static boolean isLong(Object o) { return o instanceof Long; }

    @Primitive("macroexpand")
    public Object macroexpand(Object x) {
        if (!(x instanceof Seq)) return x;
        Object o = ctx.eval(car(x));
        if (!(o instanceof Fn) || !((Fn)o).isMacro()) return x;
        return ((Fn)o).apply(ctx, ctx.env(), next(x));
    }

    @Primitive("max")
    public Number max(Number...nums) {
        if (nums.length == 0) {
            return null;
        }
        Number rslt = nums[0];

        for (int i = 1; i < nums.length; i++) {
            if (rslt == null) {
                rslt = nums[i];
            } else if (nums[i] != null && Utils.cmp(rslt,nums[i]) < 0) {
                rslt = nums[i];
            }
        }

        return rslt;
    }

    @Primitive("min")
    public Number min(Number...nums) {
        if (nums.length == 0) {
            return null;
        }
        Number rslt = nums[0];

        for (int i = 1; i < nums.length; i++) {
            if (rslt == null) {
                rslt = nums[i];
            } else if (nums[i] != null && Utils.cmp(rslt,nums[i]) > 0) {
                rslt = nums[i];
            }
        }

        return rslt;
    }

    @Primitive("modulo")
    public static Number modulo(Number n1, Number n2) {
        return n1 != null && n2 != null ? Utils.mod(n1, n2) : null;
    }

    @Primitive("negative?")
    public static boolean isNegative(Number n) { return Utils.cmp(n, 0) < 0; }

    @Primitive("not")
    public static boolean not(Object o) {
        return o == null || Boolean.FALSE.equals(o);
    }

    @Primitive("null?")
    public static boolean isNull(Object o) { return o == null; }

    @Primitive("number?")
    public static boolean isNumber(Object o) { return o instanceof Number;}

    @Primitive("odd?")
    public static boolean isOdd(Number n) { return !isZero(modulo(n,2)); }

    @Primitive("positive?")
    public static boolean isPositive(Number n) { return Utils.cmp(n, 0) > 0; }

    @Primitive("println")
    public static void println(Object...args) {
        StringBuilder sb = new StringBuilder();
        for (Object a : args) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(a);
        }
        System.out.println(sb.toString());
    }

    @Primitive("read")
    public static Object read(InputStream is) {
        return new Reader(is).read();
    }

    @Primitive("read-str")
    public static Object readStr(String s) {
        return new Reader(s).read();
    }

    @Primitive("seq")
    public static Seq seq(Object o) {
        if (o instanceof Seq) {
            return (Seq) o;
        } else if (o instanceof Iterable) {
            return new IterSeq(((Iterable) o).iterator());
        } else if (o instanceof Map) {
            return new IterSeq(((Map) o).entrySet().iterator());
        } else if (o instanceof Map.Entry) {
            Map.Entry e = (Map.Entry)o;
            return cons(e.getKey(), cons(e.getValue(), null));
        } else {
            return null;
        }
    }

    @Primitive("seq?")
    public static boolean isSeq(Object o) {
        return o instanceof Seq;
    }

    @Primitive("set-cdr")
    public static Object setCdr(Pair p, Object o) {
        p.setRest(o);
        return p;
    }

    @Primitive("short?")
    public static boolean isShort(Object o) { return o instanceof Short; }

    @Primitive(value = "str", isNative = true)
    public static String str(Seq args) {
        StringBuilder sb = new StringBuilder();
        for (Seq arg = args; arg != null; arg = next(arg)) {
            Object obj = car(arg);
            if (obj != null) {
                sb.append(obj.toString());
            } else {
                sb.append("nil");
            }
        }
        return sb.toString();
    }

    @Primitive("symbol")
    public static Symbol symbol(String name) {
        return Symbol.symbol(name);
    }

    @Primitive("symbol?")
    public static boolean isSymbol(Object o) {
        return o instanceof Symbol;
    }

    @Primitive("to-byte")
    public static byte toByte(Number n) { return n != null ? n.byteValue() : (byte)0; }

    @Primitive("to-short")
    public static short toShort(Number n) { return n != null ? n.shortValue() : (short)0; }

    @Primitive("to-int")
    public static int toInt(Number n) { return n != null ? n.intValue() : 0; }

    @Primitive("to-long")
    public static long toLong(Number n) { return n != null ? n.longValue() : 0L; }

    @Primitive("to-float")
    public static float toFloat(Number n) { return n != null ? n.floatValue() : (float)0.0; }

    @Primitive("to-double")
    public static double toDouble(Number n) { return n != null ? n.doubleValue() : 0.0; }

    @Primitive("tree-map")
    public static Map treeMap(Object...objs) {

        if (objs.length % 2 != 0) {
            throw new LispException("Uneven number of arguments passed to hash-map function.");
        }

        Map<Object,Object> m = new TreeMap<Object,Object>();

        for (int i = 1; i < objs.length; i += 2) {
            m.put(objs[i-1], objs[i]);
        }

        return m;
    }

    @Primitive("vector")
    public static List<Object> vector(Seq lst) {
        List<Object> vec = new ArrayList<Object>();
        for (Seq cur = lst; cur != null; cur = next(cur)) {
            vec.add(car(cur));
        }
        return vec;
    }

    @Primitive("zero?")
    public static boolean isZero(Number n) {
        return n != null && Utils.cmp(0, n) == 0;
    }
}

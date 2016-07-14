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

import java.io.*;
import java.util.*;

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


    @Primitive(value = "array-list", isNative = true)
    public static List arrayList(Interpreter ctx, Environment env, Seq args) {
        List<Object> rslt = new ArrayList<Object>(length(args));
        for (Seq seq = args; seq != null; seq = (Seq)cdr(seq)) {
            rslt.add(car(seq));
        }
        return rslt;
    }


    @Primitive
    public static LispMap assoc(LispMap m, Object k, Object v) {
        return m.assoc(k, v);
    }


    @Primitive("atom?")
    public static boolean isAtom(Object o) { return !isList(o); }


    @Primitive("byte?")
    public static boolean isByte(Object o) { return o instanceof Byte; }


    @Primitive
    public static Object car(Object o) { return o instanceof Seq ? ((Seq)o).first() : null; }


    @Primitive
    public static Object cdr(Object o) { return o instanceof Seq ? ((Seq)o).rest() : null; }


    @Primitive
    public static Object caar(Object o) { return car(car(o)); }


    @Primitive
    public static Object cadr(Object o) { return car(cdr(o)); }


    @Primitive
    public static Object cdar(Object o) { return cdr(car(o)); }


    @Primitive
    public static Object cddr(Object o) { return cdr(cdr(o)); }


    @Primitive
    public static Object caaar(Object o) { return car(caar(o)); }


    @Primitive
    public static Object caadr(Object o) { return car(cadr(o)); }


    @Primitive
    public static Object cadar(Object o) { return car(cdar(o)); }


    @Primitive
    public static Object caddr(Object o) { return car(cddr(o)); }


    @Primitive
    public static Object cdaar(Object o) { return cdr(caar(o)); }


    @Primitive
    public static Object cdadr(Object o) { return cdr(cadr(o)); }


    @Primitive
    public static Object cddar(Object o) { return cdr(cdar(o)); }


    @Primitive
    public static Object cdddr(Object o) { return cdr(cddr(o)); }


    @Primitive
    public static Object caaaar(Object o) { return car(caaar(o)); }


    @Primitive
    public static Object caaadr(Object o) { return car(caadr(o)); }


    @Primitive
    public static Object caadar(Object o) { return car(cadar(o)); }


    @Primitive
    public static Object caaddr(Object o) { return car(caddr(o)); }


    @Primitive
    public static Object cadaar(Object o) { return car(cdaar(o)); }


    @Primitive
    public static Object cadadr(Object o) { return car(cdadr(o)); }


    @Primitive
    public static Object caddar(Object o) { return car(cddar(o)); }


    @Primitive
    public static Object cadddr(Object o) { return car(cdddr(o)); }


    @Primitive
    public static Object cdaaar(Object o) { return cdr(caaar(o)); }


    @Primitive
    public static Object cdaadr(Object o) { return cdr(caadr(o)); }


    @Primitive
    public static Object cdadar(Object o) { return cdr(cadar(o)); }


    @Primitive
    public static Object cdaddr(Object o) { return cdr(caddr(o)); }


    @Primitive
    public static Object cddaar(Object o) { return cdr(cdaar(o)); }


    @Primitive
    public static Object cddadr(Object o) { return cdr(cdadr(o)); }


    @Primitive
    public static Object cdddar(Object o) { return cdr(cddar(o)); }


    @Primitive
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


    @Primitive
    public static LispMap dissoc(LispMap m, Object k) {
        return m.dissoc(k);
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


    @Primitive(value = "get", isNative = true)
    public static Object get(Interpreter ctx, Environment env, Seq args) {
        Object obj = car(args), k = cadr(args), dv = caddr(args);
        if (obj instanceof LispMap) {
            return ((LispMap)obj).get(k, dv);
        } else {
            Object rslt = ObjectInspector.get(obj, k);
            return rslt == null ? dv : rslt;
        }
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


    @Primitive(isNative = true)
    public static LispMap lispMap(Interpreter ctx, Environment env, Seq args) {

        if (length(args) % 2 != 0) {
            throw new LispException("Uneven number of arguments.");
        }

        LispMap m = new LispSMap(LispMap.MUTABLE);

        for (Seq seq = args; seq != null; seq = (Seq)cddr(seq)) {
            m = m.assoc(car(seq), cadr(seq));
        }

        m.setFlags(0);
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
        for (Seq cur = lst; cur != null; cur = Utils.next(cur)) {
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
        return ((Fn)o).apply(ctx, ctx.env(), Utils.next(x));
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


    @Primitive
    public static LispMap merge(LispMap...maps) {
        LispMap rslt = new LispSMap(LispMap.MUTABLE);

        boolean mutable = false;

        for (LispMap m : maps) {
            if (0 != (m.getFlags() & LispMap.MUTABLE)) {
                mutable = true;
            }
            for (Map.Entry e : m) {
                rslt = rslt.assoc(e.getKey(), e.getValue());
            }
        }

        rslt.setFlags(mutable ? LispMap.MUTABLE : 0);

        return rslt;
    }


    @Primitive("merge-recursive")
    public static LispMap mergeRecursive(LispMap...maps) {
        LispMap rslt = new LispSMap(LispMap.MUTABLE);

        boolean mutable = true;

        for (LispMap m : maps) {
            if (0 == (m.getFlags() & LispMap.MUTABLE)) {
                mutable = false;
            }
            for (Map.Entry e : m) {
                Object v = rslt.get(e.getKey());
                if (v instanceof LispMap && e.getValue() instanceof LispMap) {
                    rslt = rslt.assoc(e.getKey(), mergeRecursive(LispSMap.EMPTY, (LispMap)v, (LispMap)e.getValue()));
                } else {
                    rslt = rslt.assoc(e.getKey(), e.getValue());
                }
            }
        }

        rslt.setFlags(mutable ? LispMap.MUTABLE : 0);

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


    @Primitive
    public static String name(Object obj) {
        if (obj instanceof Symbol) {
            return ((Symbol)obj).getName();
        }
        throw new LispException("Cannot determine name of object " + obj);
    }


    @Primitive("negative?")
    public static boolean isNegative(Number n) { return Utils.cmp(n, 0) < 0; }


    @Primitive(value = "new-map", isNative = true)
    public static LispMap newMap(Interpreter ctx, Environment env, Seq args) {
        int len = length(args);
        if (len % 2 != 0) {
            throw new LispException("Uneven number for arguments when constructing map.");
        }
        if (len <= 6) {
            return new LispSMap(LispSMap.MUTABLE, args);
        } else {
            throw new LispException("Big maps not supported (yet).");
        }
    }


    @Primitive
    public static boolean not(Object o) {
        return o == null || Boolean.FALSE.equals(o);
    }

    @Primitive
    public static Object nth(LispVector vec, Integer i, Object...args) {
        return vec.get(i, args.length > 0 ? args[0] : null);
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
        return new com.jitlogic.zorka.lisp.Reader(is).read();
    }


    @Primitive("read-str")
    public static Object readStr(String s) {
        return new com.jitlogic.zorka.lisp.Reader(s).read();
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
        for (Seq arg = args; arg != null; arg = Utils.next(arg)) {
            Object obj = car(arg);
            if (obj != null) {
                sb.append(obj.toString());
            } else {
                sb.append("nil");
            }
        }
        return sb.toString();
    }


    @Primitive
    public static String slurp(String path) {
        InputStream is = null;
        try {
            if (path.startsWith("classpath:")) {
                is = StandardLibrary.class.getResourceAsStream(path.substring(10));
            } else if (path.startsWith("file:")) {
                is = new FileInputStream(path.substring(5));
            } else {
                is = new FileInputStream(path);
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int len;
            while (-1 != (len = is.read(buf))) {
                bos.write(buf, 0, len);
            }
            return new String(bos.toByteArray());
        } catch (IOException e) {
            throw new LispException("Cannot open file: " + path, e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {

                }
            }
        }
    }


    @Primitive
    public static void spit(String path, String content) {
        OutputStream os = null;
        try {
            os = new FileOutputStream(path);
            os.write(content.getBytes());
        } catch (IOException e) {
            throw new LispException("Cannot open file: " + path, e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                }
            }
        }
    }


    @Primitive("symbol")
    public static Symbol symbol(String name) {
        return Symbol.symbol(name);
    }


    @Primitive("symbol?")
    public static boolean isSymbol(Object o) {
        return o instanceof Symbol;
    }


    @Primitive("to-array")
    public static Object[] toArray(Object sobj) {
        Seq seq = seq(sobj);
        if (seq != null) {
            Object[] obj = new Object[length(seq)];
            for (int i = 0; i < obj.length; i++,seq= Utils.next(seq)) {
                obj[i] = car(seq);
            }
            return obj;
        } else {
            return new Object[0];
        }
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


    @Primitive("vec")
    public static LispVector vec(Seq lst) {
        LispVector vec = new LispVector(Associative.MUTABLE);
        for (Seq cur = lst; cur != null; cur = Utils.next(cur)) {
            vec = vec.append(car(cur));
        }
        vec.setFlags(0);
        return vec;
    }


    @Primitive("vector?")
    public static boolean isVector(Object obj) {
        return obj instanceof LispVector;
    }


    @Primitive("zero?")
    public static boolean isZero(Number n) {
        return n != null && Utils.cmp(0, n) == 0;
    }
}

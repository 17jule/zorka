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

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import static com.jitlogic.zorka.lisp.StandardLibrary.car;
import static com.jitlogic.zorka.lisp.StandardLibrary.cdr;

public class Utils {

    public static boolean objEquals(Object o1, Object o2) {
        return (o1 == o2) || (o1 != null && o1.equals(o2));
    }

    public static boolean lstEquals(Seq p1, Seq p2) {
        while (p1 != null && p2 != null) {
            if (objEquals(p1.first(), p2.first())) {
                p1 = next(p1);
                p2 = next(p2);
            } else {
                return false;
            }
        }
        return p1 == null && p2 == null;
    }

    public static Seq lstReverse(Seq lst) {
        Seq head = null;

        for (Seq p = lst; p != null; p = next(p)) {
            head = new Pair(p.first(), head);
        }

        return head;
    }

    public static String strSeq(Seq seq) {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (Object c = seq; c != null; c = cdr(c)) {
            if (c != seq) {
                sb.append(' ');
            }
            if (!(c instanceof Seq)) {
                sb.append(" . ");
                sb.append(c);
            } else if (car(c) != null) {
                sb.append(car(c));
            } else {
                sb.append("nil");
            }
        }
        sb.append(')');
        return sb.toString();
    }

    public static int seqHash(Seq seq) {
        int rslt = 31;

        for (Seq c = seq; c != null; c = next(c)) {
            if (c.first() != null) {
                rslt ^= c.first().hashCode();
            }
        }

        return rslt;
    }



    public static InputStream open(String path) {
        try {
            InputStream is;
            if ("file:".equals(path)) {
                is = new FileInputStream(path.substring(5));
            } else if ("classpath:".equals(path)) {
                is = Utils.class.getResourceAsStream(path.substring(10));
            } else if (new File(path).isFile()) {
                is = new FileInputStream(path);
            } else {
                is = Utils.class.getResourceAsStream(path);
            }
            return is;
        } catch (IOException e) {
            throw new LispException("Cannot open script file: " + path, e);
        }
    }



    public static Number add(Number n1, Number n2) {
        Class c1 = n1.getClass(), c2 = n2.getClass();
        if (c1 == Double.class || c2 == Double.class) {
            return n1.doubleValue() + n2.doubleValue();
        } else if (c1 == Float.class || c2 == Float.class) {
            return n1.floatValue() + n2.floatValue();
        } else if (c1 == Long.class || c2 == Long.class) {
            return n1.longValue() + n2.longValue();
        } else if (c1 == Integer.class || c2 == Integer.class) {
            return n1.intValue() + n2.intValue();
        } else if (c1 == Short.class || c2 == Short.class) {
            return (short)(n1.shortValue() + n2.shortValue());
        } else if (c1 == Byte.class || c2 == Byte.class) {
            return (byte)(n1.byteValue() + n2.byteValue());
        } else {
            return null;
        }
    }

    public static Number sub(Number n1, Number n2) {
        Class c1 = n1.getClass(), c2 = n2.getClass();
        if (c1 == Double.class || c2 == Double.class) {
            return n1.doubleValue() - n2.doubleValue();
        } else if (c1 == Float.class || c2 == Float.class) {
            return n1.floatValue() - n2.floatValue();
        } else if (c1 == Long.class || c2 == Long.class) {
            return n1.longValue() - n2.longValue();
        } else if (c1 == Integer.class || c2 == Integer.class) {
            return n1.intValue() - n2.intValue();
        } else if (c1 == Short.class || c2 == Short.class) {
            return (short)(n1.shortValue() - n2.shortValue());
        } else if (c1 == Byte.class || c2 == Byte.class) {
            return (byte)(n1.byteValue() - n2.byteValue());
        } else {
            return null;
        }
    }

    public static Number mul(Number n1, Number n2) {
        Class c1 = n1.getClass(), c2 = n2.getClass();
        if (c1 == Double.class || c2 == Double.class) {
            return n1.doubleValue() * n2.doubleValue();
        } else if (c1 == Float.class || c2 == Float.class) {
            return n1.floatValue() * n2.floatValue();
        } else if (c1 == Long.class || c2 == Long.class) {
            return n1.longValue() * n2.longValue();
        } else if (c1 == Integer.class || c2 == Integer.class) {
            return n1.intValue() * n2.intValue();
        } else if (c1 == Short.class || c2 == Short.class) {
            return (short)(n1.shortValue() * n2.shortValue());
        } else if (c1 == Byte.class || c2 == Byte.class) {
            return (byte)(n1.byteValue() * n2.byteValue());
        } else {
            return null;
        }
    }

    public static Number div(Number n1, Number n2) {
        Class c1 = n1.getClass(), c2 = n2.getClass();
        if (c1 == Double.class || c2 == Double.class) {
            return n1.doubleValue() / n2.doubleValue();
        } else if (c1 == Float.class || c2 == Float.class) {
            return n1.floatValue() / n2.floatValue();
        } else if (c1 == Long.class || c2 == Long.class) {
            return n1.longValue() / n2.longValue();
        } else if (c1 == Integer.class || c2 == Integer.class) {
            return n1.intValue() / n2.intValue();
        } else if (c1 == Short.class || c2 == Short.class) {
            return (short)(n1.shortValue() / n2.shortValue());
        } else if (c1 == Byte.class || c2 == Byte.class) {
            return (byte)(n1.byteValue() / n2.byteValue());
        } else {
            return null;
        }
    }

    public static int cmp(Number n1, Number n2) {
        Class c1 = n1.getClass(), c2 = n2.getClass();

        if (c1 == Double.class || c2 == Double.class || c1 == Float.class || c2 == Float.class) {
            double d1 = n1.doubleValue();
            double d2 = n2.doubleValue();
            return d1 == d2 ? 0 : d1 < d2 ? -1 : 1;
        } else  {
            long l1 = n1.longValue();
            long l2 = n2.longValue();
            return l1 == l2 ? 0 : l1 < l2 ? -1 : 1;
        }
    }

    public static Number mod(Number n1, Number n2) {
        Class c1 = n1.getClass(), c2 = n2.getClass();
        if (c1 == Double.class || c2 == Double.class) {
            return n1.doubleValue() % n2.doubleValue();
        } else if (c1 == Float.class || c2 == Float.class) {
            return n1.floatValue() % n2.floatValue();
        } else if (c1 == Long.class || c2 == Long.class) {
            return n1.longValue() % n2.longValue();
        } else if (c1 == Integer.class || c2 == Integer.class) {
            return n1.intValue() % n2.intValue();
        } else if (c1 == Short.class || c2 == Short.class) {
            return (short)(n1.shortValue() % n2.shortValue());
        } else if (c1 == Byte.class || c2 == Byte.class) {
            return (byte)(n1.byteValue() % n2.byteValue());
        } else {
            return null;
        }

    }

    /**
     * Tries to coerce a value to a specific (simple) data type.
     *
     * @param val value to be coerced (converted)
     * @param c   destination type class
     * @return coerced value
     */
    public static Object coerce(Object val, Class<?> c) {

        if (val == null || c == null) {
            return null;
        } else if (val.getClass() == c) {
            return val;
        } else if (c == String.class) {
            return castString(val);
        } else if (c == Boolean.class || c == Boolean.TYPE) {
            return coerceBool(val);
        } else if (c == Long.class || c == Long.TYPE) {
            return castLong(val);
        } else if (c == Integer.class || c == Integer.TYPE) {
            return castInteger(val);
        } else if (c == Double.class || c == Double.TYPE) {
            return castDouble(val);
        } else if (c == Short.class || c == Short.TYPE) {
            return castShort(val);
        } else if (c == Float.class || c == Float.TYPE) {
            return castFloat(val);
        }

        return null;
    }

    public static String castString(Object val) {
        try {
            return val != null ? val.toString() : "null";
        } catch (Exception e) {
            return "<ERR: " + e.getMessage() + ">";
        }
    }

    private static float castFloat(Object val) {
        return (val instanceof String)
            ? Float.parseFloat(val.toString().trim())
            : ((Number) val).floatValue();
    }

    private static short castShort(Object val) {
        return (val instanceof String)
            ? Short.parseShort(val.toString().trim())
            : ((Number) val).shortValue();
    }

    private static double castDouble(Object val) {
        return (val instanceof String)
            ? Double.parseDouble(val.toString().trim())
            : ((Number) val).doubleValue();
    }

    private static int castInteger(Object val) {
        return (val instanceof String)
            ? Integer.parseInt(val.toString().trim())
            : ((Number) val).intValue();
    }

    private static long castLong(Object val) {
        return (val instanceof String)
            ? Long.parseLong(val.toString().trim())
            : ((Number) val).longValue();
    }

    /**
     * Coerces any value to boolean.
     *
     * @param val value to be coerced
     * @return false if value is null or boolean false, true otherwise
     */
    public static boolean coerceBool(Object val) {
        return !(val == null || val.equals(false));
    }

    public static boolean isTrue(Object obj) {
        return obj != null && !Boolean.FALSE.equals(obj);
    }


    // TODO to be removed
    public static Seq next(Object obj) {
        Object rslt = StandardLibrary.cdr(obj);
        return rslt instanceof Seq ? (Seq)rslt : null;
    }


    /**
     * Returns string that contains of all elements of passed collection joined together
     *
     * @param sep separator (inserted between values)
     * @param col collection of values to concatenate
     * @return concatenated string
     */
    public static String join(String sep, Collection<?> col) {
        StringBuilder sb = new StringBuilder();

        for (Object val : col) {
            if (sb.length() > 0) sb.append(sep);
            sb.append(castString(val));
        }

        return sb.toString();
    }


    /**
     * Returns string that contains of all elements of passed (vararg) array joined together
     *
     * @param sep  separator (inserted between values)
     * @param vals array of values
     * @return concatenated string
     */
    public static String join(String sep, Object... vals) {
        StringBuilder sb = new StringBuilder();

        for (Object val : vals) {
            if (sb.length() > 0) sb.append(sep);
            sb.append(castString(val));
        }

        return sb.toString();
    }

    /**
     * Clones array of bytes. Implemented by hand as JDK 1.5 does not have such method.
     *
     * @param src source array
     * @return copied array
     */
    public static byte[] copyArray(byte[] src) {
        if (src == null) {
            return null;
        }

        byte[] dst = new byte[src.length];
        System.arraycopy(src, 0, dst, 0, src.length);
        return dst;
    }


    /**
     * Clones array of long integers. Implemented by hand as JDK 1.5 does not have such method.
     *
     * @param src source array
     * @return copied array
     */
    public static long[] copyArray(long[] src) {
        if (src == null) {
            return null;
        }

        long[] dst = new long[src.length];
        System.arraycopy(src, 0, dst, 0, src.length);
        return dst;
    }


    /**
     * Clones array of objects of type T
     *
     * @param src source array
     * @param <T> type of array items
     * @return copied array
     */
    public static <T> T[] copyArray(T[] src) {
        if (src == null) {
            return null;
        }

        Class<?> arrayType = src.getClass().getComponentType();
        T[] dst = (T[]) java.lang.reflect.Array.newInstance(arrayType, src.length);
        System.arraycopy(src, 0, dst, 0, src.length);

        return dst;
    }


    /**
     * This is useful to create a map of object in declarative way. Key-value pairs
     * are passed as arguments to this method, so call will look like this:
     * ZorkaUtil.map(k1, v1, k2, v2, ...)
     *
     * @param data keys and values (in pairs)
     * @param <K>  type of keys
     * @param <V>  type of values
     * @return mutable map
     */
    public static <K, V> HashMap<K, V> map(Object... data) {
        HashMap<K, V> map = new HashMap<K, V>(data.length + 2);

        for (int i = 1; i < data.length; i += 2) {
            map.put((K) data[i - 1], (V) data[i]);
        }

        return map;
    }

    public static <T> Iterable<T> iterable(final Seq seq) {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return new Seq.SeqIterator<T>(seq);
            }
        };
    }

    public static LispMap lispMap(Object...objs) {
        if (objs.length % 2 != 0) {
            throw new LispException("Uneven number of arguments.");
        }
        LispMap m = LispSMap.EMPTY;
        for (int i = 0; i < objs.length; i += 2) {
            m = m.assoc(objs[i], objs[i+1]);
        }
        return m;
    }

}

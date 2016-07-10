/**
 * Copyright 2012-2016 Rafal Lewczuk <rafal.lewczuk@jitlogic.com>
 *
 * ZORKA is free software. You can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * ZORKA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ZORKA. If not, see <http://www.gnu.org/licenses/>.
 */

package com.jitlogic.zorka.util;

import com.jitlogic.zorka.lisp.*;
import com.jitlogic.zorka.stats.ZorkaStats;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This utility class contains tools for introspection of various types of objects
 * and string substutution with fields of those objects.
 *
 * @author rafal.lewczuk@jitlogic.com
 */
public final class ObjectInspector {

    private static final ZorkaLog log = ZorkaLogger.getLog(ObjectInspector.class);

    /**
     * Special attribute name that will extract stack trace from throwable objects.
     */
    public static final String STACK_TRACE_KEY = "printStackTrace";

    /**
     * Private constructor to block instantiation of utility class.
     */
    private ObjectInspector() {
    }

    /**
     * Gets a chain of attributes from an object. That is, gets first attribute from obj, then
     * gets second attribute from obtained object, then gets third attribute from second obtained object etc.
     *
     * @param obj  source object
     * @param keys chain of attribute names
     * @param <T>  result type
     * @return final result
     */
    public static <T> T get(Object obj, Object... keys) {
        Object cur = obj;

        for (Object key : keys) {
            cur = getAttr(cur, key);
        }

        return (T) cur;
    }

    /**
     * Gets (logical) attribute from an object. Exact semantics may differ
     * depending on type of object. If object is an class, get() will look
     * in its static methods/fields. Instance metods or fields will be used
     * otherwise.
     *
     * @param obj object subjected
     * @param key attribute identified (name, index, etc. - depending on object type)
     * @return attribute value or null if no matching attribute has been found
     */
    private static Object getAttr(Object obj, Object key) {
        if (obj == null) {
            return null;
        }

        // TODO refactoring of this method (badly) needed
        Class<?> clazz = obj.getClass();

        if (key instanceof String && key.toString().endsWith("()")) {
            // Explicit method call for attributes ending with '()'
            String name = key.toString();
            name = name.substring(0, name.length() - 2);
            Method method = lookupMethod(clazz, name);

            if (method == null && obj instanceof Class) {
                method = lookupMethod((Class) obj, name);
            }

            return fetchViaMethod(obj, method);
        }

        if (key instanceof String && key.toString().startsWith(".")) {
            // Explicit field accesses for attributes starting with '.'
            return fetchFieldVal(obj, key.toString().substring(1));
        }

        if (obj instanceof Throwable && STACK_TRACE_KEY.equals(key)) {
            Writer rslt = new StringWriter(512);
            ((Throwable) obj).printStackTrace(new PrintWriter(rslt));
            return rslt.toString();
        }

        if (obj instanceof Map<?, ?>) {
            return ((Map<?, ?>) obj).get(key);
        } else if (obj instanceof List<?>) {
            Integer idx = (Integer) ZorkaUtil.coerce(key, Integer.class);
            return idx != null ? ((List<?>) obj).get(idx) : null;
        } else if (obj.getClass().isArray()) {
            return inspectArray(obj, key);
        } else if (obj instanceof CompositeData) {
            return ((CompositeData) obj).get("" + key);
        } else if (obj instanceof TabularData) {
            String[] keys = key.toString().split("\\,");
            obj = ((TabularData) obj).get(keys);
        } else if (obj instanceof ZorkaStats) {
            return ((ZorkaStats) obj).getStatistic(key.toString());
        } else if (ZorkaUtil.instanceOf(obj.getClass(), "javax.management.j2ee.statistics.Stats")) {
            try {
                Method m = obj.getClass().getMethod("getStatistic", String.class);
                if (m != null) {
                    return m.invoke(obj, key);
                }
            } catch (Exception e) {
                return "Error invoking getStatistic('" + key + "'): " + e.getMessage();
            }
        } else if (obj instanceof JmxObject) {
            return ((JmxObject) obj).get(key);
        }

        if (key instanceof String) {
            String name = (String) key;

            Method method = lookupMethod(clazz, "get" + name.substring(0, 1).toUpperCase() + name.substring(1));

            if (method == null && obj instanceof Class) {
                method = lookupMethod((Class) obj, "get" + name.substring(0, 1).toUpperCase() + name.substring(1));
            }

            if (method == null) {
                method = lookupMethod(clazz, "is" + name.substring(0, 1).toUpperCase() + name.substring(1));
            }

            if (method == null && obj instanceof Class) {
                method = lookupMethod((Class) obj, "is" + name.substring(0, 1).toUpperCase() + name.substring(1));
            }

            if (method == null) {
                method = lookupMethod(clazz, name);
            }

            if (method == null && obj instanceof Class) {
                method = lookupMethod((Class) obj, name);
            }

            if (method != null) {
                return fetchViaMethod(obj, method);
            }

            return fetchFieldVal(obj, name);
        }

        return null;
    }

    private static Object inspectArray(Object obj, Object key) {
        if (obj instanceof Object[]) {
            if ("length".equals(key)) {
                return ((Object[]) obj).length;
            }
            Integer idx = (Integer) ZorkaUtil.coerce(key, Integer.class);
            return idx != null ? ((Object[]) obj)[idx] : null;
        } else if (obj instanceof byte[]) {
            if ("length".equals(key)) {
                return ((byte[]) obj).length;
            }
            Integer idx = (Integer) ZorkaUtil.coerce(key, Integer.class);
            return idx != null ? ((byte[]) obj)[idx] : null;
        } else if (obj instanceof int[]) {
            if ("length".equals(key)) {
                return ((int[]) obj).length;
            }
            Integer idx = (Integer) ZorkaUtil.coerce(key, Integer.class);
            return idx != null ? ((int[]) obj)[idx] : null;
        } else if (obj instanceof long[]) {
            if ("length".equals(key)) {
                return ((long[]) obj).length;
            }
            Integer idx = (Integer) ZorkaUtil.coerce(key, Integer.class);
            return idx != null ? ((long[]) obj)[idx] : null;
        } else if (obj instanceof double[]) {
            if ("length".equals(key)) {
                return ((double[]) obj).length;
            }
            Integer idx = (Integer) ZorkaUtil.coerce(key, Integer.class);
            return idx != null ? ((double[]) obj)[idx] : null;
        } else if (obj instanceof float[]) {
            if ("length".equals(key)) {
                return ((float[]) obj).length;
            }
            Integer idx = (Integer) ZorkaUtil.coerce(key, Integer.class);
            return idx != null ? ((float[]) obj)[idx] : null;
        } else if (obj instanceof char[]) {
            if ("length".equals(key)) {
                return ((char[]) obj).length;
            }
            Integer idx = (Integer) ZorkaUtil.coerce(key, Integer.class);
            return idx != null ? ((char[]) obj)[idx] : null;
        } else if (obj instanceof short[]) {
            if ("length".equals(key)) {
                return ((short[]) obj).length;
            }
            Integer idx = (Integer) ZorkaUtil.coerce(key, Integer.class);
            return idx != null ? ((short[]) obj)[idx] : null;
        }
        return null;
    }


    /**
     * Lists attributes of an object. Depending on object type,
     *
     * @param obj source object
     * @return list of attributes (can be used to obtain their values)
     */
    public static List<?> list(Object obj) {
        List<String> lst = new ArrayList<String>();
        if (obj instanceof Map) {
            for (Object key : ((Map<?, ?>) obj).keySet()) {
                lst.add(key.toString());
            }
        } else if (obj instanceof List<?>) {
            int len = ((List) obj).size();
            List<Integer> ret = new ArrayList<Integer>(len + 2);
            for (int i = 0; i < len; i++) {
                ret.add(i);
            }
            return ret;
        } else if (obj.getClass().isArray()) {
            int len = ((Object[]) obj).length;
            List<Integer> ret = new ArrayList<Integer>(len + 2);
            for (int i = 0; i < len; i++) {
                ret.add(i);
            }
            return ret;
        } else if (obj instanceof CompositeData) {
            for (Object s : ((CompositeData) obj).getCompositeType().keySet()) {
                lst.add(s.toString());
            }
        } else if (obj instanceof TabularData) {
            for (Object k : ((TabularData) obj).keySet()) {
                lst.add(ZorkaUtil.join(",", (Collection<?>) k));
            }
        } else if (obj instanceof ZorkaStats) {
            lst = Arrays.asList(((ZorkaStats) obj).getStatisticNames());
        } else if (ZorkaUtil.instanceOf(obj.getClass(), "javax.management.j2ee.statistics.Stats")) {
            try {
                Method m = obj.getClass().getMethod("getStatisticNames");
                if (m != null) {
                    return Arrays.asList((Object[]) m.invoke(obj));
                }
            } catch (Exception e) {
                return new ArrayList<String>(1);
            }
        } else if (obj instanceof JmxObject) {
            try {
                MBeanInfo mbi = ((JmxObject) obj).getConn().getMBeanInfo(((JmxObject) obj).getName());
                for (MBeanAttributeInfo mba : mbi.getAttributes()) {
                    lst.add(mba.getName());
                }
            } catch (Exception e) {
                return new ArrayList<String>(1);
            }
        }

        Collections.sort(lst);

        return lst;
    }


    /**
     * Fetches attribute value by calling getter method from non-public class.
     *
     * @param obj    source object
     * @param method target method (method object)
     * @return result of calling target method
     */
    private static Object fetchViaNonPublicMethod(Object obj, Method method) {
        Object ret = null;
        synchronized (method) {
            method.setAccessible(true);
            try {
                if (Modifier.isStatic(method.getModifiers())) {
                    obj = obj.getClass();
                }
                ret = method.invoke(obj);
            } catch (Exception e) {
                log.error(ZorkaLogger.ZSP_ERRORS, "Method '" + method.getName() + "' invocation failed", e);
            } finally {
                method.setAccessible(false);
            }
        }
        return ret;
    }


    /**
     * Fetches attribute value by calling getter method.
     *
     * @param obj    source object
     * @param method target method (method object)
     * @return result of calling target method
     */
    private static Object fetchViaMethod(Object obj, Method method) {
        if (method != null) {
            if (!method.isAccessible()) {
                return fetchViaNonPublicMethod(obj, method);
            }
            try {
                if (Modifier.isStatic(method.getModifiers())) {
                    obj = obj.getClass();
                }
                Object ret = method.invoke(obj);
                return ret;
            } catch (Exception e) {
                log.error(ZorkaLogger.ZSP_ERRORS, "Method '" + method.getName() + "' invocation failed", e);
                return null;
            }
        }
        return null;
    }


    /**
     * Fetches attribute value by accessing field directly (and unlocking it
     * for a moment if this is private field).
     *
     * @param obj  source object
     * @param name field name
     * @return obtained value
     */
    private static Object fetchFieldVal(Object obj, String name) {
        try {
            Class<?> clazz = obj.getClass();
            name = name.startsWith(".") ? name.substring(1) : name;
            Field field = lookupField(clazz, name);
            if (field == null && obj instanceof Class) {
                field = lookupField((Class) obj, name);
            }
            if (field == null) {
                return null;
            }
            boolean accessible = field.isAccessible();
            if (!accessible) field.setAccessible(true);
            Object ret = field.get(obj);
            if (!accessible) field.setAccessible(accessible);
            return ret;
        } catch (Exception e) {
            return null;
        }
    }

    public static Class<?> findClass(String name) {
        Class<?> c = null;
        try {
            c = Class.forName(name);
        } catch (ClassNotFoundException ignored) { }

        try {
            if (c == null && !name.contains(".")) {
                c = Class.forName("java.lang." + name);
            }
        } catch (ClassNotFoundException ignored) { }

        return c;
    }




    /**
     * Lookf for field of given name.
     *
     * @param clazz class of inspected object
     * @param name  field name
     * @return field object of null if no such field exists
     */
    public static Field lookupField(Class<?> clazz, String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            if (field != null) {
                return field;
            } else if (clazz.getSuperclass() != Object.class && clazz.getSuperclass() != null) {
                return lookupField(clazz.getSuperclass(), name);
            } else {
                return null;
            }
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() != Object.class && clazz.getSuperclass() != null) {
                return lookupField(clazz.getSuperclass(), name);
            } else {
                return null;
            }
        }
    }


    /**
     * Looks for method identified with given name. It also recursively inspects all
     * interfaces that given class implements.
     *
     * @param clazz class of inspected object
     * @param name  method name
     * @return method object of null if no such method exists
     */
    public static Method lookupMethod(Class<?> clazz, String name) {
        try {
            return name != null ? clazz.getMethod(name) : null;
        } catch (NoSuchMethodException e) {
            for (Class<?> icl : clazz.getInterfaces()) {
                Method m = lookupMethod(icl, name);
                if (m != null) {
                    return m;
                }
            }
            Class<?> mcl = clazz;
            while ((mcl = mcl.getSuperclass()) != null && mcl != clazz) {
                Method m = lookupMethod(mcl, name);
                if (m != null) {
                    return m;
                }
            }
        }
        return null;
    }


    public static List<Method> lookupMethods(Class<?> clazz, String name, boolean isStatic) {
        Map<String,Method> m = new HashMap<String,Method>();
        lookupMethods(clazz, name, m);
        List<Method> rslt = new ArrayList<Method>(m.size());
        for (Map.Entry<String,Method> e : m.entrySet()) {
            if (isStatic == Modifier.isStatic(e.getValue().getModifiers())) {
                rslt.add(e.getValue());
            }
        }
        return rslt;
    }


    private static void lookupMethods(Class<?> clazz, String name, Map<String,Method> methods) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (name.equals(m.getName())) {
                String sgn = m.toGenericString();
                if (!methods.containsKey(sgn)) {
                    methods.put(sgn, m);
                }
            }
        }
        if (clazz != Object.class && clazz.getSuperclass() != null) {
            lookupMethods(clazz.getSuperclass(), name, methods);
        }
        for (Class<?> icl : clazz.getInterfaces()) {
            lookupMethods(icl, name, methods);
        }
    }

    /**
     * Queries mbean server for all objects matching given query string.
     *
     * @param conn  mbean server connection
     * @param query query string
     * @return set of object names (possibly empty set if no matching names have been found or error occured)
     */
    @SuppressWarnings("unchecked")
    public static Set<ObjectName> queryNames(MBeanServerConnection conn, String query) {
        try {
            ObjectName on = new ObjectName(query);
            return conn.queryNames(on, null);
        } catch (Exception e) {
            log.error(ZorkaLogger.ZAG_ERRORS, "Error performing '" + query + "' JMX query", e);
            return new HashSet<ObjectName>();
        }
    }


    /**
     * Regular expression for identifying substitution markers in strings. Used by substitute() methods.
     */
    public static final Pattern reVarSubstPattern = Pattern.compile("\\$\\{([^\\}]+)\\}");

    private static final Pattern reDollarSign = Pattern.compile("$", Pattern.LITERAL);
    private static final String reDollarReplacement = Matcher.quoteReplacement("\\$");

    /**
     * Substitutes marked variables in a string with record fields. Variables are marked with
     * '${FIELD.attr1.attr2...}'. Fields are resolved directly from records passed as second
     * parameter, subsequent attribute chains are used to obtain subsequent values as in
     * ObjectInspector.get() method.
     *
     * @param input  input (template) string
     * @param record spy record to be substituted
     * @return string with substitutions filled with values from record
     */
    public static String substitute(String input, Map<String, Object> record) {
        Matcher m = reVarSubstPattern.matcher(input);
        StringBuffer sb = new StringBuffer();

        while (m.find()) {
            String expr = m.group(1), def = null;
            if (expr.contains(":")) {
                String[] s = expr.split(":");
                expr = s[0];
                def = s[1];
            }
            Integer len = null;
            if (expr.contains("~")) {
                String[] s = expr.split("~");
                expr = s[0];
                len = Integer.parseInt(s[1]);
            }
            Object val = null;
            for (String exp : expr.split("\\|")) {
                String[] segs = exp.split("\\.");
                val = record.get(segs[0]);
                for (int i = 1; i < segs.length; i++) {
                    val = getAttr(val, segs[i]);
                }
                if (val != null) break;
            }
            val = val != null ? val : def;
            String s = ZorkaUtil.castString(val);
            if (len != null && s.length() > len) {
                s = s.substring(0, len);
            }
            m.appendReplacement(sb, reDollarSign.matcher(s).replaceAll(reDollarReplacement));
        }

        m.appendTail(sb);

        return sb.toString();
    }


    /**
     * Substitutes marked variables in a string with property strings.
     *
     * @param input      input (template) string
     * @param properties spy record to be substituted
     * @return string with substitutions filled with values from record
     */
    public static String substitute(String input, Properties properties) {
        Matcher m = reVarSubstPattern.matcher(input);
        StringBuffer sb = new StringBuffer();

        while (m.find()) {
            String key = m.group(1), def = null;
            if (key.contains(":")) {
                String[] s = key.split(":");
                key = s[0];
                def = s[1];
            }
            Integer len = null;
            if (key.contains("~")) {
                String[] s = key.split("~");
                key = s[0];
                len = Integer.parseInt(s[1]);
            }
            String val = null;
            for (String k : key.split("\\|")) {
                val = properties.getProperty(k);
                if (val != null) break;
            }
            if (val != null) {
                if (len != null && val.length() > len) {
                    val = val.substring(0, len);
                }
                m.appendReplacement(sb, val);
            } else if (def != null) {
                m.appendReplacement(sb, def);
            }
        }

        m.appendTail(sb);

        return sb.toString();
    }


    /**
     * Substitutes marked variables in a string with values from array. Variables are marked with
     * '${N.attr1.attr2...}'. Values are resolved directly from array passed as second
     * parameter, subsequent attribute chains are used to obtain subsequent values as in
     * ObjectInspector.get() method.
     *
     * @param input input (template) string
     * @param vals  array of values
     * @return string with substitutions filled with values from record
     */
    public static String substitute(String input, Object[] vals) {
        Matcher m = reVarSubstPattern.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String expr = m.group(1), def = null;
            if (expr.contains(":")) {
                String[] s = expr.split(":");
                expr = s[0];
                def = s[1];
            }
            Integer len = null;
            if (expr.contains("~")) {
                String[] s = expr.split("~");
                expr = s[0];
                len = Integer.parseInt(s[1]);
            }
            Object val = null;
            for (String exp : expr.split("\\|")) {
                String[] segs = exp.split("\\.");
                val = vals[Integer.parseInt(segs[0])];
                for (int i = 1; i < segs.length; i++) {
                    val = getAttr(val, segs[i]);
                }
                if (val != null) break;
            }
            val = val != null ? val : def;
            String s = ZorkaUtil.castString(val);
            if (len != null && s.length() > len) {
                s = s.substring(0, len);
            }
            m.appendReplacement(sb, s);
        }

        m.appendTail(sb);

        return sb.toString();
    }


    public static Object getField(Object obj, String fieldName) {
        if (obj != null) {
            Field field = lookupField(obj instanceof Class ? (Class) obj : obj.getClass(), fieldName);

            if (field == null) {
                return null;
            }

            boolean accessible = field.isAccessible();

            if (!accessible) {
                field.setAccessible(true);
            }

            Object ret = null;

            try {
                ret = field.get(obj);
            } catch (IllegalAccessException e) {
            }

            if (!accessible) {
                field.setAccessible(false);
            }

            return ret;
        } else {
            return null;
        }
    }

    public static Object getField(Object obj, Field field) {

        boolean accessible = field.isAccessible();

        if (!accessible) {
            field.setAccessible(true);
        }

        Object rslt = null;

        try {
            rslt = field.get(obj);
        } catch (IllegalAccessException ignored) {
        }

        if (!accessible) {
            field.setAccessible(false);
        }

        return rslt;
    }


    public static boolean setField(Object obj, String fieldName, Object value) {
        if (obj != null) {
            Field field = lookupField(obj instanceof Class ? (Class) obj : obj.getClass(), fieldName);

            if (field == null) {
                return false;
            }

            boolean accessible = field.isAccessible();

            if (!accessible) {
                field.setAccessible(true);
            }

            Class<?> t = field.getType();

            try {
                field.set(obj, t.isInstance(value) ? value : ZorkaUtil.coerce(value, t));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            if (!accessible) {
                field.setAccessible(accessible);
            }

            return true;
        }

        return false;
    }

    public static <T> T instantiate(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

//    private static <T> List<T> instantiateList(Class<T> clazz) {
//        Class<? extends T> listClazz = List.class.asSubclass()
//        //List<T>
//    }

    public static final Keyword KWAS = Keyword.keyword("as");
    public static final Keyword KWOR = Keyword.keyword("or");
    public static final Keyword KEYS = Keyword.keyword("keys");


    public static LispMap destructure(Object params, Seq args) {
        LispMap lm = new LispSMap(0);
        return destructure(params, args, lm);
    }


    public static LispMap destructure(Object params, Object args, LispMap pmap) {
        if (params instanceof Seq) {
            Seq a = StandardLibrary.seq(args);
            for (Object p = params; p != null; a = Utils.next(a), p = StandardLibrary.cdr(p)) {
                if (p instanceof Seq) {
                    Object cp = StandardLibrary.car(p);
                    if (cp instanceof Seq) {
                        pmap = destructure(cp, StandardLibrary.car(a), pmap);
                    } else {
                        Object p1 = StandardLibrary.cadr(p);
                        if (cp instanceof Symbol) {
                            if (p1 instanceof String) {
                                pmap = pmap.assoc(StandardLibrary.car(p), ObjectInspector.getAttr(args, p1));
                                p = StandardLibrary.cdr(p);
                            } else if (p1 instanceof Keyword && !KWAS.equals(p1) && !KWOR.equals(p1)) {
                                Object obj = ObjectInspector.getAttr(args, ((Keyword) p1).getName());
                                if (obj == null) {
                                    obj = ObjectInspector.getAttr(args, p1);
                                }
                                pmap = pmap.assoc(StandardLibrary.car(p), obj);
                                p = StandardLibrary.cdr(p);
                            } else {
                                pmap = pmap.assoc(cp, StandardLibrary.car(a));
                            }
                        } else if (KWAS.equals(cp)) {
                            if (p1 instanceof Symbol) {
                                pmap = pmap.assoc(p1, args);
                                p = StandardLibrary.cdr(p);
                            } else {
                                throw new LispException("Expected symbol but found: " + p1);
                            }
                        } else if (KEYS.equals(cp)) {
                            if (p1 instanceof Seq) {
                                for (Seq kseq = (Seq)p1; kseq != null; kseq = Utils.next(kseq)) {
                                    if (StandardLibrary.car(kseq) instanceof Symbol) {
                                        Symbol sym = (Symbol) StandardLibrary.car(kseq);
                                        Object obj = ObjectInspector.getAttr(args, sym.getName());
                                        if (obj == null) {
                                            obj = ObjectInspector.getAttr(args, Keyword.keyword(sym.getName()));
                                        }
                                        pmap = pmap.assoc(sym, obj);
                                    } else {
                                        throw new LispException("Invalid keylist item: " + StandardLibrary.car(kseq));
                                    }
                                }
                            } else {
                                throw new LispException("Invalid keylist: " + p1);
                            }
                            p = StandardLibrary.cdr(p);
                        } else if (KWOR.equals(cp)) {
                            if (p1 instanceof Seq) {
                                for (Seq seq = (Seq)p1; seq != null; seq = (Seq) StandardLibrary.cddr(seq)) {
                                    if (!(StandardLibrary.car(seq) instanceof Symbol)) {
                                        throw new LispException("Expected symbol but got " + StandardLibrary.car(seq));
                                    }
                                    Object key = StandardLibrary.car(seq);
                                    if (StandardLibrary.cdr(seq) == null) {
                                        throw new LispException("Expected constant value after " + key);
                                    }
                                    Object val = StandardLibrary.cadr(seq);
                                    if (pmap.get(key) == null) {
                                        pmap = pmap.assoc(key, val);
                                    }
                                }
                            } else {
                                throw new LispException("Expected list after :or but got " + p1);
                            }
                            p = StandardLibrary.cdr(p);
                        } else {
                            throw new LispException("Invalid parameter declaration: " + StandardLibrary.car(p));
                        }
                    }
                } else if (p instanceof Symbol) {
                    pmap = pmap.assoc(p, a);
                } else {
                    throw new LispException("Invalid parameter declaration: " + params);
                }
            }
        } else if (params instanceof Symbol) {
            pmap = pmap.assoc(params, args);
        }
        return pmap;
    }


}

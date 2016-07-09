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

package com.jitlogic.zorka;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.*;

import com.jitlogic.zorka.stats.AgentDiagnostics;
import com.jitlogic.zorka.stats.MethodCallStatistic;
import com.jitlogic.zorka.util.FileTrapper;
import com.jitlogic.zorka.perfmon.*;
import com.jitlogic.zorka.util.*;
import com.jitlogic.zorka.mbeans.MBeanServerRegistry;
import com.jitlogic.zorka.mbeans.AttrGetter;
import com.jitlogic.zorka.stats.ValGetter;
import com.jitlogic.zorka.mbeans.ZorkaMappedMBean;
import com.jitlogic.zorka.lisp.Namespace;
import com.jitlogic.zorka.lisp.Primitive;

import static com.jitlogic.zorka.AgentConfigConstants.*;

/**
 * Standard library for zorka-agent. All public methods implemented in this module will be available for
 * zorka configuration scripts in 'zorka' namespace.
 *
 * @author rafal.lewczuk@jitlogic.com
 */
@Namespace("zorka")
public class ZorkaLib implements ZorkaService {

    private static final ZorkaLog log = ZorkaLogger.getLog(ZorkaLib.class);

    public static final ZorkaLogLevel TRACE = ZorkaLogLevel.TRACE;
    public static final ZorkaLogLevel DEBUG = ZorkaLogLevel.DEBUG;
    public static final ZorkaLogLevel INFO = ZorkaLogLevel.INFO;
    public static final ZorkaLogLevel WARN = ZorkaLogLevel.WARN;
    public static final ZorkaLogLevel ERROR = ZorkaLogLevel.ERROR;
    public static final ZorkaLogLevel FATAL = ZorkaLogLevel.FATAL;

    private static final int MINUTE = 60000;
    private static final int SECOND = 1000;

    private ZorkaLogger logger = ZorkaLogger.getLogger();

    private ZorkaLispAgent agent;
    private Set<JmxObject> registeredObjects = new HashSet<JmxObject>();

    private MBeanServerRegistry mbsRegistry;

    private String hostname;

    private AvgRateCounter rateCounter = new AvgRateCounter(this);
    private Map<String, FileTrapper> fileTrappers = new ConcurrentHashMap<String, FileTrapper>();

    private TaskScheduler scheduler = TaskScheduler.instance();

    private String version;

    private AgentConfig config;

    private AgentInstance instance;

    /**
     * Standard constructor
     */
    public ZorkaLib(AgentInstance instance) {
        this.agent = instance.getZorkaAgent();
        this.mbsRegistry = instance.getMBeanServerRegistry();
        this.config = instance.getConfig();
        this.instance = instance;

        this.hostname = config.strVal(null, KW_HOSTNAME);
        this.version = config.strVal("?", KW_ZORKA, KW_VERSION);
    }


    /**
     * Returns agent version.
     *
     * @return string containing agent version
     */
    @Primitive
    public String version() {
        return version;
    }


    /**
     * Returns hostname (name agent will present to monitoring systems communicating with agent).
     * Note that this does have to be host name of server running application server hosting this agent.
     * Administrator can configure many JVMs on a single server and each can present different name.
     * Agent hostname can be configured in zorka.properties file as 'zorka.hostname' property.
     * <p/>
     * By convention 'app-name.os-hostname.domain' should be used.
     *
     * @return hostname string
     */
    @Primitive
    public String getHostname() {
        return "" + hostname;
    }


    /**
     * Returns list of objects from given mbean server.
     *
     * @param args attribute chain (as in ObjectInspector.get() function)
     * @return list of objects
     *         <p/>
     *         TODO fix parameters of this method
     */
    @Primitive("jmx-list")
    public List<Object> jmxList(List<Object> args) {
        List<Object> objs = new ArrayList<Object>();
        if (args.size() < 2) {
            log.error(ZorkaLogger.ZAG_ERRORS, "Zorka JMX function takes at least 2 arguments.");
            return objs;
        }
        String conname = args.get(0).toString();
        MBeanServerConnection conn = mbsRegistry.lookup(conname);
        if (conn == null) {
            log.error(ZorkaLogger.ZAG_ERRORS, "MBean server named '" + args.get(0) + "' is not registered.");
            return objs;
        }
        ClassLoader cl0 = Thread.currentThread().getContextClassLoader(), cl1 = mbsRegistry.getClassLoader(conname);

        Set<ObjectName> names = ObjectInspector.queryNames(conn, args.get(1).toString());
        if (args.size() == 2) {
            for (ObjectName name : names) {
                objs.add(new JmxObject(name, conn, cl1));
            }
        } else {
            if (cl1 != null) {
                Thread.currentThread().setContextClassLoader(cl1);
            }
            for (ObjectName name : names) {
                Object obj = null;
                try {
                    obj = conn.getAttribute(name, args.get(2).toString());
                } catch (AttributeNotFoundException e) {
                    log.error(ZorkaLogger.ZAG_ERRORS, "Object '" + conname + "|" + name + "' has no attribute '" + args.get(2) + "'.", e);
                } catch (Exception e) {
                    log.error(ZorkaLogger.ZAG_ERRORS, "Error getting attribute '" + args.get(2) + "' from '" + conname + "|" + name + "'", e);
                }

                if (args.size() > 3) {
                    obj = ObjectInspector.get(obj, args.subList(3, args.size()).toArray(new Object[0]));
                }
                objs.add(obj);
            }
            if (cl1 != null) {
                Thread.currentThread().setContextClassLoader(cl0);
            }
        }
        return objs;
    } // jmxList()


    /**
     * Return text dump of selected JMX objects
     *
     * @param args attribute chain (as in ObjectInspector.get() function)
     * @return dump of selected JMX objects
     *         TODO fix parameters of this method
     */
    @Primitive
    public String dump(Object... args) {

        StringBuffer sb = new StringBuffer();

        for (Object obj : jmxList(Arrays.asList(args))) {
            sb.append(ObjectDumper.objectDump(obj));
        }

        return sb.toString();
    } // dump()

    @Primitive
    public Object jmxv(Object defval, Object... args) {
        Object v = jmx(args);
        return v != null ? v : defval;
    }


    /**
     * Retrieves object from JMX
     *
     * @param args attribute chain (as in ObjectInspector.get() function)
     * @return retrieved obejct
     */
    @Primitive
    public Object jmx(Object... args) {

        List<Object> argList = Arrays.asList(args);

        if (argList.size() < 2) {
            log.error(ZorkaLogger.ZAG_ERRORS, "zorka.jmx() function requires at least 2 arguments");
            return null;
        }

        String conname = argList.get(0).toString();

        MBeanServerConnection conn = mbsRegistry.lookup(conname);

        if (conn == null) {
            log.error(ZorkaLogger.ZAG_ERRORS, "MBean server named '" + argList.get(0) + "' is not registered.");
            return null;
        }

        Set<ObjectName> names = ObjectInspector.queryNames(conn, argList.get(1).toString());

        if (names.isEmpty()) {
            return null;
        }

        ObjectName name = names.iterator().next();

        ClassLoader cl0 = Thread.currentThread().getContextClassLoader(), cl1 = mbsRegistry.getClassLoader(conname);

        if (argList.size() == 2) {
            return new JmxObject(name, conn, cl1);
        }

        Object obj = null;
        try {
            if (cl1 != null) {
                Thread.currentThread().setContextClassLoader(cl1);
            }
            obj = conn.getAttribute(name, argList.get(2).toString());
        } catch (AttributeNotFoundException e) {
            log.error(ZorkaLogger.ZAG_ERRORS, "Object '" + conname + "|" + name + "' has no attribute '" + argList.get(2) + "'.", e);
            return null;
        } catch (Exception e) {
            log.error(ZorkaLogger.ZAG_ERRORS, "Error getting attribute '" + argList.get(2) + "' from '" + conname + "|" + name + "'", e);
        } finally {
            if (cl1 != null) {
                Thread.currentThread().setContextClassLoader(cl0);
            }
        }

        if (argList.size() > 3 && obj != null) {
            obj = ObjectInspector.get(obj, argList.subList(3, argList.size()).toArray(new Object[0]));
        }

        return obj;
    } // jmx()

    private static final Map<Character,String> signTypes = ZorkaUtil.constMap(
        'b', "byte",   'B', Byte.class.getName(),
        'c', "char",   'C', Character.class.getName(),
        's', "short",  'S', Short.class.getName(),
        'i', "int",    'I', Integer.class.getName(),
        'l', "long",   'L', Long.class.getName(),
        'f', "float",  'F', Float.class.getName(),
        'd', "double", 'D', Double.class.getName(),
        'T', String.class.getName()
    );


    private String[] parseSignature(String s) {
        String[] rslt = new String[s.length()];
        for (int i = 0; i < s.length(); i++) {
            rslt[i] = signTypes.get(s.charAt(i));
        }
        return rslt;
    }


    public Object[] coerceArgs(String signature, Object[] args) {
        Object[] rslt = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            Object o = args[i];
            switch (Character.toUpperCase(signature.charAt(i))) {
                case 'B':
                    rslt[i] = o instanceof Number ? ((Number)o).byteValue() : o != null ? Byte.parseByte(o.toString()) : null;
                    break;
                case 'C':
                    rslt[i] = o != null ? o.toString().charAt(0) : null;
                case 'S':
                    rslt[i] = o instanceof Number ? ((Number)o).shortValue() : o != null ? Short.parseShort(""+o) : null;
                    break;
                case 'I':
                    rslt[i] = o instanceof Number ? ((Number)o).intValue() : o != null ? Integer.parseInt(""+o) : null;
                    break;
                case 'L':
                    rslt[i] = o instanceof Number ? ((Number)o).longValue() : o != null ? Long.parseLong(""+o) : null;
                    break;
                case 'F':
                    rslt[i] = o instanceof Number ? ((Number)o).floatValue() : o != null ? Float.parseFloat(""+o) : null;
                case 'T':
                    rslt[i] = o;
                    break;
            }
        }
        return rslt;
    }


    /**
     * Invokes JMX operation on an mbean (JMX Call).
     *
     * Operation signature where each character represents type of corresponding parameter. Use lower case b,c,s,i,l,f,d
     * for unboxed byte,char,short,int,long,float,double; B,S,I,L,F,D for corresponding boxed types and T for Strings.
     *
     * Arguments will be coerced to declared types and cannot be null.
     *
     * @param conname name of registered mbean server connection (eg. "java")
     * @param objname object name
     * @param mname operation name
     * @param msign operation signature
     * @param args operation arguments
     *
     * @return opration result or null
     */
    @Primitive
    public Object jmxc(String conname, String objname, String mname, String msign, Object...args) {

        MBeanServerConnection conn = mbsRegistry.lookup(conname);

        if (conn == null) {
            log.error(ZorkaLogger.ZAG_ERRORS, "MBean server named '" + conname + "' is not registered.");
            return null;
        }

        Set<ObjectName> names = ObjectInspector.queryNames(conn, objname);

        if (names.isEmpty()) {
            return null;
        }

        ObjectName name = names.iterator().next();

        ClassLoader cl0 = Thread.currentThread().getContextClassLoader(), cl1 = mbsRegistry.getClassLoader(conname);

        Object obj = null;

        try {
            if (cl1 != null) {
                Thread.currentThread().setContextClassLoader(cl1);
            }
            obj = conn.invoke(name, mname, coerceArgs(msign,args), parseSignature(msign));
        } catch (Exception e) {
            log.error(ZorkaLogger.ZAG_ERRORS, "Error invoking method '" + mname + "' from '" + conname + "|" + name + "'", e);
        } finally {
            if (cl1 != null) {
                Thread.currentThread().setContextClassLoader(cl0);
            }
        }

        return obj;
    }

    @Primitive
    public Object jmxcv(Object defval, String conname, String objname, String mname, String msign, Object...args) {
        Object rslt = jmxc(conname, objname, mname, msign, args);
        return rslt != null ? rslt : defval;
    }


    /**
     * Lists attributes of given object(s)
     *
     * @param mbsName    mbean server name
     * @param objectName object name (mask)
     * @param args       attribute chain (as in ObjectInspector.get())
     * @return string listing attributes and their values
     */
    @Primitive
    public String ls(String mbsName, String objectName, Object... args) {

        MBeanServerConnection conn = mbsRegistry.lookup(mbsName);

        if (conn == null) {
            log.error(ZorkaLogger.ZAG_ERRORS, "MBean server named '" + mbsName + "' is not registered.");
            return null;
        }

        QueryDef qdef = new QueryDef(mbsName, objectName, "*");

        for (int i = 0; i < args.length; i++) {
            qdef = qdef.listAs(args[i].toString(), "ARG" + i);
        }

        ClassLoader cl0 = Thread.currentThread().getContextClassLoader(), cl1 = mbsRegistry.getClassLoader(mbsName);

        List<String> lst = new ArrayList<String>();

        try {

            if (cl1 != null) {
                Thread.currentThread().setContextClassLoader(cl1);
                log.debug(ZorkaLogger.ZAG_DEBUG, "Switching to MBS class loader ...");
            }

            List<QueryResult> results = new QueryLister(mbsRegistry, qdef).list();


            for (QueryResult result : results) {
                StringBuilder sb = new StringBuilder();
                int n = 0;
                for (Map.Entry<String, Object> e : result.attrSet()) {
                    if (n > 0) {
                        sb.append(n == 1 ? ": " : ".");
                    }
                    n++;
                    sb.append(e.getValue());
                }
                sb.append(" -> ");
                sb.append(result.getValue());
                lst.add(sb.toString());
            }

        } finally {
            if (cl1 != null) {
                Thread.currentThread().setContextClassLoader(cl0);
                log.debug(ZorkaLogger.ZAG_DEBUG, "Switching back from class loader ...");
            }
        }

        Collections.sort(lst);
        return ZorkaUtil.join("\n", lst);
    }


    /**
     * Creates zorka dynamic MBean. Such mbean object can be populated with attributes
     * using putString() or setAttr() methods.
     *
     * @param mbs  mbean server at which bean will register
     * @param name object name
     * @return mbean object
     */
    @Primitive
    public ZorkaMappedMBean mbean(String mbs, String name) {
        return mbean(mbs, name, "");
    }


    /**
     * Creates zorka dynamic MBean. Such mbean object can be populated with attributes
     *
     * @param mbs  mbean server at which bean will register
     * @param name object name
     * @param desc mbean description
     * @return mbean object
     */
    @Primitive
    public ZorkaMappedMBean mbean(String mbs, String name, String desc) {
        // TODO wyrugować ręczne tworzenie mbeanów, zdefiniować jedną (wspólną) metodę do tego i używać jej
        try {
            ZorkaMappedMBean mbean = new ZorkaMappedMBean(desc);
            MBeanServer conn = (MBeanServer) mbsRegistry.lookup(mbs);
            if (conn == null) {
                throw new IllegalArgumentException("There is no mbean server named '" + mbs + "'");
            }

            ObjectName on = new ObjectName(name);
            conn.registerMBean(mbean, on);
            registeredObjects.add(new JmxObject(on, conn, Thread.currentThread().getContextClassLoader()));
            return mbean;
        } catch (Exception e) {
            // TODO zalogowac problem
            return null;
        }
    }


    /**
     * Creates Attribute getter object.
     *
     * @param obj
     * @param attrs
     * @return
     */
    @Primitive
    public ValGetter getter(Object obj, Object... attrs) {
        return new AttrGetter(obj, attrs);
    }


    /**
     * Calculates windowed rate of two associated parameters (eg. execution time and number of calls).
     * This is useful for statistics that only provide total values (and no averages). It requires
     * caller to periodically call this function in order to maintain collected deltas, so this method
     * will be deprecated as soon as viable alternative appears.
     *
     * @param args first two arguments are mbean server name and object name, last three arguments are
     *             nominator field, divider field and time horizon, all remaining middle arguemnts are
     *             optional and are part of attribute chain needed to reach from mbean to proper object
     *             containing statistic
     * @return calculated rate
     */
    @Primitive
    public Double rate(Object... args) {

        if (args.length < 5) {
            log.error(ZorkaLogger.ZAG_ERRORS, "Too little arguments for zorka.rate(). At least 5 args are required");
            return null;
        }

        Object oh = args[args.length - 1];
        long horizon = 0;

        if (oh instanceof String && ((String) oh).matches("^AVG[0-9]+$")) {
            horizon = Long.parseLong(oh.toString().substring(3)) * MINUTE;
        } else {
            horizon = rateCounter.coerce(args[args.length - 1]) * SECOND;
        }

        if (horizon == 0) {
            log.error(ZorkaLogger.ZAG_ERRORS, "Invalid time horizon in zorka.rate()");
            return null;
        }

        String div = (String) args[args.length - 2];
        String nom = (String) args[args.length - 3];

        return rateCounter.get(Arrays.asList(ZorkaUtil.clipArray(args, args.length - 3)), nom, div, horizon);
    }


    /**
     * Sends DEBUG message to zorka log.
     *
     * @param message message text
     * @param args    optional arguments (if message contains format string markers)
     */
    @Primitive("log-debug")
    public void logDebug(String message, Object... args) {
        log(ZorkaLogLevel.DEBUG, message, args);
    }

    /**
     * Sends INFO message to zorka log.
     *
     * @param message message text
     * @param args    optional arguments (if message contains format string markers)
     */
    @Primitive("log-info")
    public void logInfo(String message, Object... args) {
        log(ZorkaLogLevel.INFO, message, args);
    }

    /**
     * Sends WARNING message to zorka log.
     *
     * @param message message text
     * @param args    optional arguments (if message contains format string markers)
     */
    @Primitive("log-warning")
    public void logWarning(String message, Object... args) {
        log(ZorkaLogLevel.WARN, message, args);
    }

    /**
     * Sends ERROR message to zorka log.
     *
     * @param message message text
     * @param args    optional arguments (if message contains format string markers)
     */
    @Primitive("log-error")
    public void logError(String message, Object... args) {
        log(ZorkaLogLevel.ERROR, message, args);
    }

    /**
     * Sends FATAL message to zorka log.
     *
     * @param message message text
     * @param args    optional arguments (if message contains format string markers)
     */
    @Primitive("log-fatal")
    public void logFatal(String message, Object... args) {
        log(ZorkaLogLevel.ERROR, message, args);
    }

    /**
     * Logs arbitrary message to zorka log.
     *
     * @param level   log level
     * @param message message text
     * @param argv    optional arguments (if message is a format string)
     */
    @Primitive
    private void log(ZorkaLogLevel level, String message, Object... argv) {
        Throwable ex = null;
        Object[] args = argv;
        if (args.length > 0 && args[args.length - 1] instanceof Throwable) {
            ex = (Throwable) args[args.length - 1];
            args = args.length > 1 ? ZorkaUtil.clipArray(args, -1) : new Object[0];
        }
        logger.trap(level, "bsh", message, ex, args);
    }


    /**
     * Reloads agent configuration and scripts.
     *
     * @return
     */
    @Primitive
    public String reload() {
        instance.reload();
        long l = AgentDiagnostics.get(AgentDiagnostics.CONFIG_ERRORS);
        return l == 0 ? "OK" : "ERRORS(" + l + ") see agent log.";
    }

    @Primitive("load-script")
    public String loadScript(String script) {
        return agent.loadScript(script);
    }

    @Primitive
    public String require(String... names) {
        String s = "";
        for (String name : names) {
            s += agent.require(name) + "; ";
        }
        return s;
    }


    /**
     * Returns true if agent has been initialized (i.e. executing BSH code is
     * executing after initial execution of configuration scripts
     *
     * @return true if agent has been initialized
     */
    @Primitive("initialized?")
    public boolean isInitialized() {
        return agent.isInitialized();
    }


    /**
     * Registers mbean server in agent mbean server registry. It is useful for some
     * application servers that have non-standard (additional) mbean servers or when
     * agent cannot access platform mbean server at start up time (notably JBoss 4/5/6/7).
     *
     * @param name name at which mbean server will be registered
     * @param mbs  reference to mbean server connection
     */
    @Primitive("register-mbs")
    public void registerMbs(String name, MBeanServerConnection mbs) {
        mbsRegistry.register(name, mbs, mbs.getClass().getClassLoader());
    }


    /**
     * Registers mbean server in agent mbean server registry. It is useful for some
     * application servers that have non-standard (additional) mbean servers or when
     * agent cannot access platform mbean server at start up time (notably JBoss 4/5/6/7).
     *
     * @param name        name at which mbean server will be registered
     * @param mbs         reference to mbean server connection
     * @param classLoader context class loader
     */
    @Primitive("register-mbs-cl") // TODO
    public void registerMbs(String name, MBeanServerConnection mbs, ClassLoader classLoader) {
        mbsRegistry.register(name, mbs, classLoader);
    }

    @Primitive("mbs-registered?")
    public boolean isMbsRegistered(String name) {
        return mbsRegistry.lookup(name) != null;
    }


    /**
     * Presents object as an attribute of an mbean. If there is already such attribute, its value
     * will be returned instead. If mbean exists, it must be ZorkaMappedBean.
     *
     * @param mbsName  mbean server name
     * @param beanName object name
     * @param attrName attribute name
     * @param obj      object to be presented
     * @param <T>      type of object to be presented
     * @return obj if there was no such attribute or value of existing attribute
     */
    @Primitive("register-attr")
    public <T> T registerAttr(String mbsName, String beanName, String attrName, T obj) {
        return mbsRegistry.getOrRegister(mbsName, beanName, attrName, obj);
    }

    /**
     * Presents object as an attribute of an mbean. If there is already such attribute, its value
     * will be returned instead. If mbean exists, it must be ZorkaMappedBean.
     *
     * @param mbsName  mbean server name
     * @param beanName object name
     * @param attrName attribute name
     * @param obj      object to be presented
     * @param <T>      type of object to be presented
     * @param desc     attribute description
     * @return obj if there was no such attribute or value of existing attribute
     */
    @Primitive("register-attr-dsc") // TODO
    public <T> T registerAttr(String mbsName, String beanName, String attrName, T obj, String desc) {
        return mbsRegistry.getOrRegister(mbsName, beanName, attrName, obj, desc);
    }


    /**
     * Looks for file trapper and returns if trapper exists.
     *
     * @param id trapper ID
     * @return file trapper or null
     */
    @Primitive("file-trapper")
    public FileTrapper fileTrapper(String id) {
        return fileTrappers.get(id);
    }


    /**
     * Stops and unregisters file trapper
     *
     * @param id trapper ID
     */
    @Primitive("remove-file-trapper")
    public void removeFileTrapper(String id) {
        FileTrapper trapper = fileTrappers.get(id);

        if (trapper != null) {
            trapper.shutdown();
        }
    }

    @Override
    public void shutdown() {
        for (FileTrapper trapper : fileTrappers.values()) {
            trapper.shutdown();
        }
        fileTrappers.clear();
    }


    /**
     * Schedules a task.
     *
     * @param task     task (must be Runnable)
     * @param interval run interval (in milliseconds)
     */
    @Primitive
    public void schedule(Runnable task, long interval, long delay) {
        scheduler.schedule(task, interval, delay);
    }

    @Primitive("query")
    public QueryDef query(String mbsName, String query, String... attrs) {
        return new QueryDef(mbsName, query, attrs);
    }


    @Primitive("cpu-hiccup")
    public HiccupMeter cpuHiccup(String mbsName, String mbeanName, String attr) {
        return cpuHiccup(mbsName, mbeanName, attr, 10, 30000);
    }

    @Primitive("cpu-hiccup-ld")
    public HiccupMeter cpuHiccup(String mbsName, String mbeanName, String attr, long resolution, long delay) {
        MethodCallStatistic mcs = mbsRegistry.getOrRegister(mbsName, mbeanName, attr, new MethodCallStatistic("cpuHiccup"));
        HiccupMeter meter = HiccupMeter.cpuMeter(resolution, delay, mcs);
        return meter;
    }


    @Primitive("mem-hiccup")
    public HiccupMeter memHiccup(String mbsName, String mbeanName, String attr) {
        return memHiccup(mbsName, mbeanName, attr, 10, 30000);
    }

    @Primitive("mem-hiccup-ld")
    public HiccupMeter memHiccup(String mbsName, String mbeanName, String attr, long resolution, long delay) {
        MethodCallStatistic mcs = mbsRegistry.getOrRegister(mbsName, mbeanName, attr, new MethodCallStatistic("memHiccup"));
        HiccupMeter meter = HiccupMeter.memMeter(resolution, delay, mcs);
        return meter;
    }

    @Primitive("dsk-hiccup")
    public HiccupMeter dskHiccup(String mbsName, String mbeanName, String attr, String path) {
        return dskHiccup(mbsName, mbeanName, attr, 1000, 30000, path);
    }

    @Primitive("dsk-hiccup-ld")
    public HiccupMeter dskHiccup(String mbsName, String mbeanName, String attr, long resolution, long delay, String path) {
        MethodCallStatistic mcs = mbsRegistry.getOrRegister(mbsName, mbeanName, attr, new MethodCallStatistic("dskHiccup"));
        HiccupMeter meter = HiccupMeter.dskMeter(resolution, delay, path, mcs);
        return meter;
    }

}

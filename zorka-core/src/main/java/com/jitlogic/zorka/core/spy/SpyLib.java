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

package com.jitlogic.zorka.core.spy;

import com.jitlogic.zorka.common.tracedata.SymbolRegistry;
import com.jitlogic.zorka.common.util.*;
import com.jitlogic.zorka.core.ZorkaLispAgent;
import com.jitlogic.zorka.core.mbeans.MBeanServerRegistry;
import com.jitlogic.zorka.core.util.OverlayClassLoader;
import com.jitlogic.zorka.lisp.*;

import java.util.*;

/**
 * Spy library contains functions for configuring instrumentation engine. Spy definitions, matchers, probes, processors
 * and collectors can be created using functions from this library. Spy library is registered as 'spy' namespace in BSH.
 */
@Namespace("spy")
public class SpyLib {

    private static final ZorkaLog log = ZorkaLogger.getLog(SpyLib.class);

    public static final String SM_NOARGS = "<no-args>";
    public static final String SM_CONSTRUCTOR = "<init>";
    public static final String SM_ANY_TYPE = null;
    public static final String SM_STATIC = "<clinit>";


    public static final int SF_NONE = 0;
    public static final int SF_IMMEDIATE = 1;
    public static final int SF_FLUSH = 2;

    public static final int ZST_STATS = 0x01;
    public static final int ZST_ENTER = 0x02;
    public static final int ZST_EXIT = 0x04;

    // Debug levels

    /**
     * Be quiet
     */
    public static final int SPD_NONE = 0;

    /**
     * Basic status messages
     */
    public static final int SPD_STATUS = 1;

    /**
     * Detailed configuration information
     */
    public static final int SPD_CONFIG = 2;

    /**
     * Log transformed classes
     */
    public static final int SPD_CLASSXFORM = 3;

    /**
     * Log transformed methods
     */
    public static final int SPD_METHODXFORM = 4;

    /**
     * Log all collected records reaching collector dispatcher
     */
    public static final int SPD_CDISPATCHES = 5;

    /**
     * Log all collected records on each collector
     */
    public static final int SPD_COLLECTORS = 6;

    /**
     * Log all argument processing events
     */
    public static final int SPD_ARGPROC = 7;

    /**
     * Log all submissions from instrumented code
     */
    public static final int SPD_SUBMISSIONS = 8;

    /**
     * Tracer debug messages
     */
    public static final int SPD_TRACE_DEBUG = 9;

    /**
     * All possible tracer messages
     */
    public static final int SPD_TRACE_ALL = 10;

    /**
     * Log all encountered methods (only from transformed classes)
     */
    public static final int SPD_METHODALL = 11;

    /**
     * Log all classes going through transformer
     */
    public static final int SPD_CLASSALL = 12;

    /**
     * Maximum possible debug log level
     */
    public static final int SPD_MAX = 13;


    public static final String GT = ">";
    public static final String GE = ">=";
    public static final String EQ = "==";
    public static final String LE = "<=";
    public static final String LT = "<";
    public static final String NE = "!=";

    public static final int ON_ENTER = 0;
    public static final int ON_RETURN = 1;
    public static final int ON_ERROR = 2;
    public static final int ON_SUBMIT = 3;

    public static final int AC_PUBLIC = 0x000001;
    public static final int AC_PRIVATE = 0x000002;
    public static final int AC_PROTECTED = 0x000004;
    public static final int AC_STATIC = 0x000008;
    public static final int AC_FINAL = 0x000010;
    public static final int AC_SUPER = 0x000020;
    public static final int AC_SYNCHRONIZED = 0x000020;
    public static final int AC_VOLATILE = 0x000040;
    public static final int AC_BRIDGE = 0x000040;
    public static final int AC_VARARGS = 0x000080;
    public static final int AC_TRANSIENT = 0x000080;
    public static final int AC_NATIVE = 0x000100;
    public static final int AC_INTERFACE = 0x000200;
    public static final int AC_ABSTRACT = 0x000400;
    public static final int AC_STRICT = 0x000800;
    public static final int AC_SYNTHETIC = 0x001000;
    public static final int AC_ANNOTATION = 0x002000;
    public static final int AC_ENUM = 0x004000;
    public static final int AC_PKGPRIV = 0x010000;
    public static final int AC_ANY = 0x000000;

    public static final int ACTION_STATS = 0x01;
    public static final int ACTION_ENTER = 0x02;
    public static final int ACTION_EXIT = 0x04;

    public static final String TRACE = "TRACE";
    public static final String DEBUG = "DEBUG";
    public static final String INFO = "INFO";
    public static final String WARN = "WARN";
    public static final String ERROR = "ERROR";
    public static final String FATAL = "FATAL";

    private SpyClassTransformer classTransformer;
    private MBeanServerRegistry mbsRegistry;

    private ZorkaLispAgent agent;

    private Tracer tracer;

    private SymbolRegistry symbolRegistry;

    private ZorkaConfig config;


    /**
     * Creates spy library object
     *
     * @param classTransformer spy transformer
     */
    public SpyLib(SpyClassTransformer classTransformer, MBeanServerRegistry mbsRegistry, ZorkaLispAgent agent, Tracer tracer, SymbolRegistry symbolRegistry, ZorkaConfig config) {
        this.classTransformer = classTransformer;
        this.mbsRegistry = mbsRegistry;
        this.agent = agent;
        this.tracer = tracer;
        this.symbolRegistry = symbolRegistry;
        this.config = config;
    }


    /**
     * Registers spy definition(s) in Zorka Spy instrumentation engine. Only definitions registered using this function
     * will be considered by class transformer when loading classes and thus can be instrumented.
     *
     * @param sdef spy definition to be added
     */
    @Primitive("add!")
    public SpyDefinition add(SpyDefinition sdef) {
        classTransformer.add(sdef);
        return sdef;
    }

    @Primitive("sdef")
    public SpyDefinition sdef(String name) {
        return new SpyDefinition(name);
    }

    /**
     * Creates new matcher object that will match classes by annotation.
     *
     * @param annotationName class annotation pattern
     * @return spy matcher object
     */
    @Primitive("by-class-annotation")
    public SpyMatcher byClassAnnotation(String annotationName) {
        return new SpyMatcher(SpyMatcher.BY_CLASS_ANNOTATION, 1,
                "L" + annotationName + ";", "~[a-zA-Z_].*$", null);
    }

    /**
     * Creates new matcher object that will match methods by class annotation and method name.
     *
     * @param annotationName class annotation pattern
     * @param methodPattern  method name pattern
     * @return spy matcher object
     */
    @Primitive("by-class-annotation-and-method")
    public SpyMatcher byClassAnnotation(String annotationName, String methodPattern) {
        return new SpyMatcher(SpyMatcher.BY_CLASS_ANNOTATION | SpyMatcher.BY_METHOD_NAME, 1,
                "L" + annotationName + ";", methodPattern, null);
    }


    /**
     * Creates new matcher that will match all public methods of given class.
     *
     * @param className class name (or mask)
     * @return spy matched object
     */
    @Primitive("by-class")
    public SpyMatcher byClass(String className) {
        return byMethod(className, "*");
    }

    /**
     * Creates new matcher that will match all public methods of given class.
     *
     * @param iClassName interface class name (or mask)
     * @return spy matched object
     */
    @Primitive("by-interface")
    public SpyMatcher byInterface(String iClassName) {
        return byInterfaceAndMethod(iClassName, "*");
    }

    /**
     * Creates new matcher that will match methods by method annotation.
     *
     * @param classPattern     class name pattern
     * @param methodAnnotation method annotation patten
     * @return spy matcher object
     */
    @Primitive("by-method-annotation")
    public SpyMatcher byMethodAnnotation(String classPattern, String methodAnnotation) {
        return new SpyMatcher(SpyMatcher.BY_CLASS_NAME | SpyMatcher.BY_METHOD_ANNOTATION, 1,
                classPattern, "L" + methodAnnotation + ";", null);
    }


    /**
     * Creates new matcher that will match methods by class and method annotations
     *
     * @param classAnnotation  class annotation pattern
     * @param methodAnnotation method annotation pattern
     * @return spy matcher object
     */
    @Primitive("by-class-method-annotation")
    public SpyMatcher byClassMethodAnnotation(String classAnnotation, String methodAnnotation) {
        return new SpyMatcher(SpyMatcher.BY_CLASS_ANNOTATION | SpyMatcher.BY_METHOD_ANNOTATION, 1,
                "L" + classAnnotation + ";", "L" + methodAnnotation + ";", null);
    }


    /**
     * Creates new matcher object that will match methods by class name and method name.
     *
     * @param iClassPattern interface class name mask (where * matches arbitrary name and ** matches arbitrary path) or
     *                      regular expression (if starts with '~' character);
     * @param methodPattern method name mask (where '*' means arbitrary name part) or regular expression
     *                      (if starts with '~' character);
     * @return new matcher object
     */
    @Primitive("by-interface-and-method")
    public SpyMatcher byInterfaceAndMethod(String iClassPattern, String methodPattern) {
        return new SpyMatcher(SpyMatcher.BY_INTERFACE | SpyMatcher.BY_METHOD_NAME, 1, iClassPattern, methodPattern, null);
    }


    /**
     * Creates new matcher object that will match methods by class name, method name, access flags, return type and arguments.
     *
     * @param access        access flags (use spy.ACC_* constants);
     * @param iClassPattern interface class name mask (where * matches arbitrary name and ** matches arbitrary path) or
     *                      regular expression (if starts with '~' character);
     * @param methodPattern method name mask (where '*' means arbitrary string) or regular expression (if starts with '~' char);
     * @param retType       return type (eg. void, int, String, javax.servlet.HttpResponse etc.);
     * @param argTypes      types of consecutive arguments;
     * @return new matcher object;
     */
    @Primitive("by-interface-and-method-signature")
    public SpyMatcher byInterfaceAndMethod(int access, String iClassPattern, String methodPattern, String retType, String... argTypes) {
        return new SpyMatcher(SpyMatcher.BY_INTERFACE | SpyMatcher.BY_METHOD_NAME | SpyMatcher.BY_METHOD_SIGNATURE,
                access, iClassPattern, methodPattern, retType, argTypes);
    }


    /**
     * Creates new matcher object that will match methods by class name and method name.
     *
     * @param classPattern  class name mask (where * matches arbitrary name and ** matches arbitrary path) or
     *                      regular expression (if starts with '~' character);
     * @param methodPattern method name mask (where '*' means arbitrary name part) or regular expression
     *                      (if starts with '~' character);
     * @return new matcher object
     */
    @Primitive("by-method")
    public SpyMatcher byMethod(String classPattern, String methodPattern) {
        return new SpyMatcher(SpyMatcher.BY_CLASS_NAME | SpyMatcher.BY_METHOD_NAME, 1, classPattern, methodPattern, null);
    }


    /**
     * Creates new matcher object that will match methods by class name, method name, access flags, return type and arguments.
     *
     * @param access        access flags (use spy.ACC_* constants);
     * @param classPattern  class name mask (where * matches arbitrary name and ** matches arbitrary path) or
     *                      regular expression (if starts with '~' character);
     * @param methodPattern method name mask (where '*' means arbitrary string) or regular expression (if starts with '~' char);
     * @param retType       return type (eg. void, int, String, javax.servlet.HttpResponse etc.);
     * @param argTypes      types of consecutive arguments;
     * @return new matcher object;
     */
    @Primitive("by-method-signature")
    public SpyMatcher byMethod(int access, String classPattern, String methodPattern, String retType, String... argTypes) {
        return new SpyMatcher(SpyMatcher.BY_CLASS_NAME | SpyMatcher.BY_METHOD_NAME | SpyMatcher.BY_METHOD_SIGNATURE,
                access, classPattern, methodPattern, retType, argTypes);
    }


    /**
     * Creates argument fetching probe. When injected into method code by instrumentation engine, it will fetch argument
     * selected by specific index `arg`.
     *
     * @param dst name (key) used to store fetched data
     * @param arg fetched argument index
     * @return new probe
     */
    @Primitive("fetch-arg|")
    public SpyProbe fetchArg(String dst, int arg) {
        return new SpyArgProbe(arg, dst);
    }


    /**
     * Creates class fetching probe. When injected into method code it will fetch class object of given name in context
     * of method caller.
     *
     * @param dst       name (key) used to store fetched data
     * @param className class name
     * @return class fetching probe
     */
    @Primitive("fetch-class")
    public SpyProbe fetchClass(String dst, String className) {
        return new SpyClassProbe(dst, className);
    }


    /**
     * Creates exception fetching probe. When injected into method code it will fetch exception object when exception is
     * thrown out of method code.
     *
     * @param dst name (key) used to store fetched data
     * @return exception fetching probe
     */
    @Primitive("fetch-error")
    public SpyProbe fetchError(String dst) {
        return new SpyReturnProbe(dst);
    }


    /**
     * Creates return value fetching probe. When injected into method code it will fetch return value of instrumented
     * method.
     *
     * @param dst name (key) used to store fetched data
     * @return return value fetching probe
     */
    @Primitive("fetch-ret-val")
    public SpyProbe fetchRetVal(String dst) {
        return new SpyReturnProbe(dst);
    }


    /**
     * Creates time fetching probe. When injected into method code it will fetch current time.
     *
     * @param dst name (key) used to store fetched data
     * @return time fetching probe
     */
    @Primitive("fetch-time")
    public SpyProbe fetchTime(String dst) {
        return new SpyTimeProbe(dst);
    }



    @Primitive("mark-error")
    public void markError(Map<String,Object> record) {
        int f = (Integer)record.get(".STAGES");
        record.put(".STAGES", ((f | SpyLib.ON_ERROR) & ~SpyLib.ON_RETURN));
    }

    @Primitive("unmark-error")
    public void unmarkError(Map<String,Object> record) {
        int f = (Integer)record.get(".STAGES");
        record.put(".STAGES", ((f | SpyLib.ON_RETURN) & ~SpyLib.ON_ERROR));
    }



    @Primitive("lisp-fn")
    public SpyProcessor lispFn(Fn fn) {
        return new LispFnProcessor(agent.getInterpreter(), fn);
    }

    @Primitive("trace-buf-output")
    public void traceBufOutput(TraceBufOutput bufOutput) {
        tracer.setBufOutput(bufOutput);
    }

    /**
     * Adds matching method to tracer.
     *
     * @param matchers spy matcher objects (created using spy.byXxxx() functions)
     */
    @Primitive
    public void include(String... matchers) {
        for (String matcher : matchers) {
            log.info(ZorkaLogger.ZAG_CONFIG, "Tracer include: " + matcher);
            tracer.include(SpyMatcher.fromString(matcher.toString()));
        }
    }

    @Primitive("include-matchers")
    public void include(SpyMatcher... matchers) {
        for (SpyMatcher matcher : matchers) {
            log.info(ZorkaLogger.ZAG_CONFIG, "Tracer include: " + matcher);
            tracer.include(matcher);
        }
    }

    /**
     * Exclude classes/methods from tracer.
     *
     * @param matchers spy matcher objects (created using spy.byXxxx() functions)
     */
    @Primitive
    public void exclude(String... matchers) {
        for (String matcher : matchers) {
            log.info(ZorkaLogger.ZAG_CONFIG, "Tracer exclude: " + matcher);
            tracer.include(SpyMatcher.fromString(matcher.toString()).exclude());
        }
    }

    @Primitive("exclude-matchers")
    public void exclude(SpyMatcher... matchers) {
        for (SpyMatcher matcher : matchers) {
            log.info(ZorkaLogger.ZAG_CONFIG, "Tracer exclude: " + matcher);
            tracer.include((matcher).exclude());
        }

    }

    @Primitive("list-includes")
    public String listIncludes() {
        StringBuilder sb = new StringBuilder();
        for (SpyMatcher sm : tracer.getMatcherSet().getMatchers()) {
            sb.append(sm.hasFlags(SpyMatcher.EXCLUDE_MATCH) ? "excl: " : "incl: ");
            sb.append(sm);
            sb.append("\n");
        }
        return sb.toString();
    }


    @Primitive("begin!")
    public void traceBegin(String name) {
        traceBegin(name, 0);
    }


    @Primitive("begin-t!")
    public void traceBegin(String name, long minimumTraceTime) {
        traceBegin(name, minimumTraceTime, 0);
    }


    @Primitive("begin-tf!")
    public void traceBegin(String name, long minimumTraceTime, int flags) {
        TraceRecorder traceBuilder = tracer.getRecorder();
        traceBuilder.traceBegin(symbolRegistry.stringId(name), System.currentTimeMillis(), flags);
        traceBuilder.setMinimumTraceTime(minimumTraceTime);
    }


    @Primitive("in-trace?")
    public boolean isInTrace(String traceName) {
        return tracer.getRecorder().isInTrace(symbolRegistry.stringId(traceName));
    }


    /**
     * Adds trace attribute to trace record immediately. This is useful for programmatic attribute setting.
     *
     * @param attrName attribute name
     * @param value    attribute value
     */
    @Primitive("attr!")
    public void newAttr(String attrName, Object value) {
        tracer.getRecorder().newAttr(-1, symbolRegistry.stringId(attrName), value);
    }


    /**
     * @param traceName - trace name
     * @param attrName -
     * @param value
     */
    @Primitive("trace-attr!")
    public void newTraceAttr(String traceName, String attrName, Object value) {
        tracer.getRecorder().newAttr(symbolRegistry.stringId(traceName), symbolRegistry.stringId(attrName), value);
    }


    @Primitive("flags!")
    public void newFlags(int flags) {
        tracer.getRecorder().markTraceFlags(0, flags);
    }


    @Primitive("min-method-time")
    public long getTracerMinMethodTime() {
        return Tracer.getMinMethodTime();
    }


    /**
     * Sets minimum traced method execution time. Methods that took less time
     * will be discarded from traces and will only reflect in summary call/error counters.
     *
     * @param methodTime minimum execution time (in nanoseconds, 250 microseconds by default)
     */
    @Primitive("min-method-time!")
    public void setTracerMinMethodTime(long methodTime) {
        Tracer.setMinMethodTime(methodTime);
    }


    @Primitive("min-trace-time")
    public long getTracerMinTraceTime() {
        return Tracer.getMinTraceTime() / 1000000L;
    }


    /**
     * Sets minimum trace execution time. Traces that laster for shorted period
     * of time will be discarded. Not that this is default setting that can be
     * overridden with spy.begin() method.
     *
     * @param traceTime minimum trace execution time (50 milliseconds by default)
     */
    @Primitive("min-trace-time!")
    public void setTracerMinTraceTime(long traceTime) {
        Tracer.setMinTraceTime(traceTime * 1000000L);
    }


    @Primitive("trace-spy-methods!")
    public void setTraceSpyMethods(boolean tsm) {
        tracer.setTraceSpyMethods(tsm);
    }


    @Primitive("trace-spy-methods?")
    public boolean isTraceSpyMethods() {
        return tracer.isTraceSpyMethods();
    }

    @Primitive("overlay-class-loader")
    public ClassLoader overlayClassLoader(ClassLoader parent, String pattern, ClassLoader overlay) {
        return new OverlayClassLoader(parent, pattern, overlay);
    }

}

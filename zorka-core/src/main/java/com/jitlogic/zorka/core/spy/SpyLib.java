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



    public static final int ON_ENTER = 0;
    public static final int ON_RETURN = 1;
    public static final int ON_ERROR = 2;
    public static final int ON_SUBMIT = 3;


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
     * Creates argument fetching probe. When injected into method code by instrumentation engine, it will fetch argument
     * selected by specific index `arg`.
     *
     * @param dst name (key) used to store fetched data
     * @param arg fetched argument index
     * @return new probe
     */
    @Primitive("arg-probe")
    public SpyProbe argProbe(Object dst, int arg) {
        return new SpyArgProbe(arg, dst);
    }


    /**
     * Creates return value fetching probe. When injected into method code it will fetch return value of instrumented
     * method.
     *
     * @param dst name (key) used to store fetched data
     * @return return value fetching probe
     */
    @Primitive("ret-probe")
    public SpyProbe returnProbe(Object dst) {
        return new SpyReturnProbe(dst);
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
    @Primitive("tracer-include")
    public void include(Object ... matchers) {
        for (Object obj : matchers) {
            SpyMatcher matcher = obj instanceof SpyMatcher ? (SpyMatcher)obj : SpyMatcher.fromString(obj.toString());
            log.info(ZorkaLogger.ZAG_CONFIG, "Tracer include: " + matcher);
            tracer.include(matcher);
        }
    }

    /**
     * Exclude classes/methods from tracer.
     *
     * @param matchers spy matcher objects (created using spy.byXxxx() functions)
     */
    @Primitive("tracer-exclude")
    public void exclude(String... matchers) {
        for (Object obj : matchers) {
            SpyMatcher matcher = obj instanceof SpyMatcher ? (SpyMatcher)obj : SpyMatcher.fromString(obj.toString());
            log.info(ZorkaLogger.ZAG_CONFIG, "Tracer include: " + matcher);
            tracer.include(matcher.exclude());
        }
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

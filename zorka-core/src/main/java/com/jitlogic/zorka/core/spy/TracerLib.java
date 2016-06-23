/**
 * Copyright 2012 Rafal Lewczuk <rafal.lewczuk@jitlogic.com>
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

package com.jitlogic.zorka.core.spy;

import com.jitlogic.zorka.common.tracedata.*;
import com.jitlogic.zorka.common.util.ZorkaConfig;
import com.jitlogic.zorka.common.util.ZorkaLog;
import com.jitlogic.zorka.common.util.ZorkaLogger;
import com.jitlogic.zorka.core.util.OverlayClassLoader;
import com.jitlogic.zorka.lisp.Namespace;
import com.jitlogic.zorka.lisp.Primitive;

/**
 * Tracer library contains functions for configuring and using tracer.
 *
 * @author rafal.lewczuk@jitlogic.com
 */
@Namespace("tracer")
public class TracerLib {

    public static final ZorkaLog log = ZorkaLogger.getLog(TracerLib.class);


    private Tracer tracer;

    private SymbolRegistry symbolRegistry;

    private ZorkaConfig config;


    /**
     * Creates tracer library object.
     *
     * @param tracer reference to spy instance
     */
    public TracerLib(SymbolRegistry symbolRegistry, Tracer tracer, ZorkaConfig config) {
        this.symbolRegistry = symbolRegistry;
        this.tracer = tracer;
        this.config = config;
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

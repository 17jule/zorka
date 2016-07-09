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

package com.jitlogic.zorka.spy;

import com.jitlogic.zorka.ZorkaService;
import com.jitlogic.zorka.SymbolRegistry;

/**
 * Groups all tracer engine components and global settings.
 *
 * @author rafal.lewczuk@jitlogic.com
 */
public class Tracer implements ZorkaService {

    /**
     * Minimum default method execution time required to attach method to trace.
     */
    private static long minMethodTime = 250000;

    /**
     * Maximum number of records inside trace
     */
    private static int maxTraceRecords = 4096;

    private static long minTraceTime = 1000000000L;

    /**
     * Defines which classes and methods should be traced.
     */
    private SpyMatcherSet matcherSet;

    /**
     * Symbol registry containing names of all symbols tracer knows about.
     */
    private SymbolRegistry symbolRegistry;

    /**
     * Buffer manager for streaming tracer.
     */
    private TraceBufManager bufManager;

    /**
     * Buffer output for streaming tracer.
     */
    private TraceBufOutput bufOutput;

    /**
     * If true, methods instrumented by SPY will also be traced by default.
     */
    private boolean traceSpyMethods = true;

    /**
     * Thread local serving streaming tracer objects for application threads.
     */
    private ThreadLocal<TraceRecorder> localRecorders =
        new ThreadLocal<TraceRecorder>() {
            public TraceRecorder initialValue() {
                TraceRecorder recorder = new TraceRecorder(bufManager, symbolRegistry, bufOutput);
                recorder.setMinimumMethodTime(minMethodTime >> 16);
                return recorder;
            }
        };

    public Tracer(SpyMatcherSet matcherSet, SymbolRegistry symbolRegistry, TraceBufManager bufManager) {
        this.matcherSet = matcherSet;
        this.symbolRegistry = symbolRegistry;
        this.bufManager = bufManager;
    }


    public TraceRecorder getRecorder() {
        return localRecorders.get();
    }

    /**
     * Adds new matcher that includes (or excludes) classes and method to be traced.
     *
     * @param matcher spy matcher to be added
     */
    public void include(SpyMatcher matcher) {
        matcherSet = matcherSet.include(matcher);
    }

    public SpyMatcherSet clearMatchers() {
        SpyMatcherSet ret = matcherSet;
        matcherSet = new SpyMatcherSet();
        return ret;
    }


    @Override
    public synchronized void shutdown() {
    }


    public SpyMatcherSet getMatcherSet() {
        return matcherSet;
    }


    public void setMatcherSet(SpyMatcherSet matcherSet) {
        this.matcherSet = matcherSet;
    }


    public void setBufOutput(TraceBufOutput bufOutput) {
        this.bufOutput = bufOutput;
    }

    public static long getMinMethodTime() {
        return minMethodTime;
    }


    public static void setMinMethodTime(long methodTime) {
        minMethodTime = methodTime;
    }


    public static int getMaxTraceRecords() {
        return maxTraceRecords;
    }


    public static void setMaxTraceRecords(int traceSize) {
        maxTraceRecords = traceSize;
    }


    public boolean isTraceSpyMethods() {
        return traceSpyMethods;
    }


    public void setTraceSpyMethods(boolean traceSpyMethods) {
        this.traceSpyMethods = traceSpyMethods;
    }

    public static void setMinTraceTime(long traceTime) { Tracer.minTraceTime = traceTime; }

    public static long getMinTraceTime() { return Tracer.minTraceTime; }
}

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

package com.jitlogic.zorka.core.test.spy;

import com.jitlogic.zorka.core.test.spy.support.TestTraceBufOutput;
import com.jitlogic.zorka.core.test.support.ZorkaFixture;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class FullTracerUnitTest extends ZorkaFixture {


    private int sym(String s) {
        return agentInstance.getSymbolRegistry().stringId(s);
    }

    private TestTraceBufOutput bufOutput = new TestTraceBufOutput();

    @Before
    public void initOutput() {
        agentInstance.getTracer().setMinMethodTime(0); // Catch everything
        tracer.traceBufOutput(bufOutput);
        agentInstance.getTracer();
    }


    @Test @Ignore
    public void testSimpleTooShortTrace() throws Exception {
//        tracer.include(spy.byMethod(TCLASS1, "trivialMethod"));
//        spy.add(
//                spy.instance("X").onEnter(tracer.begin("TEST"))
//                        .include(spy.byMethod(TCLASS1, "trivialMethod")));
//        agentInstance.getTracer().setMinMethodTime(250000);
//        Object obj = instantiate(agentInstance.getClassTransformer(), TCLASS1);
//        invoke(obj, "trivialMethod");
//
//        assertEquals("should return begin, trace", 0, chunksCount(bufOutput.getChunks()));
    }


    @Test @Ignore
    public void testSimpleTrace() throws Exception {
//        long t0 = System.nanoTime() >> 16;
//
//        tracer.include(spy.byMethod(TCLASS1, "trivialMethod"));
//
//        spy.add(spy.instance("X").onEnter(tracer.begin("TEST", 0))
//            .include(spy.byMethod(TCLASS1, "trivialMethod")));
//
//        Object obj = instantiate(agentInstance.getClassTransformer(), TCLASS1);
//        invoke(obj, "trivialMethod");
//
//        long t1 = System.nanoTime() >> 16;
//
//        assertEquals("Tracer should send a trace.", 1, chunksCount(bufOutput.getChunks()));
//        Object tobj = decodeTrace(bufOutput.getChunks());
//        System.out.println(tobj);
//
//        assertTrue("Start time should be at or after T0.",
//            t0 <= (Long)ObjectInspector.get(tobj, "tstart"));
//        assertTrue("Stop time should be at or before T1.",
//            t1 >= (Long)ObjectInspector.get(tobj, "tstop"));
//        assertEquals("Trace name should be set properly",
//            symbols.stringId("TEST"), ObjectInspector.get(tobj, "begin", "trace"));
//
//        long md = (Long)ObjectInspector.get(tobj, "method");
//        int[] mdesc = symbols.methodDef((int)md);
//        assertEquals(TCLASS1, symbols.stringContent(mdesc[0]));
//        assertEquals("trivialMethod", symbols.stringContent(mdesc[1]));
//
    }


    @Test @Ignore
    public void testSimpleTraceWithName() throws Exception {
//        tracer.include(spy.byMethod(TCLASS1, "trivialStrMethod"));
//
//        spy.add(
//          spy.instance("X")
//            .onEnter(spy.fetchArg("TAG", 1), tracer.begin("${TAG}", 0))
//            .include(spy.byMethod(TCLASS1, "trivialStrMethod")));
//
//        Object obj = instantiate(agentInstance.getClassTransformer(), TCLASS1);
//        invoke(obj, "trivialStrMethod", "OJAAA");
//
//        assertEquals("Tracer should send a trace.", 1, chunksCount(bufOutput.getChunks()));
//        Object tobj = decodeTrace(bufOutput.getChunks());
//
//        assertEquals("Trace name should be properly translated.",
//            sym("OJAAA"), ObjectInspector.get(tobj, "begin", "trace"));
    }


    @Test @Ignore
    public void testSimpleTraceWithAttr() throws Exception {
//        tracer.include(spy.byMethod(TCLASS1, "trivialMethod"));
//        spy.add(spy.instance("X").onEnter(
//                tracer.begin("TEST", 0),
//                spy.put("URL", "http://some.url"),
//                tracer.attr("URL", "URL")
//        ).include(spy.byMethod(TCLASS1, "trivialMethod")));
//
//        Object obj = instantiate(agentInstance.getClassTransformer(), TCLASS1);
//        invoke(obj, "trivialMethod");
//
//        assertEquals("Tracer should send a trace.", 1, chunksCount(bufOutput.getChunks()));
//        Object tobj = decodeTrace(bufOutput.getChunks());
//
//        System.out.println(tobj);
//
//        assertEquals("Trace [URL] attribute not found.", "http://some.url", get(tobj, "attrs", sym("URL")));
    }

    @Test @Ignore
    public void testTraceAttrUpwardPropagationToNamedTrace() throws Exception {
//        tracer.include(spy.byMethod(TCLASS4, "recur*"));
//        spy.add(spy.instance("X").onEnter(tracer.begin("TEST1", 0)).include(spy.byMethod(TCLASS4, "recursive3")));
//        spy.add(spy.instance("X").onEnter(tracer.begin("TEST2", 0)).include(spy.byMethod(TCLASS4, "recursive2")));
//        spy.add(spy.instance("X").onEnter(tracer.formatTraceAttr("TEST1", "X", "XXX")).include(spy.byMethod(TCLASS4, "recursive1")));
//
//        Object obj = instantiate(agentInstance.getClassTransformer(), TCLASS4);
//        invoke(obj, "recursive3");
//
//        Object tobj = decodeTrace(bufOutput.getChunks());
//
//        assertEquals("XXX", get(tobj, "children", 0, "children", 0, "attrs", sym("TEST1"), sym("X")));
    }


    @Test @Ignore
    public void testTraceAttrUpwardPropagationToAnyTrace() throws Exception {
//        tracer.include(spy.byMethod(TCLASS4, "recur*"));
//        spy.add(spy.instance("X").onEnter(tracer.begin("TEST1", 0)).include(spy.byMethod(TCLASS4, "recursive3")));
//        spy.add(spy.instance("X").onEnter(tracer.begin("TEST2", 0)).include(spy.byMethod(TCLASS4, "recursive2")));
//        spy.add(spy.instance("X").onEnter(tracer.formatTraceAttr(null, "X", "XXX")).include(spy.byMethod(TCLASS4, "recursive1")));
//
//        Object obj = instantiate(agentInstance.getClassTransformer(), TCLASS4);
//        invoke(obj, "recursive3");
//
//        Object tobj = decodeTrace(bufOutput.getChunks());
//        System.out.println(tobj);
//        assertEquals("XXX", get(tobj, "children", 0, "children", 0, "attrs", 0, sym("X")));
    }


    @Test @Ignore
    public void testTraceAttrUpwardPropagationToUnknownTrace() throws Exception {
//        tracer.include(spy.byMethod(TCLASS4, "recur*"));
//        spy.add(spy.instance("X").onEnter(tracer.begin("TEST1", 0)).include(spy.byMethod(TCLASS4, "recursive3")));
//        spy.add(spy.instance("X").onEnter(tracer.begin("TEST2", 0)).include(spy.byMethod(TCLASS4, "recursive2")));
//        spy.add(spy.instance("X").onEnter(tracer.formatTraceAttr("TEST3", "X", "XXX")).include(spy.byMethod(TCLASS4, "recursive1")));
//
//        Object obj = instantiate(agentInstance.getClassTransformer(), TCLASS4);
//        invoke(obj, "recursive3");
//
//        Object tobj = decodeTrace(bufOutput.getChunks());
//
//        assertEquals("XXX", get(tobj, "children", 0, "children", 0, "attrs", sym("TEST3"), sym("X")));
    }


    @Test @Ignore
    // TODO GetAttr not (yet) implemented
    public void testGetTraceAttrFromUpperAndCurrentFrame() throws Exception {
//        tracer.include(spy.byMethod(TCLASS4, "recur*"));
//
//        spy.add(spy.instance("X")
//                .onEnter(tracer.begin("TEST1", 0), tracer.formatAttr("FIELD1", "XXX"))
//                .include(spy.byMethod(TCLASS4, "recursive3")));
//
//        spy.add(spy.instance("X")
//                .onEnter(tracer.begin("TEST2", 0),
//                    tracer.formatAttr("FIELD2", "YYY"),
//                    tracer.getTraceAttr("FIELD2C", "FIELD2"),
//                    tracer.attr("FIELD2C", "FIELD2C"),
//                    tracer.getTraceAttr("FIELD1C", "TEST1", "FIELD1"),
//                    tracer.attr("FIELD1C", "FIELD1C"))
//                .include(spy.byMethod(TCLASS4, "recursive1")));
//
//        Object obj = instantiate(agentInstance.getClassTransformer(), TCLASS4);
//        invoke(obj, "recursive3");

        // Check if both traces came back
//        assertEquals(2, results.size());
//        assertEquals("TEST2", symbols.stringContent(results.get(0).getMarker().getTraceId()));
//        assertEquals("TEST1", symbols.stringContent(results.get(1).getMarker().getTraceId()));
//
//        // Check standard attributes are set
//        assertEquals("YYY", results.get(0).getAttr(symbols.stringId("FIELD2")));
//        assertEquals("XXX", results.get(1).getAttr(symbols.stringId("FIELD1")));
//
//        // Check if attributes taken from another trace are set
//        assertEquals("XXX", results.get(0).getAttr(symbols.stringId("FIELD1C")));
//        assertEquals("YYY", results.get(0).getAttr(symbols.stringId("FIELD2C")));
    }


    @Test @Ignore
    // TODO Trace error flag not (yet) implemented
    public void testTraceFlagsUpwardPropagation() throws Exception {
//        tracer.include(spy.byMethod(TCLASS4, "recur*"));
//        spy.add(spy.instance("X").onEnter(tracer.begin("TEST1", 0)).include(spy.byMethod(TCLASS4, "recursive3")));
//        spy.add(spy.instance("X").onEnter(tracer.begin("TEST2", 0)).include(spy.byMethod(TCLASS4, "recursive2")));

//        spy.add(spy.instance()
//                .onEnter(tracer.traceFlags("TEST1", TraceMarker.ERROR_MARK))
//                .include(spy.byMethod(TCLASS4, "recursive1")));
//
//        Object obj = instantiate(agentInstance.getClassTransformer(), TCLASS4);
//        invoke(obj, "recursive3");
//
//        assertEquals("should return two traces", 2, results.size());
//
//        assertTrue("Error flag should be enabled for TEST1 trace",
//                results.get(1).getMarker().hasFlag(TraceMarker.ERROR_MARK));
//
//        assertFalse("Error flag should be disabled for TEST2 trace",
//                results.get(0).getMarker().hasFlag(TraceMarker.ERROR_MARK));
    }


    @Test @Ignore
    // TODO inTrace() not (yet) implemented
    public void testInTraceCheckerPositiveCheck() throws Exception {
//        tracer.include(spy.byMethod(TCLASS4, "recur*"));
//        spy.add(spy.instance("X").onEnter(tracer.begin("TEST1", 0)).include(spy.byMethod(TCLASS4, "recursive3")));
//
//        spy.add(spy.instance("X")
//                .onEnter(spy.subchain(tracer.inTrace("TEST1"), tracer.formatTraceAttr("TEST1", "IN", "YES")))
//                .include(spy.byMethod(TCLASS4, "recursive1")));
//
//        Object obj = instantiate(agentInstance.getClassTransformer(), TCLASS4);
//        invoke(obj, "recursive3");

//        assertEquals("should return one trace", 1, results.size());
//
//        assertEquals("YES", results.get(0).getAttr(symbols.stringId("IN")));
    }

    @Test @Ignore
    // TODO inTrace() not (yet) implemented
    public void testInTraceCheckerNegativeCheck() throws Exception {
//        tracer.include(spy.byMethod(TCLASS4, "recur*"));
//        spy.add(spy.instance("X").onEnter(tracer.begin("TEST1", 0)).include(spy.byMethod(TCLASS4, "recursive3")));
//
//        spy.add(spy.instance("X")
//                .onEnter(spy.subchain(tracer.inTrace("TEST2"), tracer.formatTraceAttr("TEST1", "IN", "YES")))
//                .include(spy.byMethod(TCLASS4, "recursive1")));
//
//        Object obj = instantiate(agentInstance.getClassTransformer(), TCLASS4);
//        invoke(obj, "recursive3");

//        assertEquals("should return one trace", 1, results.size());
//
//        assertEquals(null, results.get(0).getAttr(symbols.stringId("IN")));
    }


    @Test @Ignore
    public void testTraceSpyMethodsFlagOn() throws Exception {
//        spy.add(spy.instance("X").onEnter(tracer.begin("TEST1", 0)).include(spy.byMethod(TCLASS4, "recursive3")));
//
//        spy.add(spy.instance("X")
//                .onEnter(spy.put("AA", "OJA"))
//                .include(spy.byMethod(TCLASS4, "recursive1")));
//
//        agentInstance.getTracer().setTraceSpyMethods(true);
//
//        Object obj = instantiate(agentInstance.getClassTransformer(), TCLASS4);
//        invoke(obj, "recursive3");
//
//        Object tobj = decodeTrace(bufOutput.getChunks());
//
//        Long md = get(tobj, "children", 0, "method");
//
//        assertNotNull("Child method should exist", md);
//
//        int[] mdef = symbols.methodDef((int)(long)md);
//
//        assertEquals(sym("recursive1"), mdef[1]);
    }


    @Test @Ignore
    public void testTraceSpyMethodsFlagOff() throws Exception {
//        spy.add(spy.instance("X").onEnter(tracer.begin("TEST1", 0)).include(spy.byMethod(TCLASS4, "recursive3")));
//
//        spy.add(spy.instance("X")
//                .onEnter(spy.put("AA", "OJA"))
//                .include(spy.byMethod(TCLASS4, "recursive1")));
//
//        agentInstance.getTracer().setTraceSpyMethods(false);
//
//        Object obj = instantiate(agentInstance.getClassTransformer(), TCLASS4);
//        invoke(obj, "recursive3");
//
//        Object tobj = decodeTrace(bufOutput.getChunks());
//
//        assertNull("Child method should NOT exist", get(tobj, "children"));
    }


    @Test @Ignore
    public void testTraceSpyMethodsFlagOffAndOneMethodManuallyAddedToTracer() throws Exception {
//        tracer.include(spy.byMethod(TCLASS4, "recursive3"));
//        spy.add(spy.instance("X").onEnter(tracer.begin("TEST1", 0)).include(spy.byMethod(TCLASS4, "recursive3")));
//
//        spy.add(spy.instance("X")
//                .onEnter(spy.put("AA", "OJA"))
//                .include(spy.byMethod(TCLASS4, "recursive1")));
//
//        agentInstance.getTracer().setTraceSpyMethods(false);
//
//        Object obj = instantiate(agentInstance.getClassTransformer(), TCLASS4);
//        invoke(obj, "recursive3");
//
//        Object tobj = decodeTrace(bufOutput.getChunks());
//
//        assertNull("trace should not register recursive1 method", get(tobj, "children"));
    }

}

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

import com.jitlogic.zorka.common.util.ObjectInspector;
import com.jitlogic.zorka.core.AgentConfig;
import com.jitlogic.zorka.core.spy.SpyDefinition;
import com.jitlogic.zorka.core.spy.SpyLib;
import com.jitlogic.zorka.core.test.support.BytecodeInstrumentationFixture;

import com.jitlogic.zorka.lisp.Symbol;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.net.URL;

import static com.jitlogic.zorka.core.test.support.TestUtil.instantiate;
import static com.jitlogic.zorka.core.test.support.TestUtil.invoke;

public class LispInstrumentationUnitTest extends BytecodeInstrumentationFixture {
    @Before
    public void setUpTestConf() throws Exception {
        URL url = getClass().getResource("/cfgspy");
        AgentConfig config = new AgentConfig(url.getPath());
        ObjectInspector.setField(zorkaAgent, "config", config);
        ObjectInspector.setField(zorka, "config", config);
        zorkaAgent.loadScripts();
        zorkaAgent.loadScript("zorka.zcm");
        zorkaAgent.loadScript("LispInstrumentationUnitTest.zcm");
    }


    @Test
    public void testDefineSimpleInstrumentationAndCheckHowItHasBeenRegistered() throws Exception {
        // Configure instrumentation
        System.out.println(zorkaAgent.eval(
            "(spy/defi single-trivial-method-test " +
            " (on \"com.jitlogic.zorka.core.test.spy.support.TestClass1/trivialMethod\"" +
                " \"TestClass/otherMethod\")" +
            " (on \"com.myapp.OtherClass/yetAnotherMethod\")" +
            " (on-enter (ctx) (set! test-passed 1) ctx)" +
                ")"));

        // Check sdef
        Object sobj = zorkaAgent.getInterpreter().env().lookup(Symbol.symbol("single-trivial-method-test"));
        assertNotNull(sobj);

        SpyDefinition sdef = (SpyDefinition)sobj;
        assertEquals("single-trivial-method-test", ((SpyDefinition) sobj).getName());

        assertEquals(3, sdef.getMatcherSet().size());

        assertEquals(1, sdef.getProcessors(SpyLib.ON_ENTER).size());

        //Object obj = instantiate(engine, TCLASS1);
        //invoke(obj, "trivialMethod");

        //assertEquals(1, zorkaAgent.eval("test-passed"));
    }


    @Test
    public void testTrivialOnEnterInstrumentation() throws Exception {
        zorkaAgent.eval(
            "(spy/defi single-trivial-method-test " +
                " (on \"com.jitlogic.zorka.core.test.spy.support.TestClass1/trivialMethod\")" +
                " (on-enter (ctx) (set! test-passed 1) (println 123) ctx)" +
                ")");

        Object obj = instantiate(engine, TCLASS1);
        invoke(obj, "trivialMethod");

        assertEquals(1, zorkaAgent.eval("test-passed"));
    }

}

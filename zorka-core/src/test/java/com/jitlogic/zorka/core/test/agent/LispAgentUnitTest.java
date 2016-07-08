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

package com.jitlogic.zorka.core.test.agent;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import com.jitlogic.zorka.common.util.ObjectInspector;
import com.jitlogic.zorka.core.*;
import com.jitlogic.zorka.core.test.support.ZorkaFixture;
import com.jitlogic.zorka.core.util.ObjectDumper;
import com.jitlogic.zorka.lisp.Namespace;
import com.jitlogic.zorka.lisp.Primitive;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class LispAgentUnitTest extends ZorkaFixture {

    @Before
    public void setUp() throws Exception {
        mBeanServerRegistry.register("java", java.lang.management.ManagementFactory.getPlatformMBeanServer(), null);
    }


    @Test
    public void testTrivialQuery() throws Exception {
        assertEquals("5", zorkaAgent.query("(+ 2 3)"));
    }


    @Test
    public void testJmxCalls() throws Exception {
        assertEquals("1.0", zorkaAgent.query("(zorka/jmx \"java\" \"java.lang:type=Runtime\" \"SpecVersion\")"));
    }


    @Test
    public void testZabbixDiscoveryFunc() throws Exception {
        Map<String, List<Map<String, String>>> obj = (Map<String, List<Map<String, String>>>)
                zorkaAgent.eval("(zabbix/_discovery-2 \"java\" \"java.lang:type=MemoryPool,*\" \"name\" \"type\")");
        assertTrue("should return map", obj != null);
        List<Map<String, String>> data = obj.get("data");
        assertTrue("obj.data should be non-empty", data.size() > 0);
    }


    @Test
    public void testNewBshEllipsis() throws Exception {
        zorkaAgent.install(new SomeTestLib());
        String rslt = zorkaAgent.query("(test/join \"a\" \"b\" \"c\")");
        assertEquals("a:bc", rslt);
    }


    @Test
    public void testStartAndLoadProfilesAndScripts() throws Exception {
        URL url = getClass().getResource("/cfgp");
        AgentConfig config = new AgentConfig(url.getPath());
        ObjectInspector.setField(zorkaAgent, "config", config);
        ObjectInspector.setField(zorka, "config", config);
        zorkaAgent.loadScripts();
        assertNotNull("jvm/jvm.bsh script should be loaded.", zorkaAgent.get("jvm_bsh"));
        //assertFalse("profile.scripts properties should be filtered off", config.hasCfg("profile.scripts"));
        assertNotNull("test/test.bsh script should be loaded.", zorkaAgent.get("test_bsh"));
        assertEquals("jvm/jvm.bsh script should be called only once.", "bar", zorkaAgent.get("not_to_be_overridden"));
        assertNotNull("common.bsh script should be indirectly loaded via jvm.bsh", zorkaAgent.get("common_bsh"));
    }

    @Test
    public void testIfRequireSkipsAlreadyLoadedScriptFromFilesystem() throws Exception {
        URL url = getClass().getResource("/cfgp");
        AgentConfig config = new AgentConfig(url.getPath());
        ObjectInspector.setField(zorkaAgent, "config", config);
        ObjectInspector.setField(zorka, "config", config);
        zorka.require("test/test.zcm");
        assertEquals("Should load first script", 1, zorkaAgent.get("test_bsh"));
        zorka.require("test/test2.zcm");
        assertEquals("Should load second script", 2, zorkaAgent.get("test_bsh"));
        zorka.require("test/test1.zcm");
        assertEquals("Should refuse to load first script again", 2, zorkaAgent.get("test_bsh"));
    }

    @Test
    public void testIfRequireSkipsAlreadyLoadedScriptFromClasspath() throws Exception {
        URL url = getClass().getResource("/conf");
        AgentConfig config = new AgentConfig(url.getPath());
        ObjectInspector.setField(zorkaAgent, "config", config);
        ObjectInspector.setField(zorka, "config", config);
        zorka.require("test.zcm");
        assertEquals("Should load first script", 1, zorkaAgent.get("test_bsh"));
        zorka.require("test2.zcm");
        assertEquals("Should load second script", 2, zorkaAgent.get("test_bsh"));
        zorka.require("test.zcm");
        assertEquals("Should refuse to load first script again", 2, zorkaAgent.get("test_bsh"));
    }

    @Test
    public void testObjectDumper() throws Exception {
        Object obj = query("(zorka/jmx \"java\" \"java.lang:type=Runtime\")");
        String s = ObjectDumper.objectDump(obj);
        assertTrue(s != null && s.length() > 100);
    }


    private Object query(final String src) throws Exception {
        ZorkaBasicCallback callback = new ZorkaBasicCallback();
        ZorkaLispWorker worker = new ZorkaLispWorker(Executors.newSingleThreadExecutor(), 5000,
                agentInstance.getZorkaAgent(), src, callback);
        worker.run();
        return callback.getResult();
    }

    @Namespace("test")
    public static class SomeTestLib {
        @Primitive
        public String join(String foo, String... parts) {
            StringBuilder sb = new StringBuilder();
            sb.append(foo);
            sb.append(":");
            for (String part : parts)
                sb.append(part);
            return sb.toString();
        }
    }
}

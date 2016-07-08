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
package com.jitlogic.zorka.core.test.support;

import com.jitlogic.zorka.common.test.support.TestJmx;
import com.jitlogic.zorka.core.*;
import com.jitlogic.zorka.core.mbeans.MBeanServerRegistry;
import com.jitlogic.zorka.core.spy.*;

import com.jitlogic.zorka.common.test.support.TestUtil;

import com.jitlogic.zorka.common.tracedata.SymbolRegistry;
import org.junit.After;
import org.junit.Before;

import javax.management.ObjectName;
import java.io.File;
import java.util.Properties;

public class ZorkaFixture {

    protected MBeanServerRegistry mBeanServerRegistry;

    protected AgentInstance agentInstance;
    protected SpyClassTransformer spyTransformer;

    protected SpyLib spy;

    protected ZorkaLispAgent zorkaAgent;
    protected ZorkaLib zorka;

    protected AgentConfig config;

    protected SymbolRegistry symbols;

    protected UtilLib util;


    private String tmpDir;

    @Before
    public void setUpFixture() throws Exception {

        // Configure and spawn agent instance ...

        config = new AgentConfig("/tmp");
        agentInstance = new TestAgentInstance(config, new DummySpyRetransformer(null, config));
        agentInstance.start();

        // Get all agent components used by tests

        mBeanServerRegistry = agentInstance.getMBeanServerRegistry();
        zorkaAgent = agentInstance.getZorkaAgent();
        zorka = agentInstance.getZorkaLib();
        spy = agentInstance.getSpyLib();
        spyTransformer = agentInstance.getClassTransformer();
        util = agentInstance.getUtilLib();

        // Install test MBean server

        //mBeanServerRegistry.register("test", testMbs, testMbs.getClass().getClassLoader());

        MainSubmitter.setSubmitter(agentInstance.getSubmitter());
        MainSubmitter.setTracer(agentInstance.getTracer());

        symbols = agentInstance.getSymbolRegistry();

        tmpDir = "/tmp" + File.separatorChar + "zorka-unit-test";
        TestUtil.rmrf(tmpDir);
        new File(tmpDir).mkdirs();
    }


    @After
    public void tearDownFixture() throws Exception {

        // Uninstall test MBean server
        mBeanServerRegistry.unregister("test");

        MainSubmitter.setSubmitter(null);
        MainSubmitter.setTracer(null);
    }


    public String getTmpDir() {
        return tmpDir;
    }


    public TestJmx makeTestJmx(String name, long nom, long div) throws Exception {
        TestJmx bean = new TestJmx();
        bean.setNom(nom);
        bean.setDiv(div);

        //testMbs.registerMBean(bean, new ObjectName(name));

        return bean;
    }

}

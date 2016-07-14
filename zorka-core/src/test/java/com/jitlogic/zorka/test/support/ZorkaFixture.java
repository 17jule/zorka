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
package com.jitlogic.zorka.test.support;

import com.jitlogic.zorka.*;
import com.jitlogic.zorka.test.utils.support.TestJmx;
import com.jitlogic.zorka.mbeans.MBeanServerRegistry;
import com.jitlogic.zorka.spy.*;

import com.jitlogic.zorka.test.utils.support.TestUtil;

import com.jitlogic.zorka.SymbolRegistry;
import com.jitlogic.zorka.util.ZorkaLogLevel;
import com.jitlogic.zorka.util.ZorkaLogger;
import com.jitlogic.zorka.util.ZorkaTrapper;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

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

    protected List<String> logs = new ArrayList<String>();

    private String tmpDir;

    @Before
    public void setUpFixture() throws Exception {

        File f = new File(this.getClass().getResource("/test/cfg/zorka.conf").getPath());

        if (!f.canRead()) {
            fail("Cannot find test config: " + f);
        }

        tmpDir = "/tmp" + File.separatorChar + "zorka-unit-test";
        TestUtil.rmrf(tmpDir);
        if (!new File(tmpDir).mkdirs()) {
            fail("Cannot create temporary directory: " + tmpDir);
        }

        // Configure and spawn agent instance ...

        config = new AgentConfig(f.getParent());
        agentInstance = new TestAgentInstance(config, new DummySpyRetransformer(null, config));

        ZorkaLogger.getLogger().addTrapper(
            new ZorkaTrapper() {
                @Override
                public void trap(ZorkaLogLevel logLevel, String tag, String msg, Throwable e, Object... args) {
                    logs.add(logLevel + "|" + tag + "|" + msg + "|" + e);
                    if (e != null) e.printStackTrace();
                }
        });

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

    }


    @After
    public void tearDownFixture() throws Exception {

        // Uninstall test MBean server
        //mBeanServerRegistry.unregister("test");

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

    protected void printLogs() {
        for (String s : logs) {
            System.out.println(s);
        }
    }

}

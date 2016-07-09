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

package com.jitlogic.zorka;

import com.jitlogic.zorka.stats.AgentDiagnostics;
import com.jitlogic.zorka.stats.MethodCallStatistics;
import com.jitlogic.zorka.stats.ValGetter;
import com.jitlogic.zorka.util.ZorkaLog;
import com.jitlogic.zorka.util.ZorkaLogger;
import com.jitlogic.zorka.util.ZorkaUtil;
import com.jitlogic.zorka.mbeans.AttrGetter;
import com.jitlogic.zorka.spy.*;
import com.jitlogic.zorka.mbeans.MBeanServerRegistry;
import com.jitlogic.zorka.util.DaemonThreadFactory;

import java.util.Set;
import java.util.concurrent.*;

import static com.jitlogic.zorka.AgentConfigConstants.*;

/**
 * This method binds together all components to create fuunctional Zorka agent. It is responsible for
 * initializing all subsystems (according to property values in zorka configuration file) and starting
 * all service threads if necessary. It retains references to created components and maintains reference
 * to MBean server registry and its own instance singletons.
 *
 * @author rafal.lewczuk@jitlogic.com
 */
public class AgentInstance implements ZorkaService {

    /**
     * Logger
     */
    private final ZorkaLog log = ZorkaLogger.getLog(this.getClass());

    /**
     * MBean server registry
     */
    protected MBeanServerRegistry mBeanServerRegistry;

    /**
     * Handles accepted connections.
     */
    protected Executor connExecutor;

    /**
     * Handles BSH requests (called from connection handlers).
     */
    protected ExecutorService mainExecutor;

    /**
     * Handles scheduled tasks
     */
    protected ScheduledExecutorService scheduledExecutor;
    
    /**
     * Main zorka agent object - one that executes actual requests
     */
    protected ZorkaLispAgent zorkaAgent;

    /**
     * Reference to zorka library - basic agent functions available to zorka scripts as 'zorka.*'
     */
    protected ZorkaLib zorkaLib;


    /**
     * Reference to spy library - available to zorka scripts as 'spy.*' functions
     */
    protected SpyLib spyLib;

    protected UtilLib utilLib;

    protected MethodCallStatistics stats = new MethodCallStatistics();

    protected Tracer tracer;

    protected SpyClassTransformer classTransformer;

    protected DispatchingSubmitter submitter;

    protected AgentConfig config;

    protected SpyRetransformer retransformer;

    public AgentInstance(AgentConfig config, SpyRetransformer retransformer) {
        this.config = config;

        this.retransformer = retransformer;
    }

    /**
     * Starts agent. Real startup sequence is performed here.
     */
    public void start() {


        initLibs();

        zorkaAgent.initialize();

        if (config.boolVal(true, KW_ZORKA, KW_DIAGNOSTICS, KW_ENABLED)) {
            createZorkaDiagMBean();
        }
    }


    private void initLibs() {

        getZorkaAgent().install(getZorkaLib());
        getZorkaAgent().install(getUtilLib());

        if (config.boolVal(true, KW_ZORKA, KW_SPY, KW_ENABLED)) {
            log.info(ZorkaLogger.ZAG_CONFIG, "Enabling Zorka SPY");
            getZorkaAgent().install(getSpyLib());
        }
    }


    public void createZorkaDiagMBean() {
        String mbeanName = config.strVal(null, KW_ZORKA, KW_DIAGNOSTICS, KW_MBEAN);

        MBeanServerRegistry registry = getMBeanServerRegistry();

        registry.getOrRegister("java", mbeanName, "Version", config.strVal("2.x", KW_VERSION), "Agent Diagnostics");


        for (int i = 0; i < AgentDiagnostics.numCounters(); i++) {
            final int counter = i;
            registry.getOrRegister("java", mbeanName, AgentDiagnostics.getName(counter),
                    new ValGetter() {
                        @Override
                        public Object get() {
                            return AgentDiagnostics.get(counter);
                        }
                    });

        }

        registry.getOrRegister("java", mbeanName, "SymbolsCreated",
                new AttrGetter(getSymbolRegistry(), "size()"));

        registry.getOrRegister("java", mbeanName, "stats", stats);
    }


    public AgentConfig getConfig() {
        return config;
    }


    private SpyMatcherSet tracerMatcherSet;

    public synchronized SpyMatcherSet getTracerMatcherSet() {
        if (tracerMatcherSet == null) {
            tracerMatcherSet = new SpyMatcherSet();
        }
        return tracerMatcherSet;
    }


    private SymbolRegistry symbolRegistry;

    public synchronized SymbolRegistry getSymbolRegistry() {
        if (symbolRegistry == null) {
            symbolRegistry = new SymbolRegistry();
        }
        return symbolRegistry;
    }


    private synchronized Executor getConnExecutor() {
        if (connExecutor == null) {
            int rt = config.intVal(8, KW_ZORKA, KW_EXECUTOR, KW_THREADS);
            connExecutor = new ThreadPoolExecutor(rt, rt, 1000, TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<Runnable>(config.intVal(64, KW_ZORKA, KW_EXECUTOR, KW_QUEUE)),
                    new DaemonThreadFactory("ZORKA-conn-pool"));
        }
        return connExecutor;
    }


    private synchronized ExecutorService getMainExecutor() {
        if (mainExecutor == null) {
            int rt = config.intVal(8, KW_ZORKA, KW_EXECUTOR, KW_THREADS);
            mainExecutor = new ThreadPoolExecutor(rt, rt, 1000, TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<Runnable>(config.intVal(64, KW_ZORKA, KW_EXECUTOR, KW_QUEUE)),
                    new DaemonThreadFactory("ZORKA-main-pool"));
        }
        return mainExecutor;
    }

    private synchronized ScheduledExecutorService getScheduledExecutor() {
        if (scheduledExecutor == null) {
            int rt = config.intVal(8, KW_ZORKA, KW_EXECUTOR, KW_THREADS);
            scheduledExecutor = Executors.newScheduledThreadPool(rt, new DaemonThreadFactory("ZORKA-thread-pool"));
        }
        return scheduledExecutor;
    }

    private TraceBufManager bufManager;

    private synchronized TraceBufManager getBufManager() {
        if (bufManager == null) {
            bufManager = new TraceBufManager(65536, 16);
        }
        return bufManager;
    }

    public synchronized Tracer getTracer() {
        if (tracer == null) {
            tracer = new Tracer(getTracerMatcherSet(), getSymbolRegistry(), getBufManager());
            MainSubmitter.setTracer(getTracer());
        }
        return tracer;
    }


    public synchronized SpyRetransformer getRetransformer() {
        return retransformer;
    }


    public synchronized SpyClassTransformer getClassTransformer() {
        if (classTransformer == null) {
            classTransformer = new SpyClassTransformer(getSymbolRegistry(), getTracer(),
                getConfig().boolVal(true, KW_ZORKA, KW_SPY, KW_COMPUTE_FRAMES), stats, getRetransformer());
        }
        return classTransformer;
    }

    public synchronized DispatchingSubmitter getSubmitter() {
        if (submitter == null) {
            submitter = new DispatchingSubmitter(getClassTransformer());
        }
        return submitter;
    }

    /**
     * Returns reference to BSH agent.
     *
     * @return instance of Zorka BSH agent
     */
    public synchronized ZorkaLispAgent getZorkaAgent() {
        if (zorkaAgent == null) {
            long timeout = config.intVal(5000, KW_ZORKA, KW_EXECUTOR, KW_TIMEOUT);
            zorkaAgent = new ZorkaLispAgent(getConnExecutor(), getMainExecutor(), timeout, config);
        }
        return zorkaAgent;
    }


    public synchronized ZorkaLib getZorkaLib() {
        if (zorkaLib == null) {
            zorkaLib = new ZorkaLib(this);
        }
        return zorkaLib;
    }


    public synchronized UtilLib getUtilLib() {
        if (utilLib == null) {
            utilLib = new UtilLib();
        }
        return utilLib;
    }



    /**
     * Returns reference to Spy library
     *
     * @return instance of spy library
     */
    public synchronized SpyLib getSpyLib() {

        if (spyLib == null) {
            spyLib = new SpyLib(getClassTransformer(), getMBeanServerRegistry(), getZorkaAgent(), getTracer(), getSymbolRegistry());
        }

        return spyLib;
    }


    /**
     * Returns reference to mbean server registry.
     *
     * @return mbean server registry reference of null (if not yet initialized)
     */
    public MBeanServerRegistry getMBeanServerRegistry() {

        if (mBeanServerRegistry == null) {
            mBeanServerRegistry = new MBeanServerRegistry();
        }

        return mBeanServerRegistry;
    }


    @Override
    public void shutdown() {

        log.info(ZorkaLogger.ZAG_CONFIG, "Shutting down agent ...");

        tracer.clearMatchers();
        tracer.shutdown();

        if (zorkaLib != null) {
            zorkaLib.shutdown();
        }

    }


    public void restart() {
        log.info(ZorkaLogger.ZAG_CONFIG, "Reloading agent configuration...");
        config.reload();
        ZorkaLogger.getLogger().shutdown();
        log.info(ZorkaLogger.ZAG_CONFIG, "Agent configuration reloaded ...");

        getZorkaAgent().restart();
        initLibs();
        getZorkaAgent().reloadScripts();
        long l = AgentDiagnostics.get(AgentDiagnostics.CONFIG_ERRORS);
        log.info(ZorkaLogger.ZAG_CONFIG, "Agent configuration scripts executed (" + l + " errors).");
        log.info(ZorkaLogger.ZAG_CONFIG, "Number of matchers in tracer configuration: "
                + tracer.getMatcherSet().getMatchers().size());


    }

    public void reload() {
        SpyMatcherSet oldSet = getTracer().getMatcherSet();
        SpyClassTransformer classTransformer = getClassTransformer();
        Set<SpyDefinition> oldSdefs = classTransformer.getSdefs();
        shutdown();
        ZorkaUtil.sleep(1000);
        restart();
        long l = AgentDiagnostics.get(AgentDiagnostics.CONFIG_ERRORS);
        if (l == 0) {
            SpyMatcherSet newSet = getTracer().getMatcherSet();
            log.info(ZorkaLogger.ZAG_CONFIG, "Reinstrumenting classes for tracer ...");
            getRetransformer().retransform(oldSet, newSet, false);
            log.info(ZorkaLogger.ZAG_CONFIG, "Checking for old sdefs to be removed...");
            int removed = 0;
            for (SpyDefinition sdef : oldSdefs) {
                if (sdef == classTransformer.getSdef(sdef.getName())) {
                    classTransformer.remove(sdef);
                    removed++;
                }
            }
            log.info(ZorkaLogger.ZAG_CONFIG, "Number of sdefs removed: " + removed);
        } else {
            log.info(ZorkaLogger.ZAG_CONFIG,
                    "Reinstrumentating classes for tracer skipped due to configuration errors. Fix config scripts and try again.");
            getTracer().setMatcherSet(oldSet);
        }

    }
}

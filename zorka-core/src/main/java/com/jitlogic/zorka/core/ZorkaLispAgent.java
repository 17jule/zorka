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

package com.jitlogic.zorka.core;

import com.jitlogic.zorka.common.ZorkaAgent;
import com.jitlogic.zorka.common.ZorkaService;
import com.jitlogic.zorka.common.stats.AgentDiagnostics;
import com.jitlogic.zorka.common.util.ZorkaLog;
import com.jitlogic.zorka.common.util.ZorkaLogger;
import com.jitlogic.zorka.common.util.ZorkaUtil;
import com.jitlogic.zorka.core.util.ObjectDumper;
import com.jitlogic.zorka.lisp.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import static com.jitlogic.zorka.lisp.StandardLibrary.arrayList;

import static com.jitlogic.zorka.core.AgentConfigConstants.*;

public class ZorkaLispAgent implements ZorkaAgent, ZorkaService {

    /**
     * Logger
     */
    private final ZorkaLog log = ZorkaLogger.getLog(this.getClass());

    /**
     * LISP interpreter.
     */
    Interpreter interpreter;

    /**
     * Executor for asynchronous processing queries
     */
    private Executor connExecutor;

    private ExecutorService mainExecutor;

    private AgentConfig config;

    private long timeout;

    private boolean initialized;

    private Set<String> loadedScripts = new HashSet<String>();


    /**
     * Standard constructor.
     *
     * @param connExecutor connExecutor for asynchronous processing queries
     */
    public ZorkaLispAgent(Executor connExecutor, ExecutorService mainExecutor,
                          long timeout, AgentConfig config) {
        this.interpreter = new Interpreter();
        interpreter.install(new StandardLibrary(interpreter));
        interpreter.evalScript("/com/jitlogic/zorka/lisp/boot.zcm");

        this.connExecutor = connExecutor;
        this.mainExecutor = mainExecutor;
        this.timeout = timeout;
        this.config = config;
    }


    public void install(Object lib) {
        interpreter.install(lib);
    }

    /**
     * Installs object in LISP namespace. Typically used to install
     * objects as function libraries.
     *
     * @param name name in beanshell namespace
     * @param obj  object
     */
    public void put(String name, Object obj) {
        interpreter.env().define(Symbol.symbol(name), obj);
    }


    public Object get(String name) {
        return interpreter.env().lookup(Symbol.symbol(name));
    }


    /**
     * Evaluates LISP query. If error occurs, it returns exception text with stack dump.
     *
     * @param expr query string
     * @return response string
     */
    @Override
    public String query(String expr) {
        try {
            return "" + interpreter.eval(new Reader(expr).read());
        } catch (Exception e) {
            log.error(ZorkaLogger.ZAG_ERRORS, "Error evaluating '" + expr + "': ", e);
            return ObjectDumper.errorDump(e);
        }
    }


    /**
     * Evaluates LISP query. If evaluation error occurs, it is thrown out as EvalError.
     *
     * @param expr query string
     * @return evaluation result
     */
    public Object eval(String expr) {
        return interpreter.eval(new Reader(expr).read());
    }


    /**
     * Executes query asynchronously. Result is returned via callback object.
     *
     * @param expr     BSH expression
     * @param callback callback object
     */
    public void exec(String expr, ZorkaCallback callback) {
        log.debug(ZorkaLogger.ZAG_TRACE, "Processing request BSH expression: " + expr);
        ZorkaLispWorker worker = new ZorkaLispWorker(mainExecutor, timeout, this, expr, callback);
        connExecutor.execute(worker);
    }


    /**
     * Loads and executes beanshell script.
     *
     * @param script path to script
     */
    public synchronized String loadScript(String script) {
        String path = ZorkaUtil.path(config.strVal(null, KW_SCRIPTS_DIR), script);
        InputStream is  = null;
        try {
            if (new File(path).canRead()) {
                is = new FileInputStream(path);
            } else {
                is = getClass().getResourceAsStream(
                    "/com/jitlogic/zorka/scripts"+(script.startsWith("/") ? "" : "/")+script);
            }

            for (Seq seq = new Reader(is).readAll(); seq != null; seq = (Seq)seq.rest()) {
                interpreter.eval(seq.first());
            }
            loadedScripts.add(script);
            return "OK";
        } catch (Exception e) {
            log.error(ZorkaLogger.ZAG_ERRORS, "Error executing script " + script, e);
            AgentDiagnostics.inc(AgentDiagnostics.CONFIG_ERRORS);
            return "Error: " + e.getMessage();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    log.error(ZorkaLogger.ZAG_ERRORS, "Error closing script " + script, e);
                }
            }
        }
    }


    public synchronized String require(String script) {
        if (!loadedScripts.contains(script)) {
            return loadScript(script);
        } else {
            return "Already loaded.";
        }
    }


    /**
     * Loads and executes all script in script directory.
     */
    public void loadScripts() {
        String scriptsDir = config.strVal(null, KW_SCRIPTS_DIR);

        if (scriptsDir == null) {
            log.error(ZorkaLogger.ZAG_ERRORS, "Scripts directory not set. Internal error ?!?");
            return;
        }

        for (Object script : Utils.iterable(config.seqVal(null, KW_SCRIPTS))) {
            require((String)script);
        }

    }


    public synchronized void reloadScripts() {
        loadedScripts.clear();
        AgentDiagnostics.clear(AgentDiagnostics.CONFIG_ERRORS);
        loadScripts();
    }


    public void initialize() {
        loadScripts();
        initialized = true;
    }


    public boolean isInitialized() {
        return initialized;
    }


    public void restart() {
        interpreter = new Interpreter();
    }


    public Interpreter getInterpreter() {
        return interpreter;
    }


    @Override
    public void shutdown() {

    }
}

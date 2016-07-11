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
package com.jitlogic.zorka.net;

import com.jitlogic.zorka.ZorkaLispAgent;
import com.jitlogic.zorka.ZorkaService;
import com.jitlogic.zorka.lisp.*;
import com.jitlogic.zorka.stats.AgentDiagnostics;
import com.jitlogic.zorka.util.ZorkaLog;
import com.jitlogic.zorka.util.ZorkaLogger;

import static com.jitlogic.zorka.lisp.StandardLibrary.car;
import static com.jitlogic.zorka.lisp.StandardLibrary.cdr;
import static com.jitlogic.zorka.lisp.StandardLibrary.cons;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * General purpose agent that listens on a designated TCP port and
 *
 * @author rafal.lewczuk@jitlogic.com
 */
public class TcpServer implements Runnable, ZorkaService {

    /**
     * Logger
     */
    private final ZorkaLog log = ZorkaLogger.getLog(this.getClass());

    /**
     * BSH agent
     */
    private ZorkaLispAgent agent;

    private Interpreter interpterer;

    private Environment env;

    /**
     * Connections accepting thread
     */
    private Thread thread;

    /**
     * Thread main loop will run as long as this attribute is true
     */
    private volatile boolean running;

    /**
     * Name prefix (will appear in thread name, configuration properties will start with this prefix etc.)
     */
    private String name;

    /**
     * TCP listen port
     */
    private int listenPort;

    private String defaultAddr;

    private int defaultPort;

    private LispMap config;

    private Fn parseFn;

    private Fn formatFn;

    private Fn evalFn;

    /**
     * TCP listen address
     */
    private InetAddress listenAddr;

    /**
     * List of addresses from which agent will accept connections.
     */
    private List<InetAddress> allowAddrs = new ArrayList<InetAddress>();
    private List<InetAddress> denyAddrs = new ArrayList<InetAddress>();

    /**
     * TCP server socket
     */
    private ServerSocket socket;

    public static final Keyword KW_ADDR = Keyword.keyword("addr");
    public static final Keyword KW_PORT = Keyword.keyword("port");
    public static final Keyword KW_ALLOW = Keyword.keyword("allow");
    public static final Keyword KW_DENY = Keyword.keyword("deny");
    public static final Keyword KW_NAME = Keyword.keyword("name");
    public static final Keyword KW_PARSE = Keyword.keyword("parse");
    public static final Keyword KW_FORMAT = Keyword.keyword("parse");
    public static final Keyword KW_EVAL = Keyword.keyword("eval");

    /**
     * Standard constructor
     *
     * @param config      Map of configuration parameters
     * @param agent       LISP agent
     */
    public TcpServer(LispMap config, ZorkaLispAgent agent) {
        this.agent = agent;
        this.config = config;

        this.name = (String)config.get(KW_NAME);
        this.parseFn = (Fn)config.get(KW_PARSE);
        this.formatFn = (Fn)config.get(KW_FORMAT);
        this.evalFn = (Fn)config.get(KW_EVAL);

        setup();
    }

    protected void setup() {
        String la = (String)config.get(KW_ADDR, defaultAddr);
        try {
            listenAddr = InetAddress.getByName(la.trim());
        } catch (UnknownHostException e) {
            log.error(ZorkaLogger.ZAG_ERRORS, "Cannot parse " + name + " address in zorka.conf", e);
            AgentDiagnostics.inc(AgentDiagnostics.CONFIG_ERRORS);
        }

        listenPort = (Integer)config.get(KW_PORT, defaultPort);

        log.info(ZorkaLogger.ZAG_ERRORS, "Zorka will listen for " + name + " connections on " + listenAddr + ":" + listenPort);

        for (Seq seq = (Seq)config.get(KW_ALLOW, cons("127.0.0.1", null)); seq != null; seq = (Seq)cdr(seq)) {
            String sa = (String)car(seq);
            try {
                log.info(ZorkaLogger.ZAG_ERRORS, "Zorka will accept " + name + " connections from '" + sa.trim() + "'.");
                allowAddrs.add(InetAddress.getByName(sa.trim()));
            } catch (UnknownHostException e) {
                log.error(ZorkaLogger.ZAG_ERRORS, "Cannot parse " + name + ".server.addr in zorka.properties", e);
            }
        }
    }


    /**
     * Opens socket and starts agent thread.
     */
    public void start() {
        if (!running) {
            try {
                socket = new ServerSocket(listenPort, 0, listenAddr);
                running = true;
                thread = new Thread(this);
                thread.setName("ZORKA-" + name + "-main");
                thread.setDaemon(true);
                thread.start();
                log.info(ZorkaLogger.ZAG_CONFIG, "ZORKA-" + name + " core is listening at " + listenAddr + ":" + listenPort + ".");
            } catch (IOException e) {
                log.error(ZorkaLogger.ZAG_ERRORS, "I/O error while starting " + name + " core:" + e.getMessage());
            }
        }
    }


    /**
     * Stops agent thread and closes socket.
     */
    @SuppressWarnings("deprecation")
    public void stop() {
        if (running) {
            running = false;
            try {
                if (socket != null) {
                    socket.close();
                    socket = null;
                }
                for (int i = 0; i < 100; i++) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                    }
                    if (thread == null) {
                        return;
                    }
                }

                log.warn(ZorkaLogger.ZAG_WARNINGS, "ZORKA-" + name + " thread didn't stop after 1000 milliseconds. Shutting down forcibly.");

                thread.stop();
                thread = null;
            } catch (IOException e) {
                log.error(ZorkaLogger.ZAG_ERRORS, "I/O error in zabbix core main loop: " + e.getMessage());
            }
        }
    }

    public void restart() {
        setup();
        start();
    }

    @Override
    public void shutdown() {
        log.info(ZorkaLogger.ZAG_CONFIG, "Shutting down " + name + " agent ...");
        stop();
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
            }
            socket = null;
        }
    }


    @Override
    public void run() {

        while (running) {
            Socket sock = null;
            LispFnTcpCallback cb = null;
            try {
                sock = socket.accept();
                if (!allowedAddr(sock)) {
                    log.warn(ZorkaLogger.ZAG_WARNINGS, "Illegal connection attempt from '" + socket.getInetAddress() + "'.");
                    sock.close();
                } else {
                    Object query = parseFn.apply(
                        agent.getInterpreter(), agent.getInterpreter().env(),
                        StandardLibrary.cons(sock.getInputStream(), null));
                    cb = new LispFnTcpCallback(sock, formatFn);
                    if (query instanceof String) {
                        agent.exec((String) query, cb);
                    }
                }
            } catch (Exception e) {
                if (running) {
                    log.error(ZorkaLogger.ZAG_ERRORS, "Error occured when processing request.", e);
                }
                if (running && cb != null) {
                    cb.handleError(e);
                }
            } finally {
                try {
                    if (sock != null) {
                        sock.close();
                    }
                } catch (IOException e) {
                    log.error(ZorkaLogger.ZAG_ERRORS, "Error while closing socket: ", e);
                }
            }
        }

        thread = null;

    }


    private boolean allowedAddr(Socket sock) {

        for (InetAddress addr : allowAddrs) {
            if (addr.equals(sock.getInetAddress())) {
                return true;
            }
        }

        return false;
    }

}

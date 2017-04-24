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
import com.jitlogic.zorka.lisp.LispMap;
import com.jitlogic.zorka.lisp.Namespace;
import com.jitlogic.zorka.lisp.Primitive;
import com.jitlogic.zorka.lisp.Seq;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Namespace("net")
public class NetworkLibrary {

    private ZorkaLispAgent agent;

    public NetworkLibrary(ZorkaLispAgent agent) {
        this.agent = agent;
    }

    @Primitive("tcp-server")
    public TcpServer tcpServer(LispMap config) {
        TcpServer server = new TcpServer(config, agent);
        server.start();
        return server;
    }

    @Primitive("read-line")
    public static String readLine(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (int b = is.read(); b != 0x0a && b != -1; b = is.read()) {
            bos.write(b);
        }
        return new String(bos.toByteArray());
    }

    @Primitive("parse-bracket-query")
    public static Seq bracketQueryParse(String query) {
        return null;
    }

    private static final byte[] ZBX_HDR = {(byte) 'Z', (byte) 'B', (byte) 'X', (byte) 'D', 0x01};

    @Primitive("zabbix-read-packet")
    public static String zabbixReadPacket(InputStream is) {
        return null;
    }

    @Primitive("zabbix-write-packet")
    public static void zabbixWritePacket(OutputStream os, Object v) {
        //if (v == null) v =
    }

}

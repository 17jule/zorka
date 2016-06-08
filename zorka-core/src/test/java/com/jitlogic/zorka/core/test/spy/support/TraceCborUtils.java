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

package com.jitlogic.zorka.core.test.spy.support;

import com.jitlogic.zorka.common.cbor.CBORReader;
import com.jitlogic.zorka.core.spy.TraceBufChunk;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class TraceCborUtils {

    public static int chunksCount(TraceBufChunk chunks) {
        int count = 0;
        for (TraceBufChunk c = chunks; c != null; c = c.getNext()) {
            count++;
        }
        return count;
    }

    public static int chunksLength(TraceBufChunk chunks) {
        int len = 0;
        for (TraceBufChunk c  = chunks; c != null; c = c.getNext()) {
            len = Math.max(len, c.getOffset()+c.getSize());
        }
        return len;
    }

    public static byte[] chunksMerge(TraceBufChunk chunks) {
        byte[] buf = new byte[chunksLength(chunks)];

        for (TraceBufChunk c = chunks; c != null; c = c.getNext()) {
            System.arraycopy(c.getBuffer(), 0, buf, c.getOffset(), c.getSize());
        }

        return buf;
    }



    public static String chunksHex(TraceBufChunk chunks) {
        return DatatypeConverter.printHexBinary(chunksMerge(chunks));
    }

    public static Object decodeCbor(TraceBufChunk chunks) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(chunksMerge(chunks));
        return new CBORReader(bis).read();
    }

    public static Object decodeTrace(TraceBufChunk chunks) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(chunksMerge(chunks));
        return new CBORReader(bis, new TestTagProcessor(), new TestValResolver()).read();
    }


    public static String mkString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append('_');
        }
        return sb.toString();
    }


}

/**
 * Copyright 2012-2016 Rafal Lewczuk <rafal.lewczuk@jitlogic.com>
 * <p>
 * This is free software. You can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this software. If not, see <http://www.gnu.org/licenses/>.
 */

package com.jitlogic.zorka.cbor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import static com.jitlogic.zorka.cbor.CBORConstants.*;

public class CBORWriter {

    private OutputStream os;

    private byte[] buf = new byte[16];

    public CBORWriter(OutputStream os) {
        this.os = os;
    }

    public void write(Object obj) throws IOException {
        if (obj == null) {
            os.write(NULL_CODE);
            return;
        }
        Class<?> c = obj.getClass();
        if (c == Byte.class || c == Short.class || c == Integer.class) {
            writeInt(((Number)obj).intValue());
        } else if (c == Long.class) {
            writeLong(((Number)obj).longValue());
        } else if (c == String.class) {
            writeString((String)obj);
        } else if (obj instanceof List) {
            writeList((List)obj);
        } else if (obj instanceof Map) {
            writeMap((Map)obj);
        } else if (obj == Boolean.FALSE) {
            os.write(FALSE_CODE);
        } else if (obj == Boolean.TRUE) {
            os.write(TRUE_CODE);
        } else if (obj == BREAK) {
            os.write(BREAK_CODE);
        } else if (c == Float.class) {
            writeFloat((Float)obj);
        } else if (c == Double.class) {
            writeDouble((Double)obj);
        } else if (obj == UNKNOWN) {
            os.write(UNKNOWN_CODE);
        }
    }


    private void writeUInt(int prefix, int i) throws IOException {
        if (i < 0x18) {
            os.write(prefix + i);
        } else if (i < 0x100) {
            os.write(prefix + 0x18);
            os.write(i & 0xff);
        } else if (i < 0x10000) {
            os.write(prefix + 0x19);
            os.write((byte) ((i >> 8) & 0xff));
            os.write((byte) (i & 0xff));

        } else {
            buf[0] = (byte) (0x1a + prefix);
            buf[1] = (byte) ((i >> 24) & 0xff);
            buf[2] = (byte) ((i >> 16) & 0xff);
            buf[3] = (byte) ((i >> 8) & 0xff);
            buf[4] = (byte) (i & 0xff);
            os.write(buf, 0, 5);
        }
    }


    private void writeULong(int prefix, long l) throws IOException {
        if (l < Integer.MAX_VALUE) {
            writeUInt(prefix, (int)l);
        } else {
            buf[0] = (byte) (0x1b + prefix);
            buf[1] = (byte) ((l >> 56) & 0xff);
            buf[2] = (byte) ((l >> 48) & 0xff);
            buf[3] = (byte) ((l >> 40) & 0xff);
            buf[4] = (byte) ((l >> 32) & 0xff);
            buf[5] = (byte) ((l >> 24) & 0xff);
            buf[6] = (byte) ((l >> 16) & 0xff);
            buf[7] = (byte) ((l >> 8) & 0xff);
            buf[8] = (byte)  (l & 0xff);
            os.write(buf, 0, 9);
        }
    }


    public void writeInt(int i) throws IOException {
        if (i >= 0) {
            writeUInt(0, i);
        } else {
            writeUInt(0x20, Math.abs(i)-1);
        }
    }


    public void writeLong(long l) throws IOException {
        if (l >= 0) {
            writeULong(0, l);
        } else {
            writeULong(0x20, Math.abs(l)-1L);
        }
    }


    public void writeBytes(byte[] b) throws IOException {
        writeUInt(0x40, b.length);
        os.write(b);
    }


    public void writeString(String s) throws IOException {
        byte[] b = s.getBytes();
        writeUInt(0x60, b.length);
        os.write(b);
    }


    public void writeList(List lst) throws IOException {
        // TODO obsłużyć również array of objects, array of integers itd.
        writeUInt(0x80, lst.size());
        for (Object itm : lst) {
            write(itm);
        }
    }


    public void writeMap(Map<Object,Object> map) throws IOException {
        writeUInt(0xa0, map.size());
        for (Map.Entry e : map.entrySet()) {
            write(e.getKey());
            write(e.getValue());
        }
    }


    public void writeFloat(float f) throws IOException {
        int i = Float.floatToIntBits(f);
        buf[0] = (byte)((i >> 24) & 0xff);
        buf[1] = (byte)((i >> 16) & 0xff);
        buf[2] = (byte)((i >> 8) & 0xff);
        buf[3] = (byte)(i & 0xff);
        os.write(FLOAT_BASE4);
        os.write(buf, 0, 4);
    }


    public void writeDouble(double d) throws IOException {
        long l = Double.doubleToLongBits(d);
        buf[0] = (byte)((l >> 56) & 0xff);
        buf[1] = (byte)((l >> 48) & 0xff);
        buf[2] = (byte)((l >> 40) & 0xff);
        buf[3] = (byte)((l >> 32) & 0xff);
        buf[4] = (byte)((l >> 24) & 0xff);
        buf[5] = (byte)((l >> 16) & 0xff);
        buf[6] = (byte)((l >> 8) & 0xff);
        buf[7] = (byte)(l & 0xff);
        os.write(FLOAT_BASE8);
        os.write(buf, 0, 8);
    }


    public void writeTag(int tag) throws IOException {
        writeUInt(0xc0, tag);
    }
}

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

package com.jitlogic.zorka.common.test;

import com.jitlogic.zorka.common.cbor.CBORReader;
import com.jitlogic.zorka.common.cbor.CBORWriter;
import org.junit.Test;

import static org.junit.Assert.*;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;


import static com.jitlogic.zorka.common.cbor.CBORConstants.BREAK;

public class CBORCodecUnitTest {

    public List l(Object...objs) {
        return Arrays.asList(objs);
    }


    public static Map m(Object...objs) {
        // We use linked hash map, so
        Map<Object,Object> m = new LinkedHashMap<Object,Object>();
        for (int i = 1; i < objs.length; i += 2) {
            m.put(objs[i-1], objs[i]);
        }
        return m;
    }


    public void tst(Object obj, String cborStr) throws IOException {
        tst(obj, cborStr, true, true);
    }


    public void tst(Object obj, String cbor, boolean enc, boolean dec) throws IOException {
        if (enc) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            CBORWriter w = new CBORWriter(bos);
            w.write(obj);
            String cbor1 = DatatypeConverter.printHexBinary(bos.toByteArray()).toLowerCase();
            assertEquals("Encoded val of " + obj + " does not match.", cbor, cbor1);
        }

        if (dec) {
            ByteArrayInputStream bis = new ByteArrayInputStream(DatatypeConverter.parseHexBinary(cbor));
            CBORReader r = new CBORReader(bis);
            Object obj1 = r.read();
            assertEquals("Decoded val of " + cbor + " does not match.", obj, obj1);
        }
    }


    @Test
    public void testReadWriteIntegers() throws Exception {
        // TODO more precise demarcation for data types

        // Major type = 1: unsigned integer

        tst(0,  "00");
        tst(1,  "01");
        tst(10, "0a");
        tst(23, "17");
        tst(24, "1818");
        tst(25, "1819");
        tst(100, "1864");
        tst(254, "18fe");
        tst(255, "18ff");
        tst(1000, "1903e8");
        tst(1000000, "1a000f4240");
        tst(1000000000000L, "1b000000e8d4a51000");


        tst(-1, "20");
        tst(-10, "29");
        tst(-100, "3863");
        tst(-1000, "3903e7");
        tst(-1000000, "3a000f423f");
        tst(-1000000000000L, "3b000000e8d4a50fff");
    }


    // TODO not needed right now
    public void testTaggedDateTime() throws Exception {
        // C1 tag
    }

    // TODO not needed right now
    public void testTaggedBigNums() throws Exception {

        // C2/C3 tags

        //tst(18446744073709551615, "1bffffffffffffffff"); ?? BigDecimal ?
        //tst(18446744073709551616, "c249010000000000000000"); ?? BigDecimal ?
        //tst(-18446744073709551616, "3bffffffffffffffff"); ?? BigDecimal ?
        //tst(-18446744073709551617, "c349010000000000000000"); ?? BigDecimal ?

        // Major type = 2: negative integer
    }

    // TODO C4 - decimal fractions

    // TODO C5 - big floats

    // TODO content hints

    @Test
    public void testReadWriteFloatingPoints() throws Exception {
        tst(0.0f, "fa00000000");
        tst(0.0f, "f90000", false, true);
        tst(-0.0f, "f98000", false, true);
        tst(1.0f, "f93c00", false, true);
        tst(1.1, "fb3ff199999999999a");
        tst(1.5f, "f93e00", false, true);
        tst(65504.0f, "f97bff", false, true);
        tst(100000.0f, "fa47c35000");
        tst(3.4028234663852886e+38f, "fa7f7fffff", false, true);
        tst(1.0e+300, "fb7e37e43c8800759c");
        //tst(5.960464477539063e-08f, "f90001", false, true);
        tst(6.103515625e-05f, "f90400", false, true);
        tst(-4.0f, "f9c400", false, true);
        tst(-4.1, "fbc010666666666666");

        tst(Float.POSITIVE_INFINITY, "f97c00", false, true);
        tst(Float.NaN, "f97e00", false, true);
        tst(Float.NEGATIVE_INFINITY, "f9fc00", false, true);

        tst(Float.POSITIVE_INFINITY, "fa7f800000");
        tst(Float.NaN, "fa7fc00000");
        tst(Float.NEGATIVE_INFINITY, "faff800000");

        tst(Double.POSITIVE_INFINITY, "fb7ff0000000000000");
        tst(Double.NaN, "fb7ff8000000000000");
        tst(Double.NEGATIVE_INFINITY, "fbfff0000000000000");
    }


    @Test
    public void testBasicLiterals() throws Exception {
        tst(false, "f4");
        tst(true, "f5");
        tst(null, "f6");
        tst(BREAK, "ff");
    }


    @Test
    public void testStringEncoding() throws Exception {
        tst("", "60");
        tst("a", "6161");
        tst("IETF", "6449455446");
        tst("\"\\", "62225c");
        tst("ü", "62c3bc");
        tst("水", "63e6b0b4");
        tst("𐅑", "64f0908591");
    }


    @Test
    public void testArrayEncoding() throws Exception {
        tst(l(), "80");
        tst(l(1,2,3), "83010203");
        tst(l(1, l(2,3), l(4,5)), "8301820203820405");
        tst(l(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25),
            "98190102030405060708090a0b0c0d0e0f101112131415161718181819");
    }


    @Test
    public void testMapEncoding() throws Exception {
        tst(m(), "a0");
        tst(m(1,2, 3,4), "a201020304");
        tst(m("a",1,"b", l(2,3)), "a26161016162820203");
        tst(l("a", m("b","c")), "826161a161626163");
        tst(m("a","A","b","B","c","C","d","D","e","E"), "a56161614161626142616361436164614461656145");
    }


    @Test
    public void testVariableLengthEncoding() throws Exception {
        tst("streaming", "7f657374726561646d696e67ff", false, true);
        tst(l(), "9fff", false, true);
        tst(l(1,l(2,3),l(4,5)), "9f018202039f0405ffff", false, true);
        tst(l(1,l(2,3),l(4,5)), "83018202039f0405ff",   false, true);
        tst(l(1,l(2,3),l(4,5)), "83019f0203ff820405",   false, true);
        tst(l(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25),
            "9f0102030405060708090a0b0c0d0e0f101112131415161718181819ff", false, true);
        tst(m("a",1,"b",l(2,3)), "bf61610161629f0203ffff", false, true);
        tst(l("a",m("b","c")), "826161bf61626163ff", false, true);
        tst(m("Fun", true, "Amt", -2), "bf6346756ef563416d7421ff", false, true);
    }


}

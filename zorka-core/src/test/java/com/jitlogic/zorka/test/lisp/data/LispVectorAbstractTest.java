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

package com.jitlogic.zorka.test.lisp.data;

import com.jitlogic.zorka.lisp.LispVector;

import com.jitlogic.zorka.lisp.Seq;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public abstract class LispVectorAbstractTest {

    public final static int FULL_CHECK_GET  = 0x01;
    public final static int FULL_CHECK_SEQ  = 0x02;
    public final static int FULL_CHECK_ITER = 0x04;

    public final static int FULL_CHECK = 0xff;

    public abstract LispVector newVec();

    @Test
    public void testEmptyVector() {
        LispVector v = newVec();
        assertEquals(0, v.size());
        assertEquals("[]", v.toString());
        assertEquals(v, newVec());
    }

    @Test
    public void testVectorWithSingleElement() {
        LispVector v1 = newVec();
        v1 = v1.append(1);
        assertEquals(1, v1.size());
        assertEquals("[1]", v1.toString());

        LispVector v2 = newVec();
        v2 = v2.cons(1);
        assertEquals(1, v2.size());
        assertEquals("[1]", v2.toString());

        assertEquals(v1, v2);

    }

    @Test
    public void testVectorWithTwoElements() {
        LispVector v1 = newVec();
        v1 = v1.append(1);
        v1 = v1.append(2);
        assertEquals(2, v1.size());
        assertEquals("[1, 2]", v1.toString());

        LispVector v2 = newVec();
        v2 = v2.cons(2);
        v2 = v2.cons(1);
        assertEquals(2, v2.size());
        assertEquals("[1, 2]", v2.toString());

        assertEquals(v1, v2);
    }

    @Test
    public void testAppend32() {
        testAppend(32, FULL_CHECK_GET);
    }

    @Test
    public void testAppend256() {
        testAppend(256, FULL_CHECK_GET);
    }

    @Test
    public void testAppend4096() {
        testAppend(4096, FULL_CHECK_GET);
    }

    @Test
    public void testAppend65536() {
        testAppend(65536, 0);
    }

    @Test
    public void testAppend100000() {
        testAppend(100000, 0);
    }

    @Test
    public void testPrepend32() {
        testPrepend(32, FULL_CHECK);
    }

    @Test
    public void testPrepend256() {
        testPrepend(256, FULL_CHECK);
    }

    @Test
    public void testPrepend4096() {
        testPrepend(4096, FULL_CHECK);
    }

    @Test
    public void testPrepend65536() {
        testPrepend(65536, 0);
    }

    @Test
    public void testPrepend100000() {
        testPrepend(65536, 0);
    }

    private void testPrepend(int limit, int flags) {
        LispVector v = newVec();

        for (int i = 0; i < limit; i++) {
            v = v.cons(i);
            if (0 != (flags & FULL_CHECK_GET)) {
                for (int j = 0; j <= i; j++) {
                    assertEquals(v.get(j), (Integer)(i-j));
                }
            }
            if (0 != (flags & FULL_CHECK_SEQ)) {
                Seq seq = v;
                for (int j = 0; seq != null; seq = (Seq)seq.rest(),j++) {
                    assertEquals(seq.first(), (Integer)(i-j));
                }
            }
            if (0 != (flags & FULL_CHECK_ITER)) {
                int j = 0;
                for (Object o : v) {
                    assertEquals(o, (Integer)(i-j));
                    j++;
                }
            }
        }

        assertEquals(limit, v.size());

        for (int i = 0; i < limit; i++) {
            assertEquals(v.get(i), (Integer)(limit-i-1));
        }
    }

    private void testAppend(int limit, int flags) {
        LispVector v = newVec();

        for (int i = 0; i < limit; i++) {
            v = v.append(i);
            if (0 != (flags & FULL_CHECK_GET)) {
                for (int j = 0; j <= i; j++) {
                    assertEquals(v.get(j), (Integer)j);
                }
            }
            if (0 != (flags & FULL_CHECK_SEQ)) {
                Seq seq = v;
                for (int j = 0; seq != null; seq = (Seq)seq.rest(),j++) {
                    assertEquals(seq.first(), (Integer)j);
                }
            }
            if (0 != (flags & FULL_CHECK_ITER)) {
                int j = 0;
                for (Object o : v) {
                    assertEquals(o, (Integer)j);
                    j++;
                }
            }

        }

        assertEquals(limit, v.size());

        for (int i = 0; i < limit; i++) {
            assertEquals(v.get(i), (Integer)i);
        }
    }
}

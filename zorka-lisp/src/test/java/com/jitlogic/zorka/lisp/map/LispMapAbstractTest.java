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

package com.jitlogic.zorka.lisp.map;

import com.jitlogic.zorka.lisp.LispMap;
import com.jitlogic.zorka.lisp.Seq;
import com.jitlogic.zorka.lisp.support.LispTestSupport;
import org.junit.Test;


import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.*;

import static com.jitlogic.zorka.lisp.StandardLibrary.car;
import static com.jitlogic.zorka.lisp.StandardLibrary.cdr;
import static com.jitlogic.zorka.lisp.StandardLibrary.cadr;
import static com.jitlogic.zorka.lisp.StandardLibrary.length;

public abstract class LispMapAbstractTest extends LispTestSupport {

    public abstract LispMap newMap();

    public static final int FULL_CHECK_GET    = 0x01;
    public static final int FULL_CHECK_SEQ    = 0x02;
    public static final int FULL_CHECK_SIZE   = 0x04;
    public static final int FULL_CHECK_EQUALS = 0x08;
    public static final int FULL_CHECK_ITER   = 0x10;

    public static final int FULL_CHECK = FULL_CHECK_SIZE;

    @Test
    public void testEmptyMapProperties() {
        LispMap m = newMap();
        assertEquals(0, m.size());
        assertEquals("{}", m.toString());
        assertEquals(null, m.get(100));
        assertEquals(null, m.get(null));
        assertEquals(null, m.seq());
    }


    @Test
    public void testSingleElementMapProperties() {
        LispMap m = newMap().assoc(1, "a");
        assertEquals(1, m.size());
        assertEquals(null, m.get(0));
        assertEquals("a", m.get(1));
        assertEquals("{1 a}", m.toString());
        m = m.assoc(1, "b");
        assertEquals(1, m.size());
        assertEquals("b", m.get(1));
        assertEquals("{1 b}", m.toString());
    }


    @Test
    public void testAssocDissocGetSet4() {
        assocDissocStress(4, 4 * 1024, 1, FULL_CHECK|FULL_CHECK_ITER);
    }


    @Test
    public void testAssocDissocGetSet16() {
        assocDissocStress(16, 16 * 1024, 1, FULL_CHECK|FULL_CHECK_ITER);
    }


    @Test
    public void testAssocDissocGetSet64() {
        assocDissocStress(64, 64 * 1024, 1, FULL_CHECK|FULL_CHECK_ITER);
    }


    @Test
    public void testAssocDissocGetSet256() {
        assocDissocStress(256, 256 * 1024, 1, FULL_CHECK);
    }


    @Test
    public void testAssocDissocGetSet1024() {
        assocDissocStress(16 * 1024, 64 * 1024, 8, FULL_CHECK);
    }


    private void assocDissocStress(int nmax, int ncycles, int mult, int checks) {
        LispMap m = newMap();
        boolean[] flags = new boolean[nmax];
        Random rand = new Random();
        for (int i = 0; i < ncycles; i++) {
            int idx = rand.nextInt(nmax);
            int k = idx * mult;
            if (flags[idx]) {
                if (m.get(k) == null) {
                    fail("[" + i + "]" + " Item expected but not found:" + k);
                }
                assertEquals(""+k, m.get(k));
                int sz0 = m.size();
                LispMap m1 = m.dissoc(k);
                if (m1.get(k) != null) {
                    fail("[" + i + "]" + " Item not removed properly:" + k);
                }
                int sz1 = m1.size();
                if (0 != (checks & FULL_CHECK_SIZE) && sz0 - 1 != sz1) {
                    fail("[" + i + "]" + " Size does not match:" + sz0 + " vs " + sz1);
                }
                flags[idx] = false;

                if (0 != (checks & FULL_CHECK_GET)) checkGets(mult, flags, i, m1);
                if (0 != (checks & FULL_CHECK_SEQ)) checkSeq(mult, flags, m1);
                if (0 != (checks & FULL_CHECK_EQUALS)) checkCopyAndEquals(m1);
                if (0 != (checks & FULL_CHECK_ITER)) checkIterators(m1);

                m = m1;
            } else {
                if (m.get(k) != null) {
                    fail("[" + i + "]" + " Item NOT expected but not found:" + k);
                }
                int sz0 = m.size();
                LispMap m1 = m.assoc(k, ""+k);
                if (m1.get(k) == null) {
                    fail("[" + i + "]" + " Item not added properly:" + k);
                }
                int sz1 = m1.size();
                if (0 != (checks & FULL_CHECK_SIZE) && sz0 + 1 != sz1) {
                    fail("[" + i + "]" + " Size does not match:" + sz0 + " vs " + sz1);
                }
                flags[idx] = true;

                if (0 != (checks & FULL_CHECK_GET)) checkGets(mult, flags, i, m1);
                if (0 != (checks & FULL_CHECK_SEQ)) checkSeq(mult, flags, m1);
                if (0 != (checks & FULL_CHECK_EQUALS)) checkCopyAndEquals(m1);
                if (0 != (checks & FULL_CHECK_ITER)) checkIterators(m1);

                m = m1;
            }
        }
    }


    private void checkGets(int mult, boolean[] flags, int i, LispMap m1) {
        for (int j = 0; j < flags.length; j++) {
            int k1 = j * mult;
            if (flags[j] == (m1.get(k1) == null)) {
                fail("[" + i + "]" + " Item " + k1 + " mismatch after dissoc: "
                    + flags[j] + " vs " + (m1.get(k1) != null));
            }
        }
    }


    private void checkSeq(int mult, boolean[] flags, LispMap m) {
        boolean[] nflags = new boolean[flags.length];
        int i = 0;
        Seq seq0 = m.seq();
        int sz0 = m.size(), sz1 = length(seq0);
        if (sz0 != sz1) {
            System.out.println("m1=" + m.toString());
            fail("[" + i + "]" + " Map size and seq length do not match: " + sz0 + " vs " + sz1);
        }
        for (Seq seq = seq0; seq != null; seq = (Seq)cdr(seq),i++) {
            if (!(car(seq) instanceof Seq)) {
                fail("[" + i + "]" + " KV pair is null ? ");
            }
            Seq kv = (Seq)car(seq);
            Object k = car(kv), v = cadr(kv);
            if (!(k instanceof Integer)) {
                fail("[" + i + "]" + " key is not an INT: " + k);
            }
            if (!(v instanceof String)) {
                fail("[" + i + "]" + " value is not String: " + v);
            }
            nflags[((Integer)k)/mult] = true;
        }
        for (int j = 0; j < flags.length; j++) {
            if (flags[j] != nflags[j]) {
                System.out.println("m1=" + m.toString());
                fail("Seq result does not match at position: " + j);
            }
        }
    }


    private void checkCopyAndEquals(LispMap m) {
        LispMap m1 = newMap();
        for (Seq seq = m.seq(); seq != null; seq = (Seq)cdr(seq)) {
            Object itm = car(seq), k = car(itm), v = cadr(itm);
            m1 = m1.assoc(k, v);
        }
        if (!m.equals(m1)) {
            System.out.println(m.equals(m1));
            fail("Maps NOT equal: " + m + " vs " + m1 + " -> ");

        }
        if (!m1.equals(m)) {
            fail("Map NOT equal: " + m + " vs " + m1);
        }
        if (m.hashCode() != m1.hashCode()) {
            fail("Maps do NOT have the same hash codes: " + m.hashCode() + " vs " + m1.hashCode());
        }
    }


    private void checkIterators(LispMap m) {
        Map<Object,Object> hm = new HashMap<Object,Object>();

        for (Map.Entry e : m) {
            hm.put(e.getKey(), e.getValue());
        }

        if (hm.size() != m.size()) {
            for (Object o : m) {
                System.out.println(o);
            }
            fail("Map and iterator result sizes do not match: " + m + " <-> " + hm);
        }

        for (Object o : hm.entrySet()) {
            Map.Entry e = (Map.Entry)o;
            if (!e.getValue().equals(m.get(e.getKey()))) {
                fail("Map and iterator values do not match for key: " + e.getKey());
            }
        }
    }
}

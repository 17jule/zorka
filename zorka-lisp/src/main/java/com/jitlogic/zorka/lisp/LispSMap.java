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

package com.jitlogic.zorka.lisp;

import java.util.Iterator;

import static com.jitlogic.zorka.lisp.StandardLibrary.cons;
import static com.jitlogic.zorka.lisp.StandardLibrary.car;
import static com.jitlogic.zorka.lisp.StandardLibrary.cdar;
import static com.jitlogic.zorka.lisp.StandardLibrary.cddr;

/**
 * IMap implementation for small maps (up to 6 elements). Uses single allocated memory block.
 * On 32-bit or 64-bit JVM with CompressedOps=true it takes 64 bytes (= 1 cache line).
 * On 64-bit JVM with CompressedOps=false it takes 116 bytes.
 */
public class LispSMap implements LispMap, Cloneable {

    Object k0, k1, k2, k3, k4, k5;
    Object v0, v1, v2, v3, v4, v5;

    int flags;

    public static final LispSMap EMPTY = new LispSMap();

    public LispSMap() {
        this(0);
    }


    public LispSMap(int flags) {
        this.flags = flags;
    }


    public LispSMap(int flags, Seq args) {
        this.flags = flags;
        if (args != null) { k0 = car(args); v0 = cdar(args); args = (Seq)cddr(args); }
        if (args != null) { k1 = car(args); v1 = cdar(args); args = (Seq)cddr(args); }
        if (args != null) { k2 = car(args); v2 = cdar(args); args = (Seq)cddr(args); }
        if (args != null) { k3 = car(args); v3 = cdar(args); args = (Seq)cddr(args); }
        if (args != null) { k4 = car(args); v4 = cdar(args); args = (Seq)cddr(args); }
        if (args != null) { k5 = car(args); v5 = cdar(args); }
    }


    public int getFlags() {
        return flags;
    }


    public void setFlags(int flags) {
        this.flags = flags;
    }


    @Override
    public LispMap assoc(Object k, Object v) {
        if (k == null) k = NULL_KEY;
        try {
            LispSMap m = 0 != (flags & MUTABLE) ? this : (LispSMap)this.clone();
            if (m.k0 == null || m.k0 == k || k.equals(m.k0)) {
                m.k0 = k; m.v0 = v; return m;
            } else if (m.k1 == null || m.k1 == k || k.equals(m.k1)) {
                m.k1 = k; m.v1 = v; return m;
            } else if (m.k2 == null || m.k2 == k || k.equals(m.k2)) {
                m.k2 = k; m.v2 = v; return m;
            } else if (m.k3 == null || m.k3 == k || k.equals(m.k3)) {
                m.k3 = k; m.v3 = v; return m;
            } else if (m.k4 == null || m.k4 == k || k.equals(m.k4)) {
                m.k4 = k; m.v4 = v; return m;
            } else if (m.k5 == null || m.k5 == k || k.equals(m.k5)) {
                m.k5 = k; m.v5 = v; return m;
            } else {
                LispHMap rslt = new LispHMap(MUTABLE);
                if (k0 != null) rslt.assoc(k0, v0);
                if (k1 != null) rslt.assoc(k1, v1);
                if (k2 != null) rslt.assoc(k2, v2);
                if (k3 != null) rslt.assoc(k3, v3);
                if (k4 != null) rslt.assoc(k4, v4);
                if (k5 != null) rslt.assoc(k5, v5);
                rslt.assoc(k, v);
                rslt.setFlags(flags);
                return rslt;
            }
        } catch (CloneNotSupportedException e) {
            throw new LispException("Cannot clone SmallMap object.");
        }
    }


    @Override
    public LispMap dissoc(Object k) {
        if (k == null) k = NULL_KEY;
        try {
            LispSMap m = 0 != (flags & MUTABLE) ? this : (LispSMap)this.clone();
            if (m.k0 == k || k.equals(m.k0)) {
                m.k0 = null; m.v0 = null; return m;
            } else if (m.k1 == k || k.equals(m.k1)) {
                m.k1 = null; m.v1 = null; return m;
            } else if (m.k2 == k || k.equals(m.k2)) {
                m.k2 = null; m.v2 = null; return m;
            } else if (m.k3 == k || k.equals(m.k3)) {
                m.k3 = null; m.v3 = null; return m;
            } else if (m.k4 == k || k.equals(m.k4)) {
                m.k4 = null; m.v4 = null; return m;
            } else if (m.k5 == null || k.equals(m.k5)) {
                m.k5 = null; m.v5 = null; return m;
            } else {
                return this;
            }
        } catch (CloneNotSupportedException e) {
            throw new LispException("Cannot clone SmallMap object.");
        }
    }


    @Override
    public Object get(Object k) {
        return get(k, null);
    }


    @Override
    public Object get(Object k, Object dv) {
        if (k == null) k = NULL_KEY;
        if (k0 == k || k.equals(k0)) {
            return v0;
        } else if (k1 == k || k.equals(k1)) {
            return v1;
        } else if (k2 == k || k.equals(k2)) {
            return v2;
        } else if (k3 == k || k.equals(k3)) {
            return v3;
        } else if (k4 == k || k.equals(k4)) {
            return v4;
        } else if (k5 == null || k.equals(k5)) {
            return v5;
        } else {
            return dv;
        }
    }


    @Override
    public boolean contains(Object k) {
        if (k == null) k = NULL_KEY;
        return k0 == k || k1 == k || k2 == k || k3 == k || k4 == k || k5 == k
            || k.equals(k0) || k.equals(k1) || k.equals(k2) || k.equals(k3) || k.equals(k4) || k.equals(k5);
    }


    @Override
    public int size() {
        int sz = 0;
        if (k0 != null) sz++;
        if (k1 != null) sz++;
        if (k2 != null) sz++;
        if (k3 != null) sz++;
        if (k4 != null) sz++;
        if (k5 != null) sz++;
        return sz;
    }


    private class SeqIterator implements Iterator<LispMap.Entry> {

        int idx = -1;
        private Entry val;

        private void nextPos() {
            for (idx = idx+1; idx < 6; idx++) {
                if (idx <= 0 && k0 != null) { val = new Entry(LispSMap.this, k0, v0); return; }
                if (idx <= 1 && k1 != null) { val = new Entry(LispSMap.this, k1, v1); return; }
                if (idx <= 2 && k2 != null) { val = new Entry(LispSMap.this, k2, v2); return; }
                if (idx <= 3 && k3 != null) { val = new Entry(LispSMap.this, k3, v3); return; }
                if (idx <= 4 && k4 != null) { val = new Entry(LispSMap.this, k4, v4); return; }
                if (idx <= 5 && k5 != null) { val = new Entry(LispSMap.this, k5, v5); return; }
            }
            val = null;
        }

        public SeqIterator() {
            nextPos();
        }

        @Override
        public boolean hasNext() {
            return val != null;
        }

        @Override
        public Entry next() {
            Entry rslt = val;
            nextPos();
            return rslt;
        }

        @Override
        public void remove() {
            if (0 != (flags & MUTABLE)) {
                switch (idx) {
                    case 0: k0 = null; v0 = null; break;
                    case 1: k1 = null; v1 = null; break;
                    case 2: k2 = null; v2 = null; break;
                    case 3: k3 = null; v3 = null; break;
                    case 4: k4 = null; v4 = null; break;
                    case 5: k5 = null; v5 = null; break;
                }
                nextPos();
            } else {
                throw new LispException("Cannot remove from immutable LISP map.");
            }
        }
    }


    @Override
    public Iterator<LispMap.Entry> iterator() {
        return new SeqIterator();
    }


    public class MapSeq implements Seq {

        private int idx;
        private Object val;
        private Seq next;

        public MapSeq(int idx) {
            this.idx = idx;
            switch (this.idx) {
                case 0: val = cons(k0, cons(v0, null)); break;
                case 1: val = cons(k1, cons(v1, null)); break;
                case 2: val = cons(k2, cons(v2, null)); break;
                case 3: val = cons(k3, cons(v3, null)); break;
                case 4: val = cons(k4, cons(v4, null)); break;
                case 5: val = cons(k5, cons(v5, null)); break;
            }
        }

        @Override
        public Object first() {
            return val;
        }

        @Override
        public Object rest() {
            if (next == null) {
                next = seq(idx+1);
            }
            return next;
        }

        @Override
        public Iterator iterator() {
            return new SeqIterator(this);
        }
    }


    private Seq seq(int idx) {
        if (idx <= 0 && k0 != null) return new MapSeq(0);
        if (idx <= 1 && k1 != null) return new MapSeq(1);
        if (idx <= 2 && k2 != null) return new MapSeq(2);
        if (idx <= 3 && k3 != null) return new MapSeq(3);
        if (idx <= 4 && k4 != null) return new MapSeq(4);
        if (idx <= 5 && k5 != null) return new MapSeq(5);
        return null;
    }


    @Override
    public Seq seq() {
        return seq(0);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int n = 0;
        sb.append('{');
        if (k0 != null) { sb.append(k0); sb.append(' '); sb.append(v0); n++; }
        if (k1 != null) { if (n > 0) sb.append(", "); sb.append(k1); sb.append(' '); sb.append(v1); n++; }
        if (k2 != null) { if (n > 0) sb.append(", "); sb.append(k2); sb.append(' '); sb.append(v2); n++; }
        if (k3 != null) { if (n > 0) sb.append(", "); sb.append(k3); sb.append(' '); sb.append(v3); n++; }
        if (k4 != null) { if (n > 0) sb.append(", "); sb.append(k4); sb.append(' '); sb.append(v4); n++; }
        if (k5 != null) { if (n > 0) sb.append(", "); sb.append(k5); sb.append(' '); sb.append(v5); }
        sb.append('}');
        return sb.toString();
    }


    @Override
    public int hashCode() {
        int hc = 0;
        if (k0 != null) hc += k0.hashCode() + (v0 != null ? v0.hashCode() : 0);
        if (k1 != null) hc += k1.hashCode() + (v1 != null ? v1.hashCode() : 0);
        if (k2 != null) hc += k2.hashCode() + (v2 != null ? v2.hashCode() : 0);
        if (k3 != null) hc += k3.hashCode() + (v3 != null ? v3.hashCode() : 0);
        if (k4 != null) hc += k4.hashCode() + (v4 != null ? v4.hashCode() : 0);
        if (k5 != null) hc += k5.hashCode() + (v5 != null ? v5.hashCode() : 0);
        return hc;
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LispMap)) return false;
        LispMap m = (LispMap) o;
        return m.size() == size() &&
            !(k0 != null && !Utils.objEquals(v0, m.get(k0, NOT_FOUND))) &&
            !(k1 != null && !Utils.objEquals(v1, m.get(k1, NOT_FOUND))) &&
            !(k2 != null && !Utils.objEquals(v2, m.get(k2, NOT_FOUND))) &&
            !(k3 != null && !Utils.objEquals(v3, m.get(k3, NOT_FOUND))) &&
            !(k4 != null && !Utils.objEquals(v4, m.get(k4, NOT_FOUND))) &&
            !(k5 != null && !Utils.objEquals(v5, m.get(k5, NOT_FOUND)));
    }
}

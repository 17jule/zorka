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

import com.jitlogic.zorka.util.ZorkaUtil;

import java.util.Iterator;

public class LispVector implements Associative, Seq {

    public static final LispVector EMPTY = new LispVector(0);

    private static final int IBITS = 4;
    private static final int ISIZE = 1 << IBITS;
    private static final int IMASK = ISIZE - 1;

    private int flags;
    private int idx0, idx1;
    private Node root;


    public LispVector(int flags) {
        this.flags = flags;
        root = new Node(0);
    }


    private LispVector(LispVector orig) {
        this.flags = orig.flags;
        this.root = orig.root;
        this.idx0 = orig.idx0;
        this.idx1 = orig.idx1;
    }


    public LispVector assoc(int i, Object v) {
        int idx = i + idx0;
        boolean mutable = 0 != (flags & MUTABLE);

        LispVector rslt = mutable ? this : new LispVector(this);

        while (idx < 0) {
            if (rslt.root.slots[ISIZE-1] != null) {
                rslt.root = new Node(rslt.root.shift + IBITS, rslt.root);
                rslt.root.slots[1] = rslt.root.slots[0];
                rslt.root.slots[0] = null;
            } else {
                Object[] nsl = new Object[ISIZE];
                System.arraycopy(rslt.root.slots, 0, nsl, 1, ISIZE - 1);
                rslt.root.slots = nsl;
            }
            int delta = 1 << rslt.root.shift;
            idx += delta;
            rslt.idx0 += delta;
            rslt.idx1 += delta;
        }

        while (idx >= (ISIZE << rslt.root.shift)) {
            rslt.root = new Node(rslt.root.shift + IBITS, rslt.root);
        }

        rslt.root = rslt.root.assoc(idx, v, mutable);

        if (i < 0) {
            rslt.idx0 += i;
        } else if (i >= (idx1-idx0)) {
            rslt.idx1 = i + idx0 + 1;
        }

        return rslt;
    }


    public LispVector cons(Object v) {
        return assoc(-1, v);
    }


    public LispVector append(Object v) {
        return assoc(size(), v);
    }


    public LispVector subv(int begin, int end) {
        if (begin < 0 || begin > (idx1-idx0)) {
            throw new LispException("Invalid subvector start index " + begin + " (in vector of " + size() + " elements)");
        }

        if (end < begin || end > size()) {
            throw new LispException("Invalid subvector end index " + end + " (in vector of " + size()
                + "  elements and subvector start at " + begin);
        }

        LispVector rslt = new LispVector(this);
        rslt.idx0 = idx0 + begin;
        rslt.idx1 = idx0 + end;
        return rslt;
    }


    public LispVector subv(int begin) {
        return subv(begin, size());
    }

    @Override
    public Object get(Object k) {
        return get(k, null);
    }


    @Override
    public Object get(Object k, Object dv) {
        if (k == null || k.getClass() != Integer.class) {
            throw new LispException("Vectors accept only integers as keys.");
        }
        return root.get((Integer)k+idx0, dv);
    }


    @Override
    public boolean contains(Object k) {
        return get(k, NOT_FOUND) != NOT_FOUND;
    }


    @Override
    public int size() {
        return idx1 - idx0;
    }


    @Override
    public Seq seq() {
        return null;
    }


    @Override
    public int getFlags() {
        return flags;
    }


    @Override
    public void setFlags(int flags) {
        this.flags = flags;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < size(); i++) {
            if (sb.length() > 1) {
                sb.append(", ");
            }
            Object v = get(i);
            sb.append(v != null ? v.toString() : "nil");
        }
        sb.append(']');
        return sb.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || o.getClass() != LispVector.class) {
            return false;
        }
        LispVector v = (LispVector)o;
        if (v.size() != size()) {
            return false;
        }
        for (int i = 0; i < size(); i++) {
            if (!ZorkaUtil.objEquals(get(i), v.get(i))) {
                return false;
            }
        }
        return true;
    }


    @Override
    public int hashCode() {
        int hash = 0;

        for (int i = 0; i < size(); i++) {
            Object v = get(i);
            if (v != null) {
                hash += get(i).hashCode();
            }
        }

        return hash;
    }


    @Override
    public Object first() {
        return get(0);
    }


    @Override
    public Object rest() {
        return (idx1-idx0) > 1 ? subv(1) : null;
    }


    @Override
    public Iterator iterator() {
        return new SeqIterator(this);
    }


    public static class Node {
        int shift;
        Object[] slots;

        public Node(int shift) {
            this.shift = shift;
            slots = new Object[ISIZE];
        }

        public Node (int shift, Node child) {
            this.shift = shift;
            slots = new Object[ISIZE];
            slots[0] = child;
        }

        public Node(Node orig) {
            this.shift = orig.shift;
            this.slots = new Object[ISIZE];
            System.arraycopy(orig.slots, 0, this.slots, 0, ISIZE);
        }

        public Object get(int idx, Object dv) {
            int i = (idx >> shift) & IMASK;
            Object obj = slots[i];
            return shift == 0 ? obj : obj != null ? ((Node)obj).get(idx, dv) : dv;
        }

        public Node assoc(int idx, Object v, boolean mutable) {
            int i = (idx >> shift) & IMASK;
            Node rslt = mutable ? this : new Node(this);
            rslt.slots[i] = shift == 0 ? v : rslt.slots[i] == null
                ? new Node(shift - IBITS).assoc(idx, v, true)
                : ((Node)rslt.slots[i]).assoc(idx, v, mutable);
            return rslt;
        }
    }


}

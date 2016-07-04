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

import static com.jitlogic.zorka.lisp.StandardLibrary.car;
import static com.jitlogic.zorka.lisp.StandardLibrary.cdr;
import static com.jitlogic.zorka.lisp.StandardLibrary.cdar;
import static com.jitlogic.zorka.lisp.StandardLibrary.cddr;
import static com.jitlogic.zorka.lisp.StandardLibrary.cons;

public class LispHMap implements LispMap {

    private static final int HBITS = 4;
    private static final int HSIZE = 1 << HBITS;
    private static final int HMASK = HSIZE - 1;


    private interface Node {
        Node assoc(int shift, int hash, Object k, Object v, boolean mutable);
        Node dissoc(int shift, int hash, Object k, boolean mutable);
        Object get(int shift, int hash, Object k, Object dv);
        int size();
    }


    public static class INode implements Node {
        private int hashCode = -1;
        private int size = -1;
        private Node[] slots;


        public INode() {
            slots = new Node[HSIZE];
        }


        public INode(INode orig) {
            slots = new Node[HSIZE];
            System.arraycopy(orig.slots, 0, slots, 0, HSIZE);
        }


        @Override
        public Node assoc(int shift, int hash, Object k, Object v, boolean mutable) {
            int idx = (hash >> shift) & HMASK;
            hashCode = -1; size = -1;
            Node n0 = slots[idx];
            Node n1 = n0 != null ? n0.assoc(shift+HBITS, hash, k, v, mutable)
                : new LNode().assoc(shift+HBITS, hash, k, v, mutable);

            return cloneNode(idx, n0, n1, mutable);
        }


        @Override
        public Node dissoc(int shift, int hash, Object k, boolean mutable) {
            int idx = (hash >> shift) & HMASK;
            hashCode = -1; size = -1;
            Node n0 = slots[idx];
            Node n1 = n0 != null ? n0.dissoc(shift+HBITS, hash, k, mutable) : null;

            return cloneNode(idx, n0, n1, mutable);
        }


        private Node cloneNode(int idx, Node n0, Node n1, boolean mutable) {
            if (n0 != n1) {
                INode rslt = mutable ? this : new INode(this);
                rslt.slots[idx] = n1;
                return rslt;
            } else {
                return this;
            }
        }


        @Override
        public Object get(int shift, int hash, Object k, Object dv) {
            int idx = (hash >> shift) & HMASK;
            Node n = slots[idx];
            return n != null ? n.get(shift+HBITS, hash, k, dv) : dv;
        }


        @Override
        public int size() {
            if (size == -1) {
                size = 0;
                for (Node slot : slots) {
                    if (slot != null) {
                        size += slot.size();
                    }
                }
            }
            return size;
        }


        @Override
        public int hashCode() {
            if (hashCode == -1) {
                hashCode = 0;
                for (Node slot : slots) {
                    if (slot != null) {
                        hashCode += slot.hashCode();
                    }
                }
            }
            return hashCode;
        }
    }


    private static class LNode implements Node {
        private int hashCode = -1;
        private int size;
        private Object[] slots;

        public LNode() {
            this.size = 0;
            this.slots = new Object[HSIZE*2];
        }

        public LNode(LNode orig) {
            this.size = orig.size;
            this.slots = new Object[HSIZE*2];
            System.arraycopy(orig.slots, 0, slots, 0, size*2);
        }


        @Override
        public Node assoc(int shift, int hash, Object k, Object v, boolean mutable) {

            for (int i = 0; i < size * 2; i += 2) {
                if (k.equals(slots[i])) {
                    LNode rslt = mutable ? this : new LNode(this);
                    rslt.slots[i+1] = v;
                    return rslt;
                }
            }

            if (size < slots.length/2) {
                // There is still some space in leaf node.
                LNode rslt = mutable ? this : new LNode(this);
                if (mutable) hashCode = -1;
                int idx = size * 2;
                rslt.slots[idx] = k;
                rslt.slots[idx + 1] = v;
                rslt.size += 1;
                return rslt;
            } else if (shift >= 32) {
                // Overflow but we're at the end of hash chain. Resize leaf node.
                LNode rslt = mutable ? this : new LNode();
                if (mutable) hashCode = -1;
                Object[] nslots = new Object[slots.length * 2];
                System.arraycopy(slots, 0, nslots, 0, slots.length);
                nslots[size * 2] = k;
                nslots[size * 2 + 1] = v;
                rslt.size = size + 1;
                rslt.slots = nslots;
                return rslt;
            } else {
                // Overflow. Upgrade to INode
                Node rslt = new INode();
                for (int i = 0; i < size * 2; i+=2) {
                    rslt = rslt.assoc(shift, slots[i].hashCode(), slots[i], slots[i+1], true);
                }
                return rslt.assoc(shift, hash, k, v, true);
            }
        }


        @Override
        public Node dissoc(int shift, int hash, Object k, boolean mutable) {
            for (int i = 0; i < size * 2; i += 2) {
                if (k.equals(slots[i])) {
                    if (size == 1) {
                        return null;
                    } else {
                        LNode rslt = mutable ? this : new LNode(this);
                        if (mutable) hashCode = -1;
                        rslt.slots[i] = i == size * 2 - 2 ? null : rslt.slots[size * 2 - 2];
                        rslt.slots[i+1] = i == size * 2 - 2 ? null : rslt.slots[size * 2 - 1];
                        rslt.size -= 1;
                        return rslt;
                    }
                }
            }
            return this;
        }


        @Override
        public Object get(int shift, int hash, Object k, Object dv) {
            for (int i = 0; i < size * 2; i += 2) {
                if (k.equals(slots[i])) {
                    return slots[i+1];
                }
            }
            return dv;
        }


        @Override
        public int size() {
            return size;
        }


        @Override
        public int hashCode() {
            if (hashCode == -1) {
                hashCode = 0;
                for (int i = 0; i < size * 2; i++) {
                    if (slots[i] != null) {
                        hashCode += slots[i].hashCode();
                    }
                }
            }
            return hashCode;
        }
    }


    public static class NodeSeqTail {
        private int[] idxStack;
        private Node[] nodeStack;
        private int stackPos;


        public NodeSeqTail(Node n) {
            idxStack = new int[10];
            nodeStack = new Node[10];
            nodeStack[0] = n;
            idxStack[0] = -1;
            stackPos = 0;
        }

        public Object key() {
            if (stackPos < 0) return null;
            Node n = nodeStack[stackPos];
            int i = idxStack[stackPos];
            return n != null && n.getClass() == LNode.class && i < n.size()
                ? ((LNode)n).slots[i * 2] : null;
        }


        public Object value() {
            if (stackPos < 0) return null;
            Node n = nodeStack[stackPos];
            int i = idxStack[stackPos];
            return n != null && n.getClass() == LNode.class && i < n.size()
                ? ((LNode)n).slots[i * 2 + 1] : null;
        }


        public Seq pair() {
            Node n = nodeStack[stackPos];
            int i = idxStack[stackPos];
            return n != null && n.getClass() == LNode.class && i < n.size()
                ? cons(((LNode)n).slots[i * 2], cons(((LNode)n).slots[i * 2 + 1], null)) : null;
        }


        public boolean next() {
            if (stackPos < 0) return false;
            Node n = nodeStack[stackPos];
            if (n == null) return false;
            if (n.getClass() == LNode.class) {
                if (idxStack[stackPos] < n.size()-1) {
                    idxStack[stackPos]++;
                    return true;
                } else {
                    stackPos--;
                    return next();
                }
            } else  {
                for (int i = idxStack[stackPos] + 1; i < HSIZE; i++) {
                    Node n1 = ((INode)n).slots[i];
                    if (n1 != null) {
                        idxStack[stackPos] = i;
                        stackPos++;
                        nodeStack[stackPos] = n1;
                        idxStack[stackPos] = -1;
                        return next();
                    }
                }
                // Nothing found. Get one level up and continue searching there.
                stackPos--;
                return next();
            }
        }
    }


    public static class NodeSeq implements Seq {
        private Object pair;
        private Object next;

        public NodeSeq(Object pair, Object next) {
            this.pair = pair;
            this.next = next;
        }

        @Override
        public Object first() {
            return pair;
        }

        @Override
        public Object rest() {
            if (next != null && next.getClass() == NodeSeqTail.class) {
                NodeSeqTail tail = (NodeSeqTail)next;
                if (tail.next()) {
                    next = new NodeSeq(tail.pair(), tail);
                } else {
                    next = null;
                }
            }
            return next;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append('(');

            for (Seq seq = this; seq != null; seq = (Seq)cdr(seq)) {
                if (sb.length() > 1) {
                    sb.append(' ');
                }
                sb.append(car(seq));
            }

            sb.append(')');

            return sb.toString();
        }
    }

    private int flags;

    private Node root;

    public LispHMap() {
        this.flags = 0;
    }

    public LispHMap(int flags) {
        this.flags = flags;
    }

    public LispHMap(int flags, Seq args) {
        this.flags = flags;
        if (args != null) {
            root = new LNode();
            for (Seq seq = args; seq != null; seq = (Seq)cddr(seq)) {
                Object k = car(seq), v = cdar(seq);
                root = root.assoc(0, k.hashCode(), k, v, true);
            }
        }
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
        int hash = k.hashCode();
        boolean mutable = 0 != (flags & MUTABLE);
        Node n0 = root == null ? new LNode() : root;
        n0 = n0.assoc(0, hash, k, v, mutable);
        LispHMap rslt = mutable ? this : new LispHMap(flags);
        rslt.root = n0;
        return rslt;
    }

    @Override
    public LispMap dissoc(Object k) {
        if (k == null) k = NULL_KEY;
        if (root == null) return this;

        int hash = k.hashCode();
        boolean mutable = 0 != (flags & MUTABLE);

        Node n0 = root.dissoc(0, hash, k, mutable);

        if (n0 != root) {
            LispHMap rslt = mutable ? this : new LispHMap(flags);
            rslt.root = n0;
            return rslt;
        } else {
            return this;
        }
    }

    @Override
    public Object get(Object k) {
        return get(k, null);
    }

    @Override
    public Object get(Object k, Object dv) {
        if (k == null) k = NULL_KEY;
        return root != null ? root.get(0, k.hashCode(), k, dv) : dv;
    }

    @Override
    public boolean contains(Object k) {
        if (k == null) k = NULL_KEY;
        return root != null && root.get(0, k.hashCode(), k, NOT_FOUND) != NOT_FOUND;
    }

    @Override
    public int size() {
        return root != null ? root.size() : 0;
    }

    @Override
    public int hashCode() {
        return root != null ? root.hashCode() : 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        if (root != null) {
            NodeSeqTail tail = new NodeSeqTail(root);
            while (tail.next()) {
                if (sb.length() > 1) {
                    sb.append(", ");
                }
                Object key = tail.key();
                sb.append(key);
                sb.append(' ');
                sb.append(tail.value());
            }
        }
        sb.append('}');
        return sb.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LispMap)) return false;
        LispMap m = (LispMap) o;
        if (m.size() != size()) {
            return false;
        }
        NodeSeqTail tail = new NodeSeqTail(root);
        while (tail.next()) {
            Object k = tail.key(), v = tail.value();
            if (!Utils.objEquals(v, get(k, NOT_FOUND))) {
                return false;
            }
        }
        return true;
    }


    @Override
    public Seq seq() {
        if (root != null) {
            NodeSeqTail tail = new NodeSeqTail(root);
            return tail.next() ? new NodeSeq(tail.pair(), tail) : null;
        } else {
            return null;
        }
    }
}

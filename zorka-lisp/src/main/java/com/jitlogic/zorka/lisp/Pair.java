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

/**
 * Lisp Cell represents one list element. This is single linked list.
 */
public class Pair implements Seq {

    /**
     * Reference to object referenced by cell (car).
     */
    private Object first;

    /**
     * Reference to rest list cell (cdr);
     */
    private Object rest;

    public Pair() { this(null); }

    public Pair(Object first) {
        this(first, null);
    }

    public Pair(Object first, Object rest) {
        this.first = first;
        this.rest = rest;
    }


    @Override
    public Object first() {
        return first;
    }


    @Override
    public Object rest() {
        return rest;
    }


    public void setFirst(Object first) { this.first = first; }

    public void setRest(Object rest) {
        this.rest = rest;
    }


    @Override
    public String toString() {
        return Utils.strSeq(this);
    }


    @Override
    public int hashCode() {
        return Utils.seqHash(this);
    }


    @Override
    public boolean equals(Object o) {
        return o != null &&
            (o == this || o instanceof Seq &&
                Utils.lstEquals((Seq) o, this));
    }

    @Override
    public Iterator iterator() {
        return new SeqIterator(this);
    }
}

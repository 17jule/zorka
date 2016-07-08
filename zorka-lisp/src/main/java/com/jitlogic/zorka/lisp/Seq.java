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
 * Common interface for all sequence objects. It applies to
 * linked lists (eg. LispPair), lazy evaluation constructs
 * and wrappers for all kinds of collections etc.
 */
public interface Seq extends Iterable {

    /**
     * Returns value of current sequence item.
     */
    Object first();

    /**
     * Returns next sequence item or null if current is the last one.
     */
    Object rest();


    public static class SeqIterator<T> implements Iterator<T> {

        private Seq seq;

        public SeqIterator(Seq seq) {
            this.seq = seq;
        }

        @Override
        public boolean hasNext() {
            return seq != null;
        }

        @Override
        public T next() {
            Object rslt = seq.first();
            seq = (Seq)seq.rest();
            return (T)rslt;
        }

        @Override
        public void remove() {
            throw new LispException("Cannot modify LISP Seq.");
        }
    }

}

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

package com.jitlogic.zorka.lisp;

import java.util.Iterator;

public class IterSeq implements Seq {

    private Iterator iter;

    private volatile Object first = this;
    private volatile Object rest  = this;

    public IterSeq(Iterator iter) {
        this.iter = iter;
    }

    @Override
    public Object first() {
        if (first == this) {
            synchronized (this) {
                if (first == this) {
                    first = iter.next();
                }
            }
        }
        return first;
    }

    @Override
    public Object rest() {
        if (rest == this) {
            synchronized (this) {
                if (rest == this) {
                    first();
                    rest = iter.hasNext() ? new IterSeq(iter) : null;
                }
            }
        }
        return rest;
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
}

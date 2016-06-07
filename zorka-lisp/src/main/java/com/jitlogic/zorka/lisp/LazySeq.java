/**
 * Copyright 2012-2015 Rafal Lewczuk <rafal.lewczuk@jitlogic.com>
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

public class LazySeq implements Seq {

    private Interpreter ctx;

    private Fn fn;
    private volatile Object rslt = this;

    public LazySeq(Interpreter ctx, Fn fn) {
        this.ctx = ctx;
        this.fn = fn;
    }


    @Override
    public Object first() {
        if (rslt == this) {
            synchronized (this) {
                if (rslt == this) {
                    rslt = fn.apply(ctx, ctx.env(), null);
                    fn = null; // to free up memory after temporary fn
                }
            }
        }
        return StandardLibrary.car(rslt);
    }


    @Override
    public synchronized Object rest() {
        if (rslt == this) {
            synchronized (this) {
                if (rslt == this) {
                    first();
                }
            }
        }
        return StandardLibrary.cdr(rslt);
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

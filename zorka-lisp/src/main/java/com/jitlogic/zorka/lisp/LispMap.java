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

public interface LispMap {

    public static final Object NULL_KEY = new Object();

    public static final Object NOT_FOUND = new Object();

    public static final int MUTABLE = 0x00000001;

    LispMap assoc(Object k, Object v);

    LispMap dissoc(Object k);

    Object get(Object k);

    Object get(Object k, Object dv);

    boolean contains(Object k);

    int size();

    Seq seq();

    int getFlags();

    void setFlags(int flags);
}

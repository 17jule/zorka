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

import java.util.Map;

public interface LispMap extends Iterable<LispMap.Entry>, Associative {

    public static final Object NULL_KEY = new Object();

    LispMap assoc(Object k, Object v);

    LispMap dissoc(Object k);

    public static class Entry implements Map.Entry {
        Object k, v;
        LispMap m;

        public Entry(LispMap m, Object k, Object v) {
            this.m = m;
            this.k = k;
            this.v = v;
        }

        public Object getKey() {
            return k;
        }

        public Object getValue() {
            return v;
        }

        @Override
        public Object setValue(Object value) {
            if (0 != (m.getFlags() & MUTABLE)) {
                m.assoc(k, value);
            } else {
                throw new LispException("Cannot modify immutable map.");
            }
            return null;
        }
    }


}

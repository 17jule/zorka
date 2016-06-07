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

import java.util.HashMap;
import java.util.Map;

public class Keyword {
    String ns;

    String name;


    public Keyword(String ns, String name) {
        this.ns = ns;
        this.name = name;
    }

    public String getNs() {
        return ns;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return ":" + (ns != null ? ns + "/" + name : name);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (o.getClass() == this.getClass()) {
            Keyword sym = (Keyword)o;
            return name.equals(sym.name) && Utils.objEquals(ns, sym.ns);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 31 * name.hashCode() + (ns != null ? 17 * ns.hashCode() : 0);
    }

    private static Map<String,Keyword> keywords = new HashMap<>();

    /**
     * Returns keyword of given name.
     * @param name symbol name (
     */
    public static synchronized Keyword keyword(String name) {
        String nname = name.trim();
        if (!keywords.containsKey(nname)) {
            String[] nns = nname.split("/");
            Keyword kw = nns.length == 1 ? new Keyword(null, name) : new Keyword(nns[0], nns[1]);
            keywords.put(nname, kw);
        }
        return keywords.get(nname);
    }

}

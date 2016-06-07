/**
 * Copyright 2012-2015 Rafal Lewczuk <rafal.lewczuk@jitlogic.com>
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LISP Environment contains symbol to variable bindings.
 */
public class Environment {

    private Map<Symbol,Object> vars = new HashMap<Symbol, Object>();

    private Environment parent;

    public Environment(Environment parent) {
        this.parent = parent;
    }

    public Environment(Environment parent, Map<Symbol,Object> vars) {
        this.parent = parent;
        this.vars = vars;
    }


    public Object lookup(Symbol sym) {
        if (vars.containsKey(sym)) {
            return vars.get(sym);
        } else if (parent != null) {
            return parent.lookup(sym);
        } else if (sym.getNs() != null) {
            Class<?> c = ObjectInspector.findClass(sym.getNs());
            if (c != null) {
                Field f = ObjectInspector.lookupField(c, sym.getName());
                if (f != null && Modifier.isStatic(f.getModifiers())) {
                    return ObjectInspector.getField(null, f);
                }
                List<Method> methods = ObjectInspector.lookupMethods(c, sym.getName(), true);
                Method m = methods.size() > 0 ? methods.get(0) : null;
                if (methods.size() == 1) {
                    return new JavaMethod(methods.get(0), null);
                }
            }
            return null;
        } else {
            throw new LispException("Cannot resolve variable '" + sym + "'");
        }
    }

    public Object define(Symbol sym, Object val) {
        vars.put(sym, val);
        return val;
    }

    public Object set(Symbol sym, Object val) {
        if (vars.containsKey(sym)) {
            vars.put(sym, val);
            return val;
        } else if (parent != null) {
            return parent.set(sym, val);
        }
        throw new LispException("Cannot find symbol: " + sym);
    }
}

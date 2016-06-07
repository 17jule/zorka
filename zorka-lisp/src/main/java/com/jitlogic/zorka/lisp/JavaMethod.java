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

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


import static com.jitlogic.zorka.lisp.StandardLibrary.car;
import static com.jitlogic.zorka.lisp.StandardLibrary.length;
import static com.jitlogic.zorka.lisp.Utils.next;

public class JavaMethod implements Fn {

    private Method method;
    private Object obj;

    public JavaMethod(Method method, Object obj) {
        this.method = method;
        this.obj = obj;
    }


    @Override
    public Object apply(Interpreter ctx, Environment env, Seq args) {
        try {
            Object[] xargs = new Object[method.getParameterTypes().length]; // TODO this is expensive
            for (int i = 0; i < xargs.length - (method.isVarArgs() ? 1 : 0); i++) {
                xargs[i] = car(args);
                args = next(args);
            }
            if (method.isVarArgs()) {
                Class<?> atype = method.getParameterTypes()[method.getParameterTypes().length - 1];  // TODO this is expensive
                Object[] vargs = (Object[])Array.newInstance(atype.getComponentType(), length(args));
                for (int i = 0; i < vargs.length; i++) {
                    Object v = car(args);
                    vargs[i] = v;
                    args = next(args);
                }
                xargs[xargs.length-1] = vargs;
            }
            return method.invoke(obj, xargs);
        } catch (IllegalAccessException e) {
            throw new LispException("Method inaccessible", e);
        } catch (InvocationTargetException e) {
            throw new LispException("Method invocation failed: " , e.getCause());
        }
    }

    @Override
    public boolean isMacro() {
        return false;
    }
}

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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NativeMethod implements Fn {

    private Method method;
    private Object obj;

    private boolean isMacro;


    public NativeMethod(Method method, Object obj) {
        this.method = method;
        this.obj = obj;

        Primitive ann = method.getAnnotation(Primitive.class);
        isMacro = ann != null && ann.isMacro();
    }

    @Override
    public Object apply(Interpreter ctx, Environment env, Seq args) {
        try {
            if (method.getParameterTypes().length == 3) {  // TODO this is expensive
                return method.invoke(obj, ctx, env, args);
            } else {
                return method.invoke(obj, args);
            }
        } catch (IllegalAccessException e) {
            throw new LispException("Method inaccessible", e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof LispException) {
                throw (LispException)e.getCause();
            } else {
                throw new LispException("Method invocation failed: ", e.getCause());
            }
        }
    }

    @Override
    public boolean isMacro() {
        return isMacro;
    }
}

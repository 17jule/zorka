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

import static com.jitlogic.zorka.lisp.StandardLibrary.car;

public class Continuation implements Fn {

    private LispException jmp;
    private Object value;

    public Continuation(LispException jmp) {
        this.jmp = jmp;
    }

    @Override
    public Object apply(Interpreter ctx, Environment env, Seq args) {
        this.value = car(args);
        throw jmp;
    }

    @Override
    public boolean isMacro() {
        return false;
    }

    public Object getValue() {
        return value;
    }
}

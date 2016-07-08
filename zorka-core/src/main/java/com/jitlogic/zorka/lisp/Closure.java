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

import static com.jitlogic.zorka.lisp.StandardLibrary.car;
import static com.jitlogic.zorka.lisp.Utils.next;

public class Closure implements Fn {

    private Interpreter ctx;
    private Environment env;

    private Object params;
    private Seq code;

    private boolean macro;


    public Closure(Interpreter ctx, Environment env, Object params, Seq code, boolean macro) {
        this.ctx = ctx;
        this.env = env;
        this.params = params;
        this.code = code;
        this.macro = macro;
    }

    @Override
    public boolean isMacro() {
        return macro;
    }

    @Override
    public Object apply(Interpreter ctx, Environment _env, Seq args) {

        Map<Symbol, Object> pmap = ObjectInspector.destructure(params, args);

        Environment env1 = new Environment(env, pmap);

        Object rslt = null;

        for (Seq form = code; form != null; form = next(form)) {
            rslt = ctx.eval(car(form), env1);
        }

        return rslt;
    }


    @Override
    public String toString() {
        return "(" + (macro ? "macro " : "lambda ") +
            (params != null ? params : "()") + " " +
            code + ")";
    }
}

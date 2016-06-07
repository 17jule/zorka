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

package com.jitlogic.zorka.lisp.support;

import com.jitlogic.zorka.lisp.*;
import org.junit.Assert;

import static com.jitlogic.zorka.lisp.StandardLibrary.car;
import static com.jitlogic.zorka.lisp.StandardLibrary.cdr;
import static com.jitlogic.zorka.lisp.StandardLibrary.cadr;
import static com.jitlogic.zorka.lisp.StandardLibrary.cddr;
import static com.jitlogic.zorka.lisp.StandardLibrary.caddr;

public class LispTestLibrary {

    private Interpreter ctx;

    public LispTestLibrary(Interpreter ctx) {
        this.ctx = ctx;
    }


    @Primitive(value = "is", isNative = true, isMacro = true)
    public void assertTrue(Interpreter ctx, Environment env, Seq args) {
        Assert.assertTrue(car(args) + " == false " + (cdr(args) != null ? "\n\n" + cadr(args) + "\n" : ""),
            Utils.isTrue(ctx.eval(car(args),env)));
    }

    @Primitive(value = "isnt", isNative = true, isMacro = true)
    public void assertFalse(Interpreter ctx, Environment env, Seq args) {
        Assert.assertTrue(car(args) + " == false " + (cdr(args) != null ? "\n\n" + cadr(args) + "\n" : ""),
            !Utils.isTrue(ctx.eval(car(args),env)));
    }

    @Primitive(value = "is=", isNative = true, isMacro = true)
    public void isEqual(Interpreter ctx, Environment env, Seq args) {
        Assert.assertEquals(car(args) + " != " + cadr(args) +
            (cddr(args) != null ? "\n\n" + caddr(args) + "\n" : ""),
            ctx.eval(car(args),env),
            ctx.eval(cadr(args),env));
    }

    @Primitive(value = "is!=", isNative = true, isMacro = true)
    public void isNotEqual(Interpreter ctx, Environment env, Seq args) {
        Assert.assertNotEquals(car(args) + " != " + cadr(args) +
                (cddr(args) != null ? "\n\n" + caddr(args) + "\n" : ""),
            ctx.eval(car(args),env),
            ctx.eval(cadr(args),env));
    }

    @Primitive(value = "iseq", isNative = true, isMacro = true)
    public void isSame(Interpreter ctx, Environment env, Seq args) {
        Assert.assertSame(car(args) + " != " + cadr(args) +
                (cddr(args) != null ? "\n\n" + caddr(args) + "\n" : ""),
            ctx.eval(car(args),env),
            ctx.eval(cadr(args),env));
    }

}

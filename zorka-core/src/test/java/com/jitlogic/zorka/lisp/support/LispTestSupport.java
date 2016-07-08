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

package com.jitlogic.zorka.lisp.support;

import com.jitlogic.zorka.lisp.*;
import org.junit.Before;

import java.util.ArrayList;
import java.util.List;

public class LispTestSupport {

    protected Interpreter ctx;
    protected Environment env;

    @Before
    public void setUpInterpreter() {
        ctx = new Interpreter();
        env = ctx.env();
        ctx.install(new StandardLibrary(ctx));
        ctx.evalScript("/com/jitlogic/zorka/lisp/boot.zcm");
    }

    protected Object eval(Seq form) {
        return ctx.eval(form);
    }

    protected Object eval(String s) {
        return ctx.evalStr(s);
    }

    protected Object read(String s) {
        Reader rdr = new Reader(s);
        return rdr.read();
    }


    protected Seq readAll(String s) {
        Reader rdr = new Reader(s);
        return rdr.readAll();
    }


    protected Symbol sym(String name) {
        return Symbol.symbol(name);
    }

    protected Keyword kw(String name) {
        return Keyword.keyword(name);
    }


    protected Pair lst(Object...objs) {
        Pair head = null;

        for (int i = objs.length-1; i >= 0; i--) {
            head = new Pair(objs[i], head);
        }

        return head;
    }

    protected Pair pair(Object first, Object rest) {
        return new Pair(first, rest);
    }

    protected List vec(Object...objs) {
        List<Object> vec = new ArrayList<Object>(objs.length);
        for (Object obj : objs) {
            vec.add(obj);
        }
        return vec;
    }


}

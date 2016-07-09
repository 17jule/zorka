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

package com.jitlogic.zorka.test.lisp;

import com.jitlogic.zorka.lisp.Fn;
import com.jitlogic.zorka.test.lisp.support.LispTestSupport;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ObjReflectionUnitTest extends LispTestSupport {


    @Test
    public void testLookupStaticFields() {
        assertEquals(true,env.lookup(sym("Boolean/TRUE")));
        assertEquals(false, env.lookup(sym("Boolean/FALSE")));
        assertEquals(null, env.lookup(sym("Boolean/NON_EXISTENT")));
        assertEquals(Integer.MAX_VALUE, env.lookup(sym("Integer/MAX_VALUE")));

        assertEquals(true, eval("java.lang.Boolean/TRUE"));
        assertEquals(false, eval("Boolean/FALSE"));
    }

    public static final int F1 = 10;
    private static final int F2 = 20;

    @Test @Ignore
    public void testLookupStaticFieldsWithVariousModifiers() {
        assertEquals(10, env.lookup(sym("com.jitlogic.zorka.lisp.ObjReflectionUnitTest/F1")));
        assertEquals(20, env.lookup(sym("com.jitlogic.zorka.lisp.ObjReflectionUnitTest/F2")));
    }

    public static String m1() {
        return "m1";
    }

    private static String m2() {
        return "m2";
    }

    @Test @Ignore
    public void testLookupStaticMethod() {
        assertTrue(env.lookup(sym("com.jitlogic.zorka.lisp.ObjReflectionUnitTest/m1")) instanceof Fn);
        assertEquals("m1", eval("(com.jitlogic.zorka.lisp.ObjReflectionUnitTest/m1)"));

        assertTrue(env.lookup(sym("com.jitlogic.zorka.lisp.ObjReflectionUnitTest/m2")) instanceof Fn);
    }
}

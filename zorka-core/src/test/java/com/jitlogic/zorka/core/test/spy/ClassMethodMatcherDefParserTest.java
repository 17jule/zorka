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

package com.jitlogic.zorka.core.test.spy;

import com.jitlogic.zorka.core.spy.SpyMatcher;
import org.junit.Test;

import static org.junit.Assert.*;

import static com.jitlogic.zorka.core.spy.SpyMatcher.*;
import static org.objectweb.asm.Opcodes.*;

public class ClassMethodMatcherDefParserTest {
    @Test
    public void testSpyMatcherFromString() {
        SpyMatcher sm = SpyMatcher.fromString("com.jitlogic.**");
        assertEquals(SpyMatcher.DEFAULT_PRIORITY, sm.getPriority());
        assertEquals("com\\.jitlogic\\..+", sm.getClassPattern().toString());
        assertEquals("[a-zA-Z0-9_]+", sm.getMethodPattern().toString());
    }


    @Test
    public void testSpyMatcherFromStringWithCustomPriorityAndMethodName() {
        SpyMatcher sm = SpyMatcher.fromString("999:com.jitlogic.**/myMethod");
        assertEquals(999, sm.getPriority());
        assertEquals("com\\.jitlogic\\..+", sm.getClassPattern().toString());
        assertEquals("myMethod", sm.getMethodPattern().toString());
    }


    @Test
    public void testSpyMatcherFromStringInterfaceDef() {
        SpyMatcher sm = SpyMatcher.fromString("999:com.myapp.MyIface#");
        assertTrue(0 != (sm.getFlags() & BY_INTERFACE));
        assertTrue(0 == (sm.getFlags() & BY_CLASS_NAME));
        assertEquals(999, sm.getPriority());
    }


    @Test
    public void testSpyMatcherClassAnnotationFromStr() {
        SpyMatcher sm = SpyMatcher.fromString("@com.myapp.MyAnnotation");
        assertTrue(0 != (sm.getFlags() & BY_CLASS_ANNOTATION));
        assertTrue(0 == (sm.getFlags() & BY_CLASS_NAME));
        assertEquals("com\\.myapp\\.MyAnnotation", sm.getClassPattern().toString());
    }


    @Test
    public void testSpyMatcherMethodAnnotationFromStr() {
        SpyMatcher sm = SpyMatcher.fromString("**/@com.myapp.MethodAnnotation");
        assertTrue("Should encounter BY_MEHTOD_ANNOTATION flag.", 0 != (sm.getFlags() & BY_METHOD_ANNOTATION));
        assertTrue("Should NOT encounter BY_METHOD_NAME flag.", 0 == (sm.getFlags() & BY_METHOD_NAME));
        assertEquals("com\\.myapp\\.MethodAnnotation", sm.getMethodPattern().toString());
    }


    @Test
    public void testSpyMatcherWithArgTypes() {
        SpyMatcher sm = SpyMatcher.fromString("**/myMethod(int,int)");
        assertEquals("myMethod", sm.getMethodPattern().toString());
        assertEquals("^\\(II\\).*$", sm.getSignaturePattern().toString());
    }

    @Test
    public void testSpyMatcherWithArgTypesSP() {
        SpyMatcher sm = SpyMatcher.fromString("**/ myMethod ( int , int ) ");
        assertEquals("myMethod", sm.getMethodPattern().toString());
        assertEquals("^\\(II\\).*$", sm.getSignaturePattern().toString());
    }


    @Test
    public void testSpyMatcherWithUnfinishedArgs() {
        SpyMatcher sm = SpyMatcher.fromString("**/myMethod(int,int,...)");
        assertEquals("myMethod", sm.getMethodPattern().toString());
        assertEquals("^\\(II.*\\).*$", sm.getSignaturePattern().toString());
    }

    @Test
    public void testSpyMatcherWithArgsAndRetType() {
        SpyMatcher sm = SpyMatcher.fromString("**/int myMethod(int,int)");
        assertEquals("myMethod", sm.getMethodPattern().toString());
        assertEquals("^\\(II\\)I$", sm.getSignaturePattern().toString());
    }

    @Test
    public void testSpyMatcherWithArgsAndRetTypeAndAcceessFlag() {
        SpyMatcher sm = SpyMatcher.fromString("**/private int myMethod(int,int)");
        assertEquals("myMethod", sm.getMethodPattern().toString());
        assertEquals("^\\(II\\)I$", sm.getSignaturePattern().toString());
        assertTrue("Should have ACC_PRIVATE flag.", 0 != (sm.getAccess() & ACC_PRIVATE));
    }

    @Test
    public void testSpyMatcherWithArgsAndRetTypeAndAcceessFlagSP() {
        SpyMatcher sm = SpyMatcher.fromString("**/ private int myMethod ( int , int ) ");
        assertEquals("myMethod", sm.getMethodPattern().toString());
        assertEquals("^\\(II\\)I$", sm.getSignaturePattern().toString());
        assertTrue("Should have ACC_PRIVATE flag.", 0 != (sm.getAccess() & ACC_PRIVATE));
    }

}

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
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this software. If not, see <http://www.gnu.org/licenses/>.
 */
package com.jitlogic.zorka.test.spy;

import com.jitlogic.zorka.util.ZorkaUtil;
import com.jitlogic.zorka.spy.SpyMatcherSet;
import com.jitlogic.zorka.test.spy.support.*;
import com.jitlogic.zorka.test.support.ZorkaFixture;
import com.jitlogic.zorka.spy.SpyMatcher;

import org.junit.Assert;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import static com.jitlogic.zorka.spy.SpyMatcher.fromString;

public class ClassMethodMatchingUnitTest extends ZorkaFixture {

    @Test
    public void testSimpleClassOnlyMatch() {
        SpyMatcherSet sms = new SpyMatcherSet(new SpyMatcher(SpyMatcher.BY_CLASS_NAME, 0xFF, "com.jitlogic.zorka.core.spy.**", "*", null));

        Assert.assertTrue(sms.classMatch("com.jitlogic.zorka.core.spy.unittest.SomeClass"));
        Assert.assertTrue(sms.classMatch("com.jitlogic.zorka.core.spy.AClass"));
        Assert.assertFalse(sms.classMatch("comXjitlogicXzorkaXspyXAClass"));
    }

    @Test
    public void testClassMatchWithSingleLevelWildcard() {
        SpyMatcherSet sms = new SpyMatcherSet(new SpyMatcher(SpyMatcher.BY_CLASS_NAME, 0xFF, "com.jitlogic.zorka.core.spy.*", "*", null));
        Assert.assertFalse(sms.classMatch("com.jitlogic.zorka.core.spy.unittest.SomeClass"));
        Assert.assertTrue(sms.classMatch("com.jitlogic.zorka.core.spy.AClass"));
    }

    @Test
    public void testClassMethodMatch() {
        SpyMatcherSet sms = new SpyMatcherSet(fromString("test.SomeClass/get*"));
        Assert.assertTrue(sms.methodMatch("test.SomeClass", null, null, 1, "getVal", "()I", null));
        Assert.assertFalse(sms.methodMatch("test.SomeClass", null, null, 1, "setVal", "(I)V", null));
    }

    @Test
    public void testClassMethodStrictMatch() {
        SpyMatcherSet sms = new SpyMatcherSet(fromString("test.SomeClass/get"));
        Assert.assertTrue(sms.methodMatch("test.SomeClass", null, null, 1, "get", "()I", null));
        Assert.assertFalse(sms.methodMatch("test.SomeClass", null, null, 1, "getVal", "()I", null));
    }

    @Test
    public void testClassMatchSignatureWithoutTypes() {
        SpyMatcherSet sms = new SpyMatcherSet(fromString("test.*/get"));
        Assert.assertTrue(sms.methodMatch("test.SomeClass", null, null, 1, "get", "()V", null));
        Assert.assertTrue(sms.methodMatch("test.SomeClass", null, null, 1, "get", "(II)V", null));
        //Assert.assertFalse(sms.methodMatch("test.SomeClass", null, null, 1, "get", "malformed", null));
    }

    @Test @Ignore
    public void testClassMatchSignatureWithReturnVoidType() {
//        SpyMatcherSet sms = new SpyMatcherSet(spy.byMethod(0xff, "test.*", "someMethod", "void"));
//        Assert.assertTrue(sms.methodMatch("test.SomeClass", null, null, 1, "someMethod", "()V", null));
//        Assert.assertFalse(sms.methodMatch("test.SomeClass", null, null, 1, "someMethod", "()Void", null));
//        Assert.assertFalse(sms.methodMatch("test.SomeClass", null, null, 1, "someMethod", "()I", null));
    }

    @Test @Ignore
    public void testClassMatchSignatureReturnClassType() {
//        SpyMatcherSet sms = new SpyMatcherSet(spy.byMethod(0xff, "test.*", "get", "java.lang.String"));
//        Assert.assertTrue(sms.methodMatch("test.someClass", null, null, 1, "get", "()Ljava/lang/String;", null));
    }

    @Test @Ignore
    public void testClassMatchWithNullArgument() {
//        SpyMatcherSet sms = new SpyMatcherSet(
//                spy.byMethod(0, "javax.naming.directory.InitialDirContext", "search", null, "String", "String", null,
//                        "javax.naming.directory.SearchControls"));
//        Assert.assertTrue(sms.methodMatch("javax.naming.directory.InitialDirContext", null, null, 1, "search",
//                "(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;Ljavax/naming/directory/SearchControls;)Ljavax/naming/NamingEnumeration;", null));
    }

    @Test @Ignore
    public void testClassMatchWithArrayArgument() {
//        SpyMatcherSet sms = new SpyMatcherSet(
//                spy.byMethod(0, "javax.naming.directory.InitialDirContext", "search", null, "String", "String", "Object[]",
//                        "javax.naming.directory.SearchControls"));
//        Assert.assertTrue(sms.methodMatch("javax.naming.directory.InitialDirContext", null, null, 1, "search",
//                "(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;Ljavax/naming/directory/SearchControls;)Ljavax/naming/NamingEnumeration;", null));
    }

    @Test @Ignore
    public void testClassMatchSignatureWithSimpleReturnAndArgumentType() {
//        SpyMatcherSet sms = new SpyMatcherSet(spy.byMethod(0xff, "test.*", "frobnicate", "int", "int"));
//        SpyMatcher cm = new SpyMatcher(0, 0xFF, "test.*", "frobnicate", "int", "int");
//        Assert.assertTrue(sms.methodMatch("test.someClass", null, null, 1, "frobnicate", "(I)I", null));
//        Assert.assertFalse(sms.methodMatch("test.someClass", null, null, 1, "frobnicate", "(J)I", null));
    }


    @Test @Ignore
    public void testClassMatchSignatureWithStringType() {
//        SpyMatcherSet sms = new SpyMatcherSet(spy.byMethod(0xff, "test.*", "frobnicate", "String", "String"));
//        Assert.assertTrue(sms.methodMatch("test.someClass", null, null, 1, "frobnicate", "(Ljava/lang/String;)Ljava/lang/String;", null));
//        Assert.assertFalse(sms.methodMatch("test.someClass", null, null, 1, "frobnicate", "(J)I", null));
    }

    @Test @Ignore
    public void testClassMatchWithVariousArgs() {
//        SpyMatcherSet sms = new SpyMatcherSet(spy.byMethod(0xff, "test.*", "frobnicate", "String", "int", "com.jitlogic.zorka.spy.CallInfo"));
//        Assert.assertTrue(sms.methodMatch("test.SomeClass", null, null, 1, "frobnicate", "(ILcom/jitlogic/zorka/spy/CallInfo;)Ljava/lang/String;", null));
    }

    @Test @Ignore
    public void testClassMatchWithNoArgsMarker() {
//        SpyMatcherSet sms = new SpyMatcherSet(spy.byMethod(0xFF, "test.*", "frobnicate", null, SM_NOARGS));
//        Assert.assertTrue(sms.methodMatch("test.someClass", null, null, 1, "frobnicate", "()V", null));
//        Assert.assertFalse(sms.methodMatch("test.someClass", null, null, 1, "frobnicate", "(I)V", null));
    }

    @Test @Ignore
    public void testMatchWithMoreAttributesAndNoArgsFlag() {
//        SpyMatcherSet sms = new SpyMatcherSet(spy.byMethod(0xff, "test.*", "frobnicate", null, "int", SM_NOARGS));
//        Assert.assertTrue(sms.methodMatch("test.someClass", null, null, 1, "frobnicate", "(I)V", null));
//        Assert.assertFalse(sms.methodMatch("test.someClass", null, null, 1, "frobnicate", "(II)V", null));
    }

    @Test @Ignore
    public void testMatchWithJustSomeAttributes() {
//        SpyMatcherSet sms = new SpyMatcherSet(spy.byMethod(0xFF, "test.*", "frobnicate", null, "int"));
//        Assert.assertTrue(sms.methodMatch("test.SomeClass", null, null, 1, "frobnicate", "(I)V", null));
//        Assert.assertTrue(sms.methodMatch("test.someClass", null, null, 1, "frobnicate", "(II)V", null));
    }

    @Test @Ignore
    public void testMatchOnlyNames() {
//        SpyMatcherSet sms = new SpyMatcherSet(spy.byMethod("test.someClass", "trivialMethod"));
//        Assert.assertTrue(sms.methodMatch("test.someClass", null, null, 1, "trivialMethod", "()V", null));
//        Assert.assertTrue(sms.methodMatch("test.someClass", null, null, 1, "trivialMethod", "(II)V", null));
    }

    @Test @Ignore
    public void testMatchAnnotationBits() {
//        SpyMatcherSet sms = new SpyMatcherSet(spy.byClassAnnotation("some.Annotation"));
//        Assert.assertTrue(sms.methodMatch(null, Arrays.asList("Lsome.Annotation;"), null, 1, "trivialMethod", "()V", null));
    }

    @Test @Ignore
    public void testMatchWithFilterExclusion() {
//        SpyMatcherSet sms = new SpyMatcherSet(
//                spy.byMethod("java**", "*").exclude(),
//                spy.byMethod("com.sun.**", "*").exclude(),
//                spy.byMethod("**", "*").forTrace());
//
//        Assert.assertTrue(sms.classMatch("org.apache.catalina.Valve"));
//        Assert.assertFalse(sms.classMatch("java.util.Properties"));
    }

    @Test @Ignore
    public void testMatchMethodsWithFilterExclusion() {
//        SpyMatcherSet sms = new SpyMatcherSet(
//                spy.byMethod("com.jitlogic.foo.SomeClass", "some*"),
//                spy.byClass("com.jitlogic.**").exclude(),
//                spy.byClass("**")
//        );
//
//        Assert.assertTrue(sms.methodMatch("com.jitlogic.foo.SomeClass", null, null, 1, "someMethod", "()V", null));
//        Assert.assertFalse(sms.methodMatch("com.jitlogic.foo.SomeClass", null, null, 1, "otherMethod", "()V", null));
    }

    @Test @Ignore
    public void testMatchInnerClass() {
//        SpyMatcherSet sms = new SpyMatcherSet(spy.byMethod("some.Class$1", "run"));
//        Assert.assertTrue(sms.methodMatch("some.Class$1", null, null, 1, "run", "()V", null));
    }

    @Test @Ignore
    public void testMatchFilterWithPriorities() {
//        SpyMatcherSet sms = new SpyMatcherSet(
//                spy.byClass("**").priority(1000),
//                spy.byClass("com.jitlogic.**").exclude(),
//                spy.byClass("com.jitlogic.TestClazz").priority(10)
//        );
//
//        assertTrue(sms.classMatch("java.lang.Integer"));
//        assertFalse(sms.classMatch("com.jitlogic.zorka.core.spy.SpyProcessor"));
//        assertTrue(sms.classMatch("com.jitlogic.TestClazz"));
    }

    @Test @Ignore
    public void testClassExclusionWithOnlySomeMethod() {
//        SpyMatcherSet sms = new SpyMatcherSet(
//                spy.byMethod("com.jitlogic.zorka.core.**", "mapRow").exclude(),
//                spy.byClass("**"));
//        assertTrue(sms.classMatch("com.jitlogic.zorka.core.AgentConfig"));
//        assertTrue(sms.classMatch(AgentConfig.class, false));
    }

    @Test @Ignore
    public void testClassExclusionWithAllMethodsInClassExcluded() {
//        SpyMatcherSet sms = new SpyMatcherSet(
//                spy.byClass("com.jitlogic.zorka.core.**").exclude(),
//                spy.byClass("**"));
//        assertFalse(sms.classMatch("com.jitlogic.zorka.core.AgentConfig"));
//        assertFalse(sms.classMatch(AgentConfig.class, false));
    }


    @Test @Ignore
    public void testSpyMatchClazzByName() {
//        SpyMatcherSet sms = new SpyMatcherSet(
//                spy.byClass("com.jitlogic.zorka.core.test.spy.support.Test*"));
//        assertTrue(sms.classMatch(TestClass1.class, true));
//        assertFalse(sms.classMatch(Integer.class, true));
    }


    @Test @Ignore
    public void testSpyMatchClazzByInterface() {
//        SpyMatcherSet sms = new SpyMatcherSet(
//                spy.byInterfaceAndMethod("com.jitlogic.zorka.core.test.spy.support.TestInterface1", "*"));
//        assertFalse(sms.classMatch(TestClass1.class, true));
//        assertTrue(sms.classMatch(TestClass2.class, true));
//        assertFalse(sms.classMatch(TestClass3.class, true));
//        assertTrue(sms.classMatch(TestClass4.class, true));
    }


    @Test @Ignore
    public void testSpyMatchClazzBySuperInterface() {
//        SpyMatcherSet sms = new SpyMatcherSet(
//                spy.byInterface("com.jitlogic.zorka.core.test.spy.support.TestInterface2"));
//        assertFalse(sms.classMatch(TestClass1.class, true));
//        assertTrue(sms.classMatch(TestClass2.class, true));
//        assertFalse(sms.classMatch(TestClass3.class, true));
//        assertTrue(sms.classMatch(TestClass4.class, true));
//        assertTrue(sms.classMatch(TestClass5.class, true));
    }


    @Test
    public void testInstanceOfNonMatchingInterfaceCausesNPE() {
        assertFalse(ZorkaUtil.instanceOf(TestInterface1.class, "java.lang.Runnable"));
    }


    @Test @Ignore
    public void testSpyMatchClazzByClassAnnotation() {
//        SpyMatcherSet sms = new SpyMatcherSet(
//                spy.byClassAnnotation("com.jitlogic.zorka.core.test.spy.support.ClassAnnotation"));
//        assertTrue(sms.classMatch(TestClass1.class, true));
//        assertFalse(sms.classMatch(TestClass2.class, true));
    }


    @Test @Ignore
    public void testSpyMatchClazzByMethodAnnotation() {
//        SpyMatcherSet sms = new SpyMatcherSet(
//                spy.byMethodAnnotation("**", "com.jitlogic.zorka.core.test.spy.support.TestAnnotation"));
//        assertFalse(sms.classMatch(TestClass1.class, true));
//        assertTrue(sms.classMatch(TestClass2.class, true));
    }

    @Test @Ignore
    public void testSpyMatchClazzByMethodSignatureWithBasicTypes() {
//        SpyMatcherSet sms = new SpyMatcherSet(
//                spy.byMethod(0, "**", "paramMethod*", "void", "boolean"));
//        assertTrue(sms.classMatch(TestClass1.class, true));
//        assertFalse(sms.classMatch(TestClass2.class, true));
    }

    @Test @Ignore
    public void testSpyMatchClassByMethodSignatureWithClassTypes() {
//        SpyMatcherSet sms = new SpyMatcherSet(
//                spy.byMethod(0, "**", "*", "String", "String", SpyLib.SM_NOARGS));
//        assertFalse(sms.classMatch(TestClass1.class, true));
//        assertTrue(sms.classMatch(TestClass2.class, true));
    }

}

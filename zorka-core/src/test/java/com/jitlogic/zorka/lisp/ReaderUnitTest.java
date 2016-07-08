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

import com.jitlogic.zorka.lisp.support.LispTestSupport;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ReaderUnitTest extends LispTestSupport {

    @Before
    public void setUpInterpreter() {
        // We disable interpreter setup in this particular test class
    }

    @Test
    public void testReadBooleans() {
        assertEquals(true, read("#t"));
        assertEquals(false, read("#f"));
        assertEquals(true, read("true"));
        assertEquals(false, read("false"));
    }

    @Test
    public void testReadNumbers() {
        assertEquals(1, read("1"));
        assertEquals(23L, read("23L"));
        assertEquals(45.5, read("45.5"));
        assertEquals(-1, read("-1"));
        assertEquals(-23L, read("-23L"));
        assertEquals(-45.5, read("-45.5"));

        assertEquals(42, read("#b101010"));
        assertEquals(42L, read("#b101010L"));
        assertEquals(532, read("#o1024"));
        assertEquals(532L, read("#o1024L"));
        assertEquals(1024, read("#d1024"));
        assertEquals(1024L, read("#d1024L"));
        assertEquals(65535, read("#xffff"));
        assertEquals(65535L, read("#xffffL"));
    }

    /**
     * R5RS/6.3.4, pg. 28
     */
    @Test
    public void testReadCharacters() {
        assertEquals('a', read("#\\a"));
        assertEquals('A', read("#\\A"));
        assertEquals('(', read("#\\("));
        assertEquals(')', read("#\\)"));
        assertEquals(' ', read("#\\"));
        assertEquals(' ', read("#\\space"));
        assertEquals('\n', read("#\\newline"));
    }

    @Test
    public void testReadStrings() throws Exception {
        assertEquals("ab\ncd", read("\"ab\ncd\""));
        assertEquals("ab\ncd", read("\"ab\\ncd\""));
        assertEquals("ab;cd\nef", read("\"ab;cd\nef\""));
    }


    @Test
    public void testReadSymbols() {
        assertEquals(sym("abcd"), read("abcd"));
        assertEquals(sym("ab"), read("ab cd"));
        assertEquals(sym("!$%&*+-./:<=>?@^_~"), read("!$%&*+-./:<=>?@^_~"));

        Object o = read("ab/cd");
        assertTrue(o instanceof Symbol);

        Symbol s = (Symbol)o;
        assertEquals("ab", s.getNs());
        assertEquals("cd", s.getName());
    }

    @Test
    public void testReadKeywords() {
        assertEquals(kw("a"), read(":a"));
        assertEquals(kw("a/b"), read(":a/b"));
    }


    @Test
    public void testReadSpecialLiterals() {
        assertEquals(null, read("nil"));
        assertEquals(Boolean.TRUE, read("true"));
        assertEquals(Boolean.FALSE, read("false"));
    }


    @Test
    public void testReadSimpleLists() {
        assertEquals(lst(), read("()"));
        assertEquals(lst(), read("( )"));

        assertEquals(lst(sym("+"), 1, 2, -3), read("(+ 1 2 -3)"));
        assertEquals(lst(sym("sum"), 1, 2, 3), read("(sum ;see below\n1 2\n3)"));

        assertEquals(lst('x'), read("(#\\x)"));

        assertEquals(lst(1, 2, 3), read(" ( 1, 2, 3 ) "));
    }

    @Test
    public void testReadVectors() {
        assertEquals(vec(), read("[]"));
        assertEquals(vec(), read("[ ]"));
        assertEquals(vec(1,2), read("[1 2]"));
        assertEquals(vec(1,2,3), read("[ 1, 2, 3 ]"));
    }


    @Test
    public void testReadMaps() {
        assertEquals(LispSMap.EMPTY, read("{}"));
        assertEquals(LispSMap.EMPTY, read("{ }"));
        assertEquals(Utils.lispMap(1, 2, 3, 4), read("{1 2, 3 4}"));
    }


    @Test
    public void testReadFormWithComments() {
        assertEquals(lst(1, 2), read("(1 ;car\n 2 ;second\n)"));
    }


    @Test
    public void testReadTopLevelLists() {
        assertEquals(lst(sym("ab"), sym("cd")), readAll("ab cd"));
        assertEquals(lst(sym("ab"), sym("cd")), readAll("ab\ncd"));
    }


    @Test
    public void testReadCompositeLists() {
        assertEquals(null, read("()"));
        assertEquals(lst(lst(), lst()), read("(()())"));
        assertEquals(lst(lst(),lst()),   read(" ( ( ) ( ) )"));
        assertEquals(lst(lst(1), lst(2)), read(" ( ( 1 ) ( 2 ) ) "));
    }


    @Test
    public void testReadWhiteSpacesAndComments() throws Exception {
        assertEquals(lst(sym("ab"), sym("cd")), readAll("ab;aj waj\ncd"));
        assertEquals(lst(12, 34), readAll("12;ojoj\n34"));
    }

    @Test
    public void testReadQuote() throws Exception {
        assertEquals(lst(sym("quote"), sym("abc")), read("'abc"));
        assertEquals(lst(sym("quote"), lst(sym("a"),sym("b"),sym("c"))), read("'(a b c)"));
        assertEquals(lst(sym("quasiquote"), lst(1,2,3)), read("`(1 2 3)"));
        assertEquals(lst(sym("unquote"), lst(1,2,3)), read("~(1 2 3)"));
        assertEquals(lst(sym("unquote-splicing"), lst(1,2,3)), read("~@(1 2 3)"));
    }

    @Test
    public void testReadPair() throws Exception {
        assertEquals(pair(1,2), read("(1 . 2)"));
        assertEquals(pair(sym("name"),pair(sym("args"),sym("body"))), read("(name args . body)"));
    }

    @Test(expected=ReaderException.class)
    public void testErrorNonMatchingMarks() throws Exception {
        read("( 1 2 3");
    }


    @Test(expected=ReaderException.class)
    public void testNonFinishedString() throws Exception {
        read("( \"ala ma )");
    }

}

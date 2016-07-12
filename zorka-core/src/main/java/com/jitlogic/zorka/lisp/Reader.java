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

import java.io.*;
import java.util.regex.Pattern;

/**
 * LISP reader implementation.
 *
 * @author RLE <rafal.lewczuk@jitlogic.com>
 */
public class Reader {

    /** File name, classpath name or some other description */
    private String source;

    private PushbackReader pr = null;

    private int line = 0, col = 0, pcol = 0;
    private int rl = 0, rc = 0;

    public final static Object EOF = new Object();

    public final static Object L_BEGIN = new Object();
    public final static Object L_END = new Object();

    public final static Object DOT = new Object();
    public final static Object QUOTE = new Object();
    public final static Object QUASIQUOTE = new Object();
    public final static Object UNQUOTE = new Object();
    public final static Object UNQUOTE_SPLICING = new Object();

    public final static Object V_BEGIN = new Object();
    public final static Object V_END = new Object();

    public final static Object M_BEGIN = new Object();
    public final static Object M_END = new Object();


    /**
     * Returns one character from input stream.
     * @return a character
     */
    private int getch() throws LispException {
        try {
            int ch = pr.read();
            if (ch == '\n') {
                line++; pcol = col; col = 1;
            } else
                col++;

            return ch;
        } catch (IOException e) {
            throw new LispException("("+line+","+col+"): IO error while reading input.", e);
        }
    }


    /**
     * Pushes a character back into input stream, so it may be retrieved
     * later via getch().
     *
     * @param ch character to be pushed back
     */
    private void ungetch(int ch) throws LispException {
        try {
            pr.unread(ch);
            if (ch == '\n') {
                line--; col = pcol;
            } else
                col--;
        } catch (IOException e) {
            throw new LispException("("+line+","+col+"): IO error while reading input.", e);
        }
    }


    public static int escapeChar(int ch) {
        switch (ch) {
            case 'n':
                return '\n';
            case 't':
                return '\t';
            case 'r':
                return '\r';
            default:
                return ch;
        }
    }

    public Reader(InputStream is) {
        if (is == null) {
            throw new ReaderException(0, 0, "Null stream passed to reader.");
        }
        pr = new PushbackReader(new InputStreamReader(is));
    }

    public Reader(String source, InputStream is) {
        this(is);
        this.source = source;
    }

    public Reader(String s) {
        if (s == null) {
            throw new ReaderException(0, 0, "Null string passed to reader.");
        }
        pr = new PushbackReader(new StringReader(s));
    }

    public Reader(String source, String s) {
        this(s);
        this.source = source;
    }

    private Pair pair(int line, int col, Object first, Object rest) {
        return new CodePair(source, line, col, first, rest);
    }

    /**
     * Reads one LISP form.
     */
    public Object read() {
        Object t = readToken();

        if (QUOTE.equals(t)) {
            return pair(rl, rc, Symbol.symbol("quote"), pair(rl, rc, read(), null));
        } else if (QUASIQUOTE.equals(t)) {
            return pair(rl, rc, Symbol.symbol("quasiquote"), pair(rl, rc, read(), null));
        } else if (UNQUOTE.equals(t)) {
            return pair(rl, rc, Symbol.symbol("unquote"), pair(rl, rc, read(), null));
        } else if (UNQUOTE_SPLICING.equals(t)) {
            return pair(rl, rc, Symbol.symbol("unquote-splicing"), pair(rl, rc, read(), null));
        } else if (L_BEGIN.equals(t)) {
            Pair head = new Pair(), tail = head;
            int l = rl, c = rc;
            for (Object o = read(); o != L_END; o = read()) {
                if (o == EOF) {
                    throw new ReaderException(line, col, "Unexpected end of stream.");
                }
                if (o == DOT) {
                    Object p1 = read(), p2 = read();
                    if (head == tail || p1 == L_END || p2 != L_END) {
                        throw new ReaderException(line, col, "Badly formatted pair form.");
                    }
                    tail.setRest(p1);
                    break;
                } else {
                    tail.setRest(pair(l,c,o,null));
                }
                tail = (Pair) tail.rest();
                l = rl; c = rc;
            } // for ( .. )
            return head.rest();
        } else if (V_BEGIN.equals(t)) {
            LispVector v = new LispVector(Associative.MUTABLE);
            for (Object o = read(); o != V_END; o = read()) {
                if (o == EOF) {
                    throw new ReaderException(line, col, "Unexpected end of stream.");
                }
                v = v.append(o);
            }
            v.setFlags(0);
            return v;
        } else if (M_BEGIN.equals(t)) {
            LispMap m = new LispSMap(Associative.MUTABLE);
            Object k = null;
            for (Object o = read(); o != M_END; o = read()) {
                if (o == EOF) {
                    throw new ReaderException(line, col, "Unexpected end of stream.");
                }
                if (k == null) {
                    k = o;
                } else {
                    m = m.assoc(k, o);
                    k = null;
                }
            }
            if (k != null) {
                throw new ReaderException(line, col, "Uneven number of elements in declared map.");
            }
            m.setFlags(0);
            return m;
        }

        return t;
    }


    public Seq readAll() {
        Pair head = null;
        int l = rl, c = rc;
        for (Object o = read(); o != EOF; o = read()) {
            if (L_END.equals(o)) {
                throw new ReaderException(line, col, "Unexpected ')' token.");
            }
            head = pair(l, c, o, head);
        }
        return Utils.lstReverse(head);
    }


    private Object readToken() {
        int ch = getch();

        // Skip whitespaces
        while (Character.isWhitespace(ch) || ch == ',') ch = getch();

        rl = line; rc = col;

        switch (ch) {
            case -1:
                return EOF;
            case '(':
                return L_BEGIN;
            case ')':
                return L_END;
            case '[':
                return V_BEGIN;
            case ']':
                return V_END;
            case '{':
                return M_BEGIN;
            case '}':
                return M_END;
            case '\'':
                return QUOTE;
            case '`':
                return QUASIQUOTE;
            case '~': {
                int pc = getch();
                if (pc == '@') {
                    return UNQUOTE_SPLICING;
                } else {
                    if (pc != -1) { ungetch(pc); }
                    return UNQUOTE;
                }
            }
            case ';':
                while (ch != -1 && ch != '\n') ch = getch();
                return readToken();
            case '"': {
                StringBuilder sb = new StringBuilder();
                while ((ch = getch()) != '"') {
                    if (ch == '\\') {
                        int c1 = getch();
                        if (c1 == -1) {
                            throw new ReaderException(line, col, "Unexpected end of string.");
                        }
                        sb.append((char) escapeChar(c1));
                    } else if (ch == -1) {
                        throw new ReaderException(line, col, "Unexpected end of string.");
                    } else {
                        sb.append((char)ch);
                    }
                }
                return sb.toString();
            }
            case '#':
                return readSpecial();
            default: {
                StringBuilder sb = new StringBuilder();
                do {
                    sb.append((char)ch);
                    ch = getch();
                } while (!Character.isWhitespace(ch) && ch != -1 && ch != '(' && ch != ')' && ch != '"'
                    && ch != ';' && ch != '[' && ch != ']' && ch != '{' && ch != '}' && ch != ',');
                if (ch != -1) {
                    ungetch(ch);
                }
                return dispatchToken(sb.toString());
            }
        }
    }

    private Object readSpecial() {
        int ch = getch();
        switch (ch) {
            case 'b':
                return readIntegerLiteral(2);
            case 'd':
                return readIntegerLiteral(10);
            case 'f':
                return false;
            case 'o':
                return readIntegerLiteral(8);
            case 't':
                return true;
            case 'x':
                return readIntegerLiteral(16);
            case '\\':
                return readCharacterLiteral();
            // TODO vectors here
            default:
                throw new ReaderException(line, col, "Illegal reader macro");
        }
    }

    private Object readCharacterLiteral() {
        int cc = getch();
        if (cc == -1 || Character.isWhitespace(cc)) {
            return ' ';
        }
        if (!Character.isLetterOrDigit(cc)) {
            return (char)cc;
        }
        StringBuilder sb = new StringBuilder(16);
        sb.append((char)cc);
        for (cc = getch(); Character.isLetterOrDigit(cc); cc = getch()) {
            sb.append((char)cc);
        }
        if (cc != -1) {
            ungetch(cc);
        }
        if (sb.length() == 1) {
            return sb.charAt(0);
        }
        String s = sb.toString();
        if ("space".equals(s)) {
            return ' ';
        } else if ("newline".equals(s)) {
            return '\n';
        }
        throw new ReaderException(line, col, "Illegal character constant: " + s);
    }

    private Object readIntegerLiteral(int radix) {
        StringBuilder sb = new StringBuilder();

        int ch;

        for (ch = getch(); ch != -1 && Character.isLetterOrDigit(ch); ch = getch()) {
            sb.append((char)ch);
        }

        if (ch != -1) {
            ungetch(ch);
        }

        String s = sb.toString();

        if (s.length() == 0) {
            throw new ReaderException(line, col, "Illegal numeric constant: " + s);
        }

        if ('L' == s.charAt(s.length()-1)) {
            return Long.parseLong(s.substring(0, s.length()-1), radix);
        } else {
            return Integer.parseInt(s, radix);
        }
    }

    // characters to be used as symbols: a-zA-Z0-9 + - . * / < = > ! ? : $ % _ & ~ ^

    private static final Pattern RE_INTEGER = Pattern.compile("-?\\d+");
    private static final Pattern RE_LONG = Pattern.compile("-?\\d+L");
    private static final Pattern RE_DOUBLE = Pattern.compile("-?\\d+\\.(\\d+)?");
    private static final Pattern RE_SYM_KW = Pattern.compile("[\\w\\+\\-\\.\\*/<=>!\\?:$%_&~\\^@]+"); // R5RS/2.1, pg. 17

    private Object dispatchToken(String s) {
        if ("nil".equals(s) || "null".equals(s)) {
            return null;
        } else if ("true".equalsIgnoreCase(s)) {
            return true;
        } else if ("false".equalsIgnoreCase(s)) {
            return false;
        } else if (RE_INTEGER.matcher(s).matches()) {
            return Integer.parseInt(s);
        } else if (RE_LONG.matcher(s).matches()) {
            return Long.parseLong(s.substring(0, s.length()-1));
        } else if (RE_DOUBLE.matcher(s).matches()) {
            return Double.parseDouble(s);
        } else if (".".equals(s)) {
            return DOT;
        } else if (RE_SYM_KW.matcher(s).matches()) {
            return s.startsWith(":") ? Keyword.keyword(s.substring(1)) : Symbol.symbol(s);
        }

        throw new ReaderException(line, col, "Illegal token: '" + s + "'");
    }

}

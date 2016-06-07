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

import java.io.PrintStream;
import java.io.PrintWriter;

import static com.jitlogic.zorka.lisp.StandardLibrary.car;
import static com.jitlogic.zorka.lisp.StandardLibrary.cdr;
import static com.jitlogic.zorka.lisp.StandardLibrary.length;

public class LispException extends RuntimeException {

    private Seq evalStack, rewrittenStack;

    public LispException() {

    }

    public LispException(String msg) {
        super(msg);
    }

    public LispException(String msg, Throwable e) {
        super(msg, e);
    }

    public void addEvalItem(Object item) {
        if (item instanceof Pair) {
            evalStack = new Pair(item, evalStack);
        }
    }

    private synchronized void rewriteStackTrace() {
        if (evalStack != null && evalStack != rewrittenStack) {
            StackTraceElement[] stack = new StackTraceElement[length(evalStack)];
            Seq seq = evalStack;
            for (int i = stack.length-1; i >= 0; i--) {
                Object obj = car(seq);
                String cpoint = obj instanceof CodePair ?
                    " [" + ((CodePair) obj).getLine() + ":" + ((CodePair) obj).getCol() + "]" : "[?]";
                String fname = obj instanceof CodePair ? ((CodePair) obj).getSource() : null;
                int line = obj instanceof CodePair ? ((CodePair) obj).getLine() : 0;
                stack[i] = new StackTraceElement(obj.toString(), cpoint, fname, line);
                seq = (Seq)cdr(seq);
            }
            rewrittenStack = evalStack;
            setStackTrace(stack);
        }
    }

    public void printStackTrace(PrintStream s) {
        rewriteStackTrace();
        super.printStackTrace(s);
    }

    public void printStackTrace(PrintWriter s) {
        rewriteStackTrace();
        super.printStackTrace(s);
    }
}


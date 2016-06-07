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
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.jitlogic.zorka.lisp.StandardLibrary.*;
import static com.jitlogic.zorka.lisp.StandardLibrary.car;

public class LispTestSuite extends ParentRunner<String> {

    private final static Symbol DEFTEST = Symbol.symbol("deftest");

    private LispTests tests;

    public LispTestSuite(Class<?> testClass) throws InitializationError {
        super(testClass);
        tests = testClass.getAnnotation(LispTests.class);
        if (tests == null) {
            throw new InitializationError("Cannot obtain LispTests annotation.");
        }
    }

    @Override
    protected List<String> getChildren() {
        List<String> lst = new ArrayList<>(tests.scripts().length);
        Collections.addAll(lst, tests.scripts());
        return lst;
    }

    @Override
    protected Description describeChild(String child) {
        return Description.createSuiteDescription(child);
    }

    @Override
    protected void runChild(String child, RunNotifier notifier) {
        String name = tests.path() + "/" + child + ".zcm";
        InputStream is = getClass().getResourceAsStream(name);
        Seq testForms;
        try {
            testForms = new Reader(child, is).readAll();
        } catch (Exception e) {
            notifier.fireTestFailure(new Failure(Description.createTestDescription(child, ""),
                new AssertionError("Script not found:: " + name, e)));
            return;
        }
        Interpreter ctx = new Interpreter();
        ctx.install(new StandardLibrary(ctx));
        ctx.install(new LispTestLibrary(ctx));
        ctx.evalScript("/com/jitlogic/zorka/lisp/boot.zcm");
        for (Seq seq = testForms; seq != null; seq = (Seq)cdr(seq)) {
            if (DEFTEST.equals(caar(seq))) {
                Object testNameObj = cadar(seq);
                if (!(testNameObj instanceof Symbol)) {
                    notifier.fireTestFailure(
                        new Failure(Description.createTestDescription(child,
                            "<bad test @ " + ((CodePair)car(seq)).getLine() + ":" + ((CodePair)car(seq)).getCol()+ ">"),
                            new RuntimeException("Missing or invalid test name: " + testNameObj)));
                    continue;
                }
                Symbol testName = (Symbol)testNameObj;
                Description spec = Description.createTestDescription(child, testName.getName());
                notifier.fireTestStarted(spec);
                try {
                    for (Seq body = (Seq) cddar(seq); body != null; body = (Seq) cdr(body)) {
                        ctx.eval(car(body));
                    }
                    notifier.fireTestFinished(spec);
                } catch (Throwable e) {
                    notifier.fireTestFailure(new Failure(spec,
                        e.getCause() instanceof AssertionError ? e.getCause() : e));
                }
            } else {
                ctx.eval(car(seq));
            }
        }

    }
}


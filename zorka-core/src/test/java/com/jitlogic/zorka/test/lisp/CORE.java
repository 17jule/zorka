/**
 * Copyright 2012-2016 Rafal Lewczuk <rafal.lewczuk@jitlogic.com>
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

package com.jitlogic.zorka.test.lisp;

import com.jitlogic.zorka.test.lisp.support.LispTestSuite;
import com.jitlogic.zorka.test.lisp.support.LispTests;
import org.junit.runner.RunWith;

@RunWith(LispTestSuite.class)
@LispTests(path = "/test/lisp/CORE",

    scripts = {
        "EvalBasics",
        "StandardLibrary",
        "Destructuring",
        "Reflection"
    })
public class CORE {
}

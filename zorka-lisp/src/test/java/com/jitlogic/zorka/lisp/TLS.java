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

package com.jitlogic.zorka.lisp;

import com.jitlogic.zorka.lisp.support.LispTestSuite;
import com.jitlogic.zorka.lisp.support.LispTests;
import org.junit.runner.RunWith;

@RunWith(LispTestSuite.class)
@LispTests(path = "/test/lisp/TLS",

    scripts = {

        // The Little Schemer book
        "01_Toys",
        "02_DoItAgain",
        "03_ConsTheMagnificent",
        "04_NumbersGames",
        "05_ItsFullOfStars",
        "06_Shadows",
        "07_FriendsAndRelations",
        "08_LambdaTheUltimate",
        "09_AgainAndAgain",
        "10_ValueOfAllOfThis",

        // The Seasoned Schemer book
        "11_WelcomeBack",
        "12_TakeCover",
        "13_HopSkipJump",
        "14_LetThereBeNames",
        "15_TheDifference",
        "16_SetReadyBang",
        "17_WeChange",
        "18_WeChange",
        "19_AbscondingWithThe",
        "20_WhatsInStore"
    })
public class TLS {
}

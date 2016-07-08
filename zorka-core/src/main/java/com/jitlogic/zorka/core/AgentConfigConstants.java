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

package com.jitlogic.zorka.core;

import com.jitlogic.zorka.lisp.Keyword;

public class AgentConfigConstants {
    public static final Keyword KW_DIAGNOSTICS = Keyword.keyword("diagnostics");
    public static final Keyword KW_COMPUTE_FRAMES = Keyword.keyword("compute-frames");
    public static final Keyword KW_ENABLED = Keyword.keyword("enabled");
    public static final Keyword KW_EXECUTOR = Keyword.keyword("executor");
    public static final Keyword KW_HOSTNAME = Keyword.keyword("hostname");
    public static final Keyword KW_MBEAN = Keyword.keyword("mbean");
    public static final Keyword KW_QUEUE = Keyword.keyword("queue");
    public static final Keyword KW_SCRIPTS = Keyword.keyword("scripts");
    public static final Keyword KW_SCRIPTS_DIR = Keyword.keyword("scripts-dir");
    public static final Keyword KW_SPY = Keyword.keyword("spy");
    public static final Keyword KW_THREADS = Keyword.keyword("threads");
    public static final Keyword KW_TIMEOUT = Keyword.keyword("timeout");
    public static final Keyword KW_VERSION = Keyword.keyword("version");
    public static final Keyword KW_ZORKA = Keyword.keyword("zorka");
}

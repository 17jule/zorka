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
package com.jitlogic.zorka;


import com.jitlogic.zorka.lisp.Keyword;
import com.jitlogic.zorka.lisp.LispMap;
import com.jitlogic.zorka.lisp.Seq;
import com.jitlogic.zorka.lisp.StandardLibrary;
import com.jitlogic.zorka.util.ZorkaLog;
import com.jitlogic.zorka.util.ZorkaLogger;
import com.jitlogic.zorka.util.ZorkaUtil;

import java.io.*;

public class AgentConfig {

    private static final ZorkaLog log = ZorkaLogger.getLog(AgentConfig.class);

    public final static String DEFAULT_CONF_PATH = "/com/jitlogic/zorka/core/zorka.conf";

    private LispMap confData;

    public AgentConfig(String home) {
        this.homeDir = home;
        reload();
    }


    /** Home directory */
    protected String homeDir;


    public static final String PROP_HOME_DIR = "zorka.home.dir";


    public String getHomeDir() {
        return homeDir;
    }


    public void reload() {
        InputStream is = this.getClass().getResourceAsStream(DEFAULT_CONF_PATH);
        confData = (LispMap)StandardLibrary.read(is);
        confData = confData.assoc(AgentConfigConstants.KW_HOME_DIR, homeDir);
        confData = confData.assoc(AgentConfigConstants.KW_SCRIPTS_DIR, ZorkaUtil.path(homeDir, "scripts"));
        try {
            is.close();
        } catch (IOException e) {
            log.error(ZorkaLogger.ZAG_ERRORS, "Cannot close input stream.", e);
        }
        String fname = ZorkaUtil.path(homeDir, "zorka.conf");
        File f = new File(fname);
        if (f.canRead()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(fname);
                Object cfgObj = StandardLibrary.read(fis);
                if (cfgObj instanceof LispMap) {
                    confData = StandardLibrary.mergeRecursive(confData, (LispMap)cfgObj);
                }
            } catch (FileNotFoundException e) {
                log.error(ZorkaLogger.ZAG_ERRORS, "Cannot open config file: " + fname, e);
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        log.error(ZorkaLogger.ZAG_ERRORS, "Cannot close input stream.", e);
                    }
                }
            }
        }
    }


    public Object get(Object notFound, Keyword...path) {
        Object obj = confData;
        for (Keyword k : path) {
            if (obj instanceof LispMap) {
                obj = ((LispMap)obj).get(k);
            } else {
                return notFound;
            }
        }
        return obj;
    }


    public String strVal(String notFound, Keyword...path) {
        Object rslt = get(notFound, path);
        return rslt instanceof String ? (String)rslt : null;
    }


    public Integer intVal(Integer notFound, Keyword...path) {
        Object rslt = get(notFound, path);
        return rslt instanceof Integer ? (Integer)rslt : null;
    }


    public Boolean boolVal(Boolean notFound, Keyword...path) {
        Object rslt = get(notFound, path);
        return rslt instanceof Boolean ? (Boolean)rslt : null;
    }

    public Seq seqVal(Seq notFound, Keyword...path) {
        Object rslt = get(notFound, path);
        return rslt instanceof Seq ? (Seq)rslt : null;
    }

    public LispMap mapVal(LispMap notFound, Keyword...path) {
        Object rslt = get(notFound, path);
        return rslt instanceof LispMap ? (LispMap)rslt : null;
    }

}

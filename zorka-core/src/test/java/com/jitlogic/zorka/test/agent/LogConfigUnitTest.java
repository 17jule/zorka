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

package com.jitlogic.zorka.test.agent;

import com.jitlogic.zorka.test.support.ZorkaFixture;

import com.jitlogic.zorka.util.ZorkaLogger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import  static org.junit.Assert.*;

public class LogConfigUnitTest extends ZorkaFixture {

    @Before
    public void setTestProps() {
//        config.getProperties().setProperty("test.int", " 10 ");
//        config.getProperties().setProperty("test.broken", "oja!");
//        config.getProperties().setProperty("test.space", "  ");
//        config.getProperties().setProperty("test.lst", "  oja!,  oje! ");
//        config.getProperties().setProperty("ten.bytes", "10");
//        config.getProperties().setProperty("one.kilobyte", "1k");
//        config.getProperties().setProperty("ten.megabytes", "10M");
    }


    @Test
    public void testParseSimpleLogConfigStrings() {
        assertEquals(ZorkaLogger.ZTR_CONFIG, ZorkaLogger.parse("", "ZTR", "CONFIG"));
        assertEquals(ZorkaLogger.ZTR_CONFIG | ZorkaLogger.ZTR_TRACE_CALLS,
                ZorkaLogger.parse("", "ZTR", "CONFIG,TRACE_CALLS"));
        assertEquals(ZorkaLogger.ZTR_CONFIG | ZorkaLogger.ZTR_TRACE_CALLS,
                ZorkaLogger.parse("", "ZTR", "config, trace_calls"));
    }


    @Test @Ignore
    public void testParseIntPropsViaZorkaLib() {
//        assertEquals((Integer) 10, zorka.intCfg("test.int", null));
//        assertEquals((Integer)10, zorka.intCfg("test.int", 30));
//        assertEquals(null, zorka.intCfg("test.broken", null));
//        assertEquals((Integer)20, zorka.intCfg("test.broken", 20));
//        assertEquals(null, zorka.intCfg("test.missing", null));
    }


    @Test @Ignore
    public void testParseLongPropsViaZorkaLib() {
//        assertEquals((Long)10L, zorka.longCfg("test.int", null));
//        assertEquals((Long)10L, zorka.longCfg("test.int", 30L));
//        assertEquals(null, zorka.longCfg("test.broken", null));
//        assertEquals((Long)20L, zorka.longCfg("test.broken", 20L));
//        assertEquals(null, zorka.longCfg("test.missing", null));
    }


    @Test @Ignore
    public void testParseStringPropsViaZorkaLib() {
//        assertEquals(null, zorka.stringCfg("test.missing", null));
//        assertEquals("", zorka.stringCfg("test.space", null));
//        assertEquals("oja!", zorka.stringCfg("test.missing", "oja!"));
    }


    @Test @Ignore
    public void testParseListPropsViaZorkaLib() {
//        assertEquals(new ArrayList<String>(), zorka.listCfg("test.missing"));
//        assertEquals(new ArrayList<String>(), zorka.listCfg("test.space"));
//        assertEquals(Arrays.asList("oja!"), zorka.listCfg("test.broken"));
//        assertEquals(Arrays.asList("oja!", "oje!"), zorka.listCfg("test.lst"));
    }

    @Test @Ignore
    public void testParseKiloProps() {
//        assertEquals((Long)10L, zorka.kiloCfg("ten.bytes", null));
//        assertEquals((Long)1024L, zorka.kiloCfg("one.kilobyte", null));
//        assertEquals((Long)10485760L, zorka.kiloCfg("ten.megabytes", null));
    }
}

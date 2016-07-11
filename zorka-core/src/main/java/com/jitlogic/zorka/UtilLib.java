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


import com.jitlogic.zorka.util.JSONWriter;
import com.jitlogic.zorka.util.ObjectInspector;
import com.jitlogic.zorka.util.StringMatcher;
import com.jitlogic.zorka.util.TapInputStream;
import com.jitlogic.zorka.util.TapOutputStream;
import com.jitlogic.zorka.util.ZorkaUtil;
import com.jitlogic.zorka.util.Base64;
import com.jitlogic.zorka.lisp.Namespace;
import com.jitlogic.zorka.lisp.Primitive;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Namespace("util")
public class UtilLib {

    /**
     * Recursively accesses object. This is just a ObjectInspector.get() method facade for configuration scripts.
     *
     * @param obj  source object
     * @param args attribute chain
     * @return retrieved value
     */
    @Primitive
    public Object get(Object obj, Object... args) {
        return ObjectInspector.get(obj, args);
    }

    @Primitive("java-list")
    public List<?> list(Object...objs) {
        List<Object> lst = new ArrayList<Object>();
        for (Object obj : objs) {
            lst.add(obj);
        }
        return lst;
    }

    @Primitive("java-map")
    public Map<?,?> map(Object...objs) {
        return ZorkaUtil.map(objs);
    }

    @Primitive("java-set")
    public Set<Object> set(Object... objs) {
        return ZorkaUtil.set(objs);
    }

    @Primitive("cast-string")
    public String castString(Object obj) {
        return ZorkaUtil.castString(obj);
    }

    @Primitive("crc32")
    public String crc32sum(String input) {
        return ZorkaUtil.crc32(input);
    }

    @Primitive("crc32l")
    public String crc32sum(String input, int limit) {
        String sum = ZorkaUtil.crc32(input);
        return sum.length() > limit ? sum.substring(0, limit) : sum;
    }

    @Primitive("md5")
    public String md5sum(String input) {
        return ZorkaUtil.md5(input);
    }

    @Primitive("md5l")
    public String md5sum(String input, int limit) {
        String sum = ZorkaUtil.md5(input);
        return sum.length() > limit ? sum.substring(0, limit) : sum;
    }

    @Primitive("sha1")
    public String sha1sum(String input) {
        return ZorkaUtil.sha1(input);
    }

    @Primitive("sha1l")
    public String sha1sum(String input, int limit) {
        String sum = ZorkaUtil.sha1(input);
        return sum.length() > limit ? sum.substring(0, limit) : sum;
    }

    @Primitive("str-time")
    public String strTime(long ns) {
        return ZorkaUtil.strTime(ns);
    }

    @Primitive("str-clock")
    public String strClock(long clock) {
        return ZorkaUtil.strClock(clock);
    }

    @Primitive("string-matcher")
    public StringMatcher stringMatcher(List<String> includes, List<String> excludes) {
        return new StringMatcher(includes, excludes);
    }

    @Primitive
    public String path(String... components) {
        return ZorkaUtil.path(components);
    }

    @Primitive("get-field")
    public Object getField(Object obj, String name) {
        return ObjectInspector.getField(obj, name);
    }

    @Primitive("set-field")
    public void setField(Object obj, String name, Object value) {
        ObjectInspector.setField(obj, name, value);
    }

    @Primitive("json")
    public String json(Object obj) {
        return new JSONWriter().write(obj);
    }

    @Primitive("re-pattern")
    public Pattern rePattern(String pattern) {
        return Pattern.compile(pattern);
    }

    @Primitive("re-match")
    public boolean reMatch(Pattern pattern, String s) {
        return pattern.matcher(s).matches();
    }

    private static final int BUF_SZ = 4096;

    @Primitive("io-copy")
    public int ioCopy(InputStream is, OutputStream os) throws IOException {
        int bufsz = Math.min(is.available(), BUF_SZ);
        final byte[] buf = new byte[bufsz];
        int n = 0, total = 0;
        n = is.read(buf);
        while (n > 0) {
            os.write(buf, 0, n);
            total += n;
            n = is.read(buf);
        }
        return total;
    }

    @Primitive
    public long min(long a, long b) {
        return Math.min(a, b);
    }

    @Primitive
    public long max(long a, long b) {
        return Math.max(a, b);
    }

    @Primitive("clip-array")
    public byte[] clipArray(byte[] src, int len) {
        return ZorkaUtil.clipArray(src, len);
    }

    @Primitive("base64")
    public String base64(byte[] buf) {
        return Base64.encode(buf, false);
    }

    @Primitive("tap-input-stream")
    public TapInputStream tapInputStream(InputStream is, long init, long limit) {
        return new TapInputStream(is, (int)init, (int)limit);
    }

    @Primitive("tap-output-stream")
    public TapOutputStream tapOutputStream(OutputStream os, long init, long limit) {
        return new TapOutputStream(os, (int)init, (int)limit);
    }
}

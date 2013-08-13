/**
 * Copyright 2012-2013 Rafal Lewczuk <rafal.lewczuk@jitlogic.com>
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
package com.jitlogic.zorka.central.roof;


import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Translates mapped HTTP requests into entity proxy method calls.
 */
public class RoofEntityProxyService implements RoofService {

    private RoofEntityProxy proxy;


    public RoofEntityProxyService(RoofEntityProxy proxy) {
        this.proxy = proxy;
    }

    public RoofEntityProxy getProxy() {
        return proxy;
    }

    private Object callProxyMethod(Method m, Object...args) {
        return null;
    }

    private Object getCollection(String id, String name) {
        for (Method m : proxy.getClass().getMethods()) {
            RoofCollection jc = m.getAnnotation(RoofCollection.class);
            if (jc != null && jc.value().equals(name)) {
                return callProxyMethod(m, name);
            }
        }
        return null;
    }

    @Override
    public Object GET(List<String> path, Map<String, String> params) {
        if (path.size() == 0) {
            return proxy.list(params);
        } else if (path.size() == 1) {
            return proxy.get(path, params);
        } else {
            if (path.get(1).equals("collections") && path.size() >= 3) {
                Object col = getCollection(path.get(0), path.get(2));
                if (col instanceof RoofEntityProxy) {
                    return new RoofEntityProxyService((RoofEntityProxy)col)
                        .GET(path.subList(2, path.size()), params);
                } else if (col instanceof RoofService) {
                    return ((RoofService)col).GET(path.subList(2, path.size()), params);
                } else {
                    throw new RoofException(405, "Collection not found.");
                }
            } else {
                throw new RoofException(501, "Not implemented (yet).");
            }
        }
    }


    @Override
    public Object PUT(List<String> path, Map<String, String> params, Object data) {
        throw new RoofException(501, "Not implemented (yet).");
    }


    @Override
    public Object DELETE(List<String> path, Map<String, String> params) {
        throw new RoofException(501, "Not implemented (yet).");
    }


    @Override
    public Object POST(List<String> path, Map<String, String> params, Object data) {
        throw new RoofException(501, "Not implemented (yet).");
    }
}

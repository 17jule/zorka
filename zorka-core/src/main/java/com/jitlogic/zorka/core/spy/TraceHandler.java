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

package com.jitlogic.zorka.core.spy;

public interface TraceHandler {

    void traceBegin(int traceId, long clock, int flags);

    Object getAttr(int attrId);

    Object getAttr(int traceId, int attrId);

    void newAttr(int traceId, int attrId, Object attrVal);

    void disable();

    void enable();

    void setMinimumTraceTime(long minimumTraceTime);

    void markTraceFlags(int traceId, int flag);

    boolean isInTrace(int traceId);
}

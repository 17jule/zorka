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
package com.jitlogic.zico.core;


import com.jitlogic.zico.core.model.HostInfo;
import com.jitlogic.zico.core.model.SymbolicExceptionInfo;
import com.jitlogic.zico.core.rds.RDSStore;
import com.jitlogic.zorka.common.tracedata.FressianTraceFormat;
import com.jitlogic.zorka.common.tracedata.MetadataChecker;
import com.jitlogic.zorka.common.tracedata.Symbol;
import com.jitlogic.zorka.common.tracedata.SymbolRegistry;
import com.jitlogic.zorka.common.tracedata.SymbolicException;
import com.jitlogic.zorka.common.tracedata.TraceMarker;
import com.jitlogic.zorka.common.tracedata.TraceRecord;
import com.jitlogic.zorka.common.util.ZorkaUtil;
import com.jitlogic.zorka.common.zico.ZicoDataProcessor;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.fressian.FressianWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ReceiverContext implements MetadataChecker, ZicoDataProcessor {

    private final static Logger log = LoggerFactory.getLogger(ReceiverContext.class);

    private SymbolRegistry symbolRegistry;
    private Map<Integer, Integer> sidMap = new HashMap<Integer, Integer>();

    private RDSStore traceDataStore;
    private TraceInfoStore traceInfoStore;
    private TraceTypeRegistry traceTypeRegistry;
    private TraceTableWriter traceTableWriter;

    private HostInfo hostInfo;

    private Set<Object> visitedObjects = new HashSet<Object>();

    // TODO move all data saving activity to HostStore, only symbolID translation should be performed here;

    public ReceiverContext(HostStore store, TraceTypeRegistry traceTypeRegistry, TraceTableWriter traceTableWriter) {
        this.symbolRegistry = store.getStoreManager().getSymbolRegistry();
        this.traceDataStore = store.getRdsData();
        this.hostInfo = store.getHostInfo();
        this.traceInfoStore = store.getTraceInfoStore();
        this.traceTypeRegistry = traceTypeRegistry;
        this.traceTableWriter = traceTableWriter;
    }


    @Override
    public synchronized void process(Object obj) throws IOException {
        try {
            if (obj instanceof Symbol) {
                processSymbol((Symbol) obj);
            } else if (obj instanceof TraceRecord) {
                log.debug("Processing trace record:" + obj);
                processTraceRecord((TraceRecord) obj);
            } else {
                if (obj != null) {
                    log.warn("Unsupported object type:" + obj.getClass());
                } else {
                    log.warn("Attempted processing NULL object (?)");
                }
            }
        } catch (Exception e) {
            log.error("Error processing trace record: ", e);
        }
    }


    private void processSymbol(Symbol sym) {
        int newid = symbolRegistry.symbolId(sym.getName());
        sidMap.put(sym.getId(), newid);
    }


    private void processTraceRecord(TraceRecord rec) throws IOException {
        if (!hostInfo.hasFlag(HostInfo.DISABLED)) {
            rec.traverse(this);
            byte[] dataChunk = serialize(rec);
            long dataOffs = traceDataStore.write(dataChunk);

            traceInfoStore.save(rec, dataOffs, dataChunk.length);

            save(hostInfo.getId(), dataOffs, dataChunk.length, 0L, 0L, rec);
        } else {
            log.debug("Dropping trace for inactive host: " + hostInfo.getName());
        }
    }


    private byte[] serialize(TraceRecord rec) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        FressianWriter writer = new FressianWriter(os, FressianTraceFormat.WRITE_LOOKUP);
        writer.writeObject(rec);
        return os.toByteArray();
    }


    public void save(int hostId, long dataOffs, int dataLength, long indexOffs, long indexLength, TraceRecord tr) {

        JSONObject attrMap = new JSONObject();

        if (tr.getAttrs() != null) {
            for (Map.Entry<Integer, Object> e : tr.getAttrs().entrySet()) {
                String val = e.getValue() != null ? e.getValue().toString() : "";
                attrMap.put(symbolRegistry.symbolName(e.getKey()), val);
            }
        }

        int status = 0;

        if (tr.getException() != null
                || tr.hasFlag(TraceRecord.EXCEPTION_PASS)
                || tr.getMarker().hasFlag(TraceMarker.ERROR_MARK)) {
            status = 1;
        }

        String attrJson = attrMap.toJSONString();

        String exJson = null;

        SymbolicException e = tr.findException();

        if (e != null) {
            SymbolicExceptionInfo sei = ZicoUtil.extractSymbolicExceptionInfo(symbolRegistry, e);
            JSONObject json = new JSONObject();
            json.put("exClass", sei.getExClass());
            json.put("message", sei.getMessage());
            JSONArray stack = new JSONArray();
            if (sei.getStackTrace() != null) {
                stack.addAll(sei.getStackTrace().size() > 8 ? sei.getStackTrace().subList(0, 8) : sei.getStackTrace());
            }
            json.put("stackTrace", stack);
            exJson = json.toJSONString();
        }

        traceTableWriter.submit(ZorkaUtil.<String, Object>map(
                "HOST_ID", hostId,
                "DATA_OFFS", dataOffs,
                "DATA_LEN", dataLength,
                "INDEX_OFFS", indexOffs,
                "INDEX_LEN", indexLength,
                "TRACE_ID", tr.getMarker().getTraceId(),
                "CLOCK", tr.getClock(),
                "RFLAGS", tr.getFlags(),
                "TFLAGS", tr.getMarker().getFlags(),
                "STATUS", status,
                "CLASS_ID", tr.getClassId(),
                "METHOD_ID", tr.getMethodId(),
                "SIGN_ID", tr.getSignatureId(),
                "CALLS", tr.getCalls(),
                "ERRORS", tr.getErrors(),
                "RECORDS", ZicoUtil.numRecords(tr),
                "EXTIME", tr.getTime(),
                "ATTRS", attrJson,
                "EXINFO", exJson
        ));
    }


    @Override
    public int checkSymbol(int symbolId, Object owner) throws IOException {
        if (owner instanceof TraceMarker) {
            if (visitedObjects.contains(owner)) {
                return symbolId;
            } else {
                visitedObjects.add(owner);
            }
            if (sidMap.containsKey(symbolId)) {
                traceTypeRegistry.mark(sidMap.get(symbolId), getHostInfo().getId());
            }
        }
        return sidMap.containsKey(symbolId) ? sidMap.get(symbolId) : 0;
    }


    @Override
    public void checkMetric(int metricId) throws IOException {
    }


    public HostInfo getHostInfo() {
        return hostInfo;
    }
}

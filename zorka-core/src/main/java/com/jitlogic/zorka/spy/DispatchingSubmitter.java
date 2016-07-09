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
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this software. If not, see <http://www.gnu.org/licenses/>.
 */

package com.jitlogic.zorka.spy;

import com.jitlogic.zorka.stats.AgentDiagnostics;
import com.jitlogic.zorka.util.ZorkaLogger;
import com.jitlogic.zorka.util.ZorkaLog;
import com.jitlogic.zorka.lisp.Keyword;
import com.jitlogic.zorka.lisp.LispMap;
import com.jitlogic.zorka.lisp.LispSMap;

import java.util.List;
import java.util.Stack;

import static com.jitlogic.zorka.spy.SpyLib.*;

/**
 * Dispatching submitter receives submissions from probes, groups them (if needed) and sends through proper processing
 * chains defined in associated sdefs.
 *
 * @author rafal.lewczuk@jitlogic.com
 */
public class DispatchingSubmitter implements SpySubmitter {

    /**
     * Logger
     */
    private ZorkaLog log = ZorkaLogger.getLog(this.getClass());

    /**
     * Spy class transformer
     */
    private SpyClassTransformer transformer;

    /**
     * Submission stack is used to associate results from method entry probes with results from return/error probes.
     */
    private ThreadLocal<Stack<LispMap>> submissionStack =
            new ThreadLocal<Stack<LispMap>>() {
                @Override
                public Stack<LispMap> initialValue() {
                    return new Stack<LispMap>();
                }
            };


    /**
     * Creates dispatching submitter.
     *
     * @param transformer class file transformer containing defined spy contexts
     *                    // TODO move spy context map out of class file transformer
     */
    public DispatchingSubmitter(SpyClassTransformer transformer) {
        this.transformer = transformer;
    }


    @Override
    public void submit(int stage, int id, int submitFlags, Object[] vals) {

        if (ZorkaLogger.isLogMask(ZorkaLogger.ZSP_SUBMIT)) {
            log.debug(ZorkaLogger.ZSP_SUBMIT, "Submitted: stage=" + stage + ", id=" + id + ", flags=" + submitFlags);
        }

        SpyContext ctx = transformer.getContext(id);

        if (ctx == null) {
            return;
        }

        LispMap record = getRecord(stage, ctx, submitFlags, vals);

        SpyDefinition sdef = ctx.getSpyDefinition();

        if (null == (record = process(stage, sdef, record))) {
            return;
        }

        if (submitFlags == SF_NONE) {
            submissionStack.get().push(record);
            return;
        }

        AgentDiagnostics.inc(AgentDiagnostics.SPY_SUBMISSIONS);

        if (sdef.getProcessors(ON_SUBMIT).size() > 0) {
            process(ON_SUBMIT, sdef, record);
        }

    }

    public static Keyword CTX    = Keyword.keyword("CTX");


    /**
     * Retrieves or creates spy record for probe submission purposes.
     *
     * @param stage       method bytecode point where probe has been installed (entry, return, error)
     * @param ctx         spy context associated with submitting probe
     * @param submitFlags controls whether SUBMIT chain should be immediately processed or record should be
     *                    stored in thread local stack (and wait for another probe submission)
     * @param vals        submitted values
     * @return spy record
     */
    private LispMap getRecord(int stage, SpyContext ctx, int submitFlags, Object[] vals) {

        LispMap record;

        switch (submitFlags) {
            case SF_IMMEDIATE:
            case SF_NONE:
                record = new LispSMap(LispMap.MUTABLE).assoc(CTX, ctx);
                break;
            case SF_FLUSH:
                Stack<LispMap> stack = submissionStack.get();
                if (stack.size() > 0) {
                    record = stack.pop();
                    // TODO check if record belongs to proper frame, warn if not
                } else {
                    log.error(ZorkaLogger.ZSP_ERRORS, "Submission thread local stack mismatch (ctx=" + ctx
                            + ", stage=" + stage + ", submitFlags=" + submitFlags + ")");
                    record = new LispSMap(LispMap.MUTABLE).assoc(CTX, ctx);
                }
                break;
            default:
                log.error(ZorkaLogger.ZSP_ERRORS, "Illegal submission flag: " + submitFlags + ". Creating empty records.");
                record = new LispSMap(LispMap.MUTABLE).assoc(CTX, ctx);
                break;
        }

        SpyContext context = ((SpyContext) record.get(CTX));
        List<SpyProbe> probes = context.getSpyDefinition().getProbes(stage);

        // TODO check if vals.length == probes.size() and log something here ...

        for (int i = 0; i < probes.size(); i++) {
            SpyProbe probe = probes.get(i);
            record = record.assoc(probe.getDstField(), vals[i]);
        }

        SpyLib.markStage(record, stage);
        SpyLib.setCurStage(record, stage);

        return record;
    }


    /**
     * Processes specified processing chain of sdef in record
     *
     * @param stage  chain ID
     * @param sdef   spy definition with configured processing chains
     * @param record spy record (input)
     * @return spy record (output) or null if record should not be further processed
     */
    private LispMap process(int stage, SpyDefinition sdef, LispMap record) {
        List<SpyProcessor> processors = sdef.getProcessors(stage);

        SpyLib.markStage(record, stage);
        SpyLib.setCurStage(record, stage);

        if (ZorkaLogger.isLogMask(ZorkaLogger.ZSP_ARGPROC)) {
            log.debug(ZorkaLogger.ZSP_ARGPROC, "Processing records (stage=" + stage + ")");
        }

        for (SpyProcessor processor : processors) {
            try {
                if (null == (record = processor.process(record))) {
                    break;
                }
            } catch (Throwable e) {
                // This has to catch everything, even OOM.
                log.error(ZorkaLogger.ZSP_ERRORS, "Error processing record %s (on processor %s, stage=%s)", e,
                        record, processor, stage);
            }
        }

        return record;
    }

}

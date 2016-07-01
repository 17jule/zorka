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
package com.jitlogic.zorka.core.spy;

import com.jitlogic.zorka.common.util.ZorkaUtil;
import com.jitlogic.zorka.lisp.Symbol;

import java.util.*;

import static com.jitlogic.zorka.core.spy.SpyLib.*;

/**
 * Spy definition contains complete set of information on how to instrument a specific
 * aspect of monitored application. Think of it as 'spy configuration' for some set of
 * methods you're interested in.
 * <p/>
 * Along with spy library functions it defines mini-DSL for configuring instrumentation.
 * Language allows for choosing classes and methods to instrument, extracting parameters,
 * return values, transforming/filtering intercepted values and presenting them via
 * JMX in various ways.
 *
 * @author rafal.lewczuk@jitlogic.com
 */
public class SpyDefinition {

    private static final List<SpyProcessor> EMPTY_XF =
            Collections.unmodifiableList(Arrays.asList(new SpyProcessor[0]));
    private static final List<SpyProbe> EMPTY_AF =
            Collections.unmodifiableList(Arrays.asList(new SpyProbe[0]));

    private final Object name;

    private final Set<Symbol> tags = new HashSet<Symbol>();

    /**
     * Spy probes for various places in method bytecode
     */
    private List<SpyProbe>[] probes;

    /**
     * Lists of spy record processors and collectors (for various stages)
     */
    private List<SpyProcessor>[] processors;

    /**
     * List of matchers defining that classes/methods this sdef looks for
     */
    private SpyMatcherSet matcherSet = new SpyMatcherSet();


    private Set<Object> tagMatcherSet = new HashSet<Object>();

    /**
     * Creates unconfigured spy definition
     *
     * @return spy definition
     */
    public static SpyDefinition instance(Object name) {
        return new SpyDefinition(name);
    }

    /**
     * Creates empty (unconfigured) spy definition.
     */
    public SpyDefinition(Object name) {
        this.name = name;
        probes = new List[4];
        for (int i = 0; i < probes.length; i++) {
            probes[i] = EMPTY_AF;
        }

        processors = new List[4];
        for (int i = 0; i < processors.length; i++) {
            processors[i] = EMPTY_XF;
        }
    }


    private SpyDefinition(SpyDefinition orig) {
        this.name = orig.name;
        this.tags.addAll(orig.tags);
        this.probes = ZorkaUtil.copyArray(orig.probes);
        this.processors = ZorkaUtil.copyArray(orig.processors);
        this.matcherSet = new SpyMatcherSet(orig.matcherSet);
    }


    /**
     * Returns list of probe definitions from particular stage
     *
     * @param stage stage we're interested in
     * @return list of probes defined for this stage
     */
    public List<SpyProbe> getProbes(int stage) {
        return probes[stage];
    }


    /**
     * Returns list of processors for a particular stage.
     *
     * @param stage
     * @return
     */
    public List<SpyProcessor> getProcessors(int stage) {
        return processors[stage];
    }


    public SpyMatcherSet getMatcherSet() {
        return matcherSet;
    }

    public Object getName() {
        return name;
    }

    /**
     * Instructs spy what method to include.
     *
     * @param mdefs
     * @return
     */
    public SpyDefinition include(Object...mdefs) {
        SpyMatcher[] matchers = new SpyMatcher[mdefs.length];
        for (int i = 0; i < mdefs.length; i++) {
            matchers[i] = mdefs[i] instanceof SpyMatcher ? (SpyMatcher)mdefs[i]
                : mdefs[i] instanceof String ? SpyMatcher.fromString((String)mdefs[i]) : null;
        }
        SpyDefinition sdef = new SpyDefinition(this);
        sdef.matcherSet = new SpyMatcherSet(sdef.matcherSet, matchers);
        return sdef;
    }

    public SpyDefinition with(int curStage, Object arg) {
        SpyDefinition sdef = new SpyDefinition(this);

        List<SpyProbe> newProbes = new ArrayList<SpyProbe>(sdef.probes[curStage].size() + 1);
        newProbes.addAll(sdef.probes[curStage]);

        List<SpyProcessor> newProcessors = new ArrayList<SpyProcessor>(sdef.processors[curStage].size() + 1);
        newProcessors.addAll(sdef.processors[curStage]);

        if (arg instanceof SpyProcessor) {
            newProcessors.add((SpyProcessor) arg);
        } else if (arg instanceof SpyProbe) {
            newProbes.add((SpyProbe) arg);
        } else if (arg != null) {
            throw new IllegalArgumentException();
        }

        sdef.probes = ZorkaUtil.copyArray(probes);
        sdef.probes[curStage] = Collections.unmodifiableList(newProbes);

        sdef.processors = ZorkaUtil.copyArray(processors);
        sdef.processors[curStage] = Collections.unmodifiableList(newProcessors);

        return sdef;
    }


    public boolean sameProbes(SpyDefinition sdef) {
        for (int i = 0; i < 4; i++) {
            if (!ZorkaUtil.objEquals(this.getProbes(i), sdef.getProbes(i))) {
                return false;
            }
        }
        return true;
    }

    public int getLastArgIndex() {
        int idx = -1;

        if (probes != null) {
            for (List<SpyProbe> lp : probes) {
                if (lp != null) {
                    for (SpyProbe p : lp) {
                        if (p instanceof SpyArgProbe) {
                            idx = Math.max(idx, ((SpyArgProbe)p).getArgIndex());
                        }
                    }
                }
            }
        }

        return idx;
    }
}

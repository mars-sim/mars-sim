/*
 * Mars Simulation Project
 * TemporalExecutor.java
 * @date 2025-08-31 (parallel/CME-safe targets)
 * Base temporal fan-out with CME-safe iteration.
 */
package com.mars_sim.core.unit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;

/**
 * Base executor that manages a set of temporal targets and applies a pulse.
 * Default applyPulse is sequential; subclasses may override to parallelize.
 */
public class TemporalExecutor {

    private static final SimLogger logger = SimLogger.getLogger(TemporalExecutor.class.getName());

    /** CME-safe target list; iteration is weakly consistent and snapshot-friendly. */
    private final CopyOnWriteArrayList<Temporal> targets = new CopyOnWriteArrayList<>();

    /** Add a target once. */
    public void addTarget(Temporal t) {
        if (t != null) targets.addIfAbsent(t);
    }

    /** Remove a target if present. */
    public void removeTarget(Temporal t) {
        if (t != null) targets.remove(t);
    }

    /** Snapshot of current targets for safe external iteration (immutable). */
    protected List<Temporal> snapshotTargets() {
        if (targets.isEmpty()) return Collections.emptyList();
        return Collections.unmodifiableList(new ArrayList<>(targets));
    }

    /**
     * Applies a pulse to all targets (sequential by default, exception-shielded).
     * Subclasses may override to parallelize while preserving barrier semantics.
     */
    public void applyPulse(ClockPulse pulse) {
        for (Temporal t : snapshotTargets()) {
            try {
                t.timePassing(pulse);
            }
            catch (Throwable ex) {
                logger.severe("Problem with pulse on " + t + ": ", ex);
            }
        }
    }

    /** Stop hooks for subclasses with thread pools. */
    public void stop() { /* no-op by default */ }
}

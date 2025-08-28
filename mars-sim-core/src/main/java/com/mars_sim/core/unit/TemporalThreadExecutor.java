/*
 * Mars Simulation Project
 * TemporalThreadExecutor.java
 * Patched: 2025-08-28
 *
 * Sequential fan-out executor. CME-safe iteration via CopyOnWriteArrayList.
 * Useful when deterministic order or minimal threading is desired.
 */
package com.mars_sim.core.unit;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;

public class TemporalThreadExecutor implements TemporalExecutor {

    private static final SimLogger LOG = SimLogger.getLogger(TemporalThreadExecutor.class.getName());

    /** Snapshot-safe target list. */
    private final CopyOnWriteArrayList<Temporal> targets = new CopyOnWriteArrayList<>();

    /** Liveness flag. */
    private final AtomicBoolean running = new AtomicBoolean(true);

    public TemporalThreadExecutor() { }

    @Override
    public void addTarget(Temporal t) {
        if (t == null) return;
        targets.addIfAbsent(t);
    }

    @Override
    public void removeTarget(Temporal t) {
        if (t == null) return;
        targets.remove(t);
    }

    /**
     * Delivers the pulse to targets sequentially on the caller's thread.
     * Snapshot iteration ensures no CME if targets are modified during dispatch.
     */
    @Override
    public void applyPulse(ClockPulse pulse) {
        Objects.requireNonNull(pulse, "pulse");
        if (!running.get()) return;

        for (Temporal t : targets) {
            try {
                t.timePassing(pulse);
            } catch (Throwable ex) {
                LOG.severe("Temporal target threw during pulse #" + pulse.getId() + ": ", ex);
                // Continue with remaining targets
            }
        }
    }

    @Override
    public void stop() {
        running.set(false);
        // No thread resources to tear down in the sequential implementation
    }
}

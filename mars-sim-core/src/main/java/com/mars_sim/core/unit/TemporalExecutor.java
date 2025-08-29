/*
 * Mars Simulation Project
 * TemporalExecutor.java
 * Patched: 2025-08-28
 *
 * Contract:
 *  - addTarget/removeTarget are thread-safe.
 *  - applyPulse(pulse) delivers the pulse to all current targets.
 *  - stop() halts further execution and tears down resources.
 */
package com.mars_sim.core.unit;

import java.util.Collection;

import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;

public interface TemporalExecutor {
     void addTarget(Temporal target);
     void removeTarget(Temporal target);
     void applyPulse(ClockPulse pulse);
     void stop();
 }

    /**
     * Applies a clock pulse to all current targets.
     * Implementations should preserve tick boundary semantics:
     * - Either sequentially (single thread), or
     * - Parallel with a barrier so the method returns after all targets complete.
     */
    void applyPulse(ClockPulse pulse);

    /** Stops the executor, releasing any resources. Safe to call multiple times. */
    void stop();
}

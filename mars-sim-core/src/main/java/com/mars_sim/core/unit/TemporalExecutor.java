/*
 * Mars Simulation Project
 * TemporalExecutor.java
 * @date 2025-07-27
 * @author Barry Evans
 */
package com.mars_sim.core.unit;

import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;

/**
 * The interface for an Exector that can apply a pluse to multiple
 * Temporal instances in parallel
 */
public interface TemporalExecutor {

    /**
     * Aooly a pulse and block until all registered Temporals have applied the Pulse.
     * @param pulse Pulse to apply
     */
    void applyPulse(ClockPulse pulse);

    /**
     * Stop the executor
     */
    void stop();

    /**
     * Register a new target to the existng collection.
     * @param s Temporal to add
     */
    void addTarget(Temporal s);
}

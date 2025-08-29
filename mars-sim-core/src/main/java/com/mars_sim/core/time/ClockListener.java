/*
 * Mars Simulation Project
 * ClockListener.java
 */
package com.mars_sim.core.time;

/**
 * Receives simulated time pulses and pause changes from the MasterClock.
 * Implementations should keep work short; long work should be offloaded.
 */
public interface ClockListener {

    /** Called each simulation pulse. */
    void clockPulse(ClockPulse pulse);

    /** Optional UI pulse (reserved for UI integrations). */
    default void uiPulse(double time) { }

    /** Called when the simulation pause state changes. */
    default void pauseChange(boolean isPaused, boolean showPane) { }
}

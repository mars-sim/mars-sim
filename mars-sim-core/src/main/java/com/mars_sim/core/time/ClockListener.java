/*
 * Mars Simulation Project
 * ClockListener.java
 * @date 2025-08-28 (hardened)
 */
package com.mars_sim.core.time;

/**
 * Listener for master clock events.
 *
 * <p>All methods are default no-ops so implementors can override only what they need.</p>
 */
public interface ClockListener {

    /**
     * Called on each simulation tick/pulse.
     *
     * @param time quantity associated with the pulse (e.g., millisols)
     */
    default void clockPulse(double time) { /* no-op */ }

    /**
     * Called for UI refresh pulses (may differ in cadence from simulation pulses).
     *
     * @param time quantity associated with the pulse (e.g., millisols)
     */
    default void uiPulse(double time) { /* no-op */ }

    /**
     * Called when the pause state changes.
     *
     * @param isPaused current pause state
     * @param showPane whether the UI should show a pause pane
     */
    default void pauseChange(boolean isPaused, boolean showPane) { /* no-op */ }
}

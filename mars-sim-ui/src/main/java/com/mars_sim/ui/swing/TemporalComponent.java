/*
 * Mars Simulation Project
 * TemporalComponent.java
 * @date 2025-11-29
 * @author Barry Evans
 */
package com.mars_sim.ui.swing;

import com.mars_sim.core.time.ClockPulse;

/**
 * This represents a UI component that will react to a change in the Master Clock.
 * It will be notifiedof new Clock Pulses but the pulses are NOT continguous.
 * Pulses maybe be skipped so as not to flood the UI. The UI does not need the same
 * frequency of pulses as the main simulation engine.
 */
public interface TemporalComponent {

    /**
     * Updates content panel with clock pulse information.
     * @param pulse
     */
    void clockUpdate(ClockPulse pulse);
}

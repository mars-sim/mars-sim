/**
 * Mars Simulation Project
 * ClockListener.java
 * @version 2026-03-21
 * @author Barry Evans
 */
package com.mars_sim.core.time;

/**
 * Listener for changes to the clock settings such as desired time ratio and pause state.
 */
public interface ClockListener {

    /**
     * Desired Speed of the clock has changed
     * @param desiredTR Requested time ratio.
     */
    void desiredTimeRatioChange(int desiredTR);

	/**
	 * Change the pause state of the clock.
	 * 
	 * @param isPaused true if clock is paused.
     */
	void pauseChange(boolean isPaused);
}

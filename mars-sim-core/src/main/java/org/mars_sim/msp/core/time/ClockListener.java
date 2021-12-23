/**
 * Mars Simulation Project
 * ClockListener.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.time;

/**
 * A listener for clock time changes.
 */
public interface ClockListener {

	/**
	 * Change in time for managers in Simulation.
	 * 
	 * @param currentPulse the current pulse
	 */
	public void clockPulse(ClockPulse currentPulse);

	/**
	 * Change the pause state of the clock.
	 * 
	 * @param isPaused true if clock is paused.
	 */
	public void pauseChange(boolean isPaused, boolean showPane);
}

/**
 * Mars Simulation Project
 * ClockListener.java
 * @version 3.1.2 2020-09-02
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
	 * @param time the amount of time changed. (millisols)
	 */
	public void clockPulse(double time);

	/**
	 * Change in time for map related class
	 * 
	 * @param time the amount of time changed. (millisols)
	 */
	public void uiPulse(double time);

	/**
	 * Change the pause state of the clock.
	 * 
	 * @param isPaused true if clock is paused.
	 */
	public void pauseChange(boolean isPaused, boolean showPane);
}

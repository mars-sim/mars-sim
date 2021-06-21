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
	 * Change in time for map related class
	 * TODO: Shouldn't the UI just be another clock listener? The UI listener beats at a slowed down rate 
	 * so this should be part of the how the listener is regsitered with the MasterClock, i.e. slow or fast.
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

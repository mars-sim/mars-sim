/**
 * Mars Simulation Project
 * ClockPulseListener.java
 * @version 2021-06-20
 * @author Scott Davis
 */
package com.mars_sim.core.time;

/**
 * A listener for clock time changes.
 */
public interface ClockPulseListener {

	/**
	 * Change in time for managers in Simulation.
	 * 
	 * @param currentPulse the current pulse
	 */
	void clockPulse(ClockPulse currentPulse);
}

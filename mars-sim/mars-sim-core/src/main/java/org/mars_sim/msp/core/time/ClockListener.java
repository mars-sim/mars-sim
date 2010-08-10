/**
 * Mars Simulation Project
 * ClockListener.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */
package org.mars_sim.msp.core.time;

/**
 * A listener for clock time changes.
 */
public interface ClockListener {

	/**
	 * Change in time.
	 * param time the amount of time changed. (millisols)
	 */
	public void clockPulse(double time);
}
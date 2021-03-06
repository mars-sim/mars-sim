/**
 * Mars Simulation Project
 * Temporal.java
 * @version 3.2.0 2021-06-20
 * @author Barry Evans
 */

package org.mars_sim.msp.core.time;

/**
 * Represents an instance that is influenced by passing time.
 *
 */
public interface Temporal {
	
	/**
	 * Time has advanced.
	 * @param pulse The advancement of time.
	 * @return Was the pulse applied.
	 */
	boolean timePassing(ClockPulse pulse);
}

/**
 * Mars Simulation Project
 * Medical.java
 * @version 3.1.1 2020-07-22
 * @author Scott Davis
 */

package org.mars_sim.msp.core.vehicle;

/**
 * The Medical interface is for vehicles that have a sick bay.
 */
public interface Medical {

	/**
	 * Gets the vehicle's sick bay.
	 * @return Sickbay
	 */
	public SickBay getSickBay();
}

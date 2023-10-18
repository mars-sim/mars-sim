/**
 * Mars Simulation Project
 * Medical.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */

package com.mars_sim.core.vehicle;

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

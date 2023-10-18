/**
 * Mars Simulation Project
 * Airlockable.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis 
 */
package com.mars_sim.core.vehicle;

import com.mars_sim.core.structure.Airlock;

/**
 * This interface represents a vehicle with an airlock. 
 */
public interface Airlockable {

	/**
	 * Gets the vehicle's airlock.
	 * @return airlock
	 */
	public Airlock getAirlock();
}

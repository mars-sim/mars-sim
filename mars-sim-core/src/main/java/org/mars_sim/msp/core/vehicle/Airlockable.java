/**
 * Mars Simulation Project
 * Airlockable.java
 * @version 3.1.1 2020-07-22
 * @author Scott Davis 
 */
package org.mars_sim.msp.core.vehicle;

import org.mars_sim.msp.core.structure.Airlock;

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

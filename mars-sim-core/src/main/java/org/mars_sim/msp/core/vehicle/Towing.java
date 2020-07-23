/**
 * Mars Simulation Project
 * Towing.java
 * @version 3.1.1 2020-07-22
 * @author Scott Davis
 */

package org.mars_sim.msp.core.vehicle;

public interface Towing {

	/**
	 * Sets the vehicle this rover is currently towing.
	 * 
	 * @param towedVehicle the vehicle being towed.
	 */
	public void setTowedVehicle(Vehicle towedVehicle);

	/**
	 * Gets the vehicle this rover is currently towing.
	 * 
	 * @return towed vehicle.
	 */
	public Vehicle getTowedVehicle();
}

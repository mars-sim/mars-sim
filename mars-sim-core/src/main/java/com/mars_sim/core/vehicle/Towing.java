/*
 * Mars Simulation Project
 * Towing.java
 * @date 2024-07-24
 * @author Scott Davis
 */

package com.mars_sim.core.vehicle;

/**
 * The interface for a towing vehicle. It has a towed vehicle.
 */
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

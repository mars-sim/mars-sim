/*
 * Mars Simulation Project
 * Towed.java
 * @date 2024-07-24
 * @author Manny Kung
 */

package com.mars_sim.core.vehicle;

public interface Towed {

	/**
	 * Sets the vehicle that is towing this LUV or rover.
	 * 
	 * @param towingVehicle the vehicle being towed.
	 */
	public void setTowingVehicle(Vehicle towingVehicle);

	/**
	 * Gets the vehicle that is towing this LUV or rover.
	 * 
	 * @return towing vehicle.
	 */
	public Vehicle getTowingVehicle();
}

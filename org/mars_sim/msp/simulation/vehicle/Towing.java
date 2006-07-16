/**
 * Mars Simulation Project
 * Towing.java
 * @version 2.79 2006-07-15
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.vehicle;

public interface Towing {

    /**
     * Sets the vehicle this rover is currently towing.
     * @param towedVehicle the vehicle being towed.
     */
    public void setTowedVehicle(Vehicle towedVehicle);
    
    /**
     * Gets the vehicle this rover is currently towing.
     * @return towed vehicle.
     */
    public Vehicle getTowedVehicle();
}
/**
 * Mars Simulation Project
 * VehicleMaintenance.java
 * @version 2.75 2003-04-16
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building.function;

import java.util.Collection;
import org.mars_sim.msp.simulation.vehicle.*;
import org.mars_sim.msp.simulation.structure.building.BuildingException;
 
/**
 * The VehicleMaintenance interface is a building function for a building
 * capable of maintaining vehicles.
 */
public interface VehicleMaintenance extends Function {
        
    /** 
     * Gets the total mass of vehicles the building can accommodate.
     *
     * @return vehicle capacity (kg)
     */
    public double getVehicleCapacity();
    
    /** 
     * Gets the current mass of vehicles in the building.
     *
     * @return vehicle mass (kg)
     */
    public double getCurrentVehicleMass();
    
    /** 
     * Add vehicle to building if there's room.
     *
     * @param vehicle the vehicle to be added.
     * @throws BuildingException if vehicle cannot be added.
     */
    public void addVehicle(Vehicle vehicle) throws BuildingException;
    
    /** 
     * Remove vehicle from building if it's in the building.
     *
     * @param vehicle the vehicle to be removed.
     * @throws BuildingException if vehicle is not in the building.
     */
    public void RemoveVehicle(Vehicle vehicle) throws BuildingException;
    
    /**
     * Checks if a vehicle is in the building.
     *
     * @return true if vehicle is in the building.
     */
    public boolean containsVehicle(Vehicle vehicle);
    
    /**
     * Gets a collection of vehicles in the building.
     * 
     * @return Collection of vehicles in the building.
     */
    public VehicleCollection getVehicles();
}

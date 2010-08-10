/**
 * Mars Simulation Project
 * VehicleMaintenance.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.vehicle.Vehicle;
 
/**
 * The VehicleMaintenance interface is a building function for a building
 * capable of maintaining vehicles.
 */
public abstract class VehicleMaintenance extends Function implements Serializable {
    
    private static String CLASS_NAME = 
	    "org.mars_sim.msp.simulation.structure.building.function.VehicleMaintenance";
	
    private static Logger logger = Logger.getLogger(CLASS_NAME);
        
    protected int vehicleCapacity;
	private Collection<Vehicle> vehicles;
        
    /**
     * Constructor
     * @param name the name of the child function.
     * @param building the building this function is for.
     */
    public VehicleMaintenance(String name, Building building) {
    	// Use Function constructor.
    	super(name, building);
    	
    	vehicles = new ConcurrentLinkedQueue<Vehicle>();
    }
        
    /** 
     * Gets the number of vehicles the building can accommodate.
     * @return vehicle capacity
     */
    public int getVehicleCapacity() {
    	return vehicleCapacity;
    }
    
    /** 
     * Gets the current number of vehicles in the building.
     * @return number of vehicles
     */
    public int getCurrentVehicleNumber() {
		return vehicles.size();
    }
    
    /** 
     * Add vehicle to building if there's room.
     * @param vehicle the vehicle to be added.
     * @throws BuildingException if vehicle cannot be added.
     */
    public void addVehicle(Vehicle vehicle) throws BuildingException {
        
		// Check if vehicle cannot be added to building.
		if (vehicles.contains(vehicle)) 
			throw new BuildingException("Building already contains vehicle.");
		if (vehicles.size() >= vehicleCapacity) 
			throw new BuildingException("Building is full of vehicles.");
     
		// Remove vehicle from any other garage that it might be in.
		Iterator i = getBuilding().getBuildingManager().getBuildings(getName()).iterator();
		while (i.hasNext()) {
			Building building = (Building) i.next();
			VehicleMaintenance garage = (VehicleMaintenance) building.getFunction(getName());
			if (garage.containsVehicle(vehicle)) {
				try {
					garage.removeVehicle(vehicle);
				}
				catch (BuildingException e) {}
			}
		}
        
		// Add vehicle to building.
		vehicles.add(vehicle);
	       logger.info("Adding " + vehicle.getName());
    }
    
    /** 
     * Remove vehicle from building if it's in the building.
     * @param vehicle the vehicle to be removed.
     * @throws BuildingException if vehicle is not in the building.
     */
    public void removeVehicle(Vehicle vehicle) throws BuildingException {
		if (!containsVehicle(vehicle)) throw new BuildingException("Vehicle not in building.");
		else {
			vehicles.remove(vehicle);
			logger.info("Removing " + vehicle.getName());
		} 
    }
    
    /**
     * Checks if a vehicle is in the building.
     * @return true if vehicle is in the building.
     */
    public boolean containsVehicle(Vehicle vehicle) {
    	if (vehicles.contains(vehicle)) return true;
    	else return false;
    }
    
    /**
     * Gets a collection of vehicles in the building.
     * @return Collection of vehicles in the building.
     */
    public Collection<Vehicle> getVehicles() {
    	return vehicles;
    }
    
	/**
	 * Time passing for the building.
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	public void timePassing(double time) throws BuildingException { 
	
		// Check to see if any vehicles are in the garage that don't need to be.
		Iterator<Vehicle> i = vehicles.iterator();
		while (i.hasNext()) {
			Vehicle vehicle = i.next();
			if (!vehicle.isReserved()) removeVehicle(vehicle); 
		}
	}
	
	/**
	 * Gets the amount of power required when function is at full power.
	 * @return power (kW)
	 */
	public double getFullPowerRequired() {
		return 0D;
	}
	
	/**
	 * Gets the amount of power required when function is at power down level.
	 * @return power (kW)
	 */
	public double getPowerDownPowerRequired() {
		return 0D;
	}    
}
/**
 * Mars Simulation Project
 * StandardGroundVehicleMaintenance.java
 * @version 2.75 2003-04-18
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building.function.impl;
 
import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.*;
import org.mars_sim.msp.simulation.vehicle.*;
 
/**
 * Standard implementation of the GroundVehicleMaintenance building function.
 */
public class StandardGroundVehicleMaintenance implements GroundVehicleMaintenance, Serializable {
    
    private Building building;
    private double vehicleCapacity;
    private VehicleCollection vehicles;
    
    /**
     * Constructor
     *
     * @param the building this is implemented for.
     * @param vehicleCapacity the total mass of vehicles the building can accommodate.
     */
    public StandardGroundVehicleMaintenance(Building building, double vehicleCapacity) {
        this.building = building;
        this.vehicleCapacity = vehicleCapacity;
        
        vehicles = new VehicleCollection();
    }
    
    /** 
     * Gets the total mass of vehicles the building can accommodate.
     *
     * @return vehicle capacity (kg)
     */
    public double getVehicleCapacity() {
        return vehicleCapacity;
    }
    
    /** 
     * Gets the current mass of vehicles in the building.
     *
     * @return vehicle mass (kg)
     */
    public double getCurrentVehicleMass() {
        double result = 0D;
        VehicleIterator i = vehicles.iterator();
        while (i.hasNext()) result += i.next().getMass();
        return result;
    }       
    
    /** 
     * Add vehicle to building if there's room.
     *
     * @param vehicle the vehicle to be added.
     * @throws BuildingException if vehicle cannot be added.
     */
    public void addVehicle(Vehicle vehicle) throws BuildingException {
        
        // Check if vehicle cannot be added to building.
        if (vehicles.contains(vehicle)) 
            throw new BuildingException("Building already contains vehicle.");
        if (!(vehicle instanceof GroundVehicle)) 
            throw new BuildingException("Vehicle is not ground vehicle.");
        if (vehicle.getMass() > (getVehicleCapacity() - getCurrentVehicleMass())) 
            throw new BuildingException("Vehicle exceeds total capacity of building");
     
        // Remove vehicle from any other garage that it might be in.
        Iterator i = building.getBuildingManager().getBuildings(VehicleMaintenance.class).iterator();
        while (i.hasNext()) {
            VehicleMaintenance garage = (VehicleMaintenance) i.next();
            if (garage.containsVehicle(vehicle)) {
                try {
                    garage.removeVehicle(vehicle);
                }
                catch (BuildingException e) {}
            }
        }
        
        // Add vehicle to building.
        vehicles.add(vehicle);
    }
    
    /** 
     * Remove vehicle from building if it's in the building.
     *
     * @param vehicle the vehicle to be removed.
     * @throws BuildingException if vehicle is not in the building.
     */
    public void removeVehicle(Vehicle vehicle) throws BuildingException {
        if (!containsVehicle(vehicle)) throw new BuildingException("Vehicle not in building.");
        else vehicles.remove(vehicle);
    }
    
    /**
     * Checks if a vehicle is in the building.
     *
     * @return true if vehicle is in the building.
     */
    public boolean containsVehicle(Vehicle vehicle) {
        return vehicles.contains(vehicle);
    }
    
    /**
     * Gets a collection of vehicles in the building.
     * 
     * @return Collection of vehicles in the building.
     */
    public VehicleCollection getVehicles() {
        return new VehicleCollection(vehicles);
    }
}

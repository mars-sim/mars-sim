/**
 * Mars Simulation Project
 * MaintenanceGarage.java
 * @version 2.74 2002-04-21
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.vehicle.*;
import java.io.Serializable;
import java.util.Vector;

/**
 * The MaintenanceGarage class represents the pressurized maintenance garage in a settlement.
 * Vehicles can be taken to a maintenance garage for repair and maintenance.
 * Note: Any number or size of vehicles can always be parked outside a settlement.  The garage's
 * capacity only reflects those vehicles in the garage itself.
 */
public class MaintenanceGarage extends Facility implements Serializable {

    public final static String NAME = "Maintenance Garage";
	
    // Data members
    private double vehicleCapacity; // The total mass (kg) of vehicles the garage can accomidate.
    private VehicleCollection vehicles; // A collection of vehicles currently in the garage.

    /** Constructor for random creation. 
     *  @param manager the garage's facility manager
     */
    MaintenanceGarage(FacilityManager manager) {

        // Use Facility's constructor.
        super(manager, NAME);

        // Add scope string to malfunction manager.
	malfunctionManager.addScopeString("MaintenanceGarage");
	
        // Initialize data members
        vehicles = new VehicleCollection();

	// Set vehicle capacity capacity to 20 metric tons.
        vehicleCapacity = 20000D;
    }

    /** Constructor for set values (used later when facilities can be built or upgraded.) 
     *  @param manager the garage's facility manager
     *  @param vehicleCapacity total mass (kg) of vehicles the garage can accomidate 
     */
    MaintenanceGarage(FacilityManager manager, double vehicleCapacity) {

        // Use Facility's constructor.
        super(manager, "Maintenance Garage");

        // Initialize data members.
        vehicles = new VehicleCollection();
	this.vehicleCapacity = vehicleCapacity;
    }

    /** 
     * Gets the total mass of vehicles the garage can accomidate.
     * @return vehicle capacity (kg)
     */
    public double getVehicleCapacity() {
        return vehicleCapacity;
    }

    /** 
     * Gets the current mass of vehicles in the garage.
     * @return vehicle mass (kg)
     */
    public double getCurrentVehicleMass() {
        double vehicleMass = 0D;

	VehicleIterator i = vehicles.iterator();
	while (i.hasNext()) {
	    vehicleMass += i.next().getMass();
	}
	    
	return vehicleMass;   
    }
    
    /** Add vehicle to garage if there's room.
     * @return true if vehicle has been added successfully.
     * False if vehicle could not be added.
     */
    public boolean addVehicle(Vehicle vehicle) {

        boolean result = false;
	    
	if (vehicle.getMass() < (vehicleCapacity - getCurrentVehicleMass())) {
	    if (!vehicles.contains(vehicle)) {
	        vehicles.add(vehicle);
	        result = true;
	    }
	}

	return result;
    }

    /** Removes a vehicle from the garage.
     *  If the vehicle is not in the garage, does nothing.
     *  @param vehicle vehicle to be removed
     */
    public void removeVehicle(Vehicle vehicle) {
        if (vehicles.contains(vehicle)) {
            vehicles.remove(vehicle);
        }
    }

    /** Returns true if vehicle is currently in the garage.
     *  Returns false otherwise.
     *  @return true if vehicle is currently in the garage
     */
    public boolean vehicleInGarage(Vehicle vehicle) {
	return vehicles.contains(vehicle);
    }

    /** Returns an array of vehicle names of the vehicles currently in the garage. 
     *  @return array of vehicle names for vehicles currently in the garage
     */
    public String[] getVehicleNames() {
        String[] result = new String[vehicles.size()];

	VehicleIterator i = vehicles.iterator();
	int count = 0;
	while (i.hasNext()) {
	    result[count] = i.next().getName();
	    count++;
	}

        return result;
    }

    /** Returns an array of vehicles currently in the garage. 
     *  @return array of vehicles currently in the garage
     */
    public Vehicle[] getVehicles() {
        Vehicle[] result = new Vehicle[vehicles.size()];

	VehicleIterator i = vehicles.iterator();
	int count = 0;
	while (i.hasNext()) {
	    result[count] = i.next();
	    count++;
	}

        return result;
    }

    /**
     * Time passing for maintenance garage.
     * @param time the amount of time passing (millisols)
     */
    public void timePassing(double time) {
        if (vehicles.size() > 0) malfunctionManager.activeTimePassing(time);
    }
}

/**
 * Mars Simulation Project
 * TransportRover.java
 * @version 2.74 2002-03-03
 */

package org.mars_sim.msp.simulation.vehicle;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.structure.*;
import java.io.Serializable;

/**
 * The TransportRover class is a rover designed for transporting people 
 * from settlement to settlement.
 */
public class TransportRover extends Rover implements Serializable {
	
    // Static data members
    private static final int CREW_CAPACITY = 12; // Max number of crewmembers.
    private static final double FUEL_CAPACITY = 3750D; // Fuel capacity of rover in kg.
    private static final double OXYGEN_CAPACITY = 525D; // Oxygen capacity of rover in kg.
    private static final double WATER_CAPACITY = 2100D; // Water capacity of rover in kg.
    private static final double FOOD_CAPACITY = 787.5D; // Food capacity of rover in kg.
	
    /** 
     * Constructs an TransportRover object at a given settlement.
     * @param name the name of the rover
     * @param settlement the settlementt he rover is parked at
     * @param mars the mars instance
     */
    TransportRover(String name, Settlement settlement, VirtualMars mars) {
        // Use the Rover constructor
	super(name, settlement, mars);

	initTransportRoverData();

	// Add EVA suits
	addEVASuits();
    }

    /**
     * Constructs an TransportRover object
     * @param name the name of the rover
     * @param mars the mars instance
     * @param manager the unit manager
     * @throws Exception when there are no available settlements
     */
    TransportRover(String name, VirtualMars mars, UnitManager manager) throws Exception {
        // Use the Rover constructor
	super(name, mars, manager);

	initTransportRoverData();

	// Add EVA suits
	addEVASuits();
    }

    /**
     * Initialize rover data
     */
    private void initTransportRoverData() {
        
        // Set crew capacity
	crewCapacity = CREW_CAPACITY;

	// Set resource capacities of rover
	inventory.setResourceCapacity(Inventory.FUEL, FUEL_CAPACITY);
	inventory.setResourceCapacity(Inventory.OXYGEN, OXYGEN_CAPACITY);
	inventory.setResourceCapacity(Inventory.WATER, WATER_CAPACITY);
	inventory.setResourceCapacity(Inventory.FOOD, FOOD_CAPACITY);
    }

    /** 
     * Returns a string describing the vehicle.
     * @return string describing vehicle
     */
    public String getDescription() {
        return "Long Range Transport Rover";
    }
}

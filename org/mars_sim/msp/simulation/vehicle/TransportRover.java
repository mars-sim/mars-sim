/**
 * Mars Simulation Project
 * TransportRover.java
 * @version 2.75 2003-02-05
 */

package org.mars_sim.msp.simulation.vehicle;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.structure.*;
import org.mars_sim.msp.simulation.person.medical.MedicalAid;
import org.mars_sim.msp.simulation.person.medical.SickBay;
import java.io.Serializable;

/**
 * The TransportRover class is a rover designed for transporting people
 * from settlement to settlement.
 */
public class TransportRover extends Rover implements Serializable {

    // Static data members
    private static final int CREW_CAPACITY = 12; // Max number of crewmembers.
    private static final double CARGO_CAPACITY = 12000D; // Cargo capacity of rover in kg.
    private static final double FUEL_CAPACITY = 3750D; // Fuel capacity of rover in kg.
    private static final double OXYGEN_CAPACITY = 1000D; // Oxygen capacity of rover in kg.
    private static final double WATER_CAPACITY = 4000D; // Water capacity of rover in kg.
    private static final double FOOD_CAPACITY = 787.5D; // Food capacity of rover in kg.
    private static final int SICKBAY_LEVEL = 3; // Facility level of Sickbay
    private SickBay sickBay = null;

    /**
     * Constructs an TransportRover object at a given settlement.
     * @param name the name of the rover
     * @param settlement the settlementt he rover is parked at
     * @param mars the mars instance
     */
    public TransportRover(String name, Settlement settlement, Mars mars) {
        // Use the Rover constructor
        super(name, settlement, mars);

        initTransportRoverData();

        // Add EVA suits
        addEVASuits();
    }

    /**
     * Initialize rover data
     */
    private void initTransportRoverData() {

        // Add scope to malfunction manager.
	    malfunctionManager.addScopeString("TransportRover");
	    
        // Set crew capacity
	    crewCapacity = CREW_CAPACITY;

        // Set the cargo capacity of rover.
	    inventory.setTotalCapacity(CARGO_CAPACITY);
	
	    // Set resource capacities of rover
	    inventory.setResourceCapacity(Resource.FUEL, FUEL_CAPACITY);
	    inventory.setResourceCapacity(Resource.OXYGEN, OXYGEN_CAPACITY);
	    inventory.setResourceCapacity(Resource.WATER, WATER_CAPACITY);
	    inventory.setResourceCapacity(Resource.FOOD, FOOD_CAPACITY);

        sickBay = new SickBay("RoverSickBay", 2, SICKBAY_LEVEL, mars, this);
    }

    /**
     * Returns a string describing the vehicle.
     * @return string describing vehicle
     */
    public String getDescription() {
        return "Long Range Transport Rover";
    }

    /**
     * Returns a MedicalAid that is available in this Vehicle. This implementation
     * return a reference to the internal Sickbay.
     * @return Sick Bay
     */
    public MedicalAid getMedicalFacility() {
        return sickBay;
    }
}

/**
 * Mars Simulation Project
 * ExplorerRover.java
 * @version 2.75 2003-02-26
 */

package org.mars_sim.msp.simulation.vehicle;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.structure.*;
import java.io.Serializable;

/**
 * The ExplorerRover class is a rover designed for exploration and collecting
 * rock samples.
 */
public class ExplorerRover extends Rover implements Serializable {
	
    // Static data members
    private static final double RANGE = 3000D; // Operating range of rover in km.
    private static final int CREW_CAPACITY = 4; // Max number of crewmembers.
    private static final double CARGO_CAPACITY = 10000D; // Cargo capacity of rover in kg.
    private static final double METHANE_CAPACITY = 2500D; // Methane capacity of rover in kg.
    private static final double OXYGEN_CAPACITY = 350D; // Oxygen capacity of rover in kg.
    private static final double WATER_CAPACITY = 1400D; // Water capacity of rover in kg.
    private static final double FOOD_CAPACITY = 525D; // Food capacity of rover in kg.
    private static final double ROCK_SAMPLES_CAPACITY = 5000; // Rock samples capacity of rover in kg.

    // Data members
    private MobileLaboratory lab; // The rover's lab.
    
    /** 
     * Constructs an ExplorerRover object at a given settlement.
     * @param name the name of the rover
     * @param settlement the settlementt he rover is parked at
     * @param mars the mars instance
     */
    public ExplorerRover(String name, Settlement settlement, Mars mars) {
        // Use the Rover constructor
	    super(name, settlement, mars);

	    initExplorerRoverData();

	    // Add EVA Suits
	    addEVASuits();
    }

    /**
     * Initialize rover data
     */
    private void initExplorerRoverData() {
       
        // Add scope to malfunction manager.
	    malfunctionManager.addScopeString("ExplorerRover");
	    malfunctionManager.addScopeString("Laboratory");
	    
        // Set the operating range of rover.
        range = RANGE;
        
        // Set crew capacity
	    crewCapacity = CREW_CAPACITY;

        // Set inventory total mass capacity.
        inventory.setTotalCapacity(CARGO_CAPACITY);
	
        // Set inventory resource capacities.
        inventory.setResourceCapacity(Resource.METHANE, METHANE_CAPACITY);
        inventory.setResourceCapacity(Resource.OXYGEN, OXYGEN_CAPACITY);
        inventory.setResourceCapacity(Resource.WATER, WATER_CAPACITY);
        inventory.setResourceCapacity(Resource.FOOD, FOOD_CAPACITY);
        inventory.setResourceCapacity(Resource.ROCK_SAMPLES, ROCK_SAMPLES_CAPACITY);
        
	    // Construct mobile lab.
	    String[] techSpeciality = { "Aerology" };
	    lab = new MobileLaboratory(1, 1, techSpeciality);
    }

    /** 
     * Returns a string describing the vehicle.
     * @return string describing vehicle
     */
    public String getDescription() {
        return "Long Range Exploration Rover";
    }

    /**
     * Gets the explorer rover's lab.
     * @return laboratory
     */
    public MobileLaboratory getLab() {
        return lab; 
    }
}

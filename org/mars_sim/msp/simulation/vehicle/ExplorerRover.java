/**
 * Mars Simulation Project
 * ExplorerRover.java
 * @version 2.75 2004-03-23
 */

package org.mars_sim.msp.simulation.vehicle;

import java.io.Serializable;

import org.mars_sim.msp.simulation.Mars;
import org.mars_sim.msp.simulation.Resource;
import org.mars_sim.msp.simulation.structure.Settlement;

/**
 * The ExplorerRover class is a rover designed for exploration and collecting
 * rock samples.
 */
public class ExplorerRover extends Rover implements Medical, Serializable {

	private static final String EXPLORER_ROVER = "Explorer Rover";

    // Data members
    private MobileLaboratory lab; // The rover's lab.
    private SickBay sickBay; // The rover's sick bay.
    
    /** 
     * Constructs an ExplorerRover object at a given settlement.
     * @param name the name of the rover
     * @param settlement the settlementt the rover is parked at
     * @param mars the mars instance
     * @throws Exception if rover could not be constructed.
     */
    public ExplorerRover(String name, Settlement settlement, Mars mars) throws Exception {
        // Use the Rover constructor
	    super(name, settlement, mars);

	    initExplorerRoverData();

	    // Add EVA Suits
	    addEVASuits();
    }

    /**
     * Initialize rover data
     * @throws exception if rover data cannot be initialized.
     */
    private void initExplorerRoverData() throws Exception {
    	
    	VehicleConfig config = mars.getSimulationConfiguration().getVehicleConfiguration();
    	   
        // Set the description.
        description = "Explorer Rover";
       
        // Add scope to malfunction manager.
	    malfunctionManager.addScopeString("ExplorerRover");
	    malfunctionManager.addScopeString("Laboratory");
	    
		// Set base speed to 30kph.
		setBaseSpeed(config.getBaseSpeed(description));

		// Set the empty mass of the rover.
		baseMass = config.getEmptyMass(description);
	    
        // Set the operating range of rover.
        range = config.getRange(description);
        
        // Set crew capacity
	    crewCapacity = config.getCrewSize(description);

        // Set inventory total mass capacity.
        inventory.setTotalCapacity(config.getTotalCapacity(description));
	
        // Set inventory resource capacities.
        inventory.setResourceCapacity(Resource.METHANE, config.getCargoCapacity(description, Resource.METHANE));
        inventory.setResourceCapacity(Resource.OXYGEN, config.getCargoCapacity(description, Resource.OXYGEN));
        inventory.setResourceCapacity(Resource.WATER, config.getCargoCapacity(description, Resource.WATER));
        inventory.setResourceCapacity(Resource.FOOD, config.getCargoCapacity(description, Resource.FOOD));
        inventory.setResourceCapacity(Resource.ROCK_SAMPLES, config.getCargoCapacity(description, Resource.ROCK_SAMPLES));
        inventory.setResourceCapacity(Resource.ICE, config.getCargoCapacity(description, Resource.ICE));
        
	    // Construct mobile lab.
	    lab = new MobileLaboratory(1, config.getLabTechLevel(description), config.getLabTechSpecialities(description));
        
        // Construct sick bay.
        sickBay = new SickBay(this, config.getSickbayTechLevel(description), config.getSickbayBeds(description));
    }

    /**
     * Gets the explorer rover's lab.
     * @return laboratory
     */
    public MobileLaboratory getLab() {
        return lab; 
    }
	/**
	 * @see org.mars_sim.msp.simulation.vehicle.Medical#getSickBay()
	 */
	public SickBay getSickBay() {
		return sickBay;
	}
}
/**
 * Mars Simulation Project
 * TransportRover.java
 * @version 2.75 2004-03-23
 */

package org.mars_sim.msp.simulation.vehicle;

import java.io.Serializable;

import org.mars_sim.msp.simulation.Mars;
import org.mars_sim.msp.simulation.Resource;
import org.mars_sim.msp.simulation.structure.Settlement;

/**
 * The TransportRover class is a rover designed for transporting people
 * from settlement to settlement.
 */
public class TransportRover extends Rover implements Medical, Serializable {

    private SickBay sickBay = null;

    /**
     * Constructs an TransportRover object at a given settlement.
     * @param name the name of the rover
     * @param settlement the settlementt he rover is parked at
     * @param mars the mars instance
     * @throws Exception if rover could not be constructed.
     */
    public TransportRover(String name, Settlement settlement, Mars mars) throws Exception {
        // Use the Rover constructor
        super(name, settlement, mars);

        initTransportRoverData();

        // Add EVA suits
        addEVASuits();
    }

    /**
     * Initialize rover data
     * @throws Exception if rover data can not be initialized.
     */
    private void initTransportRoverData() throws Exception {

		VehicleConfig config = mars.getSimulationConfiguration().getVehicleConfiguration();

        // Set the description.
        description = "Transport Rover";
        
        // Add scope to malfunction manager.
	    malfunctionManager.addScopeString("TransportRover");
	    
		// Set base speed to 30kph.
		setBaseSpeed(config.getBaseSpeed(description));

		// Set the empty mass of the rover.
		baseMass = config.getEmptyMass(description);
	    
        // Set operating range of rover.
		range = config.getRange(description);
        
        // Set crew capacity
		crewCapacity = config.getCrewSize(description);

        // Set the cargo capacity of rover.
		inventory.setTotalCapacity(config.getTotalCapacity(description));
	
	    // Set resource capacities of rover
		inventory.setResourceCapacity(Resource.METHANE, config.getCargoCapacity(description, Resource.METHANE));
		inventory.setResourceCapacity(Resource.OXYGEN, config.getCargoCapacity(description, Resource.OXYGEN));
		inventory.setResourceCapacity(Resource.WATER, config.getCargoCapacity(description, Resource.WATER));
		inventory.setResourceCapacity(Resource.FOOD, config.getCargoCapacity(description, Resource.FOOD));

		sickBay = new SickBay(this, config.getSickbayTechLevel(description), config.getSickbayBeds(description));
    }

	/**
	 * @see org.mars_sim.msp.simulation.vehicle.Medical#getSickBay()
	 */
	public SickBay getSickBay() {
		return sickBay;
	}
}
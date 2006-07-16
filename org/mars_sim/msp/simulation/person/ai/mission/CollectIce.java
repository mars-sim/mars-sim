/**
 * Mars Simulation Project
 * CollectIce.java
 * @version 2.78 05-08-18
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.mission;

import org.mars_sim.msp.simulation.InventoryException;
import org.mars_sim.msp.simulation.equipment.Bag;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.job.Job;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.structure.Settlement;

/** 
 * The Exploration class is a mission to travel in a rover to several
 * random locations around a settlement and collect ice.
 */
public class CollectIce extends CollectResourcesMission {

	// Amount of ice to be gathered at a given site (kg). 
	private static final double SITE_GOAL = 1000D;
	
	// Number of bags required for the mission. 
	private static final int REQUIRED_BAGS = 20;
	
	// Collection rate of ice during EVA (kg/millisol).
	private static final double COLLECTION_RATE = 1D;
	
	// Number of collection sites.
	private static final int NUM_SITES = 1;
	
	// Minimum number of people to do mission.
	private final static int MIN_PEOPLE = 2;

	/**
	 * Constructor
	 * @param startingPerson the person starting the mission.
	 * @throws MissionException if problem constructing mission.
	 */
	public CollectIce(Person startingPerson) throws MissionException {
		
		// Use CollectResourcesMission constructor.
		super("Ice Prospecting", startingPerson, AmountResource.ICE, SITE_GOAL, COLLECTION_RATE, 
				Bag.class, REQUIRED_BAGS, NUM_SITES, MIN_PEOPLE);
	}
	
	/** 
	 * Gets the weighted probability that a given person would start this mission.
	 * @param person the given person
	 * @return the weighted probability
	 */
	public static double getNewMissionProbability(Person person) {

		double result = 0D;

		if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
			Settlement settlement = person.getSettlement();
	    
			// Check if a mission-capable rover is available.
			boolean reservableRover = areVehiclesAvailable(settlement);

			// Check if at least one person left to hold down the fort.
			boolean remainingInhabitant = atLeastOnePersonRemainingAtSettlement(settlement, person);
			
			// Check if there are enough bags at the settlement for collecting ice.
			boolean enoughBags = (numCollectingContainersAvailable(settlement, Bag.class) >= REQUIRED_BAGS);
	    
			if (reservableRover && remainingInhabitant && enoughBags) {
				// Calculate the probability based on the water situation.
				try {
					double water = settlement.getInventory().getAmountResourceStored(AmountResource.WATER);
					double amountNeeded = settlement.getAllAssociatedPeople().size() * 250D;
					if (water > 0D) result = 100D * (amountNeeded / water);
					else result = 100D * (amountNeeded / 1D);
				}
				catch (InventoryException e) {
					e.printStackTrace(System.err);
				}
			} 
			
			// Crowding modifier
			int crowding = settlement.getCurrentPopulationNum() - settlement.getPopulationCapacity();
			if (crowding > 0) result *= (crowding + 1);	
			
			// Job modifier.
			Job job = person.getMind().getJob();
			if (job != null) result *= job.getStartMissionProbabilityModifier(CollectIce.class);			
		}
        
		return result;
	}
	
    /**
     * Gets the description of a collection site.
     * @param siteNum the number of the site.
     * @return description
     */
    protected String getCollectionSiteDescription(int siteNum) {
    	return "prospecting site";
    }
}
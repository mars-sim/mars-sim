/**
 * Mars Simulation Project
 * Exploration.java
 * @version 2.78 2005-08-18
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.mission;

import org.mars_sim.msp.simulation.equipment.SpecimenContainer;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.job.Job;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.vehicle.Rover;
import org.mars_sim.msp.simulation.vehicle.Vehicle;


/** 
 * The Exploration class is a mission to travel in a rover to several
 * random locations around a settlement and collect rock samples.
 */
public class Exploration extends CollectResourcesMission {

	// Amount of rock samples to be gathered at a given site (kg). 
	private static final double SITE_GOAL = 40D;
	
	// Collection rate of rock samples during EVA (kg/millisol).
	private static final double COLLECTION_RATE = .1D;
	
	// Number of specimen containers required for the mission. 
	private static final int REQUIRED_SPECIMEN_CONTAINERS = 20;
	
	//	Number of collection sites.
	private static final int NUM_SITES = 5;
	
	// Minimum number of people to do mission.
	private final static int MIN_PEOPLE = 2;

	/**
	 * Constructor
	 * @param startingPerson the person starting the mission.
	 * @throws MissionException if problem constructing mission.
	 */
	public Exploration(Person startingPerson) throws MissionException {
		
		// Use CollectResourcesMission constructor.
		super("Exploration", startingPerson, AmountResource.ROCK_SAMPLES, 
			SITE_GOAL, COLLECTION_RATE, SpecimenContainer.class, 
			REQUIRED_SPECIMEN_CONTAINERS, NUM_SITES, MIN_PEOPLE);
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
			
			// Check if minimum number of people are available at the settlement.
			// Plus one to hold down the fort.
			boolean minNum = minAvailablePeopleAtSettlement(settlement, (MIN_PEOPLE + 1));
			
			// Check if there are enough specimen containers at the settlement for collecting rock samples.
			boolean enoughContainers = 
				(numCollectingContainersAvailable(settlement, SpecimenContainer.class) >= REQUIRED_SPECIMEN_CONTAINERS);
	    
			if (reservableRover && minNum && enoughContainers) result = 5D;
			
			// Crowding modifier
			int crowding = settlement.getCurrentPopulationNum() - settlement.getPopulationCapacity();
			if (crowding > 0) result *= (crowding + 1);		
			
			// Job modifier.
			Job job = person.getMind().getJob();
			if (job != null) result *= job.getStartMissionProbabilityModifier(Exploration.class);	
		}
        
		return result;
	}
	
	/**
	 * Compares the quality of two vehicles for use in this mission.
	 * (This method should be added to by children)
	 * @param firstVehicle the first vehicle to compare
	 * @param secondVehicle the second vehicle to compare
	 * @return -1 if the second vehicle is better than the first vehicle, 
	 * 0 if vehicle are equal in quality,
	 * and 1 if the first vehicle is better than the second vehicle.
	 * @throws Exception if problem comparing vehicles..
	 */
	protected int compareVehicles(Vehicle firstVehicle, Vehicle secondVehicle) throws Exception {
		int result = super.compareVehicles(firstVehicle, secondVehicle);
		
		// Check of one rover has a research lab and the other one doesn't.
		if ((result == 0) && (isUsableVehicle(firstVehicle)) && (isUsableVehicle(secondVehicle))) {
			boolean firstLab = ((Rover) firstVehicle).hasLab();
			boolean secondLab = ((Rover) secondVehicle).hasLab();
			if (firstLab && !secondLab) result = 1;
			else if (!firstLab && secondLab) result = -1;
		}
		
		return result;
	}
    
    /**
     * Gets the estimated time spent at a collection site.
     * @param useBuffer use time buffers in estimation if true.
     * @return time (millisols)
     */
    protected double getEstimatedTimeAtCollectionSite(boolean useBuffer) {
    	double result = super.getEstimatedTimeAtCollectionSite(useBuffer);
    	
    	// TODO: Add additional exploration time at sites.
    	
    	return result;
    }
    
    /**
     * Gets the description of a collection site.
     * @param siteNum the number of the site.
     * @return description
     */
    protected String getCollectionSiteDescription(int siteNum) {
    	return "exploration site " + siteNum;
    }
}
/**
 * Mars Simulation Project
 * Exploration.java
 * @version 2.81 2007-08-12
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.mission;

import java.util.List;

import org.mars_sim.msp.simulation.equipment.SpecimenContainer;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.vehicle.Rover;
import org.mars_sim.msp.simulation.vehicle.Vehicle;


/** 
 * The Exploration class is a mission to travel in a rover to several
 * random locations around a settlement and collect rock samples.
 */
public class Exploration extends CollectResourcesMission {

	// Default description.
	public static final String DEFAULT_DESCRIPTION = "Exploration";
	
	// Amount of rock samples to be gathered at a given site (kg). 
	public static final double SITE_GOAL = 40D;
	
	// Collection rate of rock samples during EVA (kg/millisol).
	public static final double COLLECTION_RATE = .1D;
	
	// Number of specimen containers required for the mission. 
	public static final int REQUIRED_SPECIMEN_CONTAINERS = 20;
	
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
		super(DEFAULT_DESCRIPTION, startingPerson, AmountResource.ROCK_SAMPLES, 
			SITE_GOAL, COLLECTION_RATE, SpecimenContainer.class, 
			REQUIRED_SPECIMEN_CONTAINERS, NUM_SITES, MIN_PEOPLE);
	}
	
    /**
     * Constructor with explicit data.
     * @param members collection of mission members.
     * @param startingSettlement the starting settlement.
     * @param explorationSites the sites to explore.
     * @param rover the rover to use.
     * @param description the mission's description.
     * @throws MissionException if error constructing mission.
     */
    public Exploration(PersonCollection members, Settlement startingSettlement, 
    		List explorationSites, Rover rover, String description) throws MissionException {
    	
       	// Use CollectResourcesMission constructor.
    	super(description, members, startingSettlement, AmountResource.ROCK_SAMPLES, 
    			SITE_GOAL, COLLECTION_RATE, SpecimenContainer.class, REQUIRED_SPECIMEN_CONTAINERS, 
    			explorationSites.size(), 1, rover, explorationSites);
    }

	/** 
	 * Gets the weighted probability that a given person would start this mission.
	 * @param person the given person
	 * @return the weighted probability
	 */
	public static double getNewMissionProbability(Person person) {

		double result =  CollectResourcesMission.getNewMissionProbability(person, SpecimenContainer.class, 
				REQUIRED_SPECIMEN_CONTAINERS, MIN_PEOPLE, Exploration.class);
		
		if (result > 0D) {
			// Check if min number of EVA suits at settlement.
			if (VehicleMission.getNumberAvailableEVASuitsAtSettlement(person.getSettlement()) < MIN_PEOPLE) result = 0D;
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
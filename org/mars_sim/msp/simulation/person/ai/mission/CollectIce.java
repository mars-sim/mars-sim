/**
 * Mars Simulation Project
 * CollectIce.java
 * @version 2.75 04-03-24
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.mission;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.ai.task.ReserveRover;
import org.mars_sim.msp.simulation.structure.Settlement;

/** 
 * The Exploration class is a mission to travel in a rover to several
 * random locations around a settlement and collect ice.
 */
public class CollectIce extends CollectResourcesMission {

	//	Amount of ice to be gathered at a given site (kg). 
	private static final double SITE_GOAL = 2000D;
	
	// Collection rate of ice during EVA (kg/millisol).
	private static final double COLLECTION_RATE = 1D;
	
	// Number of collection sites.
	private static final int NUM_SITES = 1;

	public CollectIce(MissionManager missionManager, Person startingPerson) {
		
		// Use CollectResourcesMission constructor.
		super("Ice Prospecting", missionManager, startingPerson, Resource.ICE, 
			SITE_GOAL, COLLECTION_RATE, NUM_SITES);
	}
	
	/** 
	 * Gets the weighted probability that a given person would start this mission.
	 * @param person the given person
	 * @param mars the virtual Mars
	 * @return the weighted probability
	 */
	public static double getNewMissionProbability(Person person, Mars mars) {

		double result = 0D;

		if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
			Settlement settlement = person.getSettlement();
            
			boolean darkArea = mars.getSurfaceFeatures().inDarkPolarRegion(person.getCoordinates());
	    
			boolean reservableRover = ReserveRover.availableRovers(ReserveRover.EXPLORER_ROVER, settlement);

			double water = settlement.getInventory().getResourceMass(Resource.WATER);
			boolean enoughWater = (water >= 5000D);

			boolean minSettlementPop = (settlement.getCurrentPopulationNum() == 1);
	    
			if (!darkArea && reservableRover && !minSettlementPop) {
				if (enoughWater) result = .5D;
				else result = 100D;
			} 
		}
        
		return result;
	}
	
	/** 
	 * Gets the weighted probability that a given person join this mission.
	 * @param person the given person
	 * @return the weighted probability
	 */
	public double getJoiningProbability(Person person) {

		double result = 0D;

		if ((phase.equals(EMBARK)) && !hasPerson(person)) {
			if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
				if (person.getSettlement() == startingSettlement) {
					if (people.size() < missionCapacity) {
						if (people.size() < person.getSettlement().getCurrentPopulationNum() - 1) 	
							result = 50D;
					}
				}
			}
		}

		return result;
	}	
}
/**
 * Mars Simulation Project
 * Exploration.java
 * @version 2.75 04-01-12
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.mission;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.ai.task.ReserveRover;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.vehicle.ExplorerRover;

/** 
 * The Exploration class is a mission to travel in a rover to several
 * random locations around a settlement and collect rock samples.
 */
public class Exploration extends CollectResourcesMission {

	// Amount of rock samples to be gathered at a given site (kg). 
	private static final double SITE_GOAL = 200D;
	
	// Collection rate of rock samples during EVA (kg/millisol).
	private static final double COLLECTION_RATE = .1D;

	public Exploration(MissionManager missionManager, Person startingPerson) {
		
		// Use CollectResourcesMission constructor.
		super("Exploration", missionManager, startingPerson, Resource.ROCK_SAMPLES, 
			SITE_GOAL, COLLECTION_RATE);
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
	    
			boolean reservableRover = ReserveRover.availableRovers(ExplorerRover.class, settlement);

			double rocks = settlement.getInventory().getResourceMass(Resource.ROCK_SAMPLES);
			boolean enoughRockSamples = (rocks >= 500D);

			boolean minSettlementPop = (settlement.getCurrentPopulationNum() == 1);
	    
			if (!darkArea && reservableRover && !enoughRockSamples && !minSettlementPop) result = 5D;
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
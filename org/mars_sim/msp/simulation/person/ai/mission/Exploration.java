/**
 * Mars Simulation Project
 * Exploration.java
 * @version 2.76 04-06-08
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.mission;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.task.ReserveRover;
import org.mars_sim.msp.simulation.structure.Settlement;

/** 
 * The Exploration class is a mission to travel in a rover to several
 * random locations around a settlement and collect rock samples.
 */
public class Exploration extends CollectResourcesMission {

	// Amount of rock samples to be gathered at a given site (kg). 
	private static final double SITE_GOAL = 200D;
	
	// Collection rate of rock samples during EVA (kg/millisol).
	private static final double COLLECTION_RATE = .1D;
	
	//	Number of collection sites.
	private static final int NUM_SITES = 8;
	
	// Minimum number of people to do mission.
	private final static int MIN_PEOPLE = 2;

	public Exploration(MissionManager missionManager, Person startingPerson) {
		
		// Use CollectResourcesMission constructor.
		super("Exploration", missionManager, startingPerson, Resource.ROCK_SAMPLES, 
			SITE_GOAL, COLLECTION_RATE, NUM_SITES, MIN_PEOPLE);
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
	    
			boolean reservableRover = ReserveRover.availableRovers(Resource.ROCK_SAMPLES, SITE_GOAL, settlement);

			double rocks = settlement.getInventory().getResourceMass(Resource.ROCK_SAMPLES);
			boolean enoughRockSamples = (rocks >= 500D);

			// At least one person left to hold down the fort.
			boolean remainingInhabitant = false;
			PersonIterator i = settlement.getInhabitants().iterator();
			while (i.hasNext()) {
				Person inhabitant = i.next();
				if (!inhabitant.getMind().hasActiveMission() && (inhabitant != person)) 
					remainingInhabitant = true;
			}
	    
			if (reservableRover && !enoughRockSamples && remainingInhabitant) result = 5D;
			
			// Crowding modifier
			int crowding = settlement.getCurrentPopulationNum() - settlement.getPopulationCapacity();
			if (crowding > 0) result *= (crowding + 1);		
			
			// Job modifier.
			result *= person.getMind().getJob().getStartMissionProbabilityModifier(Exploration.class);	
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
				
				Settlement settlement = person.getSettlement();
				
				// Person is at mission starting settlement.
				boolean inStartingSettlement = (person.getSettlement() == startingSettlement);
				
				// Mission still has room for another person.
				boolean withinMissionCapacity = (people.size() < missionCapacity);
				
				// At least one person left to hold down the fort.
				boolean remainingInhabitant = false;
				PersonIterator i = settlement.getInhabitants().iterator();
				while (i.hasNext()) {
					Person inhabitant = i.next();
					if (!inhabitant.getMind().hasActiveMission() && (inhabitant != person)) 
						remainingInhabitant = true;
				}
				
				if (inStartingSettlement && withinMissionCapacity && remainingInhabitant) 
					result = 50D;
				
				// Crowding modifier.
				int crowding = settlement.getCurrentPopulationNum() - settlement.getPopulationCapacity();
				if (crowding > 0) result *= (crowding + 1);
				
				// Job modifier.
				result *= person.getMind().getJob().getJoinMissionProbabilityModifier(Exploration.class);				
			}
		}

		return result;
	}	
}
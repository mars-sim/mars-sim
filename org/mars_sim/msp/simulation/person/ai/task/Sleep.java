/**
 * Mars Simulation Project
 * Sleep.java
 * @version 2.76 2004-05-04
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.Mars;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.*;

/** The Sleep class is a task for sleeping.
 *  The duration of the task is by default chosen randomly, between 250 - 350 millisols.
 *  Note: Sleeping reduces fatigue.
 */
class Sleep extends Task implements Serializable {

	// Static members
	private static final double STRESS_MODIFIER = -.3D; // The stress modified per millisol.

    // Data members
    private double duration; // The duration of task in millisols
    private LivingAccommodations accommodations; // The living accommodations if any.

    /** Constructs a Sleep object
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     */
    public Sleep(Person person, Mars mars) {
        super("Sleeping", person, false, false, STRESS_MODIFIER, mars);

        // If person is in a settlement, try to find a living accommodations building.
        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
        	try {
        		Building quarters = getAvailableLivingQuartersBuilding(person);
        		if (quarters != null) {
					BuildingManager.addPersonToBuilding(person, quarters); 
        			accommodations = (LivingAccommodations) quarters.getFunction(LivingAccommodations.NAME);
        			accommodations.addSleeper();
        		}
        	}
        	catch (BuildingException e){
        		System.err.println("Sleep.constructor(): " + e.getMessage());
        		endTask();
        	}
        }
        
        duration = 250D + RandomUtil.getRandomInt(100);
    }

    /** Returns the weighted probability that a person might perform this task.
     *  Returns 25 if person's fatigue is over 750, more if fatigue is much higher.
     *  Returns an additional 50 if it is night time.
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person, Mars mars) {
        double result = 0D;

		// Fatigue modifier.
		double fatigue = person.getPhysicalCondition().getFatigue();
        if (fatigue > 500D) result = (fatigue - 500D) / 10D;
        
        // Dark outside modifier.
		if (mars.getSurfaceFeatures().getSurfaceSunlight(person.getCoordinates()) == 0)
			result *= 2D;
        
        // Crowding modifier.
        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
        	try {
        		Building building = getAvailableLivingQuartersBuilding(person);
        		Task.getCrowdingProbabilityModifier(person, building);
        	}
        	catch (BuildingException e) {
        		System.err.println("Sleep.getProbability(): " + e.getMessage());
        	}
        }

        return result;
    }

    /** 
     * This task allows the person to sleep for the duration.
     * @param time the amount of time to perform this task (in millisols)
     * @return amount of time remaining after finishing with task (in millisols)
     * @throws Exception if error performing task.
     */
    double performTask(double time) throws Exception {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

		double newFatigue = person.getPhysicalCondition().getFatigue() - (time * 10D);
		if (newFatigue < 0D) newFatigue = 0D;
        person.getPhysicalCondition().setFatigue(newFatigue);
        timeCompleted += time;
        if (timeCompleted > duration) {
            endTask();
            return timeCompleted - duration;
        }
        else return 0;
    }
    
	/**
	 * Ends the task and performs any final actions.
	 */
	public void endTask() {
		super.endTask();
		
		// Remove person from living accommodations bed so others can use it.
		try {
			if (accommodations != null) accommodations.removeSleeper();
		}
		catch(BuildingException e) {}
	}
    
	/**
	 * Gets an available living accommodations building that the person can use.
	 * Returns null if no living accommodations building is currently available.
	 *
	 * @param person the person
	 * @return available living accommodations building
	 * @throws BuildingException if error finding living accommodations building.
	 */
	private static Building getAvailableLivingQuartersBuilding(Person person) throws BuildingException {
     
		Building result = null;
        
		if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
			BuildingManager manager = person.getSettlement().getBuildingManager();
			List quartersBuildings = manager.getBuildings(LivingAccommodations.NAME);
			quartersBuildings = BuildingManager.getNonMalfunctioningBuildings(quartersBuildings);
			quartersBuildings = getQuartersWithEmptyBeds(quartersBuildings);
			quartersBuildings = BuildingManager.getLeastCrowdedBuildings(quartersBuildings);
        	
			if (quartersBuildings.size() > 0) {
				// Pick random recreation building from list.
				int rand = RandomUtil.getRandomInt(quartersBuildings.size() - 1);
				result = (Building) quartersBuildings.get(rand);
			}
		}
        
		return result;
	}
	
	/**
	 * Gets living accommodations with empty beds from a list of buildings with the living accommodations function.
	 * @param buildingList list of buildings with the living accommodations function.
	 * @return list of buildings with empty beds.
	 * @throws BuildingException if any buildings in list don't have the living accommodations function.
	 */
	private static List getQuartersWithEmptyBeds(List buildingList) throws BuildingException {
		List result = new ArrayList();
		
		Iterator i = buildingList.iterator();
		while (i.hasNext()) {
			Building building = (Building) i.next();
			LivingAccommodations quarters = (LivingAccommodations) building.getFunction(LivingAccommodations.NAME);
			if (quarters.getSleepers() < quarters.getBeds()) result.add(building);
		}
		
		return result;
	}
}
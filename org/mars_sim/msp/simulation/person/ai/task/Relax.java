/**
 * Mars Simulation Project
 * Relax.java
 * @version 2.77 2004-09-09
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.*;

/** The Relax class is a simple task that implements resting and doing nothing for a while.
 *  The duration of the task is by default chosen randomly, up to 100 millisols.
 *
 *  Note: Mental stress may be added later, which this task could be used to reduce.
 */
class Relax extends Task implements Serializable {

	// Static members
	private static final double STRESS_MODIFIER = -.5D; // The stress modified per millisol.

    // Data members
    private double duration; // The predetermined duration of task in millisols

    /** 
     * Constructor
     * @param person the person to perform the task
     */
    public Relax(Person person) {
        super("Relaxing", person, false, false, STRESS_MODIFIER);

        // If person is in a settlement, try to find a place to relax.
        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
        	
        	try {
        	Building recBuilding = getAvailableRecreationBuilding(person);
        	if (recBuilding != null) BuildingManager.addPersonToBuilding(person, recBuilding);
        	}
        	catch (Exception e) {
        		System.err.println("Relax.constructor(): " + e.getMessage());
        		endTask();
        	}
        }
        
        duration = RandomUtil.getRandomInt(100);
    }

    /** Returns the weighted probability that a person might perform this task.
     *  It should return a 0 if there is no chance to perform this task given the person and his/her situation.
     *  @param person the person to perform the task
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {
    	double result = 10D;
    	
    	// Stress modifier
    	result += person.getPhysicalCondition().getStress();
    	
    	// Crowding modifier
    	if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
    		try {
    			Building recBuilding = getAvailableRecreationBuilding(person);
    			if (recBuilding != null) {
    				result *= Task.getCrowdingProbabilityModifier(person, recBuilding);
					result *= Task.getRelationshipModifier(person, recBuilding);
    			}
    		}
    		catch (Exception e) {
    			System.err.println("Relax.getProbability(): " + e.getMessage());
    		}
    	}
        
        return result;
    }

    /** 
     * This task simply waits until the set duration of the task is complete, then ends the task.
     * @param time the amount of time to perform this task (in millisols)
     * @return amount of time remaining after finishing with task (in millisols)
     * @throws Exception if error performing task.
     */
    double performTask(double time) throws Exception {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

        timeCompleted += time;
        if (timeCompleted > duration) {
            endTask();
            return timeCompleted - duration;
        }
        else return 0;
    }
    
	/**
	 * Gets an available recreation building that the person can use.
	 * Returns null if no recreation building is currently available.
	 *
	 * @param person the person
	 * @return available recreation building
	 * @throws BuildingException if error finding recreation building.
	 */
	private static Building getAvailableRecreationBuilding(Person person) throws BuildingException {
     
		Building result = null;
        
		if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
			BuildingManager manager = person.getSettlement().getBuildingManager();
			List recreationBuildings = manager.getBuildings(Recreation.NAME);
			recreationBuildings = BuildingManager.getNonMalfunctioningBuildings(recreationBuildings);
			recreationBuildings = BuildingManager.getLeastCrowdedBuildings(recreationBuildings);
			recreationBuildings = BuildingManager.getBestRelationshipBuildings(person, recreationBuildings);
        	
			if (recreationBuildings.size() > 0) result = (Building) recreationBuildings.get(0);
		}
        
		return result;
	}
	
	/**
	 * Gets the effective skill level a person has at this task.
	 * @return effective skill level
	 */
	public int getEffectiveSkillLevel() {
		return 0;	
	}
	
	/**
	 * Gets a list of the skills associated with this task.
	 * May be empty list if no associated skills.
	 * @return list of skills as strings
	 */
	public List getAssociatedSkills() {
		List results = new ArrayList();
		return results;
	}
}
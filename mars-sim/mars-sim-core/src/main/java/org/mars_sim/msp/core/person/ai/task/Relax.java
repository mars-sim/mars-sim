/**
 * Mars Simulation Project
 * Relax.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.Recreation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** The Relax class is a simple task that implements resting and doing nothing for a while.
 *  The duration of the task is by default chosen randomly, up to 100 millisols.
 */
class Relax extends Task implements Serializable {
    
    private static String CLASS_NAME = "org.mars_sim.msp.simulation.person.ai.task.Task";
	
    private static Logger logger = Logger.getLogger(CLASS_NAME);
	
	// Task phase
	private static final String RELAXING = "Relaxing";

	// Static members
	private static final double STRESS_MODIFIER = -.5D; // The stress modified per millisol.

    /** 
     * Constructor
     * @param person the person to perform the task
     * @throws Exception if error constructing task.
     */
    public Relax(Person person) {
        super("Relaxing", person, false, false, STRESS_MODIFIER, true, RandomUtil.getRandomInt(100));

        // If person is in a settlement, try to find a place to relax.
        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {      	
        	try {
        		Building recBuilding = getAvailableRecreationBuilding(person);
        		if (recBuilding != null) BuildingManager.addPersonToBuilding(person, recBuilding);
        	}
        	catch (Exception e) {
        		logger.log(Level.SEVERE,"Relax.constructor(): " + e.getMessage());
        		endTask();
        	}
        }
        
        // Initialize phase
        addPhase(RELAXING);
        setPhase(RELAXING);
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
    			logger.log(Level.SEVERE,"Relax.getProbability(): " + e.getMessage());
    		}
    	}
        
        return result;
    }

    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time (millisol) the phase is to be performed.
     * @return the remaining time (millisol) after the phase has been performed.
     * @throws Exception if error in performing phase or if phase cannot be found.
     */
    protected double performMappedPhase(double time) {
    	if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
    	if (RELAXING.equals(getPhase())) return relaxingPhase(time);
    	else return time;
    }
    
    /**
     * Performs the relaxing phase of the task.
     * @param time the amount of time (millisol) to perform the phase.
     * @return the amount of time (millisol) left after performing the phase.
     * @throws Exception if error performing the phase.
     */
    private double relaxingPhase(double time) {
    	// Do nothing
        return 0D; 
    }
    
	/**
	 * Adds experience to the person's skills used in this task.
	 * @param time the amount of time (ms) the person performed this task.
	 */
	protected void addExperience(double time) {
		// This task adds no experience.
	}
    
	/**
	 * Gets an available recreation building that the person can use.
	 * Returns null if no recreation building is currently available.
	 *
	 * @param person the person
	 * @return available recreation building
	 * @throws BuildingException if error finding recreation building.
	 */
	private static Building getAvailableRecreationBuilding(Person person) {
     
		Building result = null;
        
		if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
			BuildingManager manager = person.getSettlement().getBuildingManager();
			List<Building> recreationBuildings = manager.getBuildings(Recreation.NAME);
			recreationBuildings = BuildingManager.getNonMalfunctioningBuildings(recreationBuildings);
			recreationBuildings = BuildingManager.getLeastCrowdedBuildings(recreationBuildings);
			recreationBuildings = BuildingManager.getBestRelationshipBuildings(person, recreationBuildings);
        	
			if (recreationBuildings.size() > 0) result = recreationBuildings.get(0);
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
	public List<String> getAssociatedSkills() {
		List<String> results = new ArrayList<String>(0);
		return results;
	}
}
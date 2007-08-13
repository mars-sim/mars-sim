/**
 * Mars Simulation Project
 * Workout.java
 * @version 2.81 2007-08-12
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.*;

/** 
 * The Workout class is a task for working out in an exercise facility.
 */
public class Workout extends Task implements Serializable {
	
	// Task phase
	private static final String EXERCISING = "Exercising";

	// Static members
	private static final double STRESS_MODIFIER = -1D; // The stress modified per millisol.

	// Data members
	private Exercise gym; // The exercise building the person is using.
	
	/**
	 * Constructor
	 * This is an effort-driven task.
	 * @param person the person performing the task.
	 * @throws Exception if error constructing task.
	 */
	public Workout(Person person) throws Exception {
		// Use Task constructor.
		super("Exercise", person, true, false, STRESS_MODIFIER, true, 50D + RandomUtil.getRandomInt(100));
		
		if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
			try {
				// If person is in a settlement, try to find a gym.
				Building gymBuilding = getAvailableGym(person);
				if (gymBuilding != null) {
					BuildingManager.addPersonToBuilding(person, gymBuilding);
					gym = (Exercise) gymBuilding.getFunction(Exercise.NAME);
				}
				else endTask();
			}
			catch (BuildingException e) {
				System.err.println("Workout.constructor(): " + e.getMessage());
				endTask();
			}
		}
		else endTask();
		
		// Initialize phase
		addPhase(EXERCISING);
		setPhase(EXERCISING);
	}
	
	/** 
	 * Returns the weighted probability that a person might perform this task.
	 * It should return a 0 if there is no chance to perform this task given the person and his/her situation.
	 * @param person the person to perform the task
	 * @return the weighted probability that a person might perform this task
	 */
	public static double getProbability(Person person) {
		
		double result = 0D;
		
		if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
			// Probability affected by the person's stress and fatigue.
			PhysicalCondition condition = person.getPhysicalCondition();
			result = condition.getStress() - (condition.getFatigue() / 10D) + 20D;
			if (result < 0D) result = 0D;
			
			try {
				// Get an available gym.
				Building building = getAvailableGym(person);
				if (building != null) {
					result *= Task.getCrowdingProbabilityModifier(person, building);
					result *= Task.getRelationshipModifier(person, building);
				}
				else result = 0D;
			}
			catch (BuildingException e) {
				System.err.println("Workout.getProbability(): " + e.getMessage());
			}
		}
		
		// Effort-driven task modifier.
		result *= person.getPerformanceRating();
		
		return result;
	}
	
    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time (millisol) the phase is to be performed.
     * @return the remaining time (millisol) after the phase has been performed.
     * @throws Exception if error in performing phase or if phase cannot be found.
     */
    protected double performMappedPhase(double time) throws Exception {
    	if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
    	if (EXERCISING.equals(getPhase())) return exercisingPhase(time);
    	else return time;
    }
    
    /**
     * Performs the exercising phase.
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     * @throws Exception if error performing the phase.
     */
    private double exercisingPhase(double time) throws Exception {
    	
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
	 * Ends the task and performs any final actions.
	 */
	public void endTask() {
		super.endTask();
		
		// Remove person from exercise function so others can use it.
		try {
			if (gym != null) gym.removeExerciser();
		}
		catch(BuildingException e) {}
	}
	
	/**
	 * Gets an available building with the exercise function.
	 * @param person the person looking for the gym.
	 * @return an available exercise building or null if none found.
	 * @throws BuildingException if error finding gym building.
	 */
	private static Building getAvailableGym(Person person) throws BuildingException {
		Building result = null;
		
		// If person is in a settlement, try to find a building with a gym.	
		if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
			BuildingManager buildingManager = person.getSettlement().getBuildingManager();
			List gyms = buildingManager.getBuildings(Exercise.NAME);
			gyms = BuildingManager.getNonMalfunctioningBuildings(gyms);
			gyms = BuildingManager.getLeastCrowdedBuildings(gyms);
			gyms = BuildingManager.getBestRelationshipBuildings(person, gyms);
			
			if (gyms.size() > 0) result = (Building) gyms.get(0);
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
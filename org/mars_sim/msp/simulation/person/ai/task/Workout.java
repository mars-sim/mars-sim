/**
 * Mars Simulation Project
 * Workout.java
 * @version 2.76 2004-05-12
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.Mars;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.*;

/** 
 * The Workout class is a task for working out in an exercise facility.
 */
public class Workout extends Task implements Serializable {

	// Static members
	private static final double STRESS_MODIFIER = -1D; // The stress modified per millisol.

	// Data members
	private Exercise gym; // The exercise building the person is using.
	private double duration; // The duration (in millisols) the person will perform this task.
	
	/**
	 * Constructor
	 * This is an effort-driven task.
	 * @param person the person performing the task.
	 * @param mars the virtual Mars.
	 */
	public Workout(Person person, Mars mars) {
		// Use Task constructor.
		super("Exercise", person, true, false, STRESS_MODIFIER, mars);
		
		List gyms = getAvailableGyms(person);
		
		if (gyms.size() > 0) {
			gym = (Exercise) gyms.get(RandomUtil.getRandomInt(gyms.size() - 1));
			try {
				gym.addExerciser();
				
				// Add person to building.
				Building building = gym.getBuilding();
				LifeSupport lifeSupport = (LifeSupport) building.getFunction(LifeSupport.NAME);
				if (!lifeSupport.containsPerson(person)) lifeSupport.addPerson(person);
			}
			catch (BuildingException e) {
				System.err.println("Workout.constructor(): " + e.getMessage());
				endTask();
			}
		}
		else endTask();
		
		duration = 50D + RandomUtil.getRandomInt(100);
	}
	
	/** 
	 * Returns the weighted probability that a person might perform this task.
	 * It should return a 0 if there is no chance to perform this task given the person and his/her situation.
	 * @param person the person to perform the task
	 * @param mars the virtual Mars
	 * @return the weighted probability that a person might perform this task
	 */
	public static double getProbability(Person person, Mars mars) {
		
		double result = 0D;
		
		// Check if there are any available gyms.
		if (getAvailableGyms(person).size() > 0) {
			// Probability affected by the person's stress and fatigue.
			PhysicalCondition condition = person.getPhysicalCondition();
			result = condition.getStress() - (condition.getFatigue() / 10D) + 20D;
			if (result < 0D) result = 0D;
		} 
		
		// Effort-driven task modifier.
		result *= person.getPerformanceRating();
		
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
	 * Ends the task and performs any final actions.
	 */
	public void endTask() {
		super.endTask();
		
		// Remove person from exercise function so others can use it.
		try {
			gym.removeExerciser();
		}
		catch(BuildingException e) {}
	}
	
	/**
	 * Gets a list of buildings with an available exercise function.
	 * @param person the person looking for the gym.
	 * @return an available exercise building or null if none found.
	 */
	private static List getAvailableGyms(Person person) {
		List result = new ArrayList();
		
		// If person is in a settlement, try to find a building with a gym.	
		if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
			BuildingManager buildingManager = person.getSettlement().getBuildingManager();
			List gyms = buildingManager.getBuildings(Exercise.NAME);
			Iterator i = gyms.iterator();
			while (i.hasNext()) {
				Building building = (Building) i.next();
				try {
					Exercise gym = (Exercise) building.getFunction(Exercise.NAME);
					if (gym.getExerciserCapacity() > gym.getNumExercisers()) result.add(gym);
				}
				catch (BuildingException e) {
					System.err.println("Workout.getAvailableGyms(): " + e.getMessage());
				}
			}
		}
		
		return result;
	}
}
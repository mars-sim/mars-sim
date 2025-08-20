/*
 * Mars Simulation Project
 * Yoga.java
 * @date 2022-08-04
 * @author Sebastien Venot
 */
package com.mars_sim.core.person.ai.task;


import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.Exercise;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;

/**
 * The Yoga class is a task for practicing yoga to reduce stress.
 */
public class Yoga extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.yoga"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase DOING_YOGA = new TaskPhase(Msg.getString("Task.phase.doingYoga")); //$NON-NLS-1$

	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -.5D;

	/** The exercise building the person is using. */
	private Exercise gym;

	/**
	 * constructor.
	 * 
	 * @param person the person to perform the task
	 */
	public Yoga(Person person) {
		super(NAME, person, false, false, STRESS_MODIFIER, 10D + RandomUtil.getRandomDouble(30D));
	
		if (person.isInSettlement()) {
			// If person is in a settlement, try to find a gym.
			Building gymBuilding = BuildingManager.getAvailableFunctionTypeBuilding(person, FunctionType.EXERCISE);
			if (gymBuilding != null) {
				// Walk to gym building.
				walkToTaskSpecificActivitySpotInBuilding(gymBuilding, FunctionType.EXERCISE, false);
				gym = gymBuilding.getExercise();
				// set the boolean to true so that it won't be done again today
				person.getPreference().setTaskDue(this, true);
			}

			else
				endTask();
		}
		
		else
			endTask();

		// Initialize phase
		addPhase(DOING_YOGA);
		setPhase(DOING_YOGA);
	}


	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (DOING_YOGA.equals(getPhase())) {
			return yogaPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Does the yoga phase.
	 *
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double yogaPhase(double time) {
		// Regulates hormones
		person.getCircadianClock().exercise(time);
		//Improves musculoskeletal health
		person.getPhysicalCondition().improveMuscleHealth(time);
		// Record the sleep time [in millisols]
		person.getCircadianClock().recordExercise(time/2);
        // Reduce person's stress
		person.getPhysicalCondition().reduceStress(time/2);
		
		return 0;
	}
	
	/**
	 * Release Person from associated Gym
	 */
	@Override
	protected void clearDown() {
		// Remove person from exercise function so others can use it.
		if (gym != null && gym.getNumExercisers() > 0) {
			gym.removeExerciser();
		}
	}
}

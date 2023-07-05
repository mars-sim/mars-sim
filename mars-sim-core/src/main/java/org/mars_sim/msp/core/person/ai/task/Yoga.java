/*
 * Mars Simulation Project
 * Yoga.java
 * @date 2022-08-04
 * @author Sebastien Venot
 */
package org.mars_sim.msp.core.person.ai.task;


import org.mars.sim.tools.Msg;
import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.Exercise;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.vehicle.Rover;

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

		if (person.isInVehicle()) {
			// If person is in rover, walk to passenger activity spot.
			if (person.getVehicle() instanceof Rover) {
				walkToPassengerActivitySpotInRover((Rover) person.getVehicle(), true);
				// set the boolean to true so that it won't be done again today
				person.getPreference().setTaskDue(this, true);
			}
		} 
		
		else if (person.isInSettlement()) {
			// If person is in a settlement, try to find a gym.
			Building gymBuilding = BuildingManager.getAvailableGymBuilding(person);
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
		//Improves musculoskeletal systems
		person.getPhysicalCondition().exerciseMuscle(time);
		// Record the sleep time [in millisols]
		person.getCircadianClock().recordExercise(time);
		
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

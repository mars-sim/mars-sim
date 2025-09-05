/*
 * Mars Simulation Project
 * RecordActivity.java
 * @date 2025-07-30
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.task;


import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Rover;

/**
 * The RecordActivity class is a task for recording events/activities
 */
public class RecordActivity extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.recordActivity"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase RECORDING = new TaskPhase(Msg.getString("Task.phase.recordActivity")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -1D;

	/**
	 * Constructor. This is an effort-driven task.
	 * 
	 * @param person the person performing the task.
	 */
	public RecordActivity(Person person) {
		// Use Task constructor.
		super(NAME, person, true, false, STRESS_MODIFIER, RandomUtil.getRandomDouble(5, 50));

		if (person.isInSettlement()) {
			
			int rand = RandomUtil.getRandomInt(3);
			if (rand == 3)
				walkToRandomLocation(false);
			
			// Initialize phase
			setPhase(RECORDING);
		}

		else if (person.isInVehicle()) {
			if (person.getVehicle() instanceof Rover r) {
				walkToPassengerActivitySpotInRover(r, true);
			}
			
			// Initialize phase
			setPhase(RECORDING);
		}

		else {
			// Move to where other settlers are working outside or near a building
			endTask();
		}

	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (RECORDING.equals(getPhase())) {
			return recordingPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the recording phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double recordingPhase(double time) {
		// FUTURE: need to define what to do with this activity
		// e.g. generating contents for commercials, sponsorship request, etc.
		// Take snapshot of pics and tap video clips, etc.
		
		// 1. Choose a FunctionType to be recorded 
		//    Test if (building.hasFunction(FunctionType.ASTRONOMICAL_OBSERVATION)
		//		|| building.hasFunction(FunctionType.EARTH_RETURN)) {
		
		// 2. Walk to a spot in that building 
		//    by calling walkToRandomLocInBuilding(building, true);
		
		return 0D;
	}

	@Override
	protected void addExperience(double time) {
		// TODO: what experience to add
		double newPoints = time / 20D;
		int exp = worker.getNaturalAttributeManager().getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		int art = worker.getNaturalAttributeManager().getAttribute(NaturalAttributeType.ARTISTRY);
		newPoints += newPoints * (exp + art - 100D) / 100D;
		newPoints *= getTeachingExperienceModifier();
		worker.getSkillManager().addExperience(SkillType.REPORTING, newPoints, time);
	}

}

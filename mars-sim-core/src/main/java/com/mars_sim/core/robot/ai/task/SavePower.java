/*
 * Mars Simulation Project
 * SavePower.java
 * @date 2023-12-05
 * @author Manny Kung
 */
package com.mars_sim.core.robot.ai.task;

import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.tools.Msg;

/**
 * This task puts a robot into a power save mode to conserve the battery.
 */
public class SavePower extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	// May add back private static SimLogger logger = SimLogger.getLogger(PowerSave.class.getName())

	static final int DEFAULT_SCORE = 1;
	
	/** Simple Task name */
	public static final String SIMPLE_NAME = SavePower.class.getSimpleName();
	
	/** Task name for robot */
	public static final String NAME = Msg.getString("Task.description.savePower"); //$NON-NLS-1$
	
	public static final String END_POWER_SAVING = "Power Saving Ended";

	/** Task phases for robot. */
	private static final TaskPhase POWER_SAVING = new TaskPhase(Msg.getString("Task.phase.powerSaving")); //$NON-NLS-1$
	
	public SavePower(Robot robot) {
		super(NAME, robot, false, false, 0, 50D);
		
		// If robot is low power, leave the power save mode
		if (robot.getSystemCondition().isLowPower()) {
			
			setDescriptionDone(END_POWER_SAVING);
        	// this task has ended
			endTask();
		}
					
		walkToAssignedDutyLocation(robot, true);
		
		// Initialize phase
		addPhase(POWER_SAVING);
		setPhase(POWER_SAVING);
	}
	
	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null)
			throw new IllegalArgumentException("Task phase is null");
		else if (POWER_SAVING.equals(getPhase()))
			return powerSavingPhase(time);
		else
			return time;
	}


	/**
	 * Goes to power saving phase.
	 *
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double powerSavingPhase(double time) {
		
		if (isDone() || getTimeLeft() <= 0) {
			
			setDescription(END_POWER_SAVING, false);
        	// this task has ended
			endTask();
			
			return time;
		}

		// If robot is low power, leave the power save mode
		if (robot.getSystemCondition().isLowPower()) {
			
			setDescriptionDone(END_POWER_SAVING);
        	// this task has ended
			endTask();
			
			return time;
		}
		
		if (!robot.getSystemCondition().isPowerSave())
			robot.getSystemCondition().setPowerSave(true);

		return 0;
	}

	/**
	 * Clears down.
	 */
	@Override
	protected void clearDown() {

		robot.getSystemCondition().setPowerSave(false);

		super.clearDown();
	}

}

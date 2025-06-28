/*
 * Mars Simulation Project
 * TendHousekeeping.java
 * @date 2024-06-02
 * @author Barry Evans
 */
package com.mars_sim.core.building.function.farming.task;


import com.mars_sim.core.building.function.HouseKeeping;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.tool.Msg;

/**
 * The TendFishTank class is a task for tending the fishery in a
 * settlement. This is an effort driven task.
 */
public abstract class TendHousekeeping extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Task phases. */
	protected static final TaskPhase INSPECTING = new TaskPhase(Msg.getString("Task.phase.inspecting")); //$NON-NLS-1$
	/** Task phases. */
	protected static final TaskPhase CLEANING = new TaskPhase(Msg.getString("Task.phase.cleaning")); //$NON-NLS-1$

	protected static final String CLEANING_DETAIL = Msg.getString("Task.description.tendHousekeeping.cleaning"); //$NON-NLS-1$
	
	protected static final String INSPECTING_DETAIL = Msg.getString("Task.description.tendHousekeeping.inspecting"); //$NON-NLS-1$
		
	static final double MAX_INSPECT_TIME = 30D;
	
	static final double MAX_CLEANING_TIME = 80D;

	private double completedTime = 0D;
	
	private String goal;
	
	private HouseKeeping keeping;
	
	/**
	 * Constructor.
	 * 
	 * @param name Name of task
	 * @param cleaner Worker doing housekeeping
	 * @param keep What needs housekeeping
	 * @param impact What impact does it have doing this
	 * @param duration Task duration, maybe fixed time or logic based
	 */
	protected TendHousekeeping(String name, Worker cleaner, HouseKeeping keep, ExperienceImpact impact,
								double duration) {
		super(name, cleaner, false, impact, duration);

		this.keeping = keep;
	}

	
	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			return 0;
		} else if (INSPECTING.equals(getPhase())) {
			return inspectingPhase(time);
		} else if (CLEANING.equals(getPhase())) {
			return cleaningPhase(time);
		} else {
			return time;
		}
	}
	
	/**
	 * Performs the inspecting phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double inspectingPhase(double time) {
		if (goal == null) {
			goal = keeping.getLeastInspected();
			if (goal == null) {
				endTask();
				return time;
			}

			setDescription(INSPECTING_DETAIL + " " + goal);
		}
		
		addExperience(time);
		completedTime += time;
		
		if (completedTime > MAX_INSPECT_TIME) {
			keeping.inspected(goal, completedTime);			
			endTask();
		}
			
		return 0;
	}

	/**
	 * Performs the cleaning phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double cleaningPhase(double time) {
		if (goal == null) {
			goal = keeping.getLeastCleaned();
			if (goal == null) {
				endTask();
				return time;
			}

			setDescription(CLEANING_DETAIL + " " + goal);
		}
		
		addExperience(time);
		completedTime += time;
		
		if (completedTime > MAX_CLEANING_TIME) {
			keeping.cleaned(goal, completedTime);			
			endTask();
		}
			
		return 0;
	}
}
/*
 * Mars Simulation Project
 * DoInventory.java
 * @date 2025-07-29
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.task;

import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.Research;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;

/**
 * This class is a task for taking inventory of a lab.
 */
public class DoInventory extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(DoInventory.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.doInventory"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase DOING_INVENTORY = new TaskPhase(Msg.getString("Task.phase.doingInventory")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .5D;

	// Data members
	private double FACTOR = .001;

	private double totalEntropyReduce;
	/** The research lab chosen. */
	private Research lab;

	public RoleType roleType;

	/**
	 * Constructor. This is an effort-driven task.
	 * 
	 * @param person the person performing the task.
	 */
	public DoInventory(Person person) {
		// Use Task constructor.
		super(NAME, person, true, false, STRESS_MODIFIER, 10D + RandomUtil.getRandomInt(30));

		if (person.isInSettlement() || person.isInSettlementVicinity()) {
			
			boolean anyZone = false;
			
			int rand = RandomUtil.getRandomInt(9);
			if (rand == 9) {
				// 90% on the same zone; 10% on possibly any zone
				anyZone = true;
			}
			
			// If person is in a settlement, try to find a lab.
			lab = person.getAssociatedSettlement().getBuildingManager().getWorstEntropyLabByProbability(person, anyZone);
		
			if (lab != null) {
				// The person must walk to a spot physically to take inventory.
				walkToTaskSpecificActivitySpotInBuilding(lab.getBuilding(), FunctionType.RESEARCH, false);
				
			}
			else
				endTask();
			
			// Initialize phase
			setPhase(DOING_INVENTORY);
		}
		
		else
			endTask();
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (DOING_INVENTORY.equals(getPhase())) {
			return doingInventory(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the doing inventory phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double doingInventory(double time) {
		if (isDone() || getTimeCompleted() + time > getDuration()) {
        	// this task has ended
			endTask();
			return time;
		}
		
		double modTime = time * FACTOR;
		
		double points = RandomUtil.getRandomDouble(modTime);
		
		totalEntropyReduce += lab.reduceEntropy(Math.min(modTime/50, points));	

		// Add experience
		addExperience(time);

		// Check for accident
		checkForAccident(lab.getBuilding(), time, 0.005);
		
		return time;
	}

	/**
	 * Releases space.
	 */
	@Override
	protected void clearDown() {
		logger.fine(person, 10_000L, "Reduced a total of " 
			+ Math.round(totalEntropyReduce * 100.0)/100.0 + " entropy.");
	}
}

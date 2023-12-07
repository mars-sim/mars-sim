/*
 * Mars Simulation Project
 * OptimizeSystem.java
 * @date 2022-08-01
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.task;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.structure.building.function.Computation;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

/**
 * This class is a task for investigating what system to optimize.
 */
public class OptimizeSystem extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(OptimizeSystem.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.optimizeSystem"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase OPTIMIZING_SYSTEM = new TaskPhase(Msg.getString("Task.phase.optimizingSystem")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .5D;

	// Data members
	private static final double FACTOR = .05;

	private double totalEntropyReduce;
	
	/** The computing node. */
	private Computation node;

	public RoleType roleType;

	/**
	 * Constructor. This is an effort-driven task.
	 * 
	 * @param person the person performing the task.
	 */
	public OptimizeSystem(Person person) {
		// Use Task constructor.
		super(NAME, person, true, false, STRESS_MODIFIER, 10D + RandomUtil.getRandomInt(30));

		if (person.isInSettlement()) {
			// If person is in a settlement, try to find a server node.
			node = person.getSettlement().getBuildingManager().getWorstEntropyComputingNodeByProbability(person);
			if (node != null) {
				// Walk to the spot.
				walkToTaskSpecificActivitySpotInBuilding(node.getBuilding(), FunctionType.COMPUTATION, false);
			}
			else
				endTask();
			
			// Initialize phase
			addPhase(OPTIMIZING_SYSTEM);
			setPhase(OPTIMIZING_SYSTEM);
		}
		
		else if (person.isInVehicle()) {
			// If person is in a settlement, try to find a server node.
			node = person.getAssociatedSettlement().getBuildingManager().getWorstEntropyComputingNodeByProbability(person);
			if (node != null) {
				// May be done remotely in a vehicle
				// Walk to the passenger spot
//				walkToPassengerActivitySpotInRover((Rover)(person.getVehicle()), false);
			}
			else
				endTask();
		}
		
		else
			endTask();
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (OPTIMIZING_SYSTEM.equals(getPhase())) {
			return optimizingPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the optimizing phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double optimizingPhase(double time) {
		if (isDone() || getTimeCompleted() + time > getDuration()) {
        	// this task has ended
			endTask();
			return time;
		}
		
		double com = 0;
		
		if (person.getSkillManager().getSkill(SkillType.COMPUTING) != null) {
			com = 1 + person.getSkillManager().getSkill(SkillType.COMPUTING).getLevel();
		}
		
		double modTime = time * FACTOR * com;
		
		double points = RandomUtil.getRandomDouble(modTime);
		
		totalEntropyReduce += node.reduceEntropy(Math.min(modTime/50, points));	

		// Add experience
		addExperience(time);

		// Check for accident
		checkForAccident(node.getBuilding(), time, 0.001);
		
		return time;
	}

	/**
	 * Releases office space.
	 */
	@Override
	protected void clearDown() {
		logger.fine(person, 10_000L, "Reduced a total of " 
			+ Math.round(totalEntropyReduce * 100.0)/100.0 + " entropy.");
	}
}

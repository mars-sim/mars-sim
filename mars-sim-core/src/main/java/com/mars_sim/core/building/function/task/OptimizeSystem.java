/*
 * Mars Simulation Project
 * OptimizeSystem.java
 * @date 2025-07-29
 * @author Manny Kung
 */
package com.mars_sim.core.building.function.task;

import com.mars_sim.core.building.function.Computation;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;

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
	private static final double FACTOR = .001;

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
		super(NAME, person, true, false, STRESS_MODIFIER, 20D + RandomUtil.getRandomInt(15));

		if (person.isInSettlement()) {
	
			// If person is in a settlement, try to find a server node.
			node = person.getSettlement().getBuildingManager().getWorstEntropyComputingNodeByProbability(person, true);
			
			if (node == null) {
				endTask();
				return;
			}
				
			
			// Note : if node is null, endTask() doesn't stop it right away. getBuilding() below will result in null
			int bldgZone = node.getBuilding().getZone();
			int personZone = person.getBuildingLocation().getZone();
			
			// Note: need to consider how much is the worth in physically going to the observatory or any buildings in another zone
			// Q: since it takes time to go through the EVA airlock, it's more logical to perform a longer task by one person rather
			// than sending 2 persons.

			boolean remoteWork = true;
			
			if (bldgZone == personZone) {
				// Walk to the spot.
				walkToTaskSpecificActivitySpotInBuilding(node.getBuilding(), FunctionType.COMPUTATION, false);
			}
			else {
				// Note: This lab building is located at a different zone
				
				int rand = RandomUtil.getRandomInt(9);
				if (rand == 9) {
					// 90% remote work; 10% physical work
					remoteWork = false;
				}
				
				if (!remoteWork) {
					// Lengthen the time the person will optimize the system since it needs to physical get there
					// via EVA airlock
					setDuration(getDuration() * 3);
					// Walk to the spot.
					walkToTaskSpecificActivitySpotInBuilding(node.getBuilding(), FunctionType.COMPUTATION, false);
				}
			}
		}
		
		else if (person.isInVehicle()) {
			// If person is in a settlement, try to find a server node.
			node = person.getAssociatedSettlement().getBuildingManager().getWorstEntropyComputingNodeByProbability(person, true);
			
			if (node == null)
				endTask();
		}
		
		else if (person.isInSettlementVicinity()) {
			// If person is in a settlement, try to find a server node.
			node = person.getAssociatedSettlement().getBuildingManager().getWorstEntropyComputingNodeByProbability(person, true);
			
			if (node == null)
				endTask();
		}
		
		else
			endTask();
		
		// Initialize phase
		addPhase(OPTIMIZING_SYSTEM);
		setPhase(OPTIMIZING_SYSTEM);
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
		
		int skillF = 0;
		
		if (person.getSkillManager().getSkill(SkillType.COMPUTING) != null) {
			skillF = 1 + person.getSkillManager().getSkill(SkillType.COMPUTING).getLevel();
		}
		
		double modTime = time * FACTOR * skillF;
			
		totalEntropyReduce += node.reduceEntropy(modTime);	

		// Add experience
		addExperience(time);

		// Check for accident
		checkForAccident(node.getBuilding(), time, 0.001);
		
		return time;
	}

	/**
	 * Releases the task.
	 */
	@Override
	protected void clearDown() {
		logger.fine(person, 10_000L, "Reduced a total of " 
			+ Math.round(totalEntropyReduce * 100.0)/100.0 + " entropy.");
	}
}

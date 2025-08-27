/*
 * Mars Simulation Project
 * ManufactureGood.java
 * @date 2024-09-09
 * @author Scott Davis
 */
package com.mars_sim.core.manufacture.task;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.Manufacture;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.manufacture.ManufacturingManager;
import com.mars_sim.core.manufacture.WorkshopProcess;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillManager;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;

/**
 * A task for working on a manufacturing process.
 */
public class ManufactureWorkTask extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(ManufactureWorkTask.class.getName());

	
	/** Task name */
	private static final String NAME = Msg.getString("Task.description.manufactureGood"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase MANUFACTURE = new TaskPhase(Msg.getString("Task.phase.manufacture")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .2D;

	// Data members
	/** The manufacturing workshop the person is using. */
	private Manufacture workshop;

	/**
	 * Constructor.
	 * 
	 * @param person the person to perform the task
	 */
	public ManufactureWorkTask(Person person, Building building) {
		super(NAME, person, true, false, STRESS_MODIFIER, SkillType.MATERIALS_SCIENCE, 100D, 25);

		setupWorkshop(building);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param robot the robot to perform the task
	 * @param building Where the manufacturing is done
	 */
	public ManufactureWorkTask(Robot robot, Building building) {
		super(NAME, robot, true, false, STRESS_MODIFIER, SkillType.MATERIALS_SCIENCE, 100D,
				10D + RandomUtil.getRandomDouble(50D));

		setupWorkshop(building);
	}

	/**
	 * Get the skill of the Worker when doing Manufacturing processes.
	 * @return
	 */
	static int getWorkerSkill(Worker w) {
		SkillManager skillManager = w.getSkillManager();
		return skillManager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE);
	}
	
	/**
	 * Sets up the workshop to start helping process.
	 */
	private void setupWorkshop(Building manufactureBuilding) {
		workshop = manufactureBuilding.getManufacture();

		// Walk to manufacturing building.
		walkToTaskSpecificActivitySpotInBuilding(manufactureBuilding, FunctionType.MANUFACTURE, false);
		
		// Initialize phase
		setPhase(MANUFACTURE);
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (MANUFACTURE.equals(getPhase())) {
			return manufacturePhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Perform the manufacturing phase.
	 * 
	 * @param time the time to perform (millisols)
	 * @return remaining time after performing (millisols)
	 */
	private double manufacturePhase(double time) {

		if (worker.isOutside()) {
			endTask();
			return 0;
		}
			
		// Check if workshop has malfunction.
		Building entity = workshop.getBuilding();
		if (entity.getMalfunctionManager().hasMalfunction()) {
			logger.info(worker, 30_000, "Manufacturing halted due to malfunction.");
			endTask();
			return time * .75;
		}
        
		// Determine amount of effective work time based on "Materials Science"
		// skill.
		double workTime = time;
		int skill = getEffectiveSkillLevel();
		if (skill == 0) {
			workTime /= 2;
		} else {
			workTime += workTime * (.2D * skill);
		}

		// Apply work time to manufacturing processes.
		while ((workTime > 0D) && !isDone()) {
			workTime = workInWorkshop(workTime, skill);
		}

		// Add experience
		addExperience(time);

		// Check for accident in workshop.
		checkForAccident(entity, time, 0.004);

		return 0D;
	}

	/**
	 * Executes the manufacture process.
	 * 
	 * @param workTime
	 */
	private double workInWorkshop(double workTime, int skill) {
		var process = getRunningProcess();
		if (process == null) {
			process = createNewProcess();
			
			if (process == null) {
				endTask();
				return 0;
			}
			setDescription(process.getName());
		}

		double required = Math.min(process.getWorkTimeRemaining(), workTime);
		process.addWorkTime(required, skill);

		return workTime - required;
	}
	
	/**
	 * Gets an available running manufacturing process.
	 * 
	 * @return process or null if none.
	 */
	private WorkshopProcess getRunningProcess() {
		int skillLevel = getEffectiveSkillLevel();
		for(var process : workshop.getProcesses()) {
			if ((process.getInfo().getSkillLevelRequired() <= skillLevel)
							&& (process.getWorkTimeRemaining() > 0D)) {
				setDescription(process.getName());
				return process;
			}
		}

		return null;
	}

	/**
	 * Creates a new workshop process if possible.
	 * 
	 * @return the new  process or null if none.
	 */
	private WorkshopProcess createNewProcess() {

		if (workshop.getCapacity() > 0) {
			int skill = getWorkerSkill(worker);

			// Get something off the queue
			ManufacturingManager mgr = workshop.getBuilding().getAssociatedSettlement().getManuManager();
			var queued = mgr.claimNextProcess(workshop.getTechLevel(), skill, workshop.getAvailableTools());

			// Create chosen manufacturing process.
			if (queued != null) {
				var result = queued.createProcess(workshop);
				result.startProcess();
				return result;
			}
		}

		return null;
	}
}

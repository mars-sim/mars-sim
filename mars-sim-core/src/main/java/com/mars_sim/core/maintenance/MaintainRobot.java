/*
 * Mars Simulation Project
 * MaintainRobot.java
 * @date 2025-09-05
 * @author Manny Kung
 */
package com.mars_sim.core.maintenance;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.malfunction.Malfunctionable;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.MathUtils;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;

/**
 * The task for performing preventive maintenance on robots.
 */
public class MaintainRobot extends Task  {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(MaintainRobot.class.getName());
	
	/** Task name */
	private static final String NAME = Msg.getString(
			"Task.description.maintainRobot"); //$NON-NLS-1$

    private static final String DETAIL = Msg.getString(
    		"Task.description.maintainRobot.detail") + " "; //$NON-NLS-1$
    
	/** Task phases. */
	static final TaskPhase MAINTAIN = new TaskPhase(Msg.getString("Task.phase.maintain")); //$NON-NLS-1$

	// Static members
	private static final ExperienceImpact IMPACT = new ExperienceImpact(100,
												NaturalAttributeType.EXPERIENCE_APTITUDE, true,
												0.1D, SkillType.MECHANICS);

	// Data members
	/** Entity to be maintained. */
	private Malfunctionable entity;
	
	private Robot robotInService;

	/**
	 * Constructor.
	 *
	 * @param engineer the Worker to perform the task
	 */
	public MaintainRobot(Worker engineer, Robot entity) {
		super(NAME, engineer, false, IMPACT, RandomUtil.getRandomDouble(80, 120));

		if (engineer.isOutside()) {
			endTask();
			return;
		}

		this.entity = entity;
		
		robotInService = ((Robot)entity);
		
		robotInService.getSystemCondition().setMaintenance(true);

		String des = DETAIL + entity.getName();
		setDescription(des);
		logger.info(worker, 30_000, des + ".");

		boolean success = false;
		
		// Walk to a workshop or garage
		Settlement s = worker.getSettlement();
		if (s != null) {
			List<Building> buildingList = new ArrayList<>(
					BuildingManager.getBuildingsinSameZone(worker, FunctionType.MANUFACTURE));
			int size = buildingList.size();
			
			for (int i=0; i<size && !success; i++) {
				success = walkToActivitySpotInBuilding(buildingList.get(i), 
						FunctionType.MANUFACTURE, false);
			}
			
			if (!success) {
				buildingList = new ArrayList<>(
						BuildingManager.getBuildingsinSameZone(worker, FunctionType.RESEARCH));
				size = buildingList.size();

				for (int i=0; i<size && !success; i++) {
					success = walkToActivitySpotInBuilding(buildingList.get(i), 
							FunctionType.RESEARCH, false);
				}
			}
			
			if (!success) {
				buildingList = new ArrayList<>(
						BuildingManager.getBuildingsinSameZone(worker, FunctionType.RESEARCH));
				size = buildingList.size();

				for (int i=0; i<size && !success; i++) {
					success = walkToActivitySpotInBuilding(buildingList.get(i), 
							FunctionType.RESEARCH, false);
				}
			}
		}

		if (success) {
			// Initialize phase.
			setPhase(MAINTAIN);
		}
		else {
			robotInService.getSystemCondition().setMaintenance(false);
			clearTask("No available workspace for " + robot.getName() + " maintenance.");
			return;
		}
		
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (MAINTAIN.equals(getPhase())) {
			return maintainPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the maintain phase.
	 *
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double maintainPhase(double time) {    	
		// If worker is incapacitated, end task.
		if (worker.getPerformanceRating() <= .1) {
			robotInService.getSystemCondition().setMaintenance(false);
			endTask();
			return time;
		}

		MalfunctionManager manager = entity.getMalfunctionManager();

		// If equipment has malfunction, end task.
		if (manager.hasMalfunction()) {
			robotInService.getSystemCondition().setMaintenance(false);
			endTask();
			return time * .75;
		}

		// Determine effective work time based on "Mechanic" skill.
		double workTime = time;
		int mechanicSkill = getEffectiveSkillLevel();
		if (mechanicSkill == 0) {
			workTime /= 2;
		}
		if (mechanicSkill > 1) {
			workTime += workTime * (.4D * mechanicSkill);
		}
		
		// Check if maintenance has already been completed.
		boolean finishedMaintenance = manager.getEffectiveTimeSinceLastMaintenance() == 0D;

		boolean doneInspection = false;

		if (!finishedMaintenance) {
			doneInspection = !manager.addInspectionMaintWorkTime(workTime);
		}
		
		if (finishedMaintenance || doneInspection || getTimeCompleted() >= getDuration()) {
			// Inspect the entity
			manager.inspectEntityTrackParts(getTimeCompleted());
			// No more maintenance is needed
			robotInService.getSystemCondition().setMaintenance(false);
		
			endTask();
		}

		// Add experience points
		addExperience(time);

		// Check if an accident happens during maintenance.
		checkForAccident(entity, time, 0.005);

		// Note: workTime can be longer or shorter than time
		if (workTime > time) {
			// if work time is greater, then time is saved on this frame
			return MathUtils.between(workTime - time, 0, time * .75);
		}
		else
			return 0;
	}

	/**
	 * Gets the entity the person is maintaining. Returns null if none.
	 *
	 * @return entity
	 */
	public Malfunctionable getEntity() {
		return entity;
	}
}

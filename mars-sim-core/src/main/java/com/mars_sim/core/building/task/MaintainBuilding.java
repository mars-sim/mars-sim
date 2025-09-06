/*
 * Mars Simulation Project
 * MaintainBuilding.java
 * @date 2025-08-27
 * @author Scott Davis
 */
package com.mars_sim.core.building.task;

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
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.MathUtils;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;

/**
 * The task for performing preventive maintenance on buildings.
 */
public class MaintainBuilding extends Task  {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(MaintainBuilding.class.getName());
	
	/** Task name */
	private static final String NAME = Msg.getString(
			"Task.description.maintainBuilding"); //$NON-NLS-1$

    private static final String DETAIL = Msg.getString(
    		"Task.description.maintainBuilding.detail") + " "; //$NON-NLS-1$
    
	/** Task phases. */
	static final TaskPhase MAINTAIN = new TaskPhase(Msg.getString("Task.phase.maintain")); //$NON-NLS-1$

	// Static members
	private static final ExperienceImpact IMPACT = new ExperienceImpact(100,
												NaturalAttributeType.EXPERIENCE_APTITUDE, true,
												0.1D, SkillType.MECHANICS);

	// Data members
	/** Entity to be maintained. */
	private Malfunctionable entity;

	/**
	 * Constructor.
	 *
	 * @param engineer the Worker to perform the task
	 */
	public MaintainBuilding(Worker engineer, Building entity) {
		super(NAME, engineer, false, IMPACT, RandomUtil.getRandomDouble(80, 120));

		if (engineer.isOutside()) {
			endTask();
			return;
		}

		this.entity = entity;		

		String des = DETAIL + entity.getName();
		setDescription(des);
		logger.info(worker, 30_000, des + ".");
		
		
		if (entity.isInhabitable()) {
			// walk to an admin and remotely inspect the building
			Settlement s = worker.getSettlement();
			if (s != null) {
				List<Building> buildingList = new ArrayList<>(
						BuildingManager.getBuildingsinSameZone(worker, FunctionType.ADMINISTRATION));
				int size = buildingList.size();
				boolean success = false;
				for (int i=0; i<size && !success; i++) {
					success = walkToActivitySpotInBuilding(buildingList.get(i), 
							FunctionType.ADMINISTRATION, false);
				}
			}
		}
		else {
			// Walk to random location in building.
			walkToRandomLocInBuilding(entity, false);
		}

		// Initialize phase.
		setPhase(MAINTAIN);
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
			endTask();
			return time;
		}

		MalfunctionManager manager = entity.getMalfunctionManager();

		// If equipment has malfunction, end task.
		if (manager.hasMalfunction()) {
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

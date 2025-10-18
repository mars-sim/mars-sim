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
	/** The modified skill level. */
	private int effectiveSkillLevel;
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
			// Determine the effective skill level
			effectiveSkillLevel = getEffectiveSkillLevel();
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
//			robotInService.getSystemCondition().setMaintenance(false);
			endTask();
			return time;
		}

		MalfunctionManager manager = entity.getMalfunctionManager();

		// If equipment has malfunction, end task.
		if (manager.hasMalfunction()) {
			// Turn off maintenance and wait for repair
			robotInService.getSystemCondition().setMaintenance(false);
			endTask();
			return time * .75;
		}

		// Determine effective work time based on "Mechanic" skill.
        double workTime = time;
        int skill = effectiveSkillLevel;
        if (skill == 0) workTime /= 2;
        if (skill > 1)
        	workTime = workTime * (1 + .25 * skill);
		
        double timeCompleted = getTimeCompleted();
        
        // At the beginning of maintenance, identify if anything needs to be replaced
        if (getTimeCompleted() == 0.0) {
			// Inspect the entity
			manager.inspectEntityTrackParts(timeCompleted);
        }
        
		boolean doneInspection = false;
		
		// Check if maintenance has already been completed.
		boolean finishedMaintenance = manager.getEffectiveTimeSinceLastMaintenance() == 0D;

		if (!finishedMaintenance) {
			doneInspection = !manager.addInspectionMaintWorkTime(workTime);
		}
		
		if (finishedMaintenance || doneInspection || timeCompleted >= getDuration()) {
			double point = timeCompleted * (1 + .25 * skill);
			// Reduce fatigue
			manager.reduceFatigue(point);
			
			robotInService.getSystemCondition().tuneUpPerformance(point);
			// No more maintenance is needed
			robotInService.getSystemCondition().setMaintenance(false);
		
			endTask();
		}

		// Add experience points
		addExperience(time);

		// Check if an accident happens during maintenance.
		checkForAccident(entity, time, 0.005);

		// if work time is greater than time, then less time is spent on this frame
		return MathUtils.between((workTime - time), 0, time) * .5;
		// Note: 1. workTime can be longer or shorter than time
		//       2. the return time may range from zero to as much as half the tick  
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

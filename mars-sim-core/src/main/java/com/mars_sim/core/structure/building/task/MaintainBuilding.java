/*
 * Mars Simulation Project
 * MaintainBuilding.java
 * @date 2022-09-18
 * @author Scott Davis
 */
package com.mars_sim.core.structure.building.task;

import com.mars_sim.core.Unit;
import com.mars_sim.core.equipment.EquipmentOwner;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.malfunction.Malfunctionable;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

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
		super(NAME, engineer, false, IMPACT, RandomUtil.getRandomDouble(20, 80));

		if (engineer.isOutside()) {
			endTask();
			return;
		}

		this.entity = entity;		

		String des = DETAIL + entity.getName();
		setDescription(des);
		logger.info(worker, 30_000, des + ".");
		
		// Walk to random location in building.
		walkToRandomLocInBuilding(entity, false);

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
	
		// Note: if parts don't exist, it simply means that one can still do the 
		// inspection portion of the maintenance with no need of replacing any parts
		boolean partsPosted = manager.hasMaintenancePartsInStorage(entity.getAssociatedSettlement());
		
		if (partsPosted) {
			Unit containerUnit = entity.getAssociatedSettlement();

			int shortfall = manager.transferMaintenanceParts((EquipmentOwner) containerUnit);
			
			if (shortfall == -1) {
				logger.warning(entity, 30_000L, "No spare parts available for maintenance on " 
						+ entity + ".");
			}
		}

		// Add work to the maintenance
		if (!manager.addInspectionMaintWorkTime(workTime)) {
			// No more maintenance is needed
			endTask();
		}

		// Add experience points
		addExperience(time);

		// Check if an accident happens during maintenance.
		checkForAccident(entity, time, 0.005);

		return 0D;
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

/*
 * Mars Simulation Project
 * MaintainBuildingEVA.java
 * @date 2023-09-17
 * @author Scott Davis
 */
package com.mars_sim.core.building.task;

import com.mars_sim.core.Unit;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.equipment.EquipmentOwner;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.malfunction.Malfunctionable;
import com.mars_sim.core.map.location.LocalBoundedObject;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;

/**
 * The task for performing preventive maintenance on malfunctionable entities outdoors.
 */
public class MaintainBuildingEVA
extends EVAOperation {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(MaintainBuildingEVA.class.getName());

	/** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.maintainBuildingEVA"); //$NON-NLS-1$
    
    private static final String DETAIL = Msg.getString(
    		"Task.description.maintainBuildingEVA.detail") + " "; //$NON-NLS-1$

    /** Task phases. */
    static final TaskPhase MAINTAIN = new TaskPhase(Msg.getString(
            "Task.phase.maintain"), createPhaseImpact(SkillType.MECHANICS));

    
	// Data members
	/** Entity to be maintained. */
	private Malfunctionable entity;

	/**
	 * Constructor.
	 * 
	 * @param person the person to perform the task
	 */
	public MaintainBuildingEVA(Person person, Building target) {
		super(NAME, person, RandomUtil.getRandomDouble(90, 100), MAINTAIN);

		if (person.isSuperUnfit()) {
			endEVA("Super Unfit.");
        	return;
		}
        
		// Check suitability
		entity = target;

		String des = DETAIL + entity.getName();
		setDescription(des);
		logger.info(person, 4_000, des);
        
	    // Determine location for maintenance.
        setOutsideLocation((LocalBoundedObject) entity);
	}

    @Override
    protected double performMappedPhase(double time) {

        time = super.performMappedPhase(time);
        if (!isDone()) {
	    	if (getPhase() == null) {
	    	    throw new IllegalArgumentException("Task phase is null");
	    	}
	    	else if (MAINTAIN.equals(getPhase())) {
	    	    time = maintenancePhase(time);
	    	}
        }
    	return time;
    }


	/**
	 * Performs the maintenance phase of the task.
	 * 
	 * @param time the time to perform this phase (in millisols)
	 * @return the time remaining after performing this phase (in millisols)
	 * @throws Exception if error during maintenance.
	 */
	private double maintenancePhase(double time) {
		
		if (checkReadiness(time) > 0)
			return time;
		
		MalfunctionManager manager = entity.getMalfunctionManager();
		boolean malfunction = manager.hasMalfunction();

		if (malfunction) {
			endEVA("Building had malfunction.");
			return time;
		}

		// Determine effective work time based on "Mechanic" skill.
		double workTime = time;
		int mechanicSkill = worker.getSkillManager().getEffectiveSkillLevel(SkillType.MECHANICS);
	
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
			endTask();
		}
		
        // Add experience points
        addExperience(time);

		// Check if an accident happens during maintenance.
		checkForAccident(entity, time, 0.01);
		
		return 0;
	}
}

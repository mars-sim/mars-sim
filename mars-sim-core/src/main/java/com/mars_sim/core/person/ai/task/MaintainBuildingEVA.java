/*
 * Mars Simulation Project
 * MaintainBuildingEVA.java
 * @date 2023-09-17
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.Unit;
import com.mars_sim.core.equipment.EquipmentOwner;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.malfunction.Malfunctionable;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.mapdata.location.LocalBoundedObject;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

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
    private static final TaskPhase MAINTAIN = new TaskPhase(Msg.getString(
            "Task.phase.maintain")); //$NON-NLS-1$

    
	// Data members
	/** Entity to be maintained. */
	private Malfunctionable entity;
	private Settlement settlement;

	/**
	 * Constructor.
	 * 
	 * @param person the person to perform the task
	 */
	public MaintainBuildingEVA(Person person, Building target) {
		super(NAME, person, true, RandomUtil.getRandomDouble(90, 100), SkillType.MECHANICS);

		if (!person.isNominallyFit()) {
			checkLocation("Not nominally fit.");
        	return;
		}
		
		if (unitManager == null)
			unitManager = Simulation.instance().getUnitManager();
		
      	settlement = unitManager.findSettlement(person.getCoordinates());
        if (settlement == null) {
        	checkLocation("Person not in settlement.");
        	return;
        }
        
		// Check suitability
		entity = target;
		MalfunctionManager manager = target.getMalfunctionManager();
		
		double effectiveTime = manager.getEffectiveTimeSinceLastMaintenance();
		if (effectiveTime < 10D) {
			clearTask("Maintenance already done.");
			return;
		}

		String des = DETAIL + entity.getName();
		setDescription(des);
		logger.info(person, 4_000, des + ".");
        
	    // Determine location for maintenance.
        setOutsideLocation((LocalBoundedObject) entity);

		// Initialize phase
		addPhase(MAINTAIN);

		logger.fine(person, "Starting " + getDescription());
	}

    @Override
    protected TaskPhase getOutsideSitePhase() {
        return MAINTAIN;
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
		
		if (checkReadiness(time, true) > 0)
			return time;
		
		MalfunctionManager manager = entity.getMalfunctionManager();
		boolean malfunction = manager.hasMalfunction();

		if (malfunction) {
			checkLocation("Building had malfunction. Quit maintenance.");
			return time;
		}
		
		boolean finishedMaintenance = (manager.getEffectiveTimeSinceLastMaintenance() < 1000D);

		if (finishedMaintenance) {
			checkLocation("Maintenance finished.");
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
		
//		String des = DETAIL + entity.getName();
//		setDescription(des);
//		logger.info(worker, 4_000, des + ".");
			
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
		manager.addInspectionMaintWorkTime(workTime);
		
        // Add experience points
        addExperience(time);

		// Check if an accident happens during maintenance.
		checkForAccident(entity, time, 0.01);
		
		return 0;
	}
	

	@Override
	protected void checkForAccident(double time) {

		// Use EVAOperation checkForAccident() method.
		super.checkForAccident(time);

		int skill = worker.getSkillManager().getEffectiveSkillLevel(SkillType.MECHANICS);
		checkForAccident(entity, time, 0.005D, skill, entity.getName());
	}
}

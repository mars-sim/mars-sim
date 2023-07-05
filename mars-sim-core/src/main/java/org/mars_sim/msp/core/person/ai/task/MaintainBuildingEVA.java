/*
 * Mars Simulation Project
 * MaintainBuildingEVA.java
 * @date 2022-09-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import org.mars.sim.mapdata.location.LocalBoundedObject;
import org.mars.sim.tools.Msg;
import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

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
    
    private static final String DETAIL = "Task.description.maintainBuildingEVA.detail";

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
			checkLocation();
        	return;
		}
		
      	settlement = CollectionUtils.findSettlement(person.getCoordinates());
        if (settlement == null) {
        	checkLocation();
        	return;
        }
        
		// Check suitability
		entity = target;
		MalfunctionManager manager = target.getMalfunctionManager();
		
		// Note 2: if parts don't exist, it simply means that one can still do the 
		// inspection portion of the maintenance with no need of replacing any parts
		 
//		if (!manager.hasMaintenancePartsInStorage(worker.getAssociatedSettlement())) {		
//			clearTask("No parts");
//			return;
//		}
		
		double effectiveTime = manager.getEffectiveTimeSinceLastMaintenance();
		if (effectiveTime < 10D) {
			clearTask("Maintenance already done");
			return;
		}

		String des = Msg.getString(DETAIL, entity.getName()); //$NON-NLS-1$
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
		boolean finishedMaintenance = (manager.getEffectiveTimeSinceLastMaintenance() < 1000D);

		if (finishedMaintenance || malfunction) {
			checkLocation();
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
		
		int shortfall = manager.transferMaintenanceParts(settlement);
			
		String des = Msg.getString(DETAIL, entity.getName()); //$NON-NLS-1$
		setDescription(des);
		logger.info(worker, 4_000, des + ".");
			
        if (shortfall == -1) {
        	checkLocation();
        	return 0;
		}
        
        // Add work to the maintenance
		manager.addMaintenanceWorkTime(workTime);
		
        // Add experience points
        addExperience(time);

		// Check if an accident happens during maintenance.
		checkForAccident(entity, time, 0.005D);
		
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

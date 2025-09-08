/*
 * Mars Simulation Project
 * MaintainBuildingEVA.java
 * @date 2025-08-24
 * @author Scott Davis
 */
package com.mars_sim.core.building.task;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.malfunction.Malfunctionable;
import com.mars_sim.core.map.location.LocalBoundedObject;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.tool.MathUtils;
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
	/** The modified skill level. */
	private int effectiveSkillLevel;
	/** Entity to be maintained. */
	private Malfunctionable entity;

	/**
	 * Constructor.
	 * 
	 * @param person the person to perform the task
	 */
	public MaintainBuildingEVA(Person person, Building target) {
		super(NAME, person, AVERAGE_EVA_TIME + RandomUtil.getRandomDouble(80, 120), MAINTAIN);

		if (isSuperUnfit()) {
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
    	
		// Determine the effective skill level
		effectiveSkillLevel = getEffectiveSkillLevel();
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

        // Determine effective work time based on "Mechanic" and "EVA Operations" skills.
        double workTime = time;
        int skill = effectiveSkillLevel;
        if (skill == 0) workTime /= 2;
        if (skill > 1)
        	workTime = workTime * (1 + .25 * skill);
 
		boolean doneInspection = false;

		// Check if maintenance has already been completed.
		boolean finishedMaintenance = manager.getEffectiveTimeSinceLastMaintenance() == 0D;
		
		if (!finishedMaintenance) {
			doneInspection = !manager.addInspectionMaintWorkTime(workTime);
		}
		
		if (finishedMaintenance || doneInspection) {
			// Inspect the entity
			manager.inspectEntityTrackParts(getTimeCompleted());
			// No more maintenance is needed
			endEVA("Maintenance Done");
		}
		
        // Add experience points
        addExperience(time);

		// Check if an accident happens during maintenance.
		checkForAccident(entity, time, 0.01);
		
		// if work time is greater than time, then less time is spent on this frame
		return MathUtils.between((workTime - time), 0, time) * .5;
		// Note: 1. workTime can be longer or shorter than time
		//       2. the return time may range from zero to as much as half the tick  
	}
}

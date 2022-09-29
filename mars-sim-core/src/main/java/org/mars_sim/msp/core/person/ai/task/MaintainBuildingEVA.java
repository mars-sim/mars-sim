/*
 * Mars Simulation Project
 * MaintainBuildingEVA.java
 * @date 2022-09-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The task for performing preventive maintenance on malfunctionable entities outdoors.
 */
public class MaintainBuildingEVA
extends EVAOperation
implements Serializable {

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
	 * @param person the person to perform the task
	 */
	public MaintainBuildingEVA(Person person) {
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
        	
		entity = getWorstBuilding();

		if (entity != null) {
			if (!MaintainBuilding.hasMaintenanceParts(settlement, entity)) {		
				checkLocation();
			    return;
			}

			else {
				String des = Msg.getString(DETAIL, entity.getName()); //$NON-NLS-1$
				setDescription(des);
				logger.info(person, 4_000, des + ".");
			}
		}

		else {
			checkLocation();
		}
        
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

		// Gets the building with the worst condition
		if (entity == null) {
			entity = getWorstBuilding();
			String des = Msg.getString(DETAIL, entity.getName()); //$NON-NLS-1$
			setDescription(des);
			logger.info(worker, 4_000, des + ".");
		}
		
		if (MaintainBuilding.hasMaintenanceParts(settlement, entity)) {
			
			// Note: should allow replace one part at a time
			// and not to wait till all the parts are available
			Map<Integer, Integer> parts = new HashMap<>(manager.getMaintenanceParts());
			Iterator<Integer> j = parts.keySet().iterator();
			while (j.hasNext()) {
				Integer part = j.next();
				int number = parts.get(part);
				int numMissing = settlement.retrieveItemResource(part, number);
		        // Consume the number of repair parts that are available.
				manager.maintainWithParts(part, number - numMissing);
			}
			
        }
		else {
			// Gets the building with the worst condition
			entity = getWorstBuilding();
			String des = Msg.getString(DETAIL, entity.getName()); //$NON-NLS-1$
			setDescription(des);
			logger.info(worker, 4_000, des + ".");
		}
		
		if (entity == null) {
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


	/**
	 * Gets the building with worst condition to maintain.
	 * 
	 * @return
	 */
	public Malfunctionable getWorstBuilding() {
		Malfunctionable result = null;
		double worstCondition = 100;
		List<Building> list = worker.getAssociatedSettlement().getBuildingManager().getBuildings();
		Collections.shuffle(list);
		for (Building building: list) {
			if (!building.hasFunction(FunctionType.LIFE_SUPPORT)) {
				double condition = building.getMalfunctionManager().getWearCondition();
				if (condition < worstCondition) {
					worstCondition = condition;
					result = building;
				}
			}
		}
		return result;
	}
}

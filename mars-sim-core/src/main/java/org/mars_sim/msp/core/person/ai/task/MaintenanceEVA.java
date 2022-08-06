/*
 * Mars Simulation Project
 * MaintenanceEVA.java
 * @date 2021-10-21
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.Structure;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The Maintenance class is a task for performing
 * preventive maintenance on malfunctionable entities outdoors.
 */
public class MaintenanceEVA
extends EVAOperation
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(MaintenanceEVA.class.getName());

	/** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.maintenanceEVA"); //$NON-NLS-1$

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
	public MaintenanceEVA(Person person) {
		super(NAME, person, true, RandomUtil.getRandomDouble(50D) + 10D, SkillType.MECHANICS);

		if (!person.isBarelyFit()) {
			checkLocation();
        	return;
		}
		
      	settlement = CollectionUtils.findSettlement(person.getCoordinates());
        if (settlement == null) {
        	checkLocation();
        	return;
        }
        	
		try {
			entity = getMaintenanceMalfunctionable();
			if (entity != null) {
				if (!Maintenance.hasMaintenanceParts(settlement, entity)) {		
					checkLocation();
				    return;
				}
			}
			else {
				checkLocation();
			    return;
			}
		}
		catch (Exception e) {
		    logger.log(Level.SEVERE,"MaintenanceEVA.constructor()",e);
		    checkLocation();
			return;
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
		
		if (checkReadiness(time) > 0)
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

		// Gets a malfunctionable
		entity = getMaintenanceMalfunctionable();
		
		if (entity != null && Maintenance.hasMaintenanceParts(settlement, entity)) {
			
			Map<Integer, Integer> parts = new HashMap<>(manager.getMaintenanceParts());
			Iterator<Integer> j = parts.keySet().iterator();
			while (j.hasNext()) {
				Integer part = j.next();
				int number = parts.get(part);
				settlement.retrieveItemResource(part, number);
		        // Add repair parts if necessary.
				manager.maintainWithParts(part, number);
			}

	        // Add work to the maintenance
			manager.addMaintenanceWorkTime(workTime);
			
	        // Add experience points
	        addExperience(time);

			// Check if an accident happens during maintenance.
			checkForAccident(time);
			
        }
		else {
			checkLocation();
			return time;
		}

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
	 * Gets a random malfunctionable to perform maintenance on.
	 * 
	 * @return malfunctionable or null.
	 * @throws Exception if error finding malfunctionable.
	 */
	private Malfunctionable getMaintenanceMalfunctionable() {
		Malfunctionable result = null;

		// Determine all malfunctionables local to the person.
		Map<Malfunctionable, Double> malfunctionables = new HashMap<>();

		if (person != null) {
	        Iterator<Malfunctionable> i = MalfunctionFactory.getLocalMalfunctionables(person).iterator();
	        while (i.hasNext()) {
	            Malfunctionable entity = i.next();
	            double probability = getProbabilityWeight(entity);
	            if (probability > 0D) {
	                malfunctionables.put(entity, probability);
	            }
	        }
		}
		else {
	        Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(robot).iterator();
	        while (i.hasNext()) {
	            Malfunctionable entity = i.next();
	            double probability = getProbabilityWeight(entity);
	            if (probability > 0D) {
	                malfunctionables.put(entity, probability);
	            }
	        }
		}

        if (!malfunctionables.isEmpty()) {
            result = RandomUtil.getWeightedRandomObject(malfunctionables);
        }

		if (result != null) {
		    setDescription(Msg.getString("Task.description.maintenanceEVA.detail",
                    result.getNickName())); //$NON-NLS-1$;
		}

		return result;
	}

	/**
	 * Gets the probability weight for a malfunctionable.
	 * 
	 * @param malfunctionable the malfunctionable.
	 * @return the probability weight.
	 */
	private double getProbabilityWeight(Malfunctionable malfunctionable) {
		double result = 0D;
		boolean isStructure = (malfunctionable instanceof Structure);
		boolean uninhabitableBuilding = false;
		if (malfunctionable instanceof Building)
			uninhabitableBuilding = !((Building) malfunctionable).hasFunction(FunctionType.LIFE_SUPPORT);
		if (!(isStructure || uninhabitableBuilding))
			return 0;
		
		MalfunctionManager manager = malfunctionable.getMalfunctionManager();
		boolean hasMalfunction = manager.hasMalfunction();
		if (hasMalfunction)
			return 0;
		
		boolean hasParts = Maintenance.hasMaintenanceParts(settlement, malfunctionable);
		if (!hasParts)
			return 0;
		
		double effectiveTime = manager.getEffectiveTimeSinceLastMaintenance();
		boolean minTime = (effectiveTime >= 1000D);
		if (minTime) 
			result = effectiveTime;
		
		return result;
	}
}

/*
 * Mars Simulation Project
 * MaintenanceEVA.java
 * @date 2021-10-17
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.robot.Robot;
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
	private static final Logger logger = Logger.getLogger(MaintenanceEVA.class.getName());

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

//		if (shouldEndEVAOperation()) {
//        	if (person.isOutside())
//        		setPhase(WALK_BACK_INSIDE);
//        	else
//        		endTask();
//        	return;
//        }
			
		if (!person.isFit()) {
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
        	return;
		}
		
      	settlement = CollectionUtils.findSettlement(person.getCoordinates());
        if (settlement == null) {
        	if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
        	return;
        }
        	
		try {
			entity = getMaintenanceMalfunctionable();
			if (entity != null) {
				if (!Maintenance.hasMaintenanceParts(settlement, entity)) {		
		        	if (person.isOutside())
		        		setPhase(WALK_BACK_INSIDE);
		        	else
		        		endTask();
				    return;
				}
			}
			else {
	        	if (person.isOutside())
	        		setPhase(WALK_BACK_INSIDE);
	        	else
	        		endTask();
			    return;
			}
		}
		catch (Exception e) {
		    logger.log(Level.SEVERE,"MaintenanceEVA.constructor()",e);
        	if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
			return;
		}

        // Determine location for maintenance.
        Point2D maintenanceLoc = determineMaintenanceLocation();
        setOutsideSiteLocation(maintenanceLoc.getX(), maintenanceLoc.getY());

		// Initialize phase
		addPhase(MAINTAIN);

		logger.fine(person.getName() + " was starting " + getDescription());
	}

	public MaintenanceEVA(Robot robot) {
		super(NAME, robot, true, RandomUtil.getRandomDouble(50D) + 10D, SkillType.MECHANICS);

//		settlement = robot.getSettlement();
//
//		try {
//			entity = getMaintenanceMalfunctionable();
//			if (entity == null) {
//			    endTask();
//			    return;
//			}
//		}
//		catch (Exception e) {
//		    logger.log(Level.SEVERE,"MaintenanceEVA.constructor()",e);
//			endTask();
//		}
//
//        // Determine location for maintenance.
//        Point2D maintenanceLoc = determineMaintenanceLocation();
//        setOutsideSiteLocation(maintenanceLoc.getX(), maintenanceLoc.getY());
//
//		// Initialize phase
//		addPhase(MAINTAIN);
//
//		logger.finest(robot.getName() + " is starting " + getDescription());
	}

    /**
     * Determine location to perform maintenance.
     * @return location.
     */
    private Point2D determineMaintenanceLocation() {

        Point2D.Double newLocation = new Point2D.Double(0D, 0D);

        if (entity instanceof LocalBoundedObject) {
            LocalBoundedObject bounds = (LocalBoundedObject) entity;
            boolean goodLocation = false;
            for (int x = 0; (x < 50) && !goodLocation; x++) {
                Point2D.Double boundedLocalPoint = LocalAreaUtil.getRandomExteriorLocation(bounds, 1D);
                newLocation = LocalAreaUtil.getLocalRelativeLocation(boundedLocalPoint.getX(),
                        boundedLocalPoint.getY(), bounds);
                goodLocation = LocalAreaUtil.isLocationCollisionFree(newLocation.getX(), newLocation.getY(),
                        worker.getCoordinates());
            }
        }

        return newLocation;
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
	 * Perform the maintenance phase of the task.
	 * 
	 * @param time the time to perform this phase (in millisols)
	 * @return the time remaining after performing this phase (in millisols)
	 * @throws Exception if error during maintenance.
	 */
	private double maintenancePhase(double time) {
		
        // Check for radiation exposure during the EVA operation.
        if (isRadiationDetected(time)){
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
        }
	
        // Check if there is a reason to cut short and return.
        if (shouldEndEVAOperation() || addTimeOnSite(time)){
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
        }
        
		if (!person.isFit()) {
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
		}
		
		MalfunctionManager manager = entity.getMalfunctionManager();
		boolean malfunction = manager.hasMalfunction();
		boolean finishedMaintenance = (manager.getEffectiveTimeSinceLastMaintenance() < 1000D);

		if (person.isOutside() && (finishedMaintenance || malfunction)) {
			setPhase(WALK_BACK_INSIDE);
			return 0;
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

        // Add repair parts if necessary.
		Inventory inv = settlement.getInventory();
		entity = getMaintenanceMalfunctionable();
		
		if (entity != null && Maintenance.hasMaintenanceParts(settlement, entity)) {
			
			Map<Integer, Integer> parts = new HashMap<>(manager.getMaintenanceParts());
			Iterator<Integer> j = parts.keySet().iterator();
			while (j.hasNext()) {
				Integer part = j.next();
				int number = parts.get(part);
				inv.retrieveItemResources(part, number);
				manager.maintainWithParts(part, number);
				
				// Add item demand
				inv.addItemDemandTotalRequest(part, number);
				inv.addItemDemand(part, number);
			}

	        // Add work to the maintenance
			manager.addMaintenanceWorkTime(workTime);
			
	        // Add experience points
	        addExperience(time);

			// Check if an accident happens during maintenance.
			checkForAccident(time);
			
        }
		else {
			setPhase(WALK_BACK_INSIDE);
			return 0;
		}

		return 0D;
	}
	

	@Override
	protected void checkForAccident(double time) {

		// Use EVAOperation checkForAccident() method.
		super.checkForAccident(time);

		int skill = worker.getSkillManager().getEffectiveSkillLevel(SkillType.MECHANICS);
		checkForAccident(entity, time, 0.005D, skill, null);
	}

	/**
	 * Gets a random malfunctionable to perform maintenance on.
	 * @return malfunctionable or null.
	 * @throws Exception if error finding malfunctionable.
	 */
	private Malfunctionable getMaintenanceMalfunctionable() {
		Malfunctionable result = null;

		// Determine all malfunctionables local to the person.
		Map<Malfunctionable, Double> malfunctionables = new HashMap<Malfunctionable, Double>();

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
		else if (robot != null) {
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

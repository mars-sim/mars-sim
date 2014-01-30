/**
 * Mars Simulation Project
 * MaintenanceEVA.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import org.mars_sim.msp.core.Airlock;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Skill;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.Structure;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/** 
 * The Maintenance class is a task for performing
 * preventive maintenance on malfunctionable entities outdoors.
 */
public class MaintenanceEVA extends EVAOperation implements Serializable {
	
	private static Logger logger = Logger.getLogger(MaintenanceEVA.class.getName());
	
	// Task phase
	private static final String WALK_TO_MAINTENANCE = "Walk to Maintenance";
	private static final String MAINTAIN = "Maintenance";
	private static final String WALK_TO_AIRLOCK = "Walk to Airlock";
	
	// Data members
	private Malfunctionable entity; // Entity to be maintained.
	private Airlock airlock; // Airlock to be used for EVA.
	private double maintenanceXLoc;
    private double maintenanceYLoc;
    private double enterAirlockXLoc;
    private double enterAirlockYLoc;
	
	/** 
	 * Constructor
	 * @param person the person to perform the task
	 * @throws Exception if error constructing task.
	 */
	public MaintenanceEVA(Person person) {
		super("Performing EVA Maintenance", person);
		
		try {
			entity = getMaintenanceMalfunctionable();
			if (entity == null) {
			    endTask();
			    return;
			}
		}
		catch (Exception e) {
		    logger.log(Level.SEVERE,"MaintenanceEVA.constructor()",e);
			endTask();
		}
		
        // Determine location for maintenance.
        Point2D maintenanceLoc = determineMaintenanceLocation();
        maintenanceXLoc = maintenanceLoc.getX();
        maintenanceYLoc = maintenanceLoc.getY();
		
	    // Get an available airlock.
		if (entity instanceof LocalBoundedObject) {
		    LocalBoundedObject bounds = (LocalBoundedObject) entity;
		    airlock = getClosestWalkableAvailableAirlock(person, bounds.getXLocation(), 
		            bounds.getYLocation());
		}
		else {
		    airlock = getWalkableAvailableAirlock(person);
		}
		
        if (airlock == null) {
            endTask();
        }
        else {
            // Determine location for reentering building airlock.
            Point2D enterAirlockLoc = determineAirlockEnteringLocation();
            enterAirlockXLoc = enterAirlockLoc.getX();
            enterAirlockYLoc = enterAirlockLoc.getY();
        }
		
		// Initialize phase
        addPhase(WALK_TO_MAINTENANCE);
		addPhase(MAINTAIN);
		addPhase(WALK_TO_AIRLOCK);
		
		// logger.info(person.getName() + " is starting " + getDescription());
	}
	
	/** 
	 * Returns the weighted probability that a person might perform this task.
	 * It should return a 0 if there is no chance to perform this task given the person and his/her situation.
	 * @param person the person to perform the task
	 * @return the weighted probability that a person might perform this task
	 */
	public static double getProbability(Person person) {
		double result = 0D;

		try {
			// Total probabilities for all malfunctionable entities in person's local.
			Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(person).iterator();
			while (i.hasNext()) {
				Malfunctionable entity = i.next();
				boolean isStructure = (entity instanceof Structure);
				boolean uninhabitableBuilding = false;
				if (entity instanceof Building) 
					uninhabitableBuilding = !((Building) entity).hasFunction(LifeSupport.NAME);
				MalfunctionManager manager = entity.getMalfunctionManager();
				boolean hasMalfunction = manager.hasMalfunction();
				boolean hasParts = Maintenance.hasMaintenanceParts(person, entity);
				double effectiveTime = manager.getEffectiveTimeSinceLastMaintenance();
				boolean minTime = (effectiveTime >= 1000D);
				if ((isStructure || uninhabitableBuilding) && !hasMalfunction && minTime && hasParts) {
					double entityProb = manager.getEffectiveTimeSinceLastMaintenance() / 1000D;
					if (entityProb > 100D) entityProb = 100D;
					result += entityProb;
				}
			}   
		}
		catch (Exception e) {
		    logger.log(Level.SEVERE,"getProbability()",e);
    	}
		
		// Check if an airlock is available
		if (getWalkableAvailableAirlock(person) == null) result = 0D;

		// Check if it is night time.
		SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
		if (surface.getSurfaceSunlight(person.getCoordinates()) == 0) {
			if (!surface.inDarkPolarRegion(person.getCoordinates()))
				result = 0D;
		} 
		
		// Crowded settlement modifier
		if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
			Settlement settlement = person.getSettlement();
			if (settlement.getCurrentPopulationNum() > settlement.getPopulationCapacity()) result *= 2D;
		}
	
		// Effort-driven task modifier.
		result *= person.getPerformanceRating();
        
		// Job modifier.
		Job job = person.getMind().getJob();
		if (job != null) result *= job.getStartTaskProbabilityModifier(MaintenanceEVA.class);        
	
		return result;
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
                goodLocation = LocalAreaUtil.checkLocationCollision(newLocation.getX(), newLocation.getY(), 
                        person.getCoordinates());
            }
        }
        
        return newLocation;
    }
    
    /**
     * Determine location outside building airlock.
     * @return location.
     */
    private Point2D determineAirlockEnteringLocation() {
        
        Point2D result = null;
        
        // Move the person to a random location outside the airlock entity.
        if (airlock.getEntity() instanceof LocalBoundedObject) {
            LocalBoundedObject entityBounds = (LocalBoundedObject) airlock.getEntity();
            Point2D.Double newLocation = null;
            boolean goodLocation = false;
            for (int x = 0; (x < 20) && !goodLocation; x++) {
                Point2D.Double boundedLocalPoint = LocalAreaUtil.getRandomExteriorLocation(entityBounds, 1D);
                newLocation = LocalAreaUtil.getLocalRelativeLocation(boundedLocalPoint.getX(), 
                        boundedLocalPoint.getY(), entityBounds);
                goodLocation = LocalAreaUtil.checkLocationCollision(newLocation.getX(), newLocation.getY(), 
                        person.getCoordinates());
            }
            
            result = newLocation;
        }
        
        return result;
    }
	
    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time the phase is to be performed.
     * @return the remaining time after the phase has been performed.
     * @throws Exception if error in performing phase or if phase cannot be found.
     */
    protected double performMappedPhase(double time) {
    	if (getPhase() == null) {
    	    throw new IllegalArgumentException("Task phase is null");
    	}
    	else if (EVAOperation.EXIT_AIRLOCK.equals(getPhase())) {
    	    return exitEVAPhase(time);
    	}
        else if (WALK_TO_MAINTENANCE.equals(getPhase())) {
            return walkToMaintenanceEntityPhase(time);
        }
    	else if (MAINTAIN.equals(getPhase())) {
    	    return maintenancePhase(time);
    	}
        else if (WALK_TO_AIRLOCK.equals(getPhase())) {
            return walkToAirlockPhase(time);
        }
    	else if (EVAOperation.ENTER_AIRLOCK.equals(getPhase())) {
    	    return enterEVAPhase(time);
    	}
    	else {
    	    return time;
    	}
    }
    
	/**
	 * Adds experience to the person's skills used in this task.
	 * @param time the amount of time (ms) the person performed this task.
	 */
	protected void addExperience(double time) {
		
		// Add experience to "EVA Operations" skill.
		// (1 base experience point per 100 millisols of time spent)
		double evaExperience = time / 100D;
		
		// Experience points adjusted by person's "Experience Aptitude" attribute.
		NaturalAttributeManager nManager = person.getNaturalAttributeManager();
		int experienceAptitude = nManager.getAttribute(NaturalAttributeManager.EXPERIENCE_APTITUDE);
		double experienceAptitudeModifier = (((double) experienceAptitude) - 50D) / 100D;
		evaExperience += evaExperience * experienceAptitudeModifier;
		evaExperience *= getTeachingExperienceModifier();
		person.getMind().getSkillManager().addExperience(Skill.EVA_OPERATIONS, evaExperience);
		
		// If phase is maintenance, add experience to mechanics skill.
		if (MAINTAIN.equals(getPhase())) {
			// 1 base experience point per 100 millisols of time spent.
			// Experience points adjusted by person's "Experience Aptitude" attribute.
			double mechanicsExperience = time / 100D;
			mechanicsExperience += mechanicsExperience * experienceAptitudeModifier;
			person.getMind().getSkillManager().addExperience(Skill.MECHANICS, mechanicsExperience);
		}
	}
	
	/**
	 * Perform the exit airlock phase of the task.
	 * @param time the time to perform this phase (in millisols)
	 * @return the time remaining after performing this phase (in millisols)
	 * @throws Exception if error exiting the airlock.
	 */
	private double exitEVAPhase(double time) {
		
    	try {
    		time = exitAirlock(time, airlock);
        
    		// Add experience points
    		addExperience(time);
    	}
		catch (Exception e) {
			// Person unable to exit airlock.
			endTask();
		}
        
		if (exitedAirlock) {
		    setPhase(WALK_TO_MAINTENANCE);
		}
		return time;
	}
	
    /**
     * Perform the walk to maintenance location phase.
     * @param time the time available (millisols).
     * @return remaining time after performing phase (millisols).
     */
    private double walkToMaintenanceEntityPhase(double time) {
        
        // Check for an accident during the EVA walk.
        checkForAccident(time);
        
        // Check if there is reason to cut the EVA walk phase short and return
        // to the rover.
        if (shouldEndEVAOperation()) {
            setPhase(WALK_TO_AIRLOCK);
            return time;
        }
        
        // If not at maintenance location, create walk outside subtask.
        if ((person.getXLocation() != maintenanceXLoc) || (person.getYLocation() != maintenanceYLoc)) {
            Task walkingTask = new WalkOutside(person, person.getXLocation(), person.getYLocation(), 
                    maintenanceXLoc, maintenanceYLoc, false);
            addSubTask(walkingTask);
        }
        else {
            setPhase(MAINTAIN);
        }
        
        return time;
    }
    
    /**
     * Perform the walk to airlock phase.
     * @param time the time available (millisols).
     * @return remaining time after performing phase (millisols).
     */
    private double walkToAirlockPhase(double time) {
        
        // Check for an accident during the EVA walk.
        checkForAccident(time);
        
        // If not at outside airlock location, create walk outside subtask.
        if ((person.getXLocation() != enterAirlockXLoc) || (person.getYLocation() != enterAirlockYLoc)) {
            Task walkingTask = new WalkOutside(person, person.getXLocation(), person.getYLocation(), 
                    enterAirlockXLoc, enterAirlockYLoc, true);
            addSubTask(walkingTask);
        }
        else {
            setPhase(EVAOperation.ENTER_AIRLOCK);
        }
        
        return time;
    }
	
	/**
	 * Perform the maintenance phase of the task.
	 * @param time the time to perform this phase (in millisols)
	 * @return the time remaining after performing this phase (in millisols)
	 * @throws Exception if error during maintenance.
	 */
	private double maintenancePhase(double time) {
        
		MalfunctionManager manager = entity.getMalfunctionManager();
		boolean malfunction = manager.hasMalfunction();
		boolean finishedMaintenance = (manager.getEffectiveTimeSinceLastMaintenance() < 1000D);
        
		if (finishedMaintenance || malfunction || shouldEndEVAOperation()) {
			setPhase(WALK_TO_AIRLOCK);
			return time;
		}
        
		// Determine effective work time based on "Mechanic" skill.
		double workTime = time;
		int mechanicSkill = person.getMind().getSkillManager().getEffectiveSkillLevel(Skill.MECHANICS);
		if (mechanicSkill == 0) workTime /= 2;
		if (mechanicSkill > 1) workTime += workTime * (.2D * mechanicSkill);

        // Add repair parts if necessary.
		Inventory inv = containerUnit.getInventory();
		if (Maintenance.hasMaintenanceParts(inv, entity)) {
			Map<Part, Integer> parts = new HashMap<Part, Integer>(manager.getMaintenanceParts());
			Iterator<Part> j = parts.keySet().iterator();
			while (j.hasNext()) {
				Part part = j.next();
				int number = parts.get(part);
				inv.retrieveItemResources(part, number);
				manager.maintainWithParts(part, number);
			}
        }
		else {
			setPhase(WALK_TO_AIRLOCK);
			return time;
		}
        
        // Add work to the maintenance
		manager.addMaintenanceWorkTime(workTime);
		
        // Add experience points
        addExperience(time);
	
		// Check if an accident happens during maintenance.
		checkForAccident(time);

		return 0D;
	}  

	/**
	 * Perform the enter airlock phase of the task.
	 * @param time amount of time to perform the phase
	 * @return time remaining after performing the phase
	 * @throws Exception if error entering airlock.
	 */
	private double enterEVAPhase(double time) {
		time = enterAirlock(time, airlock);
		
        // Add experience points
        addExperience(time);
        
		if (enteredAirlock) {
		    endTask();
		}
		
		return time;
	}	
	
	/**
	 * Check for accident with entity during maintenance phase.
	 * @param time the amount of time (in millisols)
	 */
	protected void checkForAccident(double time) {

		// Use EVAOperation checkForAccident() method.
		super.checkForAccident(time);

		double chance = .001D;

		// Mechanic skill modification.
		int skill = person.getMind().getSkillManager().getEffectiveSkillLevel(Skill.MECHANICS);
		if (skill <= 3) chance *= (4 - skill);
		else chance /= (skill - 2);

		// Modify based on the entity's wear condition.
        chance *= entity.getMalfunctionManager().getWearConditionAccidentModifier();
		
		if (RandomUtil.lessThanRandPercent(chance * time)) {
			logger.info(person.getName() + " has accident while performing maintenance on " 
						     + entity.getName() + ".");
			entity.getMalfunctionManager().accident();
		}
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
        Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(person).iterator();
        while (i.hasNext()) {
            Malfunctionable entity = i.next();
            double probability = getProbabilityWeight(entity);
            if (probability > 0D) {
                malfunctionables.put(entity, probability);
            }
        }
        
        if (!malfunctionables.isEmpty()) {
            result = RandomUtil.getWeightedRandomObject(malfunctionables);
        }
		
		if (result != null) {
		    setDescription("Performing maintenance on " + result.getName());
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
			uninhabitableBuilding = !((Building) malfunctionable).hasFunction(LifeSupport.NAME);
		MalfunctionManager manager = malfunctionable.getMalfunctionManager();
		boolean hasMalfunction = manager.hasMalfunction();
		boolean hasParts = Maintenance.hasMaintenanceParts(person, malfunctionable);
		double effectiveTime = manager.getEffectiveTimeSinceLastMaintenance();
		boolean minTime = (effectiveTime >= 1000D); 
		if ((isStructure || uninhabitableBuilding) && !hasMalfunction && minTime && hasParts) result = effectiveTime;
		return result;
	}

	/**
	 * Gets the effective skill level a person has at this task.
	 * @return effective skill level
	 */
	public int getEffectiveSkillLevel() {
		SkillManager manager = person.getMind().getSkillManager();
		int EVAOperationsSkill = manager.getEffectiveSkillLevel(Skill.EVA_OPERATIONS);
		int mechanicsSkill = manager.getEffectiveSkillLevel(Skill.MECHANICS);
		return (int) Math.round((double)(EVAOperationsSkill + mechanicsSkill) / 2D); 
	}

	/**
	 * Gets a list of the skills associated with this task.
	 * May be empty list if no associated skills.
	 * @return list of skills as strings
	 */
	public List<String> getAssociatedSkills() {
		List<String> results = new ArrayList<String>(2);
		results.add(Skill.EVA_OPERATIONS);
		results.add(Skill.MECHANICS);
		return results;
	}
	
	@Override
	public void destroy() {
	    super.destroy();
	    
	    entity = null;
	    airlock = null;
	}
}
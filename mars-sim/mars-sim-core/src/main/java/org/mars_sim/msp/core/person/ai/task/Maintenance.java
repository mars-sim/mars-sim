/**
 * Mars Simulation Project
 * Maintenance.java
 * @version 3.02 2011-11-27
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Skill;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.core.vehicle.Vehicle;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/** The Maintenance class is a task for performing
 *  preventive maintenance on vehicles, settlements and equipment.
 */
public class Maintenance extends Task implements Serializable {
	
    private static Logger logger = Logger.getLogger(Maintenance.class.getName());

	// Task phase
	private static final String MAINTAIN = "Maintain";
	
	// Static members
	private static final double STRESS_MODIFIER = .1D; // The stress modified per millisol.

    // Data members
    private Malfunctionable entity; // Entity to be maintained.

    /** 
     * Constructor
     * @param person the person to perform the task
     * @throws Exception if error constructing task.
     */
    public Maintenance(Person person) {
        super("Performing Maintenance", person, true, false, STRESS_MODIFIER, 
        		true, RandomUtil.getRandomDouble(200D));

		try {
        	entity = getMaintenanceMalfunctionable();
        	if (entity == null) endTask();
		}
		catch (Exception e) {
		    	logger.log(Level.SEVERE,"Maintenance.constructor()",e);
			endTask();
		}
		
		// Initialize phase.
		addPhase(MAINTAIN);
		setPhase(MAINTAIN);
    }

    /** Returns the weighted probability that a person might perform this task.
     *  It should return a 0 if there is no chance to perform this task given the person and his/her situation.
     *  @param person the person to perform the task
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {
        double result = 0D;

        try {
        	// Total probabilities for all malfunctionable entities in person's local.
        	Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(person).iterator();
        	while (i.hasNext()) {
        		Malfunctionable entity = i.next();
        		boolean isVehicle = (entity instanceof Vehicle);
        		boolean uninhabitableBuilding = false;
        		if (entity instanceof Building) 
        			uninhabitableBuilding = !((Building) entity).hasFunction(LifeSupport.NAME);
        		MalfunctionManager manager = entity.getMalfunctionManager();
        		boolean hasMalfunction = manager.hasMalfunction();
        		boolean hasParts = hasMaintenanceParts(person, entity);
        		double effectiveTime = manager.getEffectiveTimeSinceLastMaintenance();
        		boolean minTime = (effectiveTime >= 1000D);
        		if (!hasMalfunction && !isVehicle && !uninhabitableBuilding && hasParts && minTime) {
        			double entityProb = effectiveTime / 1000D;
        			if (entityProb > 100D) entityProb = 100D;
        			result += entityProb;
        		}
            }
        }
        catch (Exception e) {
            logger.log(Level.SEVERE,"getProbability()",e);
    	}
	
        // Effort-driven task modifier.
        result *= person.getPerformanceRating();
        
		// Job modifier.
        Job job = person.getMind().getJob();
		if (job != null) result *= job.getStartTaskProbabilityModifier(Maintenance.class);        
	
        return result;
    }
    
    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time (millisol) the phase is to be performed.
     * @return the remaining time (millisol) after the phase has been performed.
     * @throws Exception if error in performing phase or if phase cannot be found.
     */
    protected double performMappedPhase(double time) {
    	if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
    	if (MAINTAIN.equals(getPhase())) return maintainPhase(time);
    	else return time;
    }
    
    /**
     * Performs the maintain phase.
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     * @throws Exception if error performing the phase.
     */
    private double maintainPhase(double time) {
        MalfunctionManager manager = entity.getMalfunctionManager();
    	
        // If person is incapacitated, end task.
        if (person.getPerformanceRating() == 0D) endTask();

        // Check if maintenance has already been completed.
        if (manager.getEffectiveTimeSinceLastMaintenance() < 1000D) endTask();

        // If equipment has malfunction, end task.
        if (manager.hasMalfunction()) endTask();

        if (isDone()) return time;
    	
        // Determine effective work time based on "Mechanic" skill.
        double workTime = time;
        int mechanicSkill = getEffectiveSkillLevel();
        if (mechanicSkill == 0) workTime /= 2;
        if (mechanicSkill > 1) workTime += workTime * (.2D * mechanicSkill);

        // Add repair parts if necessary.
        boolean repairParts = false;
        Unit container = person.getTopContainerUnit();
        if (container != null) {
        	Inventory inv = container.getInventory();
        	if (Maintenance.hasMaintenanceParts(inv, entity)) {
        		repairParts = true;
        		Map<Part, Integer> parts = new HashMap<Part, Integer>(manager.getMaintenanceParts());
        		Iterator<Part> j = parts.keySet().iterator();
        		while (j.hasNext()) {
        			Part part = j.next();
        			int number = parts.get(part);
        			inv.retrieveItemResources(part, number);
        			manager.maintainWithParts(part, number);
        		}
        	}
        }
        if (!repairParts) {
        	endTask();
        	return time;
        }
        
        // Add work to the maintenance
        manager.addMaintenanceWorkTime(workTime);
            
        // Add experience points
        addExperience(time);
            
        // If maintenance is complete, task is done.
        if (manager.getEffectiveTimeSinceLastMaintenance() == 0D) endTask();
            
        // Check if an accident happens during maintenance.
        checkForAccident(time);
    	
        return 0D;
    }
    
	/**
	 * Adds experience to the person's skills used in this task.
	 * @param time the amount of time (ms) the person performed this task.
	 */
	protected void addExperience(double time) {
		// Add experience to "Mechanics" skill
		// (1 base experience point per 100 millisols of work)
		// Experience points adjusted by person's "Experience Aptitude" attribute.
        double newPoints = time / 100D;
        int experienceAptitude = person.getNaturalAttributeManager().getAttribute(
        	NaturalAttributeManager.EXPERIENCE_APTITUDE);
        newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
		newPoints *= getTeachingExperienceModifier();
        person.getMind().getSkillManager().addExperience(Skill.MECHANICS, newPoints);
	}

    /**
     * Check for accident with entity during maintenance phase.
     * @param time the amount of time (in millisols)
     */
    private void checkForAccident(double time) {

        double chance = .001D;

        // Mechanic skill modification.
        int skill = person.getMind().getSkillManager().getEffectiveSkillLevel(Skill.MECHANICS);
        if (skill <= 3) chance *= (4 - skill);
        else chance /= (skill - 2);

        // Modify based on the entity's wear condition.
        chance *= entity.getMalfunctionManager().getWearConditionAccidentModifier();
        
        if (RandomUtil.lessThanRandPercent(chance * time)) {
            logger.info(person.getName() + " has accident while performing maintenance on " 
        	    		         + entity.getName() 
        	    		         + ".");
            entity.getMalfunctionManager().accident();
        }
    }

    /** 
     * Gets the entity the person is maintaining.
     * Returns null if none.
     * @return entity
     */
    public Malfunctionable getEntity() {
        return entity;
    }
    
    /**
     * Gets a random malfunctionable to perform maintenance on.
     * @return malfunctionable or null.
     * @throws Exception if error finding malfunctionable.
     */
    private Malfunctionable getMaintenanceMalfunctionable() {
    	Malfunctionable result = null;
    	
		// Determine entity to maintain.
		double totalProbabilityWeight = 0D;
		
		// Total probabilities for all malfunctionable entities in person's local.
		Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(person).iterator();
		while (i.hasNext()) totalProbabilityWeight += getProbabilityWeight(i.next());
		
		// Randomly determine a malfunctionable entity.
		double chance = RandomUtil.getRandomDouble(totalProbabilityWeight);
		
		// Get the malfunctionable entity chosen.
		i = MalfunctionFactory.getMalfunctionables(person).iterator();
		while (i.hasNext()) {
			Malfunctionable malfunctionable = i.next();
			double entityWeight = getProbabilityWeight(malfunctionable);
			if (chance < entityWeight) {
				result = malfunctionable;
				setDescription("Performing maintenance on " + result.getName());
				if (result instanceof Building)
				if (isInhabitableBuilding(malfunctionable)) 
					BuildingManager.addPersonToBuilding(person, (Building) result); 
				break;
			}
			else chance -= entityWeight;
		}
    	
    	return result;
    }
    
    /**
     * Checks if a malfunctionable is an inhabitable building.
     * @param malfunctionable the malfunctionable.
     * @return true if inhabitable building.
     */
    private boolean isInhabitableBuilding(Malfunctionable malfunctionable) {
    	boolean result = false;
    	if (malfunctionable instanceof Building) {
    		Building building = (Building) malfunctionable;
    		if (building.hasFunction(LifeSupport.NAME)) result = true;
    	}
    	return result;
    }
    
    /**
     * Gets the probability weight for a malfunctionable.
     * @param malfunctionable the malfunctionable
     * @return the probability weight.
     * @throws Exception if error determining probability weight.
     */
    private double getProbabilityWeight(Malfunctionable malfunctionable)  {
    	double result = 0D;
    	boolean isVehicle = (malfunctionable instanceof Vehicle);
		boolean uninhabitableBuilding = false;
		if (malfunctionable instanceof Building) 
			uninhabitableBuilding = !((Building) malfunctionable).hasFunction(LifeSupport.NAME);
		MalfunctionManager manager = malfunctionable.getMalfunctionManager();
		boolean hasMalfunction = manager.hasMalfunction();
		double effectiveTime = manager.getEffectiveTimeSinceLastMaintenance();
		boolean minTime = (effectiveTime >= 1000D); 
		boolean enoughParts = hasMaintenanceParts(person, malfunctionable);
		if (!isVehicle && !uninhabitableBuilding && !hasMalfunction && minTime && enoughParts) {
			result = effectiveTime;
			if (malfunctionable instanceof Building) {
				Building building = (Building) malfunctionable;
				if (isInhabitableBuilding(malfunctionable)) {
					result *= Task.getCrowdingProbabilityModifier(person, building);
					result *= Task.getRelationshipModifier(person, building);
				}
			}
		}
		return result;
    }
    
    /**
     * Checks if there are enough local parts to perform maintenance.
     * @param person the person performing the maintenance.
     * @param malfunctionable the entity needing maintenance.
     * @return true if enough parts.
     * @throws Exception if error checking parts availability.
     */
    static boolean hasMaintenanceParts(Person person, Malfunctionable malfunctionable) {
    	Inventory inv = null;
    	if (person.getTopContainerUnit() != null) inv = person.getTopContainerUnit().getInventory();
    	else inv = person.getInventory();
    	return hasMaintenanceParts(inv, malfunctionable);
    }
    
    /**
     * Checks if there are enough local parts to perform maintenance.
     * @param inventory inventory holding the needed parts.
     * @param malfunctionable the entity needing maintenance.
     * @return true if enough parts.
     * @throws Exception if error checking parts availability.
     */
    static boolean hasMaintenanceParts(Inventory inv, Malfunctionable malfunctionable) {
    	boolean result = true;
    	
    	Map<Part, Integer> parts = malfunctionable.getMalfunctionManager().getMaintenanceParts();
    	Iterator<Part> i = parts.keySet().iterator();
    	while (i.hasNext()) {
    		Part part = i.next();
    		int number = parts.get(part);
    		if (inv.getItemResourceNum(part) < number) result = false;
    	}
    	
    	return result;
    }
    
	/**
	 * Gets the effective skill level a person has at this task.
	 * @return effective skill level
	 */
	public int getEffectiveSkillLevel() {
		SkillManager manager = person.getMind().getSkillManager();
		return manager.getEffectiveSkillLevel(Skill.MECHANICS);
	}   
	
	/**
	 * Gets a list of the skills associated with this task.
	 * May be empty list if no associated skills.
	 * @return list of skills as strings
	 */
	public List<String> getAssociatedSkills() {
		List<String> results = new ArrayList<String>(1);
		results.add(Skill.MECHANICS);
		return results;
	} 
	
	@Override
	public void destroy() {
	    super.destroy();
	    
	    entity = null;
	}
}
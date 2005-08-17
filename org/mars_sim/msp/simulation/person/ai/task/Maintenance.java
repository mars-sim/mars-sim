/**
 * Mars Simulation Project
 * Maintenance.java
 * @version 2.78 2005-08-14
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.malfunction.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.job.Job;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.LifeSupport;
import org.mars_sim.msp.simulation.vehicle.Vehicle;

/** The Maintenance class is a task for performing
 *  preventive maintenance on vehicles, settlements and equipment.
 */
public class Maintenance extends Task implements Serializable {

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
    public Maintenance(Person person) throws Exception {
        super("Performing Maintenance", person, true, false, STRESS_MODIFIER, 
        		true, RandomUtil.getRandomDouble(100D));

		try {
        	entity = getMaintenanceMalfunctionable();
        	if (entity == null) endTask();
		}
		catch (Exception e) {
			System.err.println("Maintenance.constructor(): " + e.getMessage());
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

        // Total probabilities for all malfunctionable entities in person's local.
        Iterator i = MalfunctionFactory.getMalfunctionables(person).iterator();
        while (i.hasNext()) {
            Malfunctionable entity = (Malfunctionable) i.next();
            boolean isVehicle = (entity instanceof Vehicle);
            boolean uninhabitableBuilding = false;
            if (entity instanceof Building) 
            	uninhabitableBuilding = !((Building) entity).hasFunction(LifeSupport.NAME);
            MalfunctionManager manager = entity.getMalfunctionManager();
            boolean hasMalfunction = manager.hasMalfunction();
            if (!hasMalfunction && !isVehicle && !uninhabitableBuilding) {
                double entityProb = manager.getEffectiveTimeSinceLastMaintenance() / 1000D;
                if (entityProb > 100D) entityProb = 100D;
                result += entityProb;
            }   
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
    protected double performMappedPhase(double time) throws Exception {
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
    private double maintainPhase(double time) throws Exception {
        MalfunctionManager manager = entity.getMalfunctionManager();
    	
        // If person is incompacitated, end task.
        if (person.getPerformanceRating() == 0D) endTask();

        // Check if maintenance has already been completed.
        if (manager.getEffectiveTimeSinceLastMaintenance() == 0D) endTask();

        // If equipment has malfunction, end task.
        if (manager.hasMalfunction()) endTask();

        if (isDone()) return time;
    	
        // Determine effective work time based on "Mechanic" skill.
        double workTime = time;
        int mechanicSkill = getEffectiveSkillLevel();
        if (mechanicSkill == 0) workTime /= 2;
        if (mechanicSkill > 1) workTime += workTime * (.2D * mechanicSkill);

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
        person.getSkillManager().addExperience(Skill.MECHANICS, newPoints);
	}

    /**
     * Check for accident with entity during maintenance phase.
     * @param time the amount of time (in millisols)
     */
    private void checkForAccident(double time) {

        double chance = .001D;

        // Mechanic skill modification.
        int skill = person.getSkillManager().getEffectiveSkillLevel(Skill.MECHANICS);
        if (skill <= 3) chance *= (4 - skill);
        else chance /= (skill - 2);

        if (RandomUtil.lessThanRandPercent(chance * time)) {
            // System.out.println(person.getName() + " has accident while performing maintenance on " + entity.getName() + ".");
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
    private Malfunctionable getMaintenanceMalfunctionable() throws Exception {
    	Malfunctionable result = null;
    	
		// Determine entity to maintain.
		double totalProbabilityWeight = 0D;
		
		// Total probabilities for all malfunctionable entities in person's local.
		Iterator i = MalfunctionFactory.getMalfunctionables(person).iterator();
		while (i.hasNext()) {
			Malfunctionable e = (Malfunctionable) i.next();
			boolean isVehicle = (e instanceof Vehicle);
			boolean uninhabitableBuilding = false;
			if (e instanceof Building) 
				uninhabitableBuilding = !((Building) e).hasFunction(LifeSupport.NAME);
			MalfunctionManager manager = e.getMalfunctionManager();
			boolean hasMalfunction = manager.hasMalfunction();
			if (!isVehicle && !uninhabitableBuilding && !hasMalfunction) {
				double entityWeight = manager.getEffectiveTimeSinceLastMaintenance();
				if (e instanceof Building) {
					Building building = (Building) e;
					if (building.hasFunction(LifeSupport.NAME)) {
						entityWeight *= Task.getCrowdingProbabilityModifier(person, building);
						entityWeight *= Task.getRelationshipModifier(person, building);
					}
				}
				totalProbabilityWeight += entityWeight;
			}
		}
		
		// Randomly determine a malfunctionable entity.
		double chance = RandomUtil.getRandomDouble(totalProbabilityWeight);
		
		// Get the malfunctionable entity chosen.
		i = MalfunctionFactory.getMalfunctionables(person).iterator();
		while (i.hasNext()) {
			Malfunctionable malfunctionable = (Malfunctionable) i.next();
			boolean isVehicle = (malfunctionable instanceof Vehicle);
			boolean uninhabitableBuilding = false;
			if (malfunctionable instanceof Building) 
				uninhabitableBuilding = !((Building) malfunctionable).hasFunction(LifeSupport.NAME);
			MalfunctionManager manager = malfunctionable.getMalfunctionManager();
			boolean hasMalfunction = manager.hasMalfunction();
			if (!isVehicle && !uninhabitableBuilding && !hasMalfunction) {
				double entityWeight = manager.getEffectiveTimeSinceLastMaintenance();
				boolean inhabitableBuilding = false;
				if (malfunctionable instanceof Building) {
					Building building = (Building) malfunctionable;
					if (building.hasFunction(LifeSupport.NAME)) {
						inhabitableBuilding = true; 
						entityWeight *= Task.getCrowdingProbabilityModifier(person, building);
						entityWeight *= Task.getRelationshipModifier(person, building);
					}
				}
			
				if (chance < entityWeight) {
					result = malfunctionable;
					description = "Performing maintenance on " + result.getName();
					if (inhabitableBuilding) BuildingManager.addPersonToBuilding(person, (Building) result); 
					break;
				}
				else chance -= entityWeight;
			}
		}
    	
    	return result;
    }
    
	/**
	 * Gets the effective skill level a person has at this task.
	 * @return effective skill level
	 */
	public int getEffectiveSkillLevel() {
		SkillManager manager = person.getSkillManager();
		return manager.getEffectiveSkillLevel(Skill.MECHANICS);
	}   
	
	/**
	 * Gets a list of the skills associated with this task.
	 * May be empty list if no associated skills.
	 * @return list of skills as strings
	 */
	public List getAssociatedSkills() {
		List results = new ArrayList();
		results.add(Skill.MECHANICS);
		return results;
	} 
}
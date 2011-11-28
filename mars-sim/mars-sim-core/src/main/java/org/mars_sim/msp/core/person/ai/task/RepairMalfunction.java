/**
 * Mars Simulation Project
 * RepairMalfunction.java
 * @version 3.02 2011-11-27
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LifeSupport;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.malfunction.Malfunction;
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

import java.io.Serializable;
import java.util.*;

/**
 * The RepairMalfunction class is a task to repair a malfunction.
 */
public class RepairMalfunction extends Task implements Repair, Serializable {

	// Task phase
	private static final String REPAIRING = "Repairing";
	
	// Static members
	private static final double STRESS_MODIFIER = .3D; // The stress modified per millisol.

    // Data members
    private Malfunctionable entity; // Entity being repaired.

    /**
     * Constructor
     * @param person the person to perform the task
     * @throws Exception if error constructing task.
     */
    public RepairMalfunction(Person person) {
        super("Repairing Malfunction", person, true, false, STRESS_MODIFIER, true, RandomUtil.getRandomDouble(200D));

        // Get the malfunctioning entity.
        entity = getMalfunctionEntity(person);
        if (entity != null) {
        	// Add person to building if malfunctionable is a building with life support.
        	addPersonToMalfunctionableBuilding(entity); 
        }
        else endTask();
        
        // Initialize phase
        addPhase(REPAIRING);
        setPhase(REPAIRING);
        
        // logger.info(person.getName() + " repairing malfunction.");
    }
    
    /**
     * Gets a malfunctional entity with a normal malfunction for a user.
     * @param person the person.
     * @return malfunctional entity.
     * @throws Exception if error checking if error finding entity.
     */
    private static Malfunctionable getMalfunctionEntity(Person person) {
        Malfunctionable result = null;
        
        Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(person).iterator();
        while (i.hasNext() && (result == null)) {
            Malfunctionable entity = i.next();
            if (hasMalfunction(person, entity)) result = entity;
        }
        
        return result;
    }
    
    /**
     * Gets a malfunctional entity with a normal malfunction for a user.
     * @return malfunctional entity.
     * @throws Exception if error checking if error finding entity.
     */
    private static boolean hasMalfunction(Person person, Malfunctionable entity) {
    	boolean result = false;
    	
    	MalfunctionManager manager = entity.getMalfunctionManager();
        Iterator<Malfunction> i = manager.getNormalMalfunctions().iterator();
        while (i.hasNext() && !result) {
           	if (hasRepairPartsForMalfunction(person, i.next())) result = true;
        }
    	
    	return result;
    }
    
    /**
     * Checks if there are enough repair parts at person's location to fix the malfunction.
     * @param person the person checking.
     * @param malfunction the malfunction.
     * @return true if enough repair parts to fix malfunction.
     * @throws Exception if error checking for repair parts.
     */
    private static boolean hasRepairPartsForMalfunction(Person person, Malfunction malfunction) 
    		{
    	if (person == null) throw new IllegalArgumentException("person is null");
    	if (malfunction == null) throw new IllegalArgumentException("malfunction is null");
    	
    	boolean result = false;    	
    	Unit containerUnit = person.getTopContainerUnit();

    	if (containerUnit != null) {
    		result = true;
    		Inventory inv = containerUnit.getInventory();
    	
    		Map<Part, Integer> repairParts = malfunction.getRepairParts();
    		Iterator<Part> i = repairParts.keySet().iterator();
    		while (i.hasNext() && result) {
    			Part part = i.next();
    			int number = repairParts.get(part);
    			if (inv.getItemResourceNum(part) < number) result = false;
    		}
    	}
    	
    	return result;
    }
    
    /** 
     *  Returns the weighted probability that a person might perform this task.
     *  @param person the person to perform the task
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {
        double result = 0D;

        // Total probabilities for all malfunctionable entities in person's local.
        Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(person).iterator();
        while (i.hasNext()) {
            Malfunctionable entity = i.next();
            MalfunctionManager manager = entity.getMalfunctionManager();
            Iterator<Malfunction> j = manager.getNormalMalfunctions().iterator();
            while (j.hasNext()) {
            	Malfunction malfunction = j.next();
            	try {
            		if (hasRepairPartsForMalfunction(person, malfunction)) result += 100D;
            	}
            	catch (Exception e) {
            		e.printStackTrace(System.err);
            	}
            }
        }

        // Effort-driven task modifier.
        result *= person.getPerformanceRating();
        
		// Job modifier.
        Job job = person.getMind().getJob();
		if (job != null) result *= job.getStartTaskProbabilityModifier(RepairMalfunction.class);        

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
    	if (REPAIRING.equals(getPhase())) return repairingPhase(time);
    	else return time;
    }
    
    /**
     * Performs the repairing phase of the task.
     * @param time the amount of time (millisol) to perform the phase.
     * @return the amount of time (millisol) left after performing the phase.
     * @throws Exception if error performing the phase.
     */
    private double repairingPhase(double time) {
    	
        // Check if there are no more malfunctions.
        if (!hasMalfunction(person, entity)) endTask();

        if (isDone()) return time;
        
        // Determine effective work time based on "Mechanic" skill.
        double workTime = time;
        int mechanicSkill = person.getMind().getSkillManager().getEffectiveSkillLevel(Skill.MECHANICS);
        if (mechanicSkill == 0) workTime /= 2;
        if (mechanicSkill > 1) workTime += workTime * (.2D * mechanicSkill);

        // Get a local malfunction.
        Malfunction malfunction = null;
        Iterator<Malfunction> i = entity.getMalfunctionManager().getNormalMalfunctions().iterator();
        while (i.hasNext() && (malfunction == null)) {
           	Malfunction tempMalfunction = i.next();
           	if (hasRepairPartsForMalfunction(person, tempMalfunction)) {
           		malfunction = tempMalfunction;
           		setDescription("Repairing " + malfunction.getName() + " on " + entity.getName());
            }
        }
        
        // Add repair parts if necessary.
        if (hasRepairPartsForMalfunction(person, malfunction)) {
        	Inventory inv = person.getTopContainerUnit().getInventory();
        	Map<Part, Integer> parts = new HashMap<Part, Integer>(malfunction.getRepairParts());
        	Iterator<Part> j = parts.keySet().iterator();
        	while (j.hasNext()) {
        		Part part = j.next();
        		int number = parts.get(part);
        		inv.retrieveItemResources(part, number);
        		malfunction.repairWithParts(part, number);
        	}
        }
        else {
        	endTask();
        	return time;
        }

        // Add work to malfunction.
        // logger.info(description);
        double workTimeLeft = malfunction.addWorkTime(workTime);

        // Add experience
        addExperience(time);
        
        // Check if an accident happens during maintenance.
        checkForAccident(time);

        // Check if there are no more malfunctions.
        if (!hasMalfunction(person, entity)) endTask();
        
        return (workTimeLeft / workTime) / time;
    }
    
	/**
	 * Adds experience to the person's skills used in this task.
	 * @param time the amount of time (ms) the person performed this task.
	 */
	protected void addExperience(double time) {
		// Add experience to "Mechanics" skill
		// (1 base experience point per 20 millisols of work)
		// Experience points adjusted by person's "Experience Aptitude" attribute.
        double newPoints = time / 20D;
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
            // logger.info(person.getName() + " has accident while " + description);
            if (entity != null) entity.getMalfunctionManager().accident();
        }
    }

    /**
     * Gets the malfunctionable entity the person is currently repairing or null if none.
     * @return entity
     */
    public Malfunctionable getEntity() {
        return entity;
    }
    
    /**
     * Adds the person to building if malfunctionable is a building with life support.
     * Otherwise does nothing.
     * @param malfunctionable the malfunctionable the person is repairing.
     */
    private void addPersonToMalfunctionableBuilding(Malfunctionable malfunctionable) {
    	
    	if (malfunctionable instanceof Building) {
    		Building building = (Building) malfunctionable;
            if(building instanceof LifeSupport){
    			BuildingManager.addPersonToBuilding(person, building);
            }
    	}
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
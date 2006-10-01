/**
 * Mars Simulation Project
 * RepairMalfunction.java
 * @version 2.78 2005-08-14
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.malfunction.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.Skill;
import org.mars_sim.msp.simulation.person.ai.SkillManager;
import org.mars_sim.msp.simulation.person.ai.job.Job;
import org.mars_sim.msp.simulation.structure.building.*;

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
    public RepairMalfunction(Person person) throws Exception {
        super("Repairing Malfunction", person, true, false, STRESS_MODIFIER, true, RandomUtil.getRandomDouble(100D));

        // Initialize phase
        addPhase(REPAIRING);
        setPhase(REPAIRING);
        
        // System.out.println(person.getName() + " repairing malfunction.");
    }

    /**
     * Checks if the person has a local malfunction.
     * @return true if malfunction, false if none.
     */
    public static boolean hasMalfunction(Person person) {

        boolean result = false;

        Iterator i = MalfunctionFactory.getMalfunctionables(person).iterator();
        while (i.hasNext()) {
            MalfunctionManager manager = ((Malfunctionable) i.next()).getMalfunctionManager();
            if (manager.hasNormalMalfunction()) result = true;
        }

        return result;
    }

    /** Returns the weighted probability that a person might perform this task.
     *  @param person the person to perform the task
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {
        double result = 0D;

        // Total probabilities for all malfunctionable entities in person's local.
        Iterator i = MalfunctionFactory.getMalfunctionables(person).iterator();
        while (i.hasNext()) {
            // MalfunctionManager manager = ((Malfunctionable) i.next()).getMalfunctionManager();
            Malfunctionable entity = (Malfunctionable) i.next();
            MalfunctionManager manager = entity.getMalfunctionManager();
            if (manager.hasNormalMalfunction()) result = 100D;
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
    protected double performMappedPhase(double time) throws Exception {
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
    private double repairingPhase(double time) throws Exception {
    	
        // Check if there are no more malfunctions.
        if (!hasMalfunction(person)) endTask();

        if (isDone()) return time;
        
        // Determine effective work time based on "Mechanic" skill.
        double workTime = time;
        int mechanicSkill = person.getMind().getSkillManager().getEffectiveSkillLevel(Skill.MECHANICS);
        if (mechanicSkill == 0) workTime /= 2;
        if (mechanicSkill > 1) workTime += workTime * (.2D * mechanicSkill);

        // Get a local malfunction.
        Malfunction malfunction = null;
        Iterator i = MalfunctionFactory.getMalfunctionables(person).iterator();
        while (i.hasNext()) {
            Malfunctionable e = (Malfunctionable) i.next();
            MalfunctionManager manager = e.getMalfunctionManager();
            if (manager.hasNormalMalfunction()) {
                malfunction = manager.getMostSeriousNormalMalfunction();
            	setDescription("Repairing " + malfunction.getName() + " on " + e);
            	entity = e;
            	// Add person to building if malfunctionable is a building with life support.
            	addPersonToMalfunctionableBuilding(e);
            	break;
            }
        }

        // Add work to malfunction.
        // System.out.println(description);
        double workTimeLeft = malfunction.addWorkTime(workTime);

        // Add experience
        addExperience(time);
        
        // Check if an accident happens during maintenance.
        checkForAccident(time);

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

        if (RandomUtil.lessThanRandPercent(chance * time)) {
            // System.out.println(person.getName() + " has accident while " + description);
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
    		try {
    			BuildingManager.addPersonToBuilding(person, building);
    		}
    		catch (BuildingException e) {}
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
	public List getAssociatedSkills() {
		List results = new ArrayList();
		results.add(Skill.MECHANICS);
		return results;
	}
}
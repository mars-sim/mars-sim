/**
 * Mars Simulation Project
 * RepairEVAMalfunction.java
 * @version 2.77 2004-08-09
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.Iterator;
import org.mars_sim.msp.simulation.Airlock;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.malfunction.*;
import org.mars_sim.msp.simulation.mars.SurfaceFeatures;
import org.mars_sim.msp.simulation.person.*;

/**
 * The RepairEVAMalfunction class is a task to repair a malfunction requiring an EVA.
 */
public class RepairEVAMalfunction extends EVAOperation implements Repair, Serializable {

    // Phase names
    private static final String REPAIR_MALFUNCTION = "Repair Malfunction";
	
    // Data members
    private Malfunctionable entity; // The malfunctionable entity being repaired.
    private Airlock airlock; // The airlock to be used.
    private double duration; // Duration of task in millisols.
	
    /**
     * Constructs a RepairEVAMalfunction object.
     * @param person the person to perform the task
     */
    public RepairEVAMalfunction(Person person) {
        super("Repairing EVA Malfunction", person);

        // Get the malfunctioning entity.
        entity = getEVAMalfunctionEntity(person);
        if (entity == null) endTask();
        
        // Get an available airlock.
        airlock = getAvailableAirlock(person);
        if (airlock == null) endTask();

        phase = EXIT_AIRLOCK;

        // System.out.println(person.getName() + " has started the RepairEVAMalfunction task.");
        
        // Randomly determine duration, from 0 - 500 millisols.
        duration = RandomUtil.getRandomDouble(500D);
    }

    /**
     * Checks if the person has a local EVA malfunction.
     * @return true if malfunction, false if none.
     */
    public static boolean hasEVAMalfunction(Person person) {
   
        boolean result = false;

        Iterator i = MalfunctionFactory.getMalfunctionables(person).iterator();
        while (i.hasNext()) {
            MalfunctionManager manager = ((Malfunctionable) i.next()).getMalfunctionManager();
            if (manager.hasEVAMalfunction()) result = true;
        }

        return result;
    }

    /**
     * Checks if the malfunctionable entity has a local EVA malfunction.
     * @return true if malfunction, false if none.
     */
    public static boolean hasEVAMalfunction(Malfunctionable entity) {
   
        boolean result = false;

        Iterator i = MalfunctionFactory.getMalfunctionables(entity).iterator();
        while (i.hasNext()) {
            MalfunctionManager manager = ((Malfunctionable) i.next()).getMalfunctionManager();
            if (manager.hasEVAMalfunction()) result = true;
        }

        return result;
    }
    
    /**
     * Gets a malfunctional entity with an EVA malfunction for a user.
     * 
     * @return malfunctional entity with EVA malfunction or null if none.
     */
    public static Malfunctionable getEVAMalfunctionEntity(Person person) {
        Malfunctionable result = null;
        
        Iterator i = MalfunctionFactory.getMalfunctionables(person).iterator();
        while (i.hasNext()) {
            Malfunctionable entity = (Malfunctionable) i.next();
            MalfunctionManager manager = entity.getMalfunctionManager();
            if (manager.hasEVAMalfunction()) result = entity;
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
            MalfunctionManager manager = ((Malfunctionable) i.next()).getMalfunctionManager();
            if (manager.hasEVAMalfunction()) result = 100D;
        }

        // Check if an airlock is available
        if (getAvailableAirlock(person) == null) result = 0D;

        // Check if it is night time.
        SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
        if (surface.getSurfaceSunlight(person.getCoordinates()) == 0) {
			if (!surface.inDarkPolarRegion(person.getCoordinates()))
        		result = 0D;
        } 
	
        // Effort-driven task modifier.
        result *= person.getPerformanceRating();
        
		// Job modifier.
		result *= person.getMind().getJob().getStartTaskProbabilityModifier(RepairEVAMalfunction.class);        

        return result;
    }
    
    /**
     * Perform the task.
     * @param time the amount of time (millisols) to perform the task
     * @return amount of time remaining after performing the task
     * @throws Exception if error performing task.
     */
    double performTask(double time) throws Exception {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

        while ((timeLeft > 0D) && !isDone()) {
            if (phase.equals(EXIT_AIRLOCK)) timeLeft = exitEVA(timeLeft);
            else if (phase.equals(REPAIR_MALFUNCTION)) timeLeft = repairMalfunction(timeLeft);
            else if (phase.equals(ENTER_AIRLOCK)) timeLeft = enterEVA(timeLeft);
        }					            
	
        // Add experience to "EVA Operations" skill.
        // (1 base experience point per 20 millisols of time spent)
        // Experience points adjusted by person's "Experience Aptitude" attribute.
        double experience = time / 50D;
        NaturalAttributeManager nManager = person.getNaturalAttributeManager();
        experience += experience * (((double) nManager.getAttribute("Experience Aptitude") - 50D) / 100D);
        person.getSkillManager().addExperience(Skill.EVA_OPERATIONS, experience);

        return timeLeft;
    }

    /**
     * Perform the exit airlock phase of the task.
     * @param time the time to perform this phase (in millisols)
     * @return the time remaining after performing this phase (in millisols)
     */
    private double exitEVA(double time) {
        try {
            time = exitAirlock(time, airlock);
        }
        catch (Exception e) { 
            // System.err.println(e.getMessage()); 
        }
        
        if (exitedAirlock) phase = REPAIR_MALFUNCTION;
        return time;
    }

    /**
     * Perform the repair malfunction phase of the task.
     * @param time the time to perform this phase (in millisols)
     * @return the time remaining after performing this phase (in millisols)
     */
    private double repairMalfunction(double time) {
        
        if (!hasEVAMalfunction(entity) || shouldEndEVAOperation()) {
            phase = ENTER_AIRLOCK;
            return time;
        }
	    
        // Determine effective work time based on "Mechanic" skill.
        double workTime = time;
        int mechanicSkill = person.getSkillManager().getEffectiveSkillLevel(Skill.MECHANICS);
        if (mechanicSkill == 0) workTime /= 2;
        if (mechanicSkill > 1) workTime += workTime * (.2D * mechanicSkill);

        // Get a local malfunction.
        Malfunction malfunction = null;
        Iterator i = MalfunctionFactory.getMalfunctionables(entity).iterator();
        while (i.hasNext()) {
            Malfunctionable e = (Malfunctionable) i.next();
            MalfunctionManager manager = e.getMalfunctionManager();
            if (manager.hasEVAMalfunction()) {
                malfunction = manager.getMostSeriousEVAMalfunction();
                description = "Repairing " + malfunction.getName() + " on " + e.getName();
                entity = e;
            }
        }
	
        // Add EVA work to malfunction.
        double workTimeLeft = malfunction.addEVAWorkTime(workTime);

        // Add experience to "Mechanic" skill.
        // (1 base experience point per 20 millisols of time spent)
        // Experience points adjusted by person's "Experience Aptitude" attribute.
        double experience = time / 50D;
        NaturalAttributeManager nManager = person.getNaturalAttributeManager();
        experience += experience * (((double) nManager.getAttribute("Experience Aptitude") - 50D) / 100D);
        person.getSkillManager().addExperience(Skill.MECHANICS, experience);
	
        // Check if there are no more malfunctions. 
        if (!hasEVAMalfunction(entity)) phase = ENTER_AIRLOCK;

        // Keep track of the duration of the task.
        timeCompleted += time;
        if (timeCompleted >= duration) phase = ENTER_AIRLOCK;
	
        // Check if an accident happens during maintenance.
        checkForAccident(time);

        return (workTimeLeft / workTime) * time;
    }

    /**
     * Perform the enter airlock phase of the task.
     * @param time amount of time to perform the phase
     * @return time remaining after performing the phase
     */
    private double enterEVA(double time) {
        try {
            time = enterAirlock(time, airlock);
        }
        catch (Exception e) { 
            // System.out.println(e.getMessage()); 
        }
        
        if (enteredAirlock) endTask();
        return time;
    }	

    /**
     * Gets the malfunctionable entity the person is currently repairing or null if none.
     * @return entity
     */
    public Malfunctionable getEntity() {
        return entity;
    }
    
	/**
	 * Gets the effective skill level a person has at this task.
	 * @return effective skill level
	 */
	public int getEffectiveSkillLevel() {
		SkillManager manager = person.getSkillManager();
		int EVAOperationsSkill = manager.getEffectiveSkillLevel(Skill.EVA_OPERATIONS);
		int mechanicsSkill = manager.getEffectiveSkillLevel(Skill.MECHANICS);
		return (int) Math.round((double)(EVAOperationsSkill + mechanicsSkill) / 2D); 
	}
}
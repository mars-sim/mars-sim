/**
 * Mars Simulation Project
 * RepairMalfunction.java
 * @version 2.76 2004-05-06
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.Iterator;
import org.mars_sim.msp.simulation.Mars;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.malfunction.*;
import org.mars_sim.msp.simulation.person.*;

/**
 * The RepairMalfunction class is a task to repair a malfunction.
 */
public class RepairMalfunction extends Task implements Repair, Serializable {

	// Static members
	private static final double STRESS_MODIFIER = .3D; // The stress modified per millisol.

    // Data members
    private Malfunctionable entity; // Entity being repaired.
    private double duration; // Duration of task in millisols.

    /**
     * Constructs a RepairMalfunction object.
     * @param person the person to perform the task
     * @param mars the virtual Mars
     */
    public RepairMalfunction(Person person, Mars mars) {
        super("Repairing Malfunction", person, true, false, STRESS_MODIFIER, mars);

        // Randomly determine duration, from 0 - 500 millisols.
        duration = RandomUtil.getRandomDouble(500D);
        
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
     *  @param mars the virtual Mars
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person, Mars mars) {
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

        // If person is incompacitated, end task.
        if (person.getPerformanceRating() == 0D) endTask();

        // Check if there are no more malfunctions.
        if (!hasMalfunction(person)) endTask();

        if (isDone()) return timeLeft;

        // Determine effective work time based on "Mechanic" skill.
        double workTime = timeLeft;
        int mechanicSkill = person.getSkillManager().getEffectiveSkillLevel(Skill.MECHANICS);
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
            	description = "Repairing " + malfunction.getName() + " on " + e;
            	entity = e;
            }
        }

        // Add work to malfunction.
        // System.out.println(description);
        double workTimeLeft = malfunction.addWorkTime(workTime);

        // Add experience to "Mechanic" skill.
        // (1 base experience point per 20 millisols of time spent)
        // Experience points adjusted by person's "Experience Aptitude" attribute.
        double experience = timeLeft / 50D;
        NaturalAttributeManager nManager = person.getNaturalAttributeManager();
        experience += experience * (((double) nManager.getAttribute("Experience Aptitude") - 50D) / 100D);
        person.getSkillManager().addExperience(Skill.MECHANICS, experience);

        // Check if there are no more malfunctions.
        if (!hasMalfunction(person)) endTask();

        // Keep track of the duration of the task.
        timeCompleted += time;
        if (timeCompleted >= duration) endTask();

        // Check if an accident happens during maintenance.
        checkForAccident(timeLeft);

        return (workTimeLeft / workTime) / timeLeft;
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
            // System.out.println(person.getName() + " has accident while " + description);
            if (entity != null) entity.getMalfunctionManager().accident();
        }
    }

    /**
     * Gets the malfunctionable entity the person is currently repairing.
     * @returns null if none.
     * @return entity
     */
    public Malfunctionable getEntity() {
        return entity;
    }
}

/**
 * Mars Simulation Project
 * RepairEmergencyMalfunction.java
 * @version 2.76 2004-05-02
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.Iterator;
import org.mars_sim.msp.simulation.Mars;
import org.mars_sim.msp.simulation.malfunction.Malfunction;
import org.mars_sim.msp.simulation.malfunction.MalfunctionFactory;
import org.mars_sim.msp.simulation.malfunction.MalfunctionManager;
import org.mars_sim.msp.simulation.malfunction.Malfunctionable;
import org.mars_sim.msp.simulation.person.NaturalAttributeManager;
import org.mars_sim.msp.simulation.person.Person;

/**
 * The RepairEmergencyMalfunction class is a task to repair an emergency malfunction.
 */
public class RepairEmergencyMalfunction extends Task implements Repair, Serializable {

	// Static members
	private static final double STRESS_MODIFIER = 2D; // The stress modified per millisol.

    // Data members
    private Malfunctionable entity; // The entity being repaired.
    private Malfunction malfunction; // Problem being fixed

    /**
     * Constructs a RepairEmergencyMalfunction object.
     * @param person the person to perform the task
     * @param mars the virtual Mars
     */
    public RepairEmergencyMalfunction(Person person, Mars mars) {
        super("Repairing Emergency Malfunction", person, true, true, STRESS_MODIFIER, mars);

        claimMalfunction();

		// Create starting task event if needed.
		if (getCreateEvents()) {
			TaskEvent startingEvent = new TaskEvent(person, this, TaskEvent.START, "");
			mars.getEventManager().registerNewEvent(startingEvent);
		}

        // if (malfunction != null) System.out.println(person.getName() + " starting work on emergency malfunction: " + malfunction.getName() + "@" + Integer.toHexString(malfunction.hashCode()));
    }

    /**
     * Checks if the person has a local emergency malfunction.
     * @return true if emergency, false if none.
     */
    public static boolean hasEmergencyMalfunction(Person person) {

        boolean result = false;

        Iterator i = MalfunctionFactory.getMalfunctionables(person).iterator();
        while (i.hasNext()) {
            // MalfunctionManager manager = ((Malfunctionable) i.next()).getMalfunctionManager();
            Malfunctionable entity = (Malfunctionable) i.next();
            MalfunctionManager manager = entity.getMalfunctionManager();
            if (manager.hasEmergencyMalfunction()) result = true;
        }

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

        // Check if there emergency malfunction work is fixed.
        double workTimeLeft = malfunction.getEmergencyWorkTime() -
             malfunction.getCompletedEmergencyWorkTime();
        if (workTimeLeft == 0) {
	        // System.out.println(person.getName() + " finished work on emergency malfunction: " + malfunction.getName() + "@" + Integer.toHexString(malfunction.hashCode()));	
            endTask();
        }

        if (isDone()) return timeLeft;

        // Determine effective work time based on "Mechanic" skill.
        double workTime = timeLeft;
        int mechanicSkill = person.getSkillManager().getEffectiveSkillLevel("Mechanic");
        if (mechanicSkill == 0) workTime /= 2;
        if (mechanicSkill > 1) workTime += workTime * (.2D * mechanicSkill);

        // Add work to emergency malfunction.
        // System.out.println(person.getName() + " contributing " + workTime + " millisols of work time to emergency malfunction: " + malfunction.getName() + "@" + Integer.toHexString(malfunction.hashCode()));
        double remainingWorkTime = malfunction.addEmergencyWorkTime(workTime);
        if (remainingWorkTime > 0D) {
	        // System.out.println(person.getName() + " finished work on emergency malfunction: " + malfunction.getName() + "@" + Integer.toHexString(malfunction.hashCode()));	
            endTask();
        }

        // Add experience to "Mechanic" skill.
        // (1 base experience point per 20 millisols of time spent)
        // Experience points adjusted by person's "Experience Aptitude" attribute.
        double experience = timeLeft / 20D;
        NaturalAttributeManager nManager = person.getNaturalAttributeManager();
        experience += experience * (((double) nManager.getAttribute("Experience Aptitude") - 50D) / 100D);
        person.getSkillManager().addExperience("Mechanic", experience);

        return (timeLeft * (remainingWorkTime / workTime));
    }

    private void claimMalfunction() {
        // Get a local emergency malfunction.
        malfunction = null;
        Iterator i = MalfunctionFactory.getMalfunctionables(person).iterator();
        while (i.hasNext() && (malfunction == null)) {
            Malfunctionable e = (Malfunctionable) i.next();
            MalfunctionManager manager = e.getMalfunctionManager();
            if (manager.hasEmergencyMalfunction()) {
                malfunction = manager.getMostSeriousEmergencyMalfunction();
                entity = e;
                description = "Emergency repair " + malfunction.getName() + " on " + entity;
            }
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

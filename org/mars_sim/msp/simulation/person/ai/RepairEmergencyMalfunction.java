/**
 * Mars Simulation Project
 * RepairEmergencyMalfunction.java
 * @version 2.75 2002-06-08
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.*;
import org.mars_sim.msp.simulation.vehicle.*;
import org.mars_sim.msp.simulation.equipment.*;
import org.mars_sim.msp.simulation.malfunction.*;
import java.io.Serializable;
import java.util.*;

/**
 * The RepairEmergencyMalfunction class is a task to repair an emergency malfunction.
 */
public class RepairEmergencyMalfunction extends Task implements Repair, Serializable {

    // Data members
    private Malfunctionable entity; // The entity being repaired.
	
    /**
     * Constructs a RepairEmergencyMalfunction object.
     * @param person the person to perform the task
     * @param mars the virtual Mars
     */
    public RepairEmergencyMalfunction(Person person, Mars mars) {
        super("Repairing Emergency Malfunction", person, true, mars);
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
     */
    double performTask(double time) {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

        // If person is incompacitated, end task.
        if (person.getPerformanceRating() == 0D) done = true;

	// Check if there are no more emergency malfunctions. 
        if (!hasEmergencyMalfunction(person)) done = true;
	
	if (done) return timeLeft;

        // Determine effective work time based on "Mechanic" skill.
	double workTime = timeLeft;
        int mechanicSkill = person.getSkillManager().getEffectiveSkillLevel("Mechanic");
        if (mechanicSkill == 0) workTime /= 2;
        if (mechanicSkill > 1) workTime += workTime * (.2D * mechanicSkill);

	// Get a local emergency malfunction.
	Malfunction malfunction = null;
        Iterator i = MalfunctionFactory.getMalfunctionables(person).iterator();
	while (i.hasNext()) {
	    Malfunctionable e = (Malfunctionable) i.next();
	    MalfunctionManager manager = e.getMalfunctionManager();
	    if (manager.hasEmergencyMalfunction()) {
                malfunction = manager.getMostSeriousEmergencyMalfunction();
		entity = e;
	    }
	}
	
	// Add work to emergency malfunction.
	// System.out.println(person.getName() + " working on " + malfunction.getName() + " - workTime: " + workTime);
	description = "Repairing Emergency " + malfunction.getName();
        malfunction.addEmergencyWorkTime(workTime);

        // Add experience to "Mechanic" skill.
        // (1 base experience point per 20 millisols of time spent)
        // Experience points adjusted by person's "Experience Aptitude" attribute.
	double experience = timeLeft / 20D;
        NaturalAttributeManager nManager = person.getNaturalAttributeManager();
        experience += experience * (((double) nManager.getAttribute("Experience Aptitude") - 50D) / 100D);
        person.getSkillManager().addExperience("Mechanic", experience);
	
	// Check if there are no more emergency malfunctions. 
        if (!hasEmergencyMalfunction(person)) done = true;

	return 0D;
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

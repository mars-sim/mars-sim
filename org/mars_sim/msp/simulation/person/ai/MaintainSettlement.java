/**
 * Mars Simulation Project
 * MaintainSettlement.java
 * @version 2.74 2002-04-27
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.*;
import org.mars_sim.msp.simulation.malfunction.*;
import java.io.Serializable;
import java.util.*;

/** The MaintainSettlement class is a task for cleaning, organizing and performing
 *  preventive maintenance on a settlement.
 */
public class MaintainSettlement extends Task implements Serializable {

    // Data members
    private Malfunctionable entity; // Settlement or facility to be maintained.
    private double duration; // Duration (in millisols) the person with perform this task.

    /** Constructs a MaintainSettlement object
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     */
    public MaintainSettlement(Person person, Mars mars) {
        super("Performing Settlement Maintenance", person, true, mars);

        // Randomly determine duration, from 0 - 500 millisols
	duration = RandomUtil.getRandomDouble(500D);
	
        Settlement settlement = person.getSettlement();
	FacilityManager manager = settlement.getFacilityManager();
	
	// Determine either settlement or facility to maintain.
	double totalProbabilityWeight = 0D;
	totalProbabilityWeight += settlement.getMalfunctionManager().getTimeSinceLastMaintenance();
	Iterator i = manager.getFacilities();
	while (i.hasNext()) {
	    Facility facility = (Facility) i.next();
	    totalProbabilityWeight += facility.getMalfunctionManager().getTimeSinceLastMaintenance();
	}

	double chance = RandomUtil.getRandomDouble(totalProbabilityWeight);

	double lastMaint = settlement.getMalfunctionManager().getTimeSinceLastMaintenance();
	if (chance < lastMaint) {
            entity = settlement;
	    description = "Performing maintenance on " + settlement.getName();
	    // System.out.println(person.getName() + " " + description + " - " + lastMaint);
	}
	else {
            chance -= lastMaint; 
	    i = manager.getFacilities();
	    while (i.hasNext()) {
	        Facility facility = (Facility) i.next();
		lastMaint = facility.getMalfunctionManager().getTimeSinceLastMaintenance();
		if (chance < lastMaint) {
		    entity = facility;
		    description = "Performing maintenance on " + settlement.getName() + " " + facility.getName();
		    // System.out.println(person.getName() + " " + description + " - " + lastMaint);
		    break;
		}
		else chance -= lastMaint;
	    }
	}

	if (entity == null) done = true;
    }

    /** Returns the weighted probability that a person might perform this task.
     *  It should return a 0 if there is no chance to perform this task given the person and his/her situation.
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person, Mars mars) {
        double result = 0D;
	  
	// If person is in a settlement, determine probability based on the time
	// since last maintenance for the settlement and all of its facilities.
	Settlement settlement = person.getSettlement();
	if (settlement != null) {
            result += (settlement.getMalfunctionManager().getTimeSinceLastMaintenance() / 200D);
	    Iterator i = settlement.getFacilityManager().getFacilities();
	    while (i.hasNext()) {
	        Facility facility = (Facility) i.next();
		result += (facility.getMalfunctionManager().getTimeSinceLastMaintenance() / 200D);
	    }
	}
		
        // Effort-driven task modifier.
	result*= person.getPerformanceRating();
	
	return result;
    }

    /** This task simply waits until the set duration of the task is complete, then ends the task.
     *  @param time the amount of time to perform this task (in millisols)
     *  @return amount of time remaining after finishing with task (in millisols)
     */
    double performTask(double time) {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

	// If person is incompacitated, end task.
        if (person.getPerformanceRating() == 0D) {
	    done = true;
	    return 0D;
	}

        // Check if maintenance has already been completed.
	if (entity.getMalfunctionManager().getTimeSinceLastMaintenance() == 0D) {
            done = true;
	    return 0D;
	}
	
	// Determine effective work time based on "Mechanic" skill.
	double workTime = timeLeft;
        int mechanicSkill = person.getSkillManager().getEffectiveSkillLevel("Mechanic");
        if (mechanicSkill == 0) workTime /= 2;
        if (mechanicSkill > 1) workTime += workTime * (.2D * mechanicSkill);

        // Add work to the maintenance
        entity.getMalfunctionManager().addMaintenanceWorkTime(workTime);

        // Add experience to "Mechanic" skill.
        // (1 base experience point per 100 millisols of time spent)
        // Experience points adjusted by person's "Experience Aptitude" attribute.
        double experience = timeLeft / 100D;
	NaturalAttributeManager nManager = person.getNaturalAttributeManager();
        experience += experience * (((double) nManager.getAttribute("Experience Aptitude") - 50D) / 100D);
        person.getSkillManager().addExperience("Mechanic", experience);

        // If maintenance is complete, task is done.
	if (entity.getMalfunctionManager().getTimeSinceLastMaintenance() == 0D) done = true;

        // Keep track of the duration of the task.
	timeCompleted += time;
	if (timeCompleted >= duration) done = true;

        // Check if an accident happens during maintenance.
	checkForAccident(time);
	
	return 0D;
    }

    /**
     * Check for accident during maintenance.
     * @param time the amount of time working (in millisols)
     */
    private void checkForAccident(double time) {
    
        double chance = .001D;

        // Mechanic skill modification.
	int skill = person.getSkillManager().getEffectiveSkillLevel("Mechanic");
        if (skill <= 3) chance *= (4 - skill);
        else chance /= (skill - 2);

        if (RandomUtil.lessThanRandPercent(chance * time)) {
            System.out.println(person.getName() + " has accident while performing maintenance on " + entity.getName() + ".");
            entity.getMalfunctionManager().accident();
        }
    }

    /**
     * Gets the entity the person is maintaining.
     * Returns null if none
     * @return malfunctionable entity 
     */
    public Malfunctionable getEntity() {
        return entity;
    }
}

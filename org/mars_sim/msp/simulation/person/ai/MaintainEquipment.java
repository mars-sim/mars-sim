/**
 * Mars Simulation Project
 * MaintainEquipment.java
 * @version 2.74 2002-03-23
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.equipment.*;
import org.mars_sim.msp.simulation.malfunction.*;
import java.io.Serializable;
import java.util.*;

/** The MaintainEquipment class is a task for performing
 *  preventive maintenance on equipment.
 */
class MaintainEquipment extends Task implements Serializable {

    // Data members
    private Equipment equipment; // Equipment to be maintained.
    private double duration; // Duration (in millisols) the person with perform this task.

    /** Constructs a MaintainEquipment object
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     */
    public MaintainEquipment(Person person, Mars mars) {
        super("Performing Equipment Maintenance", person, true, mars);

        // Randomly determine duration, from 0 - 500 millisols
	duration = RandomUtil.getRandomDouble(500D);
	
        equipment = null;
	
	// Determine equipment to maintain.
	double totalProbabilityWeight = 0D;

	// Sum up probabilities for equipment in person's inventory.
	Inventory inventory = person.getInventory();
	UnitCollection personEquipmentList = inventory.getUnitsOfClass(Equipment.class);
	UnitIterator i = personEquipmentList.iterator();
	while (i.hasNext()) {
	    Equipment e = (Equipment) i.next();
	    totalProbabilityWeight += e.getMalfunctionManager().getTimeSinceLastMaintenance();
	}
	
	// Sum up probabilities for equipment in container's inventory.
	Unit container = person.getContainerUnit();
	if (container != null) {
	    inventory = person.getContainerUnit().getInventory();
	    UnitCollection containerEquipmentList = inventory.getUnitsOfClass(Equipment.class);
	    i = containerEquipmentList.iterator();
	    while (i.hasNext()) {
	        Equipment e = (Equipment) i.next();
	        totalProbabilityWeight += e.getMalfunctionManager().getTimeSinceLastMaintenance();
	    }
	}

	double chance = RandomUtil.getRandomDouble(totalProbabilityWeight);

        // Check equipment in person's inventory. 
        i = personEquipmentList.iterator();
	while (i.hasNext()) {
	    Equipment e = (Equipment) i.next();
	    double lastMaint = e.getMalfunctionManager().getTimeSinceLastMaintenance();
	    if (chance < lastMaint) {
	        equipment = e;
		description = "Performing maintenance on " + equipment.getName();
		System.out.println(person.getName() + " " + description + " - " + lastMaint);
		break;
	    }
	    else chance -= lastMaint;
	}

	if ((equipment == null) && (container != null)) {
	    inventory = person.getContainerUnit().getInventory();
	    UnitCollection containerEquipmentList = inventory.getUnitsOfClass(Equipment.class);
	    i = containerEquipmentList.iterator();
	    while (i.hasNext()) {
	        Equipment e = (Equipment) i.next();
		double lastMaint = e.getMalfunctionManager().getTimeSinceLastMaintenance();
		if (chance < lastMaint) {
		    equipment = e;
		    description = "Performing maintenance on " + equipment.getName();
		    System.out.println(person.getName() + " " + description + " - " + lastMaint);
		    break;
		}
		else chance -= lastMaint;
	    }
	}
	
	if (equipment == null) done = true;
    }

    /** Returns the weighted probability that a person might perform this task.
     *  It should return a 0 if there is no chance to perform this task given the person and his/her situation.
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person, Mars mars) {
        double result = 0D;

	// Sum up probabilities for equipment in person's inventory.
	Inventory inventory = person.getInventory();
	UnitCollection personEquipmentList = inventory.getUnitsOfClass(Equipment.class);
	UnitIterator i = personEquipmentList.iterator();
	while (i.hasNext()) {
	    Equipment e = (Equipment) i.next();
	    result += e.getMalfunctionManager().getTimeSinceLastMaintenance();
	}
	
	// Sum up probabilities for equipment in container's inventory.
	if (person.getContainerUnit() != null) {
	    inventory = person.getContainerUnit().getInventory();
	    UnitCollection containerEquipmentList = inventory.getUnitsOfClass(Equipment.class);
	    i = containerEquipmentList.iterator();
	    while (i.hasNext()) {
	        Equipment e = (Equipment) i.next();
	        result += e.getMalfunctionManager().getTimeSinceLastMaintenance();
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
	if (equipment.getMalfunctionManager().getTimeSinceLastMaintenance() == 0D) {
            done = true;
	    return 0D;
	}
	
	// Determine effective work time based on "Mechanic" skill.
	double workTime = timeLeft;
        int mechanicSkill = person.getSkillManager().getEffectiveSkillLevel("Mechanic");
        if (mechanicSkill == 0) workTime /= 2;
        if (mechanicSkill > 1) workTime += workTime * (.2D * mechanicSkill);

        // Add work to the maintenance
        equipment.getMalfunctionManager().addMaintenanceWorkTime(workTime);

        // Add experience to "Mechanic" skill.
        // (1 base experience point per 100 millisols of time spent)
        // Experience points adjusted by person's "Experience Aptitude" attribute.
        double experience = timeLeft / 100D;
	NaturalAttributeManager nManager = person.getNaturalAttributeManager();
        experience += experience * (((double) nManager.getAttribute("Experience Aptitude") - 50D) / 100D);
        person.getSkillManager().addExperience("Mechanic", experience);

        // If maintenance is complete, task is done.
	if (equipment.getMalfunctionManager().getTimeSinceLastMaintenance() == 0D) done = true;

        // Keep track of the duration of the task.
	timeCompleted += time;
	if (timeCompleted >= duration) done = true;
	
	return 0D;
    }
}


/**
 * Mars Simulation Project
 * MaintainVehicle.java
 * @version 2.74 2002-04-22
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.*;
import org.mars_sim.msp.simulation.vehicle.*;
import java.io.Serializable;
import java.util.*;

/** The MaintainVehicle class is a task for performing periodic maintenance on a vehicle
 *  in a settlements maintenance garage.
 */
class MaintainVehicle extends Task implements Serializable {

    // Data members
    private Vehicle vehicle; // Vehicle that person is performing the task on.
    private MaintenanceGarage garage; // The maintenance garage at the settlement.
    private Settlement settlement; // The settlement the person is at.
    private double duration; // Duration (in millisols) the person with perform this task.
    
    /** Constructor for periodic vehicle maintenance in a garage. This is an
     *  effort driven task.
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     */
    public MaintainVehicle(Person person, Mars mars) {
        super("Performing Vehicle Maintenance", person, true, mars);

	// Randomly determine duration, from 0 - 500 millisols
	duration = RandomUtil.getRandomDouble(500D);

        settlement = person.getSettlement();
        garage = (MaintenanceGarage) settlement.getFacilityManager().getFacility("Maintenance Garage");

	double totalProbabilityWeight = 0D;
        VehicleIterator i = settlement.getParkedVehicles().iterator();
	while (i.hasNext()) {
	    Vehicle tempVehicle = i.next();	
	    totalProbabilityWeight += tempVehicle.getMalfunctionManager().getTimeSinceLastMaintenance();
	}

	double chance = RandomUtil.getRandomDouble(totalProbabilityWeight);

	i = settlement.getParkedVehicles().iterator();
	while (i.hasNext()) {
	    Vehicle tempVehicle = i.next();
	    double lastMaint = vehicle.getMalfunctionManager().getTimeSinceLastMaintenance();
	    if (chance < lastMaint) {
	        vehicle = tempVehicle;
		description = "Performing Maintenance on " + vehicle.getName();
		System.out.println(person.getName() + " " + description + " - " + lastMaint);
		break;
	    }
	    else chance -= lastMaint;
	}

	if (vehicle == null) {
            done = true;
	    return;
	}

	if (!garage.vehicleInGarage(vehicle)) done = !garage.addVehicle(vehicle);
    }

    /** Returns the weighted probability that a person might perform this task.
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person, Mars mars) {
        double result = 0D;

        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
            Settlement settlement = person.getSettlement();
	    VehicleIterator i = settlement.getParkedVehicles().iterator();
	    while (i.hasNext()) {
                Vehicle vehicle = i.next();
		if (!vehicle.isReserved()) 
		    result += (vehicle.getMalfunctionManager().getTimeSinceLastMaintenance() / 200D);
            }
        }

	// Effort-driven task modifier.
	result *= person.getPerformanceRating();

        return result;
    }

    /** Performs the mechanic task for a given amount of time.
     *  @param time amount of time to perform the task (in millisols)
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

	// Check if maintenance has already been completed on vehicle.
	if (vehicle.getMalfunctionManager().getTimeSinceLastMaintenance() == 0D) {
	    done = true;
	    return 0D;
	}
	
        // Determine effective work time based on "Mechanic" skill.
        double workTime = timeLeft;
        int mechanicSkill = person.getSkillManager().getEffectiveSkillLevel("Mechanic");
        if (mechanicSkill == 0) workTime /= 2;
        if (mechanicSkill > 1) workTime += workTime * (.2D * mechanicSkill);

        // Add work to the vehicle maintenance
        vehicle.getMalfunctionManager().addMaintenanceWorkTime(workTime);

        // Add experience to "Mechanic" skill.
        // (1 base experience point per 100 millisols of time spent)
        // Experience points adjusted by person's "Experience Aptitude" attribute.
        double experience = timeLeft / 100D;
        experience += experience *
                (((double) person.getNaturalAttributeManager().getAttribute("Experience Aptitude") - 50D) / 100D);
        person.getSkillManager().addExperience("Mechanic", experience);

        // If vehicle maintenance is complete, task is done.
        // Move vehicle out of maintenance garage.
        if (vehicle.getMalfunctionManager().getTimeSinceLastMaintenance() == 0D) {
            garage.removeVehicle(vehicle);
            done = true;
        }

        // Keep track of the duration of the task.
	timeCompleted += time;
	if (timeCompleted >= duration) done = true;

        // Check if an accident happens during maintenance.
	checkForAccident(time);
	
        return 0D;
    }

    /**
     * Check for accident during vehicle maintenance.
     * @param time the amount of time working (in millisols)
     */
    private void checkForAccident(double time) {

        double chance = .01D;
	    
        // Mechanic skill modification.
	int skill = person.getSkillManager().getEffectiveSkillLevel("Mechanic");
        if (skill <= 3) chance *= (4 - skill);
        else chance /= (skill - 2);

	// 50% chance of accident with vehicle, 50% chance of accident with maintenance garage.
	if (RandomUtil.lessThanRandPercent(chance * time)) {
	    if (RandomUtil.lessThanRandPercent(50D)) {
                System.out.println(person.getName() + " has accident while performing maintenance on " + vehicle.getName() + ".");
                vehicle.getMalfunctionManager().accident();
	    }
	    else {
	        System.out.println(person.getName() + " has accident while performing maintenance on " + vehicle.getName() + ".");
		garage.getMalfunctionManager().accident();
	    }
        }
    }
}

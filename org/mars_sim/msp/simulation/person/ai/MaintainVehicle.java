/**
 * Mars Simulation Project
 * MaintainVehicle.java
 * @version 2.75 2002-06-08
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.*;
import org.mars_sim.msp.simulation.vehicle.*;
import org.mars_sim.msp.simulation.malfunction.*;
import java.io.Serializable;
import java.util.*;

/** 
 * The MaintainVehicle class is a task for performing
 * preventive maintenance on vehicles in a maintenance garage.
 */
public class MaintainVehicle extends Task implements Serializable {

    // Data members
    private MaintenanceGarage garage; // The maintenance garage.
    private Malfunctionable vehicle; // Vehicle to be maintained.
    private double duration; // Duration (in millisols) the person will perform this task.

    /** 
     * Constructs a MaintainVehicle object
     * @param person the person to perform the task
     * @param mars the virtual Mars
     */
    public MaintainVehicle(Person person, Mars mars) {
        super("Performing Maintenance", person, true, mars);

	// Get maintenance garage.
	garage = (MaintenanceGarage) person.getSettlement().getFacilityManager().getFacility(MaintenanceGarage.NAME);

	// Randomly determine duration, from 0 - 500 millisols
	duration = RandomUtil.getRandomDouble(500D);

        vehicle = null;
	
	// Determine entity to maintain.
	double totalProbabilityWeight = 0D;

	// Total probabilities for all malfunctionable entities in person's local.
        Iterator i = MalfunctionFactory.getMalfunctionables(person).iterator();
	while (i.hasNext()) {
	    Malfunctionable entity = (Malfunctionable) i.next();
	    MalfunctionManager manager = entity.getMalfunctionManager();
	    if (entity instanceof Vehicle) {
                Vehicle tempVehicle = (Vehicle) entity;
                boolean canAdd = true;
                if (manager.hasMalfunction()) canAdd = false;
	        if (!garage.vehicleInGarage(tempVehicle)) {
	            if (tempVehicle.getMass() > (garage.getVehicleCapacity() - garage.getCurrentVehicleMass()))
		        canAdd = false;
	        }
	        if (canAdd) totalProbabilityWeight += manager.getTimeSinceLastMaintenance();
            }
	}
	
	// Randomly determine a malfunctionable entity.
	double chance = RandomUtil.getRandomDouble(totalProbabilityWeight);

	// Get the malfunctionable entity chosen.
        i = MalfunctionFactory.getMalfunctionables(person).iterator();
	while (i.hasNext()) {
	    Malfunctionable entity = (Malfunctionable) i.next();
	    MalfunctionManager manager = entity.getMalfunctionManager();
	    double lastMaint = manager.getTimeSinceLastMaintenance();
	    if (entity instanceof Vehicle) {
                Vehicle tempVehicle = (Vehicle) entity;
                boolean canAdd = true;
                if (manager.hasMalfunction()) canAdd = false;
	        if (!garage.vehicleInGarage(tempVehicle)) {
	            if (tempVehicle.getMass() > (garage.getVehicleCapacity() - garage.getCurrentVehicleMass()))
		        canAdd = false;
	        }
	        if (canAdd && (chance < lastMaint)) {
		    vehicle = tempVehicle;
		    garage.addVehicle((Vehicle) vehicle);
		    description = "Performing maintenance on " + vehicle.getName();
		    // System.out.println(person.getName() + " " + description + " - " + lastMaint);
		    break;
		}
		else chance -= lastMaint;
            }
	}
	
        if (vehicle == null) done = true;
    }

    /** 
     * Returns the weighted probability that a person might perform this task.
     * It should return a 0 if there is no chance to perform this task given the person and his/her situation.
     * @param person the person to perform the task
     * @param mars the virtual Mars
     * @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person, Mars mars) {
        double result = 0D;

        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
		
	    MaintenanceGarage garage = 
	           (MaintenanceGarage) person.getSettlement().getFacilityManager().getFacility(MaintenanceGarage.NAME);

	    // Total probabilities for all malfunctionable entities in person's local.
            Iterator i = MalfunctionFactory.getMalfunctionables(person).iterator();
	    while (i.hasNext()) {
                Malfunctionable entity = (Malfunctionable) i.next();
		MalfunctionManager manager = entity.getMalfunctionManager();
		if (entity instanceof Vehicle) {
		    Vehicle vehicle = (Vehicle) entity;
                    boolean canAdd = true;
                    if (manager.hasMalfunction()) canAdd = false;
		    if (!garage.vehicleInGarage(vehicle)) {
		        if (vehicle.getMass() > (garage.getVehicleCapacity() - garage.getCurrentVehicleMass()))
			    canAdd = false;
		    }
		    if (canAdd) result += (manager.getTimeSinceLastMaintenance() / 200D);
		}
	    }
	}

        // Effort-driven task modifier.
	result *= person.getPerformanceRating();
	
	return result;
    }

    /** 
     * This task simply waits until the set duration of the task is complete, then ends the task.
     * @param time the amount of time to perform this task (in millisols)
     * @return amount of time remaining after finishing with task (in millisols)
     */
    double performTask(double time) {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

        MalfunctionManager manager = vehicle.getMalfunctionManager();
	
	// If person is incompacitated, end task.
        if (person.getPerformanceRating() == 0D) done = true;

        // Check if maintenance has already been completed.
	if (manager.getTimeSinceLastMaintenance() == 0D) done = true;

        // If vehicle has malfunction, end task.
	if (manager.hasMalfunction()) done = true;

	if (done) return timeLeft;
	
	// Determine effective work time based on "Mechanic" skill.
	double workTime = timeLeft;
        int mechanicSkill = person.getSkillManager().getEffectiveSkillLevel("Mechanic");
        if (mechanicSkill == 0) workTime /= 2;
        if (mechanicSkill > 1) workTime += workTime * (.2D * mechanicSkill);

        // Add work to the maintenance
        manager.addMaintenanceWorkTime(workTime);

        // Add experience to "Mechanic" skill.
        // (1 base experience point per 100 millisols of time spent)
        // Experience points adjusted by person's "Experience Aptitude" attribute.
        double experience = timeLeft / 100D;
	NaturalAttributeManager nManager = person.getNaturalAttributeManager();
        experience += experience * (((double) nManager.getAttribute("Experience Aptitude") - 50D) / 100D);
        person.getSkillManager().addExperience("Mechanic", experience);

        // If maintenance is complete, task is done.
	if (manager.getTimeSinceLastMaintenance() == 0D) {
	    garage.removeVehicle((Vehicle) vehicle);
	    // System.out.println(person.getName() + " finished " + description);
	    done = true;
	}

        // Keep track of the duration of the task.
	timeCompleted += time;
	if (timeCompleted >= duration) done = true;

        // Check if an accident happens during maintenance.
	checkForAccident(timeLeft);
	
	return 0D;
    }

    /**
     * Check for accident with entity during maintenance phase.
     * @param time the amount of time (in millisols)
     */
    private void checkForAccident(double time) {

        double chance = .001D;

	// Mechanic skill modification.
	int skill = person.getSkillManager().getEffectiveSkillLevel("Mechanic");
	if (skill <= 3) chance *= (4 - skill);
	else chance /= (skill - 2);

        if (RandomUtil.lessThanRandPercent(chance * time)) {
            // System.out.println(person.getName() + " has accident while performing maintenance on " + vehicle.getName() + ".");
            vehicle.getMalfunctionManager().accident();
        }
    }

    /** 
     * Gets the vehicle  the person is maintaining.
     * Returns null if none.
     * @return entity
     */
    public Malfunctionable getVehicle() {
        return vehicle;
    }
}

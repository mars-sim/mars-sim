/**
 * Mars Simulation Project
 * MaintainVehicle.java
 * @version 2.74 2002-02-28
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
    private MaintenanceGarageFacility garage; // The maintenance garage at the settlement.
    private Settlement settlement; // The settlement the person is at.

    /** Constructor for periodic vehicle maintenance in a garage. This is an
     *  effort driven task.
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     */
    public MaintainVehicle(Person person, VirtualMars mars) {
        super("Performing Maintenance on vehicle", person, true, mars);

        settlement = person.getSettlement();
        garage = (MaintenanceGarageFacility) settlement.getFacilityManager().getFacility("Maintenance Garage");

        // Create collection of vehicles needing maintenance.
        VehicleCollection vehiclesNeedingMaint = new VehicleCollection();
	VehicleIterator i = settlement.getParkedVehicles().iterator();
	while (i.hasNext()) {
            Vehicle tempVehicle = i.next(); 
            if ((tempVehicle.getDistanceLastMaintenance() > 5000) && !tempVehicle.isReserved()) {
                if (garage.vehicleInGarage(tempVehicle))
                    vehiclesNeedingMaint.add(tempVehicle);
                else {
		    double openCapacity = garage.getVehicleCapacity() - garage.getCurrentVehicleMass();
                    if (tempVehicle.getMass() < openCapacity) 
                        vehiclesNeedingMaint.add(tempVehicle);
                }
            }
        }

        // Choose one of the vehicles needing maintenance.
        if (vehiclesNeedingMaint.size() > 0) {
            int vehicleNum = RandomUtil.getRandomInt(vehiclesNeedingMaint.size() - 1);
	    Vehicle randVehicle = null;
	    int count = 0;
	    VehicleIterator j = settlement.getParkedVehicles().iterator();
	    while (i.hasNext()) {
	        Vehicle vehicle = i.next();
		if (vehicleNum == count) randVehicle = vehicle;
	    }
            if (!garage.vehicleInGarage(vehicle)) garage.addVehicle(vehicle);
            description = "Performing Maintenance on " + vehicle.getName();
        } 
	else done = true;
    }

    /** Returns the weighted probability that a person might perform this task.
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person, VirtualMars mars) {
        double result = 0D;

        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
            Settlement settlement = person.getSettlement();
	    VehicleIterator i = settlement.getParkedVehicles().iterator();
	    while (i.hasNext()) {
                Vehicle vehicle = i.next();
                if ((vehicle.getDistanceLastMaintenance() > 5000) && !vehicle.isReserved())
                    result = 25D;
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
	if (person.getPerformanceRating() == 0D) done = true;
	
        // Determine effective work time based on "Vehicle Mechanic" skill.
        double workTime = timeLeft;
        int mechanicSkill = person.getSkillManager().getEffectiveSkillLevel("Vehicle Mechanic");
        if (mechanicSkill == 0) workTime /= 2;
        if (mechanicSkill > 1) workTime += workTime * (.2D * mechanicSkill);

        // Add work to the vehicle maintenance
        vehicle.addWorkToMaintenance(workTime);

        // Add experience to "Vehicle Mechanic" skill.
        // (1 base experience point per 100 millisols of time spent)
        // Experience points adjusted by person's "Experience Aptitude" attribute.
        double experience = timeLeft / 100D;
        experience += experience *
                (((double) person.getNaturalAttributeManager().getAttribute("Experience Aptitude") - 50D) / 100D);
        person.getSkillManager().addExperience("Vehicle Mechanic", experience);

        // If vehicle maintenance is complete, task is done.
        // Move vehicle out of maintenance garage.
        if (vehicle.getDistanceLastMaintenance() == 0) {
            garage.removeVehicle(vehicle);
            done = true;
        }

        return 0D;
    }
}

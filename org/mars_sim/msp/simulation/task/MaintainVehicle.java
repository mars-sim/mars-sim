/**
 * Mars Simulation Project
 * MaintainVehicle.java
 * @version 2.72 2001-08-05
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.task;

import org.mars_sim.msp.simulation.*;
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

    /** Constructor for periodic vehicle maintenance in a garage. 
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     */
    public MaintainVehicle(Person person, VirtualMars mars) {
        super("Performing Maintenance on ", person, mars);

        settlement = person.getSettlement();
        garage = (MaintenanceGarageFacility) settlement.getFacilityManager().getFacility("Maintenance Garage");

        // Create vector of vehicles needing maintenance.
        Vector vehiclesNeedingMaint = new Vector();
        for (int x = 0; x < settlement.getVehicleNum(); x++) {
            Vehicle tempVehicle = settlement.getVehicle(x);
            if ((tempVehicle.getDistanceLastMaintenance() > 5000) && !tempVehicle.isReserved()) {
                if (garage.vehicleInGarage(tempVehicle))
                    vehiclesNeedingMaint.addElement(tempVehicle);
                else {
                    if (garage.getMaxVehicleSize() >= tempVehicle.getSize()) {
                        if ((garage.getMaxSizeCapacity() - garage.getTotalSize()) >=
                                tempVehicle.getSize())
                            vehiclesNeedingMaint.addElement(tempVehicle);
                    }
                }
            }
        }

        // Choose one of the vehicles needing maintenance.
        if (vehiclesNeedingMaint.size() > 0) {
            int vehicleNum = RandomUtil.getRandomInt(vehiclesNeedingMaint.size() - 1);
            vehicle = (Vehicle) vehiclesNeedingMaint.elementAt(vehicleNum);
            if (!garage.vehicleInGarage(vehicle))
                garage.addVehicle(vehicle);
            name = "Performing Maintenance on " + vehicle.getName();
            description = name;
            vehicle.setStatus("Periodic Maintenance");
            // System.out.println(person.getName() + " " + name);
        } else done = true;
    }

    /** Returns the weighted probability that a person might perform this task. 
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person, VirtualMars mars) {
        double result = 0D;

        if (person.getLocationSituation().equals("In Settlement")) {
            Settlement settlement = person.getSettlement();
            for (int x = 0; x < settlement.getVehicleNum(); x++) {
                Vehicle vehicle = settlement.getVehicle(x);
                if ((vehicle.getDistanceLastMaintenance() > 5000) && !vehicle.isReserved())
                    result = 25D;
            }
        }

        return result;
    }

    /** Performs the mechanic task for a given amount of time. 
     *  @param time amount of time to perform the task (in millisols)
     *  @return amount of time remaining after finishing with task (in millisols) 
     */
    double performTask(double time) {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

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
            vehicle.setStatus("Parked");
            done = true;
        }

        return 0D;
    }
}

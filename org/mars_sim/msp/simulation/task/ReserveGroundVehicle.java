/**
 * Mars Simulation Project
 * ReserveGroundVehicle.java
 * @version 2.72 2001-08-07
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.task;

import org.mars_sim.msp.simulation.*;

/** The ReserveGroundVehicle class is a task for reserving a ground 
 *  vehicle at a settlement for a trip.
 *  The duration of the task is 50 millisols.
 */
class ReserveGroundVehicle extends Task {

    // Data members
    private double duration = 50D; // The predetermined duration of task in millisols
    private GroundVehicle reservedVehicle; // The reserved vehicle
    private Coordinates destination; // The destination coordinates for the trip

    /** Constructs a ReserveGroundVehicle object
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @param destination the destination of the trip
     */
    public ReserveGroundVehicle(Person person, VirtualMars mars, Coordinates destination) {
        super("Reserving a vehicle", person, mars);
       
        this.destination = destination; 
        reservedVehicle = null;

        System.out.println(person.getName() + " is reserving a vehicle.");
    }

    /** Perform this task for the given amount of time. 
     *  @param time the amount of time to perform this task (in millisols)
     *  @return amount of time remaining after finishing with task (in millisols)
     */
    double performTask(double time) {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

        timeCompleted += time;
        if (timeCompleted > duration) {
            
            Settlement settlement = person.getSettlement();
            FacilityManager facilities = settlement.getFacilityManager();
            MaintenanceGarageFacility garage = (MaintenanceGarageFacility) facilities.getFacility("Maintenance Garage");
            
            for (int x=0; x < settlement.getVehicleNum(); x++) {
                Vehicle tempVehicle = settlement.getVehicle(x);
                if (tempVehicle instanceof GroundVehicle) {
                    if (!tempVehicle.isReserved() && !garage.vehicleInGarage(tempVehicle)) {
                        if (tempVehicle.getRange() > person.getCoordinates().getDistance(destination)) {
                            if (reservedVehicle == null) {
                                reservedVehicle = (GroundVehicle) tempVehicle;
                                reservedVehicle.setReserved(true);
                                System.out.println(person.getName() + " has reserved " + reservedVehicle.getName());
                            }
                        }
                    }
                }
            }

            done = true;
            return timeCompleted - duration;
        }
        else return 0;
    }

    /** Gets the reserved ground vehicle if task is done and successful.
     *  Returns null otherwise.
     *  @return reserved ground vehicle
     */
    public GroundVehicle getReservedVehicle() {
        return reservedVehicle;
    }
}


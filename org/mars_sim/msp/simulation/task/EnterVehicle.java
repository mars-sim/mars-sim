/**
 * Mars Simulation Project
 * EnterVehicle.java
 * @version 2.72 2001-08-07
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.task;

import org.mars_sim.msp.simulation.*;

/** The EnterVehicle class is a task for entering a vehicle 
 *  and strapping in for a trip. 
 *  The duration of the task is 20 millisols.
 */
class EnterVehicle extends Task {

    // Data members
    private double duration = 20D; // The predetermined duration of task in millisols
    private Vehicle vehicle; // The vehicle to be entered

    /** Constructs a EnterVehicle object
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @param vehicle the vehicle to be entered 
     */
    public EnterVehicle(Person person, VirtualMars mars, Vehicle vehicle) {
        super("Entering " + vehicle.getName(), person, mars);
       
        this.vehicle = vehicle;
        System.out.println(person.getName() + " is entering " + vehicle.getName());
    }

    /** Performs this task for the given amount of time. 
     *  @param time the amount of time to perform this task (in millisols)
     *  @return amount of time remaining after finishing with task (in millisols)
     */
    double performTask(double time) {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

        timeCompleted += time;
        if (timeCompleted > duration) {
            
            Settlement settlement = person.getSettlement();
            person.setVehicle(vehicle);
            person.setLocationSituation("In Vehicle");
            vehicle.addPassenger(person);
            settlement.personLeave(person);

            done = true;
            return timeCompleted - duration;
        }
        else return 0;
    }
}


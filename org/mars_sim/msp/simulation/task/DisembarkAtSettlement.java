/**
 * Mars Simulation Project
 * DisembarkAtSettlement.java
 * @version 2.72 2001-07-08
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.task;

import org.mars_sim.msp.simulation.*;

/** The DisembarkAtSettlement class is a task for parking a vehicle and entering a settlement. 
 */
class DisembarkAtSettlement extends Task {

    // Data members
    private Settlement destinationSettlement;
    private Vehicle vehicle;

    /** Constructs a DisembarkAtSettlement object with a given destination settlement
     *
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @param destinationSettlement the destination settlement
     */
    public DisembarkAtSettlement(Person person, VirtualMars mars, Settlement destinationSettlement) {
        super("Disembarking at " + destinationSettlement.getName(), person, mars);

        this.destinationSettlement = destinationSettlement;
        vehicle = person.getVehicle(); 
    }

    /** Performs this task for a given period of time 
     *  @param time amount of time to perform task (in millisols) 
     */
    double doTask(double time) {
        double timeLeft = super.doTask(time);
        if (subTask != null) return timeLeft;

        double timeRequired = 100D; // 100 millisols

        timeCompleted += timeLeft;
        if (timeCompleted >= timeRequired) {
           
            // Park vehicle
            vehicle.setSettlement(destinationSettlement);
            vehicle.setDestinationSettlement(null);
            vehicle.setDestinationType("None");
            vehicle.setStatus("Parked");

            // Exit passengers
            while (vehicle.getPassengerNum() > 0) {
                Person passenger = vehicle.getPassenger(0);
                vehicle.removePassenger(passenger);
                passenger.setLocationSituation("In Settlement");
                passenger.setSettlement(destinationSettlement);
            }
 
            isDone = true;
            return timeCompleted - timeRequired;
        }
        else return 0;
    }
}

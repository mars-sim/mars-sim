/**
 * Mars Simulation Project
 * ReserveRover.java
 * @version 2.75 2003-04-27
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.*;
import org.mars_sim.msp.simulation.vehicle.*;
import java.io.Serializable;

/** The ReserveRover class is a task for reserving a rover 
 *  at a settlement for a trip.
 *  The duration of the task is 50 millisols.
 */
class ReserveRover extends Task implements Serializable {
	
    // Data members
    private double duration = 50D;   // The predetermined duration of task in millisols
    private Rover reservedRover;     // The reserved rover 
    private Coordinates destination; // The destination coordinates for the trip
    private Class roverType;         // The type of rover

    /** Constructs a ReserveRover object with a destination.
     *  @param roverType the type of rover to be reserved
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @param destination the destination of the trip
     */
    public ReserveRover(Class roverType, Person person, Mars mars, Coordinates destination) {
        super("Reserving a rover", person, false, mars);

        this.roverType = roverType;
        this.destination = destination;
        reservedRover = null;
    }

    /** Constructs a ReserveRover object without a destinatiion.
     *  @param roverType the type of rover to be reserved
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     */
    public ReserveRover(Class roverType, Person person, Mars mars) {
        super("Reserving a rover", person, false, mars);

        this.roverType = roverType;
        destination = null;
        reservedRover = null;
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
            VehicleIterator i = settlement.getParkedVehicles().iterator();
            while (i.hasNext()) {
                Vehicle vehicle = i.next();
                
                boolean reservable = vehicle.getStatus().equals(Vehicle.PARKED);
                
                boolean correctRoverType = roverType.isInstance(vehicle);
                
                boolean inRange = false;
                if (destination != null) {
                    double distance = person.getCoordinates().getDistance(destination);
                    inRange = vehicle.getRange() > distance;
                }
                else inRange = true;
                
                boolean supplies = LoadVehicle.hasEnoughSupplies(settlement, vehicle);
                
                if ((reservedRover == null) && correctRoverType && reservable && inRange && supplies) {
                    reservedRover = (Rover) vehicle;
                    reservedRover.setReserved(true);
                }
            }

            endTask();
            return timeCompleted - duration;
        }
        else return 0;
    }

    /** 
     * Returns true if settlement has an available rover.
     *
     * @param roverType the type of rover
     * @param settlement
     * @return are there any available rovers 
     */
    public static boolean availableRovers(Class roverType, Settlement settlement) {

        boolean result = false;

        VehicleIterator i = settlement.getParkedVehicles().iterator();
        while (i.hasNext()) {
            Vehicle vehicle = i.next();
            boolean parked = vehicle.getStatus().equals(Vehicle.PARKED);
            boolean correctType = roverType.isInstance(vehicle);
            boolean supplies = LoadVehicle.hasEnoughSupplies(settlement, vehicle);
            if (parked && correctType && supplies) result = true;
        }
        
        return result;
    }

    /** Gets the reserved rover if task is done and successful.
     *  Returns null otherwise.
     *  @return reserved rover 
     */
    public Rover getReservedRover() {
        return reservedRover;
    }
    
    /**
     * Unreserves the reserved rover if the task is done and successful.
     */
    public void unreserveRover() {
        if (reservedRover != null) reservedRover.setReserved(false);
    }
}

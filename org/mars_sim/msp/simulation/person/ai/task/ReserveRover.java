/**
 * Mars Simulation Project
 * ReserveRover.java
 * @version 2.75 2004-04-06
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.Mars;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.vehicle.Rover;
import org.mars_sim.msp.simulation.vehicle.Vehicle;
import org.mars_sim.msp.simulation.vehicle.VehicleIterator;

/** The ReserveRover class is a task for reserving a rover 
 *  at a settlement for a trip.
 *  The duration of the task is 50 millisols.
 */
public class ReserveRover extends Task implements Serializable {
	
	// Rover types
	public static final String EXPLORER_ROVER = "Explorer Rover";
	public static final String TRANSPORT_ROVER = "Transport Rover";
	
    // Data members
    private double duration = 50D;   // The predetermined duration of task in millisols
    private Rover reservedRover;     // The reserved rover 
    private Coordinates destination; // The destination coordinates for the trip
    private String roverType;         // The type of rover

    /** Constructs a ReserveRover object with a destination.
     *  @param roverType the type of rover to be reserved
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @param destination the destination of the trip
     */
    public ReserveRover(String roverType, Person person, Mars mars, Coordinates destination) {
        super("Reserving a rover", person, false, false, mars);

        this.roverType = roverType;
        this.destination = destination;
        reservedRover = null;
    }

    /** Constructs a ReserveRover object without a destinatiion.
     *  @param roverType the type of rover to be reserved
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     */
    public ReserveRover(String roverType, Person person, Mars mars) {
        super("Reserving a rover", person, false, false, mars);

        this.roverType = roverType;
        destination = null;
        reservedRover = null;
    }

    /** 
     * Perform this task for the given amount of time.
     * @param time the amount of time to perform this task (in millisols)
     * @return amount of time remaining after finishing with task (in millisols)
     * @throws Exception if error performing task.
     */
    double performTask(double time) throws Exception {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

        timeCompleted += time;
        if (timeCompleted > duration) {
            Settlement settlement = person.getSettlement();
            VehicleIterator i = settlement.getParkedVehicles().iterator();
            while (i.hasNext()) {
                Vehicle vehicle = i.next();
                
                // boolean reservable = vehicle.getStatus().equals(Vehicle.PARKED);
                boolean reservable = !vehicle.isReserved();
                
                boolean correctRoverType = roverType.equals(vehicle.getDescription());
                
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
    public static boolean availableRovers(String roverType, Settlement settlement) {

        boolean result = false;

        VehicleIterator i = settlement.getParkedVehicles().iterator();
        while (i.hasNext()) {
            Vehicle vehicle = i.next();
            boolean parked = vehicle.getStatus().equals(Vehicle.PARKED);
            boolean correctType = roverType.equals(vehicle.getDescription());
            boolean supplies = LoadVehicle.hasEnoughSupplies(settlement, vehicle);
            boolean reserved = vehicle.isReserved();
            if (parked && correctType && supplies && !reserved) result = true;
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
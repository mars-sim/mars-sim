/**
 * Mars Simulation Project
 * ReserveRover.java
 * @version 2.74 2002-03-03
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

    // Rover types
    public static final int EXPLORER_ROVER = 1;
    public static final int TRANSPORT_ROVER = 2;
	
    // Data members
    private double duration = 50D;   // The predetermined duration of task in millisols
    private Rover reservedRover;     // The reserved rover 
    private Coordinates destination; // The destination coordinates for the trip
    private int roverType;           // The type of rover

    /** Constructs a ReserveRover object with a destination.
     *  @param roverType the type of rover to be reserved
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @param destination the destination of the trip
     */
    public ReserveRover(int roverType, Person person, VirtualMars mars, Coordinates destination) {
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
    public ReserveRover(int roverType, Person person, VirtualMars mars) {
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
            FacilityManager facilities = settlement.getFacilityManager();
            MaintenanceGarageFacility garage = (MaintenanceGarageFacility) 
	            facilities.getFacility("Maintenance Garage");

	    VehicleIterator i = settlement.getParkedVehicles().iterator();
	    while (i.hasNext()) {
	        Vehicle tempVehicle = i.next();
		boolean correctRoverType = false;
		if ((roverType == EXPLORER_ROVER) && (tempVehicle instanceof ExplorerRover)) 
		    correctRoverType = true;
		if ((roverType == TRANSPORT_ROVER) && (tempVehicle instanceof TransportRover))
		    correctRoverType = true;
		if ((reservedRover == null) && correctRoverType) {
                    boolean reservable = true;

                    if (tempVehicle.isReserved()) reservable = false;
                    if (garage.vehicleInGarage(tempVehicle)) reservable = false;
                    if (destination != null) {
                        if (tempVehicle.getRange() < person.getCoordinates().getDistance(destination)) 
				reservable = false;
                    }

                    if (reservable) {
                        reservedRover = (Rover) tempVehicle;
                        reservedRover.setReserved(true);
                    }
                }
            }

            done = true;
            return timeCompleted - duration;
        }
        else return 0;
    }

    /** Returns true if settlement has an available rover.
     *  @param settlement
     *  @return are there any available rovers 
     */
    public static boolean availableRovers(int roverType, Settlement settlement) {

        boolean result = false;

        FacilityManager facilities = settlement.getFacilityManager();
        MaintenanceGarageFacility garage = (MaintenanceGarageFacility) 
	        facilities.getFacility("Maintenance Garage");

	VehicleIterator i = settlement.getParkedVehicles().iterator();
	while (i.hasNext()) {
	    Vehicle vehicle = i.next();
	    boolean correctRoverType = false;
	    if ((roverType == EXPLORER_ROVER) && (vehicle instanceof ExplorerRover)) 
	        correctRoverType = true;
	    if ((roverType == TRANSPORT_ROVER) && (vehicle instanceof TransportRover))
	        correctRoverType = true;
            if (correctRoverType) {
                if (!vehicle.isReserved() && !garage.vehicleInGarage(vehicle)) {
		    if (LoadVehicle.hasEnoughSupplies(settlement, vehicle)) {	
		        result = true;
		    }
	        }
            }
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
}


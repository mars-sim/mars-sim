/**
 * Mars Simulation Project
 * ReserveRover.java
 * @version 2.77 2004-09-28
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.vehicle.*;

/** The ReserveRover class is a task for reserving a rover 
 *  at a settlement for a trip.
 *  The duration of the task is 50 millisols.
 */
public class ReserveRover extends Task implements Serializable {
	
	// Static members
	private static final double STRESS_MODIFIER = 0D; // The stress modified per millisol.
	
    // Data members
    private double duration = 50D;   // The predetermined duration of task in millisols
    private Rover reservedRover;     // The reserved rover 
    private Coordinates destination; // The destination coordinates for the trip
    private String resourceType;     // The type of resource the rover should be able to carry.
    private double resourceAmount;   // The required amount of resource the rover should be able to carry.

    /** 
     * Constructs a ReserveRover object with a destination.
     * @param resource the type of resource the rover must be able to carry.
     * @param amount the amount of the resource the rover must be able to carry.
     * @param person the person to perform the task
     * @param destination the destination of the trip
     */
    public ReserveRover(String resource, double amount, Person person, Coordinates destination) {
        super("Reserving a rover", person, false, false, STRESS_MODIFIER);

        this.resourceType = resource;
        this.resourceAmount = amount;
        this.destination = destination;
        reservedRover = null;
    }

    /** 
     * Constructs a ReserveRover object without a destinatiion.
     * @param resource the type of resource the rover must be able to carry.
     * @param amount the amount of the resource the rover must be able to carry.
     * @param person the person to perform the task
     */
    public ReserveRover(String resource, double amount, Person person) {
        super("Reserving a rover", person, false, false, STRESS_MODIFIER);

        this.resourceType = resource;
        this.resourceAmount = amount;
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
            
            VehicleCollection reservableRovers = new VehicleCollection();
            
            VehicleIterator i = settlement.getParkedVehicles().iterator();
            while (i.hasNext()) {
                Vehicle vehicle = i.next();
                
                boolean isRover = (vehicle instanceof Rover);
                
                boolean reservable = !vehicle.isReserved() && !vehicle.isReservedForMaintenance();
                
                boolean resourceCapable = true;
                if (resourceType != null) 
                	resourceCapable = (vehicle.getInventory().getResourceCapacity(resourceType) >= resourceAmount);
                
                boolean inRange = false;
                if (destination != null) {
                    double distance = person.getCoordinates().getDistance(destination);
                    inRange = vehicle.getRange() > distance;
                }
                else inRange = true;
                
                boolean supplies = LoadVehicle.hasEnoughSupplies(settlement, vehicle);
                
                if (isRover && (reservedRover == null) && resourceCapable && reservable && inRange && supplies) 
                	reservableRovers.add(vehicle);
            }
            
            // Get rovers with highest crew capacity.
            int bestCrewCapacity = 0;
            VehicleIterator i2 = reservableRovers.iterator();
            while (i2.hasNext()) {
            	Rover rover = (Rover) i2.next();
            	if (rover.getCrewCapacity() > bestCrewCapacity) bestCrewCapacity = rover.getCrewCapacity();
            }
            
            VehicleIterator i3 = reservableRovers.iterator();
            while (i3.hasNext()) {
            	Rover rover = (Rover) i3.next();
            	if (rover.getCrewCapacity() < bestCrewCapacity) i3.remove();
            }
            
            // Get random rover
            if (reservableRovers.size() > 0) {
            	int roverIndex = RandomUtil.getRandomInt(reservableRovers.size() - 1);
            	reservedRover = (Rover) reservableRovers.get(roverIndex); 
				reservedRover.setReserved(true);
            }

            endTask();
            return timeCompleted - duration;
        }
        else return 0;
    }

    /** 
     * Returns true if settlement has an available rover.
     *
     * @param resource the type of resource the rover must carry.
     * @param amount the amount of the resource the rover must carry.
     * @param settlement
     * @return are there any available rovers 
     */
    public static boolean availableRovers(String resource, double amount, Settlement settlement) {

        boolean result = false;

        VehicleIterator i = settlement.getParkedVehicles().iterator();
        while (i.hasNext()) {
            Vehicle vehicle = i.next();
            
			boolean resourceCapable = true;
			if (resource != null) resourceCapable = (vehicle.getInventory().getResourceCapacity(resource) >= amount);
            
            boolean parked = vehicle.getStatus().equals(Vehicle.PARKED);
            boolean supplies = LoadVehicle.hasEnoughSupplies(settlement, vehicle);
            boolean reserved = vehicle.isReserved();
      		boolean maintenance = vehicle.isReservedForMaintenance();
            if (parked && resourceCapable && supplies && !reserved && !maintenance) result = true;
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
    
	/**
	 * Gets the effective skill level a person has at this task.
	 * @return effective skill level
	 */
	public int getEffectiveSkillLevel() {
		return 0;	
	}
	
	/**
	 * Gets a list of the skills associated with this task.
	 * May be empty list if no associated skills.
	 * @return list of skills as strings
	 */
	public List getAssociatedSkills() {
		List results = new ArrayList();
		return results;
	}
}
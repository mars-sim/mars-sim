/**
 * Mars Simulation Project
 * VehicleAirlock.java
 * @version 2.75 2003-04-20
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.vehicle;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;

/** 
 * The VehicleAirlock class represents an airlock for a vehicle.
 */
public class VehicleAirlock extends Airlock {
    
    private Vehicle vehicle; // The vehicle this airlock is for.
    
    /**
     * Constructor
     * 
     * @param vehicle the vehicle this airlock of for.
     * @param capacity number of people airlock can hold.
     * @throws IllegalArgumentException if vehicle is not valid or if 
     * capacity is less than one.
     */
    public VehicleAirlock(Vehicle vehicle, int capacity) {
        // User Airlock constructor
        super(capacity);
        
        if (vehicle == null) throw new IllegalArgumentException("vehicle is null.");
        else if (!(vehicle instanceof Crewable)) throw new IllegalArgumentException("vehicle not crewable.");
        else if (!(vehicle instanceof LifeSupport)) throw new IllegalArgumentException("vehicle has no life support.");
        else this.vehicle = vehicle;
    }
    
    /**
     * Causes a person within the airlock to exit either inside or outside.
     *
     * @param person the person to exit.
     * @throws Exception if person is not in the airlock.
     */
    protected void exitAirlock(Person person) throws Exception {
        Inventory inv = vehicle.getInventory();
        if (!inv.containsUnit(person)) throw new Exception("person not in airlock.");
        else {
            if (pressurized) inv.addUnit(person);
            else inv.dropUnitOutside(person);
        }
    }
    
    /**
     * Gets the name of the entity this airlock is attached to.
     *
     * @return name
     */
    public String getEntityName() {
        return vehicle.getName();
    }
    
    /**
     * Gets the inventory of the entity this airlock is attached to.
     *
     * @return inventory
     */
    public Inventory getEntityInventory() {
        return vehicle.getInventory();
    }
}

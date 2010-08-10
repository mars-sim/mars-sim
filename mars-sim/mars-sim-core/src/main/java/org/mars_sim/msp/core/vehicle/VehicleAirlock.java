/**
 * Mars Simulation Project
 * VehicleAirlock.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */

package org.mars_sim.msp.core.vehicle;

import org.mars_sim.msp.core.*;
import org.mars_sim.msp.core.person.*;

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
        
        if (inAirlock(person)) {
            if (pressurized) vehicle.getInventory().storeUnit(person);
            else {
            	// Drop person outside.  If person, for some reason, is not in vehicle,
            	// put them outside anyway.
            	try {
            		vehicle.getInventory().retrieveUnit(person);
            	}
            	catch (InventoryException e) {
            		person.setContainerUnit(null);
            	}
            } 
        }
        else throw new Exception(person.getName() + " not in airlock of " + getEntityName());
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
/**
 * Mars Simulation Project
 * VehicleAirlock.java
 * @version 3.02 2012-05-30
 * @author Scott Davis
 */

package org.mars_sim.msp.core.vehicle;

import java.awt.geom.Point2D;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Airlock;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LifeSupport;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.person.Person;

/** 
 * The VehicleAirlock class represents an airlock for a vehicle.
 */
public class VehicleAirlock extends Airlock {

    private static Logger logger = Logger.getLogger(VehicleAirlock.class.getName());

    // Data members.
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
    protected void exitAirlock(Person person) {

        if (inAirlock(person)) {
            if (PRESSURIZED.equals(getState())) {
                // Exit person to inside vehicle.
                vehicle.getInventory().storeUnit(person);
                
                Point2D.Double vehicleLoc = LocalAreaUtil.getRandomInteriorLocation(vehicle);
                Point2D.Double settlementLoc = LocalAreaUtil.getLocalRelativeLocation(vehicleLoc.getX(), 
                        vehicleLoc.getY(), vehicle);
                person.setXLocation(settlementLoc.getX());
                person.setYLocation(settlementLoc.getY());
            }
            else if (DEPRESSURIZED.equals(getState())){
                // Exit person outside vehicle.  
                vehicle.getInventory().retrieveUnit(person);
            }
            else {
                logger.severe("Vehicle airlock in incorrect state for exiting: " + getState());
            }
        }
        else throw new IllegalStateException(person.getName() + " not in airlock of " + getEntityName());
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
/**
 * Mars Simulation Project
 * VehicleAirlock.java
 * @version 3.06 2014-03-06
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

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
    private static Logger logger = Logger.getLogger(VehicleAirlock.class.getName());

    // Data members.
    private Vehicle vehicle; // The vehicle this airlock is for.
    private Point2D airlockInsidePos;
    private Point2D airlockInteriorPos;
    private Point2D airlockExteriorPos;

    /**
     * Constructor
     * 
     * @param vehicle the vehicle this airlock of for.
     * @param capacity number of people airlock can hold.
     */
    public VehicleAirlock(Vehicle vehicle, int capacity, double xLoc, double yLoc, 
            double interiorXLoc, double interiorYLoc, double exteriorXLoc, 
            double exteriorYLoc) {
        // User Airlock constructor
        super(capacity);

        if (vehicle == null) {
            throw new IllegalArgumentException("vehicle is null.");
        }
        else if (!(vehicle instanceof Crewable)) {
            throw new IllegalArgumentException("vehicle not crewable.");
        }
        else if (!(vehicle instanceof LifeSupport)) {
            throw new IllegalArgumentException("vehicle has no life support.");
        }
        else {
            this.vehicle = vehicle;
        }
        
        // Determine airlock interior position.
        airlockInteriorPos = new Point2D.Double(interiorXLoc, interiorYLoc);
        
        // Determine airlock exterior position.
        airlockExteriorPos = new Point2D.Double(exteriorXLoc, exteriorYLoc);
        
        // Determine airlock inside position.
        airlockInsidePos = new Point2D.Double(xLoc, yLoc); 
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
            }
            else if (DEPRESSURIZED.equals(getState())) {
                
                // Exit person outside vehicle.  
                vehicle.getInventory().retrieveUnit(person);
            }
            else {
                logger.severe("Vehicle airlock in incorrect state for exiting: " + getState());
            }
        }
        else {
            throw new IllegalStateException(person.getName() + " not in airlock of " + getEntityName());
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
    
    @Override
    public Object getEntity() {
        return vehicle;
    }

    @Override
    public Point2D getAvailableInteriorPosition() {
        return LocalAreaUtil.getLocalRelativeLocation(airlockInteriorPos.getX(), 
                airlockInteriorPos.getY(), vehicle);
    }

    @Override
    public Point2D getAvailableExteriorPosition() {
        return LocalAreaUtil.getLocalRelativeLocation(airlockExteriorPos.getX(), 
                airlockExteriorPos.getY(), vehicle);
    }

    @Override
    public Point2D getAvailableAirlockPosition() {
        return LocalAreaUtil.getLocalRelativeLocation(airlockInsidePos.getX(), 
                airlockInsidePos.getY(), vehicle);
    }
}
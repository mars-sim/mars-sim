/**
 * Mars Simulation Project
 * Airlock.java
 * @version 2.77 2004-10-01
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import org.mars_sim.msp.simulation.person.*;
import java.io.Serializable;

/** 
 * The Airlock class represents an airlock to a vehicle or structure.
 */
public abstract class Airlock implements Serializable {

    private static final double CYCLE_TIME = 10D; // Pressurize/depressurize time (millisols)
    
    // Data members
    private boolean activated;     // True if airlock is activated.
    protected boolean pressurized;   // True if airlock is pressurized.
    private boolean innerDoorOpen; // True if inner door is open.
    private boolean outerDoorOpen; // True if outer door is open.
    private int capacity;          // Number of people who can use the airlock at once;
    private double activationTime; // Amount of time airlock has been activated. (in millisols)
    private PersonCollection occupants; // People currently in airlock.
    
    /**
     * Constructs an airlock object for a unit.
     *
     * @param capacity number of people airlock can hold.
     * @throws IllegalArgumentException if capacity is less than one.
     */
    public Airlock(int capacity) throws IllegalArgumentException {
        
        // Initialize data members
        if (capacity < 1) throw new IllegalArgumentException("capacity less than one.");
        else this.capacity = capacity;
        pressurized = true;
        innerDoorOpen = true;
        occupants = new PersonCollection();
    }

    /**
     * Requests that the unoccupied airlock activate and open the door opposite to 
     * the one currently open when finished.  
     * Does nothing if airlock is currently occupied.
     */
    public void requestOpenDoor() {
        if (occupants.size() == 0) activateAirlock();
    }

    /**
     * Enters a person into the airlock from either the inside or the outside.
     * Inner or outer door (respectively) must be open for person to enter.
     * @param person the person to enter the airlock
     * @param inside true if person is entering from inside
     *               false if person is entering from outside
     * @return true if person entered the airlock successfully
     */
    public boolean enterAirlock(Person person, boolean inside) {
        boolean result = false;

        if (!activated && (occupants.size() < capacity)) {
            if ((inside && innerDoorOpen) || (!inside && outerDoorOpen)) {
                occupants.add(person);
	            result = true;
            }
        }

        return result;
    }

    /**
     * Activates the airlock if it is not already activated.
     * Automatically closes both doors and starts pressurizing/depressurizing.
     * @return true if airlock was successfully activated
     */
    public boolean activateAirlock() {
        boolean result = false;

        if (!activated) {
            innerDoorOpen = false;
            outerDoorOpen = false;
            activated = true;
            result = true;
        }

        return result;
    }

    /**
     * Deactivates the airlock and opens the appropriate door.
     * Any people in the airlock are transferred inside or outside
     * the airlock.
     */
    private void deactivateAirlock() {
        if (activated) {
            activated = false;
            activationTime = 0D;
            pressurized = !pressurized;
            if (pressurized) innerDoorOpen = true;
            else outerDoorOpen = true;
            PersonIterator i = occupants.iterator();
            while (i.hasNext()) {
                try {
                    exitAirlock(i.next());
                }
                catch (Exception e) { 
                    System.err.println(e.getMessage()); 
                }
            }
            occupants.clear();
        }
    }
    
    /**
     * Causes a person within the airlock to exit either inside or outside.
     *
     * @param person the person to exit.
     * @throws Exception if person is not in the airlock.
     */
    protected abstract void exitAirlock(Person person) throws Exception;      
    
    /** 
     * Checks if the airlock's outer door is open.
     * @return true if outer door is open
     */
    public boolean isOuterDoorOpen() {
        return outerDoorOpen;
    }

    /**
     * Checks if the airlock's inner door is open.
     * @return true if inner door is open
     */
    public boolean isInnerDoorOpen() {
        return innerDoorOpen;
    }
 
    /**
     * Operates the airlock for the given amount of time.
     * Called from the unit owning the airlock.
     * @param time amount of time (in millisols)
     */
    public void timePassing(double time) {
        if (activated) {
            activationTime += time;
            if (activationTime >= CYCLE_TIME) deactivateAirlock();
        }
    }

    /**
     * Checks if given person is currently in the airlock.
     * @param person to be checked
     * @return true if person is in airlock
     */
    public boolean inAirlock(Person person) {
        return occupants.contains(person);
    }
    
    /**
     * Gets the name of the entity this airlock is attached to.
     *
     * @return name
     */
    public abstract String getEntityName();
    
    /**
     * Gets the inventory of the entity this airlock is attached to.
     *
     * @return inventory
     */
    public abstract Inventory getEntityInventory();
}

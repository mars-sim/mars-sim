/**
 * Mars Simulation Project
 * Airlock.java
 * @version 2.80 2007-03-29
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.simulation.person.Person;


/** 
 * The Airlock class represents an airlock to a vehicle or structure.
 */
public abstract class Airlock implements Serializable {
    
    private static String CLASS_NAME = "org.mars_sim.msp.simulation.Airlock";
	
    private static Logger logger = Logger.getLogger(CLASS_NAME);

    private static final double CYCLE_TIME = 5D; // Pressurize/depressurize time (millisols)
    
    // Data members
    private boolean activated;     // True if airlock is activated.
    protected boolean pressurized;   // True if airlock is pressurized.
    private boolean innerDoorOpen; // True if inner door is open.
    private boolean outerDoorOpen; // True if outer door is open.
    private int capacity;          // Number of people who can use the airlock at once;
    private double activationTime; // Amount of time airlock has been activated. (in millisols)
    private Collection occupants; // People currently in airlock.
    private double operationalTime;
    
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
        occupants = new ConcurrentLinkedQueue();
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
     * Activate the airlock for a period of time.
     * @param time activation time (millisols)
     * @return time remaining after activation (millisols)
     */
    public double addActivationTime(double time) {
    	double remainingTime = 0D;
    	
    	activateAirlock();
    	
    	if (operationalTime < time) {
    		remainingTime = time - operationalTime;
    		time = operationalTime;
    	}
    	
    	activationTime += time;
        
    	if (activationTime >= CYCLE_TIME) {
        	double extraCycleTime = activationTime - CYCLE_TIME;
        	remainingTime += extraCycleTime;
        	operationalTime += extraCycleTime - time;
        	deactivateAirlock();
        }
    	else {
    		operationalTime -= time;
    		remainingTime = 0D;
    	}
    	
    	return remainingTime;
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
            Iterator<Person> i = occupants.iterator();
            while (i.hasNext()) {
                try {
                    exitAirlock(i.next());
                }
                catch (Exception e) { 
                    logger.log(Level.SEVERE,e.getMessage()); 
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
    	operationalTime = time;
    	
        if (activated) {
        	// Check if somehow activated with no occupants or
        	// if all occupants are dead.
        	if (occupants.size() == 0) deactivateAirlock();
        	else {
        		boolean allDead = true;
        		Iterator<Person> i = occupants.iterator();
        		while (i.hasNext()) {
        			if (!i.next().getPhysicalCondition().isDead()) allDead = false;
        		}
        		if (allDead) deactivateAirlock();
        	}
        	/*
            activationTime += time;
            if (activationTime >= CYCLE_TIME) deactivateAirlock();
            */
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

/**
 * Mars Simulation Project
 * Airlock.java
 * @version 2.74 2002-03-10
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import org.mars_sim.msp.simulation.person.*;
import java.io.Serializable;

/** 
 * The Airlock class represents an airlock to a vehicle or structure.
 */
public class Airlock implements Serializable {

    // Data members
    private double airlockTime;    // The amount of time required for the airlock to 
                                   // pressurize/depressurize. (in millisols)
    private Unit unit;             // the unit the airlock is attached to.
    private VirtualMars mars;      // The Mars instance.
    private boolean activated;     // True if airlock is activated.
    private boolean pressurized;   // True if airlock is pressurized.
    private boolean innerDoorOpen; // True if inner door is open.
    private boolean outerDoorOpen; // True if outer door is open.
    private int capacity;          // Number of people who can use the airlock at once;
    private double activationTime; // Amount of time airlock has been activated. (in millisols)
    private PersonCollection occupants; // People currently in airlock.
    
    /**
     * Constructs an airlock object for a unit.
     * @param unit the unit the airlock is attached to
     * @param mars the Mars instance
     * @param capacity number of people airlock can hold
     */
    public Airlock(Unit unit, VirtualMars mars, int capacity) {
        
        // Initialize data members
	this.unit = unit;
	this.mars = mars;
	this.capacity = capacity;
	pressurized = true;
	innerDoorOpen = true;
	occupants = new PersonCollection();

	// Initialize airlock time to system property.
	airlockTime = mars.getSimulationProperties().getAirlockCycleTime();
    }

    /**
     * Requests that the unoccupied airlock activate and open the door opposite to 
     * the one currently open when finished.  
     * Does nothing if airlock is currently occupied.
     */
    public void requestOpenDoor() {
        if (occupants.size() == 0) {
	    activateAirlock();
	}
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
		if (inside) System.out.println(person.getName() + " entering " + unit.getName() + " airlock from inside.");
		else System.out.println(person.getName() + " entering " + unit.getName() + " airlock from outside.");
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
	    System.out.println(unit.getName() + " airlock activated.");
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
	    System.out.println(unit.getName() + " airlock deactivated.");
	    activated = false;
	    activationTime = 0D;
	    pressurized = !pressurized;
	    if (pressurized) innerDoorOpen = true;
	    else outerDoorOpen = true;
	    if (innerDoorOpen) System.out.println(unit.getName() + " airlock inner door open.");
	    if (outerDoorOpen) System.out.println(unit.getName() + " airlock outer door open.");
	    PersonIterator i = occupants.iterator();
	    while (i.hasNext()) {
		Person person = i.next();
	        if (pressurized) {
		    unit.getInventory().addUnit(person);
		    System.out.println(person.getName() + " entering " + unit.getName() + ".");
		}
		else {
		    unit.getInventory().dropUnitOutside(person);
		    System.out.println(person.getName() + " exiting " + unit.getName() + ".");
		}
	    }
	    occupants.clear();
	}
    }
    
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
     * @param amount of time (in millisols)
     */
    public void timePassing(double time) {
        if (activated) {
	    activationTime += time;
	    if (activationTime >= airlockTime) deactivateAirlock();
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
}				

/**
 * Mars Simulation Project
 * ExitAirlock.java
 * @version 2.74 2002-05-05
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.equipment.*;
import java.io.Serializable;

/** 
 * The ExitAirlock class is a task for exiting a airlock from an EVA operation.
 */
class ExitAirlock extends Task implements Serializable {

    // Data members
    private Airlockable entity;      // The entity to be exited.
    private boolean hasSuit = false; // True if person has an EVA suit.

    /** 
     * Constructs an ExitAirlock object
     * @param person the person to perform the task
     * @param mars the virtual Mars
     * @param entity the entity to exit
     */
    public ExitAirlock(Person person, Mars mars, Airlockable entity) {
        super("Exiting airlock for EVA", person, true, mars);

        // Initialize data members
	description = "Exiting " + entity.getName() + " for EVA";
        this.entity = entity;

	// System.out.println(person.getName() + " is starting to exit airlock of " + entity.getName());
    }

    /** 
     * Performs this task for the given amount of time.
     * @param time the amount of time to perform this task (in millisols)
     * @return amount of time remaining after finishing with task (in millisols)
     */
    double performTask(double time) {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

        // Get an EVA suit from entity inventory.
	if (!hasSuit) {
	    if (goodEVASuitAvailable(entity)) {
	        Inventory inv = ((Unit) entity).getInventory();
	        UnitCollection evaSuits = inv.getUnitsOfClass(EVASuit.class);
	        UnitIterator i = evaSuits.iterator();
	        while (i.hasNext() && !hasSuit) {
	            EVASuit suit = (EVASuit) i.next();
		    boolean goodSuit = true;
	            if (!suit.isFullyLoaded()) goodSuit = false;
	            if (!suit.lifeSupportCheck()) goodSuit = false;
	            if (suit.getMalfunctionManager().hasMalfunction()) goodSuit = false;
		    if (goodSuit) {
		        // System.out.println(person.getName() + " taking EVA suit from " + entity.getName());
		        if (inv.takeUnit(suit, person)) hasSuit = true;
		    }
	        }
	    }
	}

	// If person still doesn't have an EVA suit, end task.
	if (!hasSuit) {
            // System.out.println(person.getName() + " does not have an EVA suit, ExitAirlock ended");
	    done = true;
	    return timeLeft;
	}

        Airlock airlock = entity.getAirlock();

	// If person is in airlock, wait around.
	if (airlock.inAirlock(person)) {
	    // Make sure airlock is activated.
	    airlock.activateAirlock();
	}
	else {
	    // If person is in entity, try to enter airlock.
	    if (!person.getLocationSituation().equals(Person.OUTSIDE)) {
	        if (airlock.isInnerDoorOpen()) airlock.enterAirlock(person, true);
	        else airlock.requestOpenDoor();
	    }
	    else {
	        // If person is outside, end task.
	        done = true;
	    }
	}
	
	return 0D;
    }

    /**
     * Checks if a person can exit an airlock on an EVA.
     * @param person the person exiting
     * @param entity the entity to be exited 
     * @return true if person can exit the entity 
     */
    public static boolean canExitAirlock(Person person, Airlockable entity) {
        boolean result = true;

	// Check if EVA suit is available in the entity.
	if (!goodEVASuitAvailable(entity)) result = false;

	return result;
    }
    
    /**
     * Checks if a good EVA suit is in entity inventory.
     * @param entity the entity 
     * @return true if good EVA suit is in inventory
     */
    public static boolean goodEVASuitAvailable(Airlockable entity) {
   
        Inventory inv = ((Unit) entity).getInventory();

	UnitCollection evaSuits = inv.getUnitsOfClass(EVASuit.class);
        // System.out.println(entity.getName() + " has " + evaSuits.size() + " EVA suits.");
	int goodSuits = 0;
	UnitIterator i = evaSuits.iterator();
        while (i.hasNext()) {
	    EVASuit suit = (EVASuit) i.next();
	    boolean goodSuit = true;
	    if (!suit.isFullyLoaded()) goodSuit = false;
	    if (!suit.lifeSupportCheck()) goodSuit = false;
	    if (suit.getMalfunctionManager().hasMalfunction()) goodSuit = false;
	    if (goodSuit) goodSuits++;
	}

        // System.out.println(entity.getName() + " has " + goodSuits + " good EVA suits.");
	
	return (goodSuits > 0);
    }
}

/**
 * Mars Simulation Project
 * ExitRoverEVA.java
 * @version 2.74 2002-03-11
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.vehicle.*;
import org.mars_sim.msp.simulation.equipment.*;
import java.io.Serializable;

/** 
 * The ExitRoverEVA class is a task for exiting a rover during a EVA operation.
 */
class ExitRoverEVA extends Task implements Serializable {

    // Data members
    private Rover rover; // The rover to be exited.
    private boolean hasSuit = false; // True if person has an EVA suit.

    /** 
     * Constructs an ExitRoverEVA object
     * @param person the person to perform the task
     * @param mars the virtual Mars
     * @param rover the rover to exit
     */
    public ExitRoverEVA(Person person, Mars mars, Rover rover) {
        super("Exiting rover for EVA", person, true, mars);

        // Initialize data members
	description = "Exiting " + rover.getName() + " for EVA";
        this.rover = rover;

	// System.out.println(person.getName() + " is starting to exit " + rover.getName());
    }

    /** 
     * Performs this task for the given amount of time.
     * @param time the amount of time to perform this task (in millisols)
     * @return amount of time remaining after finishing with task (in millisols)
     */
    double performTask(double time) {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

        // Get an EVA suit from rover inventory.
	if (!hasSuit) {
	    if (goodEVASuitAvailable(rover)) {
	        Inventory inv = rover.getInventory();
	        UnitCollection evaSuits = inv.getUnitsOfClass(EVASuit.class);
	        UnitIterator i = evaSuits.iterator();
	        while (i.hasNext() && !hasSuit) {
	            EVASuit suit = (EVASuit) i.next();
		    if (suit.isFullyLoaded() && suit.lifeSupportCheck()) {
		        // System.out.println(person.getName() + " taking EVA suit from " + rover.getName());
		        if (inv.takeUnit(suit, person)) hasSuit = true;
		    }
	        }
	    }
	}

	// If person still doesn't have an EVA suit, end task.
	if (!hasSuit) {
            // System.out.println(person.getName() + " does not have an EVA suit, ExitRoverEVA ended");
	    done = true;
	    return timeLeft;
	}

        Airlock airlock = rover.getAirlock();

	// If person is in airlock, wait around.
	if (airlock.inAirlock(person)) {
	    // Make sure airlock is activated.
	    airlock.activateAirlock();
	}
	else {
	    // If person is in rover, try to enter airlock.
	    if (person.getLocationSituation().equals(Person.INVEHICLE)) {
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
     * Checks if a person can exit a rover on an EVA.
     * @param person
     * @param rover
     * @return true if person can exit the rover
     */
    public static boolean canExitRover(Person person, Rover rover) {
        boolean result = true;

	// Check if EVA suit is available in rover.
	if (!goodEVASuitAvailable(rover)) result = false;

	// Check if person's medical condition prevents EVA.
	// (implement later)
	
	return result;
    }
    
    /**
     * Checks if a good EVA suit is in rover inventory.
     * @param rover the rover
     * @return true if good EVA suit is in inventory
     */
    public static boolean goodEVASuitAvailable(Rover rover) {
   
	// System.out.println("ExitRoverEVA.goodEVASuitAvailable() ");
        Inventory inv = rover.getInventory();

	UnitCollection evaSuits = inv.getUnitsOfClass(EVASuit.class);
        // System.out.println(rover.getName() + " has " + evaSuits.size() + " EVA suits.");
	int goodSuits = 0;
	UnitIterator i = evaSuits.iterator();
        while (i.hasNext()) {
	    // System.out.println("Checking EVA suit");
	    EVASuit suit = (EVASuit) i.next();
	    // System.out.println("EVA suit.isFullyLoaded(): " + suit.isFullyLoaded());
	    // System.out.println("EVA suit.lifeSupportCheck(): " + suit.lifeSupportCheck());
	    if (suit.isFullyLoaded() && suit.lifeSupportCheck()) 
	        goodSuits++;
	}

        // System.out.println(rover.getName() + " has " + goodSuits + " good EVA suits.");
	
	return (goodSuits > 0);
    }
}

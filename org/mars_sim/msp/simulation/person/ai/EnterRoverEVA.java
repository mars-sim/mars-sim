/**
 * Mars Simulation Project
 * EnterRoverEVA.java
 * @version 2.74 2002-02-24
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.*;
import org.mars_sim.msp.simulation.vehicle.*;
import org.mars_sim.msp.simulation.equipment.*;
import java.io.Serializable;

/** 
 * The EnterRoverEVA class is a task for entering a rover from an EVA operation. 
 */
class EnterRoverEVA extends Task implements Serializable {

    // Data members
    private Rover rover; // The rover to be entered.
    private boolean inAirlock = false; // True if person is in the rover's airlock.
    private MarsClock airlockStartTime; // The start time for using an airlock.

    /** 
     * Constructs a EnterRoverEVA object
     * @param person the person to perform the task
     * @param mars the virtual Mars
     * @param rover the rover to be entered
     */
    public EnterRoverEVA(Person person, VirtualMars mars, Rover rover) {
        super("Entering rover from EVA", person, false, mars);

	// Initialize data members
	description = "Entering " + rover.getName() + " from EVA";
        this.rover = rover;

	System.out.println(person.getName() + " is starting to enter " + rover.getName());
    }

    /** 
     * Performs this task for the given amount of time.
     * @param time the amount of time to perform this task (in millisols)
     * @return amount of time remaining after finishing with task (in millisols)
     */
    double performTask(double time) {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

	// If person is not in the rover's airlock, go in if it's available
	// or wait if it's not.
	if (!inAirlock) {
            if (!rover.isAirlockOccupied()) {
		System.out.println(person.getName() + " entering " + rover.getName() + " airlock.");
                inAirlock = true;
                rover.setAirlockOccupied(true);
		airlockStartTime = (MarsClock) mars.getMasterClock().getMarsClock().clone();
            }
	    else System.out.println(person.getName() + " waiting for " + rover.getName() + " airlock to become available.");
        }

	// If person is in the rover's airlock, wait required period of time
	// and enter the rover.
	if (inAirlock) {
	    MarsClock currentTime = mars.getMasterClock().getMarsClock();
	    double currentAirlockTime = MarsClock.getTimeDiff(currentTime, airlockStartTime) + timeLeft;
	    if (currentAirlockTime >= Rover.AIRLOCK_TIME) {
		System.out.println(person.getName() + " exiting " + rover.getName() + " airlock.");
		System.out.println(person.getName() + " is inside " + rover.getName());
	        rover.setAirlockOccupied(false);
		rover.getInventory().addUnit(person);
		putAwayEVASuit();
		done = true;
	       	return currentAirlockTime - Rover.AIRLOCK_TIME;
	    }
	    else System.out.println(person.getName() + " waiting inside " + rover.getName() + " airlock.");
	}

	return 0D;
    }

    /**
     * Puts the person's EVA suite back into the rover's inventory.
     * EVA Suit is refilled with oxygen and water from the rover's inventory.
     */
    private void putAwayEVASuit() {
       
        EVASuit suit = (EVASuit) person.getInventory().findUnit(EVASuit.class);
	Inventory suitInv = suit.getInventory();
	Inventory personInv = person.getInventory();
	Inventory roverInv = rover.getInventory();

	// Refill oxygen in suit from rover's inventory. 
	double neededOxygen = suitInv.getResourceRemainingCapacity(Inventory.OXYGEN);
	double takenOxygen = roverInv.removeResource(Inventory.OXYGEN, neededOxygen);
	System.out.println(person.getName() + " refilling EVA suit with " + takenOxygen + " oxygen.");
	suitInv.addResource(Inventory.OXYGEN, takenOxygen);

	// Refill water in suit from rover's inventory.
	double neededWater = suitInv.getResourceRemainingCapacity(Inventory.WATER);
	double takenWater = roverInv.removeResource(Inventory.WATER, neededWater);
	System.out.println(person.getName() + " refilling EVA suit with " + takenWater + " water.");
	suitInv.addResource(Inventory.WATER, takenWater);

	// Return suit to rover's inventory.
	System.out.println(person.getName() + " putting away EVA suit into " + rover.getName());
	personInv.takeUnit(suit, rover);
    }
}

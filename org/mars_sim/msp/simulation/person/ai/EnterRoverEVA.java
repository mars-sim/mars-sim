/**
 * Mars Simulation Project
 * EnterRoverEVA.java
 * @version 2.74 2002-03-11
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

    /** 
     * Constructs a EnterRoverEVA object
     * @param person the person to perform the task
     * @param mars the virtual Mars
     * @param rover the rover to be entered
     */
    public EnterRoverEVA(Person person, Mars mars, Rover rover) {
        super("Entering rover from EVA", person, false, mars);

	// Initialize data members
	description = "Entering " + rover.getName() + " from EVA";
        this.rover = rover;

	// System.out.println(person.getName() + " is starting to enter " + rover.getName());
    }

    /** 
     * Performs this task for the given amount of time.
     * @param time the amount of time to perform this task (in millisols)
     * @return amount of time remaining after finishing with task (in millisols)
     */
    double performTask(double time) {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

        Airlock airlock = rover.getAirlock();
	
        // If person is in airlock, wait around.
	if (airlock.inAirlock(person)) {
	    // Make sure airlock is activated.
	    airlock.activateAirlock();
	}
	else {
	    // If person is outside, try to enter airlock.
	    if (person.getLocationSituation().equals(Person.OUTSIDE)) {
	        if (airlock.isOuterDoorOpen()) airlock.enterAirlock(person, false);
		else airlock.requestOpenDoor();
	    }
	    else {
	        // If person is inside, put stuff away and end task.
	        putAwayEVASuit();
		done = true;
	    }
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
	// System.out.println(person.getName() + " refilling EVA suit with " + takenOxygen + " oxygen.");
	suitInv.addResource(Inventory.OXYGEN, takenOxygen);

	// Refill water in suit from rover's inventory.
	double neededWater = suitInv.getResourceRemainingCapacity(Inventory.WATER);
	double takenWater = roverInv.removeResource(Inventory.WATER, neededWater);
	// System.out.println(person.getName() + " refilling EVA suit with " + takenWater + " water.");
	suitInv.addResource(Inventory.WATER, takenWater);

	// Return suit to rover's inventory.
	// System.out.println(person.getName() + " putting away EVA suit into " + rover.getName());
	personInv.takeUnit(suit, rover);
    }
}

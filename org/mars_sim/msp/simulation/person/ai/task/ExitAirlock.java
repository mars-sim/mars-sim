/**
 * Mars Simulation Project
 * ExitAirlock.java
 * @version 2.75 2004-01-06
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;

import org.mars_sim.msp.simulation.Airlock;
import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.Mars;
import org.mars_sim.msp.simulation.UnitIterator;
import org.mars_sim.msp.simulation.equipment.EVASuit;
import org.mars_sim.msp.simulation.person.Person;

/** 
 * The ExitAirlock class is a task for exiting a airlock from an EVA operation.
 */
class ExitAirlock extends Task implements Serializable {

    // Data members
    private Airlock airlock; // The airlock to be used.
    private boolean hasSuit = false; // True if person has an EVA suit.

    /** 
     * Constructs an ExitAirlock object
     * @param person the person to perform the task
     * @param mars the virtual Mars
     * @param airlock the airlock to use.
     */
    public ExitAirlock(Person person, Mars mars, Airlock airlock) {
        super("Exiting airlock for EVA", person, true, mars);

        // Initialize data members
        description = "Exiting " + airlock.getEntityName() + " for EVA";
        this.airlock = airlock;

        // System.out.println(person.getName() + " is starting to exit airlock of " + airlock.getEntityName());
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
            Inventory inv = airlock.getEntityInventory();
            EVASuit suit = getGoodEVASuit(inv);
            if (suit != null) {
                inv.takeUnit(suit, person);
                hasSuit = true;
            }
        }

        // If person still doesn't have an EVA suit, end task.
        if (!hasSuit) {
            // System.out.println(person.getName() + " does not have an EVA suit, ExitAirlock ended");
            endTask();
            return timeLeft;
        }

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
                endTask();
            }
        }
	
        return 0D;
    }

    /**
     * Checks if a person can exit an airlock on an EVA.
     * @param person the person exiting
     * @param airlock the airlock to be used
     * @return true if person can exit the entity 
     */
    public static boolean canExitAirlock(Person person, Airlock airlock) {

        // Check if EVA suit is available.
        // return (goodEVASuitAvailable(airlock.getEntityInventory()));
        boolean result = goodEVASuitAvailable(airlock.getEntityInventory());
        if (!result) {
            EVASuit finalSuit = null;
        
            UnitIterator i = airlock.getEntityInventory().getUnitsOfClass(EVASuit.class).iterator();
            while (i.hasNext() && (finalSuit == null)) {
                EVASuit suit = (EVASuit) i.next();
                boolean fullyLoaded = suit.isFullyLoaded();
                boolean lifeSupport = suit.lifeSupportCheck();
                boolean malfunction = suit.getMalfunctionManager().hasMalfunction();
                if (fullyLoaded && lifeSupport && !malfunction) finalSuit = suit; 
                else {
                    // System.out.println("EVA Suit fullyLoaded: " + fullyLoaded);
                    // System.out.println("EVA Suit lifeSupport: " + lifeSupport);
                    // System.out.println("EVA Suit malfunction: " + malfunction);
                }
            }
        
            // if (finalSuit == null) System.out.println("ExitAirlock.getGoodEVASuit() false");
        }
        
        return result;
    }
    
    /**
     * Checks if a good EVA suit is in entity inventory.
     * @param inv the inventory to check.
     * @return true if good EVA suit is in inventory
     */
    public static boolean goodEVASuitAvailable(Inventory inv) {
        return (getGoodEVASuit(inv) != null);
    }
    
    /**
     * Gets a good EVA suit from an inventory.
     *
     * @param the inventory to check.
     * @return EVA suit or null if none available.
     */
    public static EVASuit getGoodEVASuit(Inventory inv) {

        EVASuit result = null;
        
        UnitIterator i = inv.getUnitsOfClass(EVASuit.class).iterator();
        while (i.hasNext() && (result == null)) {
            EVASuit suit = (EVASuit) i.next();
            boolean fullyLoaded = suit.isFullyLoaded();
            boolean lifeSupport = suit.lifeSupportCheck();
            boolean malfunction = suit.getMalfunctionManager().hasMalfunction();
            if (fullyLoaded && lifeSupport && !malfunction) result = suit; 
        }
        
        return result;
    }
}

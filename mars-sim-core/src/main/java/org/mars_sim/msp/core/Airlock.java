/**
 * Mars Simulation Project
 * Airlock.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */

package org.mars_sim.msp.core;

import org.mars_sim.msp.core.person.Person;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;


/** 
 * The Airlock class represents an airlock to a vehicle or structure.
 */
public abstract class Airlock implements Serializable {

    private static Logger logger = Logger.getLogger(Airlock.class.getName());

    // Pressurize/depressurize time (millisols)
    public static final double CYCLE_TIME = 5D;

    // Airlock states.
    public static final String PRESSURIZED = "pressurized";
    public static final String DEPRESSURIZED = "depressurized";
    public static final String PRESSURIZING = "pressurizing";
    public static final String DEPRESSURIZING = "depressurizing";

    // Data members
    private String state; // The state of the airlock.
    private boolean activated;     // True if airlock is activated.
    private boolean innerDoorLocked; // True if inner door is locked.
    private boolean outerDoorLocked; // True if outer door is locked.
    private int capacity;          // Number of people who can use the airlock at once;
    private double remainingCycleTime; // Amount of remaining time for the airlock cycle. (in millisols)
    private Collection<Person> occupants; // People currently in airlock.
    private Person operator; // The person currently operating the airlock.
    private List<Person> awaitingInnerDoor; // People waiting for the airlock by the inner door.
    private List<Person> awaitingOuterDoor; // People waiting for the airlock by the outer door.

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

        activated = false;
        state = PRESSURIZED;
        innerDoorLocked = false;
        outerDoorLocked = true;
        remainingCycleTime = 0D;
        occupants = new ConcurrentLinkedQueue<Person>();
        operator = null;
        awaitingInnerDoor = new ArrayList<Person>();
        awaitingOuterDoor = new ArrayList<Person>();
    }

    /**
     * Enters a person into the airlock from either the inside or the outside.
     * Inner or outer door (respectively) must be unlocked for person to enter.
     * @param person the person to enter the airlock
     * @param inside true if person is entering from inside
     *               false if person is entering from outside
     * @return true if person entered the airlock successfully
     */
    public boolean enterAirlock(Person person, boolean inside) {
        boolean result = false;

        if (!occupants.contains(person) && (occupants.size() < capacity)) {

            if (inside && !innerDoorLocked) {
                if (awaitingInnerDoor.contains(person)) {
                    awaitingInnerDoor.remove(person);
                }
                logger.fine(person.getName() + " enters inner door of " + getEntityName() + " airlock.");
                result = true;
            }
            else if (!inside && !outerDoorLocked) {
                if (awaitingOuterDoor.contains(person)) {
                    awaitingOuterDoor.remove(person);
                }
                logger.fine(person.getName() + " enters outer door of " + getEntityName() + " airlock.");
                result = true;
            }

            if (result) {
                occupants.add(person);
            }
        }

        return result;
    }

    /**
     * Activates the airlock if it is not already activated.
     * Automatically closes both doors and starts pressurizing/depressurizing.
     * @param operator the person operating the airlock.
     * @return true if airlock successfully activated.
     */
    public boolean activateAirlock(Person operator) {

        boolean result = false;

        if (!activated) {
            if (!innerDoorLocked) {
                while ((occupants.size() < capacity) && (awaitingInnerDoor.size() > 0)) {
                    Person person = awaitingInnerDoor.get(0);
                    awaitingInnerDoor.remove(person);
                    if (!occupants.contains(person)) {
                        logger.fine(person.getName() + " enters inner door of " + getEntityName() + " airlock.");
                        occupants.add(person);
                    }
                }
                innerDoorLocked = true;
            }
            else if (!outerDoorLocked) {
                while ((occupants.size() < capacity) && (awaitingOuterDoor.size() > 0)) {
                    Person person = awaitingOuterDoor.get(0);
                    awaitingOuterDoor.remove(person);
                    if (!occupants.contains(person)) {
                        logger.fine(person.getName() + " enters outer door of " + getEntityName() + " airlock.");
                        occupants.add(person);
                    }
                }
                outerDoorLocked = true;
            }
            else {
                return false;
            }

            activated = true;
            remainingCycleTime = CYCLE_TIME;

            if (PRESSURIZED.equals(state)) {
                setState(DEPRESSURIZING);
            }
            else if (DEPRESSURIZED.equals(state)) {
                setState(PRESSURIZING);
            }
            else {
                logger.severe("Airlock in incorrect state for activation: " + state);
                return false;
            }

            this.operator = operator;

            result = true;
        }

        return result;
    }

    /**
     * Add airlock cycle time.
     * @param time cycle time (millisols)
     * @return true if cycle time successfully added.
     */
    public boolean addCycleTime(double time) {

        boolean result = false;

        if (activated) {
            remainingCycleTime -= time;
            if (remainingCycleTime <= 0D) {
                remainingCycleTime = 0D;
                result = deactivateAirlock();
            }
            else {
                result = true;
            }
        }

        return result;
    }

    /**
     * Deactivates the airlock and opens the appropriate door.
     * Any people in the airlock are transferred inside or outside
     * the airlock.
     * @return true if airlock was deactivated successfully.
     */
    private boolean deactivateAirlock() {

        boolean result = false;

        if (activated) {
            activated = false;

            if (DEPRESSURIZING.equals(state)) {
                setState(DEPRESSURIZED);
                outerDoorLocked = false;
            }
            else if (PRESSURIZING.equals(state)) {
                setState(PRESSURIZED);
                innerDoorLocked = false;
            }
            else {
                return false;
            }

            Iterator<Person> i = occupants.iterator();
            while (i.hasNext()) {
                try {
                    exitAirlock(i.next());
                }
                catch (Exception e) { 
                    logger.severe(e.getMessage()); 
                }
            }
            occupants.clear();

            operator = null;

            result = true;
        }

        return result;
    }

    /**
     * Causes a person within the airlock to exit either inside or outside.
     *
     * @param person the person to exit.
     * @throws Exception if person is not in the airlock.
     */
    protected abstract void exitAirlock(Person person) throws Exception;      

    /** 
     * Checks if the airlock's outer door is locked.
     * @return true if outer door is locked
     */
    public boolean isOuterDoorLocked() {
        return outerDoorLocked;
    }

    /**
     * Checks if the airlock's inner door is locked.
     * @return true if inner door is locked
     */
    public boolean isInnerDoorLocked() {
        return innerDoorLocked;
    }

    /**
     * Checks if the airlock is currently activated.
     * @return true if activated.
     */
    public boolean isActivated() {
        return activated;
    }

    /**
     * Gets the current state of the airlock.
     * @return the state string.
     */
    public String getState() {
        return state;
    }
    
    /**
     * Sets the state of the airlock.
     * @param state the airlock state.
     */
    private void setState(String state) {
        this.state = state;
        logger.fine(getEntityName() + " airlock is " + state);
    }

    /**
     * Gets the airlock operator.
     * @return the airlock operator or null if none.
     */
    public Person getOperator() {
        return operator;
    }
    
    /**
     * Clears the airlock operator.
     */
    public void clearOperator() {
        operator = null;
    }

    /**
     * Gets the remaining airlock cycle time.
     * @return time (millisols)
     */
    public double getRemainingCycleTime() {
        return remainingCycleTime;
    }

    /**
     * Adds person to queue awaiting airlock by inner door.
     * @param person the person to add to the awaiting queue.
     */
    public void addAwaitingAirlockInnerDoor(Person person) {
        if (!awaitingInnerDoor.contains(person)) {
            logger.fine(person.getName() + " awaiting inner door of " + getEntityName() + " airlock.");
            awaitingInnerDoor.add(person);
        }
    }

    /**
     * Adds person to queue awaiting airlock by outer door.
     * @param person the person to add to the awaiting queue.
     */
    public void addAwaitingAirlockOuterDoor(Person person) {
        if (!awaitingOuterDoor.contains(person)) {
            logger.fine(person.getName() + " awaiting outer door of " + getEntityName() + " airlock.");
            awaitingOuterDoor.add(person);
        }
    }

    /**
     * Time passing for airlock.
     * Check for unusual situations and deal with them.
     * Called from the unit owning the airlock.
     * @param time amount of time (in millisols)
     */
    public void timePassing(double time) {

        if (activated) {
            // Check if operator is dead.
            if (operator != null) {
                if (operator.getPhysicalCondition().isDead()) {
                    // If operator is dead, deactivate airlock.
                	String operatorName = operator.getName();
                    deactivateAirlock();
                    logger.severe("Airlock operator " + operatorName +
                    " is dead.  Deactivating airlock of " + getEntityName());
                }
            }
            else {
                // If not operator, deactivate airlock.
                deactivateAirlock();
                logger.severe("Airlock has no operator.  Deactivating airlock of " + getEntityName());
            }
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
     * Gets the airlock capacity.
     * @return capacity.
     */
    public int getCapacity() {
        return capacity;
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
    
    /**
     * Gets the entity this airlock is attached to.
     * @return entity.
     */
    public abstract Object getEntity();

    /**
     * Prepare object for garbage collection.
     */
    public void destroy() {
        occupants.clear();
        occupants = null;
        awaitingInnerDoor.clear();
        awaitingInnerDoor = null;
        awaitingOuterDoor.clear();
        awaitingOuterDoor = null;
        operator = null;
    }
}
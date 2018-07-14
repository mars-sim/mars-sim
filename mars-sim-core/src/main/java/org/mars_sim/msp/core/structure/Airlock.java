/**
 * Mars Simulation Project
 * Airlock.java
 * @version 3.1.0 2017-03-09
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.EnterAirlock;
import org.mars_sim.msp.core.person.ai.task.ExitAirlock;
import org.mars_sim.msp.core.person.ai.task.Task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;


// see discussions on Airlocks for Mars Colony at 
// https://forum.nasaspaceflight.com/index.php?topic=42098.0

// Astronauts aboard the International Space Station preparing for extra-vehicular activity (EVA) 
// "camp out" at low atmospheric pressure, 10.2 psi (0.70 bar), spending eight sleeping hours 
// in the Quest airlock chamber before their spacewalk. During the EVA they breathe 100% oxygen 
// in their spacesuits, which operate at 4.3 psi (0.30 bar),[71] although research has examined 
// the possibility of using 100% O2 at 9.5 psi (0.66 bar) in the suits to lessen the pressure 
// reduction, and hence the risk of DCS.[72]
// see https://en.wikipedia.org/wiki/Decompression_sickness

/**
 * The Airlock class represents an airlock to a vehicle or structure.
 */
public abstract class Airlock implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** default logger. */
    private static Logger logger = Logger.getLogger(Airlock.class.getName());

    /** Pressurize/depressurize time (millisols). */
    public static final double CYCLE_TIME = 5D; // TODO: should we add pre-breathing time into CYCLE_TIME ?

    // TODO Airlock states should be an enum.
    public static final String PRESSURIZED = "pressurized";
    public static final String DEPRESSURIZED = "depressurized";
    public static final String PRESSURIZING = "pressurizing";
    public static final String DEPRESSURIZING = "depressurizing";

    // Data members
    /** The state of the airlock. */
    private String state;
    
    /** True if airlock is activated. */
    private boolean activated;
    /** True if inner door is locked. */
    private boolean innerDoorLocked;
    /** True if outer door is locked. */
    private boolean outerDoorLocked;
    
    /** Number of people who can use the airlock at once. */
    private int capacity;
    /** Number of times no eva suit is found available. */
    private int numEVASuitChecks;
    
    /** Amount of remaining time for the airlock cycle. (in millisols) */
    private double remainingCycleTime;
    
    /** People currently in airlock. */
//    private Collection<Unit> occupants;
    private Collection<Person> occupants;

    
    /** The person currently operating the airlock. */
//    private Unit operator;
    private Person operator;
    /** People waiting for the airlock by the inner door. */
//    private List<Unit> awaitingInnerDoor;
    private List<Person> awaitingInnerDoor;

    /** People waiting for the airlock by the outer door. */
//    private List<Unit> awaitingOuterDoor;
    private List<Person> awaitingOuterDoor;

    /**
     * Constructs an airlock object for a unit.
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
//        occupants = new ConcurrentLinkedQueue<Unit>();
        operator = null;
//        awaitingInnerDoor = new ArrayList<Unit>();
//        awaitingOuterDoor = new ArrayList<Unit>();
        occupants = new ConcurrentLinkedQueue<Person>();
        awaitingInnerDoor = new ArrayList<Person>();
        awaitingOuterDoor = new ArrayList<Person>();
    }

    /**
     * Enters a person into the airlock from either the inside or the outside.
     * Inner or outer door (respectively) must be unlocked for person to enter.
     * @param person {@link Person} the person to enter the airlock
     * @param inside {@link Boolean} <code>true</code> if person is entering from inside<br/>
     * <code>false</code> if person is entering from outside
     * @return {@link Boolean} <code>true</code> if person entered the airlock successfully
     */
    public boolean enterAirlock(Person person, boolean inside) {
        boolean result = false;

        if (!occupants.contains(person) && (occupants.size() < capacity)) {

            if (inside && !innerDoorLocked) {
                if (awaitingInnerDoor.contains(person)) {
                    awaitingInnerDoor.remove(person);
                    if (awaitingInnerDoor.contains(person)) {
                        throw new IllegalStateException(person + " still awaiting inner door!");
                    }
                }
                logger.finer(person.getName() + " enters through the inner door of the airlock at " + getEntityName());
                result = true;
            }
            else if (!inside && !outerDoorLocked) {
                if (awaitingOuterDoor.contains(person)) {
                    awaitingOuterDoor.remove(person);
                    if (awaitingOuterDoor.contains(person)) {
                        throw new IllegalStateException(person + " still awaiting outer door!");
                    }
                }
                logger.finer(person.getName() + " enters through the outer door of the airlock at " + getEntityName());
                result = true;
            }

            if (result) {
                occupants.add(person);
            }
        }

        return result;
    }

//    public boolean enterAirlock(Robot robot, boolean inside) {
//        boolean result = false;
//
//        if (!occupants.contains(robot) && (occupants.size() < capacity)) {
//
//            if (inside && !innerDoorLocked) {
//                if (awaitingInnerDoor.contains(robot)) {
//                    awaitingInnerDoor.remove(robot);
//                    if (awaitingInnerDoor.contains(robot)) {
//                        throw new IllegalStateException(robot + " still awaiting inner door!");
//                    }
//                }
//                logger.finer(robot.getName() + " enters through the inner door of the airlock at "+ getEntityName());
//                result = true;
//            }
//            else if (!inside && !outerDoorLocked) {
//                if (awaitingOuterDoor.contains(robot)) {
//                    awaitingOuterDoor.remove(robot);
//                    if (awaitingOuterDoor.contains(robot)) {
//                        throw new IllegalStateException(robot + " still awaiting outer door!");
//                    }
//                }
//                logger.finer(robot.getName() + " enters through the outer door of the airlock at " + getEntityName());
//                result = true;
//            }
//
//            if (result) {
//                occupants.add(robot);
//            }
//        }
//
//        return result;
//    }
    
//    /**
//     * Activates the airlock if it is not already activated.
//     * Automatically closes both doors and starts pressurizing/depressurizing.
//     * @param operator the person operating the airlock.
//     * @return true if airlock successfully activated.
//     */
//    public boolean activateAirlock(Unit operator) {
//    	Person p = null;
//    	Robot r = null;
//    	if (operator instanceof Person)
//    		p = (Person) operator;
//    	else
//    		r = (Robot) operator;
//    	
//        boolean result = false;
//
//        if (!activated) {
//            if (!innerDoorLocked) {
//                while ((occupants.size() < capacity) && (awaitingInnerDoor.size() > 0)) {
//
//
//					if (awaitingInnerDoor.get(0) instanceof Person) {
//						Person person = (Person) awaitingInnerDoor.get(0);
//
//		                    awaitingInnerDoor.remove(person);
//		                    if (awaitingInnerDoor.contains(person)) {
//		                        throw new IllegalStateException(person + " still awaiting inner door!");
//		                    }
//		                    if (!occupants.contains(person)) {
//		                        logger.finer(person.getName() + " enters through the inner door of the airlock at " + getEntityName());
//		                        occupants.add(person);
//		                    }
//
//					}
//					else if (awaitingInnerDoor.get(0) instanceof Robot) {
//						Robot robot = (Robot) awaitingInnerDoor.get(0);
//
//		                    awaitingInnerDoor.remove(robot);
//		                    if (awaitingInnerDoor.contains(robot)) {
//		                        throw new IllegalStateException(robot + " still awaiting inner door!");
//		                    }
//		                    if (!occupants.contains(robot)) {
//		                        logger.finer(robot.getName() + " enters through the inner door of the airlock at " + getEntityName());
//		                        occupants.add(robot);
//		                    }
//					}
//
//                }
//                innerDoorLocked = true;
//            }
//            else if (!outerDoorLocked) {
//                while ((occupants.size() < capacity) && (awaitingOuterDoor.size() > 0)) {
//
//    				if (awaitingOuterDoor.get(0) instanceof Person) {
//
//	                    Person person = (Person) awaitingOuterDoor.get(0);
//	                    awaitingOuterDoor.remove(person);
//	                    if (awaitingOuterDoor.contains(person)) {
//	                        throw new IllegalStateException(person + " still awaiting outer door!");
//	                    }
//	                    if (!occupants.contains(person)) {
//	                        logger.finer(person.getName() + " enters through the outer door of the airlock at " + getEntityName());
//	                        occupants.add(person);
//	                    }
//
//    				}
//					else if (awaitingOuterDoor.get(0) instanceof Robot) {
//
//						Robot robot = (Robot) awaitingOuterDoor.get(0);
//	                    awaitingOuterDoor.remove(robot);
//	                    if (awaitingOuterDoor.contains(robot)) {
//	                        throw new IllegalStateException(robot + " still awaiting outer door!");
//	                    }
//	                    if (!occupants.contains(robot)) {
//	                        logger.finer(robot.getName() + " enters through the outer door of the airlock at " + getEntityName());
//	                        occupants.add(robot);
//	                    }
//
//    				}
//
//                }
//                outerDoorLocked = true;
//                
//                //operator.getBuildingLocation().getThermalGeneration().getHeating().flagHeatDumpViaAirlockOuterDoor(false);
//            }
//            else {
//                return false;
//            }
//
//            activated = true;
//            remainingCycleTime = CYCLE_TIME;
//
//            if (PRESSURIZED.equals(state)) {
//                setState(DEPRESSURIZING);
//            }
//            else if (DEPRESSURIZED.equals(state)) {
//                setState(PRESSURIZING);
//            }
//            else {
//                logger.severe("Airlock in incorrect state for activation: " + state);
//                return false;
//            }
//
//            this.operator = operator;
//
//            result = true;
//        }
//
//        return result;
//    }
 
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

					Person person = (Person) awaitingInnerDoor.get(0);

                    awaitingInnerDoor.remove(person);
                    
                    if (awaitingInnerDoor.contains(person)) {
                        throw new IllegalStateException(person + " still awaiting inner door!");
                    }
                    
                    if (!occupants.contains(person)) {
                        logger.finer(person.getName() + " enters through the inner door of the airlock at " + getEntityName());
                        occupants.add(person);
                    }

                }
                innerDoorLocked = true;
            }
            else if (!outerDoorLocked) {
                while ((occupants.size() < capacity) && (awaitingOuterDoor.size() > 0)) {

                    Person person = (Person) awaitingOuterDoor.get(0);
                    
                    awaitingOuterDoor.remove(person);
                    
                    if (awaitingOuterDoor.contains(person)) {
                        throw new IllegalStateException(person + " still awaiting outer door!");
                    }
                    
                    if (!occupants.contains(person)) {
                        logger.finer(person.getName() + " enters through the outer door of the airlock at " + getEntityName());
                        occupants.add(person);
                    }


                }
                outerDoorLocked = true;
                
                //operator.getBuildingLocation().getThermalGeneration().getHeating().flagHeatDumpViaAirlockOuterDoor(false);
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
    
//    public boolean activateAirlock(Robot operator) {
//
//        boolean result = false;
//
//        if (!activated) {
//            if (!innerDoorLocked) {
//                while ((occupants.size() < capacity) && (awaitingInnerDoor.size() > 0)) {
//                	Robot robot = (Robot) awaitingInnerDoor.get(0);
//                    awaitingInnerDoor.remove(robot);
//                    if (awaitingInnerDoor.contains(robot)) {
//                        throw new IllegalStateException(robot + " still awaiting inner door!");
//                    }
//                    if (!occupants.contains(robot)) {
//                        logger.finer(robot.getName() + " enters inner door of " + getEntityName() + " airlock.");
//                        occupants.add(robot);
//                    }
//                }
//                innerDoorLocked = true;
//            }
//            else if (!outerDoorLocked) {
//                while ((occupants.size() < capacity) && (awaitingOuterDoor.size() > 0)) {
//                	Robot robot = (Robot) awaitingOuterDoor.get(0);
//                    awaitingOuterDoor.remove(robot);
//                    if (awaitingOuterDoor.contains(robot)) {
//                        throw new IllegalStateException(robot + " still awaiting outer door!");
//                    }
//                    if (!occupants.contains(robot)) {
//                        logger.finer(robot.getName() + " enters outer door of " + getEntityName() + " airlock.");
//                        occupants.add(robot);
//                    }
//                }
//                outerDoorLocked = true;
//            }
//            else {
//                return false;
//            }
//
//            activated = true;
//            remainingCycleTime = CYCLE_TIME;
//
//            if (PRESSURIZED.equals(state)) {
//                setState(DEPRESSURIZING);
//            }
//            else if (DEPRESSURIZED.equals(state)) {
//                setState(PRESSURIZING);
//            }
//            else {
//                logger.severe("Airlock in incorrect state for activation: " + state);
//                return false;
//            }
//
//            this.operator = operator;
//
//            result = true;
//        }
//
//        return result;
//    }


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

//            Iterator<Unit> i = occupants.iterator();
//                while (i.hasNext()) {
//                     Unit occupant = i.next();
//                     if (occupant instanceof Robot) {
//                    	 Robot robot = (Robot) occupant;
//                    	 logger.finer("Airlock has been " + getState() + ". " + robot.getName() + " at " + getEntity() + " is about to call exitAirlock()");
//                     }
//                     else if (occupant instanceof Person) {
//                    	 Person person = (Person) occupant;
//                     	 logger.finer("Airlock has been " + getState() + ". " + person.getName() + " at " + getEntity() + " is about to call exitAirlock()");
//                     }
//                
//                     exitAirlock(occupant);
//                }

            Iterator<Person> i = occupants.iterator();
            while (i.hasNext()) {
            	Person occupant = i.next();
                if (occupant instanceof Person) {
                	 Person person = (Person) occupant;
                 	 logger.finer("Airlock has been " + getState() + ". " + person.getName() + " at " + getEntity() + " is about to call exitAirlock()");
                 }
            
                 exitAirlock(occupant);
            }

                
            occupants.clear();

            operator = null;

            result = true;
        }

        return result;
    }

    /**
     * Causes a person within the airlock to exit either inside or outside.
     * @param occupant the person to exit.
     */
//    protected abstract void exitAirlock(Unit occupant);
    protected abstract void exitAirlock(Person occupant);

    //protected abstract void exitAirlock(Robot robot);

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
        logger.finer(getEntityName() + " airlock is " + state);
    }

    /**
     * Gets the airlock operator.
     * @return the airlock operator or null if none.
     */
//    public Unit getOperator() {
//        return operator;
//    }

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

//    /**
//     * Adds person to queue awaiting airlock by inner door.
//     * @param person the person to add to the awaiting queue.
//     */
//    public void addAwaitingAirlockInnerDoor(Unit unit) {
//        if (!awaitingInnerDoor.contains(unit)) {
//        	if (unit instanceof Robot) {
//        		logger.finer(((Robot)unit).getName() + " awaiting inner door of " + getEntityName() + " airlock.");
//        	}
//        	else
//        		logger.finer(((Person)unit).getName() + " awaiting inner door of " + getEntityName() + " airlock.");
//            awaitingInnerDoor.add(unit);
//        }
//    }

    /**
     * Adds person to queue awaiting airlock by inner door.
     * @param person the person to add to the awaiting queue.
     */
    public void addAwaitingAirlockInnerDoor(Person p) {
        if (!awaitingInnerDoor.contains(p)) {
        	logger.finer(p.getName() + " awaiting inner door of " + getEntityName() + " airlock.");
            awaitingInnerDoor.add(p);
        }
    }

    
    /**
     * Adds person to queue awaiting airlock by outer door.
     * @param person the person to add to the awaiting queue.
     */
    public void addAwaitingAirlockOuterDoor(Person person) {
        if (!awaitingOuterDoor.contains(person)) {
            logger.finer(person.getName() + " awaiting outer door of " + getEntityName() + " airlock.");
            awaitingOuterDoor.add(person);
        }
    }

//    public void addAwaitingAirlockOuterDoor(Robot robot) {
//        if (!awaitingOuterDoor.contains(robot)) {
//            logger.finer(robot.getName() + " awaiting outer door of " + getEntityName() + " airlock.");
//            awaitingOuterDoor.add(robot);
//        }
//    }
    
    /**
     * Time passing for airlock.
     * Check for unusual situations and deal with them.
     * Called from the unit owning the airlock.
     * @param time amount of time (in millisols)
     */
    public void timePassing(double time) {
//        Person person = null;
//        Robot robot = null;

        if (activated) {
            // Check if operator is dead.
            if (operator != null) {

//                boolean isDead = false;

            	boolean isDead = operator.getPhysicalCondition().isDead();
                
//                if (operator instanceof Person) {
//                 	person = (Person) operator;
//                 	isDead = person.getPhysicalCondition().isDead();
//
//                }
//                else if (operator instanceof Robot) {
//                	robot = (Robot) operator;
//                	isDead = robot.getSystemCondition().isInoperable();
//
//                }


                if (isDead) {
                    // If operator is dead, deactivate airlock.
                	String operatorName = operator.getName();
                    deactivateAirlock();
                    logger.severe("Airlock operator " + operatorName +
                            " is dead.  Deactivating airlock of " + getEntityName());
                }
                else {
                    // Check if airlock operator still has a task involving the airlock.
                    boolean hasAirlockTask = false;

//                    Task task = null;
//
//                    if (operator instanceof Person) {
//                     	person = (Person) operator;
//                     	 task = person.getMind().getTaskManager().getTask();
//
//                    }
//                    else if (operator instanceof Robot) {
//                    	robot = (Robot) operator;
//                    	 task = robot.getBotMind().getBotTaskManager().getTask();
//
//                    }
                    
                    Task task = operator.getMind().getTaskManager().getTask();

                    while (task != null) {
                        if ((task instanceof ExitAirlock) || (task instanceof EnterAirlock)) {
                            hasAirlockTask = true;
                        }
                        task = task.getSubTask();
                    }

                    if (!hasAirlockTask) {
                        String operatorName = operator.getName();
                        deactivateAirlock();
                        logger.severe("Airlock operator " + operatorName + " is no longer " +
                                "operating the airlock.  Deactivating airlock of " + getEntityName());
                    }
                }
            }
            else {
                // If no operator, deactivate airlock.
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
//    public boolean inAirlock(Unit unit) {
//        return occupants.contains(unit);
//    }
    public boolean inAirlock(Person p) {
        return occupants.contains(p);
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
     * @return name
     */
    public abstract String getEntityName();

    /**
     * Gets the inventory of the entity this airlock is attached to.
     * @return inventory
     */
    public abstract Inventory getEntityInventory();

    /**
     * Gets the entity this airlock is attached to.
     * @return entity.
     */
    public abstract Object getEntity();

    /**
     * Gets an available position inside the airlock entity.
     * @return available local position.
     */
    public abstract Point2D getAvailableInteriorPosition();

    /**
     * Gets an available position outside the airlock entity.
     * @return available local position.
     */
    public abstract Point2D getAvailableExteriorPosition();

    /**
     * Gets an available position inside the airlock.
     * @return available local position.
     */
    public abstract Point2D getAvailableAirlockPosition();

//    public Collection<Unit> getOccupants() {
//    	return occupants;
//    }
    
    public Collection<Person> getOccupants() {
    	return occupants;
    }
    
    
    public void addCheckEVASuit() {
    	numEVASuitChecks++;
    }
    
    public void resetCheckEVASuit() {
		numEVASuitChecks = 0;
    }
	
    public int getCheckEVASuit() {
    	return numEVASuitChecks;
    }
		
		
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
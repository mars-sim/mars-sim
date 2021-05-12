/**
 * Mars Simulation Project
 * Airlock.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.mars.MarsSurface;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.BuildingAirlock;

// see discussions on Airlocks for Mars Colony at 
// https://forum.nasaspaceflight.com/index.php?topic=42098.0

// Astronauts aboard the International Space Station preparing for extra-vehicular activity (EVA) 
// "camp out" at low atmospheric pressure, 10.2 psi (0.70 bar), spending eight sleeping hours 
// in the Quest airlock chamber before their spacewalk. During the EVA they breathe 100% oxygen 
// in their spacesuits, which operate at 4.3 psi (0.30 bar),[71] although research has examined 
// the possibility of using 100% O2 at 9.5 psi (0.66 bar) in the suits to lessen the pressure 
// reduction, and hence the risk of DCS.[72]
//
// see https://en.wikipedia.org/wiki/Decompression_sickness

/**
 * The Airlock class represents an airlock to a vehicle or structure.
 */
public abstract class Airlock implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(Airlock.class.getName());

	/** Pressurize/depressurize time (millisols). */
	public static final double CYCLE_TIME = 10D; // TODO: should we add pre-breathing time into CYCLE_TIME ?

	/** The maximum number of space outside the inner and outer door. */
	public static final int MAX_SLOTS = 4;
	
	/** 
	 * Airlock State goes in only one direction. After the pressurizing state, 
	 * it will return back to the pressurized state. 
	 */
	public enum AirlockState {
		PRESSURIZED, DEPRESSURIZING, DEPRESSURIZED, PRESSURIZING 
	}

	public AirlockState airlockState; 

	// Data members
	/** True if airlock's state is locked. */
//	private boolean stateLocked;
	/** True if airlock is activated (may elect an operator or may change the airlock state). */
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
    private Set<Integer> occupantIDs;

	/** Current pool of operator candidates in airlock building. */
    private Set<Integer> operatorPool;
    
	/** The person currently operating the airlock. */
    private Integer operatorID;
   
	/** People waiting for the airlock by the inner door. */
    private Set<Integer> awaitingInnerDoor;

	/** People waiting for the airlock by the outer door. */
    private Set<Integer> awaitingOuterDoor;

	/** The lookup map for occupants. */
	private transient Map<Integer, Person> lookupPerson;
	
    protected static UnitManager unitManager; 
    protected static MarsSurface marsSurface;
    
	/**
	 * Constructs an airlock object for a unit.
	 * 
	 * @param capacity number of people airlock can hold.
	 * @throws IllegalArgumentException if capacity is less than one.
	 */
	public Airlock(int capacity) throws IllegalArgumentException {

		// Initialize data members
		if (capacity < 1)
			throw new IllegalArgumentException("capacity less than one.");
		else
			this.capacity = capacity;

		activated = false;
		airlockState = AirlockState.PRESSURIZED;
		innerDoorLocked = false;
		outerDoorLocked = true;
		remainingCycleTime = CYCLE_TIME;
		
		operatorID = Integer.valueOf(-1);
		
		lookupPerson = new ConcurrentHashMap<>();
		occupantIDs = new HashSet<>();
		awaitingInnerDoor = new HashSet<>(MAX_SLOTS);
		awaitingOuterDoor = new HashSet<>(MAX_SLOTS);
		operatorPool = new HashSet<>();
		
//		if (unit instanceof Building) {
//			locale = ((Building)unit).getLocale();
//		}
//		
//		else if (unit instanceof Vehicle) {
//			locale = ((Vehicle)unit).getLocale();
//		}
	}
	
	/**
	 * Exits the airlock from either the inside or the outside. Inner
	 * or outer door (respectively) must be unlocked for person to enter.
	 * 
	 * @param person {@link Person} the person to enter the airlock
	 * @param egress {@link boolean} <code>true</code> if person is egressing<br/>
	 *               <code>false</code> if person is ingressing
	 * @return {@link boolean} <code>true</code> if person exits the airlock
	 *         successfully
	 */
	public boolean exitAirlock(Person person, Integer id, boolean egress) {
		boolean result = false;

		// Transfer from door queue into the airlock occupantIDs set
		if (occupantIDs.contains(id)) {
			result = transferOut(id);
		}
		
		if (egress && result) {
			// Transfer the person from one container unit to another
			result = egress(person);
		}
		
		return result;
	}
	
	/**
	 * Enters a person into the airlock from either the inside or the outside. Inner
	 * or outer door (respectively) must be unlocked for person to enter.
	 * 
	 * @param person {@link Person} the person to enter the airlock
	 * @param egress {@link boolean} <code>true</code> if person is egressing<br/>
	 *               <code>false</code> if person is ingressing
	 * @return {@link boolean} <code>true</code> if person entered the airlock
	 *         successfully
	 */
	public boolean enterAirlock(Person person, Integer id, boolean egress) {
		boolean result = false;

		// Warning : do NOT use int id or else the list's method remove(int index) would be chosen to use
		// List can't tell if the method remove(Object o) should be used.

		// Add the person's ID to the lookup map
		addPersonID(person, id);

		// If the airlock is not full
		if (!occupantIDs.contains(id) && hasSpace()) {
			
			result = transferIn(person, id, egress);
		}
		
		if (result && !egress) {
			// Transfer the person from one container unit to another
			result = ingress(person);
		}
		
		
		return result;
	}
	

	/**
	 * Transfer a person into the airlock chamber
	 * 
	 * @param person {@link Person} the person to enter the airlock
	 * @param id the person's id
	 * @param inside {@link boolean} <br/> <code>true</code> if person is entering from
	 *               inside<br/>
	 *               <code>false</code> if person is entering from outside
	 * @return {@link boolean} <code>true</code> if person entered the airlock
	 *         successfully
	 */
	private boolean transferIn(Person person, Integer id, boolean inside) {
		boolean result = false;
		// Transfer the person inside the chamber

		// Transfer the person into the chamber from the inner door queue
		if (inside && !innerDoorLocked) {
			if (awaitingInnerDoor.contains(id)) {
				awaitingInnerDoor.remove(id);
				
				if (awaitingInnerDoor.contains(id)) {
					throw new IllegalStateException(person + " was still waiting inner door.");
				}
			}
			logger.log(person, Level.INFO, 0,
					"Transferred in through the inner door of " + getEntityName() + ".");
			result = true;
		} 
		
		// Transfer the person into the chamber via the outer door
		if (!inside && !outerDoorLocked) {
			if (awaitingOuterDoor.contains(id)) {
				awaitingOuterDoor.remove(id);
				
				if (awaitingOuterDoor.contains(id)) {
					throw new IllegalStateException(person + " was still awaiting outer door!");
				}
			}
			logger.log(person, Level.INFO, 0,
					"Transferred out through the outer door of " + getEntityName() + ".");
			result = true;
		}

		if (result) {
			// Add the person's ID to the occupant ID list
			occupantIDs.add(id);
		}
			
		return result;
	}
	
	/**
	 * Transfer a person out of the airlock chamber
	 * 
	 * @param id the person's id
	 * @return {@link boolean} <code>true</code> if person exiting the airlock
	 *         successfully
	 */
	public boolean transferOut(Integer id) {
		operatorPool.remove(id);
		
		if (operatorID.equals(id)) {;
			operatorID = Integer.valueOf(-1);
		}
		
		return occupantIDs.remove(id);
	}
	
	/**
	 * Remove the id of a person 
	 * 
	 * @param id the person's id
	 */
	public void removeID(Integer id) {
		operatorPool.remove(id);
		
		if (operatorID.equals(id)) {;
			operatorID = Integer.valueOf(-1);
		}

		occupantIDs.remove(id);
		awaitingInnerDoor.remove(id);
		awaitingOuterDoor.remove(id);		
	}
	
	
//	/**
//	 * Activates the airlock if it is not already activated. Automatically closes
//	 * both doors and starts pressurizing/depressurizing.
//	 * 
//	 * @param operator the person operating the airlock.
//	 * @return true if airlock successfully activated.
//	 */
//	public boolean activateAirlock(Person p) {
////		LogConsolidated.log(logger, Level.FINE, 0, sourceName, "[" + getLocale() + "] "
////				+ getEntity() + " was being activated.");
//		
//		boolean result = false;
//
//		// Add the person's ID to the lookup map 
//		addPersonID(p);
//
//		if (switch2UnsteadyState()) {
//			result = true;
//			remainingCycleTime = CYCLE_TIME;
//			activated = true;
//		}
//		else 
//			result = false;
//		
//		if (!activated) {
//			result = transferAll();
//		}
//		
//		// if no operator assigned and there are occupants inside the airlock chamber, elect a new operator
//		if (occupantIDs.size() > 0 && operatorID == -1)
//			electAnOperator();
//		
//		return result;
//	}
//
//	private boolean transferAll() {
//		boolean result = true;
//		
//		// Transfer of people 
//
//		if (!innerDoorLocked) {
//			// Transfer those waiting at the inner door into the airlock chamber
//			while ((occupantIDs.size() < capacity) && (awaitingInnerDoor.size() > 0)) {
//
//				// Grab the first one
//				Integer id = new ArrayList<>(awaitingInnerDoor).get(0);
//				Person person = getPersonByID(id);
//			
//		    	if (person == null) {
//		    		person = unitManager.getPersonByID(id);
//		    		lookupPerson.put(id, person);
//		    	}
//				
//				awaitingInnerDoor.remove(id);
//
//				if (awaitingInnerDoor.contains(id)) {
//					throw new IllegalStateException(person + " was still awaiting inner door!");
//				}
//
//				if (!occupantIDs.contains(id)) {
//					LogConsolidated.log(logger, Level.FINER, 0, sourceName,
//							"[" + person.getLocale() + "] " 
//								+ person.getName() + " entered through the inner door of the airlock at "
//								+ getEntityName());
//					occupantIDs.add(id);
//				}
//			}
//			innerDoorLocked = true;
//		} 
//		
//		else if (!outerDoorLocked) {
//			// Transfer those waiting at the outer door into the airlock chamber
//			while ((occupantIDs.size() < capacity) && (awaitingOuterDoor.size() > 0)) {
//				
//				// Grab the first one
//				Integer id = new ArrayList<>(awaitingOuterDoor).get(0);
//				Person person = getPersonByID(id);
//				
//		    	if (person == null) {
//		    		person = unitManager.getPersonByID(id);
//		    		lookupPerson.put(id, person);
//		    	}
//				
//		    	// transfer the person waiting at outer door to inside the chamber
//				awaitingOuterDoor.remove(id);
//
//				if (awaitingOuterDoor.contains(id)) {
//					throw new IllegalStateException(person + " still awaiting outer door!");
//				}
//
//				if (!occupantIDs.contains(id)) {
//					LogConsolidated.log(logger, Level.FINER, 0, sourceName,
//							"[" + person.getLocale() + "] " 
//							+ person.getName() + " entered through the outer door at "
//							+ getEntityName());
//					occupantIDs.add(id);
//				}
//			}
//			outerDoorLocked = true;
//
//			// TODO: consider dumping or extracting heat 
//			// operator.getBuildingLocation().getThermalGeneration().getHeating().flagHeatDumpViaAirlockOuterDoor(false);
//		}
//		
//		else {
//			// Not the right time to start the transfer
//			return false;
//		}
//
//		return result;
//	}
	
//	/**
//	 * Switch the state from one unsteady state
//	 */
//	private boolean switch2UnsteadyState() {
//		if (AirlockState.PRESSURIZED == airlockState) {
//			setState(AirlockState.DEPRESSURIZING);	
//			return true;
//		} 
//		
//		if (AirlockState.DEPRESSURIZED == airlockState) {
//			setState(AirlockState.PRESSURIZING);
//			return true;
//		} 
//
////			LogConsolidated.log(logger, Level.SEVERE, 5_000, sourceName,
////				"[" + p.getLocale() + "] " 
////					+ p.getName() + " reported the airlock was having incorrect state for activation: '" + airlockState + "'.");
//		
//		return false;
//	}
	
	/**
	 * Checks if the chamber is pressurizing.
	 * 
	 * @return true if it's pressurizing
	 */
	public boolean isPressurizing() {
		if (AirlockState.PRESSURIZING == airlockState) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Checks if the chamber is depressurizing.
	 * 
	 * @return true if it's depressurizing
	 */
	public boolean isDepressurizing() {
		if (AirlockState.DEPRESSURIZING == airlockState) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Sets to the pressurizing state
	 * 
	 * @return
	 */
	public boolean setPressurizing() {
		if (AirlockState.DEPRESSURIZED == airlockState) {
			setState(AirlockState.PRESSURIZING);
			innerDoorLocked = true;
			outerDoorLocked = true;
			return true;
		}
			
		return false;
	}

	/**
	 * Sets to the depressurizing state
	 * 
	 * @return
	 */
	public boolean setDepressurizing() {
		if (AirlockState.PRESSURIZED == airlockState) {
			setState(AirlockState.DEPRESSURIZING);
			innerDoorLocked = true;
			outerDoorLocked = true;
			return true;
		}
		
		return false;
	}
	
	/**
	 * Checks if the airlock has been depressurized
	 * 
	 * @return
	 */
	public boolean isDepressurized() {
		if (AirlockState.DEPRESSURIZED == airlockState) {
			return true;
		}
		return false;
	}
	
	/**
	 * Checks if the airlock has been pressurized
	 * 
	 * @return
	 */
	public boolean isPressurized() {	
		if (AirlockState.PRESSURIZED == airlockState) {
			return true;
		}	
		return false;
	}
	
	
	/**
	 * Go to the next steady state
	 * 
	 * @return true if the switch is successful
	 */
	public boolean goToNextSteadyState() {
		if (AirlockState.PRESSURIZING == airlockState) {
			setState(AirlockState.PRESSURIZED);
			activated = false;
			innerDoorLocked = false;
			outerDoorLocked = true;
			return true;
		}
		
		else if (AirlockState.DEPRESSURIZING == airlockState) {
			setState(AirlockState.DEPRESSURIZED);
			activated = false;
			innerDoorLocked = true;
			outerDoorLocked = false;
			return true;
		}
		
		return false;
	}

	/**
	 * Add airlock cycle time.
	 * 
	 * @param time cycle time (millisols)
	 * @return The the time consumed
	 */
	public double addTime(double time) {
		double consumed = 0D;

		if (activated) {
			// Cannot consume more than is needed
			consumed = Math.min(remainingCycleTime, time);
			
			remainingCycleTime -= consumed;
			// if the air cycling has been completed
			if (remainingCycleTime <= 0D) {
				// Reset remainingCycleTime back to max
				remainingCycleTime = CYCLE_TIME;
				// Go to the next steady state
				goToNextSteadyState();
			} 
		}

		return consumed;
	}
	
	public void setActivated(boolean value) {
		if (!value) {
			// Reset the cycle count down timer back to the default
			remainingCycleTime = CYCLE_TIME;
		}
		activated = value;
	}
	
	
	/**
	 * Is this person the airlock operator ?
	 * 
	 * @param p
	 * @return true if this person is the airlock operator 
	 */
	public boolean isOperator(int id) {
		if (operatorID.equals(Integer.valueOf(id)))
			return true;
		return false;
	}

	/**
	 * Gets the operator's Person instance
	 * 
	 * @return
	 */
	public String getOperatorName() {
		if (operatorID.equals(Integer.valueOf(-1)))
			return "None";
		else {
			Person p = null;
			if (lookupPerson != null)
				p = lookupPerson.get(operatorID);
			if (p != null)
				return p.getName();
			else {
				p = unitManager.getPersonByID(operatorID);
				if (p != null) {
					return p.getName();
				}
			}
		}
		
		return "N/A";
	}
	
//	/**
//	 * Volunteer this person as the operator
//	 * 
//	 * @param p
//	 */
//	public void volunteerAsOperator(Person p) {
//		int id = p.getIdentifier();
//		
//		// Ensure that the person is in the lookup map
//		addPersonID(p);	
//		
//		if (id > 0 && !occupantIDs.contains(id))
//			occupantIDs.add(id);
//		
//		if (operatorID != id) {
//			operatorID = id;
//			LogConsolidated.log(logger, Level.FINER, 4_000, sourceName, "[" + p.getLocale() + "] "
//					+ p + " stepped up becoming the operator of the airlock.");
//		}
//	}

	/**
	 * Checks on the current pool of candidates for being an operator
	 */
	private void checkOperatorPool() {
		operatorPool = new HashSet<>();
		operatorPool.addAll(occupantIDs);		
		operatorPool.addAll(awaitingInnerDoor);
		operatorPool.addAll(awaitingOuterDoor);	
	}
	
	/**
	 * Elects an operator with the best EVA skill level/experiences
	 */
	private void electAnOperator() {
		Set<Integer> pool = null;
		
		if (occupantIDs.isEmpty()) {
			
			if (!awaitingOuterDoor.isEmpty()) {
				// Note: the second preference is given to those waiting at the outer door
				pool = awaitingOuterDoor;
			}
			else
				// Note: the third preference is given to those waiting at the inner door
				pool = awaitingInnerDoor;
		}
		else
			// Note: the first preference is given to those inside the chambers
			pool = occupantIDs;
		
		// Select a person to become the operator
		Person selected = null;
		Integer selectedID = Integer.valueOf(-1);
		
		int size = pool.size();

		if (size == 1) {
			List<Integer> list = new ArrayList<>(pool);
			int id = list.get(0);
			operatorID = Integer.valueOf(id);
			selected = getPersonByID(id);
//			LogConsolidated.log(logger, Level.INFO, 4_000, sourceName, "[" + selected.getLocale() + "] "
//					+ selected + " acted as the airlock operator in " 
//					+ selected.getImmediateLocation() + ".");
		}
		
		else if (size > 1) {
			int evaExp = -1;
			int evaLevel = -1;
			for (Integer id : pool) {
				Person p = 	getPersonByID(id);
		    	if (p == null) {
		    		p = unitManager.getPersonByID(id);
		    		addPersonID(p, id);
		    	}
				int level = p.getSkillManager().getSkillLevel(SkillType.EVA_OPERATIONS);
				if (level > evaLevel) {
					selected = p;
					selectedID = id;
				}
	
				else if (level == evaExp) {
					int exp = p.getSkillManager().getSkillExp(SkillType.EVA_OPERATIONS);
					if (exp > evaExp) {
						selected = p;
						selectedID = id;
					}
				}
			}
			
			operatorID = Integer.valueOf(selectedID);
			
			logger.log(selected, Level.FINE, 4000,
					"Stepped up becoming the airlock operator.");
		}
	}
	
//	/**
//	 * Set new air pressure state, and unlock/open the (inner/outer) door. 
//	 * Any people inside the airlock proceed to leave the airlock
//	 * 
//	 * @return true if airlock was deactivated successfully.
//	 */
//	public boolean deactivateAirlock() {
////		LogConsolidated.log(logger, Level.FINE, 4000, sourceName, "[" + getLocale() + "] "
////				+ getEntity() + " was being deactivated.");
//		
//		boolean result = false;
//
//		if (activated) {
//			activated = false;
//
//			if (AirlockState.DEPRESSURIZING == airlockState) {
//				setState(AirlockState.DEPRESSURIZED);
//				outerDoorLocked = false;
//			} else if (AirlockState.PRESSURIZING == airlockState) {
//				setState(AirlockState.PRESSURIZED);
//				innerDoorLocked = false;
//			} else {
//				return false;
//			}
//
//			// Occupants are to leave the airlock one by one
//			// Note: it's critical that leaveAirlock() is called so that a person can 
//			// have a location state change.
//			boolean successful = leaveAirlock();
//			if (successful) {
//				occupantIDs.clear();
//				operatorID = Integer.valueOf(-1);
//				result = true;
//				remainingCycleTime = CYCLE_TIME;
//			}
//			else {
//				result = false;
//			}
//		}
//
//		return result;
//	}
	
//	/**
//	 * Iterates through each occupant to exit the airlock
//	 * 
//	 * @param true if the exit is successful
//	 */
//	public boolean leaveAirlock() {
////		LogConsolidated.log(logger, Level.FINE, 4000, sourceName, "[" + getLocale() + "] "
////				+ getEntity() + " called leaveAirlock().");
//		boolean successful = true;
//		
//		Iterator<Integer> i = occupantIDs.iterator();
//		while (i.hasNext()) {
//			Integer id = i.next();
//			Person p = getPersonByID(id);
//	    	if (p == null) {
//	    		p = unitManager.getPersonByID(id);
//	    		// Add the person into the lookup map
//	    		// Note: this is needed for reconstructing the lookup map after loading from the sim.
//	    		lookupPerson.put(id, p);
//	    	}
//	    	
////			LogConsolidated.log(logger, Level.FINE, 4000, sourceName,
////					"[" + p.getLocale() + "] " + p.getName()
////					+ " reported that " + getEntity() + " had been " 
////					+ getState().toString().toLowerCase() + ".");
//			
//			// Call exitAirlock() in BuildingAirlock or VehicleAirlock
//			successful = successful && egress(p);
//		}
//		
//		return successful;
//	}
//	
	/**
	 * Causes a person to do an EVA egress.
	 * 
	 * @param occupant the person exiting the settlement .
	 */
	protected abstract boolean egress(Person occupant);

	/**
	 * Causes a person to do an EVA ingress.
	 * 
	 * @param occupant the person entering the settlement .
	 */
	protected abstract boolean ingress(Person occupant);
	
	/**
	 * Checks if the airlock's outer door is locked.
	 * 
	 * @return true if outer door is locked
	 */
	public boolean isOuterDoorLocked() {
		return outerDoorLocked;
	}


	
	/**
	 * Checks if the airlock's inner door is locked.
	 * 
	 * @return true if inner door is locked
	 */
	public boolean isInnerDoorLocked() {
		return innerDoorLocked;
	}

	/**
	 * Sets the airlock's inner door locked to true or false
	 * 
	 * @param lock 
	 */
	public void setInnerDoorLocked(boolean lock) {
		innerDoorLocked = lock;
	}
	
	/**
	 * Sets the airlock's outer door locked to true or false
	 * 
	 * @param lock 
	 */
	public void setOuterDoorLocked(boolean lock) {
		outerDoorLocked = lock;
	}
	
	
	/**
	 * Checks if the airlock is currently activated.
	 * 
	 * @return true if activated.
	 */
	public boolean isActivated() {
		return activated;
	}

	/**
	 * Gets the current state of the airlock.
	 * 
	 * @return the state string.
	 */
	public AirlockState getState() {
		return airlockState;
	}

	/**
	 * Sets the state of the airlock.
	 * 
	 * @param state the airlock state.
	 */
	private void setState(AirlockState state) {
		airlockState = state;
//		logger.log(getEntityName(), Level.FINE, 0, "Set to " + state);
	}

	/**
	 * Gets the operator's Person instance
	 * 
	 * @return
	 */
//	public Person getOperator() {
//		return getPersonByID(operatorID);
//	}

//	/**
//	 * Clears the person airlock operator.
//	 */
//	public void clearOperator(Integer id) {
////		operatorID = Integer.valueOf(-1);
//		transferOut(id);
//	}

	/**
	 * Gets the remaining airlock cycle time.
	 * 
	 * @return time (millisols)
	 */
	public double getRemainingCycleTime() {
		return remainingCycleTime;
	}

	/**
	 * Adds person to queue awaiting airlock by inner door.
	 * 
	 * @param person the person to add to the awaiting queue.
	 * @return true if the person can be added or is already in the queue
	 */
	public boolean addAwaitingInnerDoor(Person p, Integer id) {
		// Add the person's ID to the lookup map	
		addPersonID(p, id);
		
		return addToZone(awaitingInnerDoor, id);
	}

	/**
	 * Adds person to queue awaiting airlock by outer door.
	 * 
	 * @param person the person to add to the awaiting queue.
	 * @return true if the person can be added or is already in the queue
	 */
	public boolean addAwaitingOuterDoor(Person p, Integer id) {		
		// Add the person's ID to the lookup map
		addPersonID(p, id);
		
		return addToZone(awaitingOuterDoor, id);
	}
	
	/**
	 * Adds this unit to the set
	 * 
	 * @param set
	 * @param id
	 * @return true if the unit is already inside the set or if the unit can be added into the set
	 */
	private boolean addToZone(Set<Integer> set, Integer id) {
		if (set.contains(id)) {
			return true;
		}
		else {
			// MAX_SLOTS - 1 because it needs to have one vacant spot
			// for the flow of traffic
			if (set.size() < MAX_SLOTS - 1) {
				set.add(id);
				return true;
			}
		}
		return false;
	}
	
	
	
	public int getNumAwaitingInnerDoor() {
		return awaitingInnerDoor.size();
	}
	
	/**
	 * Gets the number of people waiting at the outer door
	 * 
	 * @return
	 */
	public int getNumAwaitingOuterDoor() {
		return awaitingOuterDoor.size();
	}
	

	/**
	 * Checks if anyone is waiting at the outer door
	 * 
	 * @return
	 */
	public boolean hasAwaitingOuterDoor() {
		if (awaitingOuterDoor.isEmpty())
			return false;
		
		return true;
	}
	
	/**
	 * Checks if anyone is waiting at the inner door
	 * 
	 * @return
	 */
	public boolean hasAwaitingInnerDoor() {
		if (awaitingInnerDoor.isEmpty())
			return false;
		
		return true;
	}
	
	/**
	 * Time passing for airlock. Check for unusual situations and deal with them.
	 * Called from the unit owning the airlock.
	 * 
	 * @param time amount of time (in millisols)
	 */
	public void timePassing(double time) {
		
		if (activated) {
			
			if (!occupantIDs.isEmpty() || !awaitingInnerDoor.isEmpty() || !awaitingOuterDoor.isEmpty()) {
				// Create a new set of candidates
				checkOperatorPool();
				
				if (!operatorPool.contains(operatorID) || operatorID.equals(Integer.valueOf(-1))) {					
					// If no operator has been elected
					electAnOperator();
				}
				else if (!occupantIDs.isEmpty() && !occupantIDs.contains(operatorID)) {
					// Need to give the preference to those inside the chamber
					electAnOperator();
				}
			}
		}
	}

	/**
	 * Remove the record of the deceased person from the map and sets
	 * 
	 * @param person
	 */
	public void removeAirlockRecord(Person person) {
		int id = person.getIdentifier();
		boolean isDead = person.getPhysicalCondition().isDead();
		// Check if operator is dead.
		if (isDead) {
			occupantIDs.remove(id);
			awaitingInnerDoor.remove(id);
			awaitingOuterDoor.remove(id);
		}
	}
	
	/**
	 * Checks if given person is currently in the airlock.
	 * 
	 * @param person to be checked
	 * @return true if person is in airlock
	 */
	public boolean inAirlock(Person p) {
		return occupantIDs.contains((Integer)p.getIdentifier());
	}

	/**
	 * Gets the airlock capacity.
	 * 
	 * @return capacity.
	 */
	public int getCapacity() {
		return capacity;
	}

	/**
	 * Gets the name of the entity this airlock is attached to.
	 * Note: it prints the EVA building name and the settlement name
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
	 * 
	 * @return entity.
	 */
	public abstract Object getEntity();

	/**
	 * Gets the entity this airlock is attached to.
	 * 
	 * @return entity.
	 */
	public abstract String getLocale();
	
	/**
	 * Gets an available position inside the airlock entity.
	 * 
	 * @param inside true if the position is inside of the interior door
	 * @return available local position.
	 */
	public abstract Point2D getAvailableInteriorPosition(boolean inside);
	
	/**
	 * Gets an available position inside the airlock entity.
	 * 
	 * @return available local position.
	 */
	public abstract Point2D getAvailableInteriorPosition();
	
	/**
	 * Gets an available position outside the airlock entity.
	 * 
	 * @param inside true if the position is inside of the exterior door
	 * @return available local position.
	 */
	public abstract Point2D getAvailableExteriorPosition(boolean inside);

	/**
	 * Gets an available position outside the airlock entity.
	 * 
	 * @return available local position.
	 */
	public abstract Point2D getAvailableExteriorPosition();
	
	/**
	 * Gets an available airlock position
	 * 
	 * @return available airlock position.
	 */
	public abstract Point2D getAvailableAirlockPosition();

    public boolean occupy(int zone, Point2D p, Integer id) {
    	return ((BuildingAirlock)this).occupy(zone, p, id);
    }
  
	public boolean vacate(int zone, Integer id) {
		return ((BuildingAirlock)this).vacate(zone, id);
	}
	
	public boolean isInZone(Person p, int zone) {
		return ((BuildingAirlock)this).isInZone(p, zone);
	}
	
	public void loadEVAActivitySpots() {
		((BuildingAirlock)this).loadEVAActivitySpots();
	}
	
	/**
	 * Gets a collection of occupants' ids
	 * 
	 * @return
	 */
	public Set<Integer> getOccupants() {
		return occupantIDs;
	}
	
	/**
	 * Gets a collection of occupants' ids
	 * 
	 * @return
	 */
	public List<Integer> getAllInsideOccupants() {
		return ((BuildingAirlock)this).getAllInsideOccupants();
	}

	/**
	 * Checks if any occupants wear no EVA Suit
	 * 
	 * @return
	 */
	public boolean someoneHasNoEVASuit() {
		for (Integer id: occupantIDs) {
			Person p = this.getPersonByID(id);
			if (p.getSuit() == null)
				return true;
		}
		return false;
	}
	
	/**
	 * Gets a list of those having no EVA suit worn
	 * 
	 * @return
	 */
	public List<Person> noEVASuit() {
		List<Person> list = new ArrayList<>();
		List<Integer> intList = getAllInsideOccupants();
		for (Integer id: intList) {
			Person p = getPersonByID(id);
			if (p.getSuit() == null)
				list.add(p);
		}
		return list;
	}
	
	/**
	 * Gets the number of occupants currently inside the airlock
	 * 
	 * @return
	 */
	public int getNumOccupants() {
		int numWaiting = 0; //occupantIDs.size();
		if (getEntity() instanceof Building) {
			numWaiting = ((BuildingAirlock)this).getInsideTotalNum();
		}
//		else if (getEntity() instanceof Vehicle) {
//			;
//		}
		return numWaiting;
	}
	
	/**
	 * Checks if there is no occupants inside the airlock
	 * 
	 * @return true if the airlock is empty
	 */
	public boolean isEmpty() {
		return occupantIDs.isEmpty() && getNumOccupants() == 0;
	}
	
	/**
	 * Checks if there is an empty slot left in Zone 1, 2 and 3
	 * 
	 * @return true if there is space
	 */
	public boolean hasSpace() {
		if (getNumOccupants() < capacity)
			return true;
		
		return false;
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
	 * Gets a person's ID
	 * 
	 * @param id
	 * @return
	 */
	public Person getPersonByID(Integer id) {
//		System.out.print("id is " + id);
//		System.out.print("    lookupPerson is " + lookupPerson);
//		System.out.println("    lookupPerson.get(id) is " + lookupPerson.get(id));
//		logger.config("lookupPerson's size is " + lookupPerson.size());
		if (lookupPerson == null)
			lookupPerson = new ConcurrentHashMap<>();
		if (lookupPerson.get(id) != null)
			return lookupPerson.get(id);
		else {
			Person p = unitManager.getPersonByID(id);
			if (p != null) {
				return p;
			}
		}
		return null;
	}

//	/**
//	 * Add a person's ID to the lookup map for person inside the airlock
//	 * 	
//	 * @param p
//	 */
//	public void addPersonID(Person p) {
//		if (lookupPerson == null)
//			lookupPerson = new HashMap<>();
//		if (p != null && !lookupPerson.containsKey(p.getIdentifier()))
//			lookupPerson.put(p.getIdentifier(), p);
//	}
	
	/**
	 * Add a person's ID to the lookup map for person inside the airlock
	 * 	
	 * @param p
	 */
	public void addPersonID(Person p, Integer id) {
		if (lookupPerson == null)
			lookupPerson = new ConcurrentHashMap<>();
		if (p != null && !lookupPerson.containsKey(id))
			lookupPerson.put(id, p);
	}
	
	/**
	 * Initializes instances
	 * 
	 * @param um {@link UnitManager}
	 * @param ms {@link MarsSurface}
	 */
	public static void initializeInstances(UnitManager um, MarsSurface ms) {
		unitManager = um;
		marsSurface = ms;
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		occupantIDs.clear();
		occupantIDs = null;
		awaitingInnerDoor.clear();
		awaitingInnerDoor = null;
		awaitingOuterDoor.clear();
		awaitingOuterDoor = null;
		airlockState = null;
		unitManager = null;
	    marsSurface = null;
	}
}

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.mars.MarsSurface;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.EnterAirlock;
import org.mars_sim.msp.core.person.ai.task.ExitAirlock;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
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
	private static Logger logger = Logger.getLogger(Airlock.class.getName());

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());

	/** Pressurize/depressurize time (millisols). */
	public static final double CYCLE_TIME = 10D; // TODO: should we add pre-breathing time into CYCLE_TIME ?

	/** The maximum number of space outside the inner and outer door. */
	public static final double MAX_SLOTS = 4;
	
	public enum AirlockState {
		PRESSURIZED, DEPRESSURIZED, PRESSURIZING, DEPRESSURIZING
	}

	public AirlockState airlockState; 

	// Data members
	/** True if airlock's state is locked. */
//	private boolean stateLocked;
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
    private volatile Set<Integer> occupantIDs;
//	private Collection<Person> occupants;

	/** The person currently operating the airlock. */
    private volatile Integer operatorID;
//	private Person operator;
    
	/** People waiting for the airlock by the inner door. */
    private volatile Set<Integer> awaitingInnerDoor;
//	private List<Person> awaitingInnerDoor;

	/** People waiting for the airlock by the outer door. */
    private volatile Set<Integer> awaitingOuterDoor;

	/** The lookup map for settlers. */
	private volatile Map<Integer, Person> lookupPerson;
	
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
		remainingCycleTime = 0D;
		
		operatorID = Integer.valueOf(-1);
		
		lookupPerson = new ConcurrentHashMap<>();
		occupantIDs = new HashSet<>();
		awaitingInnerDoor = new HashSet<>();
		awaitingOuterDoor = new HashSet<>();
		
//		if (unit instanceof Building) {
//			locale = ((Building)unit).getLocale();
//		}
//		
//		else if (unit instanceof Vehicle) {
//			locale = ((Vehicle)unit).getLocale();
//		}
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
	public boolean exitAirlock(Person person, Integer id, boolean egress) {
		boolean result = true;

		// Warning : do NOT use int id or else the list's method remove(int index) would be chosen to use
		// List can't tell if the method remove(Object o) should be used.
//		Integer id = person.getIdentifier();
		// Add the person's ID to the lookup map
//		addPersonID(person, id);

		// Transfer from door queue into the airlock occupantIDs set
		if (occupantIDs.contains(id)) {
			result = transferOut(id);
		}
		
		if (egress && result) {
			// Transfer the person from one container unit to another
			result = result && egress(person);
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
//		Integer id = person.getIdentifier();
		// Add the person's ID to the lookup map
		addPersonID(person, id);

		// Transfer from outer door queue into the airlock occupantIDs set
		if (!occupantIDs.contains(id) && (occupantIDs.size() < capacity)) {
			result = transferIn(person, id, egress);
		}
		
		if (!egress && result) {
			// Transfer the person from one container unit to another
			result = result && ingress(person);
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
			LogConsolidated.log(logger, Level.INFO, 0, sourceName,
					"[" + person.getLocale() + "] " 
						+ person.getName() + " entered through the inner door of " + getEntityName());
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
			LogConsolidated.log(logger, Level.INFO, 0, sourceName,
					"[" + person.getLocale() + "] " 
						+ person.getName() + " entered through the outer door of " + getEntityName());
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
	private boolean transferOut(Integer id) {
		return occupantIDs.remove(id);
	}
	
	
//	/**
//	 * Activates the airlock if it is not already activated. Automatically closes
//	 * both doors and starts pressurizing/depressurizing.
//	 * 
//	 * @param operator the person operating the airlock.
//	 * @return true if airlock successfully activated.
//	 */
//	public boolean activateAirlock(Person p) {
////		LogConsolidated.log(logger, Level.INFO, 0, sourceName, "[" + getLocale() + "] "
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
	
	public boolean setPressurizing() {
		if (AirlockState.DEPRESSURIZED == airlockState) {
		setState(AirlockState.PRESSURIZING);
		return true;
	}
		
		return false;
	}

	public boolean setDepressurizing() {
		if (AirlockState.PRESSURIZED == airlockState) {
			setState(AirlockState.DEPRESSURIZING);
			return true;
		}
		
		return false;
	}
	
	public boolean isDepressurized() {
		if (AirlockState.DEPRESSURIZED == airlockState) {
			return true;
		}
		return false;
	}
	
	public boolean isPressurized() {	
		if (AirlockState.PRESSURIZED == airlockState) {
			return true;
		}	
		return false;
	}
	
	public boolean switch2SteadyState() {
		if (AirlockState.PRESSURIZING == airlockState) {
			setState(AirlockState.PRESSURIZED);
			activated = false;
			return true;
		}
		
		if (AirlockState.DEPRESSURIZING == airlockState) {
			setState(AirlockState.DEPRESSURIZED);
			activated = false;
			return true;
		}
		
		return false;
	}
	
//	/**
//	 * Add airlock cycle time.
//	 * 
//	 * @param time cycle time (millisols)
//	 * @return true if cycle time successfully added.
//	 */
//	public void turnOffChamber(double time) {
//		if (activated
//				&& AirlockState.DEPRESSURIZING != airlockState
//				&& AirlockState.PRESSURIZING != airlockState
//				) {
//			addCycleTime(time);
//		}
//	}
	
//	/**
//	 * Add airlock cycle time.
//	 * 
//	 * @param time cycle time (millisols)
//	 * @return true if cycle time successfully added.
//	 */
//	public boolean addCycleTime(double time) {
//
//		boolean result = false;
//
//		if (activated) {
//			remainingCycleTime -= time;
//			if (remainingCycleTime <= 0D) {
//				// Reset remainingCycleTime back to max
//				remainingCycleTime = CYCLE_TIME;
//				result = deactivateAirlock();
//			} else {
//				result = true;
//			}
//		}
//
//		return result;
//	}

	/**
	 * Add airlock cycle time.
	 * 
	 * @param time cycle time (millisols)
	 * @return true if cycle time successfully added.
	 */
	public void addTime(double time) {
//		LogConsolidated.log(logger, Level.INFO, 4000, sourceName, "[" + getLocale() + "] "
//				+ getEntity() + " was adding time.");
//		boolean result = false;

		if (activated) {
			remainingCycleTime -= time;
			if (remainingCycleTime <= 0D) {
				// the air cycle has been completed
				// Reset remainingCycleTime to max
				remainingCycleTime = CYCLE_TIME;
				// Switch the airlock state to a steady state
				switch2SteadyState();
				LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
						"[" + getLocale() + "] Done with the air cycling in "
						+ getEntity() + ".");
			} 
//			else {
//				result = true;
//			}
		}

//		return result;
	}
	
	public void setActivated(boolean value) {
		if (value) {
			remainingCycleTime = CYCLE_TIME;
			LogConsolidated.log(logger, Level.INFO, 4000, sourceName, "[" + getLocale() + "] "
				+ getEntity() + " was being activated.");
		}
		else {
			LogConsolidated.log(logger, Level.INFO, 4000, sourceName, "[" + getLocale() + "] "
					+ getEntity() + " was being deactivated.");
		}
		activated = value;
	}
	
	
//	/**
//	 * Is this person the airlock operator ?
//	 * 
//	 * @param p
//	 * @return true if this person is the airlock operator 
//	 */
//	public boolean isOperator(Person p) {
//		if (p.getIdentifier() == operatorID)
//			return true;
//		return false;
//	}
//	
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
//	
//	/**
//	 * Elects an operator with the best EVA skill level/experiences
//	 */
//	private void electAnOperator() {
//		// Just in case the current operator is not in occupantIDs
////		if (operatorID > 0 && !occupantIDs.contains(operatorID))
////			occupantIDs.add(operatorID);
//		
//		// Select a person to become the operator
//		Person selected = null;
//		Integer selectedID = Integer.valueOf(-1);
//		int size = occupantIDs.size();
//
//		if (size == 1) {
//			List<Integer> list = new ArrayList<>(occupantIDs);
//			int id = list.get(0);
//			operatorID = id;
//			selected = getPersonByID(id);
//			LogConsolidated.log(logger, Level.FINER, 4_000, sourceName, "[" + selected.getLocale() + "] "
//					+ selected + " stepped up becoming the airlock operator in " 
//					+ selected.getLocationTag().getImmediateLocation() + ".");
//		}
//		
//		else if (size > 1 
////				&& (operatorID > 0 && !occupantIDs.contains(operatorID)
////				|| operatorID == -1)
//				) {
//			int evaExp = -1;
//			int evaLevel = -1;
//			for (Integer id : occupantIDs) {
//				Person p = 	getPersonByID(id);
//		    	if (p == null) {
//		    		p = unitManager.getPersonByID(id);
//		    		lookupPerson.put(id, p);
//		    	}
//				int level = p.getSkillManager().getSkillLevel(SkillType.EVA_OPERATIONS);
//				if (level > evaLevel) {
//					selected = p;
//					selectedID = id;
//				}
//	
//				else if (level == evaExp) {
//					int exp = p.getSkillManager().getSkillExp(SkillType.EVA_OPERATIONS);
//					if (exp > evaExp) {
//						selected = p;
//						selectedID = id;
//					}
//				}
//			}
//			
//			operatorID = selectedID;
//			
//			LogConsolidated.log(logger, Level.INFO, 4000, sourceName, "[" + selected.getLocale() + "] "
//					+ selected + " stepped up to be the airlock operator in " 
//					+ selected.getLocationTag().getImmediateLocation() + ".");
//		}
//	}
	
//	/**
//	 * Set new air pressure state, and unlock/open the (inner/outer) door. 
//	 * Any people inside the airlock proceed to leave the airlock
//	 * 
//	 * @return true if airlock was deactivated successfully.
//	 */
//	public boolean deactivateAirlock() {
////		LogConsolidated.log(logger, Level.INFO, 4000, sourceName, "[" + getLocale() + "] "
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
////		LogConsolidated.log(logger, Level.INFO, 4000, sourceName, "[" + getLocale() + "] "
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
////			LogConsolidated.log(logger, Level.INFO, 4000, sourceName,
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
		this.airlockState = state;
		logger.finer("The airlock in " + getEntityName() + " was " + state);
	}

	/**
	 * Gets the operator's Person instance
	 * 
	 * @return
	 */
	public Person getOperator() {
		return getPersonByID(operatorID);
	}

	/**
	 * Clears the airlock operator.
	 */
	public void clearOperator() {
		operatorID = Integer.valueOf(-1);
	}

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
	 */
	public boolean addAwaitingInnerDoor(Person p, Integer id) {
		// Add the person's ID to the lookup map
//		int id = p.getIdentifier();
		
		addPersonID(p, id);
		
//		if (!awaitingInnerDoor.contains(id)) {
//			
//			String loc = p.getImmediateLocation();
//			loc = loc == null ? "[N/A]" : loc;
//			loc = loc.equalsIgnoreCase("Outside") ? loc.toLowerCase() : "in " + loc;
//			
//			LogConsolidated.log(logger, Level.INFO, 4000, sourceName, "[" + p.getLocale() + "] "
//					+ p.getName() + " was " + loc 
//					+ " waiting for the interior door to open.");
//			
//			awaitingInnerDoor.add(id);
//			
//			return true;
//		}
//		
//		return false;
		
		return addSet(awaitingInnerDoor, id);
	}

	/**
	 * Adds person to queue awaiting airlock by outer door.
	 * 
	 * @param person the person to add to the awaiting queue.
	 */
	public boolean addAwaitingOuterDoor(Person p, Integer id) {
//		int id = p.getIdentifier();
		
		addPersonID(p, id);
		
//		if (!awaitingOuterDoor.contains(id)) {
//			
//			String loc = p.getImmediateLocation();
//			loc = loc == null ? "[N/A]" : loc;
//			loc = loc.equalsIgnoreCase("Outside") ? loc.toLowerCase() : "in " + loc;
//			
//			LogConsolidated.log(logger, Level.FINER, 4000, sourceName, "[" + p.getLocale() + "] "
//					+ p.getName() + " was " + loc 
//					+ " queuing for the exterior door to open.");
//			
//			awaitingOuterDoor.add(id);
//			
//			return true;
//		}
//		
//		return false;
		
		return addSet(awaitingOuterDoor, id);
	}
	
	public boolean addSet(Set<Integer> set, Integer id) {
		if (set.size() >= MAX_SLOTS)
			return false;
		if (!set.contains(id)) {
			set.add(id);
			return true;
		}
		else {
			return true;
		}
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
		if (awaitingOuterDoor.size() == 0)
			return false;
		
		return true;
	}
	
	/**
	 * Checks if anyone is waiting at the inner door
	 * 
	 * @return
	 */
	public boolean hasAwaitingInnerDoor() {
		if (awaitingInnerDoor.size() == 0)
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
//
//		if (activated) {
//			
////			if (occupantIDs.size() > 0 && operatorID == -1)
////				electAnOperator();
//			
////			logger.config("occupantIDs.size() : " + occupantIDs.size());
//			
//			// If there is no operator, operatorID = -1
//			if (operatorID > 0) {
////				LogConsolidated.log(logger, Level.FINER, 4000, sourceName,
////						"Airlock::timePassing - the airlock was activated by " + getPersonByID(operatorID) + ".");
//				
////				logger.config("unitManager is " + unitManager);
//				Person p = getPersonByID(operatorID);
////				logger.config("operatorID is " + operatorID);
////				logger.config("person is " + p);
////				logger.config("p.getPhysicalCondition() is " + p.getPhysicalCondition());
//				boolean isDead = p.getPhysicalCondition().isDead();
//				// Check if operator is dead.
//				if (isDead) {
//					
//					if (occupantIDs.isEmpty())
//						turnOffChamber(time);
//					
//					// If operator is dead, deactivate airlock.
//					String operatorName = p.getName();
//					LogConsolidated.log(logger, Level.WARNING, 10_000, sourceName, "[" + p.getLocale() + "] "
//							+ "Airlock operator " + operatorName + " was dead.");
//				}
//				
//				else {
//					// Check if airlock operator still has a task involving the airlock.
//					boolean hasAirlockTask = false;
//
//					Task task = p.getMind().getTaskManager().getTask();
//
//					if (task != null) {
//						if ((task instanceof ExitAirlock) || (task instanceof EnterAirlock)
//								|| (task instanceof EVAOperation) ) {
//							hasAirlockTask = true;
//						}
//						task = task.getSubTask();
//					}
//
//					if (task != null) {
//						if ((task instanceof ExitAirlock) || (task instanceof EnterAirlock)
//								|| (task instanceof EVAOperation) ) {
//							hasAirlockTask = hasAirlockTask || true;
//						}
//						task = task.getSubTask();
//					}
//					
//					if (task != null) {
//						if ((task instanceof ExitAirlock) || (task instanceof EnterAirlock)
//								|| (task instanceof EVAOperation) ) {
//							hasAirlockTask = hasAirlockTask || true;
//						}
//					}
//					
//					if (!hasAirlockTask) {
//						String operatorName = p.getName();
//						LogConsolidated.log(logger, Level.FINE, 10_000, sourceName, "[" + p.getLocale() + "] "
//								+ operatorName 
//								+ " was no longer being the airlock operator at " + getEntityName());
//						
//						if (occupantIDs.isEmpty())
//							turnOffChamber(time);
//					}
//				}
//			}
//			
//			else {
//				// If no operator and no occupants, deactivate airlock.
//				if (occupantIDs.isEmpty())
//					turnOffChamber(time);
////				LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
////						"Without an operator, the airlock in " 
////						+ getEntityName() + " got deactivated.");		
//			}
//		}
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
	public abstract Point2D getAvailableInteriorPosition(boolean insid);
	
//	public Point2D getAvailableInteriorPosition(boolean inside) {
//	return ((BuildingAirlock)this).getAvailableInteriorPosition(inside);
//}
	
	public abstract Point2D getAvailableInteriorPosition();
	
	/**
	 * Gets an available position outside the airlock entity.
	 * 
	 * @param inside true if the position is inside of the exterior door
	 * @return available local position.
	 */
	public abstract Point2D getAvailableExteriorPosition(boolean inside);
	
//	public Point2D getAvailableExteriorPosition(boolean inside) {
//		return ((BuildingAirlock)this).getAvailableExteriorPosition(inside);
//	}

	public abstract Point2D getAvailableExteriorPosition();
	
	/**
	 * Gets an available airlock position
	 * 
	 * @return available local position.
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
	 * Gets the number of occupants currently inside the airlock
	 * 
	 * @return
	 */
	public int getNumOccupants() {
		return occupantIDs.size();
	}
	
	/**
	 * Checks if there is no occupants inside the airlock
	 * 
	 * @return true if the airlock is empty
	 */
	public boolean isEmpty() {
		return occupantIDs.isEmpty();
	}
	
	public boolean hasSpace() {
		if (occupantIDs.size() < capacity)
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

	/**
	 * Add a person's ID to the lookup map for person inside the airlock
	 * 	
	 * @param p
	 */
	public void addPersonID(Person p) {
		if (lookupPerson == null)
			lookupPerson = new HashMap<>();
		if (p != null && !lookupPerson.containsKey(p.getIdentifier()))
			lookupPerson.put(p.getIdentifier(), p);
	}
	
	/**
	 * Add a person's ID to the lookup map for person inside the airlock
	 * 	
	 * @param p
	 */
	public void addPersonID(Person p, Integer id) {
		if (lookupPerson == null)
			lookupPerson = new HashMap<>();
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

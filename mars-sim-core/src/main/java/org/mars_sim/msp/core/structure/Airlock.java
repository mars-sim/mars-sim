/**
 * Mars Simulation Project
 * Airlock.java
 * @version 3.1.0 2017-03-09
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.mars.MarsSurface;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.EnterAirlock;
import org.mars_sim.msp.core.person.ai.task.ExitAirlock;
import org.mars_sim.msp.core.person.ai.task.utils.Task;

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

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());

	/** Pressurize/depressurize time (millisols). */
	public static final double CYCLE_TIME = 5D; // TODO: should we add pre-breathing time into CYCLE_TIME ?

	public enum AirlockState {
		PRESSURIZED, DEPRESSURIZED, PRESSURIZING, DEPRESSURIZING
	}

	public AirlockState airlockState; 

	// Data members
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
	
	/** The locale information of the airlock. */
//	private String locale ="";
	
	/** People currently in airlock. */
    private volatile Collection<Integer> occupantIDs;
//	private Collection<Person> occupants;

	/** The person currently operating the airlock. */
    private volatile Integer operatorID;
//	private Person operator;
    
	/** People waiting for the airlock by the inner door. */
    private volatile List<Integer> awaitingInnerDoor;
//	private List<Person> awaitingInnerDoor;

	/** People waiting for the airlock by the outer door. */
    private volatile List<Integer> awaitingOuterDoor;
//	private List<Person> awaitingOuterDoor;

	private volatile Map<Integer, Person> lookupPerson;
	
    protected static UnitManager unitManager; //= Simulation.instance().getUnitManager();
    protected static MarsSurface marsSurface;// = unitManager.getMarsSurface(); //getMars().getMarsSurface();
    
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
		occupantIDs = new ConcurrentLinkedQueue<>();
		awaitingInnerDoor = new ArrayList<>();
		awaitingOuterDoor = new ArrayList<>();
		
//		if (getEntity() instanceof Building) {
//			locale = ((Building)getEntity()).getLocationTag().getLocale();//.getBuildingManager().getSettlement().getName();
//		}
//		
//		else if (getEntity() instanceof Vehicle) {
//			locale = ((Vehicle)getEntity()).getLocationTag().getLocale();
////			if (((Vehicle)getEntity()).getSettlement() != null) {
////				locale = ((Vehicle)getEntity()).getSettlement().getName();
////			}
////			else {
////				locale = ((Vehicle)getEntity()).getLocale();
////			}
//		}
	}

	/**
	 * Enters a person into the airlock from either the inside or the outside. Inner
	 * or outer door (respectively) must be unlocked for person to enter.
	 * 
	 * @param person {@link Person} the person to enter the airlock
	 * @param inside {@link boolean} <code>true</code> if person is entering from
	 *               inside<br/>
	 *               <code>false</code> if person is entering from outside
	 * @return {@link boolean} <code>true</code> if person entered the airlock
	 *         successfully
	 */
	public boolean enterAirlock(Person person, boolean inside) {
		boolean result = false;

		// Warning : do NOT use int id or else the list's method remove(int index) would be chosen to use
		// List can't tell if the method remove(Object o) should be used.
		Integer id = person.getIdentifier();
		
		addPersonID(person);
		
		if (!occupantIDs.contains(id) && (occupantIDs.size() < capacity)) {

			if (inside && !innerDoorLocked) {
				if (awaitingInnerDoor.contains(id)) {
					awaitingInnerDoor.remove((Integer)id);
					
					if (awaitingInnerDoor.contains(id)) {
						throw new IllegalStateException(person + " was still waiting inner door.");
					}
				}
				LogConsolidated.log(Level.FINER, 0, sourceName,
						"[" + person.getLocationTag().getLocale() + "] " 
							+ person.getName() + " entered through the inner door of the airlock at " + getEntityName());
				result = true;
			} else if (!inside && !outerDoorLocked) {
				if (awaitingOuterDoor.contains(id)) {
					awaitingOuterDoor.remove((Integer)id);
					
					if (awaitingOuterDoor.contains(id)) {
						throw new IllegalStateException(person + " was still awaiting outer door!");
					}
				}
				LogConsolidated.log(Level.FINER, 0, sourceName,
						"[" + person.getLocationTag().getLocale() + "] " 
							+ person.getName() + " entered through the outer door of the airlock at " + getEntityName());
				result = true;
			}

			if (result) {
				occupantIDs.add(id);
			}
		}

		return result;
	}

	/**
	 * Activates the airlock if it is not already activated. Automatically closes
	 * both doors and starts pressurizing/depressurizing.
	 * 
	 * @param operator the person operating the airlock.
	 * @return true if airlock successfully activated.
	 */
	public boolean activateAirlock(Person operator) {
		
		operatorID = (Integer) operator.getIdentifier();
		
		addPersonID(operator);
		
		LogConsolidated.log(Level.FINER, 0, sourceName,
				"[" + operator.getLocationTag().getLocale() + "] " 
					+ operator.getName() + " as the operator was getting ready to activate the airlock at "
					+ getEntityName());
		
		boolean result = false;

		if (!activated) {
			if (!innerDoorLocked) {
				while ((occupantIDs.size() < capacity) && (awaitingInnerDoor.size() > 0)) {

					Integer id = awaitingInnerDoor.get(0);
					Person person = getPersonByID(id);
				
			    	if (person == null) {
			    		person = unitManager.getPersonByID(id);
			    		lookupPerson.put(id, person);
			    	}
					
					awaitingInnerDoor.remove((Integer)id);

					if (awaitingInnerDoor.contains(id)) {
						throw new IllegalStateException(person + " was still awaiting inner door!");
					}

					if (!occupantIDs.contains(id)) {
						LogConsolidated.log(Level.FINER, 0, sourceName,
								"[" + person.getLocationTag().getLocale() + "] " 
									+ person.getName() + " entered through the inner door of the airlock at "
									+ getEntityName());
						occupantIDs.add(id);
					}

				}
				innerDoorLocked = true;
			} else if (!outerDoorLocked) {
				while ((occupantIDs.size() < capacity) && (awaitingOuterDoor.size() > 0)) {

					Integer id = awaitingOuterDoor.get(0);
					Person person = getPersonByID(id);
					
			    	if (person == null) {
			    		person = unitManager.getPersonByID(id);
			    		lookupPerson.put(id, person);
			    	}
					
					awaitingOuterDoor.remove((Integer)id);

					if (awaitingOuterDoor.contains(id)) {
						throw new IllegalStateException(person + " still awaiting outer door!");
					}

					if (!occupantIDs.contains(id)) {
						LogConsolidated.log(Level.FINER, 0, sourceName,
								"[" + person.getLocationTag().getLocale() + "] " 
								+ person.getName() + " entered through the outer door of the airlock at "
								+ getEntityName());
						occupantIDs.add(id);
					}

				}
				outerDoorLocked = true;

				// operator.getBuildingLocation().getThermalGeneration().getHeating().flagHeatDumpViaAirlockOuterDoor(false);
			} else {
				return false;
			}

			activated = true;
			remainingCycleTime = CYCLE_TIME;

			if (AirlockState.PRESSURIZED == airlockState) {
				setState(AirlockState.DEPRESSURIZING);
			} else if (AirlockState.DEPRESSURIZED == airlockState) {
				setState(AirlockState.PRESSURIZING);
			} else {
				LogConsolidated.log(Level.SEVERE, 5_000, sourceName,
						"[" + operator.getLocationTag().getLocale() + "] " 
					+ operator.getName() + " reported the airlock was having incorrect state for activation: '" + airlockState + "'.");
				return false;
			}
			
			result = true;
		}

		return result;
	}

	/**
	 * Add airlock cycle time.
	 * 
	 * @param time cycle time (millisols)
	 * @return true if cycle time successfully added.
	 */
	public boolean addCycleTime(double time) {

		boolean result = false;

		if (activated) {
			remainingCycleTime -= time;
			if (remainingCycleTime <= 0D) {
				remainingCycleTime = 0D;
//				result = deactivateAirlock();
			} else {
				result = true;
			}
		}

		return result;
	}

	/**
	 * Elects an operator with the best EVA skill level/experiences
	 */
	private void electAnOperator() {
		Person selected = null;
		Integer selectedID = Integer.valueOf(-1);
		int size = occupantIDs.size();
	
//		int rand = -1;
//		
//		if (size > 0) {
//			rand = RandomUtil.getRandomInt(size-1);
//			List<Person> list = new ArrayList<>(occupants);
//			return list.get(rand);
//		}
		
//		if (size == 0) {
//			// No operator, deactivate the air lock
//			deactivateAirlock();
//		}
//		
//		else 
		if (size >= 1) {
			int evaExp = -1;
			int evaLevel = -1;
			for (Integer id : occupantIDs) {
				Person p = 	getPersonByID(id);
		    	if (p == null) {
		    		p = unitManager.getPersonByID(id);
		    		lookupPerson.put(id, p);
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
			
			operatorID = selectedID;

			LogConsolidated.log(Level.FINER, 0, sourceName, "[" + selected.getLocationTag().getLocale() + "] "
						+ selected + " stepped up and became the operator of the airlock in "
						+ getEntityName());
		}
	}
	
	/**
	 * Set new air pressure state, and unlock/open the (inner/outer) door. 
	 * Any people inside the airlock proceed to leave the airlock
	 * 
	 * @return true if airlock was deactivated successfully.
	 */
	public boolean deactivateAirlock() {

		boolean result = false;

		if (activated) {
			activated = false;

			if (AirlockState.DEPRESSURIZING == airlockState) {
				setState(AirlockState.DEPRESSURIZED);
				outerDoorLocked = false;
			} else if (AirlockState.PRESSURIZING == airlockState) {
				setState(AirlockState.PRESSURIZED);
				innerDoorLocked = false;
			} else {
				return false;
			}

			// Occupants are to leave the airlock one by one
			boolean successful = leaveAirlock();
			if (successful) {
				occupantIDs.clear();
				operatorID = Integer.valueOf(-1);
				result = true;
			}
			else {
				result = false;
			}
		}

		return result;
	}

	/**
	 * Occupants are to exit/leave the airlock one by one
	 */
	public boolean leaveAirlock() {
		boolean successful = true;
		Iterator<Integer> i = occupantIDs.iterator();
		while (i.hasNext()) {
			Integer id = i.next();
			Person p = getPersonByID(id);
	    	if (p == null) {
	    		p = unitManager.getPersonByID(id);
	    		lookupPerson.put(id, p);
	    	}
			LogConsolidated.log(Level.FINER, 0, sourceName,
					"[" + p.getLocationTag().getLocale() + "] " + p.getName()
					+ " reported that the airlock in " + getEntity() + " had been " 
					+ getState().toString().toLowerCase() + ".");
			
			// Call BuildingAirlock or VehicleAirlock's exitAirlock() to change the state of the his container unit, namely, the EVA suit
			successful = successful && exitAirlock(p);
		}
		return successful;
	}
	
	/**
	 * Causes a person within the airlock to exit either inside or outside.
	 * 
	 * @param occupant the person to exit.
	 */
	protected abstract boolean exitAirlock(Person occupant);

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
	public void addAwaitingAirlockInnerDoor(Person p) {
		addPersonID(p);
		if (!awaitingInnerDoor.contains(p.getIdentifier())) {
			LogConsolidated.log(Level.FINER, 0, sourceName, "[" + p.getLocationTag().getLocale() + "] "
					+ p.getName() + " was awaiting the inner door of the airlock in " + getEntityName() + " to open.");
			awaitingInnerDoor.add(p.getIdentifier());
		}
	}

	/**
	 * Adds person to queue awaiting airlock by outer door.
	 * 
	 * @param person the person to add to the awaiting queue.
	 */
	public void addAwaitingAirlockOuterDoor(Person p) {
		addPersonID(p);
		if (!awaitingOuterDoor.contains(p.getIdentifier())) {
			LogConsolidated.log(Level.FINER, 0, sourceName, "[" + p.getLocationTag().getLocale() + "] "
					+ p.getName() + " was in " + p.getLocationTag().getImmediateLocation() 
					+ " and waiting the outer door of the airlock in " + getEntityName() + " to open.");
			awaitingOuterDoor.add(p.getIdentifier());
		}
	}

	/**
	 * Time passing for airlock. Check for unusual situations and deal with them.
	 * Called from the unit owning the airlock.
	 * 
	 * @param time amount of time (in millisols)
	 */
	public void timePassing(double time) {
		
		if (activated) {
			
			if (operatorID > 0) {//!= Integer.valueOf(-1)) {
//				logger.config("unitManager is " + unitManager);
				Person p = getPersonByID(operatorID);
//				logger.config("operatorID is " + operatorID);
//				logger.config("person is " + p);
//				logger.config("p.getPhysicalCondition() is " + p.getPhysicalCondition());
				boolean isDead = p.getPhysicalCondition().isDead();
				// Check if operator is dead.
				if (isDead) {
					// If operator is dead, deactivate airlock.
					String operatorName = p.getName();
					LogConsolidated.log(Level.WARNING, 10_000, sourceName, "[" + p.getLocationTag().getLocale() + "] "
							+ "Airlock operator " + operatorName + " was dead."
							+ getEntityName());
					
					// Elect a new operator
					electAnOperator();
				}
				
				else {
					// Check if airlock operator still has a task involving the airlock.
					boolean hasAirlockTask = false;

					Task task = p.getMind().getTaskManager().getTask();

					if (task != null) {
						if ((task instanceof ExitAirlock) || (task instanceof EnterAirlock)) {
							hasAirlockTask = true;
						}
						task = task.getSubTask();
					}

					if (!hasAirlockTask) {
						String operatorName = p.getName();
						LogConsolidated.log(Level.FINE, 10_000, sourceName, "[" + p.getLocationTag().getLocale() + "] "
								+ operatorName 
								+ " was no longer being the Airlock operator operating the airlock at " + getEntityName());
						
						// Elect a new operator
						electAnOperator();
					}
				}
			}
			
//			else {
//				// If no operator, deactivate airlock.
//				LogConsolidated.log(Level.SEVERE, 0, sourceName, //"[" + locale + "] "
//						"Without an operator, the airlock in " 
//						+ getEntityName() + " may get deactivated.");
//				
//				// Elect a new operator
//				electAnOperator();
//			}
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
	 * Gets an available position inside the airlock entity.
	 * 
	 * @return available local position.
	 */
	public abstract Point2D getAvailableInteriorPosition();

	/**
	 * Gets an available position outside the airlock entity.
	 * 
	 * @return available local position.
	 */
	public abstract Point2D getAvailableExteriorPosition();

	/**
	 * Gets an available position inside the airlock.
	 * 
	 * @return available local position.
	 */
	public abstract Point2D getAvailableAirlockPosition();

//    public Collection<Unit> getOccupants() {
//    	return occupants;
//    }

	public Collection<Integer> getOccupants() {
		return occupantIDs;
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
				addPersonID(p);
				return p;
			}
		}
		return null;
	}

	public void addPersonID(Person p) {
		if (lookupPerson == null)
			lookupPerson = new HashMap<>();
		if (p != null && !lookupPerson.containsKey(p.getIdentifier()))
			lookupPerson.put(p.getIdentifier(), p);
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
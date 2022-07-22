/*
 * Mars Simulation Project
 * Airlock.java
 * @date 2021-11-04
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.LocalPosition;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.environment.MarsSurface;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

// see discussions on Airlocks for Mars Colony at
// https://forum.nasaspaceflight.com/index.php?topic=42098.0

// Astronauts aboard the International Space Station preparing for extra-vehicular activity (EVA)
// "camp out" at low atmospheric pressure, 10.2 psi (0.70 sbar), spending eight sleeping hours
// in the Quest airlock chamber before their spacewalk. During the EVA they breathe 100% oxygen
// in their space suits, which operate at 4.3 psi (0.30 bar),[71] although research has examined
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

//	/** Pressurize/depressurize time (millisols). */
//	public static final double CYCLE_TIME = 5D; 
//	/** The maximum number of space in the chamber. */
//	public static final int MAX_SLOTS = 2;
	/** The maximum number of reservations that can be made for an airlock. */
	public static final int MAX_RESERVED = 4;
	/** The effective reservation period [in millisols]. */
	public static final int RESERVATION_PERIOD = 40;
	
	/**
	 * Available Airlock States
	 */
	public enum AirlockState {
		OFF 			("Off"),
		PRESSURIZED 	("Pressurized"), 
		DEPRESSURIZING 	("Depressurizing"),
		DEPRESSURIZED 	("Depressurized"),
		PRESSURIZING 	("Pressurizing");
		
		private String name;

		private AirlockState(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}
		
		public String toString() {
			return this.name;
		}
	}

	public AirlockState airlockState;

	// Data members
	/** True if inner door is locked. */
	private boolean innerDoorLocked;
	/** True if outer door is locked. */
	private boolean outerDoorLocked;

	/** Number of people who can use the airlock at once. */
	private int capacity;
	/** Number of times no eva suit is found available. */
	private int numEVASuitChecks;

	/** The person currently operating the airlock. */
    private Integer operatorID;

	/** People currently within airlock's zone 1, 2 and 3 only (but NOT zone 0 and 4). */
    private Set<Integer> occupantIDs;

	/** People waiting for the airlock by the inner door. */
    protected Set<Integer> awaitingInnerDoor;

	/** People waiting for the airlock by the outer door. */
    private Set<Integer> awaitingOuterDoor;

	/** The occupant reservation map. */
	private Map<Integer, Integer> reservationMap;

    protected static UnitManager unitManager;
    protected static MarsSurface marsSurface;
    protected static MarsClock marsClock;

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

		airlockState = AirlockState.PRESSURIZED;
		innerDoorLocked = false;
		outerDoorLocked = true;

		operatorID = Integer.valueOf(-1);

		occupantIDs = new CopyOnWriteArraySet<>();
		awaitingInnerDoor = new HashSet<>();
		awaitingOuterDoor = new HashSet<>();

		reservationMap = new HashMap<>();
	}

	/**
	 * Is this person's id on the reservation map ?
	 *
	 * @param personInt
	 * @return
	 */
	public boolean hasReservation(int personInt) {
		if (reservationMap.containsKey(personInt)) {
			int msol = marsClock.getMillisolInt();
			int lastMsol = reservationMap.get(personInt);
			int diff = 0;
			if (lastMsol > msol)
				diff = msol + 1000 - lastMsol;
			else
				diff = msol - lastMsol;
			if (diff <= RESERVATION_PERIOD) {
				return true;
			}
			else {
				// Removes the expired reservation 
				// since it goes beyond the RESERVATION_PERIOD msols.
				reservationMap.remove(personInt);
			}
		}
		return false;
	}

	/**
	 * Removes the id from all reservation maps.
	 *
	 * @param personInt
	 * @return
	 */
	public boolean removeReservation(int personInt) {
		if (getEntity() instanceof Building) {
			Airlock a = ((Building)getEntity()).getEVA().getAirlock();
			if (a.getReservationMap().containsKey(personInt)) {
				a.getReservationMap().remove(personInt);
				return true;
			}
		}
		else {
			Airlock a = ((Rover)(Vehicle)getEntity()).getAirlock();
			if (a.getReservationMap().containsKey(personInt)) {
				a.getReservationMap().remove(personInt);
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds a person's id to the reservation map.
	 *
	 * @param personInt
	 * @return true if the id can be added or is already in reservation
	 */
	public boolean addReservation(int personInt) {
		if (!reservationMap.containsKey(personInt)) {
			// Test if the reservation map already has 4 people
			if (reservationMap.size() <= MAX_RESERVED) {
				int msol = marsClock.getMillisolInt();
				reservationMap.put(personInt, msol);
				return true;
			}
			else
				return false;
		}
		else {
			int msol = marsClock.getMillisolInt();
			int lastMsol = reservationMap.get(personInt);
			int diff = 0;
			if (lastMsol > msol)
				diff = msol + 1000 - lastMsol;
			else
				diff = msol - lastMsol;

			// If it has been RESERVATION_PERIOD msols since the reservation was made,
			// reserve it again
			if (diff >= RESERVATION_PERIOD) {
				// Replace it with the new msol
				reservationMap.put(personInt, msol);
			}
			
			return true;
		}
	}

	/**
	 * Gets the reservation ids.
	 *
	 * @return
	 */
	public Set<Integer> getReserved() {
		return reservationMap.keySet();
	}

	/**
	 * Gets the number of people reserved.
	 *
	 * @return
	 */
	public int getReservedNum() {
		return reservationMap.size();
	}
	
	/**
	 * Gets the reservation map.
	 *
	 * @return
	 */
	public Map<Integer, Integer> getReservationMap() {
		return reservationMap;
	}

	/**
	 * Is the reservation full ?
	 *
	 * @return
	 */
	public boolean isReservationFull() {
		if (reservationMap.size() > MAX_RESERVED - 1) {
			return true;
		}
		return false;
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
	 * Transfers a person into zone 1, 2 and 3.
	 *
	 * @param person {@link Person} the person to enter the airlock
	 * @param id the person's id
	 * @param egress {@link boolean} <br/> <code>true</code> if person is entering from
	 *               inside<br/>
	 *               <code>false</code> if person is entering from outside
	 * @return {@link boolean} <code>true</code> if person entered the airlock
	 *         successfully
	 */
	private boolean transferIn(Person person, Integer id, boolean egress) {
		boolean result = false;
		// Transfer the person into zone 1, 2 and 3 via the inner door
		if (egress && !innerDoorLocked) {
			if (awaitingInnerDoor.contains(id)) {
				awaitingInnerDoor.remove(id);

				if (awaitingInnerDoor.contains(id)) {
					throw new IllegalStateException(person + " was still waiting at the inner door.");
				}
			}
			logger.log(person, Level.FINE, 0,
					"Transferred in through the inner door of " + getEntityName() + ".");
			result = true;
		}

		// Transfer the person into zone 1, 2 and 3 via the outer door
		else if (!egress && !outerDoorLocked) {
			if (awaitingOuterDoor.contains(id)) {
				awaitingOuterDoor.remove(id);

				if (awaitingOuterDoor.contains(id)) {
					throw new IllegalStateException(person + " was still waiting at the outer door!");
				}
			}
			logger.log(person, Level.FINE, 0,
					"Transferred in through the outer door of " + getEntityName() + ".");
			result = true;
		}

		if (result) {
			// Add the person's ID to the occupant ID list
			// Define occupants as being in zone 1, 2, and 3.
			// Being in zone 0 and 4 are not considered an airlock occupant.
			occupantIDs.add(id);
		}

		return result;
	}

	/**
	 * Transfers a person out of airlock zone 1, 2, and 3.
	 *
	 * @param id the person's id
	 * @return {@link boolean} <code>true</code> if person exiting the airlock
	 *         successfully
	 */
	public boolean transferOut(Integer id) {

		if (operatorID.equals(id)) {;
			operatorID = Integer.valueOf(-1);
		}

		return occupantIDs.remove(id);
	}

	/**
	 * Removes the id of a person.
	 *
	 * @param id the person's id
	 */
	public void removeID(Integer id) {
		if (getAirlockType() == AirlockType.BUILDING_AIRLOCK) {
			for (int i=0; i<5; i++) {
				// Remove this person from all zone maps
				vacate(i, id);
			}
		}

		if (operatorID.equals(id)) {;
			operatorID = Integer.valueOf(-1);
		}

		occupantIDs.remove(id);
		awaitingInnerDoor.remove(id);
		awaitingOuterDoor.remove(id);

		// remove the reservation
		if (getAirlockType() == AirlockType.BUILDING_AIRLOCK)
			removeReservation(id);
	}


	/**
	 * Checks if the chamber is pressurizing.
	 *
	 * @return true if it's pressurizing
	 */
	public boolean isPressurizing() {
		return AirlockState.PRESSURIZING == airlockState;
	}

	/**
	 * Checks if the chamber is depressurizing.
	 *
	 * @return true if it's depressurizing
	 */
	public boolean isDepressurizing() {
		return AirlockState.DEPRESSURIZING == airlockState;
	}

	/**
	 * Checks if the airlock has been depressurized.
	 *
	 * @return
	 */
	public boolean isDepressurized() {
		return AirlockState.DEPRESSURIZED == airlockState;
	}

	/**
	 * Checks if the airlock has been pressurized.
	 *
	 * @return
	 */
	public boolean isPressurized() {
		return AirlockState.PRESSURIZED == airlockState;
	}

	/**
	 * Activates the airlock.
	 * 
	 * @param value
	 */
	public abstract void setActivated(boolean value);

	/**
	 * Allows or disallows the airlock to be transitioning its state.
	 *
	 * @param value
	 */
	public abstract void setTransitioning(boolean value);
	
	/**
	 * Is this person the airlock operator ?
	 *
	 * @param id the id of the person
	 * @return true if this person is the airlock operator
	 */
	public boolean isOperator(int id) {
		return operatorID.equals(Integer.valueOf(id));
	}

	/**
	 * Gets the operator's Person instance.
	 *
	 * @return
	 */
	public String getOperatorName() {
		if (operatorID.equals(Integer.valueOf(-1)))
			return "None";
		else {
			Person p = getPersonByID(operatorID);
			if (p != null) {
				return p.getName();
			}
		}

		return "N/A";
	}

	/**
     * Gets a set of occupants from a particular zone.
     *
     * @param zone the zone of interest
	 * @return a set of occupants in the zone of the interest
     */
    public abstract Set<Integer> getZoneOccupants(int zone);

    /**
     * Gets the exact number of occupants who are within the chamber.
     * 
     * @return
     */
    public abstract int getNumInChamber();
    
	/**
	 * Selects a prioritized pool of candidates from a particular zone.
	 *
	 * return a pool of candidates
	 */
	private Set<Integer> getOperatorPool() {
		
		if (!occupantIDs.isEmpty()) {
			// Priority 1 : zone 2 - inside the chambers
			return occupantIDs;
		}
		else {
			// Priority 2 : zone 3 - on the inside of the outer door
			Set<Integer> pool = getZoneOccupants(3);
			if (!pool.isEmpty()) {
				return pool;
			}
			
			// Priority 3 : zone 1 - on the inside of the inner door
			pool = getZoneOccupants(1);
			if (!pool.isEmpty()) {
				return pool;
			}

			// Priority 4 : zone 4 - on the outside of the outer door, thus at awaitingOuterDoor
			pool = awaitingOuterDoor;
			if (!pool.isEmpty()) {
				return pool;
			}

			// Priority 3 : zone 0 - on the outside of the inner door, thus at awaitingInnerDoor
			pool = awaitingInnerDoor;
			if (!pool.isEmpty()) {
				return pool;
			}
			
			return pool;
		}
	}

	/**
	 * Elects an operator with the best EVA skill level/experiences.
	 *
	 * @param pool a pool of candidates
	 */
	private void electAnOperator(Set<Integer> pool) {

		int size = pool.size();

		if (size == 0)
			return;
		
		if (size == 1) {
			int id = new ArrayList<>(pool).get(0);
			operatorID = Integer.valueOf(id);
			logger.log(getPersonByID(id), Level.FINE, 4_000,
					"Acted as the airlock operator.");
		}

		else {
			Integer selectedID = Integer.valueOf(-1);
			Person selected = null;

			int evaExp = -1;
			int evaLevel = -1;
			for (Integer id : pool) {
				Person p = 	getPersonByID(id);
				int level = p.getSkillManager().getSkillLevel(SkillType.EVA_OPERATIONS);
				if (level > evaLevel) {
					selected = p;
					selectedID = id;
				}

				else if (level == evaLevel) {
					int exp = p.getSkillManager().getSkillExp(SkillType.EVA_OPERATIONS);
					if (exp > evaExp) {
						selected = p;
						selectedID = id;
					}
				}
			}

			operatorID = selectedID;
			logger.log(selected, Level.FINE, 4_000,
					"Stepped up becoming the airlock operator.");
		}
	}

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
	 * Sets the airlock's inner door locked to true or false.
	 *
	 * @param lock
	 */
	public void setInnerDoorLocked(boolean lock) {
		innerDoorLocked = lock;
	}

	/**
	 * Sets the airlock's outer door locked to true or false.
	 *
	 * @param lock
	 */
	public void setOuterDoorLocked(boolean lock) {
		outerDoorLocked = lock;
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
	}

	/**
	 * Adds person to queue awaiting airlock by inner door.
	 *
	 * @param p the person to add to the awaiting queue.
	 * @param id the id of the person
	 * @return true if the person can be added or is already in the queue
	 */
	public boolean addAwaitingInnerDoor(Integer id) {
		return addToZone(awaitingInnerDoor, id);
	}

	/**
	 * Adds person to queue awaiting airlock by outer door.
	 *
	 * @param p the person to add to the awaiting queue.
	 * @param id the id of the person
	 * @return true if the person can be added or is already in the queue
	 */
	public boolean addAwaitingOuterDoor(Integer id) {
		return addToZone(awaitingOuterDoor, id);
	}

	/**
	 * Adds this unit to the set or zone (for zone 0 and zone 4 only).
	 *
	 * @param set
	 * @param id
	 * @return true if the unit is already inside the set or if the unit can be added into the set
	 */
	protected abstract boolean addToZone(Set<Integer> set, Integer id);

	/**
	 * Gets the number of people waiting at the inner door.
	 *
	 * @return the number of people waiting at the inner door
	 */
	public int getNumAwaitingInnerDoor() {
		return awaitingInnerDoor.size();
	}

	/**
	 * Gets the number of people waiting at the outer door.
	 *
	 * @return the number of people waiting at the outer door
	 */
	public int getNumAwaitingOuterDoor() {
		return awaitingOuterDoor.size();
	}

	/**
	 * Gets the set of people waiting at the inner door.
	 *
	 * @return the set of people waiting at the inner door
	 */
	public Set<Integer> getAwaitingInnerDoor() {
		return awaitingInnerDoor;
	}

	/**
	 * Gets the set of people waiting at the outer door.
	 *
	 * @return the set of people waiting at the outer door
	 */
	public Set<Integer> getAwaitingOuterDoor() {
		return awaitingOuterDoor;
	}

	/**
	 * Checks if anyone is waiting at the outer door.
	 *
	 * @return true if someone is waiting at the outer door
	 */
	public boolean hasAwaitingOuterDoor() {
		return !awaitingOuterDoor.isEmpty();
	}

	/**
	 * Checks if anyone is waiting at the inner door.
	 *
	 * @return
	 */
	public boolean hasAwaitingInnerDoor() {
		return !awaitingInnerDoor.isEmpty();
	}

	/**
	 * Checks the occupants'id in zone 1, 2, or 3. Removes id if not there.
	 */
	public void checkOccupantIDs() {

		Iterator<Integer> i = occupantIDs.iterator();
		while (i.hasNext()) {
			int id = i.next();

			if (getZoneOccupants(1).contains(id)
				|| getZoneOccupants(2).contains(id)
				|| getZoneOccupants(3).contains(id)) {
				// If this person is not physically in zone 1, 2, or 3, remove his id
				continue;
			}
			else
				occupantIDs.remove(id);
		}
	}

	/**
	 * Checks the airlock operator.
	 */
	public void checkOperator() {
		// If no one is being assigned as an operator
		if (operatorID.equals(Integer.valueOf(-1))) { 
			// Choose a pool of candidates from a particular zone
			electAnOperator(getOperatorPool());
		}
		// If the airlock already has an existing operator,
		else {
			// Check to see if he's still inside or has left the airlock
			if (!isInAnyZones(operatorID)) {
				// Remove this operator
				operatorID = Integer.valueOf(-1);
				// Choose a pool of candidates from a particular zone
				electAnOperator(getOperatorPool());
			}
//			else {
//				int rand = RandomUtil.getRandomInt(1);
//				// Note: Provide some randomness in case the existing operator is stuck
//				// And require someone elsewhere as operator to help out.
//				if (rand == 0) {
//					// Choose a pool of candidates from a particular zone
//					electAnOperator(getOperatorPool());
//				}
//			}
		}
	}

	/**
	 * Adds airlock cycle time.
	 *
	 * @param time cycle time (millisols)
	 * @return The the time consumed
	 */
	public void addTime(double time) {
		
		if (AirlockState.PRESSURIZED == airlockState
				|| AirlockState.DEPRESSURIZING == airlockState) {
			setState(AirlockState.DEPRESSURIZING);
			
			innerDoorLocked = true;
			outerDoorLocked = true;
			
			cycleAir(time);
		}
		
		else if (AirlockState.DEPRESSURIZED == airlockState
				|| AirlockState.PRESSURIZING == airlockState) {
			setState(AirlockState.PRESSURIZING);
			
			innerDoorLocked = true;
			outerDoorLocked = true;
			
			cycleAir(time);
		}
	}
	
	
	/**
	 * Cycles the air and consumes the time
	 * 
	 * @param time
	 */
	protected abstract void cycleAir(double time);
	
	/**
	 * Checks if the airlock is currently activated.
	 *
	 * @return true if activated.
	 */
	public abstract boolean isActivated();
	
	/**
	 * Goes to the next steady state.
	 *
	 * @return true if the switch is successful
	 */
	public void goToNextSteadyState() {
		
		if (AirlockState.PRESSURIZING == airlockState) {
			setState(AirlockState.PRESSURIZED);
			innerDoorLocked = false;
			outerDoorLocked = true;
		}
		
		else if (AirlockState.DEPRESSURIZING == airlockState) {
			setState(AirlockState.DEPRESSURIZED);
			innerDoorLocked = true;
			outerDoorLocked = false;
		}
		
		setActivated(false);
		setTransitioning(false);
	}
			
	/**
	 * Removes the record of the deceased person from the map and sets.
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
	 * Checks if given person is currently in the airlock zone 1, 2, or 3.
	 *
	 * @param p the person to be checked
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
	 * Gets the entity this airlock is attached to.
	 *
	 * @return entity.
	 */
	public abstract Object getEntity();

	/**
	 * Gets the locale this airlock is at.
	 *
	 * @return entity.
	 */
	//public abstract String getLocale();

	/**
	 * Gets an available position inside the airlock entity.
	 *
	 * @param inside true if the position is inside of the interior door
	 * @return available local position.
	 */
	public abstract LocalPosition getAvailableInteriorPosition(boolean inside);

	/**
	 * Gets an available position inside the airlock entity.
	 *
	 * @return available local position.
	 */
	public abstract LocalPosition getAvailableInteriorPosition();

	/**
	 * Gets an available position outside the airlock entity.
	 *
	 * @param inside true if the position is inside of the exterior door
	 * @return available local position.
	 */
	public abstract LocalPosition getAvailableExteriorPosition(boolean inside);

	/**
	 * Gets an available position outside the airlock entity.
	 *
	 * @return available local position.
	 */
	public abstract LocalPosition getAvailableExteriorPosition();

	/**
	 * Gets an available airlock position.
	 *
	 * @return available airlock position.
	 */
	public abstract LocalPosition getAvailableAirlockPosition();

    public abstract boolean occupy(int zone, LocalPosition p, Integer id);

	public abstract boolean vacate(int zone, Integer id);

	public abstract boolean isInZone(Person p, int zone);

	public abstract void loadEVAActivitySpots();

	/**
	 * Gets a collection of occupants' ids.
	 *
	 * @return
	 */
	public Set<Integer> getOccupants() {
		return occupantIDs;
	}

	/**
	 * Is this person's id in any zones (0 to 4) ?
	 * 
	 * @param id
	 * @return
	 */
	public boolean isInAnyZones(int id) {
		return (occupantIDs.contains(id)
			|| awaitingInnerDoor.contains(id)
			|| awaitingOuterDoor.contains(id));
	}
	
	/**
	 * Gets a collection of occupants' ids.
	 *
	 * @return
	 */
	public abstract Set<Integer> getAllInsideOccupants();

	/**
	 * Checks if any occupants wear no EVA Suit.
	 *
	 * @return
	 */
	public boolean someoneHasNoEVASuit() {
		for (Integer id: occupantIDs) {
			Person p = getPersonByID(id);
			if (p != null && p.getSuit() == null)
				return true;
		}
		return false;
	}

	/**
	 * Gets a set of those having no EVA suit worn.
	 *
	 * @return
	 */
	public Set<Person> noEVASuit() {
		return getAllInsideOccupants()
				.stream()
				.map(i -> getPersonByID(i))
				.filter(p -> (p == null))
				.collect(Collectors.toSet());
	}

	/**
	 * Gets the number of occupants currently inside the airlock zone 1, 2, and 3.
	 *
	 * @return the number of occupants
	 */
	public abstract int getNumOccupants();

	/**
	 * Gets the number of empty slots.
	 *
	 * @return the number of empty slots
	 */
	public int getNumEmptied() {
		return capacity - getNumOccupants();
	}

	/**
	 * Checks if the chamber is full.
	 *
	 * @return
	 */
	public abstract boolean isChamberFull();

	/**
	 * Checks if there is no occupants inside the airlock.
	 *
	 * @return true if the airlock is empty
	 */
	public boolean isEmpty() {
		return occupantIDs.isEmpty() && getNumOccupants() == 0;
	}

	/**
	 * Checks if there is an empty slot left in Zone 1, 2 and 3.
	 *
	 * @return true if there is space
	 */
	public boolean hasSpace() {
		return getNumOccupants() < capacity;
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
	 * Gets a person's ID.
	 *
	 * @param id
	 * @return
	 */
	public Person getPersonByID(Integer id) {
		Person p = unitManager.getPersonByID(id);
		if (p != null) {
			return p;
		}
		return null;
	}

	/**
	 * Gets the type of airlock.
	 *
	 * @return AirlockType
	 */
	public abstract AirlockType getAirlockType();

	/**
	 * Time passing for the building.
	 * 
	 * @param pulse the amount of clock pulse passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	public abstract void timePassing(ClockPulse pulse);
	
	/**
	 * Initializes instances.
	 *
	 * @param um {@link UnitManager}
	 * @param ms {@link MarsSurface}
	 */
	public static void initializeInstances(UnitManager um, MarsSurface ms, MarsClock mc) {
		unitManager = um;
		marsSurface = ms;
		marsClock = mc;
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
	    marsClock = null;
	}
}

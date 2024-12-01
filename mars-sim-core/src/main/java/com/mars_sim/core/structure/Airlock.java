/*
 * Mars Simulation Project
 * Airlock.java
 * @date 2024-07-10
 * @author Scott Davis
 */

package com.mars_sim.core.structure;

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

import com.mars_sim.core.UnitManager;
import com.mars_sim.core.environment.MarsSurface;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.structure.building.BuildingException;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.time.MasterClock;

// Astronauts aboard the International Space Station preparing for extra-vehicular activity (EVA)
// "camp out" at low atmospheric pressure, 10.2 psi (0.70 sbar), spending eight sleeping hours
// in the Quest airlock chamber before their spacewalk. 
// 
// During the EVA they breathe 100% oxygen in their space suits, which operate at 4.3 psi 
// (0.30 bar), although research has examined the possibility of using 100% O2 at 9.5 psi 
// (0.66 bar) in the suits to lessen the pressure reduction, and hence the risk of DCS.
//
// Reference: 
// 1. Airlocks for Mars Colony. https://forum.nasaspaceflight.com/index.php?topic=42098.0
// 2. Pure oxygen pre-breathing..https://en.wikipedia.org/wiki/Decompression_sickness

/**
 * The Airlock class represents an airlock to a vehicle or structure.
 */
public abstract class Airlock implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(Airlock.class.getName());

	/** The maximum number of reservations that can be made for an airlock. */
	public static final int MAX_RESERVED = 4;
	/** The effective reservation period [in millisols]. */
	public static final int RESERVATION_PERIOD = 40;
	
	private AirlockMode airlockMode = AirlockMode.EGRESS;
	
	/**
	 * Available operational modes.
	 */
	public enum AirlockMode {
		NOT_IN_USE 	("Not in use"),
		INGRESS 	("Ingress"), 
		EGRESS		("Egress");
		
		private String name;

		private AirlockMode(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}
		
		@Override
		public String toString() {
			return this.name;
		}
	}
		
	
	/**
	 * Available operational states.
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
		
		@Override
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
    private Set<Integer> occupant123IDs;

	/** People waiting for the airlock by the inner door. */
    protected Set<Integer> awaitingInnerDoor;

	/** People waiting for the airlock by the outer door. */
    private Set<Integer> awaitingOuterDoor;

	/** The occupant reservation map. */
	private Map<Integer, Integer> reservationMap;

    protected static UnitManager unitManager;
    protected static MarsSurface marsSurface;
    private static MasterClock clock;

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

		occupant123IDs = new CopyOnWriteArraySet<>();
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
			int msol = clock.getMarsTime().getMillisolInt();
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
		if (reservationMap.containsKey(personInt)) {
			reservationMap.remove(personInt);
			return true;
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
		MarsTime now = clock.getMarsTime();
		if (!reservationMap.containsKey(personInt)) {
			// Test if the reservation map has been filled up
			if (!isReservationFull()) {
				int msol = now.getMillisolInt();
				reservationMap.put(personInt, msol);
				return true;
			}
			else
				return false;
		}
		else {
			int msol = now.getMillisolInt();
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
		if (reservationMap.size() >= MAX_RESERVED) {
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
		if (occupant123IDs.contains(id)) {
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
		if (!occupant123IDs.contains(id)) {
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
			occupant123IDs.add(id);
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

		return occupant123IDs.remove(id);
	}

	/**
	 * Removes the person.
	 *
	 * @param p Person   removed
	 */
	public void remove(Person p) {
		int id = p.getIdentifier();
		for (int i=0; i<5; i++) {
			vacate(AirlockZone.convert2Zone(i), p);
		}

		if (operatorID.equals(id)) {;
			operatorID = Integer.valueOf(-1);
		}

		occupant123IDs.remove(id);
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
	 * Checks if the airlock has an operator.
	 * 
	 * @return
	 */
	public boolean hasNoOperator() {
		return operatorID.equals(Integer.valueOf(-1));
	}
	
	/**
	 * Releases the operator responsibility if the person's id. 
	 * 
	 * @param id
	 */
	public void releaseOperatorID(int id) {
		if (isOperator(id)) {
			operatorID = Integer.valueOf(-1); 
		}
	}
	
	/**
	 * Elects an operator with this id.
	 * 
	 * @param id
	 */
	public void electOperator(int id) {
		operatorID = Integer.valueOf(id); 
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
		
		if (!occupant123IDs.isEmpty()) {
			// Priority 1 : zone 2 - inside the chambers
			return occupant123IDs;
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
	public void checkOccupant123IDs() {

		Iterator<Integer> i = occupant123IDs.iterator();
		while (i.hasNext()) {
			int id = i.next();

			if (getZoneOccupants(1).contains(id)
				|| getZoneOccupants(2).contains(id)
				|| getZoneOccupants(3).contains(id)) {
				// If this person is not physically in zone 1, 2, or 3, remove his id
				continue;
			}
			else
				occupant123IDs.remove(id);
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
		// If the airlock already has an existing operator
		else {
			// Check to see if he's still inside or has left the airlock
			if (!isInAnyZones(operatorID)) {
				// Remove this operator
				operatorID = Integer.valueOf(-1);
				// Choose a pool of candidates from a particular zone
				electAnOperator(getOperatorPool());
			}
		}
	}

	/**
	 * Adds airlock cycle time.
	 *
	 * @param time cycle time (millisols)
	 * @return The the time consumed
	 */
	public void addTime(double time) {
		
		if (AirlockState.PRESSURIZED == airlockState) {
			setState(AirlockState.DEPRESSURIZING);
		}
		
		else if (AirlockState.DEPRESSURIZED == airlockState) {
			setState(AirlockState.PRESSURIZING);
		}
		
		cycleAir(time);
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
	 * Gets the remaining airlock cycle time.
	 *
	 * @return time (millisols)
	 */
	public abstract double getRemainingCycleTime();
	
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
			occupant123IDs.remove(id);
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
		return occupant123IDs.contains((Integer)p.getIdentifier());
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

	/**
	 * Claims a new pos in a zone.
	 * 
	 * @param zone
	 * @param pos
	 * @param p
	 * @return
	 */
    public abstract boolean claim(AirlockZone zone, LocalPosition pos, Person p);

    /**
     * Vacates a zone.
     * 
     * @param zone
     * @param p
     * @return
     */
	public abstract boolean vacate(AirlockZone zone, Person p);

	/**
	 * Is the person in this zone ?
	 * 
	 * @param p
	 * @param zone
	 * @return
	 */
	public abstract boolean isInZone(Person p, AirlockZone zone);
	
	/**
	 * Gets a collection of occupants' ids.
	 *
	 * @return
	 */
	public Set<Integer> getOccupants123() {
		return occupant123IDs;
	}

	/**
	 * Is this person's id in any zones (0 to 4) ?
	 * 
	 * @param id
	 * @return
	 */
	public boolean isInAnyZones(int id) {
		return (occupant123IDs.contains(id)
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
		for (Integer id: occupant123IDs) {
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
				.filter(p -> (p.getSuit() == null))
				.collect(Collectors.toSet());
	}

	/**
	 * Checks if the airlock is full (numbers of occupants in zone 1, 2 and 3).
	 *
	 * @return
	 */
	public boolean isFull() {
		return getNumOccupant123() >= capacity;
	}
	
	/**
	 * Gets the number of occupants currently inside the airlock zone 1, 2, and 3.
	 *
	 * @return the number of occupants in zone 1, 2, and 3.
	 */
	public abstract int getNumInside();

	/**
	 * Gets the number of occupants in zone 1, 2, and 3.
	 *
	 * @return the number of occupants in zone 1, 2, and 3.
	 */
	public int getNumOccupant123() {
		return occupant123IDs.size();
	}
	
	/**
	 * Gets the number of empty slots.
	 *
	 * @return the number of empty slots
	 */
	public int getNumEmptied() {
		return capacity - occupant123IDs.size(); 
	}

	/**
	 * Checks if all 4 chambers in zone 2 are full.
	 *
	 * @return
	 */
	public abstract boolean areAll4ChambersFull();

	/**
	 * Checks if there is no occupants inside the airlock in Zone 1, 2 and 3.
	 *
	 * @return true if the airlock is empty
	 */
	public boolean isEmpty() {
		return occupant123IDs.isEmpty();
	}

	/**
	 * Checks if there is an empty slot left in Zone 1, 2 and 3.
	 * Note: can only use this method before ingressing through outer door
	 * or egressing through the inner door. 
	 * @return true if there is space
	 */
	public boolean hasSpace() {
		return getNumInside() < capacity;
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
	 * Gets the current mode of the airlock.
	 *
	 * @return the airlockMode.
	 */
	public AirlockMode getAirlockMode() {
		return airlockMode;
	}

	/**
	 * Sets the mode of the airlock.
	 *
	 * @param airlockMode the airlock mode.
	 */
	public void setAirlockMode(AirlockMode airlockMode) {
		this.airlockMode = airlockMode;
	}
	
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
	public static void initializeInstances(UnitManager um, MarsSurface ms, MasterClock masterClock) {
		unitManager = um;
		marsSurface = ms;
		clock = masterClock;
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		occupant123IDs.clear();
		occupant123IDs = null;
		awaitingInnerDoor.clear();
		awaitingInnerDoor = null;
		awaitingOuterDoor.clear();
		awaitingOuterDoor = null;
		airlockState = null;
	}
}

/*
 * Mars Simulation Project
 * ExitAirlock.java
 * @date 2024-11-30
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.ClassicAirlock;
import com.mars_sim.core.equipment.EVASuit;
import com.mars_sim.core.equipment.EVASuitUtil;
import com.mars_sim.core.equipment.EquipmentOwner;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionStatus;
import com.mars_sim.core.person.ai.mission.VehicleMission;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.structure.Airlock;
import com.mars_sim.core.structure.Airlock.AirlockMode;
import com.mars_sim.core.structure.AirlockType;
import com.mars_sim.core.structure.AirlockZone;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * The ExitAirlock class is a Task for EVA egress, namely, exiting an airlock of a settlement or vehicle
 * in order to perform an EVA operation outside.
 */
public class ExitAirlock extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(ExitAirlock.class.getName());


	/** Task name */
	private static final String NAME = Msg.getString("Task.description.exitAirlock"); //$NON-NLS-1$
	
	private static final String CANT_DON_SUIT = "Can't don an EVA suit - ";
	private static final String TO_REQUEST_EGRESS = " to request egress"; 
	private static final String TO_PRESSURIZE_CHAMBER = " to pressurize chamber.";
	private static final String TO_WALK_TO_CHAMBER = " to walk to a chamber.";
	private static final String TRIED_TO_STEP_THRU_INNER_DOOR = "Tried to step through inner door"; 
	private static final String PREBREATH_HALF_DONE = "Other occupant(s) already pre-breathed half-way thru.";
	private static final String PREBREATH_ONE_QUARTER_DONE = "Other occupant(s) already pre-breathed a quarter of time.";
	private static final String PREBREATH_THREE_QUARTERS_DONE = "Other occupant(s) already pre-breathed 3/4 quarters of time.";
	private static final String RESERVATION_NOT_MADE = "Reservation not made.";
	private static final String NOT_NOMINALLY_FIT = "Not nominally fit";
	private static final String NOT_EVA_FIT = "Not EVA fit";
	private static final String INNER_DOOR_LOCKED = "Inner door was locked.";
	private static final String ALL_CHAMBERS_OCCUPIED = "All chambers occupied.";
	private static final String COULDNT_WALK_TO = "Couldn't walk. ";
	private static final String COULDNT_ENTER = "Couldn't enter ";
	
    /** The minimum performance needed. */
	private static final double MIN_PERFORMANCE = 0.05;
	
	/** Task phases. */
	private static final TaskPhase REQUEST_EGRESS = new TaskPhase(
		Msg.getString("Task.phase.requestEgress")); //$NON-NLS-1$
	private static final TaskPhase PRESSURIZE_CHAMBER = new TaskPhase(
		Msg.getString("Task.phase.pressurizeChamber")); //$NON-NLS-1$
	private static final TaskPhase STEP_THRU_INNER_DOOR = new TaskPhase(
		Msg.getString("Task.phase.stepThruInnerDoor")); //$NON-NLS-1$
	private static final TaskPhase WALK_TO_CHAMBER = new TaskPhase(
		Msg.getString("Task.phase.walkToChamber")); //$NON-NLS-1$
	private static final TaskPhase DON_EVA_SUIT = new TaskPhase(
		Msg.getString("Task.phase.donEVASuit")); //$NON-NLS-1$
	private static final TaskPhase PREBREATHE = new TaskPhase(
		Msg.getString("Task.phase.prebreathe")); //$NON-NLS-1$
	private static final TaskPhase DEPRESSURIZE_CHAMBER = new TaskPhase(
		Msg.getString("Task.phase.depressurizeChamber")); //$NON-NLS-1$
	private static final TaskPhase LEAVE_AIRLOCK = new TaskPhase(
		Msg.getString("Task.phase.leaveAirlock")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .1D;
	/** The standard EVA suit donning time. */
	private static final double SUIT_DONNING_TIME = 25;
	/** The standard time for each task phase. */
	private static final double STANDARD_TIME = 0.5;

	private static final MissionStatus NO_EVA_SUITS = new MissionStatus("Mission.status.noEVASuits"); // "No good Eva Suit"

	
	// Data members
	/** Is this a building airlock in a settlement? */
	private boolean inSettlement = false;
	
	/** Is the airlock's EVA building attached to astronomy observatory ? */
	private boolean isObservatoryAttached = false;
	
	/** The remaining time in donning the EVA suit. */
	private double remainingDonningTime;
	/** The time accumulatedTime for a task phase. */
	private double accumulatedTime;
	
	/** The airlock to be used. */
	private Airlock airlock;

	/**
	 * Constructor.
	 *
	 * @param person  the person to perform the task
	 * @param airlock the airlock to use.
	 */
	public ExitAirlock(Person person, Airlock airlock) {
		super(NAME, person, false, false, STRESS_MODIFIER, SkillType.EVA_OPERATIONS, 100);

		this.airlock = airlock;

		if (airlock.getAirlockType() == AirlockType.BUILDING_AIRLOCK) {
			inSettlement = true;
			Building airlockBuilding = (Building)(airlock.getEntity());
			if (airlockBuilding.getSettlement().getBuildingManager().isObservatoryAttached(airlockBuilding)) {
				isObservatoryAttached = true;
			}
		}

		// Initialize data members
		setDescription(Msg.getString("Task.description.exitAirlock.detail", airlock.getEntityName())); // $NON-NLS-1$
		// Initialize task phase
		setPhase(REQUEST_EGRESS);

		logger.fine(person, 4_000,
				"Starting the EVA egress in " + airlock.getEntityName() + ".");
	}

	/**
	 * Is this Task interruptable? This Task can not be interrupted.
	 * @return Returns false by default
	 */
	@Override
	public boolean isInterruptable() {
        return false;
    }

	/**
	 * Performs the method mapped to the task's current phase.
	 *
	 * @param time the amount of time (millisols) the phase is to be performed.
	 * @return the remaining time (millisols) after the phase has been performed.
	 */
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");

		} else if (REQUEST_EGRESS.equals(getPhase())) {
			return requestEgress(time);
		} else if (PRESSURIZE_CHAMBER.equals(getPhase())) {
			return pressurizeChamber(time);
		} else if (STEP_THRU_INNER_DOOR.equals(getPhase())) {
			return stepThruInnerDoor(time);
		} else if (WALK_TO_CHAMBER.equals(getPhase())) {
			return walkToChamber(time);
		} else if (DON_EVA_SUIT.equals(getPhase())) {
			return donEVASuit(time);
		} else if (PREBREATHE.equals(getPhase())) {
			return prebreathe(time);
		} else if (DEPRESSURIZE_CHAMBER.equals(getPhase())) {
			return depressurizeChamber(time);
		} else if (LEAVE_AIRLOCK.equals(getPhase())) {
			return leaveAirlock(time);
		} else {
			return time;
		}
	}

	/**
	 * Transitions the person into a particular zone.
	 *
	 * @param newZone the destination
	 * @return true if the transition is successful
	 */
	private boolean transitionTo(AirlockZone newZone) {
		
		// Is the person already in this zone ?
		if (isInZone(newZone)) {
			return true;
		}
		
		// For egress, a person would first arrive at zone 0.
		// At zone 0, he progresses via the inner/interior door onto zone 1.
		// At zone 1, he's waiting for an empty chamber to be available.
		// At zone 2, he's at the airlock chamber donning his EVA suit.
		// At zone 3, he's waiting for the outer/exterior door to open.
		// At zone 4, he just stepped outside onto the surface of Mars.
		
		// the previous zone #  a lower numeric #
		int previousZone = newZone.ordinal() - 1;
		LocalPosition newPos = fetchNewPos(newZone);
		logger.fine(person, "Just fetched " + newPos + " in zone " + newZone.ordinal() + ".");
		if (newPos != null && airlock.claim(newZone, newPos, person)) {
			logger.fine(person, "Just claimed zone " + newZone.ordinal() + ".");
			if (previousZone >= 0) {
				if (airlock.vacate(AirlockZone.convert2Zone(previousZone), person)) {
					logger.fine(person, "Just vacated zone " + previousZone + ".");
					return moveThere(newPos, newZone);
				}
				else
					return false;
			}
			else {
				// Just arrived at zone 0. No need to vacate any zone.
				return moveThere(newPos, newZone);
			}
		}
			
		return false;
	}


	/**
	 * Checks if the person is already in a particular zone.
	 *
	 * @param zone the zone the person is at
	 * @return true if the person is a particular zone
	 */
	private boolean isInZone(AirlockZone zone) {
		return airlock.isInZone(person, zone);
	}

	/**
	 * Obtains a new position in the target zone.
	 *
	 * @param zone the destination zone
	 * @return LocalPosition a new location
	 */
	private LocalPosition fetchNewPos(AirlockZone zone) {
		LocalPosition newPos = null;

		if (zone == AirlockZone.ZONE_0) {
			newPos = airlock.getAvailableInteriorPosition(false);
		}
		else if (zone == AirlockZone.ZONE_1) {
			newPos = airlock.getAvailableInteriorPosition(true);
		}
		else if (zone == AirlockZone.ZONE_2) {
			newPos = airlock.getAvailableAirlockPosition();
		}
		else if (zone == AirlockZone.ZONE_3) {
			newPos = airlock.getAvailableExteriorPosition(true);
		}
		else if (zone == AirlockZone.ZONE_4) {
			newPos = airlock.getAvailableExteriorPosition(false);
		}

		return newPos;
	}

	/**
	 * Moves the person to a particular zone.
	 *
	 * @param newPos the target position in that zone
	 * @param newZone the destination zone
	 */
	private boolean moveThere(LocalPosition newPos, AirlockZone newZone) {
		
		Building b = (Building) airlock.getEntity();
		
		if (newZone == AirlockZone.ZONE_2) {	
			// Check if the person can walk to one of the 4 EVA chambers
			boolean canWalk = walkToEVASpot(b, newPos, false);
						
			if (canWalk) {
				// Convert the local activity spot to the settlement reference coordinate
				// Set the person's new position
				person.setPosition(newPos);
				
//				May add back for future testing: logger.log(person, Level.FINE, 4000, "Arrived at " + newPos.getShortFormat() + " in " + newZone + ".");
				return true;
			}
			else {
//				May add back for future testing: logger.log(person, Level.FINE, 4000, "Could not enter the chamber in airlock zone "  + newZone + ".");
				return false;
			}
		}

		else {
			// Set the person's new position
			person.setPosition(newPos);
			
//			May add back for future testing: logger.fine(person, 4000, "Arrived at "+ newPos.getShortFormat() + " in " + newZone + ".");
			return true;
		}
	}

	/**
	 * Checks if a person is EVA unfit.
	 *
	 * @return
	 */
	private boolean isEVAUnfit() {
		
		if (inSettlement) {
			if (isObservatoryAttached) {
				// Note: if the person is in an airlock next to the observatory 
				// sitting at an isolated and remote part of the settlement,
				// it will not check if he's physically fit to leave that place, or else
				// he may get stranded.
				return false;
			}
			
			// else need to go to the bottom to check fitness
		}
		else if (person.isInVehicle() && person.getVehicle().getSettlement() != null) {
			// if vehicle occupant is unfit and the vehicle is parked at settlement vicinity,
			// bypass checking for fitness since the person may just be too exhausted and
			// should be allowed to return home to recuperate.
			return false;
		}

		if (EVAOperation.isInEmergency(person)) {
	      	return false;
		}
		
		return !person.isEVAFit();
	}
	
	/**
	 * Checks if a person is nominally unfit.
	 *
	 * @return
	 */
	private boolean isNominallyUnfit() {
		
		if (inSettlement) {
			if (isObservatoryAttached) {
				// Note: if the person is in an airlock next to the observatory 
				// sitting at an isolated and remote part of the settlement,
				// it will not check if he's physically fit to leave that place, or else
				// he may get stranded.
				return false;
			}
			
			// else need to go to the bottom to check fitness
		}
		else if (person.isInVehicle() && person.getVehicle().getSettlement() != null) {
			// if vehicle occupant is unfit and the vehicle is parked at settlement vicinity,
			// bypass checking for fitness since the person may just be too exhausted and
			// should be allowed to return home to recuperate.
			return false;
		}

		if (EVAOperation.isInEmergency(person)) {
	      	return false;
		}
		
		return !person.isNominallyFit();
	}
	
	
	/**
	 * Quits the egress.
	 *
	 * @param person the person of interest
	 * @param reason the reason for walking away
	 */
	private void walkAway(Person person, String reason) {
		// Reset accumulatedTime back to zero
		accumulatedTime = 0;

		clearDown();
		
//		May add back for future testing :logger.info(person, 16_000, reason);

		// Note: For person in a vehicle with high fatigue or hunger,
		// need to call clearAllTasks() to cause a person to quit the task
		// or else it gets stuck forever in the vehicle airlock
		person.getTaskManager().clearAllTasks(reason);
	}
	
	/**
	 * Is at least one occupants already half done with prebreathing ?
	 * 
	 * @return
	 */
	public boolean isOccupantHalfPrebreathed() {
		// Verify occupant's whereabout first
		airlock.checkOccupant123IDs();
		
		List<Integer> list = new ArrayList<>(airlock.getOccupants123());
		for (int id : list) {
			Person p = airlock.getPersonByID(id);
			// Filter checking on this person doing the ExitAirlock
			// Check on others only
			if (p != person 
				// Is that person wearing a suit	
				&& p.getSuit() != null
				// Is that person already prebreating half way through
				&& p.getPhysicalCondition().isAtLeastHalfDonePrebreathing()) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Is at least one occupants already a quarter done prebreathing ?
	 * 
	 * @return
	 */
	public boolean isOccupantAQuarterPrebreathed() {
		// Verify occupant's whereabout first
		airlock.checkOccupant123IDs();

		List<Integer> list = new ArrayList<>(airlock.getOccupants123());
		for (int id : list) {
			Person p = airlock.getPersonByID(id);
			// Filter checking on this person doing the ExitAirlock
			// Check on others only
			if (p != person 
				// Is that person wearing a suit	
				&& p.getSuit() != null
				// Is that person already prebreating a quarter way through
				&& p.getPhysicalCondition().isAtLeastAQuarterDonePrebreathing()) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Is at least one occupants already 3/4 quarters done prebreathing ?
	 * 
	 * @return
	 */
	public boolean isOccupant3QuartersPrebreathed() {
		// Verify occupant's whereabout first
		airlock.checkOccupant123IDs();
		
		List<Integer> list = new ArrayList<>(airlock.getOccupants123());
		for (int id : list) {
			Person p = airlock.getPersonByID(id);
			// Filter checking on this person doing the ExitAirlock
			// Check on others only
			if (p != person 
				// Is that person wearing a suit	
				&& p.getSuit() != null
				// Is that person already prebreating 3/4 way through
				&& p.getPhysicalCondition().isAtLeast3QuartersDonePrebreathing()) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Requests the entry of the airlock.
	 *
	 * @param time the pulse
	 * @return the remaining time
	 */
	private double requestEgress(double time) {
				
		// Accumulate work for this task phase
		accumulatedTime += time;

		boolean canProceed = false;
		
		logger.fine(person, 4_000,
				"Requesting EVA egress in " + airlock.getEntityName() + ".");
		
		// If a person is in a vehicle, not needed of checking for reservation
		if (inSettlement && !airlock.addReservation(person.getIdentifier())) {
			walkAway(person, RESERVATION_NOT_MADE 
					+ " Current task: " + person.getTaskDescription() + ".");
			return 0;
		}

		// Note: no longer use isEVAFit() to check for fitness
		
		if (inSettlement && isEVAUnfit()) {
			walkAway(person, NOT_EVA_FIT + TO_REQUEST_EGRESS + ". Current task: " 
					+ person.getTaskDescription() + ".");
			return 0;
		}
		
		if (person.isOutside()) {
			walkAway(person, "Already outside, not supposed " + TO_REQUEST_EGRESS 
					+ ". Current task: " + person.getTaskDescription() + ".");
			// Reset accumulatedTime back to zero
			accumulatedTime = 0;
			return 0;
		}

		if (isOccupantAQuarterPrebreathed()) {
			walkAway(person, "Couldn't egress in " + airlock.getEntityName() + ". " + PREBREATH_ONE_QUARTER_DONE + 
					" Current task: " + person.getTaskDescription() + ".");
			return 0;
		}

		// NOTE: don't need to allow the airlock to transition its state yet.

		// Activates airlock first to check for occupant ids and operator
		// before calling other checks
		if (!airlock.isActivated()) {
			// Only the airlock operator may activate the airlock
			airlock.setActivated(true);
		}
		
		if (airlock.isOperator(id)) {
			// Command the airlock state to be transitioned to "pressurized"
			airlock.setTransitioning(true);
			
			airlock.setAirlockMode(AirlockMode.EGRESS);
		}
		
		if (inSettlement) {
			if (!airlock.addAwaitingInnerDoor(id)) {
				walkAway(person, "Couldn't egress in " + airlock.getEntityName() + ". Couldn't get a spot at the inner door of " 
						+ airlock.getEntity().toString() + ".");
				return 0;
			}

			if (airlock.isFull()) {
				walkAway(person, "Couldn't egress in " + airlock.getEntityName() + ". " + ALL_CHAMBERS_OCCUPIED
						+ " Current task: " + person.getTaskDescription() + ".");
				return 0;
			}
				
			if (transitionTo(AirlockZone.ZONE_0) && (!airlock.isInnerDoorLocked() || airlock.isEmpty())) {
				// The inner door will stay locked if the chamber is NOT pressurized
				canProceed = true;
				logger.fine(person, 4_000, "Just transitioned into zone 0.");
			}
			
			else if (airlock.isEmpty()) {
				// If the airlock is empty, it means no one is using it
				logger.fine(person, 60_000,
						"No one is at " + airlock.getEntityName() + ".");
				// Go to the next phase in order for the inner door to be unlocked.
				// After the pressurization has finished, it should be open.
				canProceed = true;
			}
		}

		else {

			if (airlock.addAwaitingInnerDoor(id) || !airlock.isInnerDoorLocked() || airlock.isEmpty()) {
				canProceed = true;
			}
			else {
				walkAway(person, "Requesting egress. can't get thru " 
						+ airlock.getEntity().toString() + "'s inner door.");
				return 0;
			}
		}

		if (canProceed && accumulatedTime > STANDARD_TIME * time) {
			// Reset accumulatedTime back to zero
			accumulatedTime -= STANDARD_TIME * time;

			if (airlock.isPressurized() && !airlock.isInnerDoorLocked()) {
				// If airlock has already been pressurized,
				// then it's ready for entry

				logger.fine(person, 4_000,
						"Chamber already pressurized for entry in "
								+ airlock.getEntityName() + ".");

				// Skip PRESSURIZE_CHAMBER phase and go to the ENTER_AIRLOCK phase
				setPhase(STEP_THRU_INNER_DOOR);
			}

			else {
				// since it's not pressurized, will need to pressurize the chamber first

				if (airlock.isOperator(id)) {
					// Command the airlock state to be transitioned to "pressurized"
					airlock.setTransitioning(true);
					
					logger.fine(person, 4_000, "Ready to pressurize the chamber.");

					if (!airlock.isPressurized() || !airlock.isPressurizing()) {
						// Get ready for pressurization
						setPhase(PRESSURIZE_CHAMBER);
					}
				}
			}
		}

		return 0;
	}

	/**
	 * Pressurizes the chamber.
	 *
	 * @param time
	 * @return the remaining time
	 */
	private double pressurizeChamber(double time) {
		// Accumulate work for this task phase
		accumulatedTime += time;

		boolean canProceed = false;

		// Activates airlock first to check for occupant ids and operator
		// before calling other checks
		if (!airlock.isActivated()) {
			// Only the airlock operator may activate the airlock
			airlock.setActivated(true);
		}
			
		if (inSettlement && isEVAUnfit()) {
			walkAway(person, NOT_EVA_FIT + TO_PRESSURIZE_CHAMBER);
			return 0;
		}
		
		if (isOccupantAQuarterPrebreathed()) {
			walkAway(person, "Can't egress. " + PREBREATH_ONE_QUARTER_DONE);
			return 0;
		}
		
		if (airlock.isPressurized() && !airlock.isInnerDoorLocked()) {

			logger.fine(person, 4_000,
					"The chamber already pressurized in "
				+ airlock.getEntityName() + ".");

			canProceed = true;
		}

		else if (airlock.isPressurizing()) {
			// just wait for pressurizing to finish
		}

		else {
			
			if (airlock.isOperator(id)) {
				// Command the airlock state to be transitioned to "pressurized"
				airlock.setTransitioning(true);	
			}
		}

		
		if (canProceed && accumulatedTime > STANDARD_TIME * time) {
			// Reset accumulatedTime back to zero
			accumulatedTime -= STANDARD_TIME * time;
			// Add experience
			addExperience(time);
	
			setPhase(STEP_THRU_INNER_DOOR);
			
			if (airlock.isEmpty())
				airlock.setAirlockMode(AirlockMode.NOT_IN_USE);
			else
				airlock.setAirlockMode(AirlockMode.EGRESS);
		}
		
		return 0;
	}

	/**
	 * Steps through the inner door to enter into the airlock.
	 *
	 * @param time
	 * @return the remaining time
	 */
	private double stepThruInnerDoor(double time) {
		// Accumulate work for this task phase
		accumulatedTime += time;

		boolean canProceed = false;

		if (isEVAUnfit()) {
			walkAway(person, TRIED_TO_STEP_THRU_INNER_DOOR + ". " + NOT_EVA_FIT + ".");
			return 0;
		}
		
		if (isOccupantHalfPrebreathed()) {
			walkAway(person, TRIED_TO_STEP_THRU_INNER_DOOR + ". " + PREBREATH_HALF_DONE);
			return 0;
		}
		
		if (!airlock.isPressurized()) {
			// Go back to the previous phase
			setPhase(PRESSURIZE_CHAMBER);
			// Reset accumulatedTime back to zero
			accumulatedTime = 0;
			return time * .75;
		}

		if (inSettlement) {

			if (airlock.isInnerDoorLocked()) {
				walkAway(person, TRIED_TO_STEP_THRU_INNER_DOOR + " but " + INNER_DOOR_LOCKED);
				return 0;
			}

			if (airlock.isFull()) {
				walkAway(person, TRIED_TO_STEP_THRU_INNER_DOOR + ". " + ALL_CHAMBERS_OCCUPIED);
				return 0;
			}
			
			if (!airlock.inAirlock(person) && airlock.enterAirlock(person, id, true)) {
				canProceed = transitionTo(AirlockZone.ZONE_1);
				logger.fine(person, 20000L, "Just transitioned into zone 1.");
			}
			else if (isInZone(AirlockZone.ZONE_1) || isInZone(AirlockZone.ZONE_2)) {
				// True if the person is already there from previous frame
				canProceed = true;
			}
            else {
				logger.log(person, Level.WARNING, 4_000,
						"Not in zone 2 or 3 in " + airlock.getEntity() + ".");
				
				walkAway(person, TRIED_TO_STEP_THRU_INNER_DOOR + ". " + "Not in right zone.");
				
				// The outer door is locked probably because of not being 
				// at the correct airlock state. Go back to the previous task phase
//				setPhase(REQUEST_EGRESS);
				// Reset accumulatedTime back to zero 
				accumulatedTime = 0;
				
				return 0;
            }
		}

		else {
			if (!airlock.isInnerDoorLocked()) {

				if (!airlock.inAirlock(person)) {
					canProceed = airlock.enterAirlock(person, id, true);
				} else // the person is already inside the airlock from previous cycle
					canProceed = true;
			}
			else {
				walkAway(person, TRIED_TO_STEP_THRU_INNER_DOOR + " but " 
						+ airlock.getEntity().toString() + " inner door locked");
				return 0;
			}
		}

		if (canProceed && accumulatedTime > STANDARD_TIME * time) {
			// Reset accumulatedTime back to zero
			accumulatedTime -= STANDARD_TIME * time;
			
			// Remove person from reservation map
			if (inSettlement)
				airlock.removeReservation(person.getIdentifier());

			logger.fine(person, 4_000,
					"Just entered through the inner door into "
					+ airlock.getEntity().toString() + ".");

			airlock.setAirlockMode(AirlockMode.EGRESS);
			
			// Add experience
			addExperience(time);

			setPhase(WALK_TO_CHAMBER);
		}

		return 0;
	}

	/**
	 * Walks to the chamber.
	 *
	 * @param time
	 * @return canProceed
	 */
	private double walkToChamber(double time) {
		// Accumulate work for this task phase
		accumulatedTime += time;

		boolean canProceed = false;

		logger.fine(person, 4_000, "Walking to a chamber in " + airlock.getEntityName() + ".");
		
		// Activates airlock first to check for occupant ids and operator
		// before calling other checks
		if (!airlock.isActivated()) {
			// Only the airlock operator may activate the airlock
			airlock.setActivated(true);
		}

		if (isEVAUnfit()) {
			walkAway(person, NOT_EVA_FIT + TO_WALK_TO_CHAMBER);
			return 0;
		}
		
		if (isOccupant3QuartersPrebreathed()) {
			walkAway(person, "Can't walk to chamber. " + PREBREATH_THREE_QUARTERS_DONE);
			return 0;
		}
		
		if (!airlock.isPressurized()) {
			// Go back to the previous phase
			setPhase(PRESSURIZE_CHAMBER);
			return time * .75;
		}

		
		if (inSettlement) {

			if (!isInZone(AirlockZone.ZONE_2) && airlock.isFull()) {
				walkAway(person, "Can't walk to chamber. " + ALL_CHAMBERS_OCCUPIED);
				return 0;
			}
			
			if (transitionTo(AirlockZone.ZONE_2)) {
				canProceed = true;
				logger.fine(person, 20000L, "Just transitioned into zone 2.");
			}

			else {
				setPhase(STEP_THRU_INNER_DOOR);
				// Reset accumulatedTime back to zero
				accumulatedTime = 0;
				return 0;
			}
		}

		else {
 			canProceed = true;
		}

		if (canProceed && accumulatedTime > STANDARD_TIME * time) {
			// Reset accumulatedTime back to zero
			accumulatedTime -= STANDARD_TIME * time;
			// Remove the reservation of this chamber
			airlock.removeReservation(person.getIdentifier());
			
			if (airlock.isOperator(id)) {
				// Elect an operator to handle this task
				if (!airlock.isPressurized() || !airlock.isPressurizing()) {
					// Get ready for pressurization
					setPhase(PRESSURIZE_CHAMBER);
				}
			}

			if (airlock.isPressurized()) {
				logger.fine(person, 4_000,
						"Chamber already pressurized for entry in " + airlock.getEntityName() + ".");

		 		// Reset the suit donning time
				remainingDonningTime = SUIT_DONNING_TIME + RandomUtil.getRandomInt(-5, 5);

				setPhase(DON_EVA_SUIT);
			}

			// Add experience
			addExperience(time);
		}


		return 0;
	}

	/**
	 * Selects an EVA suit and don it.
	 *
	 * @param time the amount of time to perform the task phase.
	 * @return the remaining time
	 */
	private double donEVASuit(double time) {
		
		boolean canProceed = false;

		// Gets the suit instance
		EVASuit suit = person.getSuit();
		
		if (suit == null) {
			// If the person hasn't donned the suit yet
			
			if (isNominallyUnfit()) {
				// Doff the suit, get back the garment and thermal bottle
				EVASuitUtil.checkIn(person, airlock.getEntity(), inSettlement, true);
				walkAway(person, NOT_NOMINALLY_FIT + " to don an EVA suit.");
				return 0;
			}
	
			if (isOccupant3QuartersPrebreathed()) {
				// Doff the suit, get back the garment and thermal bottle
				EVASuitUtil.checkIn(person, airlock.getEntity(), inSettlement, true);
				walkAway(person, CANT_DON_SUIT + PREBREATH_THREE_QUARTERS_DONE);
				return 0;
			}
			
			if (!airlock.isPressurized()) {
				// Go back to the previous phase
				setPhase(PRESSURIZE_CHAMBER);
				return time * .75;
			}

			EquipmentOwner housing = null;
			
			if (inSettlement)
				housing = ((Building)airlock.getEntity()).getSettlement();
			else
				housing = (Vehicle)airlock.getEntity();
	
			// 0. Drop off the thermal bottle 
			person.dropOffThermalBottle();
			// 1. Get a good EVA suit's instance from entity inventory
			suit = EVASuitUtil.findEVASuitWithResources(housing, person);
	
			if (suit == null) {
				logger.warning(person, 4_000,
						"Could not find a working EVA suit during " + DON_EVA_SUIT + " in "
						+ airlock.getEntityName());

				// Q: why would a person be allowed to initiate this task in the first place 
				//    if there is no known working EVA suit ? Unless there is a sync issue 
				// Q: how do we make an EVA suit pre-assigned to a person prior to starting 
				//    the process of EVA. 
				walkAway(person, "No EVA suit available.");
				
				return 0;
			}
			
			// if a person hasn't donned the suit yet
			// 0. Remove garment and put on pressure suit
			if (person.unwearGarment(housing)) {
				person.wearPressureSuit(housing);
			}
			
			// 2. Transfer the EVA suit from entity to person
			boolean success = suit.transfer(person);
			
			if (success) {
				// 3. Set the person as the owner
				suit.setRegisteredOwner(person);
							
				// 5. Loads the resources into the EVA suit
				if (suit.loadResources(housing) < 0.9D) {
					logger.warning(suit, "Not fully loaded.");
				}
	
				remainingDonningTime -= time;
			}
			else {
				logger.warning(person, 4_000,
						"Could not take transfer of " + suit.getName() + ".");
				
				walkAway(person, "Could not transfer EVA suit.");
				
				return 0;
			}

		}

		else { // the person already have the suit in his inventory
			
			remainingDonningTime -= time;

			if (remainingDonningTime <= 0) {
				canProceed = true;
			}
		}

		if (canProceed) {

			// Add experience
			addExperience(time);

			logger.log(person, Level.FINER, 4_000,
					"Donned the EVA suit and got ready to do pre-breathing.");
			// Reset the prebreathing time counter to max
			person.getPhysicalCondition().resetRemainingPrebreathingTime();

			setPhase(PREBREATHE);
		}
		
		return 0;
	}

	/**
	 * Pre-breathes in the EVA suit to prevent the potential occurrence of incapacitating decompression sickness (DCS).
	 * Prebreathing reduces the nitrogen content in the astronaut's body which prevents the formation of nitrogen
	 * bubbles in body tissues when the atmospheric pressure is reduced.
	 *
	 * @param time the pulse
	 * @return the remaining time
	 */
	private double prebreathe(double time) {

		if (isNominallyUnfit()) {
			// Doff the suit, get back the garment and thermal bottle
			EVASuitUtil.checkIn(person, airlock.getEntity(), inSettlement, true);
			walkAway(person, NOT_NOMINALLY_FIT + " to prebreath.");
			return 0;
		}
		
		boolean canProceed = false;

		PhysicalCondition pc = person.getPhysicalCondition();
		
		// Continue pre-breathing
		pc.reduceRemainingPrebreathingTime(time);

		if (pc.isDonePrebreathing()) {
			logger.fine(person,  4_000, "Done pre-breathing.");
			
			canProceed = true;
		}
		
		if (canProceed) {

			if (!airlock.isActivated()) {
				// Only the airlock operator may activate the airlock
				airlock.setActivated(true);
			}
			
			if (airlock.isOperator(id)) {
				// Command the airlock state to be transitioned to "depressurized"
				airlock.setTransitioning(true);
				// Get ready for depressurization
				setPhase(DEPRESSURIZE_CHAMBER);
			}

			if (airlock.isDepressurized()) {
				logger.fine(person, 4_000,
						"Chamber already depressurized for exit in " + airlock.getEntityName() + ".");

				setPhase(LEAVE_AIRLOCK);
			}

			// Add experience
			addExperience(time);
		}

		return 0;
	}

	/**
	 * Depressurizes the chamber.
	 *
	 * @param time the pulse
	 * @return the remaining time
	 */
	private double depressurizeChamber(double time) {
		// Accumulate work for this task phase
		accumulatedTime += time;

		boolean canProceed = false;
	
		if (isNominallyUnfit()) {
			// Get back the garment and thermal bottle
			EVASuitUtil.checkIn(person, airlock.getEntity(), inSettlement, true);
			
			walkAway(person, NOT_NOMINALLY_FIT + " to depressurize chamber.");
			return 0;
		}
		
//		if (isOccupantHalfPrebreathed()) {
//			// just wait for others to finish prebreathing
//		}		
		
		else if (airlock.isDepressurizing()) {
			// just wait for depressurization to finish
		}
		
		else if (airlock.isPressurizing()) {
			// just wait for pressurization to finish
		}
		
		else if (airlock.isPressurized()) {

			// Gets a set of settlers without wearing EVA suits
			Set<Person> noEVASuit = airlock.noEVASuit();

			if (noEVASuit.isEmpty()) {
				// Great that everyone has an EVA suit on
				
				// Now just wait for the depressurization to complete
				if (!airlock.isActivated()) {
					// Only the airlock operator may activate the airlock
					airlock.setActivated(true);
				}
				
				if (airlock.hasNoOperator()) {
					airlock.electOperator(id);
				}
				
				if (airlock.isOperator(id)) {
					// Command the airlock state to be transitioned to "depressurized"
					airlock.setTransitioning(true);
				}
			}
			else {
				for (Person p: noEVASuit) {
					
					// How to handle each of the following types of occupants:
					//
					// Those egressing :
					// 1. have suit. still prebreathing.
					// 2. have suit. done prebreathing.
					// 3. have no suit. not yet prebreathing.
					//
					// Those ingressing :
					// 4. still doffing suit. 
					// 5. just walk in. not doffing yet. 
					// 6. already doff the suit. 
					// 7. still cleaning up. 
					//					
					
					// Get back the garment and thermal bottle
					EVASuitUtil.checkIn(p, airlock.getEntity(), inSettlement, true);
					
					// Without an EVA suit, one needs to leave the airlock 
					// while the airlock is still being pressurized 
					walkAway(p, "Haven't donned EVA Suit.");
					
					logger.warning(p, 4_000,
							"Can't egree. Airlock about to be depressurized. Not enough time to don EVA suit.");
				}
			}
		}
		
		else if (airlock.isDepressurized()) {

			logger.fine(person, 4_000,
					"Chamber already depressurized for exit in "
				+ airlock.getEntityName() + ".");

			if (!inSettlement || transitionTo(AirlockZone.ZONE_3)) {
				// If in vehicle, it doesn't need to transition to zone 3
				canProceed = true;
				logger.fine(person, 20000L, "Just transitioned into zone 3.");
			}
			
		}
		
		if (canProceed && accumulatedTime > STANDARD_TIME * time) {
			// Reset accumulatedTime back to zero
			accumulatedTime -= STANDARD_TIME * time;
			
			// Add experience
			addExperience(time);

			setPhase(LEAVE_AIRLOCK);
		}
		
		return 0;
	}

	/**
	 * Departs the chamber through the outer door of the airlock.
	 *
	 * @param time the pulse
	 * @return the remaining time
	 */
	private double leaveAirlock(double time) {
		// Accumulate work for this task phase
		accumulatedTime += time;

		boolean canProceed = false;

		if (inSettlement) {

			if (airlock.isOuterDoorLocked()) {
				// Go back to previous task phase
				setPhase(DEPRESSURIZE_CHAMBER);
				// Reset accumulatedTime back to zero
				accumulatedTime = 0;
				return time / 2.0;
			}
			
			if (airlock.inAirlock(person)) {
				canProceed = airlock.exitAirlock(person, id, true);
			}
			
			else if (transitionTo(AirlockZone.ZONE_4)) {
				// True if the person is already there from previous frame
				canProceed = true;
				logger.fine(person, 20000L, "Just transitioned into zone 4.");
			}
		}

		else {

			if (airlock.inAirlock(person)) {
				canProceed = airlock.exitAirlock(person, id, true);
			}
			else {
				// True if the person has already exit the airlock
				canProceed = true;
			}
		}

		if (canProceed && accumulatedTime > STANDARD_TIME * time) {
			// Reset accumulatedTime back to zero
			accumulatedTime -= STANDARD_TIME * time;
			
			if (inSettlement) {
				// Remove the position at zone 4 before ending the task
				if (airlock.vacate(AirlockZone.ZONE_4, person)) {
					// Add experience
			 		addExperience(time);
		
					logger.fine(person, 4_000,
							"Leaving " + airlock.getEntityName() + ".");
		
					// This completes EVA egress from the airlock
					// End ExitAirlock task
					completeAirlockTask();
				}
				else {
					logger.fine(person, 4_000,
							"Can't vacate zone 4 at" + airlock.getEntityName() + ".");
				}
			}
			else {
				// Add experience
		 		addExperience(time);
	
				logger.fine(person, 4_000,
						"Leaving " + airlock.getEntityName() + ".");
	
				// This completes EVA egress from the airlock
				// End ExitAirlock task
				completeAirlockTask();
			}
		}

		return 0;
	}

	
	
	/**
	 * Removes the person from airlock and walk away and ends the airlock and walk tasks.
	 */
	public void completeAirlockTask() {
		
		clearDown();

		// Resets the pre-breath time
		person.getPhysicalCondition().resetRemainingPrebreathingTime();
		
		super.endTask();
	}

	/**
	 * Checks if an airlock is full.
	 * 
	 * @param airlock the airlock to be used
	 * @return
	 */
	public static boolean isFull(Airlock airlock) {
	
		if (airlock.isFull()) {
			return true;
		}
	
		return false;
	}
	
	/**
	 * Checks if a person can exit an airlock to do an EVA.
	 *
	 * @param person  the person exiting
	 * @param airlock the airlock to be used
	 * @return true if person can exit the entity
	 */
	public static boolean canExitAirlock(Person person, Airlock airlock) {
	
		if (airlock.isFull()) {
//			May add back for future testing :logger.info(person, 4_000, COULDNT_ENTER + airlock.getEntityName() + ". " + ALL_CHAMBERS_OCCUPIED);
			return false;
		}
		
		if (EVAOperation.isHungryAtMealTime(person, 20)) {
//			May add back for future testing :logger.info(person, 4_000, "Too close to starting meal time and is doubly hungry.");
			return false;
		}
		
		if (!person.getSettlement().getRationing().isAtEmergency()
				// Check if person is incapacitated.
			&& (person.getPerformanceRating() <= MIN_PERFORMANCE
				|| person.getPhysicalCondition().hasSeriousMedicalProblems())) {
			// May need to relocate the following code to a proper place
			
			// Prevent the logger statement below from being repeated multiple times
			logger.info(person, 4_000,
					"Could not exit the airlock from " + airlock.getEntityName()
					+ " due to crippling performance rating of " + person.getPerformanceRating() + ".");

			try {

				if (person.isInVehicle() 
						&& (person.getVehicle().isInSettlementVicinity()
							|| person.getVehicle().isInSettlement())) {
					Settlement settlement = person.getVehicle().getSettlement();

					logger.warning(person, 4_000, "Attempting a rescue operation in/near " + settlement.getName() + ".");  
					// Attempt a rescue operation
					person.rescueOperation((Rover) person.getVehicle(), settlement);
					// Note: rescueOperation() is more like a hack, rather than a legitimate way 
					// of transferring a person through the airlock into the settlement 
				}

			} catch (Exception e) {
				logger.severe(person, 4_000, "Could not get new action: ", e);
			}

			return false;
		}

		// Check if person is outside.
		if (person.isOutside()) {
			logger.severe(person, 4_000,
					COULDNT_ENTER + airlock.getEntityName() + ". Already outside and not inside.");
			return false;
		}

		else if (person.isInSettlement()) {

			EVASuit suit = EVASuitUtil.findRegisteredOrGoodEVASuit(person);

			if (suit != null) {
				// Reset counter
				airlock.resetCheckEVASuit();
				// EVA suit is available.
				return true;
			}
			
			// EVA suit is not available.
			logger.warning(person, 4_000,
					"Could not find a working EVA suit for egress in the settlement.");
			// Add to the counter
			airlock.addCheckEVASuit();
			// EVA suit is not available.
			return false;

		}

		else if (person.isInVehicle()) {

			EVASuit suit = person.getSuit();

			Vehicle v = person.getVehicle();
			
			if (suit != null) {
				// Reset counter
				airlock.resetCheckEVASuit();
				// EVA suit is available.
				return true;
			}
			else {
				suit = EVASuitUtil.findEVASuitFromVehicle(person, v);
			}
			
			if (suit != null) {
				// Reset counter
				airlock.resetCheckEVASuit();
				// EVA suit is available.
				return true;
			}
			
			// EVA suit is not available.
			logger.warning(person, 4_000,
					"Could not find a working EVA suit for egress in the rover.");

			Mission m = person.getMind().getMission();

			// Note: should at least wait for a period of time for the EVA suit to be fixed
			// before calling for rescue
			if (v != null && m != null && !v.isBeaconOn() && !v.isBeingTowed()) {

				if (airlock.getCheckEVASuit() > 100)
					// Set the emergency beacon on since no EVA suit is available
					((VehicleMission) m).getHelp(NO_EVA_SUITS);
			}
			
			// Add to the counter
			airlock.addCheckEVASuit();
			// EVA suit is not available.
			return false;
		}

		return true;
	}

	/**
	 * Releases person from the associated Airlock.
	 */
	@Override
	protected void clearDown() {
		// Clear the person as the airlock operator if task ended prematurely.
		if (airlock != null) {
			// Remove the reservation of this chamber
			airlock.removeReservation(person.getIdentifier());
			// Release the responsibility of being the airlock operator if he's one
			airlock.releaseOperatorID(id);
					
			if (inSettlement) {
				logger.fine(person, 4_000,
						"Concluded the building airlock operator task.");
			}
			else {
				logger.fine(person,  4_000,
						"Concluded the vehicle airlock operator task.");
			}
			
			if (inSettlement) {
				((ClassicAirlock)airlock).removeFromActivitySpot(person);
			}
			
			airlock.remove(person);
			
			if (airlock.isEmpty())
				airlock.setAirlockMode(AirlockMode.NOT_IN_USE);
		}
	}

	/**
	 * Can these Task be recorded ?
	 * 
	 * @return false
	 */
	@Override
	protected boolean canRecord() {
		return false;
	}
	
	@Override
	public void destroy() {
		airlock = null;
		super.destroy();
	}
}

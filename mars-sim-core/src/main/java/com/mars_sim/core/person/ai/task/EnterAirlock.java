/*
 * Mars Simulation Project
 * EnterAirlock.java
 * @date 2023-11-20
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task;

import java.util.Set;

import com.mars_sim.core.Unit;
import com.mars_sim.core.equipment.EVASuit;
import com.mars_sim.core.equipment.EVASuitUtil;
import com.mars_sim.core.equipment.EquipmentOwner;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.structure.Airlock;
import com.mars_sim.core.structure.Airlock.AirlockMode;
import com.mars_sim.core.structure.AirlockType;
import com.mars_sim.core.structure.AirlockZone;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.function.BuildingAirlock;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.mapdata.location.LocalPosition;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

/**
 * The EnterAirlock class is a Task for EVA ingress, namely, entering an airlock
 * of a settlement or vehicle after an EVA operation outside have been
 * accomplished.
 */
public class EnterAirlock extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(EnterAirlock.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.enterAirlock"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase REQUEST_INGRESS = new TaskPhase(Msg.getString("Task.phase.requestIngress")); //$NON-NLS-1$
	private static final TaskPhase DEPRESSURIZE_CHAMBER = new TaskPhase(Msg.getString("Task.phase.depressurizeChamber")); //$NON-NLS-1$
	private static final TaskPhase STEP_THRU_OUTER_DOOR = new TaskPhase(Msg.getString("Task.phase.stepThruOuterDoor")); //$NON-NLS-1$
	private static final TaskPhase WALK_TO_CHAMBER = new TaskPhase(Msg.getString("Task.phase.walkToChamber")); //$NON-NLS-1$
	private static final TaskPhase PRESSURIZE_CHAMBER = new TaskPhase(Msg.getString("Task.phase.pressurizeChamber")); //$NON-NLS-1$
	private static final TaskPhase DOFF_EVA_SUIT = new TaskPhase(Msg.getString("Task.phase.doffEVASuit")); //$NON-NLS-1$
	private static final TaskPhase CLEAN_UP = new TaskPhase(Msg.getString("Task.phase.cleanUp")); //$NON-NLS-1$
	private static final TaskPhase LEAVE_AIRLOCK = new TaskPhase(Msg.getString("Task.phase.leaveAirlock")); //$NON-NLS-1$

	private static final String CHAMBER_FULL = "All chambers are occupied in ";
	
	// Static members
	/** The standard time for doffing the EVA suit. */
	private static final double SUIT_DOFFING_TIME = 15;
	/** The standard time for cleaning oneself and the EVA suit in a settlement . */
	private static final double STANDARD_CLEANINNG_TIME = 15;
	/** The shortened time for cleaning oneself and the EVA suit in a vehicle. */
	private static final double SHORTENED_CLEANINNG_TIME = 5;
	
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .1D;
	/** The standard time for each task phase. */
	private static final double STANDARD_TIME = 0.5;
	
	
	/** Is this a building airlock in a settlement? */
	private boolean inSettlement;
	/** The time it takes to clean up oneself and the EVA suit. */
	private double remainingCleaningTime;
	/** The time it takes to doff an EVA suit. */
	private double remainingDoffingTime;
	/** The time accumulatedTime for a task phase. */
	private double accumulatedTime;
	
	// Data members
	/** The airlock to be used. */
	private Airlock airlock;
	
	/**
	 * Constructor.
	 *
	 * @param person  the person to perform the task
	 * @param airlock to be used.
	 */
	public EnterAirlock(Person person, Airlock airlock) {
		super(NAME, person, false, false, STRESS_MODIFIER, SkillType.EVA_OPERATIONS, 100D);

		this.airlock = airlock;

		if (airlock.getAirlockType() == AirlockType.BUILDING_AIRLOCK) {
			inSettlement = true;
		}
		else
			inSettlement = false;

		// Initialize data members
		setDescription(Msg.getString("Task.description.enterAirlock.detail", airlock.getEntityName())); // $NON-NLS-1$
		// Initialize task phase
		addPhase(REQUEST_INGRESS);
		addPhase(DEPRESSURIZE_CHAMBER);
		addPhase(STEP_THRU_OUTER_DOOR);
		addPhase(WALK_TO_CHAMBER);
		addPhase(PRESSURIZE_CHAMBER);
		addPhase(DOFF_EVA_SUIT);
		addPhase(CLEAN_UP);
		addPhase(LEAVE_AIRLOCK);

		setPhase(REQUEST_INGRESS);

		logger.fine(person, 4000, "Starting EVA ingress in " + airlock.getEntityName() + ".");
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
		} else if (REQUEST_INGRESS.equals(getPhase())) {
			return requestIngress(time);
		} else if (DEPRESSURIZE_CHAMBER.equals(getPhase())) {
			return depressurizeChamber(time); 
		} else if (STEP_THRU_OUTER_DOOR.equals(getPhase())) {
			return stepThruOuterDoor(time);
		} else if (WALK_TO_CHAMBER.equals(getPhase())) {
			return walkToChamber(time);
		} else if (PRESSURIZE_CHAMBER.equals(getPhase())) {
			return pressurizeChamber(time);
		} else if (DOFF_EVA_SUIT.equals(getPhase())) {
			return doffEVASuit(time);
		} else if (CLEAN_UP.equals(getPhase())) {
			return cleanUp(time);
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
		if (isInZone(newZone))
			return true;
		
		// For ingress, a person would first arrive at zone 4, right outside an EVA Airlock. 
		// At zone 4, he progresses via the outer/exterior door onto zone 3.
		// At zone 3, he's waiting for an empty chamber to be available.
		// At zone 2, he's at the airlock chamber doffing his EVA suit.
		// At zone 1, he's waiting for the inner/interior door to open.
		// At zone 0, he's just stepped back onto the settlement.
		
		// The previous zone # has a higher numeric #
		int previousZone = newZone.ordinal() + 1;
		LocalPosition newPos = fetchNewPos(newZone);
		if (newPos != null && airlock.claim(newZone, newPos, person)) {
			if (previousZone <= 4) {
				if (airlock.vacate(AirlockZone.convert2Zone(previousZone), person)) {
					return moveThere(newPos, newZone);
				}
				else
					return false;
			}
			else {
				// Just arrived at zone 4. No need to vacate any zone.
				return moveThere(newPos, newZone);
			}
		}

		return false;
	}
	
	/**
	 * Checks if the person is already in a particular zone.
	 *
	 * @param zone
	 * @return true if the person is a particular zone
	 */
	private boolean isInZone(AirlockZone zone) {
		return airlock.isInZone(person, zone);
	}

	/**
	 * Obtains a new position in the target zone.
	 *
	 * @param zone the destination
	 * @param id   the id of the person
	 * @return LocalPosition
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
	 * @param newZone
	 */
	private boolean moveThere(LocalPosition newPos, AirlockZone newZone) {
		
		Building b = (Building) airlock.getEntity();
		
		if (newZone == AirlockZone.ZONE_2) {
			// Check if the person can walk to one of the 4 EVA chambers
			boolean canWalk = walkToEVASpot(b, newPos);
						
			if (canWalk) {
				// Convert the local activity spot to the settlement reference coordinate
				// Set the person's new position
				person.setPosition(newPos);
				
				logger.fine(person, 4000, "Arrived at "
						+ newPos.getShortFormat() + " in " + newZone + ".");
				return true;
			}
			else {
				logger.info(person, 4000, "Could not enter the chamber in airlock zone " 
						+ newZone + ".");
				return false;
			}
		}

		else {
			// Set the person's new position
			person.setPosition(newPos);
			
			logger.fine(person, 4000, "Arrived at "
					+ newPos.getShortFormat() + " in " + newZone + "@" + b.getName());
			return true;
		}
	}
	
	/**
	 * Requests the entry of the airlock.
	 *
	 * @param time
	 * @return
	 */
	private double requestIngress(double time) {

		Unit unit = (Unit)airlock.getEntity();
				
		boolean canProceed = false;
		
		logger.fine(person, 20_000, "Requested EVA ingress in " + airlock.getEntity().toString() + ".");

		if (!airlock.isActivated()) {
			// Only the airlock operator may activate the airlock
			airlock.setActivated(true);
		}
		
		if (airlock.isOperator(id)) {
			// Command the airlock state to be transitioned to "depressurized"
			airlock.setTransitioning(true);
			
			airlock.setAirlockMode(AirlockMode.INGRESS);
		}
		
		if (inSettlement) {

			if (!isInZone(AirlockZone.ZONE_4) && !airlock.addAwaitingOuterDoor(id)) {
				logger.info(person, 60_000,
						"Cannot get a spot outside the outer door in " + airlock.getEntityName() + ".");
				
				// Do not call clearDown since it will wipe a person from awaiting at outer door
//				clearDown();
				
				// Reset accumulatedTime back to zero accumulatedTime = 0
				// Do nothing in this frame
				// Wait and see if he's allowed to be at the outer door in the next frame
				return 0;
			}

			if (airlock.isOuterDoorLocked()) {
				logger.info(person, 60_000,
						"Cannot ingress. "
						+ "Exterior door locked at " + airlock.getEntityName() + ".");
				
				// Do not call clearDown since it will wipe a person from awaiting at outer door
//				clearDown();
				
				// Reset accumulatedTime back to zero accumulatedTime = 0
				// Do nothing in this frame
				// Wait and see if he's allowed to be at the outer door in the next frame
				return 0;
			}
			
			if (airlock.areAll4ChambersFull() || !airlock.hasSpace()) {
				logger.info(person, 60_000,
						"Cannot ingress. "
						+ CHAMBER_FULL + airlock.getEntityName() + ".");
				
				// Do not call clearDown since it will wipe a person from awaiting at outer door
//				clearDown();
				
				// Reset accumulatedTime back to zero accumulatedTime = 0
				// Do nothing in this frame
				// Wait and see if he's allowed to be at the outer door in the next frame
				return 0;
			}				
			
			if (transitionTo(AirlockZone.ZONE_4)) {
				// The outer door will stay locked if the chamber is NOT depressurized
				canProceed = true;
			}
			
			if (!airlock.isOuterDoorLocked()) {
				// If the airlock is empty, it means no one is using it
//				logger.log(unit, person, Level.INFO, 60_000,
//						"Outer door opened at " + airlock.getEntity().toString() + ".");
				// Go to the next phase in order for the outer door to be unlocked. 
				// After the depressurization has finished, it should be open.
				canProceed = true;
			}
			else if (airlock.isEmpty()) {
				// If the airlock is empty, it means no one is using it
//				logger.log(unit, person, Level.INFO, 60_000,
//						"Empty " + airlock.getEntity().toString() + ".");
				// Go to the next phase in order for the outer door to be unlocked. 
				// After the depressurization has finished, it should be open.
				canProceed = true;
			}
		}

		else { // For vehicle

			if (airlock.addAwaitingOuterDoor(id) || !airlock.isOuterDoorLocked() || airlock.isEmpty()) {
				canProceed = true;
			}
			else {
				logger.fine(person, 4_000, "Requested ingress" 
						+ " but cannot wait at " + airlock.getEntityName() + "'s outer door.");
				
				// Do not call clearDown since it will wipe a person from awaiting at outer door
//				clearDown();
				
				return 0;
			}
		}

		if (canProceed) {

			if (airlock.isDepressurized() && !airlock.isOuterDoorLocked()) {
				// If airlock has already been depressurized,
				// then it's ready for entry
//				logger.log(unit, person, Level.INFO, 4_000,
//						"Good that chamber already depressurized for entry in " + airlock.getEntity().toString() + ".");
				// Skip DEPRESSURIZE_CHAMBER phase and go to the ENTER_AIRLOCK phase
				setPhase(STEP_THRU_OUTER_DOOR);
			}

			else {
				
				if (airlock.isOperator(id)) {
					// Command the airlock state to be transitioned to "depressurized"
					airlock.setTransitioning(true);

					logger.info(person, 4_000, "Ready to depressurize the chamber.");

					if (!airlock.isDepressurized() || !airlock.isDepressurizing()) {
						// Note: Only the operator has the authority to start the depressurization
						// Go to the next task phase
						setPhase(DEPRESSURIZE_CHAMBER);
					}
				}
			}
		}

		return 0;
	}

	/**
	 * Depressurizes the chamber.
	 *
	 * @param time
	 * @return
	 */
	private double depressurizeChamber(double time) {
		// Accumulate work for this task phase
		accumulatedTime += time;

		boolean canProceed = false;
		
		if (!airlock.isActivated()) {
			// Only the airlock operator may activate the airlock
			airlock.setActivated(true);
		}
		
		if (airlock.isOperator(id)) {
			// Command the airlock state to be transitioned to "depressurizing"
			airlock.setTransitioning(true);
		}
		
		if (airlock.isDepressurized() && !airlock.isOuterDoorLocked()) {
			// If airlock has already been depressurized,
			// then it's ready for entry
//			logger.log((Unit)airlock.getEntity(), person, Level.FINE, 4_000,
//					"Good that chamber already depressurized for entry in " + airlock.getEntity().toString() + ".");

			canProceed = true;
		}

		else if (airlock.isDepressurizing()) {
			// just wait for depressurizing to finish
			return 0;
		}

		else {

			Set<Person> list = airlock.noEVASuit();
			if (!list.isEmpty()) {
				logger.warning(person,4_000,
						"Could not depressurize " + airlock.getEntityName() + ". "
						+ list + " still inside not wearing EVA suit.");

				// need to wait here for them to put on the EVA suit first
			}
			
			// just wait for depressurization to start and complete
			return 0;
		}

		if (canProceed && accumulatedTime > STANDARD_TIME * time) {
			// Reset accumulatedTime back to zero
			accumulatedTime -= STANDARD_TIME * time;
			// Add experience
			addExperience(time);

			setPhase(STEP_THRU_OUTER_DOOR);
			
//			AirlockMode airlockMode = airlock.getAirlockMode();
//			
//			if (airlockMode != AirlockMode.INGRESS
//				&& (airlock.isEmpty() || airlockMode != AirlockMode.EGRESS))
//					airlock.setAirlockMode(AirlockMode.INGRESS);
			
			if (airlock.isEmpty())
				airlock.setAirlockMode(AirlockMode.NOT_IN_USE);
			else
				airlock.setAirlockMode(AirlockMode.INGRESS);
		}
		
		return 0;
	}

	/**
	 * Enters through the outer door into the chamber of the airlock.
	 *
	 * @param time
	 * @return
	 */
	private double stepThruOuterDoor(double time) {
		// Accumulate work for this task phase
		accumulatedTime += time;

		Unit unit = (Unit)airlock.getEntity();
		
		boolean canProceed = false;

		if (!airlock.isActivated()) {
			// Only the airlock operator may activate the airlock
			airlock.setActivated(true);
		}
		
		if (airlock.isOperator(id)) {
			// Command the airlock state to be transitioned to "depressurizing"
			airlock.setTransitioning(true);
		}
		
		// WARNING: do NOT setPhase(DEPRESSURIZE_CHAMBER) or else it will go through
		// endless cycle of pressurizing and depressurizing airlock
//		if (!airlock.isDepressurized()) {
//			// Not at the correct airlock state. Go back to the previous task phase
//			setPhase(DEPRESSURIZE_CHAMBER);
//			// Reset accumulatedTime back to zero 
//			accumulatedTime = 0;
//			return time * .75;
//		}

		if (inSettlement) {

			if (airlock.isOuterDoorLocked()) {
				logger.warning(person, 4_000,
						"Outer door locked in " + airlock.getEntityName() + ".");
				
//				clearDown();
				
				// The outer door is locked probably because of not being 
				// at the correct airlock state. Go back to the previous task phase
				setPhase(REQUEST_INGRESS);
				// Reset accumulatedTime back to zero 
				accumulatedTime = 0;
				
				return 0;
			}
			
            if (!airlock.inAirlock(person)
				&& airlock.enterAirlock(person, id, false)) { 
				canProceed = transitionTo(AirlockZone.ZONE_3);						
			}
            
            else {
				logger.warning(person, 4_000,
						"Can't enter " + airlock.getEntityName() + ".");
				
				clearDown();
				
				// The outer door is locked probably because of not being 
				// at the correct airlock state. Go back to the previous task phase
				setPhase(REQUEST_INGRESS);
				// Reset accumulatedTime back to zero 
				accumulatedTime = 0;
				
				return 0;
            }
		}

		else {
			// in vehicle
			if (!airlock.isOuterDoorLocked()) {

				if (!airlock.inAirlock(person)) {
					canProceed = airlock.enterAirlock(person, id, false);
				}
				else // the person is already inside the airlock from previous cycle
					canProceed = true;
			}

			else {
				clearDown();
				
				setPhase(REQUEST_INGRESS);
				// Reset accumulatedTime back to zero 
				accumulatedTime = 0;
				
				return 0;
			}
		}

		if (canProceed && accumulatedTime > STANDARD_TIME * time) {
			// Reset accumulatedTime back to zero
			accumulatedTime -= STANDARD_TIME * time;
			
			logger.fine(person, 4_000,
					"Just entered through the outer door into " + airlock.getEntityName() + ".");

			airlock.setAirlockMode(AirlockMode.INGRESS);
			
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
	 * @return
	 */
	private double walkToChamber(double time) {
		// Accumulate work for this task phase
		accumulatedTime += time;

		boolean canProceed = false;
		
		logger.fine(person, 4_000,
				"Walking to a chamber in " + airlock.getEntityName() + ".");

		if (inSettlement) {

			if (airlock.areAll4ChambersFull()) {
				logger.warning(person, 16_000,
						"Can't walk to a chamber. " 
						+ CHAMBER_FULL + airlock.getEntityName()+ ".");
								
				// The outer door is locked probably because of not being 
				// at the correct airlock state. Go back to the previous task phase
//				setPhase(REQUEST_INGRESS);
				
				// Reset accumulatedTime back to zero accumulatedTime = 0
				// Do nothing in this frame
				// Wait and see if he's allowed to be at the outer door in the next frame
				return 0;
			}
			
			if (transitionTo(AirlockZone.ZONE_2)) {
				canProceed = true;
			}
			
			else {
				// Do nothing in this frame
				// Wait and see if he's allowed to be at the outer door in the next frame

				// Reset accumulatedTime back to zero
//				accumulatedTime = 0;

				
				return 0;
			}
		}
		
		else {
			canProceed = true;
		}

		if (canProceed && accumulatedTime > STANDARD_TIME * time) {
			// Reset accumulatedTime back to zero
			accumulatedTime -= STANDARD_TIME * time;

			if (!airlock.isActivated()) {
				// Only the airlock operator may activate the airlock
				airlock.setActivated(true);
			}
			
			if (airlock.isOperator(id)) {
				// Elect an operator to handle this task
				if (!airlock.isPressurized() || !airlock.isPressurizing()) {
					// Get ready for pressurization
					setPhase(PRESSURIZE_CHAMBER);
				}
			}

			if (airlock.isPressurized()) {
				logger.fine(person,  4_000,
						"Chamber already pressurized for entry in " + airlock.getEntityName() + ".");

				// Reset the count down doffing time
				remainingDoffingTime = SUIT_DOFFING_TIME + RandomUtil.getRandomInt(-2, 2);

				setPhase(DOFF_EVA_SUIT);
			}

			// Add experience
			addExperience(time);
		}

		return 0;

	}

	/**
	 * Pressurizes the chamber.
	 *
	 * @param time
	 * @return
	 */
	private double pressurizeChamber(double time) {
		// Accumulate work for this task phase
		accumulatedTime += time;

		boolean canProceed = false;
		
		if (airlock.isPressurized()) {

			logger.fine(person, 4_000,
					"Chamber already pressurized for entry in " + airlock.getEntityName() + ".");
			
			canProceed = true;
		}

		else if (airlock.isPressurizing()) {
			// just wait for pressurizing to finish
		}

		else {

			if (!airlock.isActivated()) {
				// Only the airlock operator may activate the airlock
				airlock.setActivated(true);
			}
			
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
			// Start the count down doffing time
			remainingDoffingTime = SUIT_DOFFING_TIME + RandomUtil.getRandomInt(-2, 2);

			setPhase(DOFF_EVA_SUIT);
		}

		return 0;
	}

	/**
	 * Doffs the EVA suit.
	 *
	 * @param time
	 * @return
	 */
	private double doffEVASuit(double time) {

		boolean canProceed = false;

		// 1. Gets the suit instance
		EVASuit suit = person.getSuit();
		
		// If a person is in the process of doffing off his EVA suit, one 
		// must make sure the airlock must stay pressurized. But how ?
		if (airlock.isPressurized()) {
			
			if (suit != null) {
				
				remainingDoffingTime -= time;

				if (remainingDoffingTime <= 0) {
					canProceed = true;
				}
			}
			
			else {
				logger.warning(person, 4_000,
						"did not possess an EVA suit in " + airlock.getEntityName()
						+ ".");
				
				// Presumably, this person would have doffed the suit in order to get to this phase
				setPhase(CLEAN_UP);
			}
		}
		
		else {
			logger.warning(person, 4_000,
				"Not pressurized. Walking back to the chamber and wait.");

			// It's not pressurized yet, go back to the PRESSURIZE_CHAMBER phase and wait
			setPhase(PRESSURIZE_CHAMBER);
			
			return time;
		}

		if (canProceed) {

			EquipmentOwner housing = null;

			if (inSettlement)
				housing = ((Building)airlock.getEntity()).getSettlement();
			else
				housing = (Vehicle)airlock.getEntity();
		
			// 1. Doff off the suit, transfer it to the entity, take back the garment and thermal bottle
			EVASuitUtil.checkIn(person, airlock.getEntity(), inSettlement, false);
			
			// 2. Records the person as the owner (if it hasn't been done)
			suit.setRegisteredOwner(person);
			
			// 3. Unload any waste
			suit.unloadWaste(housing);
			
			// 4. Print log
//			logger.log((Unit)housing, person, Level.INFO, 4_000, "Just doffed " + suit.getName() + ".");
			
			// Add experience
			addExperience(time);

			if (inSettlement) {
				remainingCleaningTime = STANDARD_CLEANINNG_TIME + RandomUtil.getRandomInt(-3, 3);
			}
			else
				remainingCleaningTime = SHORTENED_CLEANINNG_TIME + RandomUtil.getRandomInt(-1, 1);
			
			setPhase(CLEAN_UP);
		}
		
		return 0;
	}

	/**
	 * Performs cleaning up of EVA suit and oneself.
	 *
	 * @param time
	 * @return
	 */
	private double cleanUp(double time) {

		boolean canProceed = false;

//		if (!airlock.isPressurized()) {
//			// Go back to the previous phase
//			setPhase(PRESSURIZE_CHAMBER);
//			// Reset accumulatedTime back to zero
//			accumulatedTime = 0;
//			
//			return time * .75;
//		}

		if (inSettlement) {
			boolean doneCleaning = false;

			remainingCleaningTime -= time;

			if (remainingCleaningTime <= 0) {
				logger.fine(person, 4_000, "Completed the clean-up.");
				doneCleaning = true;
			}

			if (doneCleaning && transitionTo(AirlockZone.ZONE_1)) {
				// If in vehicle, it doesn't need to clean up
				canProceed = true;
			}
		}
		
		else {
			// If in vehicle, there are only two chambers, no time to clean up
			canProceed = true;
		}
		
		if (canProceed) {
			// Reset accumulatedTime back to zero
			accumulatedTime = 0;
			
			// Add experience
			addExperience(time);

			setPhase(LEAVE_AIRLOCK);
		}
		
		return 0;
	}

	/**
	 * Departs the chamber through the inner door of the airlock.
	 *
	 * @param time
	 * @return
	 */
	private double leaveAirlock(double time) {
		// Accumulate work for this task phase
		accumulatedTime += time;

		boolean canProceed = false;

		if (inSettlement) {
			
			if (airlock.inAirlock(person)) {
				canProceed = airlock.exitAirlock(person, id, false);
			}
			
			if (transitionTo(AirlockZone.ZONE_0)) {
				// True if the person is already there from previous frame
				canProceed = true;
			}	
		}

		else {

			if (airlock.inAirlock(person)) {
				// Check if the person can exit or not
				canProceed = airlock.exitAirlock(person, id, false);
			}
			else {
				// Already exit the air lock
				canProceed = true;
			}
		}

		if (canProceed && accumulatedTime > STANDARD_TIME * time) {
			// Reset accumulatedTime back to zero
			accumulatedTime -= STANDARD_TIME * time;
			
			// Remove the position at zone 0 before ending the task
			airlock.vacate(AirlockZone.ZONE_0, person);
			
			// Add experience
			addExperience(time);

			logger.fine(person,4_000,
					"Departing " + airlock.getEntity().toString() + ".");

			// This completes the EVA ingress through the airlock
			completeAirlockTask();
		}

		return 0;
	}

	/**
	 * Checks if a person can enter an airlock from an EVA.
	 *
	 * @param person  the person trying to enter
	 * @param airlock the airlock to be used.
	 * @return true if person can enter the airlock
	 */
	public static boolean canEnterAirlock(Person person, Airlock airlock) {

		boolean result = true;

		if (person.isInside()) {
			logger.warning(person, 4_000,
					"Could not enter " + airlock.getEntityName()
					+ ". Already inside and not outside.");
			result = false;
		}

		else if (airlock.areAll4ChambersFull() || !airlock.hasSpace()) {
			logger.info(person, 20_000,
					CHAMBER_FULL + airlock.getEntityName()
					+ ". Could not enter.");
			result = false;
		}

		return result;
	}

	@Override
	protected void clearDown() {
		// Clear the person as the airlock operator if task ended prematurely.
		if (airlock != null) {
			
			// Release the responsibility of being the airlock operator if he's one
			airlock.releaseOperatorID(id);
			
			if (inSettlement) {
				logger.fine(person, 4_000,
						"Concluded the building airlock operator task.");
			}
			else {
				logger.fine(person, 4_000,
						"Concluded the vehicle airlock operator task.");
			}
			
			if (inSettlement) {
				((BuildingAirlock)airlock).removeFromActivitySpot(person);
			}
			
			airlock.remove(person);
			
			if (airlock.isEmpty())
				airlock.setAirlockMode(AirlockMode.NOT_IN_USE);
		}
	}

	/**
	 * Removes the person from airlock and walk away and ends the airlock and walk
	 * tasks.
	 */
	public void completeAirlockTask() {
		
		clearDown();
		
		super.endTask();
	}

	/**
	 * Can this Task be recorded ?
	 * 
	 * @return false
	 */
	@Override
	protected boolean canRecord() {
		return false;
	}
	
	public void destroy() {
		airlock = null;
		super.destroy();
	}
}

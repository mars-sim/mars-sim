/**
 * Mars Simulation Project
 * ExitAirlock.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.mars.MarsSurface;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The ExitAirlock class is a Task for EVA egress, namely, exiting an airlock of a settlement or vehicle 
 * in order to perform an EVA operation outside.
 */
public class ExitAirlock extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(ExitAirlock.class.getName());

	
	/** Task name */
	private static final String NAME = Msg.getString("Task.description.exitAirlock"); //$NON-NLS-1$

	private static final double MIN_PERFORMANCE = 0.05;
	
	/** Task phases. */
	private static final TaskPhase REQUEST_EGRESS = new TaskPhase(
		Msg.getString("Task.phase.requestEgress")); //$NON-NLS-1$
	private static final TaskPhase PRESSURIZE_CHAMBER = new TaskPhase(
		Msg.getString("Task.phase.pressurizeChamber")); //$NON-NLS-1$
	private static final TaskPhase ENTER_AIRLOCK = new TaskPhase(
		Msg.getString("Task.phase.enterAirlock")); //$NON-NLS-1$
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
	private static final double SUIT_DONNING_TIME = 15;

	
	// Data members
	/** True if person has an EVA suit. */
	private boolean hasSuit = false;
	/** The remaining time in donning the EVA suit. */
	private double remainingDonningTime;
	/** True if person has an reserved spot. */
//	private boolean reservedSpot = false;
	
	/** The airlock to be used. */
	private Airlock airlock;
	/** The inside airlock position. */
	private Point2D insideAirlockPos = null;
	/** The exterior airlock position. */
	private Point2D exteriorDoorPos = null;
	/** The interior airlock position. */
	private Point2D interiorDoorPos = null;
	
	private final static int oxygenID = ResourceUtil.oxygenID;
	private final static int waterID = ResourceUtil.waterID;

	
	/**
	 * Constructor.
	 * 
	 * @param person  the person to perform the task
	 * @param airlock the airlock to use.
	 */
	public ExitAirlock(Person person, Airlock airlock) {
		super(NAME, person, false, false, STRESS_MODIFIER, SkillType.EVA_OPERATIONS, 100);

		this.airlock = airlock;
		
		// Initialize data members
		setDescription(Msg.getString("Task.description.exitAirlock.detail", airlock.getEntityName())); // $NON-NLS-1$
		// Initialize task phase
		addPhase(REQUEST_EGRESS);
		addPhase(PRESSURIZE_CHAMBER);
		addPhase(ENTER_AIRLOCK);
		addPhase(WALK_TO_CHAMBER);
		addPhase(DON_EVA_SUIT);
		addPhase(PREBREATHE);
		addPhase(DEPRESSURIZE_CHAMBER);
		addPhase(LEAVE_AIRLOCK);
			
		setPhase(REQUEST_EGRESS);

		logger.log(person, Level.FINER, 4_000,
				"Starting the EVA egress in " + airlock.getEntityName() + ".");
	}

	/**
	 * Performs the method mapped to the task's current phase.
	 * 
	 * @param time the amount of time (millisols) the phase is to be performed.
	 * @return the remaining time (millisols) after the phase has been performed.
	 */
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			return 0;
//			throw new IllegalArgumentException("Task phase is null");
			
		} else if (REQUEST_EGRESS.equals(getPhase())) {
			return requestEgress(time);
		} else if (PRESSURIZE_CHAMBER.equals(getPhase())) {
			return pressurizeChamber(time);
		} else if (ENTER_AIRLOCK.equals(getPhase())) {
			return enterAirlock(time);
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
	 * Transition the person into a particular zone
	 * 
	 * @param zone the destination
	 * @return true if the transition is successful
	 */
	private boolean transitionTo(int zone) {
		
		if (isInZone(zone)) {
			return true;
		}
		
		else {
			int previousZone = zone - 1;
			
			Point2D newPos = fetchNewPos(zone);
			
			if (newPos != null) {

				if (airlock.occupy(zone, newPos, id)) {

					if (previousZone >= 0) {
						
						if (airlock.vacate(previousZone, id)) {

							moveThere(newPos, zone);						
							return true;
						}
						else
							return false;
					}
					else {
						moveThere(newPos, zone);							
						return true;
					}
				}
			}
		}
		return false;
	}	
	
	
	/**
	 * Checks if the person is already in a particular zone
	 * 
	 * @param zone the zone the person is at
	 * @return true if the person is a particular zone
	 */
	private boolean isInZone(int zone) {
		return airlock.isInZone(person, zone);
	}
	
	/**
	 * Obtains a new position in the target zone
	 * 
	 * @param zone the destination zone
	 * @return Point2D a new location
	 */
	private Point2D fetchNewPos(int zone) {
		Point2D newPos = null;
	
		if (zone == 0) {	
			newPos = airlock.getAvailableInteriorPosition(false);		
		}
		else if (zone == 1) {	
			newPos = airlock.getAvailableInteriorPosition(true);
		}
		else if (zone == 2) {
			newPos = ((Building) airlock.getEntity()).getEVA().getAvailableActivitySpot(person);
		}
		else if (zone == 3) {	
			newPos = airlock.getAvailableExteriorPosition(true);
		}
		else if (zone == 4) {	
			newPos = airlock.getAvailableExteriorPosition(false);
		}
		
//		if (newPos != null && airlock.joinQueue(zone, newPos, id))
//			airlock.removePosition(zone, oldPos, id);
		
		return newPos;
	}
	
	/**
	 * Moves the person to a particular zone
	 * 
	 * @param newPos the target position in that zone
	 * @param zone the destination zone
	 */
	private void moveThere(Point2D newPos, int zone) {
//		System.out.println(person + "::moveThere");
		if (zone == 2) {
			walkToEVASpot((Building)airlock.getEntity());
		}
		
		else if (zone == 4) {
			// Note: Do NOT do obstacle checking because this movement crosses the 
			// boundary of Zone 3 in EVA airlock and Zone 4 (which is outside) via
			// the outer door. 
			addSubTask(
					new WalkOutside(person, 
					person.getXLocation(), 
					person.getYLocation(), 
					airlock.getAvailableExteriorPosition().getX(),
					airlock.getAvailableExteriorPosition().getY(), true));
		}
		
		else {
//			addSubTask(new WalkSettlementInterior(person, (Building)airlock.getEntity(), 
//					newPos.getX(),
//					newPos.getY(), 0));
			person.setXLocation(newPos.getX());
			person.setYLocation(newPos.getY());
		}
		
		logger.log(person, Level.FINER, 4_000,
				"Arrived at (" 
			+ Math.round(newPos.getX()*100.0)/100.0 + ", " 
			+ Math.round(newPos.getY()*100.0)/100.0 + ") in airlock zone " + zone + ".");
	}
	
	/**
	 * Checks if a person is tired, too stressful or hungry and need to take break, eat and/or sleep
	 *
	 * @return true if a person is fit
	 */
	private boolean isFit() {
		// if the person is in the airlock next to the observatory 
		// he will always be qualified to leave that place, or else
		// he will get stranded
		if (person.isAdjacentBuildingType(Building.ASTRONOMY_OBSERVATORY)) {
			return true;
		}
		// Checks if a person is tired, too stressful or hungry and need 
		// to take break, eat and/or sleep
		else if (person.getPhysicalCondition().computeFitnessLevel() > 2) {
			return true;
		}
		else {
//			logger.log(person, Level.INFO, 20_000,
//					"Not fit enough in doing EVA egress in "
//							+ airlock.getEntity().toString() + ".");
//					+ Math.round(person.getXLocation()*10.0)/10.0 + ", " 
//					+ Math.round(person.getYLocation()*10.0)/10.0 + ")."
			return false;
		}
	}
	
	/**
	 * Request the entry of the airlock
	 * 
	 * @param time the pulse
	 * @return the remaining time
	 */
	private double requestEgress(double time) {

		double remainingTime = 0;
		
		if (!isFit()) {
			walkAway(person, "Not fit for egress");
			return 0;
		}
		
		if (person.isOutside()) {
			endTask();
		}
		
		if (!airlock.isActivated()) {
			// Only the airlock operator may activate the airlock
			airlock.setActivated(true);
		}
		
		logger.log(person, Level.FINE, 20_000, 
				"Requested EVA egress in " + airlock.getEntity().toString() + ".");
		
		boolean canProceed = false;
	
		if (airlock.getEntity() instanceof Building) {
			// Load up the EVA activity spots
			airlock.loadEVAActivitySpots();
			
			if (airlock.addAwaitingInnerDoor(person, id)) {
				
				if (!airlock.isChamberFull() && airlock.hasSpace()) {

					if (transitionTo(0)) {
						
						if (!airlock.isInnerDoorLocked()) {
							// The inner door will stay locked if the chamber is NOT pressurized
							canProceed = true;
							
	//						// if the inner door is locked, checks if anyone wearing EVA suit is inside
	//						List<Integer> list = new ArrayList<>(airlock.getOccupants());
	//						for (int id : list) {
	//							Person p = unitManager.getPersonByID(id);
	//							if (p.getSuit() != null) {
	//								canEnter = false;
	//								break;
	//							}
	//						}
						}
						
						else if (airlock.isEmpty()) {
							// Will allow the person come in if empty
							canProceed = true;
						}
					}
					else {
						walkAway(person, "Cannot transition to zone 0");
						return 0;
					}
				}
				else {
					walkAway(person, "Chamber full");
					return 0;
				}
			}
			else {
				walkAway(person, "Cannot wait at " + airlock.getEntity().toString() + " inner door");
				return 0;
			}
		}

		else if (airlock.getEntity() instanceof Rover) {
			
	 		if (interiorDoorPos == null) {
	 			interiorDoorPos = airlock.getAvailableInteriorPosition();
			}
	 		
//			if (LocalAreaUtil.areLocationsClose(new Point2D.Double(person.getXLocation(), person.getYLocation()), interiorDoorPos)) {			
				if (airlock.addAwaitingInnerDoor(person, id)) {			
					canProceed = true;
				}
				else {
					walkAway(person, "Cannot wait at " + airlock.getEntity().toString() + " inner door");
					return 0;
				}
//			}
//			
//			else {
//				Rover airlockRover = (Rover) airlock.getEntity();
//				logger.log(person, Level.INFO, 4_000,
//						"Walked toward the inner door in " + airlockRover);
//		 		// Walk to interior airlock position.
//		 		addSubTask(new WalkRoverInterior(person, airlockRover, 
//		 				interiorDoorPos.getX(), interiorDoorPos.getY()));
//			}
		}
		
		if (canProceed) {
			
			if (airlock.isPressurized() && !airlock.isInnerDoorLocked()) {
				// If airlock has already been pressurized,
				// then it's ready for entry
				
				logger.log(person, Level.FINE, 4_000,
						"Chamber already pressurized for entry in " 
					+ airlock.getEntity().toString() + ".");

				// Skip PRESSURIZE_CHAMBER phase and go to the ENTER_AIRLOCK phase
				setPhase(ENTER_AIRLOCK);
			}

			else {
				// since it's not pressurized, will need to pressurize the chamber first
				
				if (!airlock.isActivated()) {
					// Only the airlock operator may activate the airlock
					airlock.setActivated(true);
				}
				
				if (airlock.isOperator(id)) {
					
					logger.log(person, Level.FINE, 4_000, "Ready to pressurize the chamber.");
					
					if (!airlock.isPressurized() || !airlock.isPressurizing()) {
						// Get ready for pressurization
						setPhase(PRESSURIZE_CHAMBER);
					}
				}
			}
		}
		
//		else {
			// Can't enter the airlock
//			endTask();
//			walkAway(person, "Cannot egress");
			
//			logger.log(person, Level.WARNING, 4_000,
//				"Unable to use " 
//				+ airlock.getEntity().toString() + " for EVA egress.");
//			return 0;
//		}

		return remainingTime;
	}
	
	/**
	 * Pressurize the chamber
	 * 
	 * @param time
	 * @return the remaining time
	 */
	private double pressurizeChamber(double time) {
		
		double remainingTime = 0;
		
		if (!isFit()) {
			walkAway(person, "Not fit before pressurization");
			return 0;
		}
		
		if (!airlock.isActivated()) {
			// Only the airlock operator may activate the airlock
			airlock.setActivated(true);
		}
		
		if (airlock.isPressurized() && !airlock.isInnerDoorLocked()) {

			logger.log(person, Level.FINE, 4_000,
					"The chamber already pressurized in " 
				+ airlock.getEntity().toString() + ".");
			
			// Add experience
			addExperience(time);
			
			setPhase(ENTER_AIRLOCK);
		}
		
		else if (airlock.isPressurizing()) {
			// just wait for pressurizing to finish 
		}
		
		else {
			
			if (airlock.isOperator(id)) {
				// Command the airlock state to be transitioned to "pressurized"
				airlock.setTransitioning(true);
				
				logger.log(person, Level.FINE, 4_000,
						"Started pressurizing the chamber in " 
							+ airlock.getEntity().toString() + ".");
				// Pressurizing the chamber
				airlock.setPressurizing();
			}
		}
		
		return remainingTime;
	}
	
	/**
	 * Enter through the inner door into the chamber of the airlock
	 * 
	 * @param time
	 * @return the remaining time
	 */			
	private double enterAirlock(double time) {
//		logger.log(person, Level.INFO, 20_000, 
//				"called enterAirlock() in " + airlock.getEntity().toString() + ".");
		double remainingTime = 0;
	
		boolean canProceed = false;
		
		if (!isFit()) {
			walkAway(person, "Not fit to enter");
			return 0;
		}
		
		if (!airlock.isPressurized()) {
			// Go back to the previous phase
			setPhase(PRESSURIZE_CHAMBER);
			return 0;
		}
		
		if (airlock.getEntity() instanceof Building) {

			if (!airlock.isInnerDoorLocked()) {
				
				if (!airlock.isChamberFull() && airlock.hasSpace()) {
					
					if (!airlock.inAirlock(person)) {
						canProceed = airlock.enterAirlock(person, id, true); 
					}
					else // the person is already inside the airlock from previous cycle
						canProceed = true;
					
					if (canProceed && transitionTo(1)) {
//						logger.log(person, Level.INFO, 20_000, 
//								"called transitionTo(1) in " + airlock.getEntity().toString() + ".");
						canProceed = true;
					}	
					else if (isInZone(2)) {
						// true if the person is already inside the chamber from previous cycle
						canProceed = true;
//						logger.log(person, Level.INFO, 20_000,
//								"called isInZone(2) in " + airlock.getEntity().toString() + ".");
					}
					else
						canProceed = false;
				}
				else {
					walkAway(person, "Chamber full");
					return 0;
				}
			}
		}
		
		else if (airlock.getEntity() instanceof Rover) {
			
	 		if (interiorDoorPos == null) {
	 			interiorDoorPos = airlock.getAvailableInteriorPosition();
			}
	 		
//			if (LocalAreaUtil.areLocationsClose(new Point2D.Double(person.getXLocation(), person.getYLocation()), interiorDoorPos)) {		
				if (!airlock.isInnerDoorLocked()) {

					if (!airlock.inAirlock(person)) {
						canProceed = airlock.enterAirlock(person, id, true);
					} else // the person is already inside the airlock from previous cycle
						canProceed = true;
				}
				else {
					walkAway(person, airlock.getEntity().toString() + " inner door locked");
					return 0;
				}
//			}
			
//			else {
//				Rover airlockRover = (Rover) airlock.getEntity();
//
//		 		// Walk to interior airlock position.
//		 		addSubTask(new WalkRoverInterior(person, airlockRover, 
//		 				interiorDoorPos.getX(), interiorDoorPos.getY()));
//			
//				logger.log(person, Level.FINER, 4_000,
//						"Walked close to the interior door in " + airlockRover);
//			}	
		}
		
		if (canProceed) {
			logger.log(person, Level.FINE, 4_000,
					"Just entered through the inner door into " 
					+ airlock.getEntity().toString() + ".");
				
			// Add experience
			addExperience(time);
				
			setPhase(WALK_TO_CHAMBER);
		}
		
		return remainingTime;
	}
	
	/**
	 * Walk to the chamber
	 * 
	 * @param time
	 * @return canProceed
	 */
	private double walkToChamber(double time) {
		
		double remainingTime = 0;
		
		if (!isFit()) {
			walkAway(person, "Not fit to walk to chamber");
			return 0;
		}
		
		if (!airlock.isPressurized()) {
			// Go back to the previous phase
			setPhase(PRESSURIZE_CHAMBER);
			return 0;
		}
		
		logger.log(person, Level.FINE, 4_000,
				"Walking to a chamber in " + airlock.getEntity().toString() + ".");
		
		boolean canProceed = false;

		if (airlock.getEntity() instanceof Building) {
		
			if (transitionTo(2)) {
				canProceed = true;
			}
			
			else {
				setPhase(ENTER_AIRLOCK);
				return 0;
			}
		}
		
		else if (airlock.getEntity() instanceof Rover) {
			
			if (insideAirlockPos == null) {
	 			insideAirlockPos = airlock.getAvailableAirlockPosition();
			}
			
//	 		if (LocalAreaUtil.areLocationsClose(new Point2D.Double(person.getXLocation(), person.getYLocation()), insideAirlockPos)) {
	 			if (!airlock.isInnerDoorLocked()) {
	 				canProceed = true;
				}
	 			else {
					setPhase(ENTER_AIRLOCK);
					return 0;
				}
//			}
			
//			else {
//				Rover airlockRover = (Rover) airlock.getEntity();
//				logger.log(person, Level.FINER, 4_000,
//						"Walked to the reference position.");
//					
//		 		// Walk to interior airlock position.
//		 		addSubTask(new WalkRoverInterior(person, airlockRover, 
//		 				insideAirlockPos.getX(), insideAirlockPos.getY()));
//			}	
		}
        
		
		if (canProceed) {

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
				logger.log(person, Level.FINE, 4_000,
						"Chamber already pressurized for entry in " + airlock.getEntity().toString() + ".");

		 		// Reset the suit donning time
				remainingDonningTime = SUIT_DONNING_TIME + RandomUtil.getRandomInt(-5, 5);

				setPhase(DON_EVA_SUIT);
			}
			
			// Add experience
			addExperience(time);
		}
		
	
		return remainingTime;
	}

	/**
	 * Selects an EVA suit and don it.
	 * 
	 * @param time the amount of time to perform the task phase.
	 * @return the remaining time
	 */
	private double donEVASuit(double time) {

		double remainingTime = 0;
		
		if (!isFit()) {
			walkAway(person, "Not fit to don EVASuit");
			return 0;
		}
		
		if (!airlock.isPressurized()) {
			// Go back to the previous phase
			setPhase(PRESSURIZE_CHAMBER);
			return 0;
		}
		
// 		LogConsolidated.log(logger, Level.FINE, 4000, sourceName, 
//				"[" + person.getLocale() + "] " + person.getName() 
////				+ " " + loc 
//				+ " was ready to don the EVA suit.");
		
 		
		EVASuit suit = null;
		Inventory entityInv = null;
		
		// Check if person already has EVA suit.
		if (!hasSuit && alreadyHasEVASuit()) {
			hasSuit = true;
		}

		// Get an EVA suit from entity inventory.
		if (!hasSuit) { 
			entityInv = airlock.getEntityInventory();
			suit = getGoodEVASuit(entityInv, person);
		}

		if (!hasSuit && suit != null) {
			// if a person hasn't donned the suit yet
			try {
				// 1. Transfer the EVA suit from entityInv to person
				suit.transfer(entityInv, person);			
				// 2. set the person as the owner
				suit.setLastOwner(person);
				// 3. register the suit the person will take into the airlock to don
				person.registerSuit(suit);
				// 4. Loads the resources into the EVA suit
				loadEVASuit(suit);
				// the person has a EVA suit
				hasSuit = true;

			} catch (Exception e) {
				logger.log(person, Level.WARNING, 4000, 
						"Could not take " + suit.toString() 
						+ " or load resources into it.", e);
			}
		}
		
		if (hasSuit) {
			remainingDonningTime -= time;
			
			if (remainingDonningTime <= 0) {
				// Add experience
				addExperience(time - remainingTime);
				
				logger.log(person, Level.FINER, 4_000,
						"Donned the EVA suit and got ready to do pre-breathing.");
				// Reset the prebreathing time counter to max
				person.getPhysicalCondition().resetRemainingPrebreathingTime();
			
				setPhase(PREBREATHE);
			}
		}

		else {

			logger.log(person, Level.WARNING, 4_000,
					"Could not find a working EVA suit. End this task.");
			
			// TODO: what to do if person still doesn't have an EVA suit ?
			endTask(); 
			
			// Will need to clear the task that create the ExitAirlock sub task
//			person.getMind().getTaskManager().clearAllTasks();

			return 0D;
		}

		return remainingTime;
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

		double remainingTime = 0;
		
		boolean canProceed = true;
		
		PhysicalCondition pc = person.getPhysicalCondition();
		
		pc.reduceRemainingPrebreathingTime(time);
		
		if (hasSuit && person.getSuit() != null) {

			if (pc.isThreeQuarterDonePrebreathing() || pc.isDonePrebreathing()) {
				logger.log(person, Level.FINER, 4_000,
						"Done pre-breathing.");

				List<Integer> list = new ArrayList<>(airlock.getOccupants());
				for (int id : list) {
					Person p = airlock.getPersonByID(id);
					if (p.getSuit() == null) {
						// Two groups of people having no EVA suits.
						// (1) Those who egress but just come in to the airlock and haven't donned the suit yet
						// (2) Those who ingress and have taken off the EVA suits. They are ready to leave.
						canProceed = false;
						break;
					}
				}
			}
		}
		
		if (canProceed) {
			
			if (!airlock.isActivated()) {
				// Only the airlock operator may activate the airlock
				airlock.setActivated(true);
			}
			
			if (airlock.isOperator(id)) {
				// Elect an operator to handle this task				
				if (!airlock.isDepressurized() || !airlock.isDepressurizing()) {
					// Get ready for depressurization
					setPhase(DEPRESSURIZE_CHAMBER);
				}
			}
			
			if (airlock.isDepressurized()) {
				logger.log(person, Level.FINE, 4_000,
						"Chamber already depressurized for exit in " + airlock.getEntity().toString() + ".");

				setPhase(LEAVE_AIRLOCK);
			}
			
			// Add experience
			addExperience(time);
	
		}
		
		return remainingTime;
	}

	/**
	 * Depressurize the chamber
	 * 
	 * @param time the pulse
	 * @return the remaining time
	 */
	private double depressurizeChamber(double time) {

		double remainingTime = 0;
			
		if (!airlock.isActivated()) {
			// Only the airlock operator may activate the airlock
			airlock.setActivated(true);
		}
		
		if (airlock.isDepressurized()) {

			logger.log(person, Level.FINE, 4_000,
					"Chamber already depressurized for exit in " 
				+ airlock.getEntity().toString() + ".");
			
			// Add experience
			addExperience(time);
			
			setPhase(LEAVE_AIRLOCK);
		}
		
		else if (airlock.isDepressurizing()) {
			// just wait for depressurizing to finish 
		}
		
		else {
			
			Set<Person> list = airlock.noEVASuit();
			if (list.size() == 0) {
				
//				if (!airlock.isActivated()) {
//					// Only the airlock operator may activate the airlock
//					airlock.setActivated(true);
//				}
				
				if (airlock.isOperator(id)) {
					// Command the airlock state to be transitioned to "depressurized"
					airlock.setTransitioning(true);
					
					// Depressurizing the chamber
					boolean succeed = airlock.setDepressurizing();
					if (!succeed) {
						logger.log(person, Level.WARNING, 4_000,
								"Could not depressurize " + airlock.getEntity().toString() + ".");
					}
					
					else {
						logger.log(person, Level.FINE, 4_000,
								"Depressurizing " + airlock.getEntity().toString() + ".");
					}
				}	
			}
			
			else {				
				logger.log(person, Level.WARNING, 4_000,
						"Could not depressurize " + airlock.getEntity().toString()
						+ ". " + list + " inside not wearing EVA suit.");
	
				for (Person p: list) {
					walkAway(p, "Without EVA Suit");
					logger.log(p, Level.WARNING, 4_000,
							"Ran out of time to don EVA suit. Cancelling EVA egress.");
				}
				
				// It's not depressurized yet, go back to the WALK_TO_CHAMBER phase and wait
//				setPhase(WALK_TO_CHAMBER);
			}
		}
		
		return remainingTime;
	}
	
	/**
	 * Depart the chamber through the outer door of the airlock
	 *
	 * @param time the pulse
	 * @return the remaining time
	 */
	private double leaveAirlock(double time) {

		double remainingTime = 0;
		
		boolean canExit = false;
		
		if (airlock.getEntity() instanceof Building) {
			
			if (transitionTo(3)) {
				
				if (airlock.inAirlock(person)) {
					
					canExit = airlock.exitAirlock(person, id, true);
					
					if (canExit) {
						// Move to zone 4
						transitionTo(4);
					
						// Remove the position at zone 4 before calling endTask()
						airlock.vacate(4, id);
					}
				}
			}
		}
		
		else if (airlock.getEntity() instanceof Rover) {
			
			if (exteriorDoorPos == null) {
				exteriorDoorPos = airlock.getAvailableExteriorPosition();
			}
			
//			if (LocalAreaUtil.areLocationsClose(new Point2D.Double(person.getXLocation(), person.getYLocation()), exteriorDoorPos)) {
				if (airlock.inAirlock(person)) {
					canExit = airlock.exitAirlock(person, id, true);
				}
//			}
			
//			else {
//				Rover airlockRover = (Rover) airlock.getEntity();
//				logger.log(person, Level.FINER, 4_000,
//						"Tried walking close to the exterior door.");
//  			
//				addSubTask(new WalkRoverInterior(person, airlockRover, 
//              		exteriorDoorPos.getX(), exteriorDoorPos.getY())); 		
//			}	
		}
		
		if (canExit) {
			
			// Add experience
	 		addExperience(time);
	 		
			logger.log(person, Level.FINE, 4_000,
					"Leaving " + airlock.getEntity().toString() + ".");
			
			
			// This completes EVA egress from the airlock
			// End ExitAirlock task
			completeAirlockTask();
		}
	
		return remainingTime;
	}


	/**
	 * Walks to another location outside the airlock and ends the egress
	 *
	 * @param person the person of interest
	 * @param reason the reason for walking away
	 */
	private void walkAway(Person person, String reason) {
		airlock.removeID(person.getIdentifier());
		
		// This doesn't make sense. The endTask call will be for the current task
		// and if 'person' is the same then the walk subTask  will be immediately
		// cancelled by the endTask
		person.getTaskManager().getTask().walkToRandomLocation(false);
		endTask();
		person.getTaskManager().clearAllTasks(reason);
	}
	
	/**
	 * Remove the person from airlock and walk away and ends the airlock and walk tasks
	 */
	public void completeAirlockTask() {
		// Clear the person as the airlock operator if task ended prematurely.
		if (airlock != null && person.getName().equals(airlock.getOperatorName())) {
//			String loc = "";
			if (airlock.getEntity() instanceof Vehicle) {
//				loc = person.getVehicle().getName(); //airlock.getEntityName();
				logger.log(person, Level.FINER, 4_000,
						"Concluded the vehicle airlock operator task.");
			}
			else {//if (airlock.getEntity() instanceof Settlement) {
//				loc = ((Building) (airlock.getEntity())).getSettlement().getName();
				logger.log(person, Level.FINER, 4_000,
						"Concluded the airlock operator task.");
			}
		}
		
		airlock.removeID(id);

		// Ends the sub task 2 within the EnterAirlock task
		endSubTask2();
		
		// Remove all lingering tasks to avoid any unfinished walking tasks
//		person.getMind().getTaskManager().endSubTask();
			
		super.endTask();
	}
	
	/**
	 * Checks if a person can exit an airlock to do an EVA.
	 * 
	 * @param person  the person exiting
	 * @param airlock the airlock to be used
	 * @return true if person can exit the entity
	 */
	public static boolean canExitAirlock(Person person, Airlock airlock) {

		// Check if person is incapacitated.
		if (person.getPerformanceRating() <= MIN_PERFORMANCE) {
			// TODO: if incapacitated, should someone else help this person to get out?

			// Prevent the logger statement below from being repeated multiple times
			logger.log(person, Level.FINER, 4_000, 
					"Could NOT exit the airlock from " + airlock.getEntityName()
					+ " due to crippling performance rating");

			try {				
				if (person.isInVehicle()) {
					Settlement nearbySettlement = CollectionUtils.findSettlement(person.getVehicle().getCoordinates());
					if (nearbySettlement != null)
						// Attempt a rescue operation
						EVAOperation.rescueOperation((Rover)(person.getVehicle()), person, nearbySettlement);
				}
				else if (person.isOutside()) {
					Settlement nearbySettlement = CollectionUtils.findSettlement(person.getCoordinates());
//					Settlement nearbySettlement =  ((Building) (airlock.getEntity())).getSettlement()
					if (nearbySettlement != null)
						// Attempt a rescue operation
						EVAOperation.rescueOperation(null, person, ((Building) (airlock.getEntity())).getSettlement());
				}
				
			} catch (Exception e) {
				logger.log(person, Level.SEVERE, 4_000,
						"Could not get new action: ", e);
				e.printStackTrace(System.err);

			}

			return false;
		}
		
		// Check if person is outside.
		if (person.isOutside()) {
			logger.log(person, Level.FINER, 4_000,
					"Could NOT exit airlock from " + airlock.getEntityName() + " since he/she was already outside.");

			return false;
		}
		
		else if (person.isInSettlement()) {

			// Check if EVA suit is available.
			if (!goodEVASuitAvailable(airlock.getEntityInventory(), person)) {
				logger.log(person, Level.WARNING, 4_000, 
						"Could not find a working EVA suit and needed to wait.");

				airlock.addCheckEVASuit();
			
				EVASuit suit = person.getSuit();//(EVASuit) person.getInventory().findUnitOfClass(EVASuit.class);
				
				// Check if suit has any malfunctions.
				if (suit != null && suit.getMalfunctionManager().hasMalfunction()) {
					logger.log(person, Level.FINER, 4_000,
							"Have to end " + person.getTaskDescription() + " since " 
							+ suit.getName() + " has malfunctions and not usable.");
				}
				
				return false;
			}

			else {
				airlock.resetCheckEVASuit();			
				return true;
			}
		}

		else if (person.isInVehicle()) {
			// Check if EVA suit is available.
			if (!goodEVASuitAvailable(airlock.getEntityInventory(), person)) {
				// TODO: how to have someone deliver him a working EVASuit
				
				Vehicle v = person.getVehicle();
				Mission m = person.getMind().getMission();
				String hasMission = "";
				if (m != null)
					hasMission = " for " + m.getTypeID();
				// Mission m = missionManager.getMission(person);
				logger.log(person, Level.WARNING, 20_000, 
						v.getName() + hasMission
						+ " did NOT have a working EVA suit, awaiting the response for rescue.");
				
				// TODO: should at least wait for a period of time for the EVA suit to be fixed
				// before calling for rescue
				if (v != null && m != null && !v.isBeaconOn() && !v.isBeingTowed()) {

					airlock.addCheckEVASuit();
					
//    				person.getMind().getTaskManager().clearTask();
					// Calling getNewAction(true, false) so as not to get "stuck" inside the
					// airlock.
//                	person.getMind().getNewAction(true, false);

					// Repair this EVASuit by himself/herself
					logger.log(person, Level.WARNING, 2000, 
							v.getName() + hasMission
							+ " will try to repair an EVA suit.");
					
					EVASuit suit = person.getSuit();//(EVASuit) person.getInventory().findUnitOfClass(EVASuit.class);
					
					// Check if suit has any malfunctions.
					if (suit != null && suit.getMalfunctionManager().hasMalfunction()) {
						logger.log(person, Level.FINER, 20_000, 
								"Have to end " + person.getTaskDescription() + " since " 
								+ suit.getName() + " has malfunctions and not usable.");
					}
					
//					person.getMind().getTaskManager().addTask(new RepairMalfunction(person));

					if (airlock.getCheckEVASuit() > 21)
						// Set the emergency beacon on since no EVA suit is available
						((VehicleMission) m).setEmergencyBeacon(person, v, true, "No good Eva Suit");

				}

				return false;
			}

			else {
				airlock.resetCheckEVASuit();
				return true;
			}

		}

		return true;
	}

	/**
	 * Checks if the person already has an EVA suit in their inventory.
	 * 
	 * @return true if person already has an EVA suit.
	 */
	private boolean alreadyHasEVASuit() {
		boolean result = false;

		EVASuit suit = person.getSuit();//(EVASuit) person.getInventory().findUnitOfClass(EVASuit.class);
		if (suit != null) {
			result = true;
			// LogConsolidated.log(Level.FINER, 3000, sourceName,
			// person.getName() + " already possesses an EVA suit.", null);
		}

		return result;
	}

	/**
	 * Checks if a good EVA suit is in entity inventory.
	 * 
	 * @param inv the inventory to check.
	 * @param {@link Person}
	 * @return true if good EVA suit is in inventory
	 */
	public static boolean goodEVASuitAvailable(Inventory inv, Person p) {
		if (getGoodEVASuit(inv, p) != null) {
			return true;
		} else
			return false;
	}

	/**
	 * Gets a good EVA suit from an inventory.
	 *
	 * @param inv the inventory to check.
	 * @return EVA suit or null if none available.
	 */
	public static EVASuit getGoodEVASuit(Inventory inv, Person p) {
		List<EVASuit> malSuits = new ArrayList<>(0);
		List<EVASuit> noResourceSuits = new ArrayList<>(0);
		List<EVASuit> goodSuits = new ArrayList<>(0);
		Collection<EVASuit> suits = inv.findAllEVASuits();
		for (EVASuit suit : suits) {
			boolean malfunction = suit.getMalfunctionManager().hasMalfunction();
			if (malfunction) {
				logger.log(p, Level.WARNING, 50_000, 
						"Spotted the malfunction with " + suit.getName() + " when examining it.");
				malSuits.add(suit);
				suits.remove(suit);
			}
			
			try {
				boolean hasEnoughResources = hasEnoughResourcesForSuit(inv, suit);
				if (!malfunction && hasEnoughResources) {			
					if (p != null && suit.getLastOwner() == p)
						// Prefers to pick the same suit that a person has been tagged in the past
						return suit;
					else
						// tag it as good suit for possible use below
						goodSuits.add(suit);
				}
				else if (!malfunction && !hasEnoughResources) {
					// tag it as no resource suit for possible use below
					noResourceSuits.add(suit);					
				}
				
				
			} catch (Exception e) {
				logger.log(p, Level.SEVERE, 50_000,
						"Could not find enough resources for " + suit.getName() + ".", e);
			}
		}

		// Picks any one of the good suits
		int size = goodSuits.size();
		if (size == 1)
			return goodSuits.get(0);
		else if (size > 1)
			return goodSuits.get(RandomUtil.getRandomInt(size - 1));
		
		// Picks any one of the good suits
		size = noResourceSuits.size();
		if (size == 1)
			return noResourceSuits.get(0);
		else if (size > 1)
			return noResourceSuits.get(RandomUtil.getRandomInt(size - 1));
		
		return null;
	}

	/**
	 * Checks if entity unit has enough resource supplies to fill the EVA suit.
	 * 
	 * @param entityInv the entity unit.
	 * @param suit      the EVA suit.
	 * @return true if enough supplies.
	 * @throws Exception if error checking suit resources.
	 */
	private static boolean hasEnoughResourcesForSuit(Inventory entityInv, EVASuit suit) {

		Inventory suitInv = suit.getInventory();
		int otherPeopleNum = entityInv.findNumUnitsOfClass(Person.class) - 1;

		// Check if enough oxygen.
		double neededOxygen = suitInv.getAmountResourceRemainingCapacity(oxygenID, true, false);
		double availableOxygen = entityInv.getAmountResourceStored(oxygenID, false);
		// Make sure there is enough extra oxygen for everyone else.
		availableOxygen -= (neededOxygen * otherPeopleNum);
		boolean hasEnoughOxygen = (availableOxygen >= neededOxygen);

		// Check if enough water.
//		double neededWater = suitInv.getAmountResourceRemainingCapacity(waterID, true, false);
//		double availableWater = entityInv.getAmountResourceStored(waterID, false);
//		// Make sure there is enough extra water for everyone else.
//		availableWater -= (neededWater * otherPeopleNum);
//		boolean hasEnoughWater = (availableWater >= neededWater);

		// it's okay even if there's not enough water
//		if (!hasEnoughWater)
//			LogConsolidated.log(Level.WARNING, 20_000, sourceName,
//					"[" + suit.getContainerUnit() + "] won't have enough water to feed " + suit.getNickName() + " but can still use it.", null);

		return hasEnoughOxygen;// && hasEnoughWater;
	}

	/**
	 * Loads an EVA suit with resources from the container unit.
	 * 
	 * @param suit the EVA suit.
	 */
	private void loadEVASuit(EVASuit suit) {

		Inventory suitInv = suit.getInventory();		
		
		if (!(person.getContainerUnit() instanceof MarsSurface)) {
			Inventory entityInv = person.getContainerUnit().getInventory();
			// Warning : if person.getContainerUnit().getInventory() is null, the simulation hang up
			// person.getContainerUnit() instanceof MarsSurface may alleviate this situation
			
			// Fill oxygen in suit from entity's inventory.
			double neededOxygen = suitInv.getAmountResourceRemainingCapacity(oxygenID, true, false);
			double availableOxygen = entityInv.getAmountResourceStored(oxygenID, false);
			// Add tracking demand
			entityInv.addAmountDemandTotalRequest(oxygenID, neededOxygen);
	
			double takenOxygen = neededOxygen;
			if (takenOxygen > availableOxygen)
				takenOxygen = availableOxygen;
			try {
				entityInv.retrieveAmountResource(oxygenID, takenOxygen);
				suitInv.storeAmountResource(oxygenID, takenOxygen, true);
				// Add tracking demand
				entityInv.addAmountDemand(oxygenID, takenOxygen);
			} catch (Exception e) {
				logger.log(person, Level.SEVERE, 10_000, 
						"Ran into issues providing oxygen to " + suit.getName() + e.getMessage(),
						e);
			}
	
			// Fill water in suit from entity's inventory.
			double neededWater = suitInv.getAmountResourceRemainingCapacity(waterID, true, false);
			double availableWater = entityInv.getAmountResourceStored(waterID, false);
			// Add tracking demand
			entityInv.addAmountDemandTotalRequest(waterID, neededWater);
	
			double takenWater = neededWater;
			if (takenWater > availableWater)
				takenWater = availableWater;
			try {
				entityInv.retrieveAmountResource(waterID, takenWater);
				suitInv.storeAmountResource(waterID, takenWater, true);
				// Add tracking demand
				entityInv.addAmountDemand(waterID, takenWater);
			} catch (Exception e) {
				logger.log(person, Level.SEVERE, 10_000, 
						"Ran into issues providing water to " + suit.getName(), e);
			}

//			String loc = person.getModifiedLoc();
			
			// Return suit to entity's inventory.
			logger.log(person, Level.FINER, 4_000,
					"Loaded up "  + suit.getName() + ".");
		}
	}

	/**
	 * Release person from the associated Airlock
	 */
	@Override
	protected void clearDown() {
		// Clear the person as the airlock operator if task ended prematurely.
		if (airlock != null && person.getName().equals(airlock.getOperatorName())) {
			if (airlock.getEntity() instanceof Vehicle) {
				logger.log(person.getVehicle(), person, Level.FINE, 4_000,
						"Concluded the vehicle airlock operator task.");
			}
			else { //if (airlock.getEntity() instanceof Building Settlement) {
				logger.log(((Building) (airlock.getEntity())).getSettlement(), person, Level.FINE, 1_000,
						"Concluded the airlock operator task.");
			}
		}
		
		airlock.removeID(id);
	}

	/**
	 * Can these Task be recorded
	 * @return false
	 */
	@Override
	protected boolean canRecord() {
		return false;
	}
}

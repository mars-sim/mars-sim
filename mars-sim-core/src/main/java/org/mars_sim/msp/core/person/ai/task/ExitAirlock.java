/**
 * Mars Simulation Project
 * ExitAirlock.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.mars.MarsSurface;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
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
 * The ExitAirlock class is a Task for egress, namely, exiting an airlock of a settlement or vehicle 
 * in order to perform an EVA operation outside.
 */
public class ExitAirlock extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(ExitAirlock.class.getName());

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());

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
	
	private static int oxygenID = ResourceUtil.oxygenID;
	private static int waterID = ResourceUtil.waterID;

	
	/**
	 * Constructor.
	 * 
	 * @param person  the person to perform the task
	 * @param airlock the airlock to use.
	 */
	public ExitAirlock(Person person, Airlock airlock) {
		super(NAME, person, false, false, STRESS_MODIFIER, false, 0D);

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

		LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
				"[" + person.getLocale() + "] " + person.getName() 
				+ " was starting the EVA egress procedure in " + airlock.getEntityName() + ".");
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

	private boolean transitionTo(int zone) {
		
		if (isInZone(zone)) {
			return true;
		}
		
		else {	
			int previousZone = zone - 1;
			
			Point2D newPos = fetchNewPos(zone);
			
			if (newPos != null) {
//				System.out.println(person + " at zone " + zone + " getting newPos (" + newPos.getX() + ", " + newPos.getY() + ").");					
				if (airlock.occupy(zone, newPos, id)) {
//					System.out.println(person + " at zone " + zone + " occupy (" + newPos.getX() + ", " + newPos.getY() + ") is true.");	
					if (previousZone >= 0) {
						if (airlock.vacate(previousZone, id)) {
//							System.out.println(person + " at zone " + zone + " vacate (" + person.getXLocation() + ", " + person.getYLocation() + ") is true.");								
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
	 * @param zone
	 * @return
	 */
	private boolean isInZone(int zone) {
//		System.out.println(person + "::inZone");	
		return airlock.isInZone(person, zone);
	}
	
	/**
	 * Obtains a new position in the target zone
	 * 
	 * @param zone the destination
	 * @param id the id of the person
	 * @return Point2D
	 */
	private Point2D fetchNewPos(int zone) {
//		System.out.println(person + "::getNewPos");
//		int id = person.getIdentifier();
//		Point2D oldPos = new Point2D.Double(person.getXLocation(), person.getYLocation());
		Point2D newPos = null;
	
		if (zone == 0) {	
			newPos = airlock.getAvailableInteriorPosition(false);		
		}
		else if (zone == 1) {	
			newPos = airlock.getAvailableInteriorPosition(true);
		}
		else if (zone == 2) {	
			newPos = ((Building) airlock.getEntity()).getEVA().getAvailableActivitySpot(person);
//			logger.info("newPos : " + newPos + " in " + zone);
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
	 * @param zone
	 */
	private void moveThere(Point2D newPos, int zone) {
//		System.out.println(person + "::moveThere");
		if (zone == 2) {
			walkToEVASpot((Building)airlock.getEntity());
		}
		
		else if (zone == 4) {
			// Note: Do NOT do obstacle checking because this movement crosses the 
			// boundary of Zone 3 in EVA airlock and Zone 4 outside via
			// the outer door. 
			addSubTask(
					new WalkOutside(person, 
					person.getXLocation(), 
					person.getYLocation(), 
					airlock.getAvailableExteriorPosition().getX(),
					airlock.getAvailableExteriorPosition().getY(), true));
//					newPos.getX(),
//					newPos.getY(), true));
//			new WalkSettlementInterior(person, (Building)airlock.getEntity(), 
//					airlock.getAvailableExteriorPosition().getX(),
//					airlock.getAvailableExteriorPosition().getY(), 0));
		}
		
		else {
//			System.out.println("EnterAirlock::moveThere calling WalkSettlementInterior by " + person);
			addSubTask(new WalkSettlementInterior(person, (Building)airlock.getEntity(), 
					newPos.getX(),
					newPos.getY(), 0));
		}
		
		LogConsolidated.log(logger, Level.FINE, 4000, sourceName, 
			"[" + person.getLocale() + "] " + person.getName()
//			+ " " + loc
			+ " arrived at (" 
			+ Math.round(newPos.getX()*100.0)/100.0 + ", " 
			+ Math.round(newPos.getY()*100.0)/100.0 + ") in airlock zone " + zone + ".");
	}
	
//	/**
//	 * Checks if a person is tired, too stressful or hungry and need to take break, eat and/or sleep
//	 * @param time
//	 * @return
//	 */
//	private double checkFitness(double time) {
//		// Checks if a person is tired, too stressful or hungry and need 
//		// to take break, eat and/or sleep
//		if (!person.getPhysicalCondition().isFit()) {
//			person.getMind().getTaskManager().clearAllTasks();
//			walkToRandomLocation(true);
//		}
//		return time;
//	}
	
	/**
	 * Request the entry of the airlock
	 * 
	 * @param time
	 * @return
	 */
	private double requestEgress(double time) {

		double remainingTime = 0;
		
		if (!person.getPhysicalCondition().isFit()) {
			LogConsolidated.log(logger, Level.FINE, 4_000, sourceName, 
					"[" + person.getLocale() + "] "
					+ person.getName() 
					+ " was not fit enough to go outside ("
					+ Math.round(person.getXLocation()*10.0)/10.0 + ", " 
					+ Math.round(person.getYLocation()*10.0)/10.0 + ").");
			endTask();
			person.getMind().getTaskManager().clearAllTasks();
			walkToRandomLocation(true);
			return time;
		}
		
		if (person.isOutside()) {
			endTask();
		}
		
		String loc = person.getImmediateLocation();
		loc = loc == null ? "[N/A]" : loc;
		loc = loc.equalsIgnoreCase("Outside") ? loc.toLowerCase() : "in " + loc;
		
		LogConsolidated.log(logger, Level.FINE, 20_000, sourceName, 
				"[" + person.getLocale() + "] " + person.getName() 
//				+ " " + loc 
				+ " requested egress for an EVA in " + airlock.getEntity().toString() + ".");
		
		boolean canEnter = false;

		if (!airlock.isActivated())
			// Enable someone to be selected as an airlock operator
			airlock.setActivated(true);
		
		if (airlock.getEntity() instanceof Building) {
			// Load up the EVA activity spots
			airlock.loadEVAActivitySpots();
			
			if (!airlock.isInnerDoorLocked()) {

				if (transitionTo(0)) {
					
					if (airlock.addAwaitingInnerDoor(person, id)) {		
						
						canEnter = true;
						
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
				}
			}
			
//			else if (airlock.isEmpty()) {
//
//				if (transitionTo(0)) {
//					
//					if (airlock.addAwaitingInnerDoor(person, id)) {		
//						
//						canEnter = true;
//					}
//				}
//			}
		}
		
		else if (airlock.getEntity() instanceof Rover) {
			
	 		if (interiorDoorPos == null) {
	 			interiorDoorPos = airlock.getAvailableInteriorPosition();
			}
	 		
			if (LocalAreaUtil.areLocationsClose(new Point2D.Double(person.getXLocation(), person.getYLocation()), interiorDoorPos)) {
				
				if (airlock.addAwaitingInnerDoor(person, id)) {			
					canEnter = true;
				}
			}
			
			else {
				Rover airlockRover = (Rover) airlock.getEntity();
 		
		 		LogConsolidated.log(logger, Level.FINE, 4000, sourceName, 
							"[" + person.getLocale() + "] " + person.getName() 
							+ " " + loc + " walked close to the interior door in " + airlockRover);
		 		// Walk to interior airlock position.
		 		addSubTask(new WalkRoverInterior(person, airlockRover, 
		 				interiorDoorPos.getX(), interiorDoorPos.getY()));
			}	
		}

		if (canEnter) {
			
			// Add experience
			addExperience(time);
			
			if (airlock.isPressurized() && !airlock.isInnerDoorLocked()) {
				// If it stops adding or subtracting air, 
				// then airlock has been pressurized, 
				// ready to unlock the outer door
		
				LogConsolidated.log(logger, Level.FINE, 4000, sourceName,
					"[" + person.getLocale() 
					+ "] The chamber had just been pressurized for EVA ingress in " 
					+ airlock.getEntity().toString() + ".");
				
				// Add experience
				addExperience(time);
				
				setPhase(ENTER_AIRLOCK);
			}
			
			
			else if (airlock.hasSpace()) {
				
				if (!airlock.isActivated()) {
					// Enable someone to be selected as an airlock operator
					airlock.setActivated(true);
				}	
			}
			
			if (airlock.isOperator(id)) {
				// Add experience
				addExperience(time);
				
				setPhase(PRESSURIZE_CHAMBER);		
			}
		}
		
		else {
			endTask();
			person.getMind().getTaskManager().clearAllTasks();
			walkToRandomLocation(true);
			return time;
		}

		return remainingTime;
	}
	
	
	private double pressurizeChamber(double time) {

		double remainingTime = 0;
		
		if (!airlock.isActivated()) {
			// Enable someone to be selected as an airlock operator
			airlock.setActivated(true);
		}
		
		if (!person.getPhysicalCondition().isFit()) {
			LogConsolidated.log(logger, Level.FINE, 0, sourceName, 
					"[" + person.getLocale() + "] "
					+ person.getName() 
					+ " was not fit enough to go outside ("
					+ Math.round(person.getXLocation()*10.0)/10.0 + ", " 
					+ Math.round(person.getYLocation()*10.0)/10.0 + ").");
			endTask();
			person.getMind().getTaskManager().clearAllTasks();
			walkToRandomLocation(true);
			return time;
		}
		
		if (airlock.isPressurized() && !airlock.isInnerDoorLocked()) {
			// If it stops adding or subtracting air, 
			// then airlock has been pressurized, 
			// ready to unlock the outer door
	
			LogConsolidated.log(logger, Level.FINE, 4000, sourceName,
				"[" + person.getLocale() 
				+ "] The chamber had just been pressurized for EVA ingress in " 
				+ airlock.getEntity().toString() + ".");
			
			// Add experience
			addExperience(time);
			
			setPhase(ENTER_AIRLOCK);
		}
		
		else if (!airlock.isPressurizing()) {
			
//			if (airlock.isOperator(id)) {
				LogConsolidated.log(logger, Level.INFO, 4000, sourceName,
						"[" + person.getLocale() 
						+ "] The chamber started pressurizing in " 
						+ airlock.getEntity().toString() + ".");
				// Pressurizing the chamber
				airlock.setPressurizing();
//			}
		}
		
		if (airlock.isPressurizing()) {
//			LogConsolidated.log(logger, Level.FINE, 4000, sourceName,
//					"[" + person.getLocale() 
//					+ "] The chamber was pressurizing in " 
//					+ airlock.getEntity().toString() + ".");
			
			// Elect an operator to handle this task
			// Add air cycle time until it is fully pressurized
			airlock.addTime(time);
		}
		
		return remainingTime;
	}
	
				
	private double enterAirlock(double time) {

		double remainingTime = 0;
		
//		LogConsolidated.log(logger, Level.FINE, 4000, sourceName, 
//				"[" + person.getLocale() + "] " + person.getName() 
////				+ " " + loc 
//				+ " was entering " + airlock.getEntity().toString() + ".");
		
		boolean canEnter = false;

		if (airlock.getEntity() instanceof Building) {

			if (airlock.hasSpace() && !airlock.isInnerDoorLocked()) {
				
				if (!airlock.inAirlock(person)) {
					canEnter = airlock.enterAirlock(person, id, true); 
				}
				else
					canEnter = true;
				
				if (canEnter && transitionTo(1)) {
					canEnter = true;
				}
			}
		}
		
		else if (airlock.getEntity() instanceof Rover) {
			
	 		if (interiorDoorPos == null) {
	 			interiorDoorPos = airlock.getAvailableInteriorPosition();
			}
	 		
			if (LocalAreaUtil.areLocationsClose(new Point2D.Double(person.getXLocation(), person.getYLocation()), interiorDoorPos)) {
				
				if (airlock.hasSpace() && !airlock.isInnerDoorLocked()) {
					
					if (!airlock.inAirlock(person)) {
						canEnter = airlock.enterAirlock(person, id, true); 
					}
					else
						canEnter = true;
					
					if (canEnter && transitionTo(1)) {
						canEnter = true;
					}
				}
			}
			
			else {
				Rover airlockRover = (Rover) airlock.getEntity();
 		
		 		LogConsolidated.log(logger, Level.FINE, 4000, sourceName, 
							"[" + person.getLocale() + "] " + person.getName() 
							+ " walked close to the interior door in " + airlockRover);
		 		
		 		// Walk to interior airlock position.
		 		addSubTask(new WalkRoverInterior(person, airlockRover, 
		 				interiorDoorPos.getX(), interiorDoorPos.getY()));
			}	
		}
		
		if (canEnter) {
			
			LogConsolidated.log(logger, Level.FINE, 4000, sourceName,
					"[" + person.getLocale() 
					+ "] " + person.getName() + " just entered throught the exterior door into " 
					+ airlock.getEntity().toString() + ".");
				
			// Add experience
			addExperience(time);
				
			setPhase(WALK_TO_CHAMBER);
		}
		
		return remainingTime;
	}
	
	
	private double walkToChamber(double time) {
		
		double remainingTime = 0;
		
		String loc = person.getImmediateLocation();
		loc = loc == null ? "[N/A]" : loc;
		loc = loc.equalsIgnoreCase("Outside") ? loc.toLowerCase() : "in " + loc;
		
		LogConsolidated.log(logger, Level.FINE, 4000, sourceName, 
				"[" + person.getLocale() + "] " + person.getName() 
//				+ " " + loc 
				+ " was walking to a chamber in " + airlock.getEntity().toString() + ".");
		
		boolean canProceed = false;

		if (airlock.getEntity() instanceof Building) {
		
			if (transitionTo(2)) {
				canProceed = true;
			}
		}
		
		else if (airlock.getEntity() instanceof Rover) {
			
			if (insideAirlockPos == null) {
	 			insideAirlockPos = airlock.getAvailableAirlockPosition();
			}
	 		
			if (LocalAreaUtil.areLocationsClose(new Point2D.Double(person.getXLocation(), person.getYLocation()), insideAirlockPos)) {
				canProceed = true;
			}
			
			else {
				Rover airlockRover = (Rover) airlock.getEntity();
		         		 	 		
		 		LogConsolidated.log(logger, Level.FINE, 4000, sourceName, 
							"[" + person.getLocale() + "] " + person.getName() 
							+ " " + loc + " walked to the reference position.");
					
		 		// Walk to interior airlock position.
		 		addSubTask(new WalkRoverInterior(person, airlockRover, 
		 				insideAirlockPos.getX(), insideAirlockPos.getY()));
			}	
		}
        
		
		if (canProceed) {
			// Add experience
	 		addExperience(time);
	 		// Reset the suit donning time
			remainingDonningTime = SUIT_DONNING_TIME + RandomUtil.getRandomInt(-5, 5);
			
	 		LogConsolidated.log(logger, Level.FINE, 4000, sourceName, 
					"[" + person.getLocale() + "] " + person.getName() 
//					+ " " + loc 
					+ " was ready to don the EVA suit.");
	 		
	 		setPhase(DON_EVA_SUIT);
		}
	
		return remainingTime;
	}

	/**
	 * Selects an EVA suit.
	 * 
	 * @param time the amount of time to perform the task phase.
	 * @return the remaining time after performing the task phase.
	 */
	private double donEVASuit(double time) {

		double remainingTime = 0;
		
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
				LogConsolidated.log(logger, Level.WARNING, 4000, sourceName, "[" + person.getLocale()
								+ "] " + person.getName() + " could not take " + suit.toString() + " or load resources into it.", e);
			}
		}
		
		if (hasSuit) {
			remainingDonningTime -= time;
			
			if (remainingDonningTime <= 0) {
				// Add experience
				addExperience(time - remainingTime);
	
				LogConsolidated.log(logger, Level.FINE, 4_000, sourceName,
						"[" + person.getLocale() + "] " + person.getName()
								+ " donned the EVA suit and was getting ready to do pre-breathing.");
				// Reset the prebreathing time counter to max
				person.getPhysicalCondition().resetRemainingPrebreathingTime();
			
				setPhase(PREBREATHE);
			}
		}

		// If person still doesn't have an EVA suit, end task.
		else {
			LogConsolidated.log(logger, Level.WARNING, 4_000, sourceName,
					"[" + person.getLocale() + "] " + person.getName()
							+ " could not find a working EVA suit. End this task.");
			
			endTask(); 
			// Will need to clear the task that create the ExitAirlock sub task
			person.getMind().getTaskManager().clearAllTasks();

			return 0D;
		}

		return remainingTime;
	}
	
	/**
	 * Pre-breathes in the EVA suit to prevent the potential occurrence of incapacitating decompression sickness (DCS). 
	 * Prebreathing reduces the nitrogen content in the astronaut's body which prevents the formation of nitrogen 
	 * bubbles in body tissues when the atmospheric pressure is reduced. 
	 * 
	 * @param time
	 * @return remainingTime
	 */
	private double prebreathe(double time) {

		double remainingTime = 0;
		
		boolean result = true;
		
		if (!person.getPhysicalCondition().isFit()) {
			LogConsolidated.log(logger, Level.FINE, 0, sourceName, 
					"[" + person.getLocale() + "] "
					+ person.getName() 
					+ " was not fit enough to go outside ("
					+ Math.round(person.getXLocation()*10.0)/10.0 + ", " 
					+ Math.round(person.getYLocation()*10.0)/10.0 + ").");
			endTask();
			person.getMind().getTaskManager().clearAllTasks();
			walkToRandomLocation(true);
			return time;
		}
		
		PhysicalCondition pc = person.getPhysicalCondition();
		
		pc.reduceRemainingPrebreathingTime(time);
		
		if (hasSuit && person.getSuit() != null) {

			if (pc.isThreeQuarterDonePrebreathing()) {
				
//				if (!airlock.isInnerDoorLocked() && airlock.isOperator(id)) {
//					// Lock the inner door
//					airlock.setInnerDoorLocked(true);
//				}
				
				result = false;
			}
			
			else if (pc.isDonePrebreathing()) {
				
				LogConsolidated.log(logger, Level.INFO, 4_000, sourceName,
						"[" + person.getLocale() + "] " + person.getName()
								+ " was done pre-breathing.");

				List<Integer> list = new ArrayList<>(airlock.getOccupants());
				for (int id : list) {
					Person p = airlock.getPersonByID(id);
					if (p.getSuit() == null) {
						// Two groups of people having no EVA suits.
						// (1) Those who egress but just come in to the airlock and haven't donned the suit yet
						// (2) Those who ingress and have taken off the EVA suits. They are ready to leave.
						result = false;
						break;
					}
				}
			}
		}
		
		if (result) {
			
			if (!airlock.isActivated())
				// Enable someone to be selected as an airlock operator
				airlock.setActivated(true);
		
			if (airlock.isOperator(id) || airlock.isDepressurized()) {
				// Unlock the inner door
				LogConsolidated.log(logger, Level.FINE, 4000, sourceName,
						"[" + person.getLocale() 
						+ "] The interior door in " 
						+ airlock.getEntity().toString() 
						+ " had been locked. Ready to depressurize.");
					
				// Add experience
				addExperience(time);
					
				setPhase(DEPRESSURIZE_CHAMBER);
			}			
		}
		
		return remainingTime;
	}
			
	
	private double depressurizeChamber(double time) {

		double remainingTime = 0;
					
		if (!person.getPhysicalCondition().isFit()) {
			LogConsolidated.log(logger, Level.FINE, 0, sourceName, 
					"[" + person.getLocale() + "] "
					+ person.getName() 
					+ " was not fit enough to go outside ("
					+ Math.round(person.getXLocation()*10.0)/10.0 + ", " 
					+ Math.round(person.getYLocation()*10.0)/10.0 + ").");
			endTask();
			person.getMind().getTaskManager().clearAllTasks();
			walkToRandomLocation(true);
			return time;
		}
		
		if (!airlock.isActivated()) {
			// Enable someone to be selected as an airlock operator
			airlock.setActivated(true);
		}
		
		if (airlock.isDepressurized()) {
			// If it stops adding or subtracting air, 
			// then airlock has been depressurized, 
			// ready to unlock the outer door
	
			LogConsolidated.log(logger, Level.FINE, 4000, sourceName,
				"[" + person.getLocale() 
				+ "] The chamber had just been depressurized in " 
				+ airlock.getEntity().toString() + ".");
			
			// Add experience
			addExperience(time);
			
			setPhase(LEAVE_AIRLOCK);
		}
		
		else if (!airlock.isDepressurizing()) {
			//TODO: if someone is waiting outside the inner door, ask the C2 to unlock inner door to let him in before depressurizing
			LogConsolidated.log(logger, Level.INFO, 4000, sourceName,
					"[" + person.getLocale() 
					+ "] The chamber started depressurizing in " 
					+ airlock.getEntity().toString() + ".");
			// Depressurizing the chamber
			airlock.setDepressurizing();
		}
			
		if (airlock.isDepressurizing()) {
//			LogConsolidated.log(logger, Level.INFO, 4000, sourceName,
//					"[" + person.getLocale() 
//					+ "] The chamber was depressurizing in " 
//					+ airlock.getEntity().toString() + ".");
			
			// TODO:Elect an operator to handle this task
			
			// Add air cycle time until it is fully depressurized
			airlock.addTime(time);
		}
		
		return remainingTime;
	}
	
	
	private double leaveAirlock(double time) {

		double remainingTime = 0;
		
		if (!person.getPhysicalCondition().isFit()) {
			LogConsolidated.log(logger, Level.FINE, 0, sourceName, 
					"[" + person.getLocale() + "] "
					+ person.getName() 
					+ " was not fit enough to go outside ("
					+ Math.round(person.getXLocation()*10.0)/10.0 + ", " 
					+ Math.round(person.getYLocation()*10.0)/10.0 + ").");
			endTask();
			person.getMind().getTaskManager().clearAllTasks();
			walkToRandomLocation(true);
			return time;
		}
		
		boolean canExit = false;
		
		if (airlock.getEntity() instanceof Building) {
	
			if (transitionTo(3)) {
				
				if (airlock.inAirlock(person)) {
					canExit = airlock.exitAirlock(person, id, true);
				}
			}
		}
		
		else if (airlock.getEntity() instanceof Rover) {
			
			if (exteriorDoorPos == null) {
				exteriorDoorPos = airlock.getAvailableExteriorPosition();
			}
			
			if (LocalAreaUtil.areLocationsClose(new Point2D.Double(person.getXLocation(), person.getYLocation()), exteriorDoorPos)) {

				if (airlock.inAirlock(person)) {
					canExit = airlock.exitAirlock(person, id, true);
				}
			}
			
			else {
				Rover airlockRover = (Rover) airlock.getEntity();
		         		 	 		
				LogConsolidated.log(logger, Level.FINE, 4000, sourceName, 
  					"[" + person.getLocale() + "] " + person.getName() 
  					+ " tried walking close to the exterior door.");
  			
				addSubTask(new WalkRoverInterior(person, airlockRover, 
              		exteriorDoorPos.getX(), exteriorDoorPos.getY())); 		
			}	
		}
		
		if (canExit) {
			// Move to zone 4
			transitionTo(4);
			
			// Add experience
	 		addExperience(time);
	 		
			String loc = person.getImmediateLocation();
			loc = loc == null ? "[N/A]" : loc;
			loc = loc.equalsIgnoreCase("Outside") ? loc.toLowerCase() : "in " + loc;
			
			LogConsolidated.log(logger, Level.FINE, 4000, sourceName, 
					"[" + person.getLocale() + "] " + person.getName() 
//					+ " " + loc 
					+ " was leaving " + airlock.getEntity().toString() + ".");
			
			// Remove the position at zone 4 before calling endTask()
//			airlock.vacate(4, id);
			
			// This completes EVA egress from the airlock
			// End ExitAirlock task
			endTask();
		}
	
		return remainingTime;
	}	
	

	/**
	 * Adds experience to the person's skills used in this task.
	 * 
	 * @param time the amount of time (ms) the person performed this task.
	 */
	protected void addExperience(double time) {
		if (time == 0)
			return;
		// Add experience to "EVA Operations" skill.
		// (1 base experience point per 100 millisols of time spent)
		double evaExperience = time / 100D;

		// Experience points adjusted by person's "Experience Aptitude" attribute.
		NaturalAttributeManager nManager = person.getNaturalAttributeManager();
		int experienceAptitude = nManager.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		double experienceAptitudeModifier = (((double) experienceAptitude) - 50D) / 100D;
		evaExperience += evaExperience * experienceAptitudeModifier;
		evaExperience *= getTeachingExperienceModifier();
		person.getSkillManager().addExperience(SkillType.EVA_OPERATIONS, evaExperience, time);

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
			String newLog = "[" + person.getLocale() + "] " + person.getName() 
					+ " could NOT exit the airlock from " + airlock.getEntityName()
					+ " due to crippling performance rating";

			LogConsolidated.log(logger, Level.FINER, 4_000, sourceName, newLog);

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
				LogConsolidated.log(logger, Level.SEVERE, 4_000, sourceName,
						"[" + person.getLocale() + "] " + person.getName() + " could not get new action" + e.getMessage(), e);
				e.printStackTrace(System.err);

			}

			return false;
		}
		
		// Check if person is outside.
		if (person.isOutside()) {
			LogConsolidated.log(logger, Level.FINER, 4_000, sourceName, person.getName()
					+ " could NOT exit airlock from " + airlock.getEntityName() + " since he/she was already outside.");

			return false;
		}
		
		else if (person.isInSettlement()) {

			// Check if EVA suit is available.
			if (!goodEVASuitAvailable(airlock.getEntityInventory(), person)) {

				LogConsolidated.log(logger, Level.WARNING, 4_000, sourceName, "[" + person.getLocale() + "] "
								+ person + " could not find a working EVA suit and needed to wait.");

				airlock.addCheckEVASuit();
			
				EVASuit suit = person.getSuit();//(EVASuit) person.getInventory().findUnitOfClass(EVASuit.class);
				
				// Check if suit has any malfunctions.
				if (suit != null && suit.getMalfunctionManager().hasMalfunction()) {
					
					LogConsolidated.log(logger, Level.FINER, 4_000, sourceName, "[" + person.getLocale() + "] "
							+ person.getName() + " would have to end " + person.getTaskDescription() + " since " 
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
					hasMission = " for " + m.getName();
				// Mission m = missionManager.getMission(person);
				
				LogConsolidated.log(logger, Level.WARNING, 20_000, sourceName, "[" + person.getLocale() 
						+ "] " + person + " in " + v.getName() + hasMission
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
					
					LogConsolidated.log(logger, Level.WARNING, 2000, sourceName, "[" + person.getLocale() 
							+ "] " + person + " in " + v.getName() + hasMission
							+ " will try to repair an EVA suit.");
					
					EVASuit suit = person.getSuit();//(EVASuit) person.getInventory().findUnitOfClass(EVASuit.class);
					
					// Check if suit has any malfunctions.
					if (suit != null && suit.getMalfunctionManager().hasMalfunction()) {
						
						LogConsolidated.log(logger, Level.FINER, 20_000, sourceName, "[" + person.getLocale() + "] "
								+ person.getName() + " would have to end " + person.getTaskDescription() + " since " 
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
				LogConsolidated.log(logger, Level.WARNING, 50_000, sourceName, "[" + p.getLocale()
					+ "] " + p + " spotted the malfunction with " + suit.getName() + " when examining it.");
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
//				e.printStackTrace(System.err);
				LogConsolidated.log(logger, Level.SEVERE, 50_000, sourceName, "[" + p.getLocale()
						+ "] " + p + " could not find enough resources for " + suit.getName() + ".", e);
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
	 * @return
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
				LogConsolidated.log(
						logger, Level.SEVERE, 10_000, sourceName, "[" + person.getLocale() + "] "
								+ person + " ran into issues providing oxygen to " + suit.getName() + e.getMessage(),
						null);
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
				LogConsolidated.log(logger,  Level.SEVERE, 10_000, sourceName, "[" + person.getLocale() + "] "
								+ person + " ran into issues providing water to " + suit.getName(), e);
			}

			String loc = person.getImmediateLocation();
			loc = loc == null ? "[N/A]" : loc;
			loc = loc.equalsIgnoreCase("Outside") ? loc.toLowerCase() : "in " + loc;
			
			// Return suit to entity's inventory.
			LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
					"[" + person.getLocale() + "] " + person.getName() 
					+ " " + loc + " loaded up "  + suit.getName() + ".");
		}
	}

	@Override
	public void endTask() {
		// Clear the person as the airlock operator if task ended prematurely.
		if (airlock != null && person.getName().equals(airlock.getOperatorName())) {
			String loc = "";
			if (airlock.getEntity() instanceof Vehicle) {
				loc = person.getVehicle().getName(); //airlock.getEntityName();
				LogConsolidated.log(logger, Level.FINER, 1_000, sourceName,
						"[" + loc + "] "
						+ person + " concluded the vehicle airlock operator task.");
			}
			else {//if (airlock.getEntity() instanceof Settlement) {
				loc = ((Building) (airlock.getEntity())).getSettlement().getName();
				LogConsolidated.log(logger, Level.FINER, 1_000, sourceName,
						"[" + loc + "] "
						+ person + " concluded the airlock operator task.");
			}

		}
		
		airlock.removeID(id);
		
		super.endTask();
	}

	@Override
	public int getEffectiveSkillLevel() {
		return person.getSkillManager().getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
	}

	@Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<SkillType>(1);
		results.add(SkillType.EVA_OPERATIONS);
		return results;
	}

	@Override
	public void destroy() {
		super.destroy();

		airlock = null;
	}
}

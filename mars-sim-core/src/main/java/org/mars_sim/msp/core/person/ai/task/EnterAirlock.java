/**
 * Mars Simulation Project
 * EnterAirlock.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.mars.MarsSurface;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * The EnterAirlock class is a Task for EVA ingress, namely, entering an airlock of a settlement or 
 * vehicle after an EVA operation outside have been accomplished.
 */
public class EnterAirlock extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(EnterAirlock.class.getName());
	private static String loggerName = logger.getName();
	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());
	
	/** Task name */
	private static final String NAME = Msg.getString("Task.description.enterAirlock"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase REQUEST_INGRESS = new TaskPhase(
			Msg.getString("Task.phase.requestIngress")); //$NON-NLS-1$
	private static final TaskPhase LOCK_INNER_DOOR = new TaskPhase(
			Msg.getString("Task.phase.lockInnerDoor")); //$NON-NLS-1$
	private static final TaskPhase DEPRESSURIZE_CHAMBER = new TaskPhase(
			Msg.getString("Task.phase.depressurizeChamber")); //$NON-NLS-1$
	private static final TaskPhase UNLOCK_OUTER_DOOR = new TaskPhase(
			Msg.getString("Task.phase.unlockOuterDoor")); //$NON-NLS-1$
	private static final TaskPhase ENTER_AIRLOCK = new TaskPhase(
			Msg.getString("Task.phase.enterAirlock")); //$NON-NLS-1$
	private static final TaskPhase LOCK_OUTER_DOOR = new TaskPhase(
			Msg.getString("Task.phase.lockOuterDoor")); //$NON-NLS-1$
	private static final TaskPhase WALK_TO_CHAMBER = new TaskPhase(
			Msg.getString("Task.phase.walkToChamber")); //$NON-NLS-1$
	private static final TaskPhase PRESSURIZE_CHAMBER = new TaskPhase(
			Msg.getString("Task.phase.pressurizeChamber")); //$NON-NLS-1$
	private static final TaskPhase UNLOCK_INNER_DOOR = new TaskPhase(
			Msg.getString("Task.phase.unlockInnerDoor")); //$NON-NLS-1$
	private static final TaskPhase DOFF_OFF_SUIT = new TaskPhase(
			Msg.getString("Task.phase.doffOffSuit")); //$NON-NLS-1$
	private static final TaskPhase CLEAN_UP = new TaskPhase(
			Msg.getString("Task.phase.cleanUp")); //$NON-NLS-1$
	private static final TaskPhase LEAVE_AIRLOCK = new TaskPhase(
			Msg.getString("Task.phase.leaveAirlock")); //$NON-NLS-1$
	
	//////////////////////////////////////////////////////
	
//	private static final TaskPhase WAITING_TO_ENTER_AIRLOCK = new TaskPhase(
//			Msg.getString("Task.phase.waitingToEnterAirlock")); //$NON-NLS-1$
//	private static final TaskPhase ENTERING_AIRLOCK = new TaskPhase(
//			Msg.getString("Task.phase.enteringAirlock")); //$NON-NLS-1$
//	private static final TaskPhase WAITING_INSIDE_AIRLOCK = new TaskPhase(
//			Msg.getString("Task.phase.waitingInsideAirlock")); //$NON-NLS-1$
//	private static final TaskPhase STORING_EVA_SUIT = new TaskPhase(
//			Msg.getString("Task.phase.storingEVASuit")); //$NON-NLS-1$
//	private static final TaskPhase EXITING_AIRLOCK = new TaskPhase(
//			Msg.getString("Task.phase.exitingAirlock")); //$NON-NLS-1$
	
	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .5D;

	private double remainingCleaningTime = 10 + RandomUtil.getRandomInt(-3, 3);
	
	// Data members
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
	 * @param airlock to be used.
	 */
	public EnterAirlock(Person person, Airlock airlock) {
		super(NAME, person, false, false, STRESS_MODIFIER, false, 0D);
		
		this.airlock = airlock;
		
		// Initialize data members
		setDescription(Msg.getString("Task.description.enterAirlock.detail", airlock.getEntityName())); // $NON-NLS-1$
		// Initialize task phase
		
		addPhase(REQUEST_INGRESS);
		addPhase(LOCK_INNER_DOOR);
		addPhase(DEPRESSURIZE_CHAMBER);
		addPhase(UNLOCK_OUTER_DOOR);
		addPhase(ENTER_AIRLOCK);
		addPhase(LOCK_OUTER_DOOR);
		addPhase(WALK_TO_CHAMBER);
		addPhase(PRESSURIZE_CHAMBER);
		addPhase(UNLOCK_INNER_DOOR);
		addPhase(DOFF_OFF_SUIT);
		addPhase(CLEAN_UP);
		addPhase(LEAVE_AIRLOCK);
		
		setPhase(REQUEST_INGRESS);
		
		//////////////////////////////////////////////////////
		
//		addPhase(WAITING_TO_ENTER_AIRLOCK);
//		addPhase(ENTERING_AIRLOCK);
//		addPhase(WAITING_INSIDE_AIRLOCK);
//		addPhase(STORING_EVA_SUIT);
//		addPhase(EXITING_AIRLOCK);
//
//		setPhase(WAITING_TO_ENTER_AIRLOCK);
		
		LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
				"[" + person.getLocale() + "] " + person.getName() 
				+ " was starting the EVA ingress procedure in " + airlock.getEntityName() + ".");
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
		} else if (LOCK_INNER_DOOR.equals(getPhase())) {
			return lockInnerDoor(time);
		} else if (DEPRESSURIZE_CHAMBER.equals(getPhase())) {
			return depressurizeChamber(time);
		} else if (UNLOCK_OUTER_DOOR.equals(getPhase())) {
			return unlockOuterDoor(time);
		} else if (ENTER_AIRLOCK.equals(getPhase())) {
			return enterAirlock(time);
		} else if (LOCK_OUTER_DOOR.equals(getPhase())) {
			return lockOuterDoor(time);
		} else if (WALK_TO_CHAMBER.equals(getPhase())) {
			return walkToChamber(time);
		} else if (PRESSURIZE_CHAMBER.equals(getPhase())) {
			return pressurizeChamber(time);
		} else if (UNLOCK_INNER_DOOR.equals(getPhase())) {
			return unlockInnerDoor(time);
		} else if (DOFF_OFF_SUIT.equals(getPhase())) {
			return doffOffSuit(time);
		} else if (CLEAN_UP.equals(getPhase())) {
			return cleanUp(time);	
		} else if (LEAVE_AIRLOCK.equals(getPhase())) {
			return leaveAirlock(time);		
	
		///////////////////////////////////////////////////////////////
			
//		} else if (WAITING_TO_ENTER_AIRLOCK.equals(getPhase())) {	
//			return waitingToEnterAirlockPhase(time);
//		} else if (ENTERING_AIRLOCK.equals(getPhase())) {
//			return enteringAirlockPhase(time);
//		} else if (WAITING_INSIDE_AIRLOCK.equals(getPhase())) {
//			return waitingInsideAirlockPhase(time);
//		} else if (STORING_EVA_SUIT.equals(getPhase())) {
//			return storingEVASuitPhase(time);
//		} else if (EXITING_AIRLOCK.equals(getPhase())) {
//			return exitingAirlockPhase(time);
			
		} else {
			return time;
		}
	}

	/**
	 * Request the entry of the airlock
	 * 
	 * @param time
	 * @return
	 */
	private double requestIngress(double time) {

		double remainingTime = 0;
		
		String loc = person.getLocationTag().getImmediateLocation();
		loc = loc == null ? "[N/A]" : loc;
		loc = loc.equalsIgnoreCase("Outside") ? loc.toLowerCase() : "in " + loc;
		
		LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
				"[" + person.getLocale() + "] " + person.getName() 
//				+ " " + loc 
				+ " requested ingress from EVA in " + airlock.getEntity().toString() + ".");
		
		
		//TODO: look for an available, depressurized airlock chamber
		// if not, find an empty one and begin depressurization

		if (exteriorDoorPos == null) {
			exteriorDoorPos = airlock.getAvailableExteriorPosition();
		}
				
		Point2D personLocation = new Point2D.Double(person.getXLocation(), person.getYLocation());
		
		if (!person.isOutside()) {
			endTask();
		}
	
		if (LocalAreaUtil.areLocationsClose(personLocation, exteriorDoorPos)) {
			
			if (airlock.hasSpace()) {
				// Add person to queue awaiting airlock at inner door if not already.
				if (airlock.addAwaitingAirlockOuterDoor(person)) {
					// Add experience
					addExperience(time);
					
					setPhase(LOCK_INNER_DOOR);
				}
			}
			
			else {
				// will have to wait
				LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
						"[" + person.getLocale() + "] " + airlock.getEntity().toString() + " " 
						+ " had " + airlock.getNumOccupants() + " occupants. Will have to wait until the chamber is clear.");
			
				// TODO: record/track the wait time
			}		
		}
			
		else {
			// Walk to exterior door position.
			addSubTask(new WalkOutside(person, person.getXLocation(), 
				person.getYLocation(), exteriorDoorPos.getX(),
				exteriorDoorPos.getY(), true));
			
			
			LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
					"[" + person.getLocale() + "] " + person.getName()
//						+ " " + loc 
					+ " attempted to come closer to the exterior door of the airlock chamber.");
		}
		
		return remainingTime;
	}

	
	private double lockInnerDoor(double time) {

		double remainingTime = 0;
				
		if (airlock.hasSpace()) {
			
			if (!airlock.isInnerDoorLocked()) {
				
				String loc = person.getLocationTag().getImmediateLocation();
				loc = loc == null ? "[N/A]" : loc;
				loc = loc.equalsIgnoreCase("Outside") ? loc.toLowerCase() : "in " + loc;
				
				LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
						"[" + person.getLocale() + "] " + person.getName() 
//						+ " " + loc 
						+ " locked inner door in " + airlock.getEntity().toString() + " to prevent EVA egress.");
				
				// Lock the inner door to bar people from walking into the chamber
				airlock.setInnerDoorLocked(true);
			}
			
			if (airlock.isInnerDoorLocked()) {
				// Add experience
				addExperience(time);
				
				setPhase(DEPRESSURIZE_CHAMBER);		
			}
		}
		
		else {
			// will have to wait
			LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
					"[" + person.getLocale() + "] " + airlock.getEntity().toString() + " " 
					+ " had " + airlock.getNumOccupants() + " occupants, waiting for a chamber to clear.");
		
			// TODO: record/track the wait time
		}
		
		return remainingTime;
	}
	
	
	private double depressurizeChamber(double time) {

		double remainingTime = 0;
		
		if (airlock.isDepressurized()) {
			// If it stops adding or subtracting air, 
			// then airlock has been depressurized, 
			// ready to unlock the outer door
	
			LogConsolidated.log(logger, Level.INFO, 4000, sourceName,
				"[" + person.getLocale() 
				+ "] The chamber had just been depressurized for EVA ingress in " 
				+ airlock.getEntity().toString() + ".");
			
			// Add experience
			addExperience(time);
			
			setPhase(UNLOCK_OUTER_DOOR);
		}
		
		else if (!airlock.isDepressurizing()) {
			airlock.setDepressurizing();
		}
		
		if (airlock.isDepressurizing()) {
			LogConsolidated.log(logger, Level.INFO, 4000, sourceName,
					"[" + person.getLocale() 
					+ "] The chamber is depressurizing in " 
					+ airlock.getEntity().toString() + ".");
			
			if (!airlock.isActivated())
				// Enable someone to be selected as an airlock operator
				airlock.setActivated(true);
			
			// Elect an operator to handle this task
			// Add air cycle time untill it is fully depressurized
			airlock.addTime(time);
		}
		
		return remainingTime;
	}
	
	
	private double unlockOuterDoor(double time) {

		double remainingTime = 0;
		
		// First, unlock the outer door
		if (airlock.isOuterDoorLocked()) {
			// Unlock the outer door
			airlock.setOuterDoorLocked(false);
		}
		
		// If it is unlock or can be unlocked
		if (!airlock.isOuterDoorLocked()) {
			LogConsolidated.log(logger, Level.INFO, 4000, sourceName,
				"[" + person.getLocale() 
				+ "] The exterior door in " 
				+ airlock.getEntity().toString() + " had been unlocked. Ready for entry.");
			
			// Add experience
			addExperience(time);
			
			setPhase(ENTER_AIRLOCK);
		}
		
			
		return remainingTime;
	}
	
	
	private double enterAirlock(double time) {

		double remainingTime = 0;
		
//		// transfer those awaiting into the chamber
		boolean canEnter = false;
		
		if (exteriorDoorPos == null) {
			exteriorDoorPos = airlock.getAvailableExteriorPosition();
		}
		
		Point2D personLocation = new Point2D.Double(person.getXLocation(), person.getYLocation());
			
		if (LocalAreaUtil.areLocationsClose(personLocation, exteriorDoorPos)) {
			
			String loc = person.getLocationTag().getImmediateLocation();
			loc = loc == null ? "[N/A]" : loc;
			loc = loc.equalsIgnoreCase("Outside") ? loc.toLowerCase() : "in " + loc;
			
			LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
					"[" + person.getLocale() + "] " + person.getName()
//					+ " " + loc 
					+ " came close enough to the exterior door of " + airlock.getEntity());
			
			if (!airlock.inAirlock(person)) {
				canEnter = airlock.enterAirlock(person, false); 
			}
		}
		
		else if (person.isOutside()) {
			// Walk to exterior door position.
			addSubTask(new WalkOutside(person, person.getXLocation(), 
				person.getYLocation(), exteriorDoorPos.getX(),
				exteriorDoorPos.getY(), true));
			
			String loc = person.getLocationTag().getImmediateLocation();
			loc = loc == null ? "[N/A]" : loc;
			loc = loc.equalsIgnoreCase("Outside") ? loc.toLowerCase() : "in " + loc;
			
			LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
					"[" + person.getLocale() + "] " + person.getName()
					+ " " + loc + " attempted to come closer to the exterior door of the airlock chamber.");
		}
		
		if (canEnter) {
					
			LogConsolidated.log(logger, Level.INFO, 4000, sourceName,
					"[" + person.getLocale() 
					+ "] " + person.getName() + " just entered throught the exterior door into " 
					+ airlock.getEntity().toString() + ".");
				
			// Add experience
			addExperience(time);
				
			setPhase(LOCK_OUTER_DOOR);
		}
		
		return remainingTime;
	}
	
	private double lockOuterDoor(double time) {

		double remainingTime = 0;
		
		// If it's unlock, lock it
		if (!airlock.isOuterDoorLocked()) {

			boolean result = false;

			if (!airlock.hasSpace()) {
				result = true;
			}

			// If there are people waiting at the outer door to come in, wait
			if (airlock.hasSpace() && !airlock.hasAwaitingOuterDoor()) {
				result = true;
			}
				
			if (result)
				// Lock the outer door
				airlock.setOuterDoorLocked(true);
		}

		// See if it is locked or can be locked
		if (airlock.isOuterDoorLocked()) {
			// Unlock the outer door
			LogConsolidated.log(logger, Level.INFO, 4000, sourceName,
					"[" + person.getLocale() 
					+ "] The exterior door in " 
					+ airlock.getEntity().toString() + " had been locked. Ready to pressurize");
				
			// Add experience
			addExperience(time);
				
			setPhase(WALK_TO_CHAMBER);
		}
		
		return remainingTime;
	}
	
	private double walkToChamber(double time) {
		
		double remainingTime = 0;
		
		String loc = person.getLocationTag().getImmediateLocation();
		loc = loc == null ? "[N/A]" : loc;
		loc = loc.equalsIgnoreCase("Outside") ? loc.toLowerCase() : "in " + loc;
		
		if (airlock.getEntity() instanceof Building) {
	
			Building airlockBuilding = (Building) airlock.getEntity();
	         	
			Point2D spot = airlockBuilding.getEVA().getAvailableActivitySpot(person);
	 				
			LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
						"[" + person.getLocale() + "] " + person.getName() 
						+ " " + loc 
						+ " walked to an available activity spot.");
				
	 		// Walk to interior airlock position.
			addSubTask(new WalkSettlementInterior(person, airlockBuilding, 
					spot.getX(), spot.getY(), 0));
		}
	     
		else if (airlock.getEntity() instanceof Rover) {
	
			Rover airlockRover = (Rover) airlock.getEntity();
	         		 
	 		if (insideAirlockPos == null) {
	 			insideAirlockPos = airlock.getAvailableAirlockPosition();
			}
	 		
	 		LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
						"[" + person.getLocale() + "] " + person.getName() 
						+ " " + loc + " walked to the reference position.");
				
	 		// Walk to interior airlock position.
	 		addSubTask(new WalkRoverInterior(person, airlockRover, 
	 				insideAirlockPos.getX(), insideAirlockPos.getY()));
		}
     
        // This endTask() is needed for ending the walking sub task.
        endTask();
        
 		// Add experience
 		addExperience(time);
 			
 		setPhase(PRESSURIZE_CHAMBER);
 		
		return remainingTime;
		
	}
	
	private double pressurizeChamber(double time) {

		double remainingTime = 0;
	
		if (airlock.isPressurized()) {
			// If it stops adding or subtracting air, 
			// then airlock has been depressurized, 
			// ready to unlock the outer door
	
			LogConsolidated.log(logger, Level.INFO, 4000, sourceName,
				"[" + person.getLocale() 
				+ "] The chamber had just been pressurized in " 
				+ airlock.getEntity().toString() + ".");
			
			// Add experience
			addExperience(time);
			
			setPhase(UNLOCK_INNER_DOOR);
		}
		
		else if (!airlock.isPressurizing()) {
			// Pressurizing the chamber
			airlock.setPressurizing();
		}
			
		if (airlock.isPressurizing()) {
			LogConsolidated.log(logger, Level.INFO, 4000, sourceName,
					"[" + person.getLocale() 
					+ "] The chamber is pressurizing in " 
					+ airlock.getEntity().toString() + ".");
			
			if (!airlock.isActivated())
				// Enable someone to be selected as an airlock operator
				airlock.setActivated(true);
			
			// TODO:Elect an operator to handle this task
			
			// Add air cycle time untill it is fully pressurized
			airlock.addTime(time);
		}
		
		return remainingTime;
	}
	
	
	private double unlockInnerDoor(double time) {

		double remainingTime = 0;
		
		// First, unlock the inner door
		if (airlock.isInnerDoorLocked()) {
			// Unlock the inner door
			airlock.setInnerDoorLocked(false);
		}
		
		// If it is unlock or can be unlocked
		if (!airlock.isInnerDoorLocked()) {
			LogConsolidated.log(logger, Level.INFO, 4000, sourceName,
				"[" + person.getLocale() 
				+ "] The interior door in " 
				+ airlock.getEntity().toString() 
				+ " had been unlocked. Ready to doff off the EVA suits and for others to come in to do EVA egress.");
			
			// Add experience
			addExperience(time);
			
			setPhase(DOFF_OFF_SUIT);
		}
		
				
		return remainingTime;
	}
	
	
	private double doffOffSuit(double time) {

		double remainingTime = 0;
		
		// 1. Gets the suit instance
		EVASuit suit = person.getSuit(); 
		// 2. deregister the suit the person will take into the airlock to don
		person.registerSuit(null);		

		String loc = person.getLocationTag().getImmediateLocation();
		loc = loc == null ? "[N/A]" : loc;
		loc = loc.equalsIgnoreCase("Outside") ? loc.toLowerCase() : "in " + loc;
		
		if (suit != null) {
			// 3. set the person as the owner
			suit.setLastOwner(person);
			
			Inventory suitInv = suit.getInventory();
		
			if (person.getContainerUnit() instanceof MarsSurface) {
				LogConsolidated.log(logger, Level.WARNING, 4000, sourceName,
						"[" + person.getLocale() + "] "  
						+ person + " " + loc + " still had MarsSurface as the container unit.");
			}
			
			else {
				// 4. Empty the EVA suit
				LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
						"[" + person.getLocale() + "] " + person.getName() 
						+ " " + loc + " was going to retrieve the O2 and H2O in " + suit.getName() + ".");
	
				Inventory entityInv = airlock.getEntityInventory();
				
				if (entityInv != null && suitInv != null) {
	
					// 5. Unloads the resources from the EVA suit to the entityEnv
					
					try {
						// 5a. Unload oxygen from suit.
						double oxygenAmount = suitInv.getAmountResourceStored(oxygenID, false);
						double oxygenCapacity = entityInv.getAmountResourceRemainingCapacity(oxygenID, true, false);
						if (oxygenAmount > oxygenCapacity)
							oxygenAmount = oxygenCapacity;
						
						suitInv.retrieveAmountResource(oxygenID, oxygenAmount);
						entityInv.storeAmountResource(oxygenID, oxygenAmount, true);
						entityInv.addAmountSupply(oxygenID, oxygenAmount);
		
					} catch (Exception e) {

						LogConsolidated.log(logger, Level.WARNING, 4000, sourceName, 
								"[" + person.getLocale() + "] " + person.getName() 
								+ " " + loc
								+ " but was unable to retrieve/store oxygen : ", e);
//						endTask();
					}
		
					// 5b. Unload water from suit.
					double waterAmount = suitInv.getAmountResourceStored(waterID, false);
					double waterCapacity = entityInv.getAmountResourceRemainingCapacity(waterID, true, false);
					if (waterAmount > waterCapacity)
						waterAmount = waterCapacity;
					
					try {
						suitInv.retrieveAmountResource(waterID, waterAmount);
						entityInv.storeAmountResource(waterID, waterAmount, true);
						entityInv.addAmountSupply(waterID, waterAmount);
		
					} catch (Exception e) {

						LogConsolidated.log(logger, Level.WARNING, 4000, sourceName, 
								"[" + person.getLocale() + "] " + person.getName() 
								+ " " + loc
								+ " but was unable to retrieve/store water : ", e);
//						endTask();
					}
		
					// 5c Transfer the EVA suit from person to entityInv 
					suit.transfer(person, entityInv);	
			
					// Return suit to entity's inventory.
					LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
							"[" + person.getLocale() + "] " + person.getName() 
							+ " " + loc 
							+ " had just stowed away "  + suit.getName() + ".");
					
					// Add experience
					addExperience(time);

					setPhase(CLEAN_UP);
				}
			}
		}
		
		else { // the person doesn't have the suit
			
			LogConsolidated.log(logger, Level.WARNING, 4000, sourceName,
					"[" + person.getLocale() + "] " 
					+ person.getName() + " " + loc 
					+ " was supposed to put away an EVA suit but somehow did not have one.");
			
			setPhase(CLEAN_UP);
		}
				
		return remainingTime;
	}
	
	
	private double cleanUp(double time) {

		double remainingTime = 0;
		
		remainingCleaningTime -= time;
		
		if (remainingCleaningTime <= 0) {
			LogConsolidated.log(logger, Level.INFO, 4000, sourceName,
					"[" + person.getLocale() + "] " 
					+ person.getName()
					+ " completed cleaning the EVA suit.");
			
			// Add experience
			addExperience(time);

			setPhase(LEAVE_AIRLOCK);
		}
		
		return remainingTime;
	}
	
	
	private double leaveAirlock(double time) {

		double remainingTime = 0;
		
		if (interiorDoorPos == null) {
			interiorDoorPos = airlock.getAvailableInteriorPosition();
		}
		
		Point2D personLocation = new Point2D.Double(person.getXLocation(), person.getYLocation());
		
		if (LocalAreaUtil.areLocationsClose(personLocation, interiorDoorPos)) {
			
			LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
					"[" + person.getLocale() + "] " + person.getName() 
					+ " came through the interior door to enter the settlement, thus concluding the EVA ingress.");

		}
		
		else {// the person is NOT close enough to the interior door position
							
    		String loc = person.getLocationTag().getImmediateLocation();
    		loc = loc == null ? "[N/A]" : loc;
    		loc = loc.equalsIgnoreCase("Outside") ? loc.toLowerCase() : "in " + loc;
    		
	           // Walk to interior airlock position.
            if (airlock.getEntity() instanceof Building) {

                Building airlockBuilding = (Building) airlock.getEntity();
                	
//               logger.finest(person + " exiting airlock inside " + airlockBuilding);
    			LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
    					"[" + person.getLocale() + "] " + person.getName() 
    					+ " " + loc + " walked close to the interior door and stepped through into the settlement.");
    			
                addSubTask(new WalkSettlementInterior(person, airlockBuilding, 
                        interiorDoorPos.getX(), interiorDoorPos.getY(), 0));   
            }
            
            else if (airlock.getEntity() instanceof Rover) {

                Rover airlockRover = (Rover) airlock.getEntity();
                
//                logger.finest(person + " exiting airlock inside " + airlockRover);
    			LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
    					"[" + person.getLocale() + "] " + person.getName() 
    					+ " " + loc + " tried to walk close to the interior door.");
    			
                addSubTask(new WalkRoverInterior(person, airlockRover, 
                        interiorDoorPos.getX(), interiorDoorPos.getY()));
            }
				
            // This endTask() is needed for ending the walking sub task.
            endTask();
		}
		
		// Add experience
		addExperience(time);		
		
		// This completes the task of ingress through the airlock
		// End EnterAirlock task
		endTask();
				
		return remainingTime;
	}	
	
	
//	/**
//	 * Performs the waiting to enter airlock phase of the task.
//	 * 
//	 * @param time the amount of time to perform the task phase.
//	 * @return the remaining time after performing the task phase.
//	 */
//	private double waitingToEnterAirlockPhase(double time) {
//
//		double remainingTime = time;
//		
//		String loc = person.getLocationTag().getImmediateLocation();
//		loc = loc == null ? "[N/A]" : loc;
//		loc = loc.equalsIgnoreCase("Outside") ? loc.toLowerCase() : "in " + loc;
//		
//		// If person is already inside, don't need the ingress. End the task.
//		if (person.isInside()) {
//			LogConsolidated.log(logger, Level.WARNING, 4000, sourceName, 
//					"[" + person.getLocale() + "] " + person.getName() + 
//					" was in the 'waiting to enter' phase for EVA ingress"
//					+ " but was reportedly inside. End the Task.");
//			
//			setPhase(ENTERING_AIRLOCK);
//			
//			return remainingTime;
//		}
//
//		else {
//			LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
//				"[" + person.getLocale() + "] " + person.getName() + " " 
//				+ loc + " was in the 'waiting to enter' phase for EVA ingress.");
//		
//		}
//		
//		
//		// The airlock is not being fully prepared for ingress yet.
//			
//		// Add person to queue awaiting airlock at inner door if not already.
//		airlock.addAwaitingAirlockOuterDoor(person);
//
//		loc = person.getLocationTag().getImmediateLocation();
//		loc = loc == null ? "[N/A]" : loc;
//		loc = loc.equalsIgnoreCase("Outside") ? loc.toLowerCase() : "in " + loc;
//		
//		// If airlock has not been activated, activate it.
//		if (!airlock.isActivated()) {
//			if (airlock.activateAirlock(person)) {
//				
//				LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
//						"[" + person.getLocale() + "] " + person.getName() 
//						+ " " + loc + 
//						" activated the airlock in 'waiting to enter' phase.");
//
//				
//			}
//			
//			else {
//				LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
//						"[" + person.getLocale() + "] " + person.getName()
//						+ " " + loc + " could not activate the airlock for EVA ingress.");
//				
//			}
//			
//		}
//		
//		else { // the airlock has activated
//			LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
//					"[" + person.getLocale() + "] " + person.getName()
//					+ " " + loc + " observed the airlock had been activated for ingress.");			
//		}
//		
//		// Check if person is the airlock operator.
//        if (person.equals(airlock.getOperator())) {
//            // If person is airlock operator, add cycle time to airlock.
//            double activationTime = remainingTime;
//            if (airlock.getRemainingCycleTime() < remainingTime) {
//                remainingTime -= airlock.getRemainingCycleTime();
//            }
//            else {
//                remainingTime = 0D;
//            }
//            boolean activationSuccessful = airlock.addCycleTime(activationTime);
//            if (!activationSuccessful) {
//
//				LogConsolidated.log(logger, Level.WARNING, 4000, sourceName, 
//						"[" + person.getLocale() + "] "
//						+ person.getName() + " " + loc 
//						+ " had problems with airlock activation in the 'waiting to enter' phase.");
//            }
//            
//            else {
//            	LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
//    					"[" + person.getLocale() + "] "
//    					+ person.getName() + " " + loc 
//    					+ " completed the airlock activation in the 'waiting to enter' phase. Standby.");    	
//            	
//            }
//        }
//        
//        else {
//            // If person is not airlock operator, just wait.
//			LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
//					"[" + person.getLocale() + "] "
//					+ person.getName() + " " + loc 
//					+ " was not the operator in the 'waiting to enter' phase. Standby.");				
//            remainingTime = 0D;
//        }
//
//		if (Airlock.AirlockState.DEPRESSURIZED.equals(airlock.getState()) 
//				&& !airlock.isOuterDoorLocked()) {
////    				&& airlock.inAirlock(person)) {
//		// If airlock has been depressurized with outer door unlocked, ready for entry.
//	
//			LogConsolidated.log(logger, Level.INFO, 4000, sourceName,
//				"[" + person.getLocale() 
//				+ "] The chamber had just been depressurized with the exterior door unlocked for EVA ingress in " 
//				+ airlock.getEntity().toString() + ".");
//			
//			// Add experience
//			addExperience(time - remainingTime);
//			
//			setPhase(ENTERING_AIRLOCK);
//		}
//		 
//		return remainingTime;
//	}
//
//	/**
//	 * Performs the enter airlock phase of the task.
//	 * 
//	 * @param time the amount of time to perform the task phase.
//	 * @return remaining time after performing task phase.
//	 */
//	private double enteringAirlockPhase(double time) {
//
//		double remainingTime = time;
//
//		if (exteriorDoorPos == null) {
//			exteriorDoorPos = airlock.getAvailableExteriorPosition();
//		}
//		
//		// If airlock has not been activated, activate it.
//        if (!airlock.isActivated()) {
//            airlock.activateAirlock(person);
//        }
//        
//		String loc = person.getLocationTag().getImmediateLocation();
//		loc = loc == null ? "[N/A]" : loc;
//		loc = loc.equalsIgnoreCase("Outside") ? loc.toLowerCase() : "in " + loc;
//		
//		LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
//				"[" + person.getLocale() + "] " + person.getName() 
//				+ " " + loc + " was in the 'entering the airlock' phase for EVA ingress.");
//		
//		Point2D personLocation = new Point2D.Double(person.getXLocation(), person.getYLocation());
//		
//		// If person is already inside, don't need the ingress. End the task.
//		if (person.isInside()) {
//			LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
//					"[" + person.getLocale() + "] " + person.getName() 
//					+ " already went inside " + loc + ". Ready to to 'waiting inside airlock' phase.");
//						
//			setPhase(WAITING_INSIDE_AIRLOCK);
//		}
//
////		else if (airlock.inAirlock(person)) {
////			
////			LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
////					"[" + person.getLocale() + "] " + person.getName() + 
////					" just went inside the airlock chamber. Ready to go to 'waiting inside airlock' phase.");
////			
////			// Add experience
////			addExperience(time - remainingTime);
////			
////			setPhase(WAITING_INSIDE_AIRLOCK);
////		}
//		
//		else if (LocalAreaUtil.areLocationsClose(personLocation, exteriorDoorPos)) {
//			
//			LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
//					"[" + person.getLocale() + "] " + person.getName()
//					+ " came close enough to the exterior door of the airlock chamber.");
//
//            // Check if person is the airlock operator.
////            if (person.equals(airlock.getOperator())) {
//                // If person is airlock operator, add cycle time to airlock.
//                double activationTime = remainingTime;
//                if (airlock.getRemainingCycleTime() < remainingTime) {
//                    remainingTime -= airlock.getRemainingCycleTime();
//                }
//                else {
//                    remainingTime = 0D;
//                }
//                boolean activationSuccessful = airlock.addCycleTime(activationTime);
//                if (!activationSuccessful) {
////	                    logger.severe("Problem with airlock activation: " + person.getName());
//                    LogConsolidated.log(logger, Level.WARNING, 4000, sourceName, 
//							"[" + person.getLocale() + "] "
//							+ person.getName() + " " + loc 
//							+ " had problems with airlock activation in the 'entering airlock' phase.");	                    
//                }
//                
//                else {
//        			LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
//        					"[" + person.getLocale() + "] "
//        					+ person.getName() + " " + loc 
//        					+ " completed the airlock activation in the 'entering airlock' phase. Standby.");	
//					
//        			// Enter airlock from outside
//        			// Note: make sure the param 'inside' is false
//        			if (airlock.enterAirlock(person, false)) {
//        				
//        				loc = person.getLocationTag().getImmediateLocation();
//        				loc = loc == null ? "[N/A]" : loc;
//        				loc = loc.equalsIgnoreCase("Outside") ? loc.toLowerCase() : "in " + loc;
//        				
//        				LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
//        						"[" + person.getLocale() + "] "
//        						+ person.getName() 
//        						+ " " + loc + " just entered the airlock chamber.");
//
//        				// Add experience
//        				addExperience(time - remainingTime);
//        				
//        				setPhase(WAITING_INSIDE_AIRLOCK);
//
//        			}
//                }
////            }
////            
////            else {
////                // If person is not airlock operator, just wait.
////    			LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
////    					"[" + person.getLocale() + "] "
////    					+ person.getName() + " " + loc 
////    					+ " was not the operator in the 'entering airlock' phase. Standby.");
////            			
////                remainingTime = 0D;
////            }
//		}
//		
//		else if (person.isOutside()) {
//			// Walk to exterior door position.
//			addSubTask(new WalkOutside(person, person.getXLocation(), 
//				person.getYLocation(), exteriorDoorPos.getX(),
//				exteriorDoorPos.getY(), true));
//			
//			LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
//					"[" + person.getLocale() + "] " + person.getName()
//					+ " " + loc + " attempted to come closer to the exterior door of the airlock chamber.");
//		}
//
//		return remainingTime;
//	}
//
//	/**
//	 * Performs the waiting inside airlock phase of the task.
//	 * 
//	 * @param time the amount of time to perform the task phase.
//	 * @return the remaining time after performing the task phase.
//	 */
//	private double waitingInsideAirlockPhase(double time) {
//
//		double remainingTime = time;
//
//		String loc = person.getLocationTag().getImmediateLocation();
//		loc = loc == null ? "[N/A]" : loc;
//		loc = loc.equalsIgnoreCase("Outside") ? loc.toLowerCase() : "in " + loc;
//		
//		if (airlock.inAirlock(person)) {
//
//            // If airlock has not been activated, activate it.
//            if (!airlock.isActivated()) {
//                airlock.activateAirlock(person);
//            }
//            
//			// Check if person is the airlock operator.
////            if (person.equals(airlock.getOperator())) {
//                // If person is airlock operator, add cycle time to airlock.
// 
////            }
//            
////            else {
////                // If person is not airlock operator, just wait.
////    			LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
////    					"[" + person.getLocale() + "] "
////    					+ person.getName() + " " + loc 
////    					+ " was not the operator in the 'waiting inside airlock' phase. Standby.");
////                remainingTime = 0D;
////            }
//        }
//
//        else { // not in the airlock(
//        	
////            logger.finer(person + " is already internal during waiting inside airlock phase.");
//			LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
//					"[" + person.getLocale() + "] "
//					+ person.getName() + " " + loc 
//					+ " was not in the airlock.");
//				
//        }
//	      
//		return remainingTime;
//	}
//
//	/**
//	 * Performs the storing EVA suit phase of the task.
//	 * 
//	 * @param time the amount of time to perform the task phase.
//	 * @return the remaining time after performing the task phase.
//	 */
//	private double storingEVASuitPhase(double time) {
//
//		double remainingTime = time;
//
////		EVASuit suit = (EVASuit) person.getInventory().findUnitOfClass(EVASuit.class); ////(EVASuit) person.getContainerUnit();
//		// 5.1. Gets the suit instance
//		EVASuit suit = person.getSuit(); 
//		// 5.2 deregister the suit the person will take into the airlock to don
//		person.registerSuit(null);		
//
//		String loc = person.getLocationTag().getImmediateLocation();
//		loc = loc == null ? "[N/A]" : loc;
//		loc = loc.equalsIgnoreCase("Outside") ? loc.toLowerCase() : "in " + loc;
//		
//		if (suit != null) {
//			// 5.3 set the person as the owner
//			suit.setLastOwner(person);
//			
//			Inventory suitInv = suit.getInventory();
//		
//			if (person.getContainerUnit() instanceof MarsSurface) {
//				LogConsolidated.log(logger, Level.WARNING, 4000, sourceName,
//						"[" + person.getLocale() + "] "  
//						+ person + " " + loc + " still had MarsSurface as the container unit.");
//			}
//			
//			else {
//				// Empty the EVA suit
//				LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
//						"[" + person.getLocale() + "] " + person.getName() 
//						+ " " + loc + " was going to retrieve the O2 and H2O in " + suit.getName() + ".");
//	
//				Inventory entityInv = airlock.getEntityInventory();
//				
//				if (entityInv != null && suitInv != null) {
//	
//					// 5.4 Unloads the resources from the EVA suit to the entityEnv
//					
//					try {
//						// Unload oxygen from suit.
//						double oxygenAmount = suitInv.getAmountResourceStored(oxygenID, false);
//						double oxygenCapacity = entityInv.getAmountResourceRemainingCapacity(oxygenID, true, false);
//						if (oxygenAmount > oxygenCapacity)
//							oxygenAmount = oxygenCapacity;
//						
//						suitInv.retrieveAmountResource(oxygenID, oxygenAmount);
//						entityInv.storeAmountResource(oxygenID, oxygenAmount, true);
//						entityInv.addAmountSupply(oxygenID, oxygenAmount);
//		
//					} catch (Exception e) {
//
//						LogConsolidated.log(logger, Level.WARNING, 4000, sourceName, 
//								"[" + person.getLocale() + "] " + person.getName() 
//								+ " " + loc
//								+ " but was unable to retrieve/store oxygen : ", e);
////						endTask();
//					}
//		
//					// Unload water from suit.
//					double waterAmount = suitInv.getAmountResourceStored(waterID, false);
//					double waterCapacity = entityInv.getAmountResourceRemainingCapacity(waterID, true, false);
//					if (waterAmount > waterCapacity)
//						waterAmount = waterCapacity;
//					
//					try {
//						suitInv.retrieveAmountResource(waterID, waterAmount);
//						entityInv.storeAmountResource(waterID, waterAmount, true);
//						entityInv.addAmountSupply(waterID, waterAmount);
//		
//					} catch (Exception e) {
//
//						LogConsolidated.log(logger, Level.WARNING, 4000, sourceName, 
//								"[" + person.getLocale() + "] " + person.getName() 
//								+ " " + loc
//								+ " but was unable to retrieve/store water : ", e);
////						endTask();
//					}
//		
//					// 5.5 Transfer the EVA suit from person to entityInv 
//					suit.transfer(person, entityInv);	
//			
//					// Return suit to entity's inventory.
//					LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
//							"[" + person.getLocale() + "] " + person.getName() 
//							+ " " + loc 
//							+ " had just stowed away "  + suit.getName() + ".");
//					
//					// Add experience
//					addExperience(remainingTime);
//
//					setPhase(EXITING_AIRLOCK);
//				}
//			}
//		}
//		
//		else { // the person doesn't have the suit
//			
//			LogConsolidated.log(logger, Level.WARNING, 4000, sourceName,
//					"[" + person.getLocale() + "] " 
//					+ person.getName() + " " + loc 
//					+ " was supposed to put away an EVA suit but somehow did not have one.");
//			
//			setPhase(EXITING_AIRLOCK);
//		}
//
//		return remainingTime;
//	}
//	
//	/**
//	 * Performs the exit airlock phase of the task.
//	 * 
//	 * @param time the amount of time to perform the task phase.
//	 * @return the remaining time after performing the task phase.
//	 */
//	private double exitingAirlockPhase(double time) {
//
//		double remainingTime = time;
//
//		if (interiorDoorPos == null) {
//			interiorDoorPos = airlock.getAvailableInteriorPosition();
//		}
//		
//        // If airlock has not been activated, activate it.
//        if (!airlock.isActivated()) {
//            airlock.activateAirlock(person);
//        }
//        
//        double activationTime = remainingTime;
//        if (airlock.getRemainingCycleTime() < remainingTime) {
//            remainingTime -= airlock.getRemainingCycleTime();
//        }
//        else {
//            remainingTime = 0D;
//        }
//        
//        boolean activationSuccessful = airlock.addCycleTime(activationTime);
//        
//		String loc = person.getLocationTag().getImmediateLocation();
//		loc = loc == null ? "[N/A]" : loc;
//		loc = loc.equalsIgnoreCase("Outside") ? loc.toLowerCase() : "in " + loc;
//
//        if (!activationSuccessful) {
//			LogConsolidated.log(logger, Level.WARNING, 4000, sourceName, 
//					"[" + person.getLocale() + "] "
//					+ person.getName() + " " + loc 
//					+ " had problems with airlock activation in the 'exiting airlock' phase.");		
//			
//			 remainingTime = 0D;
//        }
//        
//        else {
//			LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
//					"[" + person.getLocale() + "] "
//					+ person.getName() + " " + loc 
//					+ " activated airlock successfully. Check if it's closer to the interior door enough.");
//			
//			 remainingTime = 0D;
//        }
//        
//		// logger.finer(person + " exiting airlock inside.");
//		LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
//				"[" + person.getLocale() + "] " + person.getName() 
//				+ " was about to come through the interior door to complete the ingress.");
//?
//		Point2D personLocation = new Point2D.Double(person.getXLocation(), person.getYLocation());
//		
//		if (LocalAreaUtil.areLocationsClose(personLocation, interiorDoorPos)) {
//			
//			LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
//					"[" + person.getLocale() + "] " + person.getName() 
//					+ " came close enough to the interior door to enter the settlement.");
//
//			// Add experience
//			addExperience(time - remainingTime);
//			
//			// This completes the task of ingress through the airlock
//			endTask();
//		}
//		
//		else {// the person is NOT close enough to the interior door position
//								
//	           // Walk to interior airlock position.
//            if (airlock.getEntity() instanceof Building) {
//
//                Building airlockBuilding = (Building) airlock.getEntity()
//                		;
////                logger.finest(person + " exiting airlock inside " + airlockBuilding);
//    			LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
//    					"[" + person.getLocale() + "] " + person.getName() 
//    					+ " " + loc + " tried to walk close to the interior door.");
//    			
//                addSubTask(new WalkSettlementInterior(person, airlockBuilding, 
//                        interiorDoorPos.getX(), interiorDoorPos.getY(), 0));
//            }
//            
//            else if (airlock.getEntity() instanceof Rover) {
//
//                Rover airlockRover = (Rover) airlock.getEntity();
//                
////                logger.finest(person + " exiting airlock inside " + airlockRover);
//    			LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
//    					"[" + person.getLocale() + "] " + person.getName() 
//    					+ " " + loc + " tried to walk close to the interior door.");
//    			
//                addSubTask(new WalkRoverInterior(person, airlockRover, 
//                        interiorDoorPos.getX(), interiorDoorPos.getY()));
//            }
//
//			// Walk toward the reference point inside the airlock
////			walkToReference(loc);
//		}
//		
//		return remainingTime;
//	}

//	/**
//	 * Walks toward the reference inside of the airlock
//	 * 
//	 * @param loc
//	 */
//	private void walkToReference(String loc) {
//		if (airlock.getEntity() instanceof Building) {
//			
//			Building airlockBuilding = (Building) airlock.getEntity();
//
//			if (airlockBuilding != null) {
//
//				Building startBuilding = BuildingManager.getBuilding(person);
//				
//				if (startBuilding != null) {
//					
//					LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
//							"[" + person.getLocale() + "] " + person.getName() 
//							+ " attempted to step closer to the interior door of the airlock.");
//					
//					// Walk to interior airlock position.
//					addSubTask(new WalkSettlementInterior(person, airlockBuilding, interiorAirlockPos.getX(),
//							interiorAirlockPos.getY(), 0));
//				}
//				
//				else {
//					LogConsolidated.log(logger, Level.WARNING, 4000, sourceName, 
//							"[" + person.getLocale() + "] " + person.getName() 
//							+ " was not inside a building. Ending the task.");
//					endTask();
//				}
//			} 
//			
//			else {
//				LogConsolidated.log(logger, Level.WARNING, 4000, sourceName, 
//						"[" + person.getLocale() + "] " + person.getName() 
//						+ " was waiting to enter airlock but airlockBuilding is null.");
//				endTask();
//			}
//
//		} else if (airlock.getEntity() instanceof Rover) {
//
//			Rover airlockRover = (Rover) airlock.getEntity();
//			
//			LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
//					"[" + person.getLocale() + "] " + person.getName() 
//					+ " attempted to walk toward the internal airlock in " + airlockRover);
//			
//			addSubTask(new WalkRoverInterior(person, airlockRover, interiorAirlockPos.getX(),
//					interiorAirlockPos.getY()));
//		}
//	}
	

	/**
	 * Adds experience to the person's skills used in this task.
	 * 
	 * @param time the amount of time (ms) the person performed this task.
	 */
	protected void addExperience(double time) {

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
	 * Checks if a person can enter an airlock from an EVA.
	 * 
	 * @param person  the person trying to enter
	 * @param airlock the airlock to be used.
	 * @return true if person can enter the airlock
	 */
	public static boolean canEnterAirlock(Person person, Airlock airlock) {

		boolean result = true;

		if (person.isInside()) {
			LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
					"[" + person.getLocale() + "] " + person.getName() 
					+ " could not enter airlock to " + airlock.getEntityName()
					+ " since he/she is already inside and not being outside.");
			result = false;
		}

		return result;
	}

	@Override
	public void endTask() {
		// Clear the person as the airlock operator if task ended prematurely.
		if ((airlock != null) && person.equals(airlock.getOperator())) {
			LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
					"[" + person.getLocale() + "] " + person.getName() 
					+ " concluded the airlock operator task.");
//					+  person.getLocationTag().getImmediateLocation() + ".");
			airlock.clearOperator();
		}
		
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

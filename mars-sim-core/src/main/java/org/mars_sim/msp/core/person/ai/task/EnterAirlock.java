/**
 * Mars Simulation Project
 * EnterAirlock.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

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
	private static final TaskPhase DEPRESSURIZE_CHAMBER = new TaskPhase(
			Msg.getString("Task.phase.depressurizeChamber")); //$NON-NLS-1$
	private static final TaskPhase ENTER_AIRLOCK = new TaskPhase(
			Msg.getString("Task.phase.enterAirlock")); //$NON-NLS-1$
	private static final TaskPhase WALK_TO_CHAMBER = new TaskPhase(
			Msg.getString("Task.phase.walkToChamber")); //$NON-NLS-1$
	private static final TaskPhase PRESSURIZE_CHAMBER = new TaskPhase(
			Msg.getString("Task.phase.pressurizeChamber")); //$NON-NLS-1$
	private static final TaskPhase DOFF_EVA_SUIT = new TaskPhase(
			Msg.getString("Task.phase.doffEVASuit")); //$NON-NLS-1$
	private static final TaskPhase CLEAN_UP = new TaskPhase(
			Msg.getString("Task.phase.cleanUp")); //$NON-NLS-1$
	private static final TaskPhase LEAVE_AIRLOCK = new TaskPhase(
			Msg.getString("Task.phase.leaveAirlock")); //$NON-NLS-1$

	// Static members
	/** The standard time for doffing the EVA suit. */
	private static final double STANDARD_DOFFING_TIME = 10;
	/** The standard time for cleaning oneself and the EVA suit. */
	private static final double STANDARD_CLEANINNG_TIME = 15;
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .1D;
	/** The time it takes to clean up oneself and the EVA suit. */
	private double remainingCleaningTime;
	/** The time it takes to doff an EVA suit. */
	private double remainingDoffingTime; ;
	
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
		super(NAME, person, false, false, STRESS_MODIFIER, SkillType.EVA_OPERATIONS, 100D);
		
		this.airlock = airlock;
		
		// Initialize data members
		setDescription(Msg.getString("Task.description.enterAirlock.detail", airlock.getEntityName())); // $NON-NLS-1$
		// Initialize task phase
		
		addPhase(REQUEST_INGRESS);
		addPhase(DEPRESSURIZE_CHAMBER);
		addPhase(ENTER_AIRLOCK);
		addPhase(WALK_TO_CHAMBER);
		addPhase(PRESSURIZE_CHAMBER);
		addPhase(DOFF_EVA_SUIT);
		addPhase(CLEAN_UP);
		addPhase(LEAVE_AIRLOCK);
		
		setPhase(REQUEST_INGRESS);
	
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
		} else if (DEPRESSURIZE_CHAMBER.equals(getPhase())) {
			return depressurizeChamber(time);
		} else if (ENTER_AIRLOCK.equals(getPhase())) {
			return enterAirlock(time);
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
	 * Transitions the person into a particular zone
	 * 
	 * @param zone the destination
	 * @return true if the transition is successful
	 */
	private boolean transitionTo(int zone) {
	
		if (isInZone(zone)) {
			return true;
		}
		
		else {
			int previousZone = zone + 1;
		
			Point2D newPos = fetchNewPos(zone);
			
			if (newPos != null) {

				if (airlock.occupy(zone, newPos, id)) {

					if (previousZone <= 4) {
					
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
	 * @param zone
	 * @return true if the person is a particular zone
	 */
	private boolean isInZone(int zone) {	
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
		
		return newPos;
	}
	
	/**
	 * Moves the person to a particular zone
	 * 
	 * @param newPos the target position in that zone
	 * @param zone
	 */
	private void moveThere(Point2D newPos, int zone) {
//		if (person.getName().contains("Aliena")) System.out.println(person + "::moveThere (" + person.getXLocation() + ", " + person.getYLocation() + ")");
//		System.out.println(person + "::moveThere");
		if (zone == 2) {
			walkToEVASpot((Building)airlock.getEntity());
		}
		
		else if (zone == 4) {	
			addSubTask(new WalkOutside(person, 
					person.getXLocation(), 
					person.getYLocation(), 
					newPos.getX(),
					newPos.getY(), true));
		}
		
		else {
//			if (person.getName().contains("Aliena")) System.out.println("EnterAirlock::moveThere calling WalkSettlementInterior by " + person);
//			addSubTask(new WalkSettlementInterior(person, (Building)airlock.getEntity(), 
//					newPos.getX(),
//					newPos.getY(), 0));
			person.setXLocation(newPos.getX());
			person.setYLocation(newPos.getY());
		}
		
		LogConsolidated.log(logger, Level.FINE, 4000, sourceName, 
				"[" + person.getLocale() + "] " + person.getName()
//			+ " " + loc
			+ " arrived at (" 
			+ Math.round(newPos.getX()*100.0)/100.0 + ", " 
			+ Math.round(newPos.getY()*100.0)/100.0 + ") in airlock zone " + zone + ".");
	}
	
	
	/**
	 * Request the entry of the airlock
	 * 
	 * @param time
	 * @return
	 */
	private double requestIngress(double time) {

		double remainingTime = 0;
		
		if (!person.isOutside()) {
			endTask();
		}
		
//		String loc = person.getLocationTag().getModifiedLoc();
	
		LogConsolidated.log(logger, Level.FINE, 20_000, sourceName, 
				"[" + person.getLocale() + "] " + person.getName() 
//				+ " " + loc 
				+ " requested ingress from EVA in " + airlock.getEntity().toString() + ".");
		
		boolean canEnter = false;
		
		if (airlock.hasSpace()) {
			
			if (!airlock.isActivated()) {
				// Enable someone to be selected as an airlock operator
				airlock.setActivated(true);
			}	
		}

		if (airlock.getEntity() instanceof Building) {
			// Load up the EVA activity spots
			airlock.loadEVAActivitySpots();

			if (airlock.addAwaitingOuterDoor(person, id)) {
				
				if (transitionTo(4)) {
					
					if (!airlock.isOuterDoorLocked() 
							|| airlock.isEmpty()) {
						// The outer door will stay locked if the chamber is NOT depressurized
						// Will allow the person come in either case
						canEnter = true;
					}
				}
			}
			
			else {
				LogConsolidated.log(logger, Level.INFO, 20_000, sourceName, 
						"[" + person.getLocale() + "] " + person.getName() 
						+ " outside"
						+ " could not find a space near the outer door in " + airlock.getEntity().toString() + ".");
				endTask();
				return 0;
			}
		}
		
		else if (airlock.getEntity() instanceof Rover) {
			
			if (exteriorDoorPos == null) {
				exteriorDoorPos = airlock.getAvailableExteriorPosition();
			}

			if (LocalAreaUtil.areLocationsClose(new Point2D.Double(person.getXLocation(), person.getYLocation()), exteriorDoorPos)) {
				
				if (airlock.addAwaitingOuterDoor(person, id)) {			
					canEnter = true;
				}
			}
			
			else {
				Rover airlockRover = (Rover) airlock.getEntity();
		         		 
				// Walk to exterior door position.
				addSubTask(new WalkOutside(person, person.getXLocation(), 
					person.getYLocation(), exteriorDoorPos.getX(),
					exteriorDoorPos.getY(), true));
							
				LogConsolidated.log(logger, Level.FINE, 4000, sourceName, 
						"[" + person.getLocale() + "] " + person.getName()
//							+ " " + loc 
						+ " attempted to step closer to the airlock's exterior door in " + airlockRover);
			}	
		}

		if (canEnter) {
			
			if (airlock.isDepressurized() && !airlock.isOuterDoorLocked()) {
				// If it stops adding or subtracting air, 
				// then airlock has been depressurized, 
				// ready to unlock the outer door
		
				LogConsolidated.log(logger, Level.FINE, 4000, sourceName,
					"[" + person.getLocale() 
					+ "] The chamber had just been depressurized for EVA ingress in " 
					+ airlock.getEntity().toString() + ".");
				
				// Add experience
				addExperience(time);
				
				setPhase(ENTER_AIRLOCK);
			}
			
			else if (!airlock.isActivated())
				// Enable someone to be selected as an airlock operator
				airlock.setActivated(true);		
			
			if (airlock.isOperator(id)) {
				// Add experience
				addExperience(time);
				LogConsolidated.log(logger, Level.FINE, 4000, sourceName,
						"[" + person.getLocale() 
						+ "] " + person + " was ready to depressurize the chamber.");
				setPhase(DEPRESSURIZE_CHAMBER);	
			}
		}
		
		return remainingTime;
	}

	
	private double depressurizeChamber(double time) {

		double remainingTime = 0;
		
		if (airlock.isDepressurized() && !airlock.isOuterDoorLocked()) {
			// If it stops adding or subtracting air, 
			// then airlock has been depressurized, 
			// ready to unlock the outer door
	
			LogConsolidated.log(logger, Level.FINE, 4000, sourceName,
				"[" + person.getLocale() 
				+ "] The chamber had just been depressurized for EVA ingress in " 
				+ airlock.getEntity().toString() + ".");
			
			// Add experience
			addExperience(time);
			
			setPhase(ENTER_AIRLOCK);
		}
		
		else if (!airlock.isDepressurizing()) {
			if (!airlock.someoneHasNoEVASuit()) {
				LogConsolidated.log(logger, Level.FINE, 4_000, sourceName,
						"[" + person.getLocale() 
						+ "] The chamber started depressurizing in " 
						+ airlock.getEntity().toString() + ".");
				// Depressurizing the chamber
				airlock.setDepressurizing();
			}
		}
		
		if (airlock.isDepressurizing()) {
//			LogConsolidated.log(logger, Level.FINE, 4000, sourceName,
//					"[" + person.getLocale() 
//					+ "] The chamber was depressurizing in " 
//					+ airlock.getEntity().toString() + ".");
			
			// Elect an operator to handle this task
			// Add air cycle time untill it is fully depressurized
			remainingTime =  time - airlock.addTime(time);
		}
		
		return remainingTime;
	}

	
	private double enterAirlock(double time) {

		double remainingTime = 0;

		boolean canEnter = false;

		if (airlock.getEntity() instanceof Building) {

			if (exteriorDoorPos == null) {
				exteriorDoorPos = airlock.getAvailableExteriorPosition();
			}

			if (LocalAreaUtil.areLocationsClose(new Point2D.Double(person.getXLocation(), person.getYLocation()), exteriorDoorPos)) {
				
				if (airlock.hasSpace() && !airlock.isOuterDoorLocked()) {
					
					if (!airlock.inAirlock(person)) {
						canEnter = airlock.enterAirlock(person, id, false); 
					}
					
					else
						canEnter = true;
					
					if (canEnter && transitionTo(3)) {
						canEnter = true;
					}
				}
			}
			
			else {
	         		 
				// Walk to exterior door position.
				addSubTask(new WalkOutside(person, person.getXLocation(), 
					person.getYLocation(), exteriorDoorPos.getX(),
					exteriorDoorPos.getY(), true));
				
				
				LogConsolidated.log(logger, Level.FINE, 4000, sourceName, 
						"[" + person.getLocale() + "] " + person.getName()
//							+ " " + loc 
						+ " attempted to come closer to the airlock's exterior door in " + airlock.getEntity());
			}			
		}
		
		else if (airlock.getEntity() instanceof Rover) {
			
			if (exteriorDoorPos == null) {
				exteriorDoorPos = airlock.getAvailableExteriorPosition();
			}

			if (LocalAreaUtil.areLocationsClose(new Point2D.Double(person.getXLocation(), person.getYLocation()), exteriorDoorPos)) {
				
				if (airlock.hasSpace() && !airlock.isOuterDoorLocked()) {
					
					if (!airlock.inAirlock(person)) {
						canEnter = airlock.enterAirlock(person, id, false); 
					}
					
					else
						canEnter = true;
					
					if (canEnter && transitionTo(3)) {
						canEnter = true;
					}
				}
			}
			
			else {
//				Rover airlockRover = (Rover) airlock.getEntity();
		         		 
				// Walk to exterior door position.
				addSubTask(new WalkOutside(person, person.getXLocation(), 
					person.getYLocation(), exteriorDoorPos.getX(),
					exteriorDoorPos.getY(), true));
				
				
				LogConsolidated.log(logger, Level.FINE, 4000, sourceName, 
						"[" + person.getLocale() + "] " + person.getName()
//							+ " " + loc 
						+ " attempted to come closer to the airlock's exterior door in " + airlock.getEntity());
			}	
		}

		if (canEnter) {
			
			LogConsolidated.log(logger, Level.FINE, 4000, sourceName,
					"[" + person.getLocale() 
					+ "] " + person.getName() + " just entered through the exterior door into " 
					+ airlock.getEntity().toString() + ".");
				
			// Add experience
			addExperience(time);
				
			setPhase(WALK_TO_CHAMBER);
		}
		
		return remainingTime;
	}
	
	
	private double walkToChamber(double time) {
		
		double remainingTime = 0;
		
		String loc = person.getLocationTag().getModifiedLoc();
	
		boolean canEnter = false;

		if (airlock.getEntity() instanceof Building) {
		
			if (transitionTo(2)) {
				canEnter = true;
//				System.out.println(person + " transitionTo(2) is true");
			}
		}
		
		else if (airlock.getEntity() instanceof Rover) {
			
			if (insideAirlockPos == null) {
	 			insideAirlockPos = airlock.getAvailableAirlockPosition();
			}
	 		
			if (LocalAreaUtil.areLocationsClose(new Point2D.Double(person.getXLocation(), person.getYLocation()), insideAirlockPos)) {
				canEnter = true;
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
        
		
		if (canEnter) {
			
			if (!airlock.isActivated()) {
				// Enable someone to be selected )as an airlock operator
				airlock.setActivated(true);
			}
			
			if (airlock.isOperator(id) || airlock.isPressurized()) {
		 		// Add experience
		 		addExperience(time);
		 			
		 		setPhase(PRESSURIZE_CHAMBER);
			}
		}
 		
		return remainingTime;
		
	}
	
	private double pressurizeChamber(double time) {

		double remainingTime = 0;
	
//		if (!airlock.isOuterDoorLocked() && airlock.isOperator(id)) {
//			// Lock the outer door
//			airlock.setOuterDoorLocked(true);
//		}
			
		if (airlock.isPressurized()) {
			// If it stops adding or subtracting air, 
			// then airlock has been depressurized, 
			// ready to unlock the outer door
			
			LogConsolidated.log(logger, Level.FINE, 4000, sourceName,
				"[" + person.getLocale() 
				+ "] The chamber had just been pressurized in " 
				+ airlock.getEntity().toString() + ".");
			
			// Add experience
			addExperience(time);
			
			remainingDoffingTime = STANDARD_DOFFING_TIME + RandomUtil.getRandomInt(-2, 2);
			
			setPhase(DOFF_EVA_SUIT);
		}
		
		else if (!airlock.isPressurizing()) {
			//TODO: if someone is waiting outside the outer door, ask the C2 to unlock outer door to let him in before pressurizing
			LogConsolidated.log(logger, Level.FINE, 4_000, sourceName,
					"[" + person.getLocale() 
					+ "] The chamber started pressurizing in " 
					+ airlock.getEntity().toString() + ".");					
			// Pressurizing the chamber
			airlock.setPressurizing();
		}
			
		if (airlock.isPressurizing()) {
//			LogConsolidated.log(logger, Level.FINE, 4000, sourceName,
//					"[" + person.getLocale() 
//					+ "] The chamber was pressurizing in " 
//					+ airlock.getEntity().toString() + ".");
			
			// TODO:Elect an operator to handle this task
			
			// Add air cycle time until it is fully pressurized
			remainingTime =  time - airlock.addTime(time);
		}
		
		return remainingTime;
	}
	
	
	private double doffEVASuit(double time) {

		double remainingTime = 0;
		
		// 1. Gets the suit instance
		EVASuit suit = person.getSuit(); 

		if (airlock.isPressurized()) {
				
			remainingDoffingTime -= time;
			
			// 2. Doff this suit
			if (suit != null && suit.getLastOwner().equals(person) && remainingDoffingTime <= 0) {
				// 2a. Records the person as the owner		
				suit.setLastOwner(person);
				// 2b. Doff this suit. Deregister the suit from the person
				person.registerSuit(null);
	
				Inventory entityInv = airlock.getEntityInventory();
				// 2c Transfer the EVA suit from person to entityInv
				suit.transfer(person, entityInv);	
		
				String loc = person.getLocationTag().getImmediateLocation();
	
				// 2d. Return suit to entity's inventory.
				LogConsolidated.log(logger, Level.FINE, 4000, sourceName, 
						"[" + person.getLocale() + "] " + person.getName() 
						+ " " + loc 
						+ " had just doffed the "  + suit.getName() + ".");
				
	//			if (person.getContainerUnit() instanceof MarsSurface) {
	//				LogConsolidated.log(logger, Level.WARNING, 4000, sourceName,
	//						"[" + person.getLocale() + "] "  
	//						+ person + " " + loc + " still had MarsSurface as the container unit.");
	//			}
	
				Inventory suitInv = suit.getInventory();
				
				if (entityInv != null && suitInv != null) {
					
					LogConsolidated.log(logger, Level.FINE, 4000, sourceName, 
							"[" + person.getLocale() + "] " + person.getName() 
							+ " " + loc + " was going to retrieve the O2 and H2O in " + suit.getName() + ".");
					
					// 2e. Unloads the resources from the EVA suit to the entityEnv			
					try {
						// 2e1. Unload oxygen from the suit.
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
		
					// 2e2. Unload water from the suit.
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
					
					// Add experience
					addExperience(time);
	
					remainingCleaningTime = STANDARD_CLEANINNG_TIME + RandomUtil.getRandomInt(-2, 2);
					
					setPhase(CLEAN_UP);
				}
	
			}
					
	//		else { // the person doesn't have the suit
	//			
	//			LogConsolidated.log(logger, Level.WARNING, 4000, sourceName,
	//					"[" + person.getLocale() + "] " 
	//					+ person.getName() + " " + loc 
	//					+ " was supposed to put away an EVA suit but somehow did not have one.");
	//			
	//			setPhase(CLEAN_UP);
	//		}
				
		}
		
		return remainingTime;
	}
	
	
	private double cleanUp(double time) {

		double remainingTime = 0;
		
		remainingCleaningTime -= time;
		
		if (remainingCleaningTime <= 0) {
			LogConsolidated.log(logger, Level.FINE, 4000, sourceName,
					"[" + person.getLocale() + "] " 
					+ person.getName()
					+ " completed cleaning up oneself and the EVA suit.");
			
			// Add experience
			addExperience(time);

			setPhase(LEAVE_AIRLOCK);
		}
		
		return remainingTime;
	}
	
	
	private double leaveAirlock(double time) {

		double remainingTime = 0;

		boolean canExit = false;

		if (airlock.getEntity() instanceof Building) {
			
			if (transitionTo(1)) {
				
				if (airlock.inAirlock(person)) {
					canExit = airlock.exitAirlock(person, id, false); 
				}
			}
		}
		
		else if (airlock.getEntity() instanceof Rover) {
			
			if (interiorDoorPos == null) {
				interiorDoorPos = airlock.getAvailableInteriorPosition();
			}
			
			if (LocalAreaUtil.areLocationsClose(new Point2D.Double(person.getXLocation(), person.getYLocation()), interiorDoorPos)) {
				
				if (airlock.inAirlock(person)) {
					canExit = airlock.exitAirlock(person, id, false); 
				}
			}
			
			else {
				Rover airlockRover = (Rover) airlock.getEntity();
		         		 	 		
				LogConsolidated.log(logger, Level.FINE, 4000, sourceName, 
  					"[" + person.getLocale() + "] " + person.getName() 
  					+ " tried walking close to the interior door.");
  			
				addSubTask(new WalkRoverInterior(person, airlockRover, 
						interiorDoorPos.getX(), interiorDoorPos.getY())); 		
			}	
		} 
		
		if (canExit) {
			
			// Add experience
			addExperience(time);		
			
			// Move to zone 0
			transitionTo(0);
			
			// Remove the position at zone 0 before calling endTask
			airlock.vacate(0, id);	
					
			// This completes the EVA ingress through the airlock
			completeAirlockTask();
		}
				
		return remainingTime;
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
			LogConsolidated.log(logger, Level.FINE, 4000, sourceName, 
					"[" + person.getLocale() + "] " + person.getName() 
					+ " could not enter airlock to " + airlock.getEntityName()
					+ ". Already inside and not outside.");
			result = false;
		}
		
		else if (!airlock.hasSpace()) {
			LogConsolidated.log(logger, Level.FINE, 4000, sourceName, 
					"[" + person.getLocale() + "] " + person.getName() 
					+ " could not enter airlock to " + airlock.getEntityName()
					+ ". Already full.");
			result = false;
		}

		return result;
	}

	@Override
	public void endTask() {
		// Clear the person as the airlock operator if task ended prematurely.
//		if (airlock != null && person.getName().equals(airlock.getOperatorName())) {
//			LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
//					"[" + person.getLocale() + "] " + person.getName() 
//					+ " concluded the airlock operator task.");
//		}
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

	/**
	 * Remove the person from airlock and walk away and ends the airlock and walk tasks
	 */
	public void completeAirlockTask() {
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

		// Ends the sub task 2 within the EnterAirlock task
		endSubTask2();
		
		// Remove all lingering tasks to avoid any unfinished walking tasks
//		person.getMind().getTaskManager().endSubTask();
		
		// Walk away from this airlock anywhere in the settlement
		walkToRandomLocation(false);
		
		super.endTask();
	}

	@Override
	public void destroy() {
		super.destroy();

		airlock = null;
	}
}

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

//    private static final double EGRESS_TIME = .05; // in millisols

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.exitAirlock"); //$NON-NLS-1$

	private static final double MIN_PERFORMANCE = 0.05;
	
	/** Task phases. */
	private static final TaskPhase PROCURING_EVA_SUIT = new TaskPhase(
			Msg.getString("Task.phase.procuringEVASuit")); //$NON-NLS-1$
	private static final TaskPhase WAITING_TO_ENTER_AIRLOCK = new TaskPhase(
			Msg.getString("Task.phase.waitingToEnterAirlock")); //$NON-NLS-1$
	private static final TaskPhase ENTERING_AIRLOCK = new TaskPhase(
			Msg.getString("Task.phase.enteringAirlock")); //$NON-NLS-1$
	private static final TaskPhase WAITING_INSIDE_AIRLOCK = new TaskPhase(
			Msg.getString("Task.phase.waitingInsideAirlock")); //$NON-NLS-1$
	private static final TaskPhase EXITING_AIRLOCK = new TaskPhase(
			Msg.getString("Task.phase.exitingAirlock")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .5D;

	// Data members
	/** True if person has an EVA suit. */
	private boolean hasSuit = false;
	/** The airlock to be used. */
	private Airlock airlock;
	/** The inside airlock position. */
	private Point2D insideAirlockPos = null;
	/** The exterior airlock position. */
	private Point2D exteriorDoorPos = null;
	
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
		addPhase(PROCURING_EVA_SUIT);
		addPhase(WAITING_TO_ENTER_AIRLOCK);
		addPhase(ENTERING_AIRLOCK);
		addPhase(WAITING_INSIDE_AIRLOCK);
		addPhase(EXITING_AIRLOCK);

		setPhase(PROCURING_EVA_SUIT);

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
		} else if (PROCURING_EVA_SUIT.equals(getPhase())) {
			return procuringEVASuit(time);
		} else if (WAITING_TO_ENTER_AIRLOCK.equals(getPhase())) {
			return waitingToEnterAirlockPhase(time);
		} else if (ENTERING_AIRLOCK.equals(getPhase())) {
			return enteringAirlockPhase(time);
		} else if (WAITING_INSIDE_AIRLOCK.equals(getPhase())) {
			return waitingInsideAirlockPhase(time);
		} else if (EXITING_AIRLOCK.equals(getPhase())) {
			return exitingAirlockPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the procuring EVA suit phase of the task.
	 * 
	 * @param time the amount of time to perform the task phase.
	 * @return the remaining time after performing the task phase.
	 */
	private double procuringEVASuit(double time) {
		// logger.info(person + " procuring an EVA suit.");

		double remainingTime = time;

		// Check if person already has EVA suit.
		if (!hasSuit && alreadyHasEVASuit()) {
			hasSuit = true;
		}

		// Get an EVA suit from entity inventory.
		if (!hasSuit) {
			Inventory entityInv = airlock.getEntityInventory();
			EVASuit suit = getGoodEVASuit(entityInv, person);
			if (suit != null) {
				// logger.info(person + " found an EVA suit.");
				try {
					// 1.1 Transfer the EVA suit from entityInv to person
					suit.transfer(entityInv, person);			
					// 1.2 set the person as the owner
					suit.setLastOwner(person);
					// 1.3 register the suit the person will take into the airlock to don
					person.registerSuit(suit);
					// 1.4 Loads the resources into the EVA suit
					loadEVASuit(suit);
					// the person has a EVA suit
					hasSuit = true;
					// logger.info(person + " grabbed an EVA suit.");
				} catch (Exception e) {
					LogConsolidated.log(logger, Level.WARNING, 4000, sourceName, "[" + person.getLocale()
									+ "] " + person.getName() + " could not take " + suit.toString(), e);

				}
			}
		}

		// If person still doesn't have an EVA suit, end task.
		if (!hasSuit) {
			LogConsolidated.log(logger, Level.WARNING, 4000, sourceName,
					"[" + person.getLocale() + "] " + person.getName()
							+ " could not find a working EVA suit.");

//				person.getMind().getTaskManager().clearTask();
//				person.getMind().getTaskManager().getNewTask();
			
			endTask(); // will call Walk many times again
			return 0D;
		}
		
		else {
			// Add experience
			addExperience(time - remainingTime);
			
			setPhase(WAITING_TO_ENTER_AIRLOCK);
		}

		return remainingTime;
	}

	/**
	 * Performs the waiting to enter airlock phase of the task.
	 * 
	 * @param time the amount of time to perform the task phase.
	 * @return the remaining time after performing the task phase.
	 */
	private double waitingToEnterAirlockPhase(double time) {

		double remainingTime = time;

		String loc = person.getLocationTag().getImmediateLocation();
		loc = loc == null ? "[N/A]" : loc;
		loc = loc.equalsIgnoreCase("Outside") ? loc.toLowerCase() : "in " + loc;
		
		// If person is already outside, change to exit airlock phase.
		if (person.isOutside()) {
			LogConsolidated.log(logger, Level.WARNING, 4000, sourceName, 
				"[" + person.getLocale() + "] " + person.getName() + 
				" was in the 'waiting to enter' phase for EVA egress"
				+ " but was reportedly " + loc + ". End the Task.");
			
	        setPhase(EXITING_AIRLOCK);
//			endTask();
			return remainingTime;
		}
//		else { // Not in v3.07
//			LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
//				"[" + person.getLocale() + "] " + person.getName() + " " 
//				+ loc + " was in the 'waiting to enter' phase for EVA egress.");
//		
//			if (insideAirlockPos == null) {
//				insideAirlockPos = airlock.getAvailableAirlockPosition();
//			}
//			// Walk toward the reference point inside the airlock
//			walkToReference(loc);
//		}
		
		// If airlock is pressurized and inner door unlocked, enter airlock.
		if ((Airlock.AirlockState.PRESSURIZED == airlock.getState() && !airlock.isInnerDoorLocked())
				|| airlock.inAirlock(person)) {
			
			LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
					"[" + person.getLocale() + "] The airlock chamber"// in " 
//					 + ((Building)(airlock.getEntity())).getNickName() 
					 + " had just been PRESSURIZED with the interior door UNLOCKED for entry.");
			
			// Add experience
			addExperience(time - remainingTime);
			
			setPhase(ENTERING_AIRLOCK);
		} 
		
		else {	// The airlock is not being fully prepared for egress yet.
			
			// Add person to queue awaiting airlock at inner door if not already.
			airlock.addAwaitingAirlockInnerDoor(person);
	
			loc = person.getLocationTag().getImmediateLocation();
			loc = loc == null ? "[N/A]" : loc;
			loc = loc.equalsIgnoreCase("Outside") ? loc.toLowerCase() : "in " + loc;
			
			// If airlock has not been activated, activate it.
			if (!airlock.isActivated()) {
				if (airlock.activateAirlock(person)) {
//					LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
//							"[" + person.getLocale() + "] " + person.getName() 
//							+ " " + loc + 							
//							" pressed a button asking for activating the airlock in 'waiting to enter' phase.");			
				}
				
//				else {
//					LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
//							"[" + person.getLocale() + "] " + person.getName()
//							+ " " + loc + " failed to activate the airlock for egress.");
//				}
			}
			
			else { // the airlock has activated
				LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
						"[" + person.getLocale() + "] " + person.getName()
						+ " " + loc + " observed the airlock just got activated for EVA egress.");
				
			}
			
	           // Check if person is the airlock operator.
            if (person.equals(airlock.getOperator())) {
                // If person is airlock operator, add cycle time to airlock.
                double activationTime = remainingTime;
                if (airlock.getRemainingCycleTime() < remainingTime) {
                    remainingTime -= airlock.getRemainingCycleTime();
                }
                else {
                    remainingTime = 0D;
                }
                boolean activationSuccessful = airlock.addCycleTime(activationTime);
                if (!activationSuccessful) {
//                    logger.severe("Problem with airlock activation: " + person.getName());
					LogConsolidated.log(logger, Level.WARNING, 4000, sourceName, 
							"[" + person.getLocale() + "] "
							+ person.getName() + " " + loc 
							+ " had problems with airlock activation in the 'waiting to enter' phase.");
                }
            }
            else {
                // If person is not airlock operator, just wait.
                remainingTime = 0D;
            }
            
		}
		
		return remainingTime;
	}

	/**
	 * Performs the entering airlock phase of the task.
	 * 
	 * @param time the amount of time to perform the task phase.
	 * @return the remaining time after performing the task phase.
	 */
	private double enteringAirlockPhase(double time) {

		double remainingTime = time;

		if (insideAirlockPos == null) {
			insideAirlockPos = airlock.getAvailableAirlockPosition();
		}

		String loc = person.getLocationTag().getImmediateLocation();
		loc = loc == null ? "[N/A]" : loc;
		loc = loc.equalsIgnoreCase("Outside") ? loc.toLowerCase() : "in " + loc;
		
		LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
				"[" + person.getLocale() + "] " + person.getName() 
				+ " " + loc + " was in the 'entering airlock' phase for EVA egress.");
		
		Point2D personLocation = new Point2D.Double(person.getXLocation(), person.getYLocation());

		Point2D spot = null;
		
		if (airlock.getEntity() instanceof Building) {		       
			spot = ((Building) airlock.getEntity()).getEVA().getAvailableActivitySpot(person);
		}
		
		else if (airlock.getEntity() instanceof Rover) {
			spot = insideAirlockPos;
		}
		
		if (airlock.inAirlock(person)) {
			
			LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
					"[" + person.getLocale() + "] " + person.getName() + 
					" went inside the airlock chamber, waiting inside the airlock.");
			
			// Add experience
			addExperience(time - remainingTime);
			
			setPhase(WAITING_INSIDE_AIRLOCK);
		}
		
		// If person is already outside, change to exit airlock phase.
		else if (person.isOutside()) {
			LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
				"[" + person.getLocale() + "] " + person.getName() + 
				" was in the 'entering the airlock' phase for EVA egress"
				+ " but was reportedly outside. End the Task.");
			
	        setPhase(EXITING_AIRLOCK);
//			endTask();
			return remainingTime;
		}

		else if (person.isInside()) {	
			// Walk toward the reference point inside the airlock
			walkToReferencePoint(loc, spot);
		}
			
		if (LocalAreaUtil.areLocationsClose(personLocation, spot)) {
			
			LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
					"[" + person.getLocale() + "] " + person.getName()
					+ " was at the right spot in the airlock.");

	           // Enter airlock.
            if (airlock.enterAirlock(person, true)) {

                // If airlock has not been activated, activate it.
                if (!airlock.isActivated()) {
                    airlock.activateAirlock(person);
                }

                logger.finer(person + " has entered airlock");

                setPhase(WAITING_INSIDE_AIRLOCK);
            }
            else {
                // If airlock has not been activated, activate it.
                if (!airlock.isActivated()) {
                    airlock.activateAirlock(person);
                }

                // Check if person is the airlock operator.
                if (person.equals(airlock.getOperator())) {
                    // If person is airlock operator, add cycle time to airlock.
                    double activationTime = remainingTime;
                    if (airlock.getRemainingCycleTime() < remainingTime) {
                        remainingTime -= airlock.getRemainingCycleTime();
                    }
                    else {
                        remainingTime = 0D;
                    }
                    boolean activationSuccessful = airlock.addCycleTime(activationTime);
                    if (!activationSuccessful) {
//                        logger.severe("Problem with airlock activation: " + person.getName());
    					LogConsolidated.log(logger, Level.WARNING, 4000, sourceName, 
    							"[" + person.getLocale() + "] "
    							+ person.getName() + " " + loc 
    							+ " had problems with airlock activation in the 'entering airlock' phase.");
                    }
                }
                else {
                    // If person is not airlock operator, just wait.
                    remainingTime = 0D;
                }
            }
		}

		return remainingTime;
	}

	/**
	 * Walks toward the reference point inside of the airlock
	 * 
	 * @param loc
	 * @param personLocation
	 */
	private void walkToReferencePoint(String loc, Point2D spot) {
		
		// Walk to inside airlock position.
		if (airlock.getEntity() instanceof Building) {
			
			Building airlockBuilding = (Building) airlock.getEntity();
	 					
			double distance = Point2D.distance(person.getXLocation(), person.getYLocation(),
					spot.getX(), spot.getY());
					
			if (distance > 0) {

				LogConsolidated.log(logger, Level.INFO, 4000, sourceName, 
						"[" + person.getLocale() + "] " + person.getName() 
						+ " " + loc 
						+ " walked to an available activity spot.");
				
				addSubTask(new WalkSettlementInterior(person, airlockBuilding, spot.getX(),
						spot.getY(), 0));
				
//				LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
//						"[" + person.getLocale() + "] " + person.getName()
//						+ " " + loc + " attempted to come closer to the reference point inside of the airlock.");
			}
			
			else {
				LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
						"[" + person.getLocale() + "] " + person.getName()
						+ " was exactly at the reference point of the airlock in " + airlock.getEntity());
			}	
		}
		
		else if (airlock.getEntity() instanceof Rover) {

			Rover airlockRover = (Rover) airlock.getEntity();

			addSubTask(new WalkRoverInterior(person, airlockRover, spot.getX(),
					spot.getY()));
			
			LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
					"[" + person.getLocale() + "] " + person.getName()
					+ " " + loc + " attempted to come closer to the reference point inside of the vehicle airlock.");
		}	
	}
	
	/**
	 * Performs the waiting and donning of the EVA suit while inside the airlock.
	 * 
	 * @param time the amount of time to perform the task phase.
	 * @return the remaining time after performing the task phase.
	 */
	private double waitingInsideAirlockPhase(double time) {

		double remainingTime = time;

		if (airlock.inAirlock(person)) {
			
			// Waiting inside
			LogConsolidated.log(logger, Level.FINER, 4000, sourceName,
					"[" + person.getLocale() + "] "
	  						+ person + " was in the 'waiting inside airlock' phase.");
			
			// TODO: how to account for the speed of donning an EVA suit
			
			// 4.1 find the EVA suit from a person's inventory
//				EVASuit suit = (EVASuit) person.getInventory().findUnitOfClass(EVASuit.class);
			// 4.2 register the suit the person dons
//				person.registerSuit(suit);
			
			// If airlock has not been activated, activate it.
			if (!airlock.isActivated()) {

				if (airlock.activateAirlock(person)) {
					
					String loc = person.getLocationTag().getImmediateLocation();
					loc = loc == null ? "[N/A]" : loc;
					loc = loc.equalsIgnoreCase("Outside") ? loc.toLowerCase() : "in " + loc;
					
					LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
							"[" + person.getLocale() + "] " + person.getName() 
							+ " " + loc + 	
							" pressed a button asking for activating the airlock in 'walking inside airlock' phase.");
					
				}
				else {
					LogConsolidated.log(logger, Level.WARNING, 4000, sourceName, 
							"[" + person.getLocale() + "] " + person.getName()
							+ " failed to activate the airlock.");
//					endTask();
				}
			}

			// Check if person is the airlock operator.
			if (person.equals(airlock.getOperator())) {
				
				// If person is airlock operator, add cycle time to airlock.
				double activationTime = remainingTime;
				if (airlock.getRemainingCycleTime() < remainingTime) {
					remainingTime -= airlock.getRemainingCycleTime();
				} else {
					remainingTime = 0D;
				}
				
				boolean activationSuccessful = airlock.addCycleTime(activationTime);
				// Note: calling deactivateAirlock() would cause the person to have 
				// a location state change.
//				boolean deactivated = airlock.deactivateAirlock();
				
				String loc = person.getLocationTag().getImmediateLocation();
				loc = loc == null ? "[N/A]" : loc;
				loc = loc.equalsIgnoreCase("Outside") ? loc.toLowerCase() : "in " + loc;
				
				if (!activationSuccessful) {// || !deactivated) {
					LogConsolidated.log(logger, Level.WARNING, 10_000, sourceName, 
							"[" + person.getLocale() + "] "
							+ person.getName() + " " + loc 
							+ " had problems with airlock activation in the 'waiting inside airlock' phase.");
					// Note: Calling endTask may someone get stranded in the airlock 
//					endTask();
				}
				
//				else {
//					LogConsolidated.log(logger, Level.FINER, 4000, sourceName,
//						"[" + person.getLocale() + "] " 
//						+ person.getName() 
//						+ " as being the operator just deactivated and left the airlock.");
//				}
			}
			
			else {
				// If person is not airlock operator, just wait.
				LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
					"[" + person.getLocale() + "] " + person.getName() + 
					" was not the operator, waiting for others to do the task.");
				
	               remainingTime = 0D;
			}
		}
		
		// If person is already outside, change to exit airlock phase.
		else if (person.isOutside()) {
			LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
				"[" + person.getLocale() + "] " + person.getName() + 
				" was in the 'entering the airlock' phase for EVA egress"
				+ " but was reportedly outside. End the Task.");
			
	        setPhase(EXITING_AIRLOCK);

			addExperience(time - remainingTime);
			
			return remainingTime;
		}

		return remainingTime;
	}

	/**
	 * Performs the exit airlock phase of the task.
	 * 
	 * @param time the amount of time to perform the task phase.
	 * @return the remaining time after performing the task phase.
	 */
	private double exitingAirlockPhase(double time) {

		double remainingTime = 0;

		LogConsolidated.log(logger, Level.FINER, 4000, sourceName,
				"[" + person.getLocale() + "] " + person 
				+ " was about to come through the exterior door to complete the egress.");

		if (person.isInside()) {
			LogConsolidated.log(logger, Level.FINER, 4000, sourceName,
					"[" + person.getLocale() + "] " + person 
					+ " was in 'exiting airlock' phase but is not outside.");
		}
		
		// If person is already outside, change to exit airlock phase.
		else if (person.isOutside()) {
			LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
				"[" + person.getLocale() + "] " + person.getName() + 
				" was in the 'entering the airlock' phase for EVA egress"
				+ " but was reportedly outside. End the Task.");
			
			addExperience(time);
			// This completes the task of exiting the airlock
			endTask();
		}
		
		if (exteriorDoorPos == null) {
			exteriorDoorPos = airlock.getAvailableExteriorPosition();
		}

		Point2D personLocation = new Point2D.Double(person.getXLocation(), person.getYLocation());
		
		if (LocalAreaUtil.areLocationsClose(personLocation, exteriorDoorPos)) {
			
			LogConsolidated.log(logger, Level.FINER, 4000, sourceName,
					"[" + person.getLocale() + "] " + person 
					+ " came very close to the exterior door to complete the egress.");		
			
			// This completes the task of exiting the airlock
			endTask();
		}

		else {
			LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
			"[" + person.getLocale() + "] " + person.getName() 
			+ " did not walk close enough to the exterior door of the airlock. Will attempt again.");

			// Walk to exterior airlock position.
			addSubTask(new WalkOutside(person, person.getXLocation(), person.getYLocation(),
				exteriorDoorPos.getX(), exteriorDoorPos.getY(), true));

		}

		// Add experience
		addExperience(time);

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
			String newLog = person.getName() + " could NOT exit the airlock from " + airlock.getEntityName()
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
						person + " could not get new action" + e.getMessage(), e);
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

			String loc = person.getLocationTag().getImmediateLocation();
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
		super.endTask();
		// Clear the person as the airlock operator if task ended prematurely.
		if ((airlock != null) && person.equals(airlock.getOperator())) {
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
			
			airlock.clearOperator();
		}
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

/**
 * Mars Simulation Project
 * EnterAirlock.java
 * @version 3.1.0 2017-09-13
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.person.NaturalAttributeType;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * The EnterAirlock class is a task for entering a airlock from an EVA
 * operation.
 */
public class EnterAirlock extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(EnterAirlock.class.getName());

//    private static final double INGRESS_TIME = .05; // in millisols

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.enterAirlock"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase WAITING_TO_ENTER_AIRLOCK = new TaskPhase(
			Msg.getString("Task.phase.waitingToEnterAirlock")); //$NON-NLS-1$
	private static final TaskPhase ENTERING_AIRLOCK = new TaskPhase(Msg.getString("Task.phase.enteringAirlock")); //$NON-NLS-1$
	private static final TaskPhase WAITING_INSIDE_AIRLOCK = new TaskPhase(
			Msg.getString("Task.phase.waitingInsideAirlock")); //$NON-NLS-1$
	private static final TaskPhase EXITING_AIRLOCK = new TaskPhase(Msg.getString("Task.phase.exitingAirlock")); //$NON-NLS-1$
	private static final TaskPhase STORING_EVA_SUIT = new TaskPhase(Msg.getString("Task.phase.storingEVASuit")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .5D;

	// Data members
	private static int oxygenID = ResourceUtil.oxygenID;
	private static int waterID = ResourceUtil.waterID;

	/** The airlock to be used. */
	private Airlock airlock;
	private Point2D insideAirlockPos = null;
	private Point2D interiorAirlockPos = null;

	/**
	 * Constructor.
	 * 
	 * @param person  the person to perform the task
	 * @param airlock to be used.
	 */
	public EnterAirlock(Person person, Airlock airlock) {
		super(NAME, person, false, false, STRESS_MODIFIER, false, 0D);
		this.airlock = airlock;
		logger.fine(person.getName() + " is starting to enter " + airlock.getEntityName());
		// Initialize data members
		setDescription(Msg.getString("Task.description.enterAirlock.detail", airlock.getEntityName())); // $NON-NLS-1$
		// Initialize task phase
		addPhase(WAITING_TO_ENTER_AIRLOCK);
		addPhase(ENTERING_AIRLOCK);
		addPhase(WAITING_INSIDE_AIRLOCK);
		addPhase(EXITING_AIRLOCK);
		addPhase(STORING_EVA_SUIT);

		setPhase(WAITING_TO_ENTER_AIRLOCK);
	}

//    public EnterAirlock(Robot robot, Airlock airlock) {
//        super(NAME, robot, false, false, STRESS_MODIFIER, false, 0D);
//        this.airlock = airlock;
//        logger.fine(robot.getName() + " is starting to enter " + airlock.getEntityName());
//        // Initialize data members
//        setDescription(Msg.getString("Task.description.enterAirlock.detail",
//                airlock.getEntityName())); //$NON-NLS-1$
//        // Initialize task phase
//        addPhase(WAITING_TO_ENTER_AIRLOCK);
//        addPhase(ENTERING_AIRLOCK);
//        addPhase(WAITING_INSIDE_AIRLOCK);
//        addPhase(EXITING_AIRLOCK);
//        addPhase(STORING_EVA_SUIT);
//
//        setPhase(WAITING_TO_ENTER_AIRLOCK);
//    }

	/**
	 * Performs the method mapped to the task's current phase.
	 * 
	 * @param time the amount of time (millisols) the phase is to be performed.
	 * @return the remaining time (millisols) after the phase has been performed.
	 */
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (WAITING_TO_ENTER_AIRLOCK.equals(getPhase())) {
			return waitingToEnterAirlockPhase(time);
		} else if (ENTERING_AIRLOCK.equals(getPhase())) {
			return enteringAirlockPhase(time);
		} else if (WAITING_INSIDE_AIRLOCK.equals(getPhase())) {
			return waitingInsideAirlockPhase(time);
		} else if (EXITING_AIRLOCK.equals(getPhase())) {
			return exitingAirlockPhase(time);
		} else if (STORING_EVA_SUIT.equals(getPhase())) {
			return storingEVASuitPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the waiting to enter airlock phase of the task.
	 * 
	 * @param time the amount of time to perform the task phase.
	 * @return the remaining time after performing the task phase.
	 */
	private double waitingToEnterAirlockPhase(double time) {

		double remainingTime = time;

//         if (person != null) {
		logger.finer(person + " waiting to enter airlock from outside.");

		// If person is already inside, change to exit airlock phase.
		if (person.isInside()) {
			setPhase(EXITING_AIRLOCK);
			return remainingTime;
		}

		// If airlock is depressurized and outer door unlocked, enter airlock.
		if ((Airlock.DEPRESSURIZED.equals(airlock.getState()) && !airlock.isOuterDoorLocked())
				|| airlock.inAirlock(person)) {
			setPhase(ENTERING_AIRLOCK);
		} else {
			// Add person to queue awaiting airlock at inner door if not already.
			airlock.addAwaitingAirlockOuterDoor(person);

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
				} else {
					remainingTime = 0D;
				}
				boolean activationSuccessful = airlock.addCycleTime(activationTime);
				if (!activationSuccessful) {
					logger.severe("Problem with airlock activation: " + person.getName());
				}
			} else {
				// If person is not airlock operator, just wait.
				remainingTime = 0D;
			}
		}

//        }

//        else if (robot != null) {
//            logger.finer(robot + " waiting to enter airlock from outside.");
//
//            // If robot is already inside, change to exit airlock phase.
//            if (!robot.isOutside()) {
//                setPhase(EXITING_AIRLOCK);
//                return remainingTime;
//            }
//
//            // If airlock is depressurized and outer door unlocked, enter airlock.
//            if ((Airlock.DEPRESSURIZED.equals(airlock.getState()) && !airlock.isOuterDoorLocked()) ||
//                    airlock.inAirlock(robot)) {
//                setPhase(ENTERING_AIRLOCK);
//            }
//            else {
//                // Add robot to queue awaiting airlock at inner door if not already.
//                airlock.addAwaitingAirlockOuterDoor(robot);
//
//                // If airlock has not been activated, activate it.
//                if (!airlock.isActivated()) {
//                    airlock.activateAirlock(robot);
//                }
//
//                // Check if robot is the airlock operator.
//                if (robot.equals(airlock.getOperator())) {
//                    // If robot is airlock operator, add cycle time to airlock.
//                    double activationTime = remainingTime;
//                    if (airlock.getRemainingCycleTime() < remainingTime) {
//                        remainingTime -= airlock.getRemainingCycleTime();
//                    }
//                    else {
//                        remainingTime = 0D;
//                    }
//                    boolean activationSuccessful = airlock.addCycleTime(activationTime);
//                    if (!activationSuccessful) {
//                        logger.severe("Problem with airlock activation: " + robot.getName());
//                    }
//                }
//                else {
//                    // If robot is not airlock operator, just wait.
//                    remainingTime = 0D;
//                }
//            }
//
//        }

		// Add experience
		addExperience(time - remainingTime);

		return remainingTime;
	}

	/**
	 * Performs the enter airlock phase of the task.
	 * 
	 * @param time the amount of time to perform the task phase.
	 * @return remaining time after performing task phase.
	 */
	private double enteringAirlockPhase(double time) {

		double remainingTime = time;

		if (insideAirlockPos == null) {
			insideAirlockPos = airlock.getAvailableAirlockPosition();
		}

//        if (person != null) {
		// logger.finer(person + " entering airlock from outside.");
		logger.finer(person + " is trying to enter an airlock from outside.");

		Point2D personLocation = new Point2D.Double(person.getXLocation(), person.getYLocation());

		if (airlock.inAirlock(person)) {
			logger.finer(person + " is entering airlock, but is already in airlock.");
			setPhase(WAITING_INSIDE_AIRLOCK);
		} else if (person.isInside()) {//!person.isOutside()) {
			logger.finer(person + " is entering airlock, but is already inside.");
			endTask();
		} else if (LocalAreaUtil.areLocationsClose(personLocation, insideAirlockPos)) {

			// Enter airlock.
			if (airlock.enterAirlock(person, false)) {

				// If airlock has not been activated, activate it.
				if (!airlock.isActivated()) {
					airlock.activateAirlock(person);
				}

				logger.finer(person + " has entered airlock");

				setPhase(WAITING_INSIDE_AIRLOCK);
			} else {
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
					} else {
						remainingTime = 0D;
					}
					boolean activationSuccessful = airlock.addCycleTime(activationTime);
					if (!activationSuccessful) {
						logger.severe("Problem with airlock activation: " + person.getName());
					}
				} else {
					// If person is not airlock operator, just wait.
					remainingTime = 0D;
				}
			}
		} else if (person.isOutside()) {
			// Walk to inside airlock position.
			addSubTask(new WalkOutside(person, person.getXLocation(), person.getYLocation(), insideAirlockPos.getX(),
					insideAirlockPos.getY(), true));
		}

//        }
//        else if (robot != null) {
//            //logger.finer(robot + " entering airlock from outside.");
//            logger.finer(robot + " is trying to enter an airlock from outside.");
//
//            Point2D robotLocation = new Point2D.Double(robot.getXLocation(), robot.getYLocation());
//
//            if (airlock.inAirlock(robot)) {
//                logger.finer(robot + " is entering airlock, but is already in airlock.");
//                setPhase(WAITING_INSIDE_AIRLOCK);
//            }
//            else if (!robot.isOutside()) {
//                logger.finer(robot + " is entering airlock, but is already inside.");
//                endTask();
//            }
//            else if (LocalAreaUtil.areLocationsClose(robotLocation, insideAirlockPos)) {
//
//                // Enter airlock.
//                if (airlock.enterAirlock(robot, false)) {
//
//                    // If airlock has not been activated, activate it.
//                    if (!airlock.isActivated()) {
//                        airlock.activateAirlock(robot);
//                    }
//
//                    logger.finer(robot + " has entered airlock and begun waiting countdown");
//
//                    setPhase(WAITING_INSIDE_AIRLOCK);
//                }
//                else {
//                    // If airlock has not been activated, activate it.
//                    if (!airlock.isActivated()) {
//                        airlock.activateAirlock(robot);
//                    }
//
//                    // Check if robot is the airlock operator.
//                    if (robot.equals(airlock.getOperator())) {
//                        // If robot is airlock operator, add cycle time to airlock.
//                        double activationTime = remainingTime;
//                        if (airlock.getRemainingCycleTime() < remainingTime) {
//                            remainingTime -= airlock.getRemainingCycleTime();
//                        }
//                        else {
//                            remainingTime = 0D;
//                        }
//                        boolean activationSuccessful = airlock.addCycleTime(activationTime);
//                        if (!activationSuccessful) {
//                            logger.severe("Problem with airlock activation: " + robot.getName());
//                        }
//                    }
//                    else {
//                        // If robot is not airlock operator, just wait.
//                        remainingTime = 0D;
//                    }
//                }
//            }
//            else if (robot.isOutside()) {
//            	// Walk to inside airlock position.
//            	addSubTask(new WalkOutside(robot, robot.getXLocation(), robot.getYLocation(),
//                            insideAirlockPos.getX(), insideAirlockPos.getY(), true));
//            }
//
//        }

		// Add experience
		addExperience(time - remainingTime);

		return remainingTime;
	}

	/**
	 * Performs the waiting inside airlock phase of the task.
	 * 
	 * @param time the amount of time to perform the task phase.
	 * @return the remaining time after performing the task phase.
	 */
	private double waitingInsideAirlockPhase(double time) {

		double remainingTime = time;

//        if (person != null) {
		logger.finer(person + " waiting inside airlock.");

		if (airlock.inAirlock(person)) {

			// Check if person is the airlock operator.
			if (person.equals(airlock.getOperator())) {

				// If airlock has not been activated, activate it.
				if (!airlock.isActivated()) {
					airlock.activateAirlock(person);
				}

				// If person is airlock operator, add cycle time to airlock.
				double activationTime = remainingTime;
				if (airlock.getRemainingCycleTime() < remainingTime) {
					remainingTime -= airlock.getRemainingCycleTime();
				} else {
					remainingTime = 0D;
				}
				boolean activationSuccessful = airlock.addCycleTime(activationTime);
				if (!activationSuccessful) {
					logger.severe("Problem with airlock activation: " + person.getName());
				}
			} else {
				// If person is not airlock operator, just wait.
				remainingTime = 0D;
			}
		} else {
			logger.finer(person + " is already internal during waiting inside airlock phase.");
			setPhase(EXITING_AIRLOCK);
		}

//        }
//        else if (robot != null) {
//            logger.finer(robot + " waiting inside airlock.");
//
//            if (airlock.inAirlock(robot)) {
//
//                // Check if robot is the airlock operator.
//                if (robot.equals(airlock.getOperator())) {
//
//                    // If airlock has not been activated, activate it.
//                    if (!airlock.isActivated()) {
//                        airlock.activateAirlock(robot);
//                    }
//
//                    // If robot is airlock operator, add cycle time to airlock.
//                    double activationTime = remainingTime;
//                    if (airlock.getRemainingCycleTime() < remainingTime) {
//                        remainingTime -= airlock.getRemainingCycleTime();
//                    }
//                    else {
//                        remainingTime = 0D;
//                    }
//                    boolean activationSuccessful = airlock.addCycleTime(activationTime);
//                    if (!activationSuccessful) {
//                        logger.severe("Problem with airlock activation: " + robot.getName());
//                    }
//                }
//                else {
//                    // If robot is not airlock operator, just wait.
//                    remainingTime = 0D;
//                }
//            }
//            else {
//                logger.finer(robot + " is already internal during waiting inside airlock phase.");
//                setPhase(EXITING_AIRLOCK);
//            }
//
//        }

		// Add experience
		addExperience(time - remainingTime);

		return remainingTime;
	}

	/**
	 * Performs the exit airlock phase of the task.
	 * 
	 * @param time the amount of time to perform the task phase.
	 * @return the remaining time after performing the task phase.
	 */
	private double exitingAirlockPhase(double time) {

		double remainingTime = time;

		if (interiorAirlockPos == null) {
			interiorAirlockPos = airlock.getAvailableInteriorPosition();
		}

//        if (person != null) {
		// logger.finer(person + " exiting airlock inside.");
		logger.finer(person + " is in exitingAirlockPhase() trying to exit an airlock.");

		Point2D personLocation = new Point2D.Double(person.getXLocation(), person.getYLocation());
		if (LocalAreaUtil.areLocationsClose(personLocation, interiorAirlockPos)) {

			// logger.finer(person + " has exited airlock inside.");
			logger.finer(person + " is going to store the EVA suit");

			setPhase(STORING_EVA_SUIT);
		} else {

			// Walk to interior airlock position.
			if (airlock.getEntity() instanceof Building) {

				Building airlockBuilding = (Building) airlock.getEntity();

				if (airlockBuilding != null) {

					Building startBuilding = BuildingManager.getBuilding(person);
					if (startBuilding != null) {
						logger.finer(
								person + " walking from " + startBuilding + " to an airlock at " + airlockBuilding);
						addSubTask(new WalkSettlementInterior(person, airlockBuilding, interiorAirlockPos.getX(),
								interiorAirlockPos.getY()));
					} else {
						logger.finer(person + " is not inside a building");
						endTask();
					}
				} else {
					logger.finer(airlockBuilding + " is null");
				}

//				 logger.finest(person + " exiting airlock inside " + airlockBuilding);
//				 addSubTask(new WalkSettlementInterior(person, airlockBuilding,
//				 interiorAirlockPos.getX(), interiorAirlockPos.getY()));
			} else if (airlock.getEntity() instanceof Rover) {

				Rover airlockRover = (Rover) airlock.getEntity();
				logger.finest(person + " is walking to an airlock of " + airlockRover);
				addSubTask(new WalkRoverInterior(person, airlockRover, interiorAirlockPos.getX(),
						interiorAirlockPos.getY()));
			}
		}

//        }
//        else if (robot != null) {
//            //logger.finer(robot + " exiting airlock inside.");
//            logger.finer(robot + " is in exitingAirlockPhase() trying to exit an airlock.");
//
//            Point2D robotLocation = new Point2D.Double(robot.getXLocation(), robot.getYLocation());
//            if (LocalAreaUtil.areLocationsClose(robotLocation, interiorAirlockPos)) {
//
//                //logger.finer(robot + " has exited airlock inside.");
//              	logger.finer(robot + " is bypassing the need of storing the EVA suit");
//
//                // EVA SUIT NOT NEEDED for robot but may still call that so as to endTask() properly
//                setPhase(STORING_EVA_SUIT);
//
//            }
//            else {
//
//                // Walk to interior airlock position.
//                if (airlock.getEntity() instanceof Building) {
//
//                    Building airlockBuilding = (Building) airlock.getEntity();
//
//                    if (airlockBuilding != null) {
//
//                        Building startBuilding = BuildingManager.getBuilding(robot);
//                        if (startBuilding != null) {
//                            logger.finer(robot + " walking from " + startBuilding + " to an airlock at " + airlockBuilding);
//                        	addSubTask(new WalkSettlementInterior(robot, airlockBuilding,
//                            interiorAirlockPos.getX(), interiorAirlockPos.getY()));
//                        }
//                        else {
//                        	logger.finer(robot + " is not inside a building");
//                        	endTask();
//                        }
//                    }
//                    else {
//                        logger.finer(airlockBuilding + " is null");
//                    }
//                }
//                else if (airlock.getEntity() instanceof Rover) {
//
//                    Rover airlockRover = (Rover) airlock.getEntity();
//                    logger.finer(robot + " is walking to an airlock of " + airlockRover);
//                    addSubTask(new WalkRoverInterior(robot, airlockRover,
//                            interiorAirlockPos.getX(), interiorAirlockPos.getY()));
//                }
//            }
//
//        }

		// Add experience
		addExperience(time - remainingTime);

		return remainingTime;
	}

	/**
	 * Performs the storing EVA suit phase of the task.
	 * 
	 * @param time the amount of time to perform the task phase.
	 * @return the remaining time after performing the task phase.
	 */
	private double storingEVASuitPhase(double time) {

		double remainingTime = time;

//        if (person != null) {
		logger.finer(person + " is stowing away the EVA suit");
		// Store EVA suit in settlement or rover.
		EVASuit suit = (EVASuit) person.getInventory().findUnitOfClass(EVASuit.class);
		if (suit != null) {
			Inventory suitInv = suit.getInventory();
			Inventory personInv = person.getInventory();
			if (person.getContainerUnit() == null)
				System.err.println("storingEVASuitPhase: person.getContainerUnit() == null : " 
									+ person + " is " 
									+ person.getLocationStateType());
			Inventory entityInv = person.getContainerUnit().getInventory(); // why NullPointerException ?

			// Unload oxygen from suit.
			double oxygenAmount = suitInv.getARStored(oxygenID, false);
			double oxygenCapacity = entityInv.getARRemainingCapacity(oxygenID, true, false);
			if (oxygenAmount > oxygenCapacity)
				oxygenAmount = oxygenCapacity;
			try {
				suitInv.retrieveAR(oxygenID, oxygenAmount);
				entityInv.storeAR(oxygenID, oxygenAmount, true);
				entityInv.addAmountSupplyAmount(oxygenID, oxygenAmount);

			} catch (Exception e) {
				logger.severe("Oxygen is not available : " + e.getMessage());
			}

			// Unload water from suit.
			double waterAmount = suitInv.getARStored(waterID, false);
			double waterCapacity = entityInv.getARRemainingCapacity(waterID, true, false);
			if (waterAmount > waterCapacity)
				waterAmount = waterCapacity;
			try {
				suitInv.retrieveAR(waterID, waterAmount);
				entityInv.storeAR(waterID, waterAmount, true);
				entityInv.addAmountSupplyAmount(waterID, waterAmount);

			} catch (Exception e) {
				logger.severe("Water is not available : " + e.getMessage());
			}

			// Return suit to entity's inventory.
//			 logger.finer(person.getName() + " putting away EVA suit into " +
//			 entity.getName());
			personInv.retrieveUnit(suit);
//			 suit.setLastOwner(person);
			entityInv.storeUnit(suit);
		} else {
			logger.severe("[" + person.getLocationTag().getLocale() + "] " 
					+ person.getName() + " doesn't have an EVA suit to put away.");
		}

//        }
//        else if (robot != null) {
//        	// no need of stowing away the EVA suit
//            //logger.finer(robot + " has no EVA suit to stow away.");
//        }

		endTask();

		// Add experience
		addExperience(time - remainingTime);

		return remainingTime;
	}

	/**
	 * Adds experience to the person's skills used in this task.
	 * 
	 * @param time the amount of time (ms) the person performed this task.
	 */
	protected void addExperience(double time) {

		// Add experience to "EVA Operations" skill.
		// (1 base experience point per 100 millisols of time spent)
		double evaExperience = time / 100D;

//        if (person != null) {
		// Experience points adjusted by person's "Experience Aptitude" attribute.
		NaturalAttributeManager nManager = person.getNaturalAttributeManager();
		int experienceAptitude = nManager.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		double experienceAptitudeModifier = (((double) experienceAptitude) - 50D) / 100D;
		evaExperience += evaExperience * experienceAptitudeModifier;
		evaExperience *= getTeachingExperienceModifier();
		person.getMind().getSkillManager().addExperience(SkillType.EVA_OPERATIONS, evaExperience);

//        }
//        else if (robot != null) {
//
//        	// TODO: determine if this skill is needed for the robot.
//
//            // Experience points adjusted by robot's "Experience Aptitude" attribute.
//            RoboticAttributeManager nManager = robot.getRoboticAttributeManager();
//            int experienceAptitude = nManager.getAttribute(RoboticAttributeType.EXPERIENCE_APTITUDE);
//            double experienceAptitudeModifier = (((double) experienceAptitude) - 50D) / 100D;
//            evaExperience += evaExperience * experienceAptitudeModifier;
//            evaExperience *= getTeachingExperienceModifier();
//            robot.getBotMind().getSkillManager().addExperience(SkillType.EVA_OPERATIONS, evaExperience);
//
//        }
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
			logger.fine(person.getName() + " cannot enter airlock to " + airlock.getEntityName()
					+ " due to not being outside.");
			result = false;
		}

		return result;
	}

//    public static boolean canEnterAirlock(Robot robot, Airlock airlock) {
//
//        boolean result = true;
//
//        if (robot.isInside()) {
//            logger.fine(robot.getName() + " cannot enter airlock to " + airlock.getEntityName() +
//                    " due to not being outside.");
//            result = false;
//        }
//
//        return result;
//    }

	@Override
	public void endTask() {
		super.endTask();

//        if (person != null) {
		// Clear the person as the airlock operator if task ended prematurely.
		if ((airlock != null) && person.equals(airlock.getOperator())) {
			logger.severe(person + " ending entering airlock task prematurely, " + "clearing as airlock operator for "
					+ airlock.getEntityName());
			airlock.clearOperator();
		}
//        }
//        else if (robot != null) {
//            // Clear the robot as the airlock operator if task ended prematurely.
//            if ((airlock != null) && robot.equals(airlock.getOperator())) {
//                logger.severe(robot + " ending entering airlock task prematurely, " +
//                        "clearing as airlock operator for " + airlock.getEntityName());
//                airlock.clearOperator();
//            }
//        }

	}

	@Override
	public int getEffectiveSkillLevel() {
		SkillManager manager = null;
//    	if (person != null)
		manager = person.getMind().getSkillManager();
//    	else if (robot != null)
//            manager = robot.getBotMind().getSkillManager();

		return manager.getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
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
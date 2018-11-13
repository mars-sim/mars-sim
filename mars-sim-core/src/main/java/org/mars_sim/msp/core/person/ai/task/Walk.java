/**
 * Mars Simulation Project
 * Walk.java
 * @version 3.1.0 2017-01-19
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.MissionMember;
import org.mars_sim.msp.core.person.ai.task.WalkingSteps.WalkStep;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Airlockable;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * A general walking task that includes interior/exterior walking and
 * entering/exiting airlocks.
 */
public class Walk extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default serial id. */
	private static Logger logger = Logger.getLogger(Walk.class.getName());

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());

	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -.2D;
	/** Task name */
	private static final String NAME = Msg.getString("Task.description.walk"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase WALKING_SETTLEMENT_INTERIOR = new TaskPhase(
			Msg.getString("Task.phase.walkingSettlementInterior")); //$NON-NLS-1$
	private static final TaskPhase WALKING_ROVER_INTERIOR = new TaskPhase(
			Msg.getString("Task.phase.walkingRoverInterior")); //$NON-NLS-1$
	private static final TaskPhase WALKING_EXTERIOR = new TaskPhase(Msg.getString("Task.phase.walkingExterior")); //$NON-NLS-1$
	private static final TaskPhase EXITING_AIRLOCK = new TaskPhase(Msg.getString("Task.phase.exitingAirlock")); //$NON-NLS-1$
	private static final TaskPhase ENTERING_AIRLOCK = new TaskPhase(Msg.getString("Task.phase.enteringAirlock")); //$NON-NLS-1$
	private static final TaskPhase EXITING_ROVER_GARAGE = new TaskPhase(Msg.getString("Task.phase.exitingRoverGarage")); //$NON-NLS-1$
	private static final TaskPhase ENTERING_ROVER_GARAGE = new TaskPhase(
			Msg.getString("Task.phase.enteringRoverGarage")); //$NON-NLS-1$

	// Data members
	private int walkingStepIndex;

	/** The person performing the task. */
	// protected Person person;
	/** The robot performing the task. */
	// protected Robot robot;

	private WalkingSteps walkingSteps;

	private Map<Integer, TaskPhase> walkingStepPhaseMap;

	private static UnitManager unitManager;

	/**
	 * Constructor.
	 * 
	 * @param person the person performing the task.
	 */
	public Walk(Person person) {
		super(null, person, false, false, STRESS_MODIFIER, false, 0D);
		// this.person = person;

		// Initialize data members.
		walkingStepIndex = 0;

		if (person.isInSettlement()) {// || person.isInVehicleInGarage()) {

			// Walk to random inhabitable building at settlement.
			//Building currentBuilding = BuildingManager.getBuilding(person);
			List<Building> buildingList = person.getSettlement().getBuildingManager().getBuildings(FunctionType.LIFE_SUPPORT);
			if (buildingList.size() > 0) {
				int buildingIndex = RandomUtil.getRandomInt(buildingList.size() - 1);
				Building destinationBuilding = buildingList.get(buildingIndex);
				Point2D interiorPos = LocalAreaUtil.getRandomInteriorLocation(destinationBuilding);
				Point2D adjustedInteriorPos = LocalAreaUtil.getLocalRelativeLocation(interiorPos.getX(),
						interiorPos.getY(), destinationBuilding);
				walkingSteps = new WalkingSteps(person, adjustedInteriorPos.getX(), adjustedInteriorPos.getY(),
						destinationBuilding);
			}

		} else if (person.isInVehicle()) {

			Vehicle vehicle = person.getVehicle();

			// If no mission and vehicle is at a settlement location, enter settlement.
			boolean walkToSettlement = false;
			if ((person.getMind().getMission() == null) && (vehicle.getSettlement() != null)) {

				Settlement settlement = vehicle.getSettlement();

				// Check if vehicle is in garage.
				Building garageBuilding = BuildingManager.getBuilding(vehicle);
				if (garageBuilding != null) {

					Point2D interiorPos = LocalAreaUtil.getRandomInteriorLocation(garageBuilding);
					Point2D adjustedInteriorPos = LocalAreaUtil.getLocalRelativeLocation(interiorPos.getX(),
							interiorPos.getY(), garageBuilding);
					walkingSteps = new WalkingSteps(person, adjustedInteriorPos.getX(), adjustedInteriorPos.getY(),
							garageBuilding);
					walkToSettlement = true;
				} else if (vehicle instanceof Rover) {

					// Check if person has a good EVA suit available if in a rover.
					boolean goodEVASuit = true;
					boolean roverSuit = ExitAirlock.goodEVASuitAvailable(vehicle.getInventory());
					boolean wearingSuit = person.getInventory().containsUnitClass(EVASuit.class);
					goodEVASuit = roverSuit || wearingSuit;

					if (goodEVASuit) {
						// Walk to nearest emergency airlock in settlement.
						Airlock airlock = settlement.getClosestAvailableAirlock(person);
						if (airlock != null) {
							LocalBoundedObject entity = (LocalBoundedObject) airlock.getEntity();
							Point2D interiorPos = LocalAreaUtil.getRandomInteriorLocation(entity);
							Point2D adjustedInteriorPos = LocalAreaUtil.getLocalRelativeLocation(interiorPos.getX(),
									interiorPos.getY(), entity);
							walkingSteps = new WalkingSteps(person, adjustedInteriorPos.getX(),
									adjustedInteriorPos.getY(), entity);
							walkToSettlement = true;
						}
					}

				} else {
					// If not a rover, retrieve person from vehicle.
					vehicle.getInventory().retrieveUnit(person);
				}
			}

			if (!walkToSettlement) {

				// Walk to random location within rover.
				if (person.getVehicle() instanceof Rover) {
					Rover rover = (Rover) person.getVehicle();
					Point2D interiorPos = LocalAreaUtil.getRandomInteriorLocation(rover);
					Point2D adjustedInteriorPos = LocalAreaUtil.getLocalRelativeLocation(interiorPos.getX(),
							interiorPos.getY(), rover);
					walkingSteps = new WalkingSteps(person, adjustedInteriorPos.getX(), adjustedInteriorPos.getY(),
							rover);
				}
			}
		}

		// Determine if person is outside.
		else if (person.isOutside()) {

			Airlock airlock = findEmergencyAirlock(person);
			if (airlock != null) {
				LocalBoundedObject entity = (LocalBoundedObject) airlock.getEntity();
				Point2D interiorPos = LocalAreaUtil.getRandomInteriorLocation(entity);
				Point2D adjustedInteriorPos = LocalAreaUtil.getLocalRelativeLocation(interiorPos.getX(),
						interiorPos.getY(), entity);
				walkingSteps = new WalkingSteps(person, adjustedInteriorPos.getX(), adjustedInteriorPos.getY(), entity);
			}
		}

//        else {
//            throw new IllegalStateException("Could not determine walking steps for " + person.getName() +
//                    " at location " + person.getLocationSituation());
//        }

		if (walkingSteps == null) {
			LogConsolidated.log(logger, Level.SEVERE, 5000, sourceName,
					"[" + person.getLocationTag().getLocale() + "] "
      						+ person + " is in " + person.getLocationTag().getImmediateLocation()
      						+ " but walking steps could not be determined.", null);
			endTask();
			person.getMind().getNewAction(true, false);
		} else if (!canWalkAllSteps(person, walkingSteps)) {
			LogConsolidated.log(logger, Level.SEVERE, 5000, sourceName,
					"[" + person.getLocationTag().getLocale() + "] "
      						+ person + " is in " + person.getLocationTag().getImmediateLocation()
					+ " but Valid Walking steps could not be determined.", null);
			endTask();
			person.getMind().getNewAction(true, false);
		}

		// Initialize task phase.
		addPhase(WALKING_SETTLEMENT_INTERIOR);
		addPhase(WALKING_ROVER_INTERIOR);
		addPhase(WALKING_EXTERIOR);
		addPhase(EXITING_AIRLOCK);
		addPhase(ENTERING_AIRLOCK);
		addPhase(EXITING_ROVER_GARAGE);
		addPhase(ENTERING_ROVER_GARAGE);

		setPhase(getWalkingStepPhase());
	}

	/**
	 * Constructor 2.
	 * 
	 * @param robot the robot performing the task.
	 */
	public Walk(Robot robot) {
		super(null, robot, false, false, 0D, false, 0D);
		// this.robot = robot;
		// logger.finer(robot + " starting new walk task.");

		// Initialize data members.
		walkingStepIndex = 0;

		if (robot.isInSettlement() || robot.isInVehicleInGarage()) {

			// Walk to random building at settlement.
			Building currentBuilding = BuildingManager.getBuilding(robot);
			List<Building> buildingList = currentBuilding.getBuildingManager()
					.getBuildings(FunctionType.ROBOTIC_STATION);
			if (buildingList.size() > 0) {
				int buildingIndex = RandomUtil.getRandomInt(buildingList.size() - 1);
				Building destinationBuilding = buildingList.get(buildingIndex);
				Point2D interiorPos = LocalAreaUtil.getRandomInteriorLocation(destinationBuilding);
				Point2D adjustedInteriorPos = LocalAreaUtil.getLocalRelativeLocation(interiorPos.getX(),
						interiorPos.getY(), destinationBuilding);
				walkingSteps = new WalkingSteps(robot, adjustedInteriorPos.getX(), adjustedInteriorPos.getY(),
						destinationBuilding);
			}

		}
//        else if (robot.isInVehicle()) {
//
//            Vehicle vehicle = robot.getVehicle();
//
//            // If no mission and vehicle is at a settlement location, enter settlement.
//            boolean walkToSettlement = false;
//            if ((robot.getBotMind().getMission() == null) && (vehicle.getSettlement() != null)) {
//
//                Settlement settlement = vehicle.getSettlement();
//
//                // Check if vehicle is in garage.
//                Building garageBuilding = BuildingManager.getBuilding(vehicle);
//                if (garageBuilding != null) {
//
//                    Point2D interiorPos = LocalAreaUtil.getRandomInteriorLocation(garageBuilding);
//                    Point2D adjustedInteriorPos = LocalAreaUtil.getLocalRelativeLocation(
//                            interiorPos.getX(), interiorPos.getY(), garageBuilding);
//                    walkingSteps = new WalkingSteps(robot, adjustedInteriorPos.getX(),
//                            adjustedInteriorPos.getY(), garageBuilding);
//                    walkToSettlement = true;
//                }
//                else if (vehicle instanceof Rover) {
//
//                    // Check if robot has a good EVA suit available if in a rover.
//                    //boolean goodEVASuit = true;
//                   // boolean roverSuit = ExitAirlock.goodEVASuitAvailable(vehicle.getSettlementInventory());
//                    //boolean wearingSuit = robot.getSettlementInventory().containsUnitClass(EVASuit.class);
//                    //goodEVASuit = roverSuit || wearingSuit;
//                    //if (goodEVASuit) {
//
//                        // Walk to nearest emergency airlock in settlement.
//                        Airlock airlock = settlement.getClosestAvailableAirlock(robot);
//                        if (airlock != null) {
//                            LocalBoundedObject entity = (LocalBoundedObject) airlock.getEntity();
//                            Point2D interiorPos = LocalAreaUtil.getRandomInteriorLocation(entity);
//                            Point2D adjustedInteriorPos = LocalAreaUtil.getLocalRelativeLocation(
//                                    interiorPos.getX(), interiorPos.getY(), entity);
//                            walkingSteps = new WalkingSteps(robot, adjustedInteriorPos.getX(),
//                                    adjustedInteriorPos.getY(), entity);
//                            walkToSettlement = true;
//                        }
//                    //}
//
//                }
//                else {
//                    // If not a rover, retrieve robot from vehicle.
//                    vehicle.getInventory().retrieveUnit(robot);
//                }
//            }
//
//            if (!walkToSettlement) {
//
//                // Walk to random location within rover.
//                if (robot.getVehicle() instanceof Rover) {
//                    Rover rover = (Rover) robot.getVehicle();
//                    Point2D interiorPos = LocalAreaUtil.getRandomInteriorLocation(rover);
//                    Point2D adjustedInteriorPos = LocalAreaUtil.getLocalRelativeLocation(
//                            interiorPos.getX(), interiorPos.getY(), rover);
//                    walkingSteps = new WalkingSteps(robot, adjustedInteriorPos.getX(),
//                            adjustedInteriorPos.getY(), rover);
//                }
//            }
//        }

		// Determine if robot is outside.
//        else {//if (robot.isOutside()) {
//
//            Airlock airlock = findEmergencyAirlock(robot);
//            if (airlock != null) {
//                LocalBoundedObject entity = (LocalBoundedObject) airlock.getEntity();
//                Point2D interiorPos = LocalAreaUtil.getRandomInteriorLocation(entity);
//                Point2D adjustedInteriorPos = LocalAreaUtil.getLocalRelativeLocation(
//                        interiorPos.getX(), interiorPos.getY(), entity);
//                walkingSteps = new WalkingSteps(robot, adjustedInteriorPos.getX(),
//                        adjustedInteriorPos.getY(), entity);
//            }
//        }

//        else {
//            throw new IllegalStateException("Could not determine walking steps for " + robot.getName() +
//                    " at location " + robot.getLocationSituation());
//        }

		if (walkingSteps == null) {
			logger.severe("Walking steps could not be determined for " + robot.getName());
			endTask();
		} else if (!canWalkAllSteps(robot, walkingSteps)) {
			logger.fine("Valid Walking steps could not be determined for " + robot.getName());
			endTask();
		}

		// Initialize task phase.
		// Temporarily disabled the possibility of exiting airlock for bots
		addPhase(WALKING_SETTLEMENT_INTERIOR);

		setPhase(getWalkingStepPhase());
	}

	/**
	 * Constructor with destination parameters.
	 * 
	 * @param person         the person performing the task.
	 * @param xLoc           the destination X location.
	 * @param yLoc           the destination Y location.
	 * @param interiorObject the interior destination object (inhabitable building
	 *                       or rover).
	 */
	public Walk(Person person, double xLoc, double yLoc, LocalBoundedObject interiorObject) {
		super(null, person, false, false, 0D, false, 0D);

		// logger.finer(person + " starting new walk task to a location in " +
		// interiorObject);

		// Initialize data members.
		walkingStepIndex = 0;

		walkingSteps = new WalkingSteps(person, xLoc, yLoc, interiorObject);

		// End task if all steps cannot be walked.
		if (!canWalkAllSteps(person, walkingSteps)) {
			LogConsolidated.log(logger, Level.SEVERE, 5000, sourceName,
					"[" + person.getLocationTag().getLocale() + "] "
      						+ person + " is in " + person.getLocationTag().getImmediateLocation()
					+ " and could not find valid walking steps to " + interiorObject, null);
			endTask();
		}

		// Initialize task phase.
		addPhase(WALKING_SETTLEMENT_INTERIOR);
		addPhase(WALKING_ROVER_INTERIOR);
		addPhase(WALKING_EXTERIOR);
		addPhase(EXITING_AIRLOCK);
		addPhase(ENTERING_AIRLOCK);
		addPhase(EXITING_ROVER_GARAGE);
		addPhase(ENTERING_ROVER_GARAGE);

		setPhase(getWalkingStepPhase());
	}

	public Walk(Robot robot, double xLoc, double yLoc, LocalBoundedObject interiorObject) {
		super(null, robot, false, false, 0D, false, 0D);

		// logger.finer(robot + " starting new walk task to a location in " +
		// interiorObject);

		// Initialize data members.
		walkingStepIndex = 0;

		walkingSteps = new WalkingSteps(robot, xLoc, yLoc, interiorObject);

		// End task if all steps cannot be walked.
		if (!canWalkAllSteps(robot, walkingSteps)) {
			LogConsolidated.log(logger, Level.SEVERE, 5000, sourceName,
					"[" + robot.getLocationTag().getLocale() + "] "
      						+ robot + " is in " + robot.getLocationTag().getImmediateLocation()
      						+ " and could not find valid walking steps to " + interiorObject, null);
			endTask();
		}

		// Initialize task phase.
		addPhase(WALKING_SETTLEMENT_INTERIOR);

		setPhase(getWalkingStepPhase());
	}

	/**
	 * Find an emergency airlock at a person's location.
	 * 
	 * @param person the person.
	 * @return airlock or null if none found.
	 */
	public static Airlock findEmergencyAirlock(Person person) {

		if (unitManager == null)
			unitManager = Simulation.instance().getUnitManager();

		Airlock result = null;

		// Determine airlock from other members on mission.
		if (person.getMind().getMission() != null) {
			Iterator<MissionMember> i = person.getMind().getMission().getMembers().iterator();
			while (i.hasNext() && (result == null)) {
				MissionMember member = i.next();
				if (member != person) {
					if (member.isInSettlement()) {
						result = member.getSettlement().getClosestAvailableAirlock(person);
					} else if (member.isInVehicle()) {
						Vehicle vehicle = member.getVehicle();
						if (vehicle instanceof Airlockable) {
							result = ((Airlockable) vehicle).getAirlock();
						}
					}
				}
			}
		}

		// If not look for any settlements at person's location.
		if (result == null) {
			Iterator<Settlement> i = unitManager.getSettlements().iterator();
			while (i.hasNext() && (result == null)) {
				Settlement settlement = i.next();
				if (person.getCoordinates().equals(settlement.getCoordinates())) {
					result = settlement.getClosestAvailableAirlock(person);
				}
			}
		}

		// If not look for any vehicles with airlocks at person's location.
		if (result == null) {
			Iterator<Vehicle> i = unitManager.getVehicles().iterator();
			while (i.hasNext() && (result == null)) {
				Vehicle vehicle = i.next();
				if (person.getCoordinates().equals(vehicle.getCoordinates())) {
					if (vehicle instanceof Airlockable) {
						result = ((Airlockable) vehicle).getAirlock();
					}
				}
			}
		}

		return result;
	}

//    public static Airlock findEmergencyAirlock(Robot robot) {
//
//        Airlock result = null;
//
//        // Determine airlock from other robots on mission.
//        if (robot.getBotMind().getMission() != null) {
//            Iterator<MissionMember> i = robot.getBotMind().getMission().getMembers().iterator();
//            while (i.hasNext() && (result == null)) {
//                MissionMember member = i.next();
//                if (member != robot) {
//                    if (member.isInSettlement()) {
//                        result = member.getSettlement().getClosestAvailableAirlock(robot);
//                    }
//                    else if (member.isInVehicle()) {
//                        Vehicle vehicle = member.getVehicle();
//                        if (vehicle instanceof Airlockable) {
//                            result = ((Airlockable) vehicle).getAirlock();
//                        }
//                    }
//                }
//            }
//        }
//
//        // If not look for any settlements at robot's location.
//        if (result == null) {
//            Iterator<Settlement> i = unitManager.getSettlements().iterator();
//            while (i.hasNext() && (result == null)) {
//                Settlement settlement = i.next();
//                if (robot.getCoordinates().equals(settlement.getCoordinates())) {
//                    result = settlement.getClosestAvailableAirlock(robot);
//                }
//            }
//        }
//
//        // If not look for any vehicles with airlocks at robot's location.
//        if (result == null) {
//            Iterator<Vehicle> i = unitManager.getVehicles().iterator();
//            while (i.hasNext() && (result == null)) {
//                Vehicle vehicle = i.next();
//                if (robot.getCoordinates().equals(vehicle.getCoordinates())) {
//                    if (vehicle instanceof Airlockable) {
//                        result = ((Airlockable) vehicle).getAirlock();
//                    }
//                }
//            }
//        }
//
//        return result;
//    }

	/**
	 * Check if person can walk to a local destination.
	 * 
	 * @param person         the person.
	 * @param xLoc           the X location.
	 * @param yLoc           the Y location.
	 * @param interiorObject the destination interior object, or null if none.
	 * @return true if a person can walk all the steps to the destination.
	 */
	public static boolean canWalkAllSteps(Person person, double xLoc, double yLoc, LocalBoundedObject interiorObject) {

		WalkingSteps walkingSteps = new WalkingSteps(person, xLoc, yLoc, interiorObject);

		return canWalkAllSteps(person, walkingSteps);
	}

	public static boolean canWalkAllSteps(Robot robot, double xLoc, double yLoc, LocalBoundedObject interiorObject) {

		WalkingSteps walkingSteps = new WalkingSteps(robot, xLoc, yLoc, interiorObject);

		return canWalkAllSteps(robot, walkingSteps);
	}

	/**
	 * Check if person can walk to a local destination.
	 * 
	 * @param person       the person.
	 * @param walkingSteps the walking steps.
	 * @return true if a person can walk all the steps to the destination.
	 */
	private static boolean canWalkAllSteps(Person person, WalkingSteps walkingSteps) {

		boolean result = true;

		// Check if all steps can be walked.
		if (!walkingSteps.canWalkAllSteps()) {
			result = false;
		}

		// Check if all airlocks can be exited.
		if (!canExitAllAirlocks(person, walkingSteps)) {
			result = false;
		}

		return result;
	}

	private static boolean canWalkAllSteps(Robot robot, WalkingSteps walkingSteps) {

		boolean result = true;

		// Check if all steps can be walked.
		if (!walkingSteps.canWalkAllSteps()) {
			result = false;
		}

		// Check if all airlocks can be exited.
//        if (!canExitAllAirlocks(robot, walkingSteps)) {
//            result = false;
//        }

		return result;
	}

	/**
	 * Populates the walking step phase map.
	 */
	private void populateWalkingStepPhaseMap() {

		walkingStepPhaseMap = new HashMap<Integer, TaskPhase>(7);
		walkingStepPhaseMap.put(WalkingSteps.WalkStep.ENTER_AIRLOCK, ENTERING_AIRLOCK);
		walkingStepPhaseMap.put(WalkingSteps.WalkStep.ENTER_GARAGE_ROVER, ENTERING_ROVER_GARAGE);
		walkingStepPhaseMap.put(WalkingSteps.WalkStep.EXIT_AIRLOCK, EXITING_AIRLOCK);
		walkingStepPhaseMap.put(WalkingSteps.WalkStep.EXIT_GARAGE_ROVER, EXITING_ROVER_GARAGE);
		walkingStepPhaseMap.put(WalkingSteps.WalkStep.EXTERIOR_WALK, WALKING_EXTERIOR);
		walkingStepPhaseMap.put(WalkingSteps.WalkStep.ROVER_INTERIOR_WALK, WALKING_ROVER_INTERIOR);
		walkingStepPhaseMap.put(WalkingSteps.WalkStep.SETTLEMENT_INTERIOR_WALK, WALKING_SETTLEMENT_INTERIOR);
	}

	/**
	 * Populates the walking step phase map.
	 */
	private void populateRobotWalkingStepPhaseMap() {

		walkingStepPhaseMap = new HashMap<Integer, TaskPhase>(1);
		walkingStepPhaseMap.put(WalkingSteps.WalkStep.SETTLEMENT_INTERIOR_WALK, WALKING_SETTLEMENT_INTERIOR);
	}

	/**
	 * Gets the walking step phase.
	 * 
	 * @return walking step task phase.
	 */
	private TaskPhase getWalkingStepPhase() {

		TaskPhase result = null;

		if (person != null) {

			// Create and populate walkingStepPhaseMap if necessary.
			if (walkingStepPhaseMap == null) {
				populateWalkingStepPhaseMap();
			}

			if (walkingStepIndex < walkingSteps.getWalkingStepsNumber()) {

				WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
				result = walkingStepPhaseMap.get(step.stepType);
			}

			else { // if (result == null)
				logger.fine(person.getName() + " in " + person.getBuildingLocation().getNickName() + " at "
						+ person.getAssociatedSettlement() + " : setting TaskPhase to null ");
			}

		} else if (robot != null) {

			// Create and populate walkingStepPhaseMap if necessary.
			if (walkingStepPhaseMap == null) {
				populateRobotWalkingStepPhaseMap();
			}
			if (walkingStepIndex < walkingSteps.getRobotWalkingStepsNumber()) {
				WalkingSteps.RobotWalkStep step = walkingSteps.getRobotWalkingStepsList().get(walkingStepIndex);
				result = walkingStepPhaseMap.get(step.stepType);
			}

			else { // if (result == null)
				logger.fine(robot.getName() + " in " + robot.getBuildingLocation().getNickName() + " at "
						+ robot.getAssociatedSettlement()
						+ " : walkingStepIndex >= walkingSteps.getWalkingStepsNumber()");

			}
		}

		return result;
	}

	/**
	 * Checks if a person can exit all airlocks in walking steps.
	 * 
	 * @return true is all airlocks can be exited (or no airlocks in walk steps).
	 */
	private static boolean canExitAllAirlocks(Person person, WalkingSteps walkingSteps) {

		boolean result = true;

		List<WalkingSteps.WalkStep> stepList = walkingSteps.getWalkingStepsList();
		if (stepList != null) {
			Iterator<WalkingSteps.WalkStep> i = stepList.iterator();
			while (i.hasNext()) {
				WalkingSteps.WalkStep step = i.next();
				if (step.stepType == WalkingSteps.WalkStep.EXIT_AIRLOCK) {
					Airlock airlock = step.airlock;
					if (!ExitAirlock.canExitAirlock(person, airlock)) {
						result = false;
						 LogConsolidated.log(logger, Level.WARNING, 5000, sourceName,
								 "[" + person.getLocationTag().getLocale() + "] "
				      					+ person + " is in " + person.getLocationTag().getImmediateLocation()
				      					+ " and cannot exit airlock at " + airlock.getEntityName(), null);
					}
				}
			}
		}

		return result;
	}

//    private static boolean canExitAllAirlocks(Robot robot, WalkingSteps walkingSteps) {
//
//        boolean result = true;
//
//        List<WalkingSteps.WalkStep> stepList = walkingSteps.getWalkingStepsList();
//        if (stepList != null) {
//            Iterator<WalkingSteps.WalkStep> i = stepList.iterator();
//            while (i.hasNext()) {
//                WalkingSteps.WalkStep step = i.next();
//                if (step.stepType == WalkingSteps.WalkStep.EXIT_AIRLOCK) {
//                    Airlock airlock = step.airlock;
//                    if (!ExitAirlock.canExitAirlock(robot, airlock)) {
//                        result = false;
//                       	LogConsolidated.log(logger, Level.SEVERE, 5000, sourceName,
//                       			robot + " cannot exit airlock at " + airlock.getEntityName(), null);
//                    }
//                }
//            }
//        }
//
//        return result;
//    }

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			return time;
//            throw new IllegalArgumentException("Task phase is null");
		} else if (WALKING_SETTLEMENT_INTERIOR.equals(getPhase())) {
			return walkingSettlementInteriorPhase(time);
		} else if (WALKING_ROVER_INTERIOR.equals(getPhase())) {
			return walkingRoverInteriorPhase(time);
		} else if (WALKING_EXTERIOR.equals(getPhase())) {
			return walkingExteriorPhase(time);
		} else if (EXITING_AIRLOCK.equals(getPhase())) {
			return exitingAirlockPhase(time);
		} else if (ENTERING_AIRLOCK.equals(getPhase())) {
			return enteringAirlockPhase(time);
		} else if (EXITING_ROVER_GARAGE.equals(getPhase())) {
			return exitingRoverGaragePhase(time);
		} else if (ENTERING_ROVER_GARAGE.equals(getPhase())) {
			return enteringRoverInsideGaragePhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the walking settlement interior phase of the task.
	 * 
	 * @param time the amount of time (millisol) to perform the walking phase.
	 * @return the amount of time (millisol) left after performing the walking
	 *         phase.
	 */
	private double walkingSettlementInteriorPhase(double time) {
		double timeLeft = time;

		if (person != null) {
			logger.finer(person + " walking settlement interior phase.");

			// Check if person has reached destination location.
			WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
			Building building = BuildingManager.getBuilding(person);
			Point2D personLocation = new Point2D.Double(person.getXLocation(), person.getYLocation());
			double x = Math.round(step.xLoc * 100.0) / 100.0;
			double y = Math.round(step.yLoc * 100.0) / 100.0;
			Point2D stepLocation = new Point2D.Double(x, y);
			if (step.building.equals(building) && LocalAreaUtil.areLocationsClose(personLocation, stepLocation)) {
				if (walkingStepIndex < (walkingSteps.getWalkingStepsNumber() - 1)) {
					walkingStepIndex++;
					// setDescription("Almost arriving at (" + x + ", " + y + ") in " +
					// building.getNickName());
					setPhase(getWalkingStepPhase());
				} else {
					// setDescription("Arrived at (" + x + ", " + y + ") in " +
					// building.getNickName());
					endTask();
				}
			} else {
				if (building != null) { // && step.building != null) {
					// Going from building to step.building
					// setDescription("Walking inside from " + building.getNickName() + " to " +
					// step.building.getNickName());
					if (step.building != null)
						addSubTask(new WalkSettlementInterior(person, step.building, x, y));
					else {
						logger.severe(person + " in " + person.getBuildingLocation()
								+ " can't find a destination building to go.");
						endTask();
					}
				} else {
					logger.severe(person + " is not in a building.");
					endTask();
					// do this for now so as to debug why this happen and how often
					// setPhase(WALKING_EXTERIOR); // TODO: this certainly violate the logic and is
					// considered "cheating"
					// logger.severe(person + " set phase to WALKING_EXTERIOR.");
				}
			}

		} else if (robot != null) {
			logger.finer(robot + " walking settlement interior phase.");

			// Check if robot has reached destination location.
			WalkingSteps.RobotWalkStep step = walkingSteps.getRobotWalkingStepsList().get(walkingStepIndex);
			Building building = BuildingManager.getBuilding(robot);
			Point2D robotLocation = new Point2D.Double(robot.getXLocation(), robot.getYLocation());
			double x = Math.round(step.xLoc * 100.0) / 100.0;
			double y = Math.round(step.yLoc * 100.0) / 100.0;
			Point2D stepLocation = new Point2D.Double(x, y);
			if (step.building.equals(building) && LocalAreaUtil.areLocationsClose(robotLocation, stepLocation)) {
				if (walkingStepIndex < (walkingSteps.getRobotWalkingStepsNumber() - 1)) {
					walkingStepIndex++;
					// setDescription("Almost arriving at (" + x + ", " + y + ") in " +
					// building.getNickName());
					setPhase(getWalkingStepPhase());
				} else {
					// setDescription("Arrived at (" + x + ", " + y + ") in " +
					// building.getNickName());
					endTask();
				}
			} else {
				if (building != null) { // && step.building != null) {
					// Going from building to step.building
					// setDescription("Walking inside from " + building.getNickName() + " to " +
					// step.building.getNickName());
					addSubTask(new WalkSettlementInterior(robot, step.building, x, y));
				} else {
					logger.severe(robot + " is not in a building");
//	        		logger.info(robot + " may be at " + robot.getBuildingLocation());
//	        		logger.info(robot + "'s location is " + robot.getLocationSituation());
//	        		logger.info(robot + " is in " + robot.getSettlement());
//	        		logger.info(robot + " is associated to " + robot.getAssociatedSettlement());
//	        		logger.info(robot + " has the container unit of " + robot.getContainerUnit());
//	        		logger.info(robot + " has the top container unit of " + robot.getTopContainerUnit());
					endTask();

					// do this for now so as to debug why this happen and how often
					// setPhase(WALKING_EXTERIOR); // TODO: this certainly violate the logic and is
					// considered "cheating"
					// logger.severe(robot + "set phase to WALKING_EXTERIOR.");
				}
			}
		}

		return timeLeft;
	}

	/**
	 * Performs the walking rover interior phase of the task.
	 * 
	 * @param time the amount of time (millisol) to perform the walking phase.
	 * @return the amount of time (millisol) left after performing the walking
	 *         phase.
	 */
	private double walkingRoverInteriorPhase(double time) {
		double timeLeft = time;

		if (person != null) {
			// logger.finer(person + " walking rover interior phase.");

			// Check if person has reached destination location.
			WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
			Rover rover = (Rover) person.getVehicle();

			// TODO: working on resolving NullPointerException
			if (rover != null) {
				// Update rover destination if rover has moved and existing destination is no
				// longer within rover.
				if (!LocalAreaUtil.checkLocationWithinLocalBoundedObject(step.xLoc, step.yLoc, rover)) {
					// Determine new destination location within rover.
					// TODO: Determine location based on activity spot?
					Point2D newRoverLoc = LocalAreaUtil.getRandomInteriorLocation(rover);
					Point2D relativeRoverLoc = LocalAreaUtil.getLocalRelativeLocation(newRoverLoc.getX(),
							newRoverLoc.getY(), rover);
					step.xLoc = relativeRoverLoc.getX();
					step.yLoc = relativeRoverLoc.getY();
				}
			}

			Point2D personLocation = new Point2D.Double(person.getXLocation(), person.getYLocation());
			double x = Math.round(step.xLoc * 100.0) / 100.0;
			double y = Math.round(step.yLoc * 100.0) / 100.0;
			Point2D stepLocation = new Point2D.Double(x, y);
			// Point2D stepLocation = new Point2D.Double(step.xLoc, step.yLoc);
			if (step.rover.equals(rover) && LocalAreaUtil.areLocationsClose(personLocation, stepLocation)) {
				if (walkingStepIndex < (walkingSteps.getWalkingStepsNumber() - 1)) {
					walkingStepIndex++;
					// setDescription("Walking back to the rover at (" + x + ", " + y + ")");
					setPhase(getWalkingStepPhase());
				} else {
					// setDescription("Arrived at (" + x + ", " + y + ")");
					endTask();
				}
			} else { // this is a high traffic case when a person is in a vehicle

//				if (person.isInSettlement() || person.isInVehicleInGarage()) {
//					LogConsolidated.log(logger, Level.SEVERE, 10_000, sourceName,
//		      				"[" + person.getLocationTag().getLocale() + "] "
//							+ person + " is in " + person.getLocationTag().getImmediateLocation()
//							+ " but is in walkingRoverInteriorPhase() and NOT in a rover.",
//							null);
//					person.getMind().getTaskManager().clearTask();
//					person.getMind().getTaskManager().getNewTask();// .clearTask();
//				}

				if (person.isInVehicle() || person.isInVehicleInGarage()) {
					addSubTask(new WalkRoverInterior(person, step.rover, x, y));
				}

				else if (person.isOutside()) {
					LogConsolidated.log(logger, Level.SEVERE, 10_000, sourceName,
							"[" + person.getLocationTag().getLocale() + "] "
							+ person +  " is in " + person.getLocationTag().getImmediateLocation()
							+ " and is outside but is in walkingRoverInteriorPhase() and NOT in rover.", null);
					person.getMind().getTaskManager().clearTask();
					person.getMind().getTaskManager().getNewTask();// clearTask();
				}

			}

		} else if (robot != null) {
			logger.finer(robot + " walking rover interior phase.");
			// Check if robot has reached destination location.
			WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
			Rover rover = (Rover) robot.getVehicle();

			// Update rover destination if rover has moved and existing destination is no
			// longer within rover.
			if (!LocalAreaUtil.checkLocationWithinLocalBoundedObject(step.xLoc, step.yLoc, rover)) {
				// Determine new destination location within rover.
				// TODO: Determine location based on activity spot?
				Point2D newRoverLoc = LocalAreaUtil.getRandomInteriorLocation(rover);
				Point2D relativeRoverLoc = LocalAreaUtil.getLocalRelativeLocation(newRoverLoc.getX(),
						newRoverLoc.getY(), rover);
				step.xLoc = relativeRoverLoc.getX();
				step.yLoc = relativeRoverLoc.getY();
			}

			Point2D robotLocation = new Point2D.Double(robot.getXLocation(), robot.getYLocation());
			// Point2D stepLocation = new Point2D.Double(step.xLoc, step.yLoc);
			double x = Math.round(step.xLoc * 100.0) / 100.0;
			double y = Math.round(step.yLoc * 100.0) / 100.0;
			Point2D stepLocation = new Point2D.Double(x, y);
			if (step.rover.equals(rover) && LocalAreaUtil.areLocationsClose(robotLocation, stepLocation)) {
				if (walkingStepIndex < (walkingSteps.getWalkingStepsNumber() - 1)) {
					walkingStepIndex++;
					// setDescription("Walking back to the rover at (" + x + ", " + y + ")");
					setPhase(getWalkingStepPhase());
				} else {
					// setDescription("Arrived at (" + x + ", " + y + ")");
					endTask();
				}
			} else {
				logger.finer("Starting walk rover interior from Walk.walkingRoverInteriorPhase.");
				addSubTask(new WalkRoverInterior(robot, step.rover, x, y));
			}

		}

		return timeLeft;
	}

	/**
	 * Performs the walking exterior phase of the task.
	 * 
	 * @param time the amount of time (millisol) to perform the walking phase.
	 * @return the amount of time (millisol) left after performing the walking
	 *         phase.
	 */
	private double walkingExteriorPhase(double time) {

		double timeLeft = time;

		if (person != null) {

			logger.finer(person + " walking exterior phase.");

			// Check if person has reached destination location.
			WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
			Point2D personLocation = new Point2D.Double(person.getXLocation(), person.getYLocation());
			double x = person.getXLocation();
			double y = person.getYLocation();

			double xx = Math.round(step.xLoc * 100.0) / 100.0;
			double yy = Math.round(step.yLoc * 100.0) / 100.0;
			Point2D stepLocation = new Point2D.Double(xx, yy);
			// Point2D stepLocation = new Point2D.Double(step.xLoc, step.yLoc);
			if (LocalAreaUtil.areLocationsClose(personLocation, stepLocation)) {
				if (walkingStepIndex < (walkingSteps.getWalkingStepsNumber() - 1)) {
					walkingStepIndex++;
					// setDescription("Walking to (" + xx + ", " + yy + ")");
					setPhase(getWalkingStepPhase());
				} else {

					// setDescription("Arriving at (" + xx + ", " + yy + ")");
					endTask();
				}
			} else {
				// endTask();
				if (person.isOutside()) {
					logger.finer(person + " starting walk outside task.");
					// setDescription("Walking Outside from (" + x + ", " + y + ") to (" + xx + ", "
					// + yy + ")");
					addSubTask(new WalkOutside(person, x, y, xx, yy, true));
				} else {
					logger.severe(person + " is already physically outside.");
					endTask();
				}

			}

		} else if (robot != null) {

			logger.finer(robot + " walking exterior phase.");

			// Check if robot has reached destination location.
			WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
			Point2D robotLocation = new Point2D.Double(robot.getXLocation(), robot.getYLocation());
			double xx = Math.round(step.xLoc * 100.0) / 100.0;
			double yy = Math.round(step.yLoc * 100.0) / 100.0;
			Point2D stepLocation = new Point2D.Double(xx, yy);
			// Point2D stepLocation = new Point2D.Double(step.xLoc, step.yLoc);
			double x = robot.getXLocation();
			double y = robot.getYLocation();
			if (LocalAreaUtil.areLocationsClose(robotLocation, stepLocation)) {
				if (walkingStepIndex < (walkingSteps.getWalkingStepsNumber() - 1)) {
					walkingStepIndex++;
					// setDescription("Walking toward (" + xx + ", " + yy + ")");
					setPhase(getWalkingStepPhase());
				} else {
					// setDescription("Arriving at (" + xx + ", " + yy + ")");
					endTask();
				}
			} else {
				// endTask();
				if (robot.isOutside()) {
					logger.finer(robot + " starting walk outside task.");
					// setDescription("Walking Outside from (" + x + ", " + y + ") to (" + xx + ", "
					// + yy + ")");
					addSubTask(new WalkOutside(robot, x, y, xx, yy, true));
				}

				else {
					logger.severe(robot + " is already physically outside.");
					endTask();
				}

			}

		}

		return timeLeft;
	}

	/**
	 * Performs the exiting airlock phase of the task.
	 * 
	 * @param time the amount of time (millisol) to perform the walking phase.
	 * @return the amount of time (millisol) left after performing the walking
	 *         phase.
	 */
	private double exitingAirlockPhase(double time) {
		double timeLeft = time;
		if (person != null) {
			logger.finer(person + " in exitingAirlockPhase()");
			// Check if person has reached the outside of the airlock.
			WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
			Airlock airlock = step.airlock;
			// TODO: what does it mean to be outside in exitingAirlockPhase() ?
			if (person.isOutside()) {
				if (walkingStepIndex < (walkingSteps.getWalkingStepsNumber() - 1)) {
					walkingStepIndex++;
					// setDescription("Walking outside to an airlock");
					// setDescription("is OUTSIDE and still walking...");
					setPhase(getWalkingStepPhase());
				} else {
					// setDescription("Arriving at an airlock outside a building");
					// setDescription("is OUTSIDE and have arrived");
					endTask();
				}
			} else {
				if (ExitAirlock.canExitAirlock(person, airlock)) {
					logger.finer(person + " to add ExitAirlock as a subTask");
					addSubTask(new ExitAirlock(person, airlock));
				} else {
					LogConsolidated.log(
							logger, Level.SEVERE, 1000, sourceName, 
		      				"[" + person.getLocationTag().getLocale() + "] "
		      						+ person + " is in " + person.getLocationTag().getImmediateLocation()
									+ " is unable to physically exit the airlock of " + airlock.getEntityName() + ".",
							null);

					person.getMind().getTaskManager().clearTask();
					person.getMind().getTaskManager().getNewTask();
					// endTask(); // will call Walk many times again
				}
			}

		} else if (robot != null) {
			// Note : robot is NOT allowed to leave the settlement
			endTask();

//            logger.finer(robot + " in exitingAirlockPhase()");
//
//            // Check if robot has reached the outside of the airlock.
//            WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
//            Airlock airlock = step.airlock;
//            if (robot.getLocationSituation() == LocationSituation.OUTSIDE) {
//                if (walkingStepIndex < (walkingSteps.getWalkingStepsNumber() - 1)) {
//                    walkingStepIndex++;
//    	        	//setDescription("Walking outside to an airlock");
//    	        	setDescription("Already OUTSIDE and still walking...");
//                    setPhase(getWalkingStepPhase());
//                }
//                else {
//    	        	//setDescription("Arriving at an airlock outside a building");
//    	        	setDescription("Already OUTSIDE walking and have arrived.");
//                    endTask();
//                }
//            }
//            else {
//                if (ExitAirlock.canExitAirlock(robot, airlock)) {
//                    logger.finer(robot + " to add ExitAirlock as a subTask");
//                    addSubTask(new ExitAirlock(robot, airlock));
//                }
//                else {
//                    logger.severe(robot.getName() + " unable to physically exit the airlock of " +
//                            airlock.getEntityName() + ". Calling endTask()");
//                    endTask();
//                }
//            }
		}

		return timeLeft;
	}

	/**
	 * Performs the entering airlock phase of the task.
	 * 
	 * @param time the amount of time (millisol) to perform the walking phase.
	 * @return the amount of time (millisol) left after performing the walking
	 *         phase.
	 */
	private double enteringAirlockPhase(double time) {
		double timeLeft = time;

//        if (person != null) {
		logger.finer(person + " walking entering airlock phase.");

		// Check if person has reached the inside of the airlock.
		WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
		Airlock airlock = step.airlock;
		if (person.isOutside()) {
			if (EnterAirlock.canEnterAirlock(person, airlock)) {
				// setDescription("is OUTSIDE and attempting to enter an airlock. calling
				// EnterAirlock as a subTask now");
				addSubTask(new EnterAirlock(person, airlock));
			} else {
				endTask();
				LogConsolidated.log(
						logger, Level.SEVERE, 1000, sourceName, 
	      				"[" + person.getLocationTag().getLocale() + "] "
	      						+ person + " is in " + person.getLocationTag().getImmediateLocation()
								+ " is OUTSIDE, unable to physically enter the airlock of " + airlock.getEntityName(),
						null);
			}

		} else {
			if (walkingStepIndex < (walkingSteps.getWalkingStepsNumber() - 1)) {
				walkingStepIndex++;
				// setDescription("Walking outside to an airlock to enter");
				// setDescription("is INSIDE and still walking toward an airlock");
				setPhase(getWalkingStepPhase());
			} else {
				// setDescription("is INSIDE and arrived at an airlock");
				endTask();
			}
		}

//        }
//        else if (robot != null) {
//            logger.finer(robot + " walking entering airlock phase.");
//
//            // Check if robot has reached the inside of the airlock.
//            WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
//            Airlock airlock = step.airlock;
//            if (robot.isOutside()) {
//                if (EnterAirlock.canEnterAirlock(robot, airlock)) {
//    	        	//setDescription("is OUTSIDE and attempting to enter an airlock. calling EnterAirlock as a subTask now");
//                    addSubTask(new EnterAirlock(robot, airlock));
//                }
//                else {
//                    endTask();
//                    LogConsolidated.log(logger, Level.SEVERE, 1000, sourceName, 
//                    		robot.getName() + " is still OUTSIDE, unable to physically enter the airlock of " +
//                            airlock.getEntityName(), null);
//                }
//            }
//            else {
//                if (walkingStepIndex < (walkingSteps.getWalkingStepsNumber() - 1)) {
//                    walkingStepIndex++;
//       	        	// 2015-11-11 setDescription()
//    	        	//setDescription("Walking outside to an airlock to enter");
//    	        	//setDescription("is INSIDE and still walking toward an airlock");
//                    setPhase(getWalkingStepPhase());
//                }
//                else {
//    	        	//setDescription("is INSIDE and arrived at an airlock");
//                    endTask();
//                }
//            }
//        }

		return timeLeft;
	}

	/**
	 * Performs the exiting rover in garage phase of the task.
	 * 
	 * @param time the amount of time (millisol) to perform the walking phase.
	 * @return the amount of time (millisol) left after performing the walking
	 *         phase.
	 */
	private double exitingRoverGaragePhase(double time) {

		double timeLeft = time;

		WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
		Rover rover = step.rover;
		Building garageBuilding = step.building;

		if (person != null) {
			logger.finer(person + " walking exiting rover garage phase.");
//			System.out.println("exiting rover : " + person + "'s vehicle is " + person.getVehicle().getName());
			rover.getInventory().retrieveUnit(person);
			garageBuilding.getSettlementInventory().storeUnit(person);
			BuildingManager.addPersonOrRobotToBuildingSameLocation(person, garageBuilding);
//			System.out.println("exiting rover : " + person + "'s Settlement is " + person.getSettlement());

		} 
		
		else if (robot != null) {
			logger.finer(robot + " walking exiting rover garage phase.");
			rover.getInventory().retrieveUnit(robot);
			garageBuilding.getSettlementInventory().storeUnit(robot);
			BuildingManager.addPersonOrRobotToBuildingSameLocation(robot, garageBuilding);

		}

		if (walkingStepIndex < (walkingSteps.getWalkingStepsNumber() - 1)) {
			walkingStepIndex++;
			setPhase(getWalkingStepPhase());
		} else {
			endTask();
		}

		return timeLeft;
	}

	/**
	 * Performs the entering rover in garage phase of the task.
	 * 
	 * @param time the amount of time (millisol) to perform the walking phase.
	 * @return the amount of time (millisol) left after performing the walking
	 *         phase.
	 */
	private double enteringRoverInsideGaragePhase(double time) {

		double timeLeft = time;

		WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
		Rover rover = step.rover;
		Building garageBuilding = step.building;

		if (person != null) {
//			logger.info(person + " walking entering rover garage phase.");
//			logger.info(person + " location situation: " + person.getLocationSituation());
			garageBuilding.getSettlementInventory().retrieveUnit(person);
//			System.out.println("entering rover : " + person + "'s Settlement is " + person.getSettlement());
//			person.getSettlement().getInventory().retrieveUnit(person);
			BuildingManager.removePersonOrRobotFromBuilding(person, garageBuilding);
			rover.getInventory().storeUnit(person);
//			System.out.println("entering rover : " + person + "'s vehicle is " + person.getVehicle().getName());
		} 
		
		else if (robot != null) {
			logger.finer(robot + " walking entering rover garage phase.");
			logger.finer(robot + " location situation: " + robot.getLocationSituation());
			garageBuilding.getSettlementInventory().retrieveUnit(robot);
//			robot.getSettlement().getInventory().retrieveUnit(person);
			BuildingManager.removePersonOrRobotFromBuilding(robot, garageBuilding);
			rover.getInventory().storeUnit(robot);

		}

		if (walkingStepIndex < (walkingSteps.getWalkingStepsNumber() - 1)) {
			walkingStepIndex++;
			setPhase(getWalkingStepPhase());
		} else {
			endTask();
		}

		return timeLeft;
	}

	/**
	 * Checks if the person or robot is walking through a given building.
	 * 
	 * @param building the building.
	 * @return true if walking through building.
	 */
	public boolean isWalkingThroughBuilding(Building building) {
		boolean result = false;

		// Check if any walk steps are in building.
		Iterator<WalkStep> i = walkingSteps.getWalkingStepsList().iterator();
		while (i.hasNext() && !result) {
			WalkStep step = i.next();
			if (building.equals(step.building)) {
				result = true;
			}
		}

		return result;
	}

	/**
	 * Checks if the person or robot is walking through a given vehicle.
	 * 
	 * @param vehicle the vehicle.
	 * @return true if walking through vehicle.
	 */
	public boolean isWalkingThroughVehicle(Vehicle vehicle) {

		boolean result = false;

		// Check if any walk steps are in vehicle.
		Iterator<WalkStep> i = walkingSteps.getWalkingStepsList().iterator();
		while (i.hasNext() && !result) {
			WalkStep step = i.next();
			if (vehicle.equals(step.rover)) {
				result = true;
			}
		}

		return result;
	}

	@Override
	public int getEffectiveSkillLevel() {
		return 0;
	}

	@Override
	public List<SkillType> getAssociatedSkills() {
		return new ArrayList<SkillType>(0);
	}

	@Override
	protected void addExperience(double time) {
		// Do nothing
	}

	public void destroy() {
		walkingSteps = null;
		walkingStepPhaseMap = null;
		unitManager = null;
	}

}
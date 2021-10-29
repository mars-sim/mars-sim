/*
 * Mars Simulation Project
 * Walk.java
 * @date 2021-10-21
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.MissionMember;
import org.mars_sim.msp.core.person.ai.task.WalkingSteps.WalkStep;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
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
	private static SimLogger logger = SimLogger.getLogger(Walk.class.getName());
	
	// Static members
	static final double PERSON_WALKING_SPEED = 1D; // [km per hr].
	static final double ROBOT_WALKING_SPEED = 0.25; // [km per hr].
	
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -.25D;
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
	private static final TaskPhase ENTERING_ROVER_GARAGE = new TaskPhase(Msg.getString("Task.phase.enteringRoverGarage")); //$NON-NLS-1$
	private static final TaskPhase CLIMB_UP_LADDER = new TaskPhase(Msg.getString("Task.phase.climbUpLadder")); //$NON-NLS-1$
	private static final TaskPhase CLIMB_DOWN_LADDER = new TaskPhase(Msg.getString("Task.phase.climbDownLadder")); //$NON-NLS-1$
	
	// Data members
	private int walkingStepIndex;

	/** The WalkingSteps instance. */
	private WalkingSteps walkingSteps;

	private Map<Integer, TaskPhase> walkingStepPhaseMap;

	/**
	 * Constructor.
	 * 
	 * @param person the person performing the task.
	 */
	public Walk(Person person) {
		super(NAME, person, false, false, STRESS_MODIFIER, null, 100D);
		// this.person = person;

		unitManager = Simulation.instance().getUnitManager();
		
		// Initialize data members.
		walkingStepIndex = 0;

		if (person.isInSettlement()) {

			// Walk to random inhabitable building at settlement.
			List<Building> buildingList = person.getSettlement().getBuildingManager().getBuildings(FunctionType.LIFE_SUPPORT);
			if (buildingList.size() > 0) {
				int buildingIndex = RandomUtil.getRandomInt(buildingList.size() - 1);
				Building destinationBuilding = buildingList.get(buildingIndex);
				Point2D interiorPos = LocalAreaUtil.getRandomInteriorLocation(destinationBuilding);
				Point2D adjustedInteriorPos = LocalAreaUtil.getLocalRelativeLocation(interiorPos.getX(),
						interiorPos.getY(), destinationBuilding);
				walkingSteps = new WalkingSteps(person, adjustedInteriorPos.getX(), adjustedInteriorPos.getY(), 0,
						destinationBuilding);
			}

		} else if (person.isInVehicle()) {

			Vehicle vehicle = person.getVehicle();

			// If no mission and vehicle is at a settlement location, enter settlement.
			boolean walkToSettlement = false;
			if ((person.getMind().getMission() == null) && (vehicle.getSettlement() != null)) {

				Settlement settlement = vehicle.getSettlement();

				// Check if vehicle is in garage.
				// person.isInVehicleInGarage()
				Building garageBuilding = BuildingManager.getBuilding(vehicle);
				if (garageBuilding != null) {

					Point2D interiorPos = LocalAreaUtil.getRandomInteriorLocation(garageBuilding);
					Point2D adjustedInteriorPos = LocalAreaUtil.getLocalRelativeLocation(interiorPos.getX(),
							interiorPos.getY(), garageBuilding);
					walkingSteps = new WalkingSteps(person, adjustedInteriorPos.getX(), adjustedInteriorPos.getY(), 0,
							garageBuilding);
					walkToSettlement = true;
				} 
				
				else if (vehicle instanceof Rover) {
					// If not on a LUV

					// Check if person has a good EVA suit available if in a rover.
					boolean goodEVASuit = true;
					boolean roverSuit = vehicle.containsEquipment(EquipmentType.EVA_SUIT);
					boolean wearingSuit = (person.getSuit() != null);
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
									adjustedInteriorPos.getY(), 0, entity);
							walkToSettlement = true;
						}
					}
				}
				else {
					// If on a LUV, retrieve person from vehicle.
					if (person.transfer(unitManager.getMarsSurface())) {
						logger.info(person, "successfully retrieved " + person + " from " + vehicle.getName());
					}
					else {
						logger.warning(worker, "failed to retrieve " + person + " from " + vehicle.getName());
					}
				}
			}

			if (!walkToSettlement) {

				// Walk to random location within rover.
				if (vehicle instanceof Rover) {
					Rover rover = (Rover) person.getVehicle();
					Point2D interiorPos = LocalAreaUtil.getRandomInteriorLocation(rover);
					Point2D adjustedInteriorPos = LocalAreaUtil.getLocalRelativeLocation(interiorPos.getX(),
							interiorPos.getY(), rover);
					walkingSteps = new WalkingSteps(person, adjustedInteriorPos.getX(), adjustedInteriorPos.getY(), 0,
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
				walkingSteps = new WalkingSteps(person, adjustedInteriorPos.getX(), adjustedInteriorPos.getY(), 0, entity);
			}
		}

		if (walkingSteps == null) {
			logger.log(person, Level.SEVERE, 4_000,
      				"Walking steps could not be determined.");

			endTask();
			return;
		} 
		
		else if (!canWalkAllSteps(person, walkingSteps)) {
			logger.log(person, Level.SEVERE, 4_000, "Could not walk all steps.");
			endTask();
			return;
		}

		// Initialize task phase.
		addPhase(WALKING_SETTLEMENT_INTERIOR);
		addPhase(WALKING_ROVER_INTERIOR);
		addPhase(WALKING_EXTERIOR);
		addPhase(EXITING_AIRLOCK);
		addPhase(ENTERING_AIRLOCK);
		addPhase(EXITING_ROVER_GARAGE);
		addPhase(ENTERING_ROVER_GARAGE);
		addPhase(CLIMB_UP_LADDER);
		addPhase(CLIMB_DOWN_LADDER);
		
		setPhase(getWalkingStepPhase());
	}

	/**
	 * Constructor 2.
	 * 
	 * @param robot the robot performing the task.
	 */
	public Walk(Robot robot) {
		super(null, robot, false, false, 0D, null, 100D);
		// this.robot = robot;
		// logger.finer(robot + " starting new walk task.");

		// Initialize data members.
		walkingStepIndex = 0;

		if (robot.isInSettlement()) {// || robot.isInVehicleInGarage()) {

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
				walkingSteps = new WalkingSteps(robot, adjustedInteriorPos.getX(), adjustedInteriorPos.getY(), 0,
						destinationBuilding);
			}

		}

		if (walkingSteps == null) {
			logger.log(robot, Level.WARNING, 0,
				"Walking steps could not be determined");
			endTask();
			return;
		} 
		
		else if (!canWalkAllSteps(robot, walkingSteps)) {
			logger.log(robot, Level.WARNING, 0,
					"Could not walk all steps.");
			endTask();
			return;
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
	public Walk(Person person, double xLoc, double yLoc, double zLoc, LocalBoundedObject interiorObject) {
		super("Walk", person, false, false, 0D, null, 100D);

		// logger.finer(person + " starting new walk task to a location in " +
		// interiorObject);

		// Initialize data members.
		walkingStepIndex = 0;

		walkingSteps = new WalkingSteps(person, xLoc, yLoc, zLoc, interiorObject);

		// End task if all steps cannot be walked.
		if (!canWalkAllSteps(person, walkingSteps)) {
			logger.log(person, Level.WARNING, 0,
					"Could not walk all steps to " + interiorObject);
			endTask();
			return;
		}

		// Initialize task phase.
		addPhase(WALKING_SETTLEMENT_INTERIOR);
		addPhase(WALKING_ROVER_INTERIOR);
		addPhase(WALKING_EXTERIOR);
		addPhase(EXITING_AIRLOCK);
		addPhase(ENTERING_AIRLOCK);
		addPhase(EXITING_ROVER_GARAGE);
		addPhase(ENTERING_ROVER_GARAGE);
		addPhase(CLIMB_UP_LADDER);
		addPhase(CLIMB_DOWN_LADDER);
		
		setPhase(getWalkingStepPhase());
	}

	public Walk(Robot robot, double xLoc, double yLoc, double zLoc, LocalBoundedObject interiorObject) {
		super("Walk", robot, false, false, 0D, null, 100D);

		// logger.finer(robot + " starting new walk task to a location in " +
		// interiorObject);

		// Initialize data members.
		walkingStepIndex = 0;

		walkingSteps = new WalkingSteps(robot, xLoc, yLoc, zLoc, interiorObject);

		// End task if all steps cannot be walked.
		if (!canWalkAllSteps(robot, walkingSteps)) {
			logger.log(robot, Level.WARNING, 0,
					"Could not walk all steps to " + interiorObject);
			endTask();
			return;
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


		if (unitManager == null)
			unitManager = Simulation.instance().getUnitManager();

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
	public static boolean canWalkAllSteps(Person person, double xLoc, double yLoc, double zLoc, LocalBoundedObject interiorObject) {

		WalkingSteps walkingSteps = null;
		
		try {
			walkingSteps = new WalkingSteps(person, xLoc, yLoc, zLoc, interiorObject);
		} catch (Exception e) {
          	logger.log(Level.SEVERE, "Cannot instantiate walking steps: " + e.getMessage());
		}

		if (walkingSteps != null)
			return canWalkAllSteps(person, walkingSteps);
		
		return false;
	}

	public static boolean canWalkAllSteps(Robot robot, double xLoc, double yLoc, double zLoc, LocalBoundedObject interiorObject) {

		WalkingSteps walkingSteps = new WalkingSteps(robot, xLoc, yLoc, zLoc, interiorObject);

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

		boolean result = walkingSteps.canWalkAllSteps();

		// Check if all steps can be walked.

        // Check if all airlocks can be exited.
		if (!canExitAllAirlocks(person, walkingSteps)) {
			result = false;
		}

		return result;
	}

	private static boolean canWalkAllSteps(Robot robot, WalkingSteps walkingSteps) {

		boolean result = walkingSteps.canWalkAllSteps();

		// Check if all steps can be walked.

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
		walkingStepPhaseMap = new ConcurrentHashMap<Integer, TaskPhase>(7);
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
		walkingStepPhaseMap = new ConcurrentHashMap<Integer, TaskPhase>(1);
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

			else {
				logger.log(person, Level.FINE, 0,
						"Invalid walking step index.");
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

			else {
				logger.log(robot, Level.FINE, 0,
						"Invalid walking step index.");
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

						logger.log(person, Level.WARNING, 0,
								"Could not exit " + airlock.getEntityName());
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
//                       	LogConsolidated.log(Level.SEVERE, 4_000, sourceName,
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
			return 0;
//            throw new IllegalArgumentException("Task phase is null");
		} else if (WALKING_SETTLEMENT_INTERIOR.equals(getPhase())) {
			return walkingSettlementInteriorPhase(time);
		} else if (WALKING_ROVER_INTERIOR.equals(getPhase())) {
			return walkingRoverInteriorPhase(time);
		} else if (WALKING_EXTERIOR.equals(getPhase())) {
			return walkingExteriorPhase(time);
		} else if (EXITING_AIRLOCK.equals(getPhase())) {
			return egressingAirlockPhase(time);
		} else if (ENTERING_AIRLOCK.equals(getPhase())) {
			return ingressingAirlockPhase(time);
		} else if (EXITING_ROVER_GARAGE.equals(getPhase())) {
			return exitingRoverGaragePhase(time);
		} else if (ENTERING_ROVER_GARAGE.equals(getPhase())) {
			return enteringRoverInsideGaragePhase(time);
//		} else if (CLIMB_UP_LADDER.equals(getPhase())) {
//			return climbingUpLadder(time);			
//		} else if (CLIMB_DOWN_LADDER.equals(getPhase())) {
//			return climbingDownLadder(time);						
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
		setDescription(Msg.getString("Task.description.walk")); //$NON-NLS-1$
		
		if (person != null) {
			logger.log(person, Level.FINE, 0,
					"At walkingSettlementInteriorPhase.");

			// Check if person has reached destination location.
			WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
			Building building = BuildingManager.getBuilding(person);
			Point2D personLocation = new Point2D.Double(person.getXLocation(), person.getYLocation());
			double x = Math.round(step.xLoc * 100.0) / 100.0;
			double y = Math.round(step.yLoc * 100.0) / 100.0;
			Point2D stepLocation = new Point2D.Double(x, y);
			
			if (step.building != null && step.building.equals(building) && LocalAreaUtil.areLocationsClose(personLocation, stepLocation)) {
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
				if (building != null) {
					// Going from building to step.building
					// setDescription("Walking inside from " + building.getNickName() + " to " +
					// step.building.getNickName());
					if (step.building != null) {
//						System.out.println("Walk::walkingSettlementInteriorPhase " + person);
						addSubTask(new WalkSettlementInterior(person, step.building, x, y, 0));
					}
					else {
						logger.log(person, Level.SEVERE, 5_000,
			      				"Could notfind a destination building to go.");
						endTask();
					}
				} else if (person.isOutside()) {
					logger.log(person, Level.SEVERE, 5_000,
							"Was not in a building.");
//					endTask();
					// do this for now so as to debug why this happen and how often
					setPhase(WALKING_EXTERIOR); // TODO: this certainly violate the logic and is
					// considered "cheating"
					// logger.severe(person + " set phase to WALKING_EXTERIOR.");
				}
			}
		} 
		else if (robot != null) {
			logger.log(robot, Level.FINER, 4000, "Walking inside a settlement.");
			
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
					logger.log(robot , Level.SEVERE, 5_000,"Was not in a building.");
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
		setDescription(Msg.getString("Task.description.walk")); //$NON-NLS-1$
		
		if (person != null) {
			
			logger.log(person, Level.FINE, 5_000,
					"Walking inside a rover.");
			
			// Check if person has reached destination location.
			WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
			Rover rover = (Rover) person.getVehicle();

			// TODO: working on resolving NullPointerException
			if (rover != null) {
				// Update rover destination if rover has moved and existing destination is no
				// longer within rover.
				if (!LocalAreaUtil.isLocationWithinLocalBoundedObject(step.xLoc, step.yLoc, rover)) {
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

				if (person.isInSettlement()) {
					logger.log(person, Level.SEVERE, 5_000,
						"Was supposed to be in a rover.");
					endTask();
//					person.getMind().getTaskManager().clearTask();
//					person.getMind().getTaskManager().getNewTask();// .clearTask();
				}

				if (person.isInVehicle() || person.isInVehicleInGarage()) {
					logger.log(person, Level.FINE , 5_000,
						"Starting WalkRoverInterior.");
					addSubTask(new WalkRoverInterior(person, step.rover, x, y));
				}

				else if (person.isOutside()) {
					logger.log(person, Level.SEVERE, 5_000,	
						"Outside calling walkingRoverInteriorPhase() and NOT in a rover.");
					endTask();
//					person.getMind().getTaskManager().clearTask();
//					person.getMind().getTaskManager().getNewTask();// clearTask();
				}

			}

		} else if (robot != null) {
			logger.log(robot, Level.SEVERE, 5_000,
					"Walking inside a rover.");
			
			// Check if robot has reached destination location.
			WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
			Rover rover = (Rover) robot.getVehicle();

			// Update rover destination if rover has moved and existing destination is no
			// longer within rover.
			if (!LocalAreaUtil.isLocationWithinLocalBoundedObject(step.xLoc, step.yLoc, rover)) {
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
//				logger.finest("Starting walk rover interior from Walk.walkingRoverInteriorPhase.");
				logger.log(person, Level.SEVERE, 5_000,
					"Starting WalkRoverInterior.");
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
		setDescription(Msg.getString("Task.description.walk")); //$NON-NLS-1$
		
		if (person != null) {
			logger.log(person, Level.FINER, 4000, 
					"Calling walkingExteriorPhase().");
			
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
					setPhase(getWalkingStepPhase());
				} 
				else {
					// setDescription("Arriving at (" + xx + ", " + yy + ")");
					endTask();
				}
			}
			else {
				if (person.isOutside()) {
					setDescription("Walking outside toward (" + xx + ", " + yy + ")");
//					logger.info(person, "Walking outside from (" + x + ", " + y + ") to (" 
//							+ xx + ", " + yy + ")");
					addSubTask(new WalkOutside(person, x, y, xx, yy, true));
				} 
				else {
					logger.log(person, Level.SEVERE, 5_000, 
							"Not being outside.");
					endTask();
				}
			}
		}
		
		else if (robot != null) {

			logger.log(robot, Level.SEVERE, 5_000,
					"Calling walkingExteriorPhase().");
			
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
				} 
				else {
					// setDescription("Arriving at (" + xx + ", " + yy + ")");
					endTask();
				}
			} 
			else {
				if (robot.isOutside()) {
					
					logger.log(robot, Level.FINER, 4_000,
							"Outside. Starting WalkOutside.");
					// setDescription("Walking Outside from (" + x + ", " + y + ") to (" + xx + ", "
					// + yy + ")");
					addSubTask(new WalkOutside(robot, x, y, xx, yy, true));
				}
				else {
					logger.log(robot, Level.SEVERE, 5_000,
							"Already physically outside.");
					endTask();
				}
			}
		}

		return timeLeft;
	}

	/**
	 * Performs the egressing airlock phase of the task.
	 * 
	 * @param time the amount of time (millisol) to perform the walking phase.
	 * @return the amount of time (millisol) left after performing the walking
	 *         phase.
	 */
	private double egressingAirlockPhase(double time) {
		double timeLeft = time;
		setDescription(Msg.getString("Task.description.walk.egressingAirlock")); //$NON-NLS-1$
		
		if (person != null) {
			
			logger.log(person, Level.FINER, 4_000, 
					"Calling egressingAirlockPhase().");
			
			// Check if person has reached the outside of the airlock.
			if (walkingSteps == null)
				return 0;
			
			WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
			Airlock airlock = step.airlock;

			if (person.isOutside()) {
				// the person is already outside
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
				// the person is still inside the settlement before 
				if (ExitAirlock.canExitAirlock(person, airlock)) {
					addSubTask(new ExitAirlock(person, airlock));
				} else {
					logger.log(person, Level.INFO, 4_000,
							"Unable to physically exit the airlock of " 
		      				+ airlock.getEntityName() + ".");
					endTask(); // will call Walk many times again
				}
			}

		} else if (robot != null) {
			// Note : robot is NOT allowed to leave the settlement
			endTask();
		}

		return timeLeft;
	}

	/**
	 * Performs the ingressing airlock phase of the task.
	 * 
	 * @param time the amount of time (millisol) to perform the walking phase.
	 * @return the amount of time (millisol) left after performing the walking
	 *         phase.
	 */
	private double ingressingAirlockPhase(double time) {
		double timeLeft = time;
		setDescription(Msg.getString("Task.description.walk.ingressingAirlock")); //$NON-NLS-1$
		
		logger.log(person, Level.FINER, 4_000, 
				"Calling ingressingAirlockPhase.");

		// Check if person has reached the inside of the airlock.
		WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
		Airlock airlock = step.airlock;
		if (person.isOutside()) {
			if (EnterAirlock.canEnterAirlock(person, airlock)) {
				logger.log(person, Level.FINER, 4_000,
						". Starting EnterAirlock.");
				addSubTask(new EnterAirlock(person, airlock));
			} else {
				logger.log(person, Level.FINER, 4_000, 
								"Ended the walk task. Could not enter the airlock in " 
	      						+ airlock.getEntityName() + ".");
				endTask();
			}

		} else { 
			// the person is inside the settlement
			if (walkingStepIndex < (walkingSteps.getWalkingStepsNumber() - 1)) {
				walkingStepIndex++;
				// setDescription("Walking outside to an airlock to enter");
				// setDescription("is INSIDE and still walking toward an airlock");
				setPhase(getWalkingStepPhase());
			} else {
				setDescription("Arrived at an airlock");
				endTask();
			}
		}

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

		setDescription(Msg.getString("Task.description.walk.exitingRoverInGarage")); //$NON-NLS-1$
		
		if (person != null) {
	
			logger.log(person, Level.FINER, 4000, 
					"About to exit the rover " + rover.getName() 
					+ ".");
			
			// Exit the rover parked inside a garage onto the settlement
			if (person.isInVehicleInGarage()) {
				
				if (person.transfer(garageBuilding.getSettlement())) {
					logger.log(person, Level.FINER, 4000, 
						"Just exited rover " + rover.getName() 
						+ " inside " + garageBuilding + ".");
					
					// Add the person onto the garage
					BuildingManager.addPersonOrRobotToBuilding(person, garageBuilding);

				}
				else {
					logger.log(person, Level.WARNING, 4000, 
						"Failed to exit rover " + rover.getName() 
						+ " inside " + garageBuilding + ".");
				}
			}
		}
		
		else if (robot != null) {

			logger.log(robot, Level.FINER, 4000,
					"About to exit rover " + rover.getName()
					+ ".");			

			// Exit the rover parked inside a garage onto the settlement
			if (robot.isInVehicleInGarage()) {
				
				if (robot.transfer(garageBuilding.getSettlement())) {
					logger.log(robot, Level.FINER, 4000, 
						"Just exited rover " + rover.getName() 
						+ " inside " + garageBuilding + ".");
					
					// Add the robot onto the garage
					BuildingManager.addPersonOrRobotToBuilding(robot, garageBuilding);
	
				}
				else {
					logger.log(robot, Level.WARNING, 4000, 
						"Failed to exit rover " + rover.getName() 
						+ " inside " + garageBuilding + ".");
				}
			}
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
		
		setDescription(Msg.getString("Task.description.walk.enteringRoverInsideGarage")); //$NON-NLS-1$
		
		if (person != null) {
			
			// Place this person within a vehicle inside a garage in a settlement
			if (person.transfer(rover)) {
				logger.log(person, Level.FINER, 4000, 
					"Just entered rover " + rover.getName() 
					+ " inside " + garageBuilding + ".");
			}
			else {
				logger.log(person, Level.WARNING, 4000, 
					"Failed to enter rover " + rover.getName() 
					+ " inside " + garageBuilding + ".");
			}
		} 
		
		else if (robot != null) {
			
			// Place this robot within a vehicle inside a garage in a settlement
			if (robot.transfer(rover)) {
				logger.log(robot, Level.FINER, 4000, 
					"Just entered rover " + rover.getName() 
					+ " inside " + garageBuilding + ".");
			}
			else {
				logger.log(robot, Level.WARNING, 4000, 
					"Failed to enter rover " + rover.getName() 
					+ " inside " + garageBuilding + ".");
			}
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
	 * Climbing up the ladder to the next level
	 * 
	 * @param time
	 * @return
	 */
	public double climbingUpLadder(double time) {

		double timeLeft = time;

		WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
	
		Building building = step.building;
		
		setDescription(Msg.getString("Task.description.walk.climbingUpLadder")); //$NON-NLS-1$
		
        if (building.isAHabOrHub()) {
    		if (person != null) {

    		} 
    		
    		else if (robot != null) {

    		}	
    		
    		if (walkingStepIndex < (walkingSteps.getWalkingStepsNumber() - 1)) {
    			walkingStepIndex++;
    			setPhase(getWalkingStepPhase());
    		} else {
    			endTask();
    		}

        }
        else {
			endTask();
		}

		return timeLeft;
	}

	/**
	 * Climbing down the ladder to the lower level
	 * 
	 * @param time
	 * @return
	 */
	public double climbingDownLadder(double time) {

		double timeLeft = time;

		WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
	
		Building building = step.building;
		
		setDescription(Msg.getString("Task.description.walk.climbingDownLadder")); //$NON-NLS-1$
		
        if (building.isAHabOrHub()) {
    		if (person != null) {

    		} 
    		
    		else if (robot != null) {

    		}	
    		
    		if (walkingStepIndex < (walkingSteps.getWalkingStepsNumber() - 1)) {
    			walkingStepIndex++;
    			setPhase(getWalkingStepPhase());
    		} else {
    			endTask();
    		}

        }
        else {
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

	/**
	 * Reloads instances after loading from a saved sim
	 * 
	 * @param mgr
	 */
	public static void initializeInstances(UnitManager mgr) {
		unitManager = mgr;
	}
	
	/**
	 * Does a change of Phase for this Task generate an entry in the Task Schedule 
	 * @return false
	 */
	@Override
	protected boolean canRecord() {
		return false;
	}
	
	public void destroy() {
		walkingSteps = null;
//		walkingStepPhaseMap.clear();
		walkingStepPhaseMap = null;
		unitManager = null;
	}

}

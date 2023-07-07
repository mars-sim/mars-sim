/*
 * Mars Simulation Project
 * Walk.java
 * @date 2021-10-21
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.mars.sim.mapdata.location.LocalBoundedObject;
import org.mars.sim.mapdata.location.LocalPosition;
import org.mars.sim.tools.Msg;
import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.data.UnitSet;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.WalkingSteps.WalkStep;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.structure.Airlock.AirlockMode;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.time.MarsTime;
import org.mars_sim.msp.core.vehicle.Airlockable;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * A general walking task that includes interior/exterior walking and
 * entering/exiting airlocks.
 */
public class Walk extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default serial id. */
	private static SimLogger logger = SimLogger.getLogger(Walk.class.getName());

	// Static members
	private static final double MIN_PULSE_TIME = 0.25;
	static final double PERSON_WALKING_SPEED = 1D; // [kph].
	static final double ROBOT_WALKING_SPEED = 0.25; // [kph].
	static final double PERSON_WALKING_SPEED_PER_MILLISOL = PERSON_WALKING_SPEED * MarsTime.MILLISOLS_PER_HOUR; // [km per millisol].
	static final double ROBOT_WALKING_SPEED_PER_MILLISOL = ROBOT_WALKING_SPEED * MarsTime.MILLISOLS_PER_HOUR; // [km per millisol].

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
	
	/** The minimum pulse time for completing a task phase in this class.  */
	private static double minPulseTime = Math.min(standardPulseTime, MIN_PULSE_TIME);

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

		unitManager = Simulation.instance().getUnitManager();

		LocalBoundedObject targetObject = null;
		if (person.isInSettlement()) {
			// Walk to random inhabitable building at settlement.
			List<Building> buildingList = person.getSettlement().getBuildingManager().getBuildings(FunctionType.LIFE_SUPPORT);
			if (!buildingList.isEmpty()) {
				int buildingIndex = RandomUtil.getRandomInt(buildingList.size() - 1);
				targetObject  = buildingList.get(buildingIndex);
			}

		} else if (person.isInVehicle()) {

			Vehicle vehicle = person.getVehicle();

			// If no mission and vehicle is at a settlement location, enter settlement.
			boolean walkToSettlement = false;
			if ((person.getMind().getMission() == null) && (vehicle.getSettlement() != null)) {

				Settlement settlement = vehicle.getSettlement();

				// Check if vehicle is in garage.
				Building garageBuilding = vehicle.getGarage();
				if (garageBuilding != null) {
					targetObject = garageBuilding;
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
						Airlock airlock = settlement.getClosestAvailableAirlock(person, true);
						if (airlock != null) {
							targetObject = (LocalBoundedObject) airlock.getEntity();
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
					targetObject = person.getVehicle();
				}
			}
		}

		// Determine if person is outside.
		else if (person.isOutside()) {

			Airlock airlock = findEmergencyAirlock(person);
			if (airlock != null) {
				targetObject = (LocalBoundedObject) airlock.getEntity();
			}
		}

		if (targetObject == null) {
			logger.log(person, Level.SEVERE, 30_000,
      				"Walking steps could not be determined.");

			endTask();
			return;
		}

		LocalPosition targetPosition = LocalAreaUtil.getRandomLocalRelativePosition(targetObject);
		walkingSteps = new WalkingSteps(person, targetPosition, 0, targetObject);

		if (!canWalkAllSteps(person, walkingSteps)) {
			logger.log(person, Level.SEVERE, 30_000, "Could not walk all steps.");
			endTask();
			return;
		}

		setupPersonWalk();
	}

	
	/**
	 * Constructor for factory method with preprocessed walking steps
	 *
	 * @param person         the person performing the task.
	 * @param walkingSteps	 Precalculated and verified walking steps
	 */
	private Walk(Person person, WalkingSteps walkingSteps) {
		super("Walk", person, false, false, 0D, null, 100D);

		this.walkingSteps = walkingSteps;
		setupPersonWalk();
	}

	private void setupPersonWalk() {
		// Initialize data members.
		walkingStepIndex = 0;

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

	private Walk(Robot robot, WalkingSteps walkingSteps) {
		super("Walk", robot, false, false, 0D, null, 100D);

		// Initialize data members.
		walkingStepIndex = 0;

		this.walkingSteps = walkingSteps;

		// Initialize task phase.
		addPhase(WALKING_SETTLEMENT_INTERIOR);

		setPhase(getWalkingStepPhase());
	}

	/**
	 * This is a factory method to create a Walk task if there is a valid path.
	 *
	 * @param person Person doing the walking
	 * @param destPosition FInal destination within an interior object
	 * @param destZ Vertical destination
	 * @param destObject Destination
	 * @return
	 */
	public static Walk createWalkingTask(Person person, LocalPosition destPosition, double destZ, LocalBoundedObject destObject) {
		WalkingSteps walkingSteps = new WalkingSteps(person, destPosition, destZ, destObject);
		boolean canWalk = walkingSteps.canWalkAllSteps();

        // Check if all airlocks can be exited.
		// Q: Why does it have to check for all airlocks if the person may or may not exit airlock ?
		canWalk = canWalk && canExitAllAirlocks(person, walkingSteps);

		if (canWalk) {
			return new Walk(person, walkingSteps);
		}
		return null;
	}


	/**
	 * This is a factory method to create a Walk task if there is a valid path.
	 *
	 * @param robot Robot doing the walking
	 * @param destPosition FInal destination within an interior object
	 * @param destObject Destination
	 * @return
	 */
	public static Walk createWalkingTask(Robot robot, LocalPosition destPosition, LocalBoundedObject destObject) {
		WalkingSteps walkingSteps = new WalkingSteps(robot, destPosition, 0D, destObject);
		boolean canWalk = walkingSteps.canWalkAllSteps();

		if (canWalk) {
			return new Walk(robot, walkingSteps);
		}
		return null;
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
		// Q: Why does it have to check for all airlocks if the person may or may not exit airlock ?
		if (!canExitAllAirlocks(person, walkingSteps)) {
			result = false;
		}

		return result;
	}
	
	/**
	 * Populates the walking step phase map.
	 */
	private synchronized void populateWalkingStepPhaseMap() {
		walkingStepPhaseMap = new HashMap<>();
		walkingStepPhaseMap.put(WalkingSteps.WalkStep.ENTER_AIRLOCK, ENTERING_AIRLOCK);
		walkingStepPhaseMap.put(WalkingSteps.WalkStep.ENTER_GARAGE_ROVER, ENTERING_ROVER_GARAGE);
		walkingStepPhaseMap.put(WalkingSteps.WalkStep.EXIT_AIRLOCK, EXITING_AIRLOCK);
		walkingStepPhaseMap.put(WalkingSteps.WalkStep.EXIT_GARAGE_ROVER, EXITING_ROVER_GARAGE);
		walkingStepPhaseMap.put(WalkingSteps.WalkStep.EXTERIOR_WALK, WALKING_EXTERIOR);
		walkingStepPhaseMap.put(WalkingSteps.WalkStep.ROVER_INTERIOR_WALK, WALKING_ROVER_INTERIOR);
		walkingStepPhaseMap.put(WalkingSteps.WalkStep.SETTLEMENT_INTERIOR_WALK, WALKING_SETTLEMENT_INTERIOR);
	}

	/**
	 * Gets the walking step phase.
	 *
	 * @return walking step task phase.
	 */
	private TaskPhase getWalkingStepPhase() {

		TaskPhase result = null;
		// Create and populate walkingStepPhaseMap if necessary.
		if (walkingStepPhaseMap == null) {
			populateWalkingStepPhaseMap();
		}

		if (person != null) {
			if (walkingStepIndex < walkingSteps.getWalkingStepsNumber()) {
				WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
				result = walkingStepPhaseMap.get(step.stepType);
			}
			else {
				logger.log(person, Level.FINE, 0,
						"Invalid walking step index.");
			}

		}
		else {
			if (walkingStepIndex < walkingSteps.getWalkingStepsNumber()) {
				WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
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
		List<WalkingSteps.WalkStep> stepList = walkingSteps.getWalkingStepsList();
		if (stepList != null) {
			Iterator<WalkingSteps.WalkStep> i = stepList.iterator();
			while (i.hasNext()) {
				WalkingSteps.WalkStep step = i.next();
				if (step.stepType == WalkingSteps.WalkStep.EXIT_AIRLOCK) {
					Airlock airlock = step.airlock;
					if (!ExitAirlock.canExitAirlock(person, airlock)) {
						logger.log(person, Level.WARNING, 10_000L,
								"Could not exit " + airlock.getEntityName());
						return false;
					}
				}
			}
		}

		return true;
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
			Iterator<Worker> i = person.getMind().getMission().getMembers().iterator();
			while (i.hasNext() && (result == null)) {
				Worker member = i.next();
				if (member != person) {
					if (member.isInSettlement()) {
						result = member.getSettlement().getClosestAvailableAirlock(person, true);
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
					result = settlement.getClosestAvailableAirlock(person, true);
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



	
	/**
	 * Removes all airlock reservations
	 */
	public static void removeAllReservations(BuildingManager buildingManager) {
		List<Airlock> airlocks = getAllAirlocks(buildingManager);

		for (Airlock a: airlocks) {
			a.getReservationMap().clear();
		}
	}
	
	/**
	 * Gets a list of airlock of this settlement
	 *
	 * @return
	 */
	private static List<Airlock> getAllAirlocks(BuildingManager buildingManager) {
		return buildingManager.getBuildings(FunctionType.EVA).stream()
				.map(b -> b.getEVA().getAirlock())
				.collect(Collectors.toList());
	}

	/**
	 * Are any airlocks available for ingree or egress ?
	 * Used by DigLocalMeta
	 * 
	 * @param person
	 * @param ingress
	 * @return
	 */
	public static boolean anyAirlocksForIngressEgress(Person person, boolean ingress) {
		Set<Building> bldgs = person.getSettlement().getBuildingManager().getBuildingSet(FunctionType.EVA);

		Iterator<Building> i = bldgs.iterator();
		while (i.hasNext()) {
			Airlock airlock = i.next().getEVA().getAirlock();
			boolean chamberFull = airlock.areAll4ChambersFull();
			AirlockMode airlockMode = airlock.getAirlockMode();
			boolean isIngressMode = airlockMode == AirlockMode.INGRESS;
			boolean isEgressMode = airlockMode == AirlockMode.EGRESS;
			boolean notInUse = airlockMode == AirlockMode.NOT_IN_USE;

			if (!chamberFull
				&& (notInUse
					|| (ingress && isIngressMode)
					|| (!ingress && isEgressMode))) {
						return true;
			}
		}
		
		return false;
	}


	/**
	 * Checks for available airlocks.
	 * 
	 * @param buildingManager
	 */
	public static void checkAvailableAirlocks(BuildingManager buildingManager) {
		Set<Building> pressurizedBldgs = new UnitSet<>();
		Set<Building> depressurizedBldgs = new UnitSet<>();

		for(Building airlockBdg : buildingManager.getBuildingSet(FunctionType.EVA)) {
			Airlock airlock = airlockBdg.getEVA().getAirlock();
			if (airlock.isPressurized()	|| airlock.isPressurizing())
				pressurizedBldgs.add(airlockBdg);
			else if (airlock.isDepressurized() || airlock.isDepressurizing())
				depressurizedBldgs.add(airlockBdg);
		}

		if (!pressurizedBldgs.isEmpty()) {
			buildingManager.getSettlement().trackAirlocks(pressurizedBldgs, true);
		}

		if (!depressurizedBldgs.isEmpty()) {
			buildingManager.getSettlement().trackAirlocks(depressurizedBldgs, false);
		}
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
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
		
		double remainingTime = time - minPulseTime;

		if (person != null) {
			logger.log(person, Level.FINE, 4000, "Walking inside a settlement.");

			// Check if person has reached destination location.
			WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
			Building building = BuildingManager.getBuilding(person);
			if (step.building != null && step.building.equals(building) && step.loc.isClose(person.getPosition())) {
				if (walkingStepIndex < (walkingSteps.getWalkingStepsNumber() - 1)) {
					walkingStepIndex++;

					setPhase(getWalkingStepPhase());
				}
				else {
					endTask();
				}
			}
			else {
				if (building != null) {
					// Going from building to step.building
					// setDescription("Walking inside from " + building.getNickName() + " to " +
					// step.building.getNickName());
					if (step.building != null) {
						addSubTask(new WalkSettlementInterior(person, step.building, step.loc, 0));
					}
					else {
						logger.log(person, Level.SEVERE, 5_000,
			      				"Could not find a destination building to go.");
						endTask();
					}
				} else if (person.isOutside()) {
					logger.log(person, Level.SEVERE, 5_000, "Not in a building.");
					
					endTask();
				}
			}
		}
		else {
			logger.log(robot, Level.FINER, 4000, "Walking inside a settlement.");

			// Check if robot has reached destination location.
			WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
			Building building = BuildingManager.getBuilding(robot);
			if (step.building.equals(building) && step.loc.isClose(robot.getPosition())) {
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
			}
			else {
				if (building != null) {
					
					if (step.building != null) {
						addSubTask(new WalkSettlementInterior(robot, step.building, step.loc));
					}
					else {
						logger.log(robot, Level.SEVERE, 5_000,
			      				"Could not find a destination building to go.");
						endTask();
					}				
				} else if (robot.isOutside()) {
					logger.log(robot, Level.SEVERE, 5_000, "Not in a building.");
					
					endTask();
				}
			}
		}

		return remainingTime;
	}

	/**
	 * Performs the walking rover interior phase of the task.
	 *
	 * @param time the amount of time (millisol) to perform the walking phase.
	 * @return the amount of time (millisol) left after performing the walking
	 *         phase.
	 */
	private double walkingRoverInteriorPhase(double time) {
		
		double remainingTime = time - minPulseTime;
		
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
				if (!LocalAreaUtil.isPositionWithinLocalBoundedObject(step.loc, rover)) {
					// Determine new destination location within rover.
					LocalPosition relativeRoverLoc = LocalAreaUtil.getRandomLocalRelativePosition(rover);
					step.loc = relativeRoverLoc;
				}
			}


			if (step.rover.equals(rover) && step.loc.isClose(person.getPosition())) {
				if (walkingStepIndex < (walkingSteps.getWalkingStepsNumber() - 1)) {
					walkingStepIndex++;
					// setDescription("Walking back to the rover at (" + x + ", " + y + ")");
					setPhase(getWalkingStepPhase());
				} else {
					endTask();
				}
			} else { // this is a high traffic case when a person is in a vehicle

				if (person.isInSettlement()) {
					logger.log(person, Level.SEVERE, 5_000,
						"Was supposed to be in a rover.");
					endTask();
				}

				if (person.isInVehicle() || person.isInVehicleInGarage()) {
					logger.log(person, Level.FINE , 5_000,
						"Starting WalkRoverInterior.");
					addSubTask(new WalkRoverInterior(person, step.rover, step.loc));
				}

				else if (person.isOutside()) {
					logger.log(person, Level.SEVERE, 5_000,
						"Outside calling walkingRoverInteriorPhase() and NOT in a rover.");
					endTask();
				}

			}

		} else {
			logger.log(robot, Level.SEVERE, 5_000,
					"Walking inside a rover.");

			// Check if robot has reached destination location.
			WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
			Rover rover = (Rover) robot.getVehicle();

			// Update rover destination if rover has moved and existing destination is no
			// longer within rover.
			if (!LocalAreaUtil.isPositionWithinLocalBoundedObject(step.loc, rover)) {
				// Determine new destination location within rover.
				// TODO: Determine location based on activity spot?
				LocalPosition relativeRoverLoc = LocalAreaUtil.getRandomLocalRelativePosition(rover);
				step.loc = relativeRoverLoc;
			}

			if (step.rover.equals(rover) && step.loc.isClose(robot.getPosition())) {
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
				addSubTask(new WalkRoverInterior(robot, step.rover, step.loc));
			}
		}

		return remainingTime;
	}

	/**
	 * Performs the walking exterior phase of the task.
	 *
	 * @param time the amount of time (millisol) to perform the walking phase.
	 * @return the amount of time (millisol) left after performing the walking
	 *         phase.
	 */
	private double walkingExteriorPhase(double time) {
		
		double remainingTime = time - minPulseTime;
		
		setDescription(Msg.getString("Task.description.walk")); //$NON-NLS-1$

		if (person != null) {
			logger.log(person, Level.FINER, 4000,
					"Calling walkingExteriorPhase().");

			// Check if person has reached destination location.
			WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
			if (step.loc.isClose(person.getPosition())) {
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
					setDescription("Walking outside toward " + step.loc.getShortFormat());
//					logger.info(person, "Walking outside from (" + x + ", " + y + ") to ("
//							+ xx + ", " + yy + ")");
					addSubTask(new WalkOutside(person, person.getPosition(), step.loc, true));
				}
				else {
					logger.log(person, Level.SEVERE, 5_000,
							"Not being outside.");
					endTask();
				}
			}
		}

		else {

			logger.log(robot, Level.SEVERE, 5_000,
					"Calling walkingExteriorPhase().");

			// Check if robot has reached destination location.
			WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
			if (step.loc.isClose(robot.getPosition())) {
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
					addSubTask(new WalkOutside(robot, robot.getPosition(), step.loc, true));
				}
				else {
					logger.log(robot, Level.SEVERE, 5_000,
							"Already physically outside.");
					endTask();
				}
			}
		}

		return remainingTime;
	}

	/**
	 * Performs the egressing airlock phase of the task.
	 *
	 * @param time the amount of time (millisol) to perform the walking phase.
	 * @return the amount of time (millisol) left after performing the walking
	 *         phase.
	 */
	private double egressingAirlockPhase(double time) {
		
		double remainingTime = time - minPulseTime;
		
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

		} else {
			// Note : robot is NOT allowed to leave the settlement
			endTask();
		}

		return remainingTime;
	}

	/**
	 * Performs the ingressing airlock phase of the task.
	 *
	 * @param time the amount of time (millisol) to perform the walking phase.
	 * @return the amount of time (millisol) left after performing the walking
	 *         phase.
	 */
	private double ingressingAirlockPhase(double time) {
		
		double remainingTime = time - minPulseTime;

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
				// Consume all of the time waiting to enter; prevents repeated tries
				remainingTime = 0D;
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

		return remainingTime;
	}

	/**
	 * Performs the exiting rover in garage phase of the task.
	 *
	 * @param time the amount of time (millisol) to perform the walking phase.
	 * @return the amount of time (millisol) left after performing the walking
	 *         phase.
	 */
	private double exitingRoverGaragePhase(double time) {

		double remainingTime = time - minPulseTime;
		
		boolean canExit = false;

		WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
		Rover rover = step.rover;
		Building garageBuilding = step.building;

		setDescription(Msg.getString("Task.description.walk.exitingRoverInGarage")); //$NON-NLS-1$

		if (person != null
			// Exit the rover parked inside a garage onto the settlement
			&& person.isInVehicleInGarage()
				&& person.transfer(garageBuilding)) {
					logger.log(person, Level.INFO, 4_000,
							"Exited rover " + rover.getName()
							+ " inside " + garageBuilding + ".");
					endTask();
					canExit = true;
		}

		else
			// Exit the rover parked inside a garage onto the settlement
			if (robot.isInVehicleInGarage()
				&& robot.transfer(garageBuilding)) {
					logger.log(robot, Level.INFO, 4_000,
							"Exited rover " + rover.getName()
							+ " inside " + garageBuilding + ".");
					endTask();
					canExit = true;
		}

		if (!canExit) {
			if (walkingStepIndex < (walkingSteps.getWalkingStepsNumber() - 1)) {
				walkingStepIndex++;
				setPhase(getWalkingStepPhase());
			} else {
				endTask();
			}
		}

		return remainingTime;
	}

	/**
	 * Performs the entering rover in garage phase of the task.
	 *
	 * @param time the amount of time (millisol) to perform the walking phase.
	 * @return the amount of time (millisol) left after performing the walking
	 *         phase.
	 */
	private double enteringRoverInsideGaragePhase(double time) {

		double remainingTime = time - minPulseTime;

		WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
		Rover rover = step.rover;
		Building garageBuilding = step.building;
		double distance = garageBuilding.getWidth() /2.0;
		double timeTraveled = 0;
		setDescription(Msg.getString("Task.description.walk.enteringRoverInsideGarage")); //$NON-NLS-1$

		if (person != null) {
			// Place this person within a vehicle inside a garage in a settlement
			if (person.transfer(rover)) {
				logger.log(person, Level.INFO, 4_000,
						"Entered rover " + rover.getName()
						+ " inside " + garageBuilding + ".");
				endTask();
				
				timeTraveled = distance / PERSON_WALKING_SPEED_PER_MILLISOL;
				remainingTime = time - timeTraveled;
				if (remainingTime < 0)
					remainingTime = 0;
				return remainingTime ;
			}
		}

		else {
			// Place this robot within a vehicle inside a garage in a settlement
			if (robot.transfer(rover)) {
				logger.log(robot, Level.INFO, 4_000,
						"Entered rover " + rover.getName()
						+ " inside " + garageBuilding + ".");
				endTask();
				
				timeTraveled = distance / ROBOT_WALKING_SPEED_PER_MILLISOL;
				remainingTime = time - timeTraveled;
				if (remainingTime < 0)
					remainingTime = 0;
				return remainingTime ;
			}
		}

		if (walkingStepIndex < (walkingSteps.getWalkingStepsNumber() - 1)) {
			walkingStepIndex++;
			setPhase(getWalkingStepPhase());
		} else {
			endTask();
		}

		return remainingTime;
	}

	/**
	 * Climbs up the ladder to the next level.
	 *
	 * @param time
	 * @return
	 */
	public double climbingUpLadder(double time) {

		double remainingTime = time - minPulseTime;

		WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);

		Building building = step.building;

		setDescription(Msg.getString("Task.description.walk.climbingUpLadder")); //$NON-NLS-1$

        if (building.isAHabOrHub()) {
    		if (person != null) {
    			// check if it's a hab or hub building since they are the only ones
    			// having a ladder to go up or down
    	   		if (walkingStepIndex < (walkingSteps.getWalkingStepsNumber() - 1)) {
        			walkingStepIndex++;
        			setPhase(getWalkingStepPhase());
        		} else {
        			endTask();
        		}
    		}
        }
        else {
			endTask();
		}

		return remainingTime;
	}

	/**
	 * Climbs down the ladder to the lower level.
	 *
	 * @param time
	 * @return
	 */
	public double climbingDownLadder(double time) {

		double remainingTime = time - minPulseTime;

		WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);

		Building building = step.building;

		setDescription(Msg.getString("Task.description.walk.climbingDownLadder")); //$NON-NLS-1$

        if (building.isAHabOrHub()) {
    		if (person != null) {
    			// check if it's a hab or hub building since they are the only ones
    			// having a ladder to go up or down
    	   		if (walkingStepIndex < (walkingSteps.getWalkingStepsNumber() - 1)) {
        			walkingStepIndex++;
        			setPhase(getWalkingStepPhase());
        		} else {
        			endTask();
        		}
    		}
        }
        else {
			endTask();
		}

		return remainingTime;
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
	 * Does a change of Phase for this Task generate an entry in the Task Schedule
	 * @return false
	 */
	@Override
	protected boolean canRecord() {
		return false;
	}
	
	public void destroy() {
		walkingSteps = null;
		walkingStepPhaseMap = null;
	}
}

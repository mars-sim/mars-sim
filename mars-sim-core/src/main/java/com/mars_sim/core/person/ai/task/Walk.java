/*
 * Mars Simulation Project
 * Walk.java
 * @date 2025-09-18
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.LocalBoundedObject;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.WalkingSteps.WalkStep;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Airlock;
import com.mars_sim.core.structure.Airlock.AirlockMode;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Airlockable;
import com.mars_sim.core.vehicle.Crewable;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * A general walking task that includes interior/exterior walking and
 * entering/exiting airlocks.
 */
public class Walk extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default serial id. */
	private static SimLogger logger = SimLogger.getLogger(Walk.class.getName());
	
	/** Simple Task name */
	public static final String SIMPLE_NAME = Walk.class.getSimpleName();

	/** Simple Task name */
	public static final String WALK = "Walk";
	
	// Static members
	static final double MIN_PULSE_TIME = 0.0129;
	// See https://en.wikipedia.org/wiki/Preferred_walking_speed
	static final double PERSON_WALKING_SPEED = 5.1; // [kph].
	static final double ROBOT_WALKING_SPEED = 2D; // [kph].
	static final double PERSON_WALKING_SPEED_PER_MILLISOL = PERSON_WALKING_SPEED * MarsTime.MILLISOLS_PER_HOUR; // [km per millisol].
	static final double ROBOT_WALKING_SPEED_PER_MILLISOL = ROBOT_WALKING_SPEED * MarsTime.MILLISOLS_PER_HOUR; // [km per millisol].

	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -.25D;
	/** Task name */
	private static final String NAME = Msg.getString("Task.description.walk"); //$NON-NLS-1$
	
	private static final String ARRIVED_AIRLOCK = Msg.getString("Task.description.walk.arrivedAirlock"); //$NON-NLS-1$

	private static final String EGRESSING_AIRLOCK = Msg.getString("Task.description.walk.egressingAirlock"); //$NON-NLS-1$
	private static final String INGRESSING_AIRLOCK = Msg.getString("Task.description.walk.ingressingAirlock"); //$NON-NLS-1$

	private static final String ENTERING_GARAGE = Msg.getString("Task.description.walk.enteringRoverInsideGarage"); //$NON-NLS-1$
	private static final String EXITING_GARAGE = Msg.getString("Task.description.walk.exitingRoverInGarage"); //$NON-NLS-1$
	
	private static final String WALKING_OUTSIDE = Msg.getString("Task.description.walk.outside"); //$NON-NLS-1$
	private static final String WALKING_IN_ROVER = Msg.getString("Task.description.walk.rover"); //$NON-NLS-1$
	
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
//	private static double minPulseTime = 0; //Math.min(standardPulseTime, MIN_PULSE_TIME);

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

		LocalBoundedObject targetObject = null;
		if (person.isInSettlement()) {
			// Walk to random inhabitable building at settlement.
			List<Building> buildingList = person.getSettlement().getBuildingManager()
					.getBuildings(FunctionType.LIFE_SUPPORT)
					.stream()
					.filter(b -> b != person.getBuildingLocation())
					.toList();
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
					// Note: if the vehicle is already garaged, what difference does it make 
					// to the occupants of this vehicle when it comes to 
					// walking out of the vehicle into the garage ?
					targetObject = garageBuilding;
					
					((Crewable)vehicle).getCrew().remove(person);
					
					walkToSettlement = true;
				}

				else {			
					// Question: is this the right place to direct a person to walk
				    // out of a garaged vehicle 
				
					// Check if person has a good EVA suit available if in a rover.
					boolean goodEVASuit = true;
					boolean roverSuit = vehicle.containsEquipment(EquipmentType.EVA_SUIT);
					boolean wearingSuit = (person.getSuit() != null);
					goodEVASuit = roverSuit || wearingSuit;

					if (goodEVASuit) {
						// Walk to nearest emergency airlock in settlement.
						Airlock airlock = settlement.getClosestIngressAirlock(person);
						if (airlock != null) {
							targetObject = (LocalBoundedObject) airlock.getEntity();
							walkToSettlement = true;
						}
					}
				}
			}

			if (!walkToSettlement) {
				// Walk to random location within rover.
				if (vehicle instanceof Rover r) {
					targetObject = r;
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

		LocalPosition targetPosition = LocalAreaUtil.getRandomLocalPos(targetObject);
		walkingSteps = new WalkingSteps(person, targetPosition, targetObject);

		if (!canWalkAllSteps(person, walkingSteps)) {
			logger.log(person, Level.SEVERE, 30_000, "Could not walk all steps.");
			endTask();
			return;
		}

		setupWalk();
	}

	
	/**
	 * Constructor for factory method with preprocessed walking steps
	 *
	 * @param worker         the worker performing the task.
	 * @param walkingSteps	 Precalculated and verified walking steps
	 */
	public Walk(Worker worker, WalkingSteps walkingSteps) {
		super(WALK, worker, false, false, 0D, null, 100D);

		this.walkingSteps = walkingSteps;
		
		setupWalk();
	}

	private void setupWalk() {
		// Initialize data members.
		walkingStepIndex = 0;
		// Initialize task phase.
		setPhase(getWalkingStepPhase());
	}

	/**
	 * This is a factory method to create a Walk task if there is a valid path.
	 *
	 * @param worker worker doing the walking
	 * @param destPosition Final destination within an interior object
	 * @param destZ Vertical destination
	 * @param destObject Destination
	 * @param needEVA
	 * @return
	 */
	public static Walk createWalkingTask(Worker worker, LocalPosition destPosition, 
			LocalBoundedObject destObject, boolean needEVA) {
		WalkingSteps walkingSteps = new WalkingSteps(worker, destPosition, destObject);
		boolean canWalk = walkingSteps.canWalkAllSteps();

		if (canWalk) {
			if (needEVA) {
		        // Check if all airlocks can be exited
				canWalk = canExitAllAirlocks(worker, walkingSteps);
			}
			// Q: Why does it have to check for all airlocks if the person may or may not exit airlock ?
			// A: Only if walkingSteps include WalkStep.EXIT_AIRLOCK
		}
		else {
			return null;
		}
		
		if (canWalk) {
			return new Walk(worker, walkingSteps);
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
	public static boolean canWalkAllSteps(Person person, WalkingSteps walkingSteps) {
		// Note: should add boolean needEVA param to avoid airlock checks
		
		// Check if all steps can be walked.
		boolean canWalk = walkingSteps.canWalkAllSteps();
		if (!canWalk)
			return false;
		
        // Check if all airlocks can be exited.
		// Q: Why does it have to check for all airlocks if the person may or may not exit airlock ?
		// A: Only if walkingSteps include WalkStep.EXIT_AIRLOCK
		return canExitAllAirlocks(person, walkingSteps);
	}
	
//	/**
//	 * This is a factory method to create a Walk task if there is a valid path.
//	 *
//	 * @param person Person doing the walking
//	 * @param destPosition FInal destination within an interior object
//	 * @param destZ Vertical destination
//	 * @param destObject Destination
//	 * @return
//	 */
//	public static boolean canWalk(Person person, LocalPosition destPosition, LocalBoundedObject destObject) {
//		WalkingSteps walkingSteps = new WalkingSteps(person, destPosition, destObject);
//		return canWalkAllSteps(person, walkingSteps);
//	}
//	
//	/**
//	 * This is a factory method to create a Walk task if there is a valid path.
//	 *
//	 * @param robot Robot doing the walking
//	 * @param destPosition FInal destination within an interior object
//	 * @param destZ Vertical destination
//	 * @param destObject Destination
//	 * @return
//	 */
//	public static boolean canWalk(Robot robot, LocalPosition destPosition, LocalBoundedObject destObject) {
//		WalkingSteps walkingSteps = new WalkingSteps(robot, destPosition, destObject);
//		return canWalkAllSteps(robot, walkingSteps);
//	}
	
	/**
	 * Check if robot can walk to a local destination.
	 *
	 * @param robot       the robot.
	 * @param walkingSteps the walking steps.
	 * @return true if a person can walk all the steps to the destination.
	 */
	public static boolean canWalkAllSteps(Robot robot, WalkingSteps walkingSteps) {
		// Check if all steps can be walked.
		return walkingSteps.canWalkAllSteps();
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

		if (walkingStepIndex < walkingSteps.getWalkingStepsNumber()) {
			WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
			result = walkingStepPhaseMap.get(step.stepType);
		}
		else {
			logger.log(worker, Level.FINE, 0,
					"Invalid walking step index.");
		}

		return result;
	}

	/**
	 * Checks if a worker can exit all airlocks in walking steps.
	 *
	 * @return true is all airlocks can be exited (or no airlocks in walk steps).
	 */
	private static boolean canExitAllAirlocks(Worker worker, WalkingSteps walkingSteps) {
		List<WalkingSteps.WalkStep> stepList = walkingSteps.getWalkingStepsList();
		if (stepList != null) {
			Iterator<WalkingSteps.WalkStep> i = stepList.iterator();
			while (i.hasNext()) {
				WalkingSteps.WalkStep step = i.next();
				if (step.stepType == WalkingSteps.WalkStep.EXIT_AIRLOCK) {
					Airlock airlock = step.airlock;
					
					// Question: can it be more than one airlock ?
					// Answer: it may involve a vehicle airlock and a building airlock
					
					// But is it realistic to check beyond the immediate airlock
					// such as the faraway (second airlock)	?	 
					
					if (ExitAirlock.isFull(airlock)) {
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
						result = member.getSettlement().getClosestIngressAirlock(person);
					} else if (member.isInVehicle()) {
						Vehicle vehicle = member.getVehicle();
						if (vehicle instanceof Airlockable v) {
							result = v.getAirlock();
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
					result = settlement.getClosestIngressAirlock(person);
				}
			}
		}

		// If not look for any vehicles with airlocks at person's location.
		if (result == null) {
			Iterator<Vehicle> i = unitManager.getVehicles().iterator();
			while (i.hasNext() && (result == null)) {
				Vehicle vehicle = i.next();
				if (person.getCoordinates().equals(vehicle.getCoordinates())) {
					if (vehicle instanceof Airlockable v) {
						result = v.getAirlock();
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
	 * Gets a list of airlock of this settlement.
	 *
	 * @return
	 */
	private static List<Airlock> getAllAirlocks(BuildingManager buildingManager) {
		return buildingManager.getBuildings(FunctionType.EVA).stream()
				.map(b -> b.getEVA().getAirlock())
				.collect(Collectors.toList());
	}

	/**
	 * Are any airlocks available for ingress or egress ?
	 * 
	 * @param person
	 * @param ingress
	 * @return
	 */
	public static boolean anyAirlocksForIngressEgress(Person person, boolean ingress) {
		Set<Building> bldgs = person.getSettlement().getBuildingManager().getAirlocks();

		Iterator<Building> i = bldgs.iterator();
		while (i.hasNext()) {
			Airlock airlock = i.next().getEVA().getAirlock();
			boolean chamberFull = airlock.isFull();
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

		logger.log(worker, Level.FINE, 4000, "Walking inside a settlement.");

		// Check if person has reached destination location.
		WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
		Building building = BuildingManager.getBuilding(worker);
		if (step.building != null && step.building.equals(building) && step.loc.isClose(worker.getPosition())) {
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
				if (step.building != null) {

					boolean canAdd = addSubTask(new WalkSettlementInterior(worker, step.building, step.loc));
					if (!canAdd) {
						logger.log(worker, Level.WARNING, 4_000,
								". Unable to add subtask WalkSettlementInterior.");
						// Note: may call below many times
						endTask();
					}
				}
				else {
					logger.log(worker, Level.SEVERE, 5_000,
		      				"Could not find a destination building to go.");
					endTask();
				}
			} else if (worker.isOutside()) {
				logger.log(worker, Level.SEVERE, 5_000, "Not in a building.");
				
				endTask();
			}
		}

		return 0;
	}

	/**
	 * Performs the walking rover interior phase of the task.
	 *
	 * @param time the amount of time (millisol) to perform the walking phase.
	 * @return the amount of time (millisol) left after performing the walking
	 *         phase.
	 */
	private double walkingRoverInteriorPhase(double time) {
		
		setDescription(WALKING_IN_ROVER);

		logger.log(worker, Level.FINE, 5_000,
				"Walking inside a rover.");

		// Check if worker has reached destination location.
		WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
		Rover rover = (Rover) worker.getVehicle();

		// TODO: working on resolving NullPointerException
		if (rover != null) {
			// Update rover destination if rover has moved and existing destination is no
			// longer within rover.
			if (!LocalAreaUtil.isPositionWithinLocalBoundedObject(step.loc, rover)) {
				// Determine new destination location within rover.
				LocalPosition relativeRoverLoc = LocalAreaUtil.getRandomLocalPos(rover);
				step.loc = relativeRoverLoc;
			}
		}


		if (step.rover.equals(rover) && step.loc.isClose(worker.getPosition())) {
			if (walkingStepIndex < (walkingSteps.getWalkingStepsNumber() - 1)) {
				walkingStepIndex++;
				// setDescription("Walking back to the rover at (" + x + ", " + y + ")");
				setPhase(getWalkingStepPhase());
			} else {
				endTask();
			}
		} else { // this is a high traffic case when a worker is in a vehicle

			if (worker.isInSettlement()) {
				logger.log(worker, Level.SEVERE, 5_000,
					"Was supposed to be in a rover.");
				endTask();
			}

			if (worker.isInVehicle() || worker.isInVehicleInGarage()) {
				logger.log(worker, Level.FINE , 5_000,
					"Starting WalkRoverInterior.");
				
				Task currentTask = worker.getTaskManager().getTask();
        		Task subTask = worker.getTaskManager().getTask().getSubTask();
        		if ((currentTask != null && !currentTask.getName().equalsIgnoreCase(WalkRoverInterior.NAME))
        			|| (subTask != null && !subTask.getName().equalsIgnoreCase(WalkRoverInterior.NAME))) {	
				
					boolean canAdd = addSubTask(new WalkRoverInterior(worker, step.rover, step.loc));
					if (!canAdd) {
						logger.log(worker, Level.WARNING, 4_000,
								". Unable to add subtask WalkRoverInterior.");
						// Note: may call below many times
						endTask();
					}
        		}
			}

			else if (worker.isOutside()) {
				logger.log(worker, Level.SEVERE, 5_000,
					"Outside calling walkingRoverInteriorPhase() and NOT in a rover.");
				endTask();
			}

		}

		return 0;
	}

	/**
	 * Performs the walking exterior phase of the task.
	 *
	 * @param time the amount of time (millisol) to perform the walking phase.
	 * @return the amount of time (millisol) left after performing the walking
	 *         phase.
	 */
	private double walkingExteriorPhase(double time) {
	
		setDescription(WALKING_OUTSIDE);

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
					endTask();
				}
			}
			else {
				if (person.isOutside()) {
					setDescription(WALKING_OUTSIDE + " toward " + step.loc.getShortFormat());

	        		// Note that addSubTask() will internally check if the task is a duplicate
					boolean canAdd = addSubTask(new WalkOutside(person, person.getPosition(), step.loc, true));
					if (!canAdd) {
						logger.log(person, Level.WARNING, 4_000,
								". Unable to add subtask WalkOutside.");
						// Note: may call below many times
						endTask();
					}
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

					setPhase(getWalkingStepPhase());
				}
				else {
					endTask();
				}
			}
			else {
				if (robot.isOutside()) {

					logger.log(robot, Level.FINER, 4_000,
							"Outside. Starting WalkOutside.");
					boolean canUse = addSubTask(new WalkOutside(robot, robot.getPosition(), step.loc, true));
					if (!canUse) {
						logger.log(robot, Level.WARNING, 4_000,
								". Unable to add subtask WalkOutside.");
						// Note: may call below many times
						endTask();
					}
				}
				else {
					logger.log(robot, Level.SEVERE, 5_000,
							"Already physically outside.");
					endTask();
				}
			}
		}

		return 0;
	}

	/**
	 * Performs the egressing airlock phase of the task.
	 *
	 * @param time the amount of time (millisol) to perform the walking phase.
	 * @return the amount of time (millisol) left after performing the walking
	 *         phase.
	 */
	private double egressingAirlockPhase(double time) {
		
		double remainingTime = time *.75;
		
		setDescription(EGRESSING_AIRLOCK);

		if (person != null) {

			logger.log(person, Level.FINER, 4_000,
					"Calling egressingAirlockPhase().");

			// Check if person has reached the outside of the airlock.
			if (walkingSteps == null)
				return 0;

			WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
			// Future: if this airlock is not available (e.g. chambers are full), how to look for another airlock ?
			Airlock airlock = step.airlock;

			if (person.isOutside()) {
				// the person is outside
				if (walkingStepIndex < (walkingSteps.getWalkingStepsNumber() - 1)) {
					walkingStepIndex++;
					setPhase(getWalkingStepPhase());
				} 
				else {
					setDescription(ARRIVED_AIRLOCK);
					// Consume all of the time waiting to enter; prevents repeated tries
					remainingTime = 0D;
					
					endTask();
				}
			} else {
				// the person is still inside the settlement before
				if (ExitAirlock.canExitAirlock(person, airlock)) {
					logger.log(person, Level.FINER, 4_000,
							". Adding subtask ExitAirlock.");
					// Check reservation
					if (airlock.hasReservation(person.getIdentifier()) 
							|| (!airlock.isReservationFull() 
								&& airlock.addReservation(person.getIdentifier()))) {
					
						boolean canAdd = addSubTask(new ExitAirlock(person, airlock));
						if (!canAdd) {
							logger.log(person, Level.WARNING, 4_000,
									". Unable to add subtask ExitAirlock.");
							// Consume all of the time waiting to enter; prevents repeated tries
							remainingTime = 0D;
							// Note: may call below many times
							endTask();
						}
					}
					
				} else {
					logger.log(person, Level.INFO, 4_000,
							"Unable to physically exit the airlock of "
		      				+ airlock.getEntityName() + ".");
					// Consume all of the time waiting to enter; prevents repeated tries
					remainingTime = 0D;
					// Note: may call below many times
					endTask();
				}
			}

		} else {
			// Consume all of the time waiting to enter; prevents repeated tries
			remainingTime = 0D;
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
		
		double remainingTime = time *.75;

		setDescription(INGRESSING_AIRLOCK);

		logger.log(person, Level.FINER, 4_000,
				"Calling ingressingAirlockPhase.");

		// Check if person has reached the inside of the airlock.
		WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
		
		Airlock airlock = step.airlock;
		
		if (person.isOutside()) {
			// the person is outside
			if (EnterAirlock.canEnterAirlock(person, airlock)) {
				logger.log(person, Level.FINER, 4_000,
						". Adding subtask EnterAirlock.");
				// Check reservation
				if (airlock.hasReservation(person.getIdentifier()) 
						|| (!airlock.isReservationFull() 
							&& airlock.addReservation(person.getIdentifier()))) {
				
					boolean canAdd = addSubTask(new EnterAirlock(person, airlock));
					if (!canAdd) {
						logger.log(person, Level.WARNING, 4_000,
								". Unable to add subtask EnterAirlock.");
						// Note: may call below many times
						endTask();
					}
				}
			} else {
				logger.log(person, Level.FINER, 4_000,
								"Unable to physically enter the airlock of "
	      						+ airlock.getEntityName() + ".");
				// Consume all of the time waiting to enter; prevents repeated tries
				remainingTime = 0D;
				// Note: will call below many times
				endTask();
			}

		} else {
			// the person is inside the settlement
			if (walkingStepIndex < (walkingSteps.getWalkingStepsNumber() - 1)) {
				walkingStepIndex++;
				setPhase(getWalkingStepPhase());
			} 
			else {
				setDescription(ARRIVED_AIRLOCK);
				// Consume all of the time waiting to enter; prevents repeated tries
				remainingTime = 0D;
				
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
	
		boolean canExit = false;

		WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
		Rover rover = step.rover;
		Building garageBuilding = step.building;

		setDescription(EXITING_GARAGE);

		// WARNING: Transferring a person/robot/equipment from a vehicle into a settlement 
		// can be problematic if no building is assigned.
		// If exiting a vehicle in a garage, it's recommended using garageBuilding as a destination
		
		if (person != null
			// Exit the rover parked inside a garage onto the settlement
			&& person.isInVehicleInGarage()
			// Note: in transfer(), it will automatically switch the destination 
			//       from building back to settlement
				&& person.transfer(garageBuilding)) {
					logger.log(person, Level.INFO, 4_000,
							"Exited rover " + rover.getName()
							+ " inside " + garageBuilding + ".");
					endTask();
					canExit = true;
		}

		else if (robot != null 
			&& robot.isInVehicleInGarage()
			// Note: in transfer(), it will automatically switch the destination 
			//       from building back to settlement
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

		return 0;
	}

	/**
	 * Performs the entering rover in garage phase of the task.
	 *
	 * @param time the amount of time (millisol) to perform the walking phase.
	 * @return the amount of time (millisol) left after performing the walking
	 *         phase.
	 */
	private double enteringRoverInsideGaragePhase(double time) {

		double remainingTime = time;

		WalkingSteps.WalkStep step = walkingSteps.getWalkingStepsList().get(walkingStepIndex);
		Rover rover = step.rover;
		Building garageBuilding = step.building;
		double distance = garageBuilding.getWidth() / 2.0;
		double timeTraveled = 0;
		
		setDescription(ENTERING_GARAGE);

		if (person != null) {
			// Place this person within a vehicle inside a garage in a settlement
			if (person.transfer(rover)) {
				logger.log(person, Level.INFO, 4_000,
						"Entered rover " + rover.getName()
						+ " inside " + garageBuilding + ".");
				endTask();
				
				timeTraveled = distance / PERSON_WALKING_SPEED_PER_MILLISOL;
				remainingTime = remainingTime - timeTraveled;
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
				remainingTime = remainingTime - timeTraveled;
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

		return 0;
	}

	/**
	 * Climbs down the ladder to the lower level.
	 *
	 * @param time
	 * @return
	 */
	public double climbingDownLadder(double time) {

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

		return 0;
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
	 * Does a change of Phase for this Task generate an entry in the Task Schedule ?
	 * 
	 * @return false
	 */
	@Override
	protected boolean canRecord() {
		return false;
	}
	
	/**
	 * Prepares object for garbage collection.
	 */
	@Override
	public void destroy() {
		walkingSteps = null;
		walkingStepPhaseMap = null;
		
		super.destroy();
	}
}

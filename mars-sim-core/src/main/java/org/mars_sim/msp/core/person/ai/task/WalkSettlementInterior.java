/**
` * Mars Simulation Project
 * WalkSettlementInterior.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.Iterator;
import java.util.logging.Level;

import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalPosition;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.connection.BuildingConnector;
import org.mars_sim.msp.core.structure.building.connection.BuildingLocation;
import org.mars_sim.msp.core.structure.building.connection.Hatch;
import org.mars_sim.msp.core.structure.building.connection.InsideBuildingPath;
import org.mars_sim.msp.core.structure.building.connection.InsidePathLocation;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * A subtask for walking between two interior locations in a settlement. (Ex:
 * Between two connected inhabitable buildings or two locations in a single
 * inhabitable building.)
 */
public class WalkSettlementInterior extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(WalkSettlementInterior.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.walkSettlementInterior"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase WALKING = new TaskPhase(Msg.getString("Task.phase.walking")); //$NON-NLS-1$

	// Static members
//	private static final double PERSON_WALKING_SPEED = Walk.PERSON_WALKING_SPEED; // [km per hr].
//	private static final double ROBOT_WALKING_SPEED = Walk.ROBOT_WALKING_SPEED; // [km per hr].

	private static final double VERY_SMALL_DISTANCE = .00001D;
	private static final double STRESS_MODIFIER = -.2D;

	// Data members
	private LocalPosition destPosition;
	private double destZLoc;
	
	private Settlement settlement;
	private Building destBuilding;
	private InsideBuildingPath walkingPath;

	/**
	 * Constructor for the person
	 * 
	 * @param person               the person performing the task.
	 * @param destinationBuilding  the building that is walked to. (Can be same as
	 *                             current building).
	 * @param destinationPosition the destination position at the settlement.
	 * @param destinationYLocation the destination Z location at the settlement.
	 */
	public WalkSettlementInterior(Person person, Building destinationBuilding, LocalPosition destinationPosition,
								  double destinationZLocation) {
		super(NAME, person, false, false, STRESS_MODIFIER, null, 100D);

		// Check that the person is currently inside the settlement.
		if (!person.isInSettlement()) {
			logger.warning(person, "Started WalkSettlementInterior task when not in a settlement.");
			person.getMind().getTaskManager().clearAllTasks("Not in a settlement");
			return;
		}

		// Initialize data members.
		this.settlement = person.getSettlement();
		this.destBuilding = destinationBuilding;
		this.destPosition = destinationPosition;
		this.destZLoc = destinationZLocation;
		
		// Check if (destXLoc, destYLoc) is within destination building.
		if (!LocalAreaUtil.isPositionWithinLocalBoundedObject(destPosition, destBuilding)) {
			logger.warning(person, "Was unable to walk to the destination in " + destBuilding);
			person.getMind().getTaskManager().clearAllTasks("Destination not reachable");
			return;
		}

		// Check that the person is currently inside a building.
		Building startBuilding = BuildingManager.getBuilding(person);
		if (startBuilding == null) {
			logger.warning(person, "Was not currently in a building.");
			person.getMind().getTaskManager().clearAllTasks("Not in start building");
			return;
		}

		try {
			// Determine the walking path to the destination.
			if (settlement != null)
				walkingPath = settlement.getBuildingConnectorManager().determineShortestPath(startBuilding,
					person.getPosition(), destinationBuilding, destPosition);
	
			// If no valid walking path is found, end task.
			if (walkingPath == null) {
				logger.warning(person, "Unable to walk. No valid interior path.");
				person.getMind().getTaskManager().clearAllTasks("No walking routes.");
				return;
				// TODO: if it's the astronomy observatory building, it will call it thousands of time
				// e.g (Warning) [x23507] WalkSettlementInterior : Jani Patokallio unable to walk from Lander Hab 2 to Astronomy Observatory 1.  Unable to find valid interior path.
	//			person.getMind().getTaskManager().getNewTask();
			}
			
			logger.log(person, Level.FINER, 20_000, "Proceeded to the walking phase in WalkSettlementInterior.");
			
			// Initialize task phase.
			addPhase(WALKING);
			setPhase(WALKING);
		
		} catch (StackOverflowError ex) {
			logger.severe(person, "Was unable to walk. No valid interior path.", ex);
			person.getMind().getTaskManager().clearAllTasks("Can not get path");
		}
	}

	/**
	 * Constructor for robot
	 * 
	 * @param robot
	 * @param destinationBuilding
	 * @param destinationXLocation
	 * @param destinationYLocation
	 */
	public WalkSettlementInterior(Robot robot, Building destinationBuilding, LocalPosition destinationPosition) {
		super("Walking Settlement Interior", robot, false, false, STRESS_MODIFIER, null, 100D);

		// Check that the robot is currently inside the settlement.
		if (!robot.isInSettlement()) {
			throw new IllegalStateException("WalkSettlementInterior task started when robot is not in settlement.");
		}

		// Initialize data members.
		this.settlement = robot.getSettlement();
		this.destBuilding = destinationBuilding;
		this.destPosition = destinationPosition;
		
		// Check that destination location is within destination building.
		if (!LocalAreaUtil.isPositionWithinLocalBoundedObject(destPosition, destBuilding)) {
			logger.warning(robot, "Was unable to walk to the destination in " + robot.getBuildingLocation());
			// "Given destination walking location not within destination building.");
			endTask();
			return;
		}

		// Check that the robot is currently inside a building.
		Building startBuilding = BuildingManager.getBuilding(robot);
		if (startBuilding == null) {
			// logger.severe(robot.getName() + " is not currently in a building.");
			// endTask();
			return;
		}

		// Determine the walking path to the destination.
		walkingPath = settlement.getBuildingConnectorManager().determineShortestPath(startBuilding,
				robot.getPosition(), destinationBuilding, destPosition);

		// If no valid walking path is found, end task.
		if (walkingPath == null) {
			logger.warning(robot, "Was unable to walk from " + startBuilding.getNickName() + " to "
									+ destinationBuilding.getNickName() + ". No valid interior path.");
			endTask();
			return;
		}

		// Initialize task phase.
		addPhase(WALKING);
		setPhase(WALKING);
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {

			logger.severe(worker, "Task phase is null");
			throw new IllegalArgumentException("Task phase is null");
		}
		if (WALKING.equals(getPhase())) {
			return walkingPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the walking phase of the task.
	 * 
	 * @param time the amount of time (millis)ol) to perform the walking phase.
	 * @return the amount of time (millisol) left after performing the walking
	 *         phase.
	 */
	private double walkingPhase(double time) {
		double timeHours = MarsClock.HOURS_PER_MILLISOL * time;
		double speed = 0;
		
		if (person != null) {
			speed = person.calculateWalkSpeed();

		}
		else if (robot != null) {
			speed = robot.calculateWalkSpeed();
		}
		else {
			throw new IllegalStateException("Do not know who is walking");
		}
		
		// Check that remaining path locations are valid.
		if (!checkRemainingPathLocations()) {
			// Flooding with the following statement in stacktrace
			logger.severe(worker, "Unable to continue walking due to missing path objects.");
			endTask();
			return 0;
		}
		
		// Determine walking distance.
		double coveredKm = speed * timeHours;
		double coveredMeters = coveredKm * 1000D;
		double remainingPathDistance = getRemainingPathDistance();

		// Determine time left after walking.
		double timeLeft = 0D;
		if (coveredMeters > remainingPathDistance) {
			coveredMeters = remainingPathDistance;

			timeLeft = time - MarsClock.convertSecondsToMillisols((coveredMeters / 1000D) / speed * 60D * 60D);	
		}

		while (coveredMeters > VERY_SMALL_DISTANCE) {
			// Walk to next path location.
			InsidePathLocation location = walkingPath.getNextPathLocation();
			double distanceToLocation = worker.getPosition().getDistanceTo(location.getPosition());

			if (coveredMeters >= distanceToLocation) {

				// Set person at next path location, changing buildings if necessary.
				worker.setPosition(location.getPosition());

				coveredMeters -= distanceToLocation;
				
				if (!changeBuildings(location)) {
					logger.severe(worker, "Unable to change building.");
					if (worker.getUnitType() == UnitType.PERSON)
						((Person)worker).getMind().getTaskManager().clearAllTasks("Unable to change building");
					else
						((Robot)worker).getBotMind().getBotTaskManager().clearAllTasks("Unable to change building");
				}
				
				if (!walkingPath.isEndOfPath()) {
					walkingPath.iteratePathLocation();
				}
			}
			
			else {
				// Walk in direction of next path location.
				
				// Determine direction
				double direction = worker.getPosition().getDirectionTo(location.getPosition());
				
				// Determine person's new location at distance and direction.
				walkInDirection(direction, coveredMeters);

				// Set person at next path location, changing buildings if necessary.
				// TODO Is this right becausw the walk in direiiton also updates 
				worker.setPosition(location.getPosition());

				coveredMeters = 0D;
			}
		}

		// If path destination is reached, end task.
		if (getRemainingPathDistance() <= VERY_SMALL_DISTANCE) {
			InsidePathLocation location = walkingPath.getNextPathLocation();

			logger.log(worker, Level.FINEST, 0, "Close enough to final destination ("
					+ location.getPosition());
			
			worker.setPosition(location.getPosition());

			endTask();
		}
		
		return timeLeft;
	}

	/**
	 * Walk in a given direction for a given distance.
	 * 
	 * @param direction the direction (radians) of travel.
	 * @param distance  the distance (meters) to travel.
	 */
	void walkInDirection(double direction, double distance) {
		worker.setPosition(worker.getPosition().getPosition(distance, direction));
	}

	/**
	 * Check that the remaining path locations are valid.
	 * 
	 * @return true if remaining path locations are valid.
	 */
	private boolean checkRemainingPathLocations() {
		// Check all remaining path locations.
		Iterator<InsidePathLocation> i = walkingPath.getRemainingPathLocations().iterator();
		while (i.hasNext()) {
			InsidePathLocation loc = i.next();
			if (loc instanceof Building) {
				// Check that building still exists.
				Building building = (Building) loc;
				if (!settlement.getBuildingManager().containsBuilding(building)) {
					return false;
				}
			} else if (loc instanceof BuildingLocation) {
				// Check that building still exists.
				BuildingLocation buildingLoc = (BuildingLocation) loc;
				Building building = buildingLoc.getBuilding();
				if (!settlement.getBuildingManager().containsBuilding(building)) {
					return false;
				}
			} else if (loc instanceof BuildingConnector) {
				// Check that building connector still exists.
				BuildingConnector connector = (BuildingConnector) loc;
				if (!settlement.getBuildingConnectorManager().containsBuildingConnector(connector)) {
					return false;
				}
			} else if (loc instanceof Hatch) {
				// Check that building connector for hatch still exists.
				Hatch hatch = (Hatch) loc;
				BuildingConnector connector = hatch.getBuildingConnector();
				if (!settlement.getBuildingConnectorManager().containsBuildingConnector(connector)) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Gets the remaining path distance.
	 * 
	 * @return distance (meters).
	 */
	private double getRemainingPathDistance() {

		double result = 0D;
		LocalPosition prevPosition = worker.getPosition();

		Iterator<InsidePathLocation> i = walkingPath.getRemainingPathLocations().iterator();
		while (i.hasNext()) {
			InsidePathLocation nextLoc = i.next();
			result += nextLoc.getPosition().getDistanceTo(prevPosition);
			prevPosition = nextLoc.getPosition();
		}

		return result;
	}

	/**
	 * Changes the current building to a new one if necessary.
	 * 
	 * @param location the path location the person has reached.
	 */
	private boolean changeBuildings(InsidePathLocation location) {

		if (location instanceof Hatch) {
			// If hatch leads to new building, place person in the new building.
			Hatch hatch = (Hatch) location;

			if (person != null) {
				Building currentBuilding = BuildingManager.getBuilding(person);
				if (!hatch.getBuilding().equals(currentBuilding)) {
					BuildingManager.removePersonFromBuilding(person, currentBuilding);
					BuildingManager.addPersonOrRobotToBuilding(person, hatch.getBuilding());
				}
			} 
			
			else if (robot != null) {
				Building currentBuilding = BuildingManager.getBuilding(robot);
				if (!hatch.getBuilding().equals(currentBuilding)) {
					BuildingManager.removeRobotFromBuilding(robot, currentBuilding);
					BuildingManager.addPersonOrRobotToBuilding(robot, hatch.getBuilding());
				}
			}

		} else if (location instanceof BuildingConnector) {
			// If non-split building connector, place person in the new building.
			BuildingConnector connector = (BuildingConnector) location;
			if (!connector.isSplitConnection()) {
				Building currentBuilding = null;
				if (person != null) {
					currentBuilding = BuildingManager.getBuilding(person);
				} 
				
				else if (robot != null) {
					currentBuilding = BuildingManager.getBuilding(robot);
				}

				Building newBuilding = null;
				if (connector.getBuilding1().equals(currentBuilding)) {
					newBuilding = connector.getBuilding2();
				} 
				
				else if (connector.getBuilding2().equals(currentBuilding)) {
					newBuilding = connector.getBuilding1();
				} 
				
				else {
					logger.severe(worker, "Bad building connection (" 
							+ connector.getBuilding1() + " <--> " + connector.getBuilding2()
							+ ").");
					return false;
				}

				if (newBuilding != null) {
					
					if (person != null) {
						BuildingManager.removePersonFromBuilding(person, currentBuilding);
						BuildingManager.addPersonOrRobotToBuilding(person, newBuilding);
					}
					else if (robot != null) {
						BuildingManager.removeRobotFromBuilding(robot, currentBuilding);
						BuildingManager.addPersonOrRobotToBuilding(robot, newBuilding);
					}
				}
			}
		}
		
		return true;
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
}

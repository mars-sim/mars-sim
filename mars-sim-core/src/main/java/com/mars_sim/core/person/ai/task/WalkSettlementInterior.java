/*
`* Mars Simulation Project
 * WalkSettlementInterior.java
 * @date 2025-07-18
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task;

import java.util.Iterator;

import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.connection.BuildingConnector;
import com.mars_sim.core.building.connection.BuildingLocation;
import com.mars_sim.core.building.connection.Hatch;
import com.mars_sim.core.building.connection.InsideBuildingPath;
import com.mars_sim.core.building.connection.InsidePathLocation;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.Msg;

/**
 * A subtask for walking between two interior locations in a settlement. (Ex:
 * Between two connected inhabitable buildings or two locations in a single
 * inhabitable building.)
 */
public class WalkSettlementInterior extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(WalkSettlementInterior.class.getName());

	/** Simple Task name */
	public static final String SIMPLE_NAME = WalkSettlementInterior.class.getSimpleName();

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.walkSettlementInterior"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase WALKING = new TaskPhase(Msg.getString("Task.phase.walking")); //$NON-NLS-1$

	// Static members
	private static final double VERY_SMALL_DISTANCE = .01D;
	private static final double STRESS_MODIFIER = -.2D;
	/** The minimum pulse time for completing a task phase in this class. */
	private static double minPulseTime = 0;

	private LocalPosition destPosition;
	private Settlement settlement;
	private Building destBuilding;
	private InsideBuildingPath walkingPath;

	/**
	 * Constructor for the person
	 * 
	 * @param worker               the person performing the task.
	 * @param destinationBuilding  the building that is walked to. (Can be same as
	 *                             current building).
	 * @param destinationPosition  the destination position at the settlement.
	 */
	public WalkSettlementInterior(Worker worker, Building destinationBuilding, LocalPosition destinationPosition) {
		super(NAME, worker, false, false, STRESS_MODIFIER, null, 100D);

		// Check if the person is currently inside the settlement.
		if (!worker.isInSettlement()) {
			logger.warning(worker, "Not in a settlement.");
			worker.getTaskManager().clearAllTasks("Not in a settlement.");
			return;
		}

		// Initialize data members.
		this.settlement = worker.getSettlement();
		this.destBuilding = destinationBuilding;
		this.destPosition = destinationPosition;

		if (!LocalAreaUtil.isPositionWithinLocalBoundedObject(destPosition, destBuilding)) {
			logger.warning(worker, 10_000L, "Destination position " + destPosition + " is not inside " + destBuilding
					+ " @ " + LocalAreaUtil.getDescription(destinationBuilding));
			endTask();
			return;
		}

		// Check if the person is currently inside a building.
		Building startBuilding = BuildingManager.getBuilding(worker);
		if (startBuilding == null) {
			logger.warning(worker, 10_000L, "Not in a building.");
			worker.getTaskManager().clearAllTasks("Not in a building.");
			endTask();
			return;
		}

		try {

			// Note: use Breadth-First Search (BFS) or Dijkstra's algorithm to find shortest
			// path

			// The choice depends on whether the graph is unweighted or weighted,
			// respectively.

			// BFS is suitable for unweighted graphs and guarantees the shortest path in
			// terms of
			// the number of edges.

			// Dijkstra’s algorithm is used for weighted graphs with non-negative weights,
			// ensuring the path with the minimum total weight is found.

			// Both algorithms maintain a parent map to reconstruct the path from the source
			// to
			// the destination.

			// The key difference lies in the data structure used: BFS uses a FIFO queue,
			// while Dijkstra uses a priority queue to prioritize nodes with the smallest
			// distance.

			// Determine the walking path to the destination.
			if (settlement != null) {
				walkingPath = settlement.getBuildingConnectorManager().determineShortestPath(startBuilding,
						worker.getPosition(), destinationBuilding, destPosition);
			}

			// If no valid walking path is found, end task.
			if (walkingPath == null) {
				logger.warning(worker, 0L, "Currently in " + startBuilding + ". Found no walkable path between my loc @ " + worker.getPosition() 
				+ " and " + destinationBuilding.getName() + " @ " + destPosition + ".");
				worker.getTaskManager().clearAllTasks("Null walking path.");
				endTask();
				return;
			}

			// Initialize task phase.
			setPhase(WALKING);

		} catch (Exception ex) {
			logger.severe(worker, 10_000L, "Unable to walk. No valid interior path.", ex);
			worker.getTaskManager().clearAllTasks("No valid interior path");
		}
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			logger.severe(worker, 10_000L, "Task phase is null.");
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
		// Check that remaining path locations are valid.
		if (!checkRemainingPathLocations()) {
			// Flooding with the following statement in stacktrace
			logger.severe(worker, 10_000L, "Unable to continue walking due to missing path objects.");
			endTask();
			return 0;
		}

		double remainingTime = time - minPulseTime;
		double timeHours = MarsTime.HOURS_PER_MILLISOL * remainingTime;
		double speedKPH = 0;

		if (person != null) {
			speedKPH = Walk.PERSON_WALKING_SPEED * person.getWalkSpeedMod();

		} else {
			speedKPH = Walk.ROBOT_WALKING_SPEED * robot.getWalkSpeedMod();
		}

		// Determine walking distance.
		double coveredKm = speedKPH * timeHours;
		double coveredMeters = coveredKm * 1_000;
		double remainingPathDistance = getRemainingPathDistance();

		if (coveredMeters > remainingPathDistance) {
			coveredMeters = remainingPathDistance;

			if (speedKPH > 0) {
				double usedTime = MarsTime.convertSecondsToMillisols(coveredMeters / speedKPH * 3.6);
				remainingTime = remainingTime - usedTime;
			}

			if (remainingTime < 0)
				remainingTime = 0;
		} else {
			remainingTime = 0D; // Use all the remaining time
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
					logger.severe(worker, 10_000L, "Unable to change building.");
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
//				worker.setPosition(location.getPosition());

				coveredMeters = 0;
			}
		}

		// If path destination is reached, end task.
		if (getRemainingPathDistance() <= VERY_SMALL_DISTANCE) {

			InsidePathLocation location = walkingPath.getNextPathLocation();

//			logger.log(worker, Level.FINEST, 0, "Close enough to final destination (" + location.getPosition());

			worker.setPosition(location.getPosition());

			endTask();
		}

		return remainingTime;
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
			if (loc instanceof Building building) {
				// Check that building still exists.
				if (!settlement.getBuildingManager().containsBuilding(building)) {
					return false;
				}
			} else if (loc instanceof BuildingLocation buildingLoc) {
				// Check that building still exists.
				Building building = buildingLoc.getBuilding();
				if (!settlement.getBuildingManager().containsBuilding(building)) {
					return false;
				}
			} else if (loc instanceof BuildingConnector connector) {
				// Check that building connector still exists.
				if (!settlement.getBuildingConnectorManager().containsBuildingConnector(connector)) {
					return false;
				}
			} else if (loc instanceof Hatch hatch) {
				// Check that building connector for hatch still exists.
				if (!settlement.getBuildingConnectorManager().containsBuildingConnector(hatch.getBuildingConnector())) {
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

		if (location instanceof Hatch hatch) {
			// If hatch leads to new building, place worker in the new building.
			Building currentBuilding = BuildingManager.getBuilding(worker);
			if (!hatch.getBuilding().equals(currentBuilding)) {
				BuildingManager.transferFromBuildingToBuilding(worker, currentBuilding, hatch.getBuilding());
			}
		} else if (location instanceof BuildingConnector connector) {
			// If non-split building connector, place worker in the new building.
			if (!connector.isSplitConnection()) {
				Building currentBuilding = BuildingManager.getBuilding(worker);

				Building newBuilding = null;
				if (connector.getBuilding1().equals(currentBuilding)) {
					newBuilding = connector.getBuilding2();
				}

				else if (connector.getBuilding2().equals(currentBuilding)) {
					newBuilding = connector.getBuilding1();
				}

				else {
					logger.severe(worker, 10_000L, "Bad building connection between " + connector.getBuilding1() + " and "
							+ connector.getBuilding2() + ".");
					return false;
				}

				if (newBuilding != null) {
					BuildingManager.transferFromBuildingToBuilding(worker, currentBuilding, newBuilding);
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

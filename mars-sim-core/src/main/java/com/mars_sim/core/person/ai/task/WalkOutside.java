/*
 * Mars Simulation Project
 * WalkOutside.java
 * @date 2025-07-22
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task;

import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.equipment.EVASuit;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.LocalBoundedObject;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.Msg;

/**
 * A subtask for walking between locations outside of a settlement or vehicle.
 */
public class WalkOutside extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(WalkOutside.class.getName());

	/** Simple Task name */
	public static final String SIMPLE_NAME = WalkOutside.class.getSimpleName();
	
	/** Task name */
	public static final String NAME = Msg.getString("Task.description.walkOutside"); //$NON-NLS-1$
	
	/** Task phases. */
	private static final TaskPhase WALKING = new TaskPhase(Msg.getString("Task.phase.walking")); //$NON-NLS-1$

	// Static members
//	private static final double MIN_PULSE_TIME = Walk.MIN_PULSE_TIME;
	/** The speed factor due to walking in EVA suit. */
	private static final double EVA_MOD = .3;
	/** A very small distance (meters) for measuring how close two positions are. */
	private static final double ONE_CENTIMETER = .01; // within a centimeter
	
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .3D;
	/** The base chance of an accident per millisol. */
	public static final double BASE_ACCIDENT_CHANCE = .001;
	/** Obstacle avoidance path neighbor distance (meters). */
	public static final double NEIGHBOR_DISTANCE = 7D;
	/** The minimum pulse time for completing a task phase in this class.  */
	private static double minPulseTime = 0; //Math.min(standardPulseTime, MIN_PULSE_TIME);

	// Data members
	private boolean obstaclesInPath;
	private boolean ignoreEndEVA;
	private int walkingPathIndex;
	private double[] obstacleSearchLimits;

//	private long tLast;
	
	private LocalPosition start;
	private LocalPosition destination;
	private List<LocalPosition> walkingPath;


	/**
	 * Constructor 1.
	 *
	 * @param person               the person performing the task.
	 * @param start                the starting local location.
	 * @param destination		   the destination local location.
	 * @param ignoreEndEVA         ignore end EVA situations and continue walking
	 *                             task.
	 */
	public WalkOutside(Person person, LocalPosition start, LocalPosition destination,
			boolean ignoreEndEVA) {

		// Use Task constructor.
		super(NAME, person, false, false, STRESS_MODIFIER, SkillType.EVA_OPERATIONS, 100D);

		// Check that the person is currently outside a settlement or vehicle.
		if (person.isInside())
			throw new IllegalStateException("WalkOutside task started when " + person + " was " + person.getLocationStateType());

		init(start, destination, ignoreEndEVA);
	}

	/**
	 * Constructor 2.
	 *
	 * @param robot                the robot performing the walk.
	 * @param start                the starting local location.
	 * @param destination		   the destination local location.
	 * @param ignoreEndEVA         ignore end EVA situations and continue walking
	 *                             task.
	 */
	public WalkOutside(Robot robot, LocalPosition start, LocalPosition destination,
					   boolean ignoreEndEVA) {

		// Use Task constructor.
		super(NAME, robot, false, false, STRESS_MODIFIER, SkillType.EVA_OPERATIONS, 100D);

		// Check that the robot is currently outside a settlement or vehicle.
		if (robot.isInside())
			throw new IllegalStateException("WalkOutside task started when " + robot + " was " + robot.getLocationStateType());

		init(start, destination, ignoreEndEVA);
	}

	private void init(LocalPosition start, LocalPosition destination, boolean ignoreEndEVA) {
		
		// Initialize data members.
		this.start = start;
		this.destination = destination;
		this.ignoreEndEVA = ignoreEndEVA;

		obstaclesInPath = false;
		walkingPathIndex = 1;

		// Determine walking path.
		walkingPath = determineWalkingPath();

		// Initialize task phase.
		setPhase(WALKING);
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			logger.severe(worker, "Task phase is null.");
		}
		if (WALKING.equals(getPhase())) {
			return walkingPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * How many waypoints are there?
	 */
	public int getNumberWayPoints() {
		return walkingPath.size();
	}
	
	/**
	 * Determines the outside walking path, avoiding obstacles as necessary.
	 *
	 * @return walking path as list of X,Y locations.
	 */
	private List<LocalPosition> determineWalkingPath() {

		List<LocalPosition> result = new ArrayList<>();
		result.add(start);

		// Check if direct walking path to destination is free of obstacles.
		Line2D line = new Line2D.Double(start.getX(), start.getY(), destination.getX(), destination.getY());

		boolean freePath = LocalAreaUtil.isLinePathCollisionFree(line, worker.getCoordinates(), true);

		if (freePath) {
			result.add(destination);
		}

		else {
			// Determine path around obstacles using A* path planning algorithm.
			List<LocalPosition> obstacleAvoidancePath = determineObstacleAvoidancePath();
		
			if (obstacleAvoidancePath != null) {
				// Set to obstacle avoidance path.
				result = obstacleAvoidancePath;
			}

			else {
				// Accept obstacle-blocked path as last resort.
				result.add(destination);
				obstaclesInPath = true;
			}
		}

		return result;
	}

	/**
	 * Determines obstacle avoidance path.
	 *
	 * @return path as list of points or null if no path found.
	 */
	private List<LocalPosition> determineObstacleAvoidancePath() {

		// Check if start or destination locations are within obstacles.
		// Return null if either are within obstacles.
		boolean startLocWithinObstacle = !LocalAreaUtil.isPositionCollisionFree(start, worker.getCoordinates());
		if (startLocWithinObstacle) {
			//logger.warning(worker, "Start/End positions are inside an entity");
			return null;
		}
		
		boolean destinationLocWithinObstacle = !LocalAreaUtil.isPositionCollisionFree(destination, worker.getCoordinates());
		if (destinationLocWithinObstacle) {
			//logger.warning(worker, "Start/End positions are inside an entity");
			return null;
		}
	
		List<LocalPosition> result = null;

		// Using A* path planning algorithm, testing out neighbor locations in a 1m x 1m
		// grid.
		// https://en.wikipedia.org/wiki/A*_search_algorithm
		// https://stackabuse.com/graphs-in-java-a-star-algorithm/
		// https://www.happycoders.eu/algorithms/a-star-algorithm-java/

		// The set of locations already evaluated.
		Set<LocalPosition> closedSet = new HashSet<>();

		// The set of tentative locations to be evaluated
		Set<LocalPosition> openSet = new HashSet<>();

		// Initially add starting location to openSet.
		openSet.add(start);

		// The map of navigated locations.
		Map<LocalPosition, LocalPosition> cameFrom = new HashMap<>();
					
		// A note on benchmark: The time it takes for the openSet while loop below 
		// vary greatly, between 2 and 6000 ms to complete
		
		int count = 0;
		
		// Check each location in openSet.
		while (!openSet.isEmpty() && count < 40) {
			// Limit count to 40 to eliminate execution time spike
			count++;
			
			// A note on benchmark: The getLowestFScore() methods below take 2 ms to complete
			
			// Find loc in openSet with lowest fScore value.
			// FScore is distance (m) from start through currentLoc to destination.
			LocalPosition currentLoc = getLowestFScore(openSet);

			if (currentLoc == null)
				break;

			// A note on benchmark: The 3 path methods below take between 2 and 5 ms to complete
			
			// Check if clear path to destination.
			if (checkClearPathToDestination(currentLoc, destination)) {

				// Create path from currentLoc.
//				List<LocalPosition> path = recreatePath(cameFrom, currentLoc);
				result = optimizePath(recreatePath(cameFrom, currentLoc));
				break;
			}
			
			// Remove currentLoc from openSet.
			openSet.remove(currentLoc);

			// Add currentLoc to closedSet.
			closedSet.add(currentLoc);

			double currentGScore = getGScore(currentLoc);
		
			// DO NOT DELETE.
//			long tnow = System.currentTimeMillis();			

			// A note on benchmark: The getNeighbors() methods below take between 2 and 6 ms to complete
			
			// Go through each reachable neighbor location.
			Iterator<LocalPosition> i = getNeighbors(currentLoc).iterator();
//			logger.warning(worker.getAssociatedSettlement(), worker, "getNeighbors() size: " + getNeighbors(currentLoc).size());
			
			// DO NOT DELETE. Debug the real time elapsed [in milliseconds]
//			tLast = System.currentTimeMillis();
//			long elapsedMS = tLast - tnow;
//			if (elapsedMS > 1)
//				logger.severe(worker, "getNeighbors() elapsedMS: " + elapsedMS);
	
			// A note on benchmark: Each while loop here takes between 2 and 5 ms to complete

			while (i.hasNext()) {
				
				LocalPosition neighborLoc = i.next();

				if (closedSet.contains(neighborLoc)) {
					continue;
				}

				double tentativeGScore = currentGScore + NEIGHBOR_DISTANCE;
				double neighborGScore = getGScore(neighborLoc);
				if ((!openSet.contains(neighborLoc) || (tentativeGScore < neighborGScore))
						&& withinObstacleSearch(neighborLoc)) {

					// Map neighbor location from current location in cameFrom.
					cameFrom.put(neighborLoc, currentLoc);

					// Add neighbor location to openSet.
					if (!openSet.contains(neighborLoc)) {
						openSet.add(neighborLoc);
					}
				}
			}
		}

		return result;
	}

	/**
	 * Checks if path between two locations is free of obstacles.
	 *
	 * @param startPos  the first location.
	 * @param endPos     the second location.
	 * @return true if path free of obstacles.
	 */
	private boolean checkClearPathToDestination(LocalPosition startPos, LocalPosition endPos) {
		Line2D line = new Line2D.Double(startPos.getX(), startPos.getY(), endPos.getX(), endPos.getY());
		return LocalAreaUtil.isLinePathCollisionFree(line, worker.getCoordinates(), true);
	}
	
	/**
	 * Finds location in openSet with lowest fScore value. The fScore value is the
	 * distance (m) from start through location to destination.
	 *
	 * @param openSet a set of locations.
	 * @return location with lowest fScore value.
	 */
	private LocalPosition getLowestFScore(Set<LocalPosition> openSet) {

		double lowestFScore = Double.POSITIVE_INFINITY;
		LocalPosition result = null;
		Iterator<LocalPosition> i = openSet.iterator();
		while (i.hasNext()) {
			LocalPosition loc = i.next();
			double fScore = getFScore(loc);
			if (fScore < lowestFScore) {
				result = loc;
				lowestFScore = fScore;
			}
		}

		return result;
	}

	/**
	 * Recreates a path from the cameFromMap.
	 *
	 * @param cameFrom a map of locations and their previous locations.
	 * @param currentLoc2 the last location in a path (not destination location).
	 * @return path as list of points.
	 */
	private List<LocalPosition> recreatePath(Map<LocalPosition, LocalPosition> cameFrom, LocalPosition currentLoc2) {

		List<LocalPosition> result = new ArrayList<>();

		// Add destination location to end of path.
		result.add(destination);

		// Add endLocation location to start of path.
		result.add(0, currentLoc2);

		LocalPosition currentLoc = currentLoc2;
		while (cameFrom.containsKey(currentLoc)) {
			LocalPosition cameFromLoc = cameFrom.get(currentLoc);

			// Add came from location to start of path.
			result.add(0, cameFromLoc);

			// Remove currentLoc key from cameFromMap to prevent endless loops.
			cameFrom.remove(currentLoc);

			currentLoc = cameFromLoc;
		}

		return result;
	}

	/**
	 * Optimizes a path by removing unnecessary locations.
	 *
	 * @param initialPath the initial path to optimize.
	 * @return optimized path.
	 */
	private List<LocalPosition> optimizePath(List<LocalPosition> initialPath) {

		// Cannot be a CopyOnWrite because remove method is not Supported
		List<LocalPosition> optimizedPath = new ArrayList<>(initialPath);

		Iterator<LocalPosition> i = optimizedPath.iterator();
		while (i.hasNext()) {
			LocalPosition loc = i.next();
			int locIndex = optimizedPath.indexOf(loc);
			if ((locIndex > 0) && (locIndex < (optimizedPath.size() - 1))) {
				LocalPosition prevLoc = optimizedPath.get(locIndex - 1);
				LocalPosition nextLoc = optimizedPath.get(locIndex + 1);

				// If clear path between previous and next location,
				// remove this location from path.
				Line2D line = new Line2D.Double(prevLoc.getX(), prevLoc.getY(), nextLoc.getX(), nextLoc.getY());
				if (LocalAreaUtil.isLinePathCollisionFree(line, worker.getCoordinates(), true)) {
					i.remove();
				}
			}
		}

		return optimizedPath;
	}

	/**
	 * Gets the gScore value for a location. The gScore value is the distance (m)
	 * from the starting location to this location.
	 *
	 * @param loc the location.
	 * @return gScore value.
	 */
	private double getGScore(LocalPosition loc) {
		return start.getDirectionTo(loc);
	}

	/**
	 * Gets the fScore value for a location. The fScore value is the total distance
	 * (m) from the starting location to this location and then to the destination
	 * location.
	 *
	 * @param loc the location.
	 * @return the fScore value.
	 */
	private double getFScore(LocalPosition loc) {
		double gScore = getGScore(loc);
		double fScore = gScore + loc.getDistanceTo(destination);
		return fScore;
	}

	/**
	 * Gets search location neighbors to a given location. This method gets a set of
	 * four locations at 1m distance North, South, East and West of the current
	 * location.
	 *
	 * @param currentLoc the current location.
	 * @return set of neighbor locations.
	 */
	private Set<LocalPosition> getNeighbors(LocalPosition currentLoc) {

		Set<LocalPosition> result = new HashSet<>(8);

		// Get location North of currentLoc.
		LocalPosition northLoc = new LocalPosition(currentLoc.getX(), currentLoc.getY() + NEIGHBOR_DISTANCE);
		Line2D northLine = new Line2D.Double(currentLoc.getX(), currentLoc.getY(), northLoc.getX(), northLoc.getY());
		if (LocalAreaUtil.isLinePathCollisionFree(northLine, worker.getCoordinates(), true)) {
			result.add(northLoc);
		}


		// Get location East of currentLoc.
		LocalPosition eastLoc = new LocalPosition(currentLoc.getX() - NEIGHBOR_DISTANCE, currentLoc.getY());
		Line2D eastLine = new Line2D.Double(currentLoc.getX(), currentLoc.getY(), eastLoc.getX(), eastLoc.getY());
		if (LocalAreaUtil.isLinePathCollisionFree(eastLine, worker.getCoordinates(), true)) {
			result.add(eastLoc);
		}

		// Get location South of currentLoc.
		LocalPosition southLoc = new LocalPosition(currentLoc.getX(), currentLoc.getY() - NEIGHBOR_DISTANCE);
		Line2D southLine = new Line2D.Double(currentLoc.getX(), currentLoc.getY(), southLoc.getX(), southLoc.getY());
		if (LocalAreaUtil.isLinePathCollisionFree(southLine, worker.getCoordinates(), true)) {
			result.add(southLoc);
		}


		// Get location West of currentLoc.
		LocalPosition westLoc = new LocalPosition(currentLoc.getX() + NEIGHBOR_DISTANCE, currentLoc.getY());
		Line2D westLine = new Line2D.Double(currentLoc.getX(), currentLoc.getY(), westLoc.getX(), westLoc.getY());

		if (LocalAreaUtil.isLinePathCollisionFree(westLine, worker.getCoordinates(), true)) {
			result.add(westLoc);
		}

		return result;
	}

	/**
	 * Gets the local obstacle path search limits for a coordinate location.
	 *
	 * @param location the coordinate location.
	 * @return array of four double values representing X max, X min, Y max, and Y
	 *         min.
	 */
	private double[] getLocalObstacleSearchLimits(Coordinates location) {

		double[] result = new double[] { 0D, 0D, 0D, 0D };

		// Get all local bounded objects at the location.
		Iterator<LocalBoundedObject> i = LocalAreaUtil.getAllLocalBoundedObjectsAtLocation(location).iterator();
		while (i.hasNext()) {

			// Determine bounding rectangle for local bounded object with facing.
			Rectangle2D bounds = LocalAreaUtil.getBoundingRectangle(i.next());

			// Extend result boundaries to enclose bounding rectangle's limits.
			if (result[0] < bounds.getMaxX()) {
				result[0] = bounds.getMaxX();
			}

			if (result[1] > bounds.getMinX()) {
				result[1] = bounds.getMinX();
			}

			if (result[2] < bounds.getMaxY()) {
				result[2] = bounds.getMaxY();
			}

			if (result[3] > bounds.getMinY()) {
				result[3] = bounds.getMinY();
			}
		}

		// Extend boundary to include starting location.
		double startXLocation = start.getX();
		double startYLocation = start.getY();
		double destinationXLocation = destination.getX();
		double destinationYLocation = destination.getY();
		if (result[0] < startXLocation) {
			result[0] = startXLocation;
		}

		if (result[1] > startXLocation) {
			result[1] = startXLocation;
		}

		if (result[2] < startYLocation) {
			result[2] = startYLocation;
		}

		if (result[3] > startYLocation) {
			result[3] = startYLocation;
		}

		// Extend boundary to include destination location.
		if (result[0] < destinationXLocation) {
			result[0] = destinationXLocation;
		}

		if (result[1] > destinationXLocation) {
			result[1] = destinationXLocation;
		}

		if (result[2] < destinationYLocation) {
			result[2] = destinationYLocation;
		}

		if (result[3] > destinationYLocation) {
			result[3] = destinationYLocation;
		}

		// Extend result boundaries by neighbor distance.
		result[0] += NEIGHBOR_DISTANCE;
		result[1] -= NEIGHBOR_DISTANCE;
		result[2] += NEIGHBOR_DISTANCE;
		result[3] -= NEIGHBOR_DISTANCE;

		return result;
	}

	/**
	 * Checks if point location is within the obstacle search limits.
	 *
	 * @param neighborLoc the location.
	 * @return true if location is within search limits.
	 */
	private boolean withinObstacleSearch(LocalPosition neighborLoc) {

		if (obstacleSearchLimits == null) {
			obstacleSearchLimits = getLocalObstacleSearchLimits(worker.getCoordinates());
		}

		boolean result = !(neighborLoc.getX() > obstacleSearchLimits[0]);

		// Check if X value is larger than X max limit.

        // Check if X value is smaller than X min limit.
		if (neighborLoc.getX() < obstacleSearchLimits[1]) {
			result = false;
		}

		// Check if Y value is larger than Y max limit.
		if (neighborLoc.getY() > obstacleSearchLimits[2]) {
			result = false;
		}

		// Check if Y value is smaller thank Y min limit.
		if (neighborLoc.getY() < obstacleSearchLimits[3]) {
			result = false;
		}

		return result;
	}

	/**
	 * Checks if there are any obstacles in the walking path.
	 *
	 * @return true if any obstacles in walking path.
	 */
	public boolean areObstaclesInPath() {
		return obstaclesInPath;
	}

	/**
	 * Performs the walking phase of the task.
	 *
	 * @param time the amount of time (millisol) to perform the walking phase.
	 * @return the amount of time (millisol) left after performing the walking
	 *         phase.
	 */
	private double walkingPhase(double time) {
		double remainingTime = time - minPulseTime;
		double timeHours = MarsTime.HOURS_PER_MILLISOL * remainingTime;
		double speedKPH = 0;

		if (person != null) {
			// Check for accident.
			EVASuit suit = person.getSuit();
			if (suit != null) {

				// EVA operations skill modification.
				int skill = person.getSkillManager().getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
				checkForAccident(suit, time, BASE_ACCIDENT_CHANCE, skill, "EVA");
			}

			// Check for radiation exposure during the EVA operation.
			// checkForRadiation(time);
			// If there are any EVA problems, end walking outside task.
			if (!ignoreEndEVA && (hasEVAProblem(person) || EVAOperation.isGettingDark(person))) {
				endTask();
				return time;
			}
			else
				speedKPH = Walk.PERSON_WALKING_SPEED * person.getWalkSpeedMod() * EVA_MOD;
		}

		else if (robot != null) {
			speedKPH = Walk.ROBOT_WALKING_SPEED * robot.getWalkSpeedMod() * EVA_MOD;
		}

		// Determine walking distance.
		double coveredKm = speedKPH * timeHours;
		double coveredMeters = coveredKm * 1_000;
		double remainingPathDistance = getRemainingPathDistance();

		// Determine time left after walking.
		if (coveredMeters > remainingPathDistance) {
			coveredMeters = remainingPathDistance;
			
			if (speedKPH > 0) {
				double usedTime = MarsTime.convertSecondsToMillisols(coveredMeters / speedKPH * 3.6);
				remainingTime = remainingTime - usedTime;
			}
			
			if (remainingTime < 0)
				remainingTime = 0;
		}
		else {
			remainingTime = 0D; // Use all the remaining time
		}
		
		while (coveredMeters > ONE_CENTIMETER) {
			// Walk to next path location.
			LocalPosition location = walkingPath.get(walkingPathIndex);
			double distanceToLocation = worker.getPosition().getDistanceTo(location);
			
			if (coveredMeters >= distanceToLocation) {

				// Set person at next path location.
				worker.setPosition(location);

				coveredMeters -= distanceToLocation;
				
				if (walkingPath.size() > (walkingPathIndex + 1)) {
					walkingPathIndex++;
				}
			}

			else {
				// Walk in direction of next path location.

				// Determine direction
				//double direction = determineDirection(location.getX(), location.getY());
				double direction = worker.getPosition().getDirectionTo(location);
				
				// Determine person's new location at distance and direction.
				walkInDirection(direction, coveredMeters);

				// Set person at next path location.
//				worker.setPosition(location);

				coveredMeters = 0D;
			}
		}

		// If path destination is reached, end task.
		if (getRemainingPathDistance() <= ONE_CENTIMETER) {

			LocalPosition finalLocation = walkingPath.get(walkingPath.size() - 1);
			
			logger.log(worker, Level.FINER, 5000, "Finished walking to new location outside.");
			
			worker.setPosition(finalLocation);

			endTask();
		}
		
        // Warning: see GitHub issue #1039 for details on return a 
        // non-zero value from this method
//      return remainingTime;
		
        return 0;
	}

	/**
	 * Checks if there is an EVA problem for a person.
	 *
	 * @param person the person.
	 * @return true if an EVA problem.
	 */
	public boolean hasEVAProblem(Person person) {
		return EVAOperation.hasEVASuitProblem(person);
	}

	/**
	 * Walks in a given direction for a given distance.
	 *
	 * @param direction the direction (radians) of travel.
	 * @param distance  the distance (meters) to travel.
	 */
	private void walkInDirection(double direction, double distance) {
		worker.setPosition(worker.getPosition().getPosition(distance, direction) );
	}

	/**
	 * Gets the remaining path distance.
	 *
	 * @return distance (meters).
	 */
	private double getRemainingPathDistance() {

		double result = 0D;

		LocalPosition prevLoc = worker.getPosition();

		for (int x = walkingPathIndex; x < walkingPath.size(); x++) {
			LocalPosition nextLoc = walkingPath.get(x);
			double distance = prevLoc.getDistanceTo(nextLoc);
			result += distance;
			prevLoc = nextLoc;
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
		start = null;
		destination = null;
		walkingPath.clear();
		walkingPath = null;
			
		super.destroy();
	}
}

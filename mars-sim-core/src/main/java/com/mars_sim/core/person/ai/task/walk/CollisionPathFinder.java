package com.mars_sim.core.person.ai.task.walk;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.LocalBoundedObject;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.task.util.Worker;

/**
 * A path finder that avoids obstacles using a collision detection algorithm.
 * The finder is created from a starting location and a worker doing the walking
 */
public class CollisionPathFinder implements OutsidePathFinder {

    /** Obstacle avoidance path neighbor distance (meters). */
	private static final double NEIGHBOR_DISTANCE = 7D;

    private LocalPosition start;
    private Worker walker;

    private double[] obstacleSearchLimits;
    private int maxAttempts;

    public CollisionPathFinder(Worker walker, LocalPosition start) {
        this.walker = walker;
        this.start = start;
        this.maxAttempts = 40;
    }

    /**
	 * Determines the outside walking path, avoiding obstacles as necessary.
	 *
     * @param destination the destination location.
	 * @return walking path as list of X,Y locations.
	 */
	@Override
    public PathSolution determineWalkingPath(LocalPosition destination) {

		List<LocalPosition> result = new ArrayList<>();
		result.add(start);

		// Check if direct walking path to destination is free of obstacles.
		boolean freePath = LocalAreaUtil.isLinePathCollisionFree(start, destination, walker.getCoordinates());

		boolean obstaclesInPath = false;

        if (freePath) {
			result.add(destination);
		}

		else {
			// Determine path around obstacles using A* path planning algorithm.
			List<LocalPosition> obstacleAvoidancePath = determineObstacleAvoidancePath(destination, maxAttempts);
		
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

		return new PathSolution(result, obstaclesInPath);
	}

	/**
	 * Determines obstacle avoidance path.
	 * @param maxAttempts 
	 *
	 * @return path as list of points or null if no path found.
	 */
	private List<LocalPosition> determineObstacleAvoidancePath(LocalPosition destination, int maxAttempts) {

		// Check if start or destination locations are within obstacles.
		// Return null if either are within obstacles.
		boolean startLocWithinObstacle = !LocalAreaUtil.isPositionCollisionFree(start, walker.getCoordinates());
		if (startLocWithinObstacle) {
			return null;
		}
		
		boolean destinationLocWithinObstacle = !LocalAreaUtil.isPositionCollisionFree(destination, walker.getCoordinates());
		if (destinationLocWithinObstacle) {
			return null;
		}
	
        // Do this once up front
		obstacleSearchLimits = getLocalObstacleSearchLimits(walker.getCoordinates(), destination);

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
		while (!openSet.isEmpty() && count < maxAttempts) {
			// Limit count to 40 to eliminate execution time spike
			count++;
			
			// A note on benchmark: The getLowestFScore() methods below take 2 ms to complete
			
			// Find loc in openSet with lowest fScore value.
			// FScore is distance (m) from start through currentLoc to destination.
			LocalPosition currentLoc = getLowestFScore(openSet, destination);

			if (currentLoc == null)
				break;

			// A note on benchmark: The 3 path methods below take between 2 and 5 ms to complete
			
			// Check if clear path to destination.
			if (LocalAreaUtil.isLinePathCollisionFree(currentLoc, destination, walker.getCoordinates())) {

				// Create path from currentLoc.
				result = optimizePath(recreatePath(cameFrom, currentLoc, destination));
				break;
			}
			
			// Remove currentLoc from openSet.
			openSet.remove(currentLoc);

			// Add currentLoc to closedSet.
			closedSet.add(currentLoc);

			double currentGScore = getGScore(currentLoc);	

			// A note on benchmark: The getNeighbors() methods below take between 2 and 6 ms to complete
			
			// Go through each reachable neighbor location.
			Iterator<LocalPosition> i = getNeighbors(currentLoc).iterator();
	
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
	 * Finds location in openSet with lowest fScore value. The fScore value is the
	 * distance (m) from start through location to destination.
	 *
	 * @param openSet a set of locations.
	 * @param destination 
	 * @return location with lowest fScore value.
	 */
	private LocalPosition getLowestFScore(Set<LocalPosition> openSet, LocalPosition destination) {

		double lowestFScore = Double.POSITIVE_INFINITY;
		LocalPosition result = null;
		Iterator<LocalPosition> i = openSet.iterator();
		while (i.hasNext()) {
			LocalPosition loc = i.next();
			double fScore = getFScore(loc, destination);
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
	 * @param destination the final destination location.
	 * @return path as list of points.
	 */
	private List<LocalPosition> recreatePath(Map<LocalPosition, LocalPosition> cameFrom, LocalPosition currentLoc2,
                                            LocalPosition destination) {

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
				if (LocalAreaUtil.isLinePathCollisionFree(nextLoc, prevLoc, walker.getCoordinates())) {
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
	 * @param destination 
	 * @return the fScore value.
	 */
	private double getFScore(LocalPosition loc, LocalPosition destination) {
		double gScore = getGScore(loc);
		return gScore + loc.getDistanceTo(destination);
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

		Set<LocalPosition> result = new HashSet<>();

		// Get location North of currentLoc.
		LocalPosition northLoc = new LocalPosition(currentLoc.getX(), currentLoc.getY() + NEIGHBOR_DISTANCE);
		if (LocalAreaUtil.isLinePathCollisionFree(currentLoc, northLoc, walker.getCoordinates())) {
			result.add(northLoc);
		}


		// Get location East of currentLoc.
		LocalPosition eastLoc = new LocalPosition(currentLoc.getX() - NEIGHBOR_DISTANCE, currentLoc.getY());
		if (LocalAreaUtil.isLinePathCollisionFree(currentLoc, eastLoc, walker.getCoordinates())) {
			result.add(eastLoc);
		}

		// Get location South of currentLoc.
		LocalPosition southLoc = new LocalPosition(currentLoc.getX(), currentLoc.getY() - NEIGHBOR_DISTANCE);
		if (LocalAreaUtil.isLinePathCollisionFree(currentLoc, southLoc, walker.getCoordinates())) {
			result.add(southLoc);
		}


		// Get location West of currentLoc.
		LocalPosition westLoc = new LocalPosition(currentLoc.getX() + NEIGHBOR_DISTANCE, currentLoc.getY());
		if (LocalAreaUtil.isLinePathCollisionFree(currentLoc, westLoc, walker.getCoordinates())) {
			result.add(westLoc);
		}

		return result;
	}

	/**
	 * Gets the local obstacle path search limits for a coordinate location.
	 *
	 * @param location the coordinate location.
	 * @param destination 
	 * @return array of four double values representing X max, X min, Y max, and Y
	 *         min.
	 */
	private double[] getLocalObstacleSearchLimits(Coordinates location, LocalPosition destination) {

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

		boolean result = (neighborLoc.getX() <= obstacleSearchLimits[0]);

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
}

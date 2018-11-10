/**
 * Mars Simulation Project
 * WalkOutside.java
 * @version 3.1.0 2017-09-13
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.mars.Mars;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.NaturalAttributeType;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RoboticAttributeType;
import org.mars_sim.msp.core.robot.RoboticAttributeManager;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * A subtask for walking between locations outside of a settlement or vehicle.
 */
public class WalkOutside
extends Task
implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** default logger. */
    private static Logger logger = Logger.getLogger(WalkOutside.class.getName());

    /** Task phases. */
    private static final TaskPhase WALKING = new TaskPhase(Msg.getString(
            "Task.phase.walking")); //$NON-NLS-1$

    // Static members
    /** in km / hr. */
    private static final double BASE_WALKING_SPEED = 2D;
    /** in km / hr. */
    private static final double MAX_WALKING_SPEED = 5D;
    private static final double VERY_SMALL_DISTANCE = .00001D;

    /** The stress modified per millisol. */
    private static final double STRESS_MODIFIER = .3D;

    /** The base chance of an accident per millisol. */
    public static final double BASE_ACCIDENT_CHANCE = .001;

    /** Obstacle avoidance path neighbor distance (meters). */
    public static final double NEIGHBOR_DISTANCE = 7D;

    // Data members
    private double startXLocation;
    private double startYLocation;
    private double destinationXLocation;
    private double destinationYLocation;
    private boolean obstaclesInPath;
    private List<Point2D> walkingPath;
    private int walkingPathIndex;
    private double[] obstacleSearchLimits;
    private boolean ignoreEndEVA;

	private static int oxygenID = ResourceUtil.oxygenID;
	private static int waterID = ResourceUtil.waterID;
	
    /**
     * Constructor.
     * @param person the person performing the task.
     * @param startXLocation the starting local X location.
     * @param startYLocation the starting local Y location.
     * @param destinationXLocation the destination local X location.
     * @param destinationYLocation the destination local Y location.
     * @param ignoreEndEVA ignore end EVA situations and continue walking task.
     */
    public WalkOutside(Person person, double startXLocation, double startYLocation,
            double destinationXLocation, double destinationYLocation, boolean ignoreEndEVA) {

        // Use Task constructor.
        super("Walking Exterior", person, false, false, STRESS_MODIFIER, false, 0D);

        // Initialize data members.
        this.startXLocation = startXLocation;
        this.startYLocation = startYLocation;
        this.destinationXLocation = destinationXLocation;
        this.destinationYLocation = destinationYLocation;
        this.ignoreEndEVA = ignoreEndEVA;

        // Check that the person is currently outside a settlement or vehicle.
        LocationSituation location = person.getLocationSituation();
        if (location != LocationSituation.OUTSIDE) {
            throw new IllegalStateException(
                    "WalkOutside task started when " + person + " is not outside.");
        }

        init();
    }

    public WalkOutside(Robot robot, double startXLocation, double startYLocation,
            double destinationXLocation, double destinationYLocation, boolean ignoreEndEVA) {

        // Use Task constructor.
        super("Walking Exterior", robot, false, false, STRESS_MODIFIER, false, 0D);
        // Initialize data members.
        this.startXLocation = startXLocation;
        this.startYLocation = startYLocation;
        this.destinationXLocation = destinationXLocation;
        this.destinationYLocation = destinationYLocation;
        this.ignoreEndEVA = ignoreEndEVA;

        // Check that the robot is currently outside a settlement or vehicle.
        LocationSituation location = robot.getLocationSituation();
        if (location != LocationSituation.OUTSIDE) {
            throw new IllegalStateException(
                    "WalkOutside task started when " + robot + " is not outside.");
        }

        init();
    }

    public void init() {

        obstaclesInPath = false;
        walkingPathIndex = 1;

        // Determine walking path.
        walkingPath = determineWalkingPath();

        // Initialize task phase.
        addPhase(WALKING);
        setPhase(WALKING);
    }


    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        if (WALKING.equals(getPhase())) {
            return walkingPhase(time);
        }
        else {
            return time;
        }
    }

    /**
     * Determine the outside walking path, avoiding obstacles as necessary.
     * @return walking path as list of X,Y locations.
     */
    List<Point2D> determineWalkingPath() {

        Point2D startLoc = new Point2D.Double(startXLocation, startYLocation);
        Point2D destinationLoc = new Point2D.Double(destinationXLocation, destinationYLocation);

        List<Point2D> result = new ArrayList<Point2D>();
        result.add(startLoc);

        // Check if direct walking path to destination is free of obstacles.
        Line2D line = new Line2D.Double(startXLocation, startYLocation, destinationXLocation,
                destinationYLocation);

        boolean freePath = false;

		if (person != null)
        	freePath = LocalAreaUtil.checkLinePathCollision(line, person.getCoordinates(), true);
		else if (robot != null)
        	freePath = LocalAreaUtil.checkLinePathCollision(line, robot.getCoordinates(), true);

        if (freePath) {
            result.add(destinationLoc);
        }
        else {

            // Determine path around obstacles using A* path planning algorithm.
            List<Point2D> obstacleAvoidancePath = determineObstacleAvoidancePath();

            if (obstacleAvoidancePath != null) {

                // Set to obstacle avoidance path.
                result = obstacleAvoidancePath;
            }
            else {

                // Accept obstacle-blocked path as last resort.
                result.add(destinationLoc);
                obstaclesInPath = true;
            }
        }

        return result;
    }

    /**
     * Determine obstacle avoidance path.
     * @return path as list of points or null if no path found.
     */
    List<Point2D> determineObstacleAvoidancePath() {

        List<Point2D> result = null;

        Point2D startLoc = new Point2D.Double(startXLocation, startYLocation);
        Point2D endLoc = new Point2D.Double(destinationXLocation, destinationYLocation);

        // Check if start or destination locations are within obstacles.
        // Return null if either are within obstacles.
        boolean startLocWithinObstacle = false;
        boolean destinationLocWithinObstacle = false;

		if (person != null) {
	        startLocWithinObstacle = !LocalAreaUtil.checkLocationCollision(startXLocation,
	                startYLocation, person.getCoordinates());
	        destinationLocWithinObstacle = !LocalAreaUtil.checkLocationCollision(destinationXLocation,
	                destinationYLocation, person.getCoordinates());

		}
		else if (robot != null) {
	        startLocWithinObstacle = !LocalAreaUtil.checkLocationCollision(startXLocation,
	                startYLocation, robot.getCoordinates());
	        destinationLocWithinObstacle = !LocalAreaUtil.checkLocationCollision(destinationXLocation,
	                destinationYLocation, robot.getCoordinates());
		}


        if (startLocWithinObstacle || destinationLocWithinObstacle) {
            return null;
        }

        // Using A* path planning algorithm, testing out neighbor locations in a 1m x 1m grid.
        // http://en.wikipedia.org/wiki/A*

        // The set of locations already evaluated.
        Set<Point2D> closedSet = new HashSet<Point2D>();

        // The set of tentative locations to be evaluated
        Set<Point2D> openSet = new HashSet<Point2D>();

        // Initially add starting location to openSet.
        openSet.add(startLoc);

        // The map of navigated locations.
        Map<Point2D, Point2D> cameFrom = new HashMap<Point2D, Point2D>();

        // Check each location in openSet.
        while (openSet.size() > 0) {

            // Find loc in openSet with lowest fScore value.
            // FScore is distance (m) from start through currentLoc to destination.
            Point2D currentLoc = getLowestFScore(openSet);

            // Check if clear path to destination.
            if (checkClearPathToDestination(currentLoc, endLoc)) {

                // Create path from currentLoc.
                List<Point2D> path = recreatePath(cameFrom, currentLoc);
                result = optimizePath(path);
                break;
            }

            // Remove currentLoc from openSet.
            openSet.remove(currentLoc);

            // Add currentLoc to closedSet.
            closedSet.add(currentLoc);

            double currentGScore = getGScore(currentLoc);

            // Go through each reachable neighbor location.
            Iterator<Point2D> i = getNeighbors(currentLoc).iterator();
            while (i.hasNext()) {

                Point2D neighborLoc = i.next();

                if (closedSet.contains(neighborLoc)) {
                    continue;
                }

                double tentativeGScore = currentGScore + NEIGHBOR_DISTANCE;
                double neighborGScore = getGScore(neighborLoc);
                if ((!openSet.contains(neighborLoc) || (tentativeGScore < neighborGScore)) &&
                        withinObstacleSearch(neighborLoc)) {

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
     * Find location in openSet with lowest fScore value.
     * The fScore value is the distance (m) from start through location to destination.
     * @param openSet a set of locations.
     * @return location with lowest fScore value.
     */
    private Point2D getLowestFScore(Set<Point2D> openSet) {

        double lowestFScore = Double.POSITIVE_INFINITY;
        Point2D result = null;
        Iterator<Point2D> i = openSet.iterator();
        while (i.hasNext()) {
            Point2D loc = i.next();
            double fScore = getFScore(loc);
            if (fScore < lowestFScore) {
                result = loc;
                lowestFScore = fScore;
            }
        }

        return result;
    }

    /**
     * Checks if path between two locations is free of obstacles.
     * @param currentLoc the first location.
     * @param endLoc the second location.
     * @return true if path free of obstacles.
     */
    boolean checkClearPathToDestination(Point2D currentLoc, Point2D endLoc) {

    	boolean result = false;

        Line2D line = new Line2D.Double(currentLoc.getX(), currentLoc.getY(), endLoc.getX(),
                endLoc.getY());

		if (person != null) {
			result = LocalAreaUtil.checkLinePathCollision(line, person.getCoordinates(), true);
		}
		else if (robot != null) {
			result = LocalAreaUtil.checkLinePathCollision(line, robot.getCoordinates(), true);
		}

		 return result;
    }

    /**
     * Recreate a path from the cameFromMap.
     * @param cameFromMap a map of locations and their previous locations.
     * @param endLocation the last location in a path (not destination location).
     * @return path as list of points.
     */
    private List<Point2D> recreatePath(Map<Point2D, Point2D> cameFromMap, Point2D endLocation) {

        List<Point2D> result = new ArrayList<Point2D>();

        // Add destination location to end of path.
        result.add(new Point2D.Double(destinationXLocation, destinationYLocation));

        // Add endLocation location to start of path.
        result.add(0, endLocation);

        Point2D currentLoc = endLocation;
        while (cameFromMap.containsKey(currentLoc)) {
            Point2D cameFromLoc = cameFromMap.get(currentLoc);

            // Add came from location to start of path.
            result.add(0, cameFromLoc);

            // Remove currentLoc key from cameFromMap to prevent endless loops.
            cameFromMap.remove(currentLoc);

            currentLoc = cameFromLoc;
        }

        return result;
    }

    /**
     * Optimizes a path by removing unnecessary locations.
     * @param initialPath the initial path to optimize.
     * @return optimized path.
     */
    private List<Point2D> optimizePath(List<Point2D> initialPath) {

        List<Point2D> optimizedPath = new ArrayList<Point2D>(initialPath);

        Iterator<Point2D> i = optimizedPath.iterator();
        while (i.hasNext()) {
            Point2D loc = i.next();
            int locIndex = optimizedPath.indexOf(loc);
            if ((locIndex > 0) && (locIndex < (optimizedPath.size() - 1))) {
                Point2D prevLoc = optimizedPath.get(locIndex - 1);
                Point2D nextLoc = optimizedPath.get(locIndex + 1);

                // If clear path between previous and next location,
                // remove this location from path.
                Line2D line = new Line2D.Double(prevLoc.getX(), prevLoc.getY(),
                        nextLoc.getX(), nextLoc.getY());

				if (person != null) {
	                if (LocalAreaUtil.checkLinePathCollision(line, person.getCoordinates(), true)) {
	                    i.remove();
	                }
				}
				else if (robot != null) {
	                if (LocalAreaUtil.checkLinePathCollision(line, robot.getCoordinates(), true)) {
	                    i.remove();
	                }
				}



            }
        }

        return optimizedPath;
    }

    /**
     * Gets the gScore value for a location.
     * The gScore value is the distance (m) from the starting location to this location.
     * @param loc the location.
     * @return gScore value.
     */
    private double getGScore(Point2D loc) {
        return Point2D.distance(startXLocation, startYLocation, loc.getX(), loc.getY());
    }

    /**
     * Gets the fScore value for a location.
     * The fScore value is the total distance (m) from the starting location to this location
     * and then to the destination location.
     * @param loc the location.
     * @return the fScore value.
     */
    private double getFScore(Point2D loc) {
        double gScore = getGScore(loc);
        double fScore = gScore + Point2D.distance(loc.getX(), loc.getY(), destinationXLocation,
                destinationYLocation);
        return fScore;
    }

    /**
     * Get search location neighbors to a given location.
     * This method gets a set of four locations at 1m distance North, South, East and West of the
     * current location.
     * @param currentLoc the current location.
     * @return set of neighbor locations.
     */
    private Set<Point2D> getNeighbors(Point2D currentLoc) {

        Set<Point2D> result = new HashSet<Point2D>(8);

        // Get location North of currentLoc.
        Point2D northLoc = new Point2D.Double(currentLoc.getX(), currentLoc.getY() + NEIGHBOR_DISTANCE);
        Line2D northLine = new Line2D.Double(currentLoc.getX(), currentLoc.getY(), northLoc.getX(),
                northLoc.getY());
		if (person != null) {
	        if (LocalAreaUtil.checkLinePathCollision(northLine, person.getCoordinates(), true)) {
	            result.add(northLoc);
	        }
		}
		else if (robot != null) {
	        if (LocalAreaUtil.checkLinePathCollision(northLine, robot.getCoordinates(), true)) {
	            result.add(northLoc);
	        }
		}



        // Get location East of currentLoc.
        Point2D eastLoc = new Point2D.Double(currentLoc.getX() - NEIGHBOR_DISTANCE, currentLoc.getY());
        Line2D eastLine = new Line2D.Double(currentLoc.getX(), currentLoc.getY(), eastLoc.getX(),
                eastLoc.getY());
		if (person != null) {
	        if (LocalAreaUtil.checkLinePathCollision(eastLine, person.getCoordinates(), true)) {
	            result.add(eastLoc);
	        }
		}
		else if (robot != null) {
	        if (LocalAreaUtil.checkLinePathCollision(eastLine, robot.getCoordinates(), true)) {
	            result.add(eastLoc);
	        }
		}




        // Get location South of currentLoc.
        Point2D southLoc = new Point2D.Double(currentLoc.getX(), currentLoc.getY() - NEIGHBOR_DISTANCE);
        Line2D southLine = new Line2D.Double(currentLoc.getX(), currentLoc.getY(), southLoc.getX(),
                southLoc.getY());

		if (person != null) {
	        if (LocalAreaUtil.checkLinePathCollision(southLine, person.getCoordinates(), true)) {
	            result.add(southLoc);
	        }
		}
		else if (robot != null) {
	        if (LocalAreaUtil.checkLinePathCollision(southLine, robot.getCoordinates(), true)) {
	            result.add(southLoc);
	        }
		}



        // Get location West of currentLoc.
        Point2D westLoc = new Point2D.Double(currentLoc.getX() + NEIGHBOR_DISTANCE, currentLoc.getY());
        Line2D westLine = new Line2D.Double(currentLoc.getX(), currentLoc.getY(), westLoc.getX(),
                westLoc.getY());

		if (person != null) {
	        if (LocalAreaUtil.checkLinePathCollision(westLine, person.getCoordinates(), true)) {
	            result.add(westLoc);
	        }
		}
		else if (robot != null) {
	        if (LocalAreaUtil.checkLinePathCollision(westLine, robot.getCoordinates(), true)) {
	            result.add(westLoc);
	        }
		}



        return result;
    }

    /**
     * Gets the local obstacle path search limits for a coordinate location.
     * @param location the coordinate location.
     * @return array of four double values representing X max, X min, Y max, and Y min.
     */
    double[] getLocalObstacleSearchLimits(Coordinates location) {

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
     * Check if point location is within the obstacle search limits.
     * @param location the location.
     * @return true if location is within search limits.
     */
    private boolean withinObstacleSearch(Point2D location) {

		if (person != null) {
	        if (obstacleSearchLimits == null) {
	            obstacleSearchLimits = getLocalObstacleSearchLimits(person.getCoordinates());
	        }
		}
		else if (robot != null) {
	        if (obstacleSearchLimits == null) {
	            obstacleSearchLimits = getLocalObstacleSearchLimits(robot.getCoordinates());
	        }
		}




        boolean result = true;

        // Check if X value is larger than X max limit.
        if (location.getX() > obstacleSearchLimits[0]) {
            result = false;
        }

        // Check if X value is smaller than X min limit.
        if (location.getX() < obstacleSearchLimits[1]) {
            result = false;
        }

        // Check if Y value is larger than Y max limit.
        if (location.getY() > obstacleSearchLimits[2]) {
            result = false;
        }

        // Check if Y value is smaller thank Y min limit.
        if (location.getY() < obstacleSearchLimits[3]) {
            result = false;
        }

        return result;
    }

    /**
     * Check if there are any obstacles in the walking path.
     * @return true if any obstacles in walking path.
     */
    public boolean areObstaclesInPath() {
        return obstaclesInPath;
    }

    /**
     * Performs the walking phase of the task.
     * @param time the amount of time (millisol) to perform the walking phase.
     * @return the amount of time (millisol) left after performing the walking phase.
     */
    private double walkingPhase(double time) {

        // Check for accident.
        checkForAccident(time);

		if (person != null) {

		    // Check for radiation exposure during the EVA operation.
	        //checkForRadiation(time);
	        // If there are any EVA problems, end walking outside task.
	        if (!ignoreEndEVA && checkEVAProblem(person)) {
	            endTask();
	            return time;
	        }
		}
		else if (robot != null) {
	        // If there are any EVA problems, end walking outside task.
	        //if (!ignoreEndEVA && EVAOperation.checkEVAProblem(robot)) {
	            endTask();
	            return time;
	        //}
		}



        double walkingSpeed = getWalkingSpeed();

        // Determine walking distance.
        double timeHours = MarsClock.convertMillisolsToSeconds(time) / 60D / 60D;
        double distanceKm = walkingSpeed * timeHours;
        double distanceMeters = distanceKm * 1000D;
        double remainingPathDistance = getRemainingPathDistance();

        // Determine time left after walking.
        double timeLeft = 0D;
        if (distanceMeters > remainingPathDistance) {
            double overDistance = distanceMeters - remainingPathDistance;
            timeLeft = MarsClock.convertSecondsToMillisols(overDistance / 1000D / walkingSpeed * 60D * 60D);
            distanceMeters = remainingPathDistance;
        }

        while (distanceMeters > VERY_SMALL_DISTANCE) {

            // Walk to next path location.
            Point2D location = walkingPath.get(walkingPathIndex);

            double distanceToLocation = 0;

			if (person != null) {
				distanceToLocation = Point2D.distance(person.getXLocation(), person.getYLocation(),
		                    location.getX(), location.getY());

			}
			else if (robot != null) {
				distanceToLocation = Point2D.distance(robot.getXLocation(), robot.getYLocation(),
		                    location.getX(), location.getY());

			}


            if (distanceMeters >= distanceToLocation) {

				if (person != null) {
	                // Set person at next path location.
					person.setXLocation(location.getX());
	                person.setYLocation(location.getY());

				}
				else if (robot != null) {
	                // Set robot at next path location.
	                robot.setXLocation(location.getX());
	                robot.setYLocation(location.getY());
				}


                distanceMeters -= distanceToLocation;
                if (walkingPath.size() > (walkingPathIndex + 1)) {
                    walkingPathIndex++;
                }
            }
            else {
                // Walk in direction of next path location.

                // Determine direction
                double direction = determineDirection(location.getX(), location.getY());

                // Determine person's new location at distance and direction.
                walkInDirection(direction, distanceMeters);

                distanceMeters = 0D;
            }
        }

        // If path destination is reached, end task.
        if (getRemainingPathDistance() <= VERY_SMALL_DISTANCE) {

			if (person != null) {
	            logger.fine(person.getName() + " finished walking to new location outside.");
	            Point2D finalLocation = walkingPath.get(walkingPath.size() - 1);
	            person.setXLocation(finalLocation.getX());
	            person.setYLocation(finalLocation.getY());
			}
			else if (robot != null) {
	            logger.fine(robot.getName() + " finished walking to new location outside.");
	            Point2D finalLocation = walkingPath.get(walkingPath.size() - 1);
	            robot.setXLocation(finalLocation.getX());
	            robot.setYLocation(finalLocation.getY());
			}

            endTask();
        }

        return timeLeft;
    }


    /**
     * Checks if there is an EVA problem for a person.
     * @param person the person.
     * @return true if an EVA problem.
     */
    public boolean checkEVAProblem(Person person) {	
    	return EVAOperation.checkEVAProblem(person);
    }


    /**
     * Check for radiation exposure of the person performing this EVA.
     * @param time the amount of time on EVA (in millisols)

    protected void isRadiationDetected(double time) {

    	if (person != null) {
    	    int millisols =  (int) marsClock.getMillisol();
    		// Check every RADIATION_CHECK_FREQ (in millisols)
    	    // Compute whether a baseline, GCR, or SEP event has occurred
    	    // Note : remainder = millisols % RadiationExposure.RADIATION_CHECK_FREQ ;
    	    if (millisols % RadiationExposure.RADIATION_CHECK_FREQ == 0)
    	    	person.getPhysicalCondition().getRadiationExposure().isRadiationDetected(time);

    	} else if (robot != null) {

    	}
    }
*/

    /**
     * Gets the person's EVA walking speed.
     * @return walking speed (Km/hr).
     */
    private double getWalkingSpeed() {

        double result = BASE_WALKING_SPEED;

        // Modify walking speed by EVA operations skill level.
        double evaSkill = getEffectiveSkillLevel();
        double speedModifyer = evaSkill * .3D;
        result += speedModifyer;

        // Don't allow walking speed to exceed maximum walking speed.
        if (result > MAX_WALKING_SPEED) {
            result = MAX_WALKING_SPEED;
        }

        return result;
    }

    /**
     * Determine the direction of travel to a location.
     * @param destinationXLocation the destination X location.
     * @param destinationYLocation the destination Y location.
     * @return direction (radians).
     */
    private double determineDirection(double destinationXLocation, double destinationYLocation) {
    	double result = 0;

		if (person != null) {
	        result = Math.atan2(person.getXLocation() - destinationXLocation,
	                destinationYLocation - person.getYLocation());
		}
		else if (robot != null) {
	        result = Math.atan2(robot.getXLocation() - destinationXLocation,
	                destinationYLocation - robot.getYLocation());
		}



        while (result > (Math.PI * 2D)) {
            result -= (Math.PI * 2D);
        }

        while (result < 0D) {
            result += (Math.PI * 2D);
        }

        return result;
    }

    /**
     * Walk in a given direction for a given distance.
     * @param direction the direction (radians) of travel.
     * @param distance the distance (meters) to travel.
     */
    private void walkInDirection(double direction, double distance) {

		if (person != null) {

	        double newXLoc = (-1D * Math.sin(direction) * distance) + person.getXLocation();
	        double newYLoc = (Math.cos(direction) * distance) + person.getYLocation();

	        person.setXLocation(newXLoc);
	        person.setYLocation(newYLoc);

		}
		else if (robot != null) {

	        double newXLoc = (-1D * Math.sin(direction) * distance) + robot.getXLocation();
	        double newYLoc = (Math.cos(direction) * distance) + robot.getYLocation();

	        robot.setXLocation(newXLoc);
	        robot.setYLocation(newYLoc);

		}

    }

    /**
     * Gets the remaining path distance.
     * @return distance (meters).
     */
    private double getRemainingPathDistance() {

        double result = 0D;

        double prevXLoc = 0;
        double prevYLoc = 0;

		if (person != null) {
			prevXLoc = person.getXLocation();
	        prevYLoc = person.getYLocation();
		}
		else if (robot != null) {
			prevXLoc = robot.getXLocation();
	        prevYLoc = robot.getYLocation();
		}


        for (int x = walkingPathIndex; x < walkingPath.size(); x++) {
            Point2D nextLoc = walkingPath.get(x);
            double distance = Point2D.Double.distance(prevXLoc, prevYLoc,
                    nextLoc.getX(), nextLoc.getY());
            result += distance;
            prevXLoc = nextLoc.getX();
            prevYLoc = nextLoc.getY();
        }

        return result;
    }

    /**
     * Check for accident with EVA suit.
     * @param time the amount of time on EVA (in millisols)
     */
    private void checkForAccident(double time) {

//		if (person != null) {
    	EVASuit suit = (EVASuit) person.getInventory().findUnitOfClass(EVASuit.class);
        if (suit != null) {

            double chance = BASE_ACCIDENT_CHANCE;

            // EVA operations skill modification.
            int skill = person.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
            if (skill <= 3) chance *= (4 - skill);
            else chance /= (skill - 2);

            // Modify based on the suit's wear condition.
            chance *= suit.getMalfunctionManager().getWearConditionAccidentModifier();

            if (RandomUtil.lessThanRandPercent(chance * time)) {
//                logger.info(person.getName() + " has an accident during EVA.");
                suit.getMalfunctionManager().createASeriesOfMalfunctions("EVA", person);
            }
        }
//		}
//		else if (robot != null) {		
//		       EVASuit suit = (EVASuit) robot.getInventory().findUnitOfClass(EVASuit.class);
//		        if (suit != null) {
//
//		            double chance = BASE_ACCIDENT_CHANCE;
//
//		            // EVA operations skill modification.
//		            int skill = robot.getBotMind().getSkillManager().getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
//		            if (skill <= 3) chance *= (4 - skill);
//		            else chance /= (skill - 2);
//
//		            // Modify based on the suit's wear condition.
//		            chance *= suit.getMalfunctionManager().getWearConditionAccidentModifier();
//
//		            if (RandomUtil.lessThanRandPercent(chance * time)) {
//		                logger.fine(robot.getName() + " has accident during EVA walking.");
//		                suit.getMalfunctionManager().createASeriesOfMalfunctions(robot);
//		            }
//		        } 
//		}
    }

    @Override
    public int getEffectiveSkillLevel() {
        SkillManager manager = null;
		if (person != null) {
	        manager = person.getMind().getSkillManager();
		}
		else if (robot != null) {
	        manager = robot.getBotMind().getSkillManager();
		}

        int EVAOperationsSkill = manager.getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
        return EVAOperationsSkill;
    }

    @Override
    public List<SkillType> getAssociatedSkills() {
        List<SkillType> results = new ArrayList<SkillType>(2);
        results.add(SkillType.EVA_OPERATIONS);
        return results;
    }

    @Override
    protected void addExperience(double time) {

        // Add experience to "EVA Operations" skill.
        // (1 base experience point per 100 millisols of time spent)
        double evaExperience = time / 100D;
        NaturalAttributeManager nManager = null;
        RoboticAttributeManager rManager = null;
        int experienceAptitude = 0;
        if (person != null) {
            nManager = person.getNaturalAttributeManager();
            experienceAptitude = nManager.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
        }
        else if (robot != null) {
        	rManager = robot.getRoboticAttributeManager();
            experienceAptitude = rManager.getAttribute(RoboticAttributeType.EXPERIENCE_APTITUDE);
        }

        double experienceAptitudeModifier = (((double) experienceAptitude) - 50D) / 100D;
        evaExperience += evaExperience * experienceAptitudeModifier;
        evaExperience *= getTeachingExperienceModifier();
		if (person != null)
	        person.getMind().getSkillManager().addExperience(SkillType.EVA_OPERATIONS, evaExperience);
		else if (robot != null)
        	robot.getBotMind().getSkillManager().addExperience(SkillType.EVA_OPERATIONS, evaExperience);
    }
}
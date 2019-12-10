/**
` * Mars Simulation Project
 * WalkSettlementInterior.java
 * @version 3.1.0 2017-01-19
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
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
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * A subtask for walking between two interior locations in a settlement. (Ex:
 * Between two connected inhabitable buildings or two locations in a single
 * inhabitable building.)
 */
public class WalkSettlementInterior extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(WalkSettlementInterior.class.getName());

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.walkSettlementInterior"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase WALKING = new TaskPhase(Msg.getString("Task.phase.walking")); //$NON-NLS-1$

	// Static members
	private static final double PERSON_WALKING_SPEED = Walk.PERSON_WALKING_SPEED; // [km per hr].
	private static final double ROBOT_WALKING_SPEED = Walk.ROBOT_WALKING_SPEED; // [km per hr].

	private static final double VERY_SMALL_DISTANCE = .00001D;
	private static final double STRESS_MODIFIER = -.2D;

	// Data members
	private double destXLoc;
	private double destYLoc;
	private double destZLoc;
	
	private Settlement settlement;
	// private Building startBuilding;
	private Building destBuilding;
	private InsideBuildingPath walkingPath;

	/**
	 * Constructor.
	 * 
	 * @param person               the person performing the task.
	 * @param destinationBuilding  the building that is walked to. (Can be same as
	 *                             current building).
	 * @param destinationXLocation the destination X location at the settlement.
	 * @param destinationYLocation the destination Y location at the settlement.
	 */
	public WalkSettlementInterior(Person person, Building destinationBuilding, double destinationXLocation,
			double destinationYLocation, double destinationZLocation) {
		super(NAME, person, false, false, STRESS_MODIFIER, false, 0D);

		// Check that the person is currently inside the settlement.
		if (!person.isInSettlement()) {
//			throw new IllegalStateException(
			LogConsolidated.log(Level.WARNING, 20_000, sourceName, 
					"[" + person.getLocationTag().getLocale() + "] "
					+ person + "WalkSettlementInterior task started when person is not in settlement.");
			person.getMind().getTaskManager().clearAllTasks();
			return;
		}

		// Initialize data members.
		this.settlement = person.getSettlement();
		this.destBuilding = destinationBuilding;
		this.destXLoc = destinationXLocation;
		this.destYLoc = destinationYLocation;
		this.destZLoc = destinationZLocation;
		
		// Check that destination location is within destination building.
		if (!LocalAreaUtil.checkLocationWithinLocalBoundedObject(destXLoc, destYLoc, destBuilding)) {
//			throw new IllegalStateException(
			LogConsolidated.log(Level.WARNING, 20_000, sourceName, 
					"[" + person.getLocationTag().getLocale() + "] "
					+ person + " was unable to walk to the destination in " + person.getBuildingLocation());
			person.getMind().getTaskManager().clearAllTasks();
			return;
		}

		// Check that the person is currently inside a building.
		Building startBuilding = BuildingManager.getBuilding(person);
		if (startBuilding == null) {
//			throw new IllegalStateException(
			LogConsolidated.log(Level.WARNING, 20_000, sourceName,		
					"[" + person.getLocationTag().getLocale() + "] "
					+person.getName() + " is not currently in a building.");
			person.getMind().getTaskManager().clearAllTasks();
			return;
		}

		try {
			// Determine the walking path to the destination.
	//		System.out.println("WalkSettlementInterior : settlement : " + settlement);
	//		System.out.println("WalkSettlementInterior : person's settlement : " + person.getSettlement());
			if (settlement != null)
				walkingPath = settlement.getBuildingConnectorManager().determineShortestPath(startBuilding,
					person.getXLocation(), person.getYLocation(), destinationBuilding, destinationXLocation,
					destinationYLocation);
	
			// If no valid walking path is found, end task.
			if (walkingPath == null) {
				LogConsolidated.log(Level.WARNING, 20_000, sourceName, 
						"[" + person.getLocationTag().getLocale() + "] "
	//							+ person.getName() + " was unable to walk from " + startBuilding.getNickName() + " to "
	//									+ destinationBuilding.getNickName() + ". No valid interior path.");
								+ person.getName() + " was unable to walk. No valid interior path.");
				person.getMind().getTaskManager().clearAllTasks();
				return;
				// TODO: if it's the astronomy observatory building, it will call it thousands of time
				// e.g (Warning) [x23507] WalkSettlementInterior : Jani Patokallio unable to walk from Lander Hab 2 to Astronomy Observatory 1.  Unable to find valid interior path.
	//			person.getMind().getTaskManager().getNewTask();
			}
			
			LogConsolidated.log(Level.FINER, 20_000, sourceName, "[" + person.getLocationTag().getLocale() + "] "
					+ person.getName() + " proceeded to the walking phase in WalkSettlementInterior.");
			
			// Initialize task phase.
			addPhase(WALKING);
			setPhase(WALKING);
		
		} catch (StackOverflowError ex) {
			ex.printStackTrace();
			LogConsolidated.log(Level.WARNING, 20_000, sourceName, "[" + person.getLocationTag().getLocale() + "] "
//					+ person.getName() + " was unable to walk from " + startBuilding.getNickName() + " to "
//							+ destinationBuilding.getNickName() + ". No valid interior path.");
					+ person.getName() + " was unable to walk. No valid interior path.");
			person.getMind().getTaskManager().clearAllTasks();
		}
	}

	public WalkSettlementInterior(Robot robot, Building destinationBuilding, double destinationXLocation,
			double destinationYLocation) {
		super("Walking Settlement Interior", robot, false, false, STRESS_MODIFIER, false, 0D);

		// Check that the robot is currently inside the settlement.
//        LocationSituation location = robot.getLocationSituation();
//        if (location != LocationSituation.IN_SETTLEMENT) {
		if (!robot.isInSettlement()) {
			throw new IllegalStateException("WalkSettlementInterior task started when robot is not in settlement.");
		}

		// Initialize data members.
		this.settlement = robot.getSettlement();
		this.destBuilding = destinationBuilding;
		this.destXLoc = destinationXLocation;
		this.destYLoc = destinationYLocation;
		
		// Check that destination location is within destination building.
		if (!LocalAreaUtil.checkLocationWithinLocalBoundedObject(destXLoc, destYLoc, destBuilding)) {
			LogConsolidated.log(Level.WARNING, 20_000, sourceName,
					robot + " was unable to walk to the destination in " + robot.getBuildingLocation() + " at "
							+ robot.getSettlement());
			// throw new IllegalStateException(
			// "Given destination walking location not within destination building.");
			endTask();
//			robot.getBotMind().getBotTaskManager().getNewTask();
			return;
		}

		// Check that the robot is currently inside a building.
		Building startBuilding = BuildingManager.getBuilding(robot);
		if (startBuilding == null) {
			// throw new IllegalStateException(robot.getName() + " is not currently in a
			// building.");

			// Note: the above will trigger exception below and halt the sim
			// Exception in thread "pool-4-thread-3024" java.lang.IllegalStateException:
			// RepairBot 003 is not currently in a building.
			// at
			// org.mars_sim.msp.core.person.ai.task.WalkSettlementInterior.<init>(WalkSettlementInterior.java:145)
			// at
			// org.mars_sim.msp.core.person.ai.task.EnterAirlock.exitingAirlockPhase(EnterAirlock.java:549)
			// at
			// org.mars_sim.msp.core.person.ai.task.EnterAirlock.performMappedPhase(EnterAirlock.java:129)

			// Question: can we use a gentler approach as follows until it's clearly
			// understood and resolved.
			// logger.severe(robot.getName() + " is not currently in a building.");
			// endTask();
//			throw new IllegalStateException(robot.getName() + " is not currently in a building.");
			return;
		}

		// Determine the walking path to the destination.
		walkingPath = settlement.getBuildingConnectorManager().determineShortestPath(startBuilding,
				robot.getXLocation(), robot.getYLocation(), destinationBuilding, destinationXLocation,
				destinationYLocation);

		// If no valid walking path is found, end task.
		if (walkingPath == null) {
			LogConsolidated
					.log(Level.WARNING, 20_000, sourceName,
							robot.getName() + " was unable to walk from " + startBuilding.getNickName() + " to "
									+ destinationBuilding.getNickName() + ". No valid interior path.");
			endTask();
//			robot.getBotMind().getBotTaskManager().getNewTask();
			return;
		}

		// Initialize task phase.
		addPhase(WALKING);
		setPhase(WALKING);
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {

			if (person != null) {
				LogConsolidated.log(Level.SEVERE, 2000, sourceName,
						person.getName() + " at " + person.getBuildingLocation() + " : Task phase is null");
			} else if (robot != null) {
				LogConsolidated.log(Level.SEVERE, 2000, sourceName,
						robot.getName() + " at " + robot.getBuildingLocation() + " : Task phase is null");
			}
			// endTask();
			throw new IllegalArgumentException("Task phase is null");
			// return 0;
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
	double walkingPhase(double time) {
		double timeHours = MarsClock.HOURS_PER_MILLISOL * time;
		double distanceKm = 0;

		if (person != null) {
			person.caculateWalkSpeedMod();
			double mod = person.getWalkSpeedMod();
//			System.out.println("mod : " + mod);
			distanceKm = PERSON_WALKING_SPEED * timeHours * mod;
			// Check that remaining path locations are valid.
			if (!checkRemainingPathLocations()) {
				// Exception in thread "pool-4-thread-1" java.lang.StackOverflowError
				// Flooding with the following statement in stacktrace
				LogConsolidated.log(Level.SEVERE, 1000, sourceName,
						person.getName() + " was unable to continue walking due to missing path objects.");
				// endTask();
				return time / 2D;
			}
		} else if (robot != null) {
			double mod = robot.getWalkSpeedMod();
			distanceKm = ROBOT_WALKING_SPEED * timeHours * mod;
			// Check that remaining path locations are valid.
			if (!checkRemainingPathLocations()) {
				// Exception in thread "pool-4-thread-1" java.lang.StackOverflowError
				// Flooding with the following statement in stacktrace
				LogConsolidated.log(Level.SEVERE, 1000, sourceName,
						robot.getName() + " was unable to continue walking due to missing path objects.");
				// endTask();
				return time / 2D;
			}
		}

		// Determine walking distance.
		double distanceMeters = distanceKm * 1000D;
		double remainingPathDistance = getRemainingPathDistance();

		// Determine time left after walking.
		double timeLeft = 0D;
		if (distanceMeters > remainingPathDistance) {
			double overDistance = distanceMeters - remainingPathDistance;
			timeLeft = MarsClock.MILLISOLS_PER_HOUR * overDistance / PERSON_WALKING_SPEED / 1000D; //MarsClock.convertSecondsToMillisols(overDistance / 1000D / PERSON_WALKING_SPEED * 60D * 60D); ?
			distanceMeters = remainingPathDistance;
		}

		while (distanceMeters > VERY_SMALL_DISTANCE) {

			// Walk to next path location.
			InsidePathLocation location = walkingPath.getNextPathLocation();
			double distanceToLocation = 0;
			if (person != null) {
				distanceToLocation = Point2D.distance(person.getXLocation(), person.getYLocation(),
						location.getXLocation(), location.getYLocation());

			} else if (robot != null) {
				distanceToLocation = Point2D.distance(robot.getXLocation(), robot.getYLocation(),
						location.getXLocation(), location.getYLocation());
			}

			if (distanceMeters >= distanceToLocation) {

				if (person != null) {
					// Set person at next path location, changing buildings if necessary.
					person.setXLocation(location.getXLocation());
					person.setYLocation(location.getYLocation());

				} else if (robot != null) {
					// Set robot at next path location, changing buildings if necessary.
					robot.setXLocation(location.getXLocation());
					robot.setYLocation(location.getYLocation());

				}

				distanceMeters -= distanceToLocation;
				
				changeBuildings(location);
				
				if (!walkingPath.isEndOfPath()) {
					walkingPath.iteratePathLocation();
				}
			} 
			
			else {
				// Walk in direction of next path location.
				// Determine direction
				double direction = determineDirection(location.getXLocation(), location.getYLocation());
				// Determine person's new location at distance and direction.
				walkInDirection(direction, distanceMeters);

				distanceMeters = 0D;
			}
		}

		// If path destination is reached, end task.
		if (getRemainingPathDistance() <= VERY_SMALL_DISTANCE) {

			if (person != null) {
				Building startBuilding = BuildingManager.getBuilding(person);
				if (startBuilding == null || destBuilding == null) {
					Vehicle v = person.getVehicle();
					logger.fine("WalkSettlementInterior : [" + person.getLocationTag().getLocale()
							+ "] " + person + " in " + person.getLocationTag().getImmediateLocation() 
							+ " [vehicle : " + v + "] [start : " + startBuilding
							+ "] [destination :  " + destBuilding + "]");
					// e.g. WalkSettlementInterior : [Alpha Base vicinity] Jason Slack - [vehicle : null] [start : null] [destination :  Garage 1]
					// (Warning) ExitAirlock : [0.0° N 0.0° W] Jason Slack was about to enter the airlock within the settlement, but was already outside. End task.
				} 
//				else {
					// logger.finer(person.getName() + " walked from " + startBuilding.getNickName()
					// + " to " +
					// destBuilding.getNickName());
					InsidePathLocation location = walkingPath.getNextPathLocation();
					person.setXLocation(location.getXLocation());
					person.setYLocation(location.getYLocation());
//				}
			} else if (robot != null) {
				// Building startBuilding = BuildingManager.getBuilding(robot);
				// logger.finer(robot.getName() + " walked from " + startBuilding.getNickName()
				// + " to " +
				// destBuilding.getNickName());
				InsidePathLocation location = walkingPath.getNextPathLocation();
				robot.setXLocation(location.getXLocation());
				robot.setYLocation(location.getYLocation());
			}

			endTask();
		}

		return timeLeft;
	}

	/**
	 * Determine the direction of travel to a location.
	 * 
	 * @param destinationXLocation the destination X location.
	 * @param destinationYLocation the destination Y location.
	 * @return direction (radians).
	 */
	double determineDirection(double destinationXLocation, double destinationYLocation) {
		double result = 0;

		if (person != null) {
			result = Math.atan2(person.getXLocation() - destinationXLocation,
					destinationYLocation - person.getYLocation());
		} else if (robot != null) {
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
	 * 
	 * @param direction the direction (radians) of travel.
	 * @param distance  the distance (meters) to travel.
	 */
	void walkInDirection(double direction, double distance) {

		if (person != null) {
			double newXLoc = (-1D * Math.sin(direction) * distance) + person.getXLocation();
			double newYLoc = (Math.cos(direction) * distance) + person.getYLocation();

			person.setXLocation(newXLoc);
			person.setYLocation(newYLoc);
		} else if (robot != null) {
			double newXLoc = (-1D * Math.sin(direction) * distance) + robot.getXLocation();
			double newYLoc = (Math.cos(direction) * distance) + robot.getYLocation();

			robot.setXLocation(newXLoc);
			robot.setYLocation(newYLoc);
		}

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
					// System.out.println("building is NOT in BuildingManager");
					return false;
					// result = false;
				}
			} else if (loc instanceof BuildingLocation) {
				// Check that building still exists.
				BuildingLocation buildingLoc = (BuildingLocation) loc;
				Building building = buildingLoc.getBuilding();
				if (!settlement.getBuildingManager().containsBuilding(building)) {
					return false;
					// result = false;
				}
			} else if (loc instanceof BuildingConnector) {
				// Check that building connector still exists.
				BuildingConnector connector = (BuildingConnector) loc;
				if (!settlement.getBuildingConnectorManager().containsBuildingConnector(connector)) {
					return false;
					// result = false;
				}
			} else if (loc instanceof Hatch) {
				// Check that building connector for hatch still exists.
				Hatch hatch = (Hatch) loc;
				BuildingConnector connector = hatch.getBuildingConnector();
				if (!settlement.getBuildingConnectorManager().containsBuildingConnector(connector)) {
					return false;
					// result = false;
				}
			}
		}

		return true;
		// return result;
	}

	/**
	 * Gets the remaining path distance.
	 * 
	 * @return distance (meters).
	 */
	private double getRemainingPathDistance() {

		double result = 0D;
		double prevXLoc = 0;
		double prevYLoc = 0;

		if (person != null) {
			prevXLoc = person.getXLocation();
			prevYLoc = person.getYLocation();

		} else if (robot != null) {
			prevXLoc = robot.getXLocation();
			prevYLoc = robot.getYLocation();

		}

		Iterator<InsidePathLocation> i = walkingPath.getRemainingPathLocations().iterator();
		while (i.hasNext()) {
			InsidePathLocation nextLoc = i.next();
			result += Point2D.Double.distance(prevXLoc, prevYLoc, nextLoc.getXLocation(), nextLoc.getYLocation());
			prevXLoc = nextLoc.getXLocation();
			prevYLoc = nextLoc.getYLocation();
		}

		return result;
	}

	/**
	 * Change the person's current building to a new one if necessary.
	 * 
	 * @param location the path location the person has reached.
	 */
	private void changeBuildings(InsidePathLocation location) {

		if (location instanceof Hatch) {
			// If hatch leads to new building, place person in the new building.
			Hatch hatch = (Hatch) location;

			if (person != null) {
				Building currentBuilding = BuildingManager.getBuilding(person);
				if (!hatch.getBuilding().equals(currentBuilding)) {
					BuildingManager.addPersonOrRobotToBuilding(person, hatch.getBuilding());
				}
			} 
			
			else if (robot != null) {
				Building currentBuilding = BuildingManager.getBuilding(robot);
				if (!hatch.getBuilding().equals(currentBuilding)) {
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
					logger.severe("Connector from " + connector.getBuilding1() + " to " + connector.getBuilding2()
							+ " was not connected to current building " + currentBuilding);
//					throw new IllegalStateException("Connector not connected to " + currentBuilding);
				}

				if (newBuilding != null) {
					
					if (person != null)
						BuildingManager.addPersonOrRobotToBuilding(person, newBuilding);
					else if (robot != null)
						BuildingManager.addPersonOrRobotToBuilding(robot, newBuilding);
				}
			}
		}
	}

	@Override
	public int getEffectiveSkillLevel() {
		return 0;
	}

	@Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<SkillType>(0);
		return results;
	}

	@Override
	protected void addExperience(double time) {
		// This task adds no experience.
	}

	@Override
	public void destroy() {
		super.destroy();

		destBuilding = null;
		if (walkingPath != null) {
			walkingPath.destroy();
			walkingPath = null;
		}
	}
}
/**
 * Mars Simulation Project
 * DriveGroundVehicle.java
 * @version 3.1.0 2017-09-13
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Direction;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RoboticAttributeType;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.GroundVehicle;

/**
 * The Drive Ground Vehicle class is a task for driving a ground vehicle to a
 * destination.
 */
public class DriveGroundVehicle extends OperateVehicle implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(DriveGroundVehicle.class.getName());

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.driveGroundVehicle"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase AVOID_OBSTACLE = new TaskPhase(Msg.getString("Task.phase.avoidObstacle")); //$NON-NLS-1$
	private static final TaskPhase WINCH_VEHICLE = new TaskPhase(Msg.getString("Task.phase.winchVehicle")); //$NON-NLS-1$

	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .2D;
	/** Half the PI. */
	private static final double HALF_PI = Math.PI / 2D;
	
	// Side directions.
	private final static int NONE = 0;
	private final static int LEFT = 1;
	private final static int RIGHT = 2;

	// Data members
	private int sideDirection = NONE;
	/** The person performing the task. */
	protected Person person;
	/** The robot performing the task. */
	protected Robot robot;
	
	/**
	 * Default Constructor.
	 * 
	 * @param person            the person to perform the task
	 * @param vehicle           the vehicle to be driven
	 * @param destination       location to be driven to
	 * @param startTripTime     the starting time of the trip
	 * @param startTripDistance the starting distance to destination for the trip
	 */
	public DriveGroundVehicle(Person person, GroundVehicle vehicle, Coordinates destination, MarsClock startTripTime,
			double startTripDistance) {

		// Use OperateVehicle constructor
		super(NAME, person, vehicle, destination, startTripTime, startTripDistance, STRESS_MODIFIER, true,
				(300D + RandomUtil.getRandomDouble(20D)));

		this.person = person;
		
		// Set initial parameters
		setDescription(Msg.getString("Task.description.driveGroundVehicle.detail", vehicle.getName())); // $NON-NLS-1$
		addPhase(AVOID_OBSTACLE);
		addPhase(WINCH_VEHICLE);

		LogConsolidated.log(logger, Level.INFO, 20_000, sourceName,
				"[" + person.getLocationTag().getLocale() + "] " + person.getName() + " took the wheel of rover "
//						+ (person.getGender() == GenderType.MALE ? "his" : "her") + " driving " 
						+ vehicle.getName()
						+ ".",
				null);
	}

	public DriveGroundVehicle(Robot robot, GroundVehicle vehicle, Coordinates destination, MarsClock startTripTime,
			double startTripDistance) {

		// Use OperateVehicle constructor
		super(NAME, robot, vehicle, destination, startTripTime, startTripDistance, STRESS_MODIFIER, true,
				(300D + RandomUtil.getRandomDouble(20D)));

		this.robot = robot;
		
		// Set initial parameters
		setDescription(Msg.getString("Task.description.driveGroundVehicle.detail", vehicle.getName())); // $NON-NLS-1$
		addPhase(AVOID_OBSTACLE);
		addPhase(WINCH_VEHICLE);

		LogConsolidated.log(logger, Level.INFO, 20_000, sourceName,
				"[" + robot.getLocationTag().getLocale() + "] " + robot.getName() + " took the wheel of rover " 
						+ vehicle.getName()
						+ ".",
				null);
	}

	/**
	 * Constructs with a given starting phase.
	 * 
	 * @param person            the person to perform the task
	 * @param vehicle           the vehicle to be driven
	 * @param destination       location to be driven to
	 * @param startTripTime     the starting time of the trip
	 * @param startTripDistance the starting distance to destination for the trip
	 * @param startingPhase     the starting phase for the task
	 */
	public DriveGroundVehicle(Person person, GroundVehicle vehicle, Coordinates destination, MarsClock startTripTime,
			double startTripDistance, TaskPhase startingPhase) {

		// Use OperateVehicle constructor
		super(NAME, person, vehicle, destination, startTripTime, startTripDistance, STRESS_MODIFIER, true,
				(100D + RandomUtil.getRandomDouble(20D)));

		this.person = person;
		
		// Set initial parameters
		setDescription(Msg.getString("Task.description.driveGroundVehicle.detail", vehicle.getName())); // $NON-NLS-1$
		addPhase(AVOID_OBSTACLE);
		addPhase(WINCH_VEHICLE);
		if (startingPhase != null)
			setPhase(startingPhase);

		LogConsolidated.log(logger, Level.INFO, 20_000, sourceName,
				"[" + person.getLocationTag().getLocale() + "] " + person.getName() + " took the wheel of rover "
//						+ (person.getGender() == GenderType.MALE ? "his" : "her") + " driving " 
						+ vehicle.getName()
						+ " at the starting phase of '" + startingPhase + "'.",
				null);

	}

	public DriveGroundVehicle(Robot robot, GroundVehicle vehicle, Coordinates destination, MarsClock startTripTime,
			double startTripDistance, TaskPhase startingPhase) {

		// Use OperateVehicle constructor
		super(NAME, robot, vehicle, destination, startTripTime, startTripDistance, STRESS_MODIFIER, true,
				(100D + RandomUtil.getRandomDouble(20D)));

		this.robot = robot;
		
		// Set initial parameters
		setDescription(Msg.getString("Task.description.driveGroundVehicle.detail", vehicle.getName())); // $NON-NLS-1$
		addPhase(AVOID_OBSTACLE);
		addPhase(WINCH_VEHICLE);
		if (startingPhase != null)
			setPhase(startingPhase);

		LogConsolidated.log(logger, Level.INFO, 20_000, sourceName,
				"[" + robot.getLocationTag().getLocale() + "] " + robot.getName() + " took the wheel of rover "
						+ vehicle.getName()
						+ " at the starting phase of '" + startingPhase + "'.",
				null);
	}

	/**
	 * Performs the method mapped to the task's current phase.
	 * 
	 * @param time the amount of time the phase is to be performed.
	 * @return the remaining time after the phase has been performed.
	 */
	protected double performMappedPhase(double time) {

		time = super.performMappedPhase(time);

		if (getPhase() == null) {
//			throw new IllegalArgumentException("Task phase is null");
			LogConsolidated.log(Level.INFO, 10_000, sourceName, "[" + person.getLocationTag().getLocale() + "] "
					+ person.getName() + " had an unknown phase when driving " 
					+ getVehicle().getName() + ".");
			// If it called endTask() in OperateVehicle, then Task is no longer available
			// WARNING: do NOT call endTask() here or it will end up calling endTask() 
			// recursively.
			return time;		
			
		} else if (AVOID_OBSTACLE.equals(getPhase())) {
			return obstaclePhase(time);
		} else if (WINCH_VEHICLE.equals(getPhase())) {
			return winchingPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Move the vehicle in its direction at its speed for the amount of time given.
	 * Stop if reached destination.
	 * 
	 * @param time the amount of time (ms) to drive.
	 * @return the amount of time (ms) left over after driving (if any)
	 */
	protected double mobilizeVehicle(double time) {

		// If vehicle is stuck, try winching.
		if (((GroundVehicle) getVehicle()).isStuck() && (!WINCH_VEHICLE.equals(getPhase()))) {
			setPhase(WINCH_VEHICLE);
			return (time);
		}

		// If speed is less the 1 kph, change to avoiding obstacle phase.
		if ((getVehicle().getSpeed() < 1D) && (!AVOID_OBSTACLE.equals(getPhase()))
				&& (!WINCH_VEHICLE.equals(getPhase()))) {
			setPhase(AVOID_OBSTACLE);
			return (time);
		} else
			return super.mobilizeVehicle(time);
	}

	/**
	 * Perform task in obstacle phase.
	 * 
	 * @param time the amount of time to perform the task (in millisols)
	 * @return time remaining after performing phase (in millisols)
	 */
	private double obstaclePhase(double time) {

		double timeUsed = 0D;
		GroundVehicle vehicle = (GroundVehicle) getVehicle();

		// Update vehicle elevation.
		updateVehicleElevationAltitude();

		// Get the direction to the destination.
		Direction destinationDirection = vehicle.getCoordinates().getDirectionToPoint(getDestination());

		// If speed in destination direction is good, change to mobilize phase.
		double destinationSpeed = getSpeed(destinationDirection);
		if (destinationSpeed >= 1D) {
			vehicle.setDirection(destinationDirection);
			setPhase(OperateVehicle.MOBILIZE);
			sideDirection = NONE;
			return time;
		}

		// Determine the direction to avoid the obstacle.
		Direction travelDirection = getObstacleAvoidanceDirection();

		// If an obstacle avoidance direction could not be found, winch vehicle.
		if (travelDirection == null) {
			setPhase(WINCH_VEHICLE);
			sideDirection = NONE;
			return time;
		}

		// Set the vehicle's direction.
		vehicle.setDirection(travelDirection);

		// Update vehicle speed.
		vehicle.setSpeed(getSpeed(vehicle.getDirection()));

		// Drive in the direction
		timeUsed = time - mobilizeVehicle(time);

		// Add experience points
		addExperience(time);

		// Check for accident.
		if (!isDone())
			checkForAccident(timeUsed);

		// If vehicle has malfunction, end task.
		// if (malfunctionManager == null)
		// malfunctionManager = vehicle.getMalfunctionManager();
		if (vehicle.getMalfunctionManager().hasMalfunction())
			endTask();

		return time - timeUsed;
	}

	/**
	 * Perform task in winching phase.
	 * 
	 * @param time the amount of time to perform the phase.
	 * @return time remaining after performing the phase.
	 */
	private double winchingPhase(double time) {

		double timeUsed = 0D;
		GroundVehicle vehicle = (GroundVehicle) getVehicle();

		// Find current direction and update vehicle.
		vehicle.setDirection(vehicle.getCoordinates().getDirectionToPoint(getDestination()));

		// Update vehicle elevation.
		updateVehicleElevationAltitude();

		// If speed given the terrain would be better than 1kph, return to normal
		// driving.
		// Otherwise, set speed to .2kph for winching speed.
		if (getSpeed(vehicle.getDirection()) > 1D) {
			setPhase(OperateVehicle.MOBILIZE);
			vehicle.setStuck(false);
			return (time);
		} else
			vehicle.setSpeed(.2D);

		// Drive in the direction
		timeUsed = time - mobilizeVehicle(time);

		// Add experience points
		addExperience(time);

		// Check for accident.
		if (!isDone())
			checkForAccident(timeUsed);

		// If vehicle has malfunction, end task.
		// if (malfunctionManager == null)
		// malfunctionManager = vehicle.getMalfunctionManager();
		if (vehicle.getMalfunctionManager().hasMalfunction())
			endTask();

		return time - timeUsed;
	}

	/**
	 * Gets the direction for obstacle avoidance.
	 * 
	 * @return direction for obstacle avoidance in radians or null if none found.
	 */
	private Direction getObstacleAvoidanceDirection() {
		Direction result = null;

		GroundVehicle vehicle = (GroundVehicle) getVehicle();
		boolean foundGoodPath = false;

		double initialDirection = vehicle.getCoordinates().getDirectionToPoint(getDestination()).getDirection();

		if (sideDirection == NONE) {
			for (int x = 1; (x < 11) && !foundGoodPath; x++) {
				double modAngle = x * (Math.PI / 10D);
				for (int y = 1; (y < 3) && !foundGoodPath; y++) {
					Direction testDirection = null;
					if (y == 1)
						testDirection = new Direction(initialDirection - modAngle);
					else
						testDirection = new Direction(initialDirection + modAngle);
					double testSpeed = getSpeed(testDirection);
					if (testSpeed > 1D) {
						result = testDirection;
						if (y == 1)
							sideDirection = LEFT;
						else
							sideDirection = RIGHT;
						foundGoodPath = true;
					}
				}
			}
		} else {
			for (int x = 1; (x < 21) && !foundGoodPath; x++) {
				double modAngle = x * (Math.PI / 10D);
				Direction testDirection = null;
				if (sideDirection == LEFT)
					testDirection = new Direction(initialDirection - modAngle);
				else
					testDirection = new Direction(initialDirection + modAngle);
				double testSpeed = getSpeed(testDirection);
				if (testSpeed > 1D) {
					result = testDirection;
					foundGoodPath = true;
				}
			}
		}

		return result;
	}

	/**
	 * Update vehicle with its current elevation or altitude.
	 */
	protected void updateVehicleElevationAltitude() {
		// Update vehicle elevation.
		((GroundVehicle) getVehicle()).setElevation(getVehicleElevation());
	}

	/**
	 * Determine vehicle speed for a given direction.
	 * 
	 * @param direction the direction of travel
	 * @return speed in km/hr
	 */
	protected double getSpeed(Direction direction) {
		double result = super.getSpeed(direction);
		result *= getSpeedLightConditionModifier();
		result *= getTerrainModifier(direction);
		return result;
	}

	/**
	 * Gets the lighting condition speed modifier.
	 * 
	 * @return speed modifier (0D - 1D)
	 */
	protected double getSpeedLightConditionModifier() {
		// Ground vehicles travel at 30% speed at night.
		double lightConditions = surfaceFeatures.getSunlightRatio(getVehicle().getCoordinates());
		double result = (lightConditions * .7D) + .3D;
		return result;
	}

	/**
	 * Gets the terrain speed modifier.
	 * 
	 * @param direction the direction of travel.
	 * @return speed modifier (0D - 1D)
	 */
	protected double getTerrainModifier(Direction direction) {
		GroundVehicle vehicle = (GroundVehicle) getVehicle();

		// Get vehicle's terrain handling capability.
		double handling = vehicle.getTerrainHandlingCapability();

		// Determine modifier.
		double angleModifier = handling + getEffectiveSkillLevel() - 10D;
		if (angleModifier < 0D)
			angleModifier = Math.abs(1D / angleModifier);
		double tempAngle = Math.abs(vehicle.getTerrainGrade(direction) / angleModifier);
		if (tempAngle > HALF_PI)
			tempAngle = HALF_PI;
		return Math.cos(tempAngle);
	}

	/**
	 * Check if vehicle has had an accident.
	 * 
	 * @param time the amount of time vehicle is driven (millisols)
	 */
	protected void checkForAccident(double time) {

		GroundVehicle vehicle = (GroundVehicle) getVehicle();

		double chance = OperateVehicle.BASE_ACCIDENT_CHANCE;

		// Driver skill modification.
		int skill = getEffectiveSkillLevel();
		if (skill <= 3)
			chance *= (4 - skill);
		else
			chance /= (skill - 2);

		// Get task phase modification.
		if (AVOID_OBSTACLE.equals(getPhase()))
			chance *= 1.2D;
		else if (WINCH_VEHICLE.equals(getPhase()))
			chance *= 1.3D;

		// Terrain modification.
		chance *= (1D + Math.sin(vehicle.getTerrainGrade()));

		// Vehicle handling modification.
		chance /= (1D + vehicle.getTerrainHandlingCapability());

		// Light condition modification.
		double lightConditions = surfaceFeatures.getSunlightRatio(vehicle.getCoordinates());
		chance *= (5D * (1D - lightConditions)) + 1D;
		if (chance < 0D) {
			chance = 0D;
		}

		// if (malfunctionManager == null)
		MalfunctionManager malfunctionManager = vehicle.getMalfunctionManager();
		// Modify based on the vehicle's wear condition.
		chance *= malfunctionManager.getWearConditionAccidentModifier();

//        System.out.println("chance*time : " + chance * time);

		if (RandomUtil.lessThanRandPercent(chance * time)) {

			if (person != null) {
				// logger.info(person.getName() + " has an accident while driving " +
				// vehicle.getName());
				malfunctionManager.createASeriesOfMalfunctions(vehicle.getName(), person);
			} else if (robot != null) {
				// logger.info(robot.getName() + " has an accident while driving " +
				// vehicle.getName());
				malfunctionManager.createASeriesOfMalfunctions(vehicle.getName(), robot);
			}

		}
	}

	/**
	 * Gets the effective skill level a person has at this task.
	 * 
	 * @return effective skill level
	 */
	public int getEffectiveSkillLevel() {
		SkillManager manager = null;
		if (person != null)
			manager = person.getSkillManager();
		else if (robot != null)
			manager = robot.getSkillManager();
		if (person == null) System.out.println("person : " + person);
		if (manager == null) System.out.println("manager : " + manager);
		return manager.getEffectiveSkillLevel(SkillType.PILOTING);
	}

	/**
	 * Gets a list of the skills associated with this task. May be empty list if no
	 * associated skills.
	 * 
	 * @return list of skills as strings
	 */
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<SkillType>(1);
		results.add(SkillType.PILOTING);
		return results;
	}

	/**
	 * Adds experience to the person's skills used in this task.
	 * 
	 * @param time the amount of time (ms) the person performed this task.
	 */
	protected void addExperience(double time) {
		// Add experience points for driver's 'Driving' skill.
		// Add one point for every 100 millisols.
		double newPoints = time / 100D;
		int experienceAptitude = 0;
		if (person != null)
			experienceAptitude = person.getNaturalAttributeManager()
					.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		else if (robot != null)
			experienceAptitude = robot.getRoboticAttributeManager()
					.getAttribute(RoboticAttributeType.EXPERIENCE_APTITUDE);

		newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
		newPoints *= getTeachingExperienceModifier();
		double phaseModifier = 1D;
		if (AVOID_OBSTACLE.equals(getPhase()))
			phaseModifier = 4D;
		newPoints *= phaseModifier;
		if (person != null)
			person.getSkillManager().addExperience(SkillType.PILOTING, newPoints, time);
		else if (robot != null)
			robot.getSkillManager().addExperience(SkillType.PILOTING, newPoints, time);

	}

	/**
	 * Ends the task and performs any final actions.
	 */
	public void endTask() {
		if (person != null)
			LogConsolidated.log(logger, Level.INFO, 20_000, sourceName, "[" + person.getLocationTag().getLocale() + "] "
					+ person.getName() + " took a break from driving " + getVehicle().getName() + ".", null);

		else if (robot != null)
			LogConsolidated.log(logger, Level.INFO, 20_000, sourceName, "[" + robot.getLocationTag().getLocale() + "] "
					+ robot.getName() +  " took a break from driving " + getVehicle().getName() + ".", null);

		// ((GroundVehicle) getVehicle()).setStuck(false);

		if (getVehicle() != null) {
			getVehicle().setSpeed(0D);
//	        VehicleOperator vo = getVehicle().getOperator();
	        if (getVehicle() != null)
		        // Need to set the vehicle operator to null before clearing the driving task 
	        	getVehicle().setOperator(null);
//	        if (vo != null)
//	        	clearDrivingTask(vo);
		}
		
		super.endTask();
	}
}
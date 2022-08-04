/**
 * Mars Simulation Project
 * DriveGroundVehicle.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.logging.Level;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Direction;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.function.Computation;
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
	private static final SimLogger logger = SimLogger.getLogger(DriveGroundVehicle.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.driveGroundVehicle"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase AVOID_OBSTACLE = new TaskPhase(Msg.getString("Task.phase.avoidObstacle")); //$NON-NLS-1$
	private static final TaskPhase WINCH_VEHICLE = new TaskPhase(Msg.getString("Task.phase.winchVehicle")); //$NON-NLS-1$

	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .2D;
	/** The computing resources [in CUs] needed per km. */
	private static final double CU_PER_KM = .05;
	
	// Side directions.
	private final static int NONE = 0;
	private final static int LEFT = 1;
	private final static int RIGHT = 2;

	// Data members
	private int sideDirection = NONE;
    /** Computing Units used per millisol. */		
	private double computingUsed = 0;
	
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
		super(NAME, person, vehicle, destination, startTripTime, startTripDistance, STRESS_MODIFIER, 
				(300D + RandomUtil.getRandomDouble(20D)));
		
		// Set initial parameters
		setDescription(Msg.getString("Task.description.driveGroundVehicle.detail", vehicle.getName())); // $NON-NLS-1$
		addPhase(AVOID_OBSTACLE);
		addPhase(WINCH_VEHICLE);

		logger.log(person, Level.INFO, 20_000, "Took the wheel of the rover.");
	}

	public DriveGroundVehicle(Robot robot, GroundVehicle vehicle, Coordinates destination, MarsClock startTripTime,
			double startTripDistance) {

		// Use OperateVehicle constructor
		super(NAME, robot, vehicle, destination, startTripTime, startTripDistance, STRESS_MODIFIER, true,
				(300D + RandomUtil.getRandomDouble(20D)));
		
		// Set initial parameters
		setDescription(Msg.getString("Task.description.driveGroundVehicle.detail", vehicle.getName())); // $NON-NLS-1$
		addPhase(AVOID_OBSTACLE);
		addPhase(WINCH_VEHICLE);

		logger.log(robot, Level.INFO, 20_000, "Took the wheel of the rover.");
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
		super(NAME, person, vehicle, destination, startTripTime, startTripDistance, STRESS_MODIFIER, 
				(100D + RandomUtil.getRandomDouble(20D)));
		
		// Set initial parameters
		setDescription(Msg.getString("Task.description.driveGroundVehicle.detail", vehicle.getName())); // $NON-NLS-1$
		addPhase(AVOID_OBSTACLE);
		addPhase(WINCH_VEHICLE);
		if (startingPhase != null)
			setPhase(startingPhase);

		logger.log(person, Level.INFO, 20_000, "Took the wheel of rover at the starting phase of '"
					+ startingPhase + "'.");

	}

	public DriveGroundVehicle(Robot robot, GroundVehicle vehicle, Coordinates destination, MarsClock startTripTime,
			double startTripDistance, TaskPhase startingPhase) {

		// Use OperateVehicle constructor
		super(NAME, robot, vehicle, destination, startTripTime, startTripDistance, STRESS_MODIFIER, true,
				(100D + RandomUtil.getRandomDouble(20D)));
		
		// Set initial parameters
		setDescription(Msg.getString("Task.description.driveGroundVehicle.detail", vehicle.getName())); // $NON-NLS-1$
		addPhase(AVOID_OBSTACLE);
		addPhase(WINCH_VEHICLE);
		if (startingPhase != null)
			setPhase(startingPhase);

		logger.log(robot, Level.INFO, 20_000, "Took the wheel of rover at the starting phase of '"
					+ startingPhase + "'.");
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
			logger.log(worker, Level.INFO, 10_000, "Had an unknown phase when driving.");
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
	 * Moves the vehicle in its direction at its speed for the amount of time given.
	 * Stop if reached destination.
	 * 
	 * @param time the amount of time (ms) to drive.
	 * @return the amount of time (ms) left over after driving (if any)
	 */
	protected double mobilizeVehicle(double time) {
		// If vehicle is stuck, try winching.
		if (((GroundVehicle) getVehicle()).isStuck() && (!WINCH_VEHICLE.equals(getPhase()))) {
			setPhase(WINCH_VEHICLE);
		}

		// If speed is less than or equal to LOW_SPEED, change to avoiding obstacle phase.
		if (!getVehicle().isInSettlement() && (getVehicle().getSpeed() <= LOW_SPEED) 
				&& (!AVOID_OBSTACLE.equals(getPhase()))
				&& (!WINCH_VEHICLE.equals(getPhase()))) {
			setPhase(AVOID_OBSTACLE);
		} 
		else
			 return super.mobilizeVehicle(time);
		
		return time;
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

		// Get the direction to the destination.
		Direction destinationDirection = vehicle.getCoordinates().getDirectionToPoint(getDestination());

		// If speed in destination direction is good, change to mobilize phase.
		double destinationSpeed = testSpeed(destinationDirection);
		
		if (destinationSpeed > LOW_SPEED) {
			// Set new direction
			vehicle.setDirection(destinationDirection);
			// Update vehicle elevation.
			updateVehicleElevationAltitude();
			
			setPhase(OperateVehicle.MOBILIZE);
			sideDirection = NONE;
			return time;
		}

		// Determine the direction to avoid the obstacle.
		Direction travelDirection = getObstacleAvoidanceDirection(time);

		// If an obstacle avoidance direction could not be found, winch vehicle.
		if (travelDirection == null) {
			setPhase(WINCH_VEHICLE);
			sideDirection = NONE;
			return time;
		}

		// Set the vehicle's direction.
		vehicle.setDirection(travelDirection);

		// Update vehicle speed.
		vehicle.setSpeed(testSpeed(vehicle.getDirection()));

		// Drive in the direction
		timeUsed = time - mobilizeVehicle(time);

		int msol = marsClock.getMillisolInt();       
        boolean successful = false; 
        
        double lastDistance = vehicle.getLastDistanceTravelled();
        double workPerMillisol = lastDistance * CU_PER_KM * time;
        
    	// Submit his request for computing resources
    	Computation center = person.getAssociatedSettlement().getBuildingManager().getMostFreeComputingNode(workPerMillisol, msol + 1, msol + 2);
    	if (center != null)
    		successful = center.scheduleTask(workPerMillisol, msol + 1, msol + 2);
    	if (successful) {
    		computingUsed += timeUsed;
      	}
    	else {
    		logger.info(person, 30_000L, "No computing resources available for " 
    			+ Msg.getString("Task.description.driveGroundVehicle.detail", // $NON-NLS-1$
    				vehicle.getName()) + ".");
    	}
    	
		// Check for accident.
		if (!isDone())
			checkForAccident(timeUsed);

		// Add experience points
		addExperience(timeUsed);
		
		// If vehicle has malfunction, end task.
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
		double remainingTime = time - standardPulseTime;
		double timeUsed = 0D;
		
		GroundVehicle vehicle = (GroundVehicle) getVehicle();

		// Find current direction and update vehicle.
		vehicle.setDirection(vehicle.getCoordinates().getDirectionToPoint(getDestination()));

		// Update vehicle elevation.
		updateVehicleElevationAltitude();

		// If speed given the terrain would be better than 1kph, return to normal
		// driving.
		// Otherwise, set speed to LOW_SPEED for winching speed.
		if (testSpeed(vehicle.getDirection()) > LOW_SPEED) {
			setPhase(OperateVehicle.MOBILIZE);
			vehicle.setStuck(false);
			return remainingTime;
		} else
			vehicle.setSpeed(.2D);

		// Drive in the direction
		timeUsed = time - mobilizeVehicle(time);

		// Add experience points
		addExperience(timeUsed);

		// Check for accident.
		if (!isDone())
			checkForAccident(timeUsed);

		// If vehicle has malfunction, end task.
		if (vehicle.getMalfunctionManager().hasMalfunction())
			endTask();

		return time - timeUsed;
	}

	/**
	 * Gets the direction for obstacle avoidance.
	 * 
	 * @return direction for obstacle avoidance in radians or null if none found.
	 */
	private Direction getObstacleAvoidanceDirection(double time) {
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
					double testSpeed = testSpeed(testDirection);
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
				double testSpeed = testSpeed(testDirection);
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
		((GroundVehicle) getVehicle()).setElevation(getGroundElevation());
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

		if (RandomUtil.lessThanRandPercent(chance * time)) {
			malfunctionManager.createASeriesOfMalfunctions(vehicle.getName(), worker);
		}
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
		int experienceAptitude = worker.getNaturalAttributeManager()
					.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);

		newPoints += newPoints * (1.0 * experienceAptitude - 50D) / 100D;
		newPoints *= getTeachingExperienceModifier();
		double phaseModifier = 1D;
		if (AVOID_OBSTACLE.equals(getPhase()))
			phaseModifier = 4D;
		newPoints *= phaseModifier;
		worker.getSkillManager().addExperience(SkillType.PILOTING, newPoints, time);
	}

	/**
	 * Stops the vehicle.
	 */
	protected void clearDown() {
		if (getVehicle() != null) {
			getVehicle().setSpeed(0D);
		    // Need to set the vehicle operator to null before clearing the driving task 
	        getVehicle().setOperator(null);

		}
	}
}

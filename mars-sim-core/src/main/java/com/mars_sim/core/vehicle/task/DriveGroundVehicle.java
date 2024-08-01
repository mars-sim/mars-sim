/*
 * Mars Simulation Project
 * DriveGroundVehicle.java
 * @date 2023-01-11
 * @author Scott Davis
 */
package com.mars_sim.core.vehicle.task;

import java.util.logging.Level;

import com.mars_sim.core.Unit;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.structure.building.function.Computation;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.vehicle.GroundVehicle;
import com.mars_sim.mapdata.location.Coordinates;
import com.mars_sim.mapdata.location.Direction;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

/**
 * The Drive Ground Vehicle class is a task for driving a ground vehicle to a
 * destination.
 */
public class DriveGroundVehicle extends OperateVehicle {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(DriveGroundVehicle.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.driveGroundVehicle"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase AVOID_OBSTACLE = new TaskPhase(Msg.getString("Task.phase.avoidObstacle"),
																	IMPACT.changeSkillsRatio(0.2D));
	private static final TaskPhase WINCH_VEHICLE = new TaskPhase(Msg.getString("Task.phase.winchVehicle")); //$NON-NLS-1$

	// Side directions.
	private static final int NONE = 0;
	private static final int LEFT = 1;
	private static final int RIGHT = 2;

	/** The computing resources [in CUs] needed per km. */
	private static final double CU_PER_KM = .05;
	
	// Data members
	private int sideDirection = NONE;
	
	/**
	 * Default Constructor for a person operator.
	 * 
	 * @param driver            the worker driving
	 * @param vehicle           the vehicle to be driven
	 * @param destination       location to be driven to
	 * @param startTripTime     the starting time of the trip
	 * @param startTripDistance the starting distance to destination for the trip
	 */
	public DriveGroundVehicle(Worker driver, GroundVehicle vehicle, Coordinates destination, MarsTime startTripTime,
			double startTripDistance) {
		this(driver, vehicle, destination, startTripTime, startTripDistance, null);
	}

	
	/**
	 * Constructs this task for a person with a given starting phase.
	 * 
	 * @param driver            the worker driving
	 * @param vehicle           the vehicle to be driven
	 * @param destination       location to be driven to
	 * @param startTripTime     the starting time of the trip
	 * @param startTripDistance the starting distance to destination for the trip
	 * @param startingPhase     the starting phase for the task
	 */
	public DriveGroundVehicle(Worker driver, GroundVehicle vehicle, Coordinates destination, MarsTime startTripTime,
			double startTripDistance, TaskPhase startingPhase) {

		// Note: OperateVehicle constructor should have set the phase to MOBILIZE
		super(NAME, driver, vehicle, destination, startTripTime, startTripDistance, (100D + RandomUtil.getRandomDouble(-20D, 20D)));
		
		// Set initial parameters
		setDescription(Msg.getString("Task.description.driveGroundVehicle.detail", vehicle.getName())); // $NON-NLS-1$
		
		if (getPhase() == null) {
			logger.log(driver, Level.INFO, 4_000, "Starting phase is null.");
		}
		
		if (startingPhase != null) {
			setPhase(startingPhase);
			logger.log(driver, Level.INFO, 4_000, "Attempting to take the helm of the rover at phase '"
					+ startingPhase + "'.");
		}
	}


	/**
	 * Performs the method mapped to the task's current phase.
	 * 
	 * @param time the amount of time the phase is to be performed.
	 * @return the remaining time after the phase has been performed.
	 */
	@Override
	protected double performMappedPhase(double time) {

		time = super.performMappedPhase(time);

		if (getPhase() == null) {
    	    logger.info(worker, "Phase is null. No longer driving " + getVehicle() + ".");
			// If it called endTask() in OperateVehicle, then Task is no longer available
			// WARNING: do NOT call endTask() here or 
    	    // it will end up calling endTask() again recursively.
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
	 * Checks for the speed of the vehicle to determine if a new phase is warranted. 
	 * Calls OperateVehicle's mobilizeVehicle() to propel the vehicle in its direction 
	 * at its speed for the amount of time given.
	 * 
	 * @param time the amount of time (ms) to drive.
	 * @return the amount of time (ms) left over after driving (if any)
	 */
	@Override
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
	 * Performs task in obstacle phase.
	 * 
	 * @param time the amount of time to perform the task (in millisols)
	 * @return time remaining after performing phase (in millisols)
	 */
	private double obstaclePhase(double time) {		
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
			// Leave this phase and go to MOBILIZE phase
			setPhase(MOBILIZE);
			
			sideDirection = NONE;
			
			return time;
		}

		// Determine the direction to avoid the obstacle.
		Direction travelDirection = getObstacleAvoidanceDirection(time);

		// If an obstacle avoidance direction could not be found, winch vehicle.
		if (travelDirection == null) {
			// Leave this phase and go to WINCH_VEHICLE phase
			setPhase(WINCH_VEHICLE);
			
			sideDirection = NONE;
			
			return time;
		}

		// Set the vehicle's direction.
		vehicle.setDirection(travelDirection);

		// Update vehicle speed.
		vehicle.setSpeed(testSpeed(vehicle.getDirection()));

		// Drive in the direction
		double timeUsed = time - mobilizeVehicle(time);

		int msol = getMarsTime().getMillisolInt();       
        
        double lastDistance = vehicle.getLastDistanceTravelled();
        double workPerMillisol = lastDistance * CU_PER_KM * time;
        
    	// Submit his request for computing resources
    	Computation center = person.getAssociatedSettlement().getBuildingManager().getMostFreeComputingNode(workPerMillisol, msol + 1, msol + 2);
    	if (center != null)
    		center.scheduleTask(workPerMillisol, msol + 1, msol + 2);
    	
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
	 * Performs task in winching phase.
	 * 
	 * @param time the amount of time to perform the phase.
	 * @return time remaining after performing the phase.
	 */
	private double winchingPhase(double time) {
		double remainingTime = 0;
		
		GroundVehicle vehicle = (GroundVehicle) getVehicle();

		// Find current direction and update vehicle.
		vehicle.setDirection(vehicle.getCoordinates().getDirectionToPoint(getDestination()));

		// Update vehicle elevation.
		updateVehicleElevationAltitude();

		// If speed given the terrain would be better than LOW_SPEED, return to normal
		// driving.
		// Otherwise, set speed to LOW_SPEED for winching speed.
		if (testSpeed(vehicle.getDirection()) > LOW_SPEED) {
			// Leave this phase and go to MOBILIZE phase
			setPhase(OperateVehicle.MOBILIZE);
			
			vehicle.setStuck(false);
			
			return remainingTime;
		} else
			vehicle.setSpeed(LOW_SPEED/2);

		// Drive in the direction
		double timeUsed = time - mobilizeVehicle(time);

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
	 * Check if vehicle has an accident.
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

		MalfunctionManager malfunctionManager = vehicle.getMalfunctionManager();
		// Modify based on the vehicle's wear condition.
		chance *= malfunctionManager.getAccidentModifier();

		if (RandomUtil.lessThanRandPercent(chance * time)) {
			malfunctionManager.createASeriesOfMalfunctions(vehicle.getName(), (Unit)worker);
		}
	}

	/**
	 * Stops the vehicle.
	 */
	@Override
	public void clearDown() {
		var v = getVehicle();
		if (v != null) {
			v.setSpeed(0D);
		    // Need to set the vehicle operator to null before clearing the driving task 
	        v.setOperator(null);
		}

		super.clearDown();
	}
}

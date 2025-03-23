/*
 * Mars Simulation Project
 * PilotDrone.java
 * @date 2024-07-15
 * @author Manny
 */
package com.mars_sim.core.vehicle.task;

import java.util.logging.Level;

import com.mars_sim.core.Unit;
import com.mars_sim.core.building.function.Computation;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.Direction;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Flyer;

/**
 * The PilotDrone class is a task for piloting a drone to a
 * destination.
 */
public class PilotDrone extends OperateVehicle {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(PilotDrone.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.pilotDrone"); //$NON-NLS-1$

	/** Collision phase produces more skill experience */
	private static final TaskPhase AVOID_COLLISION = new TaskPhase(Msg.getString("Task.phase.avoidObstacle"),
									IMPACT.changeSkillsRatio(0.2D));

	/** The computing resources [in CUs] needed per km. */
	private static final double CU_PER_KM = .05;
	
	// Side directions.
	private static final int NONE = 0;
	private static final int LEFT = 1;
	private static final int RIGHT = 2;

	// Data members
	private int sideDirection = NONE;
			
	/**
	 * Default Constructor.
	 * 
	 * @param pilot            the worker piloting
	 * @param flyer             the flyer to be driven
	 * @param destination       location to be driven to
	 * @param startTripTime     the starting time of the trip
	 * @param startTripDistance the starting distance to destination for the trip
	 */
	public PilotDrone(Worker pilot, Flyer flyer, Coordinates destination, MarsTime startTripTime,
			double startTripDistance) {
		this(pilot, flyer, destination, startTripTime, startTripDistance, null);
	}

	/**
	 * Constructs with a given starting phase.
	 * 
	 * @param pilot            the worker piloting
	 * @param vehicle           the vehicle to be driven
	 * @param destination       location to be driven to
	 * @param startTripTime     the starting time of the trip
	 * @param startTripDistance the starting distance to destination for the trip
	 * @param startingPhase     the starting phase for the task
	 */
	public PilotDrone(Worker pilot, Flyer flyer, Coordinates destination, MarsTime startTripTime,
			double startTripDistance, TaskPhase startingPhase) {

		// Note: OperateVehicle constructor should have set the phase to MOBILIZE
		super(NAME, pilot, flyer, destination, startTripTime, startTripDistance, 200);
		
		// Set initial parameters
		setDescription(Msg.getString("Task.description.pilotDrone.detail", flyer.getName())); // $NON-NLS-1$

		if (getPhase() == null) {
			logger.log(pilot, Level.INFO, 4_000, "Starting phase is null.");
		}
		
		if (startingPhase != null) {
			setPhase(startingPhase);
			logger.log(pilot, Level.INFO, 4_000, "Attempting to take control of the drone at phase '"
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
    	    logger.info(worker, "Phase is null. No longer piloting " + getVehicle() + ".");
			// If it called endTask() in OperateVehicle, then Task is no longer available
			// WARNING: do NOT call endTask() here or it will end up calling endTask() 
			// recursively.
			return time;		
			
		} else if (AVOID_COLLISION.equals(getPhase())) {
			return obstaclePhase(time);
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
	@Override
	protected double mobilizeVehicle(double time) {

		// If speed is less than or equal to LOW_SPEED, change to avoiding collision phase.
		if (!getVehicle().isInSettlement() && (getVehicle().getSpeed() <= LOW_SPEED) 
				&& !AVOID_COLLISION.equals(getPhase())) {
			setPhase(AVOID_COLLISION);
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
		Flyer flyer = (Flyer) getVehicle();

		// Get the direction to the destination.
		Direction destinationDirection = flyer.getCoordinates().getDirectionToPoint(getDestination());

		// If speed in destination direction is good, change to mobilize phase.
		double destinationSpeed = testSpeed(destinationDirection);
		
		if (destinationSpeed > LOW_SPEED) {
			// Set new direction
			flyer.setDirection(destinationDirection);
			// Update elevation
//			updateVehicleElevationAltitude(true, time);
			
			setPhase(MOBILIZE);
			
			sideDirection = NONE;
			
			return time;
		}

		// Determine the direction to avoid the obstacle.
		Direction travelDirection = getObstacleAvoidanceDirection(time);

		// If an direction could not be found, change the elevation
		if (travelDirection == null) {
			// Ascend into to get around obstacles
//			updateVehicleElevationAltitude(false, time);
			
			sideDirection = NONE;
			
			return time;
		}

		// Set the vehicle's direction.
		flyer.setDirection(travelDirection);

		// Update vehicle speed.
		flyer.setSpeed(testSpeed(flyer.getDirection()));

		// Drive in the direction
		double timeUsed = time - mobilizeVehicle(time);
		
		int msol = getMarsTime().getMillisolInt();       
        
        double lastDistance = flyer.getLastDistanceTravelled();
        double workPerMillisol = lastDistance * CU_PER_KM * time;
        
    	// Submit his request for computing resources
    	Computation center = person.getAssociatedSettlement().getBuildingManager().getMostFreeComputingNode(workPerMillisol, msol + 1, msol + 2);
    	if (center != null)
    		center.scheduleTask(workPerMillisol, msol + 1, msol + 2);
		
		// Add experience points
		addExperience(timeUsed);

		// If vehicle has malfunction, end task.
		if (flyer.getMalfunctionManager().hasMalfunction())
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

		Flyer flyer = (Flyer) getVehicle();
		boolean foundGoodPath = false;

		double initialDirection = flyer.getCoordinates().getDirectionToPoint(getDestination()).getDirection();

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

	@Override
	protected void updateVehicleElevationAltitude() {
		// Not needed for some reason !!
	}
	
//	/**
//	 * Updates vehicle with its current elevation or altitude.
//	 * 
//	 * @param horizontalMovement
//	 * @param time
//	 */
//	protected void updateVehicleElevationAltitude(boolean horizontalMovement, double time) {
//		int mod = 1;
//		if (!horizontalMovement)
//			mod = 4;
//			
//		double droneH = ((Flyer)getVehicle()).getHoveringHeight();
//		double oldGroundE = ((Flyer)getVehicle()).getElevation();
//		double newGroundE = getGroundElevation() * 1000;
//		
//		double ascentE = (Flyer.ELEVATION_ABOVE_GROUND - droneH) + (newGroundE - oldGroundE);
//		double climbE = 0;
//		
//		if (ascentE > 0) {
//			// Future: Use Newton's law to determine the amount of height the flyer can climb 
//			double tSec = time * MarsTime.SECONDS_PER_MILLISOL;
//			double speed = .0025 * mod;
//			climbE = speed * tSec;
//			
//		}
//		else if (ascentE < 0) {
//			// Future: Use Newton's law to determine the amount of height the flyer can climb 
//			double tSec = time * MarsTime.SECONDS_PER_MILLISOL;
//			double speed = -.02 * mod;
//			climbE = speed * tSec;
//		}
//		
//		double elev = climbE + oldGroundE;
//		((Flyer) getVehicle()).setElevation(elev);
//	}

	/**
	 * Check if vehicle has had an accident.
	 * 
	 * @param time the amount of time vehicle is driven (millisols)
	 */
	@Override
	protected void checkForAccident(double time) {

		Flyer flyer = (Flyer) getVehicle();

		double chance = OperateVehicle.BASE_ACCIDENT_CHANCE;

		// Driver skill modification.
		int skill = getEffectiveSkillLevel();
		if (skill <= 3)
			chance *= (4 - skill);
		else
			chance /= (skill - 2);

		// Get task phase modification.
		if (AVOID_COLLISION.equals(getPhase()))
			chance *= 1.2D;

		// Terrain modification.
		chance *= (1D + Math.sin(flyer.getTerrainGrade()));

		// Vehicle handling modification.
//		chance /= (1D + flyer.getTerrainHandlingCapability());

		// Light condition modification.
		double lightConditions = surfaceFeatures.getSunlightRatio(flyer.getCoordinates());
		chance *= (5D * (1D - lightConditions)) + 1D;
		if (chance < 0D) {
			chance = 0D;
		}

		MalfunctionManager malfunctionManager = flyer.getMalfunctionManager();
		// Modify based on the vehicle's wear condition.
		chance *= malfunctionManager.getAccidentModifier();

		if (RandomUtil.lessThanRandPercent(chance * time)) {
			malfunctionManager.createASeriesOfMalfunctions(flyer.getName(), (Unit)worker);
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

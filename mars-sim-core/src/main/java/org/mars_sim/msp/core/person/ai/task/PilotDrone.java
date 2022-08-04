/*
 * Mars Simulation Project
 * PilotDrone.java
 * @date 2022-06-17
 * @author Manny
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.logging.Level;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Direction;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.function.Computation;
import org.mars_sim.msp.core.time.ClockUtils;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Flyer;

/**
 * The PilotDrone class is a task for piloting a drone to a
 * destination.
 */
public class PilotDrone extends OperateVehicle implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(PilotDrone.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.pilotDrone"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase AVOID_COLLISION = new TaskPhase(Msg.getString("Task.phase.avoidObstacle")); //$NON-NLS-1$
//	private static final TaskPhase WINCH_VEHICLE = new TaskPhase(Msg.getString("Task.phase.winchVehicle")); //$NON-NLS-1$

	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .2D;
	/** The speed at which the obstacle / winching phase commence. */
	private static final double LOW_SPEED = .5;
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
	 * @param flyer             the flyer to be driven
	 * @param destination       location to be driven to
	 * @param startTripTime     the starting time of the trip
	 * @param startTripDistance the starting distance to destination for the trip
	 */
	public PilotDrone(Person person, Flyer flyer, Coordinates destination, MarsClock startTripTime,
			double startTripDistance) {

		// Use OperateVehicle constructor
		super(NAME, person, flyer, destination, startTripTime, startTripDistance, STRESS_MODIFIER, 
				150D + RandomUtil.getRandomDouble(10D) - RandomUtil.getRandomDouble(10D));
		
		// Set initial parameters
		setDescription(Msg.getString("Task.description.pilotDrone.detail", flyer.getName())); // $NON-NLS-1$
		addPhase(AVOID_COLLISION);
//		addPhase(WINCH_VEHICLE);

		logger.log(flyer, person, Level.INFO, 20_000, "Took control of the drone.");
	}

	public PilotDrone(Robot robot, Flyer flyer, Coordinates destination, MarsClock startTripTime,
			double startTripDistance) {

		// Use OperateVehicle constructor
		super(NAME, robot, flyer, destination, startTripTime, startTripDistance, STRESS_MODIFIER, true,
				1000D);
		
		// Set initial parameters
		setDescription(Msg.getString("Task.description.pilotDrone.detail", flyer.getName())); // $NON-NLS-1$
		addPhase(AVOID_COLLISION);
//		addPhase(WINCH_VEHICLE);

		logger.log(flyer, robot, Level.INFO, 20_000, "Took control of the drone.");
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
	public PilotDrone(Person person, Flyer flyer, Coordinates destination, MarsClock startTripTime,
			double startTripDistance, TaskPhase startingPhase) {

		// Use OperateVehicle constructor
		super(NAME, person, flyer, destination, startTripTime, startTripDistance, STRESS_MODIFIER, 
				150D + RandomUtil.getRandomDouble(10D) - RandomUtil.getRandomDouble(10D));
		
		// Set initial parameters
		setDescription(Msg.getString("Task.description.pilotDrone.detail", flyer.getName())); // $NON-NLS-1$
		addPhase(AVOID_COLLISION);
//		addPhase(WINCH_VEHICLE);
		if (startingPhase != null)
			setPhase(startingPhase);

		logger.log(person, Level.INFO, 20_000, "Took control of the drone at phase '"
					+ startingPhase + "'.");

	}

	public PilotDrone(Robot robot, Flyer flyer, Coordinates destination, MarsClock startTripTime,
			double startTripDistance, TaskPhase startingPhase) {

		// Use OperateVehicle constructor
		super(NAME, robot, flyer, destination, startTripTime, startTripDistance, STRESS_MODIFIER, true,
				1000D);
		
		// Set initial parameters
		setDescription(Msg.getString("Task.description.pilotDrone.detail", flyer.getName())); // $NON-NLS-1$
		addPhase(AVOID_COLLISION);
//		addPhase(WINCH_VEHICLE);
		if (startingPhase != null)
			setPhase(startingPhase);

		logger.log(robot, Level.INFO, 0, "Took control of the drone at phase '"
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
			logger.log(worker, Level.INFO, 10_000, "Had an unknown phase when piloting");
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
	 * Move the vehicle in its direction at its speed for the amount of time given.
	 * Stop if reached destination.
	 * 
	 * @param time the amount of time (ms) to drive.
	 * @return the amount of time (ms) left over after driving (if any)
	 */
	protected double mobilizeVehicle(double time) {
		double remainingTime = time - standardPulseTime;
		
		// If speed is less than or equal to the .5 kph, change to avoiding obstacle phase.
		if ((getVehicle().getSpeed() <= LOW_SPEED) 
				&& !AVOID_COLLISION.equals(getPhase())) {
			setPhase(AVOID_COLLISION);
			return (remainingTime);
		} else
			return super.mobilizeVehicle(standardPulseTime);
	}

	/**
	 * Perform task in obstacle phase.
	 * 
	 * @param time the amount of time to perform the task (in millisols)
	 * @return time remaining after performing phase (in millisols)
	 */
	private double obstaclePhase(double time) {
		double remainingTime = time - standardPulseTime;
		double timeUsed = 0D;
		
		Flyer flyer = (Flyer) getVehicle();

		// Get the direction to the destination.
		Direction destinationDirection = flyer.getCoordinates().getDirectionToPoint(getDestination());

		// If speed in destination direction is good, change to mobilize phase.
		double destinationSpeed = testSpeed(destinationDirection);
		
		if (destinationSpeed > LOW_SPEED) {
			// Set new direction
			flyer.setDirection(destinationDirection);
			// Update vehicle elevation.
			updateVehicleElevationAltitude(true, time);
			
			setPhase(PilotDrone.MOBILIZE);
			sideDirection = NONE;
			return remainingTime;
		}

		// Determine the direction to avoid the obstacle.
		Direction travelDirection = getObstacleAvoidanceDirection(time);

		// If an direction could not be found, change the elevation
		if (travelDirection == null) {
			// Update vehicle elevation.
			updateVehicleElevationAltitude(false, time);
			
			sideDirection = NONE;
			return remainingTime;
		}

		// Set the vehicle's direction.
		flyer.setDirection(travelDirection);

		// Update vehicle speed.
		flyer.setSpeed(testSpeed(flyer.getDirection()));

		// Drive in the direction
		timeUsed = time - mobilizeVehicle(time);
		
		int msol = marsClock.getMillisolInt();       
        boolean successful = false; 
        
        double lastDistance = flyer.getLastDistanceTravelled();
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
    			+ Msg.getString("Task.description.pilotDrone.detail", // $NON-NLS-1$
    					flyer.getName()) + "."); 
    	}
		
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
	}
	
	/**
	 * Update vehicle with its current elevation or altitude.
	 */
	protected void updateVehicleElevationAltitude(boolean horizontalMovement, double time) {
		int mod = 1;
		if (!horizontalMovement)
			mod = 4;
			
		double currentE = ((Flyer)getVehicle()).getHoveringElevation();
		double oldGroundE = ((Flyer)getVehicle()).getElevation();
		double newGroundE = getGroundElevation();
		
		double ascentE = (Flyer.ELEVATION_ABOVE_GROUND - currentE) + (newGroundE - oldGroundE);
		double climbE = 0;
		
		if (ascentE > 0) {
			// Future: Use Newton's law to determine the amount of height the flyer can climb 
			double tSec = time * ClockUtils.SECONDS_PER_MILLISOL;
			double speed = .0025 * mod;
			climbE = speed * tSec;
			
		}
		else if (ascentE < 0) {
			// Future: Use Newton's law to determine the amount of height the flyer can climb 
			double tSec = time * ClockUtils.SECONDS_PER_MILLISOL;
			double speed = -.02 * mod;
			climbE = speed * tSec;
		}
		
		double elev = climbE + oldGroundE;
		((Flyer) getVehicle()).setElevation(elev);
		
//		logger.log(getVehicle(), person, Level.INFO, 20_000, 
//				"Old Elevation: " + Math.round(oldGroundE * 100.00)/100.00 + " km."
//				+ "   New Elevation: " + Math.round(elev * 100.00)/100.00 + " km.");
	}

	/**
	 * Check if vehicle has had an accident.
	 * 
	 * @param time the amount of time vehicle is driven (millisols)
	 */
	protected void checkForAccident(double time) {
	}

	/**
	 * Adds experience to the worker skills used in this task.
	 * 
	 * @param time the amount of time (ms) the person performed this task.
	 */
	protected void addExperience(double time) {
		// Add experience points for driver's 'Driving' skill.
		// Add one point for every 100 millisols.
		double newPoints = time / 100D;
		int experienceAptitude = worker.getNaturalAttributeManager()
					.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);

		newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
		newPoints *= getTeachingExperienceModifier();
		double phaseModifier = 1D;
		if (AVOID_COLLISION.equals(getPhase()))
			phaseModifier = 4D;
		newPoints *= phaseModifier;
		worker.getSkillManager().addExperience(SkillType.PILOTING, newPoints, time);
	}

	/**
	 * Stop the vehicle
	 */
	protected void clearDown() {
		if (getVehicle() != null) {
		    // Need to set the vehicle operator to null before clearing the driving task 
	        getVehicle().setOperator(null);
		}
	}
}

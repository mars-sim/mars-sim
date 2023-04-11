/*
 * Mars Simulation Project
 * OperateVehicle.java
 * @date 2023-04-11
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.util.List;
import java.util.logging.Level;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Direction;
import org.mars_sim.msp.core.LocalPosition;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.environment.TerrainElevation;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionStatus;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskManager;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.person.ai.training.TrainingType;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Flyer;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The OperateVehicle class is an abstract task for operating a vehicle and
 * driving it to a destination.
 */
public abstract class OperateVehicle extends Task {
	
    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
	// default logger.
	private static final SimLogger logger = SimLogger.getLogger(OperateVehicle.class.getName());
	
    /** Task phases. */
    protected static final TaskPhase MOBILIZE = new TaskPhase(Msg.getString(
            "Task.phase.mobilize")); //$NON-NLS-1$
    
	/** The ratio of time per experience points. */
	private static final double EXP = .2D;
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .2D;
	/** The speed at which the obstacle / winching phase commence. */
	protected static final double LOW_SPEED = .05;
	/** Conversion factor : 1 m/s = 3.6 km/h (or kph) */
	private static final double KPH_CONV = 3.6;
	/** Half the PI. */
	private static final double HALF_PI = Math.PI / 2D;
	/** Comparison to indicate a small but non-zero amount of fuel (methane) in kg that can still work on the fuel cell to propel the engine. */
    private static final double LEAST_AMOUNT = GroundVehicle.LEAST_AMOUNT;
    /** Distance buffer for arriving at destination (km). */
    private static final double DESTINATION_BUFFER = .000_1;
    /** The base percentage chance of an accident while operating vehicle per millisol. */
    public static final double BASE_ACCIDENT_CHANCE = .01D;
			
    /** Need to provide oxygen as fuel oxidizer for the fuel cells. */
	public static final int OXYGEN_ID = ResourceUtil.oxygenID;
    /** The fuel cells will generate 2.25 kg of water per 1 kg of methane being used. */
	public static final int WATER_ID = ResourceUtil.waterID;
	
	// Data members
	/** The fuel type of this vehicle. */
	private int fuelType;
	/** The distance [km] to the destination at the start of the trip. */
	private double startTripDistance; 
	
	
	/** The vehicle to operate. */ 
	private Vehicle vehicle;
	/** The location of the destination of the trip. */
	private Coordinates destination;
	/** The timestamp the trip is starting. */
	private MarsClock startTripTime;
	/** The malfunctionManager of this vehicle. */
	private MalfunctionManager malfunctionManager;
	
	/**
	 * Constructor for a human pilot.
	 * 
	 * @param name the name of the particular task.
	 * @param person the person performing the task.
	 * @param vehicle the vehicle to operate.
	 * @param destination the location of destination of the trip.
	 * @param startTripTime the time/date the trip is starting.
	 * @param startTripDistance the distance (km) to the destination at the start of the trip.
	 * @param duration the time duration (millisols) of the task (or 0 if none).
	 */
	public OperateVehicle(String name, Person person, Vehicle vehicle, Coordinates destination, 
			MarsClock startTripTime, double startTripDistance, double duration) {
		
		// Use Task constructor
		super(name, person, false, false, STRESS_MODIFIER, SkillType.PILOTING, EXP, duration);
		
		// Initialize data members.
		this.vehicle = vehicle;
		this.destination = destination;
		this.startTripTime = startTripTime;
		this.startTripDistance = startTripDistance;
		
        fuelType = vehicle.getFuelType();
		
		malfunctionManager = vehicle.getMalfunctionManager();
		
		if (destination == null) {
		    throw new IllegalArgumentException("destination is null");
		}
		
		if (startTripTime == null) {
		    throw new IllegalArgumentException("startTripTime is null");
		}
		
		if (startTripDistance < 0D) {
		    throw new IllegalArgumentException("startTripDistance is < 0");
		}
		
		// Select the vehicle operator
		Worker vo = vehicle.getOperator();

		// Check if there is a driver assigned to this vehicle.
		if (vo == null) 
			vehicle.setOperator(person);
			
		else if (!person.getName().equals(vo.getName())) {
        	// Remove the task from the last driver
	        clearDrivingTask(vo);
	        // Replace the driver
			vehicle.setOperator(person);
		}
		
		// Walk to operation activity spot in vehicle.
		if (vehicle instanceof Rover) {
		    walkToOperatorActivitySpotInRover((Rover) vehicle, false);
		}
		
		addPhase(MOBILIZE);
		
		// Set initial phase
		setPhase(MOBILIZE);
	}
	
	/**
	 * Constructor for a robot pilot.
	 * 
	 * @param name
	 * @param robot
	 * @param vehicle
	 * @param destination
	 * @param startTripTime
	 * @param startTripDistance
	 * @param duration
	 */
	public OperateVehicle(String name, Robot robot, Vehicle vehicle, Coordinates destination, 
			MarsClock startTripTime, double startTripDistance, double duration) {
		
		// Use Task constructor
		super(name, robot, false, false, STRESS_MODIFIER, SkillType.PILOTING, EXP, duration);
		
		// Initialize data members.
		this.vehicle = vehicle;
		this.destination = destination;
		this.startTripTime = startTripTime;
		this.startTripDistance = startTripDistance;
		
        fuelType = vehicle.getFuelType();
        
		// Check for valid parameters.
		if (destination == null) {
		    throw new IllegalArgumentException("destination is null");
		}
		if (startTripTime == null) {
		    throw new IllegalArgumentException("startTripTime is null");
		}
		if (startTripDistance < 0D) {
		    throw new IllegalArgumentException("startTripDistance is < 0");
		}
		
		malfunctionManager = vehicle.getMalfunctionManager();
		// Select the vehicle operator
		Worker vo = vehicle.getOperator();
	
		// Check if there is a driver assigned to this vehicle.
		if (vo == null) 
			vehicle.setOperator(robot);
		
		else if (!robot.equals(vo)) {
        	// Remove the task from the last driver
	        clearDrivingTask(vo);
	        // Replace the driver
			vehicle.setOperator(robot);
		}
		
		// Walk to operation activity spot in vehicle.
		if (vehicle instanceof Rover) {
		    walkToOperatorActivitySpotInRover((Rover) vehicle, false);
		}
		
		addPhase(MOBILIZE);
		
		// Set initial phase
		setPhase(MOBILIZE);
	}    

	/**
	 * Walks to an activity spot in the rover.
	 * 
	 * @param rover
	 * @param allowFail
	 */
	private void walkToOperatorActivitySpotInRover(Rover rover, boolean allowFail) {
		walkToActivitySpotInRover(rover, rover.getOperatorActivitySpots(), allowFail);
	}
	
    @Override
    protected double performMappedPhase(double time) {
    	if (getPhase() == null) {
    	    throw new IllegalArgumentException("Task phase is null");
    	}
    	else if (MOBILIZE.equals(getPhase())) {
    	    return mobilizeVehiclePhase(time);
    	}
    	else {
    	    return time;
    	}
    }
	
	/**
	 * Gets the vehicle operated with this task.
	 * 
	 * @return vehicle
	 */
	public Vehicle getVehicle() {
		return vehicle;
	}
	
	/** 
	 * Gets the location of the destination of the trip.
	 * 
	 * @return location of destination
	 */
	public Coordinates getDestination() {
		return destination;
	}
	
	/**
	 * Sets the location of the destination of this trip.
	 * 
	 * @param newDestination location of the destination.
	 */
	public void setDestination(Coordinates newDestination) {
		this.destination = newDestination;
        vehicle.setCoordinates(destination);
	}
	
	/**
	 * Gets the time/date the trip was started on.
	 * 
	 * @return start time
	 */
	protected MarsClock getStartTripTime() {
		return startTripTime;
	}
	
	/**
	 * Gets the distance to the destination at the start of the trip.
	 * 
	 * @return distance (km) to destination.
	 */
	protected double getStartTripDistance() {
		return startTripDistance;
	}
	
	/**
	 * Clears this task in the task manager.
	 * 
	 * @param vo
	 */
	protected void clearDrivingTask(Worker vo) {
    	// Clear the OperateVehicle task from the last driver
		TaskManager taskManager = vo.getTaskManager();
		taskManager.clearSpecificTask(DriveGroundVehicle.class.getSimpleName());
		taskManager.clearSpecificTask(PilotDrone.class.getSimpleName());
		taskManager.clearSpecificTask(OperateVehicle.class.getSimpleName());
	}
	
	/**
	 * Performs the mobilize vehicle phase for the amount of time given.
	 * 
	 * @param time the amount of time (ms) to perform the phase.
	 * @return the amount of time left over after performing the phase.
	 */
	protected double mobilizeVehiclePhase(double time) {
	
        // Find current direction and update vehicle.
        vehicle.setDirection(vehicle.getCoordinates().getDirectionToPoint(destination));
        
        // Find current elevation/altitude and update vehicle.
        updateVehicleElevationAltitude();
  		
        if (vehicle.getSpeed() == 0.0d)
        	vehicle.setSpeed(LOW_SPEED * 1.1);
        		
        // Mobilize vehicle
        double timeUsed = time - mobilizeVehicle(time);
        
        // Add experience to the operator
        addExperience(timeUsed);
        
        // Check for accident.
        if (!isDone()) {
            checkForAccident(timeUsed);
        }
        
        // If vehicle has malfunction, end task.
        if (malfunctionManager.hasMalfunction()) {
            endTask();
        }
        
        return time - timeUsed;
	}
	
	/**
	 * Turns on the emergency beacon to ask for help.
	 * 
	 * @param resource
	 */
	private void turnOnBeacon(int resource) {
		vehicle.setSpeed(0D);
        MissionStatus status = MissionStatus.createResourceStatus(resource);
        	
    	if (!vehicle.isBeaconOn()) {
    		Mission m = vehicle.getMission();
    		((VehicleMission)m).getHelp(status);
    	}
	}
	
	/**
	 * Moves the vehicle in its direction at its speed for the amount of time given.
	 * Stops if destination reached.
	 * 
	 * @param time the amount of time (ms) to drive.
	 * @return the amount of time (ms) left over after driving (if any)
	 */
	protected double mobilizeVehicle(double time) {
        double remainingTime = 0;

        if (worker.getUnitType() == UnitType.ROBOT
        	&& ((Robot)worker).getSystemCondition().isLowPower()) {
        	logger.log((Robot)worker, Level.WARNING, 20_000,
        			". Can't pilot " + getVehicle() + ".");
	        endTask();
	        return time;
        }
        
		if (time < 0) {
			logger.severe(vehicle, "Negative time: " + time);
        	return 0;
		}
 
    	// Case 0 : no fuel or oxidizer left     
        if (vehicle.isInSettlement()) 
        	return time;
  	
        double remainingFuel = vehicle.getAmountResourceStored(fuelType);
        double remainingOxidizer = vehicle.getAmountResourceStored(OXYGEN_ID);

    	if (remainingFuel < LEAST_AMOUNT) {
    		logger.log(vehicle, Level.SEVERE, 20_000L, 
					"Case A: Out of fuel. Cannot drive.");
    		// Turn on emergency beacon
	    	turnOnBeacon(fuelType);
        	endTask();
        	return time;
    	}

    	if (remainingOxidizer < LEAST_AMOUNT) {
    		logger.log(vehicle, Level.SEVERE, 20_000L, 
					"Case B: Out of fuel oxidizer. Cannot drive.");
    		// Turn on emergency beacon
	    	turnOnBeacon(OXYGEN_ID);
        	endTask();
        	return time;
        }
        
        // Find the starting distance here to destination.
        double startingDistanceToDestination = getDistanceToDestination();
       
        if (Double.isNaN(startingDistanceToDestination)) {
        	logger.severe("startingDistance is " + startingDistanceToDestination );
        	return time;
        }
        
        // Case 0: arrived
        if (startingDistanceToDestination <= DESTINATION_BUFFER) {
        	logger.log(vehicle, Level.CONFIG,  20_000L, "Case 0: Arrived at " + destination 
        			+ " (startingDistanceToDestination: " 
        			+ Math.round(startingDistanceToDestination * 1_000.0)/1_000.0 + " km).");

        	// Note: Need to consider the case in which VehicleMission's determineEmergencyDestination() causes the 
        	// the vehicle to switch the destination to a settlement when this settlement is within a very short
        	// distance away.

        	// Stop the vehicle
        	haltVehicle();

            if (isSettlementDestination())
                determineInitialSettlementParkedLocation();
            
			endTask();
        	return remainingTime;
        }
        
        // Determine the hours used.
        double hrsTime = MarsClock.HOURS_PER_MILLISOL * time;
        
//		logger.log(vehicle, Level.CONFIG, 20_000L, 
//				"hrsTime: " + Math.round(hrsTime * 1000.0)/1000.0 + " hrs");
		
        double a_ms = vehicle.getAccel(); // [in m/s2]
        double u_kph = vehicle.getSpeed();
        double u_ms = u_kph / KPH_CONV; // [in m/s]

        double v_ms = u_ms + a_ms * hrsTime * 3600; // [in m/s]
        double v_kph = v_ms * KPH_CONV;
        
        // Assume vehicle's speed max out
    	v_kph = Math.min(v_kph, 2 * getAverageVehicleSpeed(vehicle, worker));
    	v_ms = v_kph / KPH_CONV;
   
//    	logger.log(vehicle, Level.INFO, 20_000L, "max v_kph: " + Math.round(v_kph * 1000.0)/1000.0 + " kph");
    	
    	// Determine distance traveled in time given.
        double d_km = hrsTime * (u_kph + v_kph) / 2.0; // [in km]
    	
        double fuelUsed = 0;
           
        if (Double.isNaN(d_km)) {
        	logger.severe("distancedtraveled is NaN.");
        	return time;
        }
        
        // Case 1 : overshot. Need to recalculate d, t and u
        if (startingDistanceToDestination <= (d_km + DESTINATION_BUFFER)) {
        	logger.log(vehicle, Level.INFO,  20_000L, "Case 1: Arriving near "
        			+ destination + " - " 
        			+ Math.round(startingDistanceToDestination * 1_000.0)/1_000.0 + " km away.");
        	
        	// Reset d_km to the remaining distance and recalculate speed and time
        	d_km = startingDistanceToDestination; // [in km]
        	// Recalculate the time based on previous speed
        	// Note: assume no emergency and the vehicle will choose to use constant velocity to get there
        	hrsTime = d_km / u_kph; // [in hrs]

        	// Maintain the constant speed. Assign v as u.
        	v_kph = u_kph;
        	
    		v_ms = u_ms;
    		
            // Calculate the fuel needed
            fuelUsed = vehicle.getController().calculateFuelUsed(u_ms, v_ms, d_km, hrsTime, remainingFuel);
            
            // Assume it won't run out of fuel there
            
            // Calculate the remaining time
            remainingTime = time - hrsTime / MarsClock.MILLISOLS_PER_HOUR;
        }
        
        else {
            // Calculate the fuel needed
            fuelUsed = vehicle.getController().calculateFuelUsed(u_ms, v_ms, d_km, hrsTime, remainingFuel);
		    
        	// Bring back the cache values of hrsTime and d_km
        	hrsTime = vehicle.getController().getHrsTime();
        	d_km = vehicle.getController().getDistanceCache();
            
            if (hrsTime > 0 || d_km > 0) {
            	// Case 2 : ran out of fuel. The rover may use whatever amount of fuel left
				logger.log(vehicle, Level.WARNING,  20_000L, 
						"Case 2: Used up the last drop of fuel to drive toward "
						+ destination + " - " 
	        			+ Math.round(d_km * 1_000.0)/1_000.0 + " km away.");
				
            	remainingTime = time - hrsTime / MarsClock.MILLISOLS_PER_HOUR;
            }
            else {
            	// Case 3 : the rover may use all the prescribed time to drive 
				logger.log(vehicle, Level.INFO,  20_000L, "Case 3: Driving toward "
						+ destination + " - " 
	        			+ Math.round(startingDistanceToDestination * 1_000.0)/1_000.0 + " km away.");
				// Consume all time
            	remainingTime = 0;
            }

            // Determine new position
 			// ----- Calling this is PROBLEMATIC
            vehicle.setCoordinates(vehicle.getCoordinates().getNewLocation(vehicle.getDirection(), d_km));

		}
        
        if (fuelUsed > 0) {
	    	// Retrieve the fuel needed for the distance traveled
		    vehicle.retrieveAmountResource(fuelType, fuelUsed);
		    // Assume double amount of oxygen as fuel oxidizer
		    vehicle.retrieveAmountResource(OXYGEN_ID, 2 * fuelUsed);
		    // Generate 1.75 times amount of the water from the fuel cells
		    vehicle.storeAmountResource(WATER_ID, 1.75 * fuelUsed);
        }
        
        return remainingTime;   
	}
        
	/**
	 * Stops the vehicle.
	 */
	public void haltVehicle() {
		// Note: instead of wasting the momentum/energy, 
		// calculate the power stored based on regenerative braking
		
		// Set speed to zero
		vehicle.setSpeed(0D);
		// Determine new position.
		vehicle.setCoordinates(destination);
		// Remove the vehicle operator
		vehicle.setOperator(null);

		updateVehicleElevationAltitude();
	}	

	/**
	 * Checks if the destination is at the location of a settlement.
	 * 
	 * @return true if destination is at a settlement location.
	 */
	private boolean isSettlementDestination() {
        return CollectionUtils.findSettlement(destination) instanceof Settlement;
    }
	
	/**
	 * Determines the vehicle's initial parked location.
	 */
	private void determineInitialSettlementParkedLocation() {
	   
        // Park 200 meters from the new settlement in the direction of travel
        LocalPosition parkingPlace = LocalPosition.DEFAULT_POSITION.getPosition(200D, vehicle.getDirection().getDirection() + Math.PI);
        double degDir = vehicle.getDirection().getDirection() * 180D / Math.PI;
	    
        vehicle.setParkedLocation(parkingPlace, degDir);
	}
	
	/**
	 * Updates vehicle with its current elevation or altitude.
	 */
	protected abstract void updateVehicleElevationAltitude();
	
    /** 
     * Determines the ETA (Estimated Time of Arrival) to the destination.
     * 
     * @return MarsClock instance of date/time for ETA
     */
    public MarsClock getETA() {
    	
    	if (marsClock == null)
    		marsClock = Simulation.instance().getMasterClock().getMarsClock();

        // Determine time difference between now and from start of trip in millisols.
        double millisolsDiff = MarsClock.getTimeDiff(marsClock, startTripTime);
        double hoursDiff = MarsClock.HOURS_PER_MILLISOL * millisolsDiff;

        // Determine average speed so far in km/hr.
        double avgSpeed = (startTripDistance - getDistanceToDestination()) / hoursDiff;

        // Determine estimated speed in km/hr.
        // Assume the crew will drive the overall 50 % of the time (including the time for stopping by various sites)
        double estimatorConstant = .5D;
        double estimatedSpeed = estimatorConstant *  getAverageVehicleSpeed(vehicle, worker); //(vehicle.getBaseSpeed() + getSpeedSkillModifier());

        // Determine final estimated speed in km/hr.
        double tempAvgSpeed = avgSpeed * ((startTripDistance - getDistanceToDestination()) / startTripDistance);
        double tempEstimatedSpeed = estimatedSpeed * (getDistanceToDestination() / startTripDistance);
        double finalEstimatedSpeed = tempAvgSpeed + tempEstimatedSpeed;

        // Determine time to destination in millisols.
        double hoursToDestination = getDistanceToDestination() / finalEstimatedSpeed;
        double millisolsToDestination = hoursToDestination / MarsClock.HOURS_PER_MILLISOL;// MarsClock.convertSecondsToMillisols(hoursToDestination * 60D * 60D);

        // Determine ETA
        MarsClock eta = new MarsClock(marsClock);
        eta.addTime(millisolsToDestination);

        return eta;
    }
    
    /**
     * Checks if vehicle has had an accident.
     * 
     * @param time the amount of time vehicle is driven (millisols)
     */
    protected abstract void checkForAccident(double time);
    
    
    /**
	 * Gets the lighting condition speed modifier.
	 * 
	 * @return speed modifier
	 */
	protected double getLightConditionModifier() {
		double light = surfaceFeatures.getSolarIrradiance(getVehicle().getCoordinates());
		if (light > 30)
			// one definition of night is when the irradiance is less than 30
			return 1;
		else //if (light > 0 && light <= 30)
			// Ground vehicles travel at a max of 30% speed at night.
			return light/300.0  + .29;
	}
	
	/**
	 * Gets the terrain speed modifier.
	 * 
	 * @param direction the direction of travel.
	 * @return speed modifier (0D - 1D)
	 */
	protected double getTerrainModifier(Direction direction) {
		double angleModifier = 0;
		double result = 0;
		
		if (vehicle instanceof Rover) {
			
			GroundVehicle vehicle = (GroundVehicle) getVehicle();
			// Get vehicle's terrain handling capability.
			double handling = vehicle.getTerrainHandlingCapability();
		
			// Determine modifier.
			angleModifier = handling - 10 + getEffectiveSkillLevel()/2D;
		
			if (angleModifier < 0D)
				angleModifier = Math.abs(1D / angleModifier);
			else if (angleModifier == 0D) {
				// Will produce a divide by zero otherwise
				angleModifier = 1D;
			}
		
			double tempAngle = Math.abs(vehicle.getTerrainGrade(direction) / angleModifier);
			if (tempAngle > HALF_PI)
				tempAngle = HALF_PI;
		
			result = Math.cos(tempAngle);
		}
		
		else {
			
			Flyer vehicle = (Flyer) getVehicle();
			// Determine modifier.
			angleModifier = getEffectiveSkillLevel()/2D - 5;
		
			if (angleModifier < 0D)
				angleModifier = Math.abs(1D / angleModifier);
			else if (angleModifier == 0D) {
				// Will produce a divide by zero otherwise
				angleModifier = 1D;
			}
		
			double tempAngle = Math.abs(vehicle.getTerrainGrade(direction) / angleModifier);
			if (tempAngle > HALF_PI)
				tempAngle = HALF_PI;

			result = Math.cos(tempAngle);
		}
		
		return result;
	}
	
    /**
     * Tests the speed.
     * 
     * @param direction
     * @return
     */
    protected double testSpeed(Direction direction) {

    	double speed = getAverageVehicleSpeed(vehicle, worker); 
        if (speed < 0D) {
        	speed = 0D;
        }
       
        return speed;
    }
    
    /**
     * Determines the speed modifier based on the driver's skill level.
     * 
     * @return speed modifier (km/hr)
     */
    protected double getSpeedSkillModifier() {
        if (person == null)
        	return 0;
        
    	double mod = 0D;
        double baseSpeed = vehicle.getBaseSpeed();
        int effectiveSkillLevel = getEffectiveSkillLevel();
        if (effectiveSkillLevel <= 5) {
            mod = 0D - ((baseSpeed / 4D) * ((5D - effectiveSkillLevel) / 5D));
        }
        else {
            double tempSpeed = baseSpeed;
            for (int x=0; x < effectiveSkillLevel - 5; x++) {
                tempSpeed /= 2D;
                mod += tempSpeed;
            }
        }
        
        if (person.getMind().getJob() == JobType.PILOT) {
        	mod += baseSpeed * 0.25; 
		}
		
		// Look up a person's prior pilot related training.
        mod += baseSpeed * getPilotingMod(person);
        
        return mod;
    }
    
	/**
	 * Calculates the piloting modifier for a Person based on their training.
	 * 
	 * @param operator
	 * @return
	 */
	private static double getPilotingMod(Person operator) {
		List<TrainingType> trainings = operator.getTrainings();
		double mod = 0;
		if (trainings.contains(TrainingType.AVIATION_CERTIFICATION))
			mod += .2;
		if (trainings.contains(TrainingType.FLIGHT_SAFETY))
			mod += .25;
		if (trainings.contains(TrainingType.NASA_DESERT_RATS))
			mod += .15;
		
		return mod;
	}
	
    /**
     * Gets the distance to the destination.
     * 
     * @return distance (km)
     */
    protected double getDistanceToDestination() {
    	return vehicle.getCoordinates().getDistance(destination);
    }
    
    /** 
     * Returns the elevation of the vehicle on the ground.
     * 
     *  @return elevation in km.
     */
    protected double getGroundElevation() {
        return TerrainElevation.getMOLAElevation(vehicle.getCoordinates());
    }
    
    /**
     * Stops the vehicle and removes operator.
     */
    protected void clearDown() {
    	if (vehicle != null) {
    		vehicle.setSpeed(0D);
    		// Need to set the vehicle operator to null before clearing the driving task 
        	vehicle.setOperator(null);
    	}
    }
    
    /**
     * Gets the average operating speed of a vehicle for a given operator.
     * 
     * @param vehicle the vehicle.
     * @param operator the vehicle operator.
     * @return average operating speed (km/h)
     */
    public static double getAverageVehicleSpeed(Vehicle vehicle, Worker operator) {
    	if (vehicle != null) {
    		// Need to update this to reflect the particular operator's average speed operating the vehicle.
    		double baseSpeed = vehicle.getBaseSpeed();
    		double mod = 0;
    		if (operator instanceof Person) {
    			Person p = (Person)operator;
    			
    			if (p.getMind().getJob() == JobType.PILOT) {
    				mod = baseSpeed; 
    			}
    			
    			// Look up a person's prior pilot related training.
    			mod += baseSpeed * getPilotingMod(p);
    			
    			int skill = p.getSkillManager().getEffectiveSkillLevel(SkillType.PILOTING);
    			if (skill <= 5) {
    				mod += - baseSpeed / 10 * (5D - skill) / 5;
    	        }
    	        else {
    	            double tempSpeed = baseSpeed;
    	            for (int x=0; x < skill - 5; x++) {
    	                tempSpeed /= 5;
    	                mod += tempSpeed;
    	            }
    	        }
    		}
    		
    		return baseSpeed + mod;
    	}
    	else
    		return 0;
    }
}

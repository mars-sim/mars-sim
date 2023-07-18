/*
 * Mars Simulation Project
 * OperateVehicle.java
 * @date 2023-04-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.util.List;
import java.util.logging.Level;

import org.mars.sim.mapdata.location.Coordinates;
import org.mars.sim.mapdata.location.Direction;
import org.mars.sim.mapdata.location.LocalPosition;
import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.environment.TerrainElevation;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionStatus;
import org.mars_sim.msp.core.person.ai.mission.NavPoint;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskManager;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.person.ai.training.TrainingType;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.time.MarsTime;
import org.mars_sim.msp.core.vehicle.Flyer;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleType;

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
    
    /** Need to provide oxygen as fuel oxidizer for the fuel cells. */
 	public static final int OXYGEN_ID = ResourceUtil.oxygenID;
     /** The fuel cells will generate 2.25 kg of water per 1 kg of methane being used. */
 	public static final int WATER_ID = ResourceUtil.waterID;
 	
 	private static final double THRESHOLD_SUNLIGHT = 60;
 	private static final double MAX_PERCENT_SPEED = 30;
 	
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
    /** The ratio of the amount of oxidizer to fuel. */
    private static final double RATIO_OXIDIZER_FUEL = 1.5;
    /** Distance buffer for arriving at destination (km). Good within 20 meters or 0.02 km. */
    public static final double DESTINATION_BUFFER = .02;
    /** The base percentage chance of an accident while operating vehicle per millisol. */
    public static final double BASE_ACCIDENT_CHANCE = .01D;
    
    private static final String KM = " km  ";	
    private static final String KPH = " kph  ";
	
	// Data members
	/** The fuel type id of this vehicle. */
	private int fuelTypeID;
	/** The distance [km] to the destination at the start of the trip. */
	private double startTripDistance; 

	/** The vehicle to operate. */ 
	private Vehicle vehicle;
	/** The location of the destination of the trip. */
	private Coordinates destination;
	/** The timestamp the trip is starting. */
	private MarsTime startTripTime;
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
			MarsTime startTripTime, double startTripDistance, double duration) {
		
		// Use Task constructor
		super(name, person, false, false, STRESS_MODIFIER, SkillType.PILOTING, EXP, duration);
		
		// Initialize data members.
		this.vehicle = vehicle;
		this.destination = destination;
		this.startTripTime = startTripTime;
		this.startTripDistance = startTripDistance;
		
        fuelTypeID = vehicle.getFuelTypeID();
		
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
			MarsTime startTripTime, double startTripDistance, double duration) {
		
		// Use Task constructor
		super(name, robot, false, false, STRESS_MODIFIER, SkillType.PILOTING, EXP, duration);
		
		// Initialize data members.
		this.vehicle = vehicle;
		this.destination = destination;
		this.startTripTime = startTripTime;
		this.startTripDistance = startTripDistance;
		
        fuelTypeID = vehicle.getFuelTypeID();
        
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
    	    logger.warning(worker, "No longer piloting " + getVehicle() + ".");
    	    return time;
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
	protected MarsTime getStartTripTime() {
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
        double mobilizedTime = mobilizeVehicle(time);
//        double timeUsed = time - mobilizedTime;
//		logger.log(vehicle, Level.CONFIG, 20_000, 
//				"time: " +  Math.round(time * 1000.0)/1000.0 + " millisols"
//				+ "  mobilizedTime: " +  Math.round(mobilizedTime * 1000.0)/1000.0 + " millisols"
//				+ "  timeUsed: " +  Math.round(timeUsed * 1000.0)/1000.0 + " millisols"
//				);

        // Add experience to the operator
        addExperience(mobilizedTime);
        
        // Check for accident.
        if (!isDone()) {
            checkForAccident(mobilizedTime);
        }
        
        // If vehicle has malfunction, end task.
        if (malfunctionManager.hasMalfunction()) {
            endTask();
        }
        
        return mobilizedTime;
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
    
        if (vehicle.isInSettlement()) 
        	return time;
  	
        double remainingFuel = -1;
        
        double remainingOxidizer = -1;
        
        if (fuelTypeID > 0) {
	        remainingFuel = vehicle.getAmountResourceStored(fuelTypeID);
	
	    	if (remainingFuel < LEAST_AMOUNT) {
	    		logger.log(vehicle, Level.SEVERE, 20_000L, 
						"Case A: Out of fuel. Cannot drive.");
	    		// Turn on emergency beacon
		    	turnOnBeacon(fuelTypeID);
	        	endTask();
	        	return time;
	    	}
	
	        remainingOxidizer = vehicle.getAmountResourceStored(OXYGEN_ID);
	
	    	if (remainingOxidizer < LEAST_AMOUNT * RATIO_OXIDIZER_FUEL) {
	    		logger.log(vehicle, Level.SEVERE, 20_000L, 
						"Case B: Out of fuel oxidizer. Cannot drive.");
	    		// Turn on emergency beacon
		    	turnOnBeacon(OXYGEN_ID);
	        	endTask();
	        	return time;
	        }
        }
        
        // Find the distance to destination.
        double dist2Dest = getDistanceToDestination();
       
        if (Double.isNaN(dist2Dest)) {
    		logger.log(vehicle, Level.SEVERE, 20_000L, 
					"Case C: Invalid distance.");
        	endTask();
        	return time;
        }
        
        // Convert time from millisols to hours
        double hrsTime = MarsTime.HOURS_PER_MILLISOL * time;
              
        // Case 2: Already arrived
        if (dist2Dest <= DESTINATION_BUFFER) {
        	logger.log(vehicle, Level.INFO,  20_000L, "Case I: Arrived at " + getNavpointName()
        			+ " (dist: " 
        			+ Math.round(dist2Dest * 1_000.0)/1_000.0 + " km).");

        	// Note: Need to consider the case in which VehicleMission's determineEmergencyDestination() causes the 
        	// the vehicle to switch the destination to a settlement when this settlement is within a very short
        	// distance away.
        	
        	// Sets final speed to zero and use regen braking to recharge the battery.
//        	vehicle.getController().adjustSpeed(hrsTime, dist, 0, remainingFuel, remainingOxidizer);
   
        	// Stop the vehicle
        	haltVehicle();
       
            if (isSettlementDestination())
                determineInitialSettlementParkedLocation();
            
			endTask();
        	return 0;
        }
        
        else {
        	// Vehicle is moving
        	return moveVehicle(hrsTime, dist2Dest, remainingFuel, remainingOxidizer) / MarsTime.HOURS_PER_MILLISOL;
        }
	} 
	
	/**
	 * Moves the vehicle by engaging its motor controller to compute fuel and energy usage and distance to be traversed.
	 * 
	 * @param hrsTime 			time [in hrs]
	 * @param dist2Dest 		distance to destination [in km]
	 * @param remainingFuel
	 * @param remainingOxidizer
	 * @return remaining hours
	 */
	private double moveVehicle(double hrsTime, double dist2Dest, double remainingFuel, double remainingOxidizer) {
		double remainingHrs = hrsTime;
    	// Gets initial speed in kph
    	double uKPH = vehicle.getSpeed(); 
    	// Gets initial speed in m/s
    	double uMS = uKPH / KPH_CONV;
    	// Gets the max allowable accel of this vehicle in m/s2
    	double maxAccel = vehicle.getAllowedAccel();
   
    	double skillMod = getSkillMod();
    	// Get the sunlight modifier
		double lightMod = getLightConditionModifier();
		  
    	// Get the terrain modifier
    	double terrainMod = getTerrainModifier(vehicle.getDirection());
    	
    	double topSpeedKPH = 0;
    	
    	if (vehicle.getVehicleType() == VehicleType.DELIVERY_DRONE) {
         	// Allow only 50% impact from lightMod
    		topSpeedKPH = vehicle.getBaseSpeed() * getSkillMod() * (0.5 * 0.5 * lightMod);
    	}
    	else {
        	// Gets top speed in kph allowed by this pilot 
    		// Allow only 30% impact from lightMod and 30% from terrain
        	topSpeedKPH = vehicle.getBaseSpeed() * getSkillMod() * (0.4 + 0.3 * lightMod + 0.3 * terrainMod);
    	}
    	
    	// Gets the ideal speed after acceleration. v^2 = u^2 + 2*a*d
		double idealSpeedMS = Math.sqrt(uMS * uMS + 2 * maxAccel * dist2Dest);
    	// Gets the ideal speed in kph
    	double idealSpeedKPH = idealSpeedMS * KPH_CONV;
    	// Gets the next speed to be used in kph      	
    	double nextSpeedKPH = Math.min(idealSpeedKPH, topSpeedKPH);
    	
		// Find the new possible speed
    	double vKPHProposed = Math.max(uKPH, nextSpeedKPH);
    	
      	// If staying at the same speed
    	double dist2Cover = vKPHProposed * hrsTime * KPH_CONV;
    	
//    	double newDistToCover = uMS * hrsTime * KPH_CONV;
    	
    	// Note that Case I: Arrived at destination in mobilizeVehicle()
    	
    	if (dist2Dest <= dist2Cover) {
    		// Case II: Will overshoot within this prescribed period of time if not slowing down or changing final velocity
    		// Slowing down the speed to expect to arrive
    		vKPHProposed = dist2Dest / hrsTime / KPH_CONV;
    		
          	logger.log(vehicle, Level.INFO, 1_000,  "Case II: Arriving soon at " + getNavpointName() 
	       		+ ". Slowing down. dist2Dest: " + Math.round(dist2Dest * 1_000.0)/1_000.0 + KM
	       		+ "distanceToCover: " + Math.round(dist2Cover * 1_000.0)/1_000.0 + KM
				+ "uKPH: " + + Math.round(uKPH * 1_000.0)/1_000.0 + KPH
				+ "vKPHProposed: " + + Math.round(vKPHProposed * 1_000.0)/1_000.0 + KPH  			
				+ "hrsTime: " + + Math.round(hrsTime * 1_000.0)/1_000.0 + " hrs  "
	           	+ "Time: " + + Math.round(hrsTime * 3600 * 1_000.0)/1_000.0 + " secs  "
	           	+ "maxAccel: " + Math.round(maxAccel * 1_000.0)/1_000.0 + " m/s2  "
	            + "skillMod: " + Math.round(skillMod * 100.0)/100.0 + "  "  
	            + "lightMod: " + Math.round(lightMod * 100.0)/100.0 + "  "
	            + "terrainMod: " + Math.round(terrainMod * 100.0)/100.0 + "  "
	           	+ "idealSpeedMS: " + Math.round(idealSpeedMS * 1_000.0)/1_000.0 + " m/s  "
	        	+ "topSpeedKPH: " + + Math.round(topSpeedKPH * 1_000.0)/1_000.0 + KPH
	           	+ "idealSpeedKPH: " + + Math.round(idealSpeedKPH * 1_000.0)/1_000.0 + KPH		
				+ "nextSpeedKPH: " + + Math.round(nextSpeedKPH * 1_000.0)/1_000.0 + KPH
    		);
          	
          	remainingHrs = vehicle.getController().consumeFuelEnergy(hrsTime, dist2Dest, vKPHProposed, remainingFuel, remainingOxidizer);
    	}
     	
        else {
        	// Case III: May speed up or slow down to get there, depending on terrain and sunlight
     	
          	logger.log(vehicle, Level.INFO, 1_000,  "Case III: Proceeding to " + getNavpointName() 
	       		+ ". dist2Dest: " + Math.round(dist2Dest * 1_000.0)/1_000.0 + KM
	       		+ "distanceToCover: " + Math.round(dist2Cover * 1_000.0)/1_000.0 + KM
				+ "uKPH: " + + Math.round(uKPH * 1_000.0)/1_000.0 + KPH
				+ "vKPHProposed: " + + Math.round(vKPHProposed * 1_000.0)/1_000.0 + KPH  			
				+ "hrsTime: " + + Math.round(hrsTime * 1_000.0)/1_000.0 + " hrs  "
	           	+ "Time: " + + Math.round(hrsTime * 3600 * 1_000.0)/1_000.0 + " secs  "
	           	+ "maxAccel: " + Math.round(maxAccel * 1_000.0)/1_000.0 + " m/s2  "
	            + "skillMod: " + Math.round(skillMod * 100.0)/100.0 + "  "  
	            + "lightMod: " + Math.round(lightMod * 100.0)/100.0 + "  "
	            + "terrainMod: " + Math.round(terrainMod * 100.0)/100.0 + "  "
	           	+ "idealSpeedMS: " + Math.round(idealSpeedMS * 1_000.0)/1_000.0 + " m/s  "
	        	+ "topSpeedKPH: " + + Math.round(topSpeedKPH * 1_000.0)/1_000.0 + KPH
	           	+ "idealSpeedKPH: " + + Math.round(idealSpeedKPH * 1_000.0)/1_000.0 + KPH		
				+ "nextSpeedKPH: " + + Math.round(nextSpeedKPH * 1_000.0)/1_000.0 + KPH
    		);
          	
        	remainingHrs = vehicle.getController().consumeFuelEnergy(hrsTime, dist2Cover, vKPHProposed, remainingFuel, remainingOxidizer);	
        }

		return remainingHrs;
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
        return CollectionUtils.isSettlement(destination);
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
    public MarsTime getETA() {
    	
        // Determine time difference between now and from start of trip in millisols.
        double millisolsDiff = getMarsTime().getTimeDiff(startTripTime);
        double hoursDiff = MarsTime.HOURS_PER_MILLISOL * millisolsDiff;

        // Determine average speed so far in km/hr.
        double avgSpeed = (startTripDistance - getDistanceToDestination()) / hoursDiff;

        // Determine estimated speed in km/hr.
        // Assume the crew will drive the overall 50 % of the time (including the time for stopping by various sites)
        double estimatorConstant = .5D;
        double estimatedSpeed = estimatorConstant *  getAverageVehicleSpeed(vehicle, worker);

        // Determine final estimated speed in km/hr.
        double tempAvgSpeed = avgSpeed * ((startTripDistance - getDistanceToDestination()) / startTripDistance);
        double tempEstimatedSpeed = estimatedSpeed * (getDistanceToDestination() / startTripDistance);
        double finalEstimatedSpeed = tempAvgSpeed + tempEstimatedSpeed;

        // Determine time to destination in millisols.
        double hoursToDestination = getDistanceToDestination() / finalEstimatedSpeed;
        double millisolsToDestination = hoursToDestination / MarsTime.HOURS_PER_MILLISOL;

        // Determine ETA
        return getMarsTime().addTime(millisolsToDestination);
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
	 * @return speed modifier between 0 and 1
	 */
	protected double getLightConditionModifier() {
		double light = surfaceFeatures.getSolarIrradiance(getVehicle().getCoordinates());
		// Assume ground vehicles travel at a max of MAX_PERCENT_SPEED at night.
		return (1 - MAX_PERCENT_SPEED/100) / THRESHOLD_SUNLIGHT * light + MAX_PERCENT_SPEED/100;
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
     * Gets an operator's average operating speed (modified by skill).
     * 
     * @param vehicle the vehicle.
     * @param operator the vehicle operator.
     * @return average operating speed (km/h)
     */
    public static double getAverageVehicleSpeed(Vehicle vehicle, Worker operator) {
    	if (vehicle != null) {
 
    		double baseSpeed = vehicle.getBaseSpeed();
    		double mod = 1;
    		
    		if (operator.getUnitType() == UnitType.PERSON) {
    			Person p = (Person)operator;
    			
    			// If the person's current job is pilot
    			if (p.getMind().getJob() == JobType.PILOT) {
    				mod += 0.25; 
    			}
  			
    			// Look up a person's prior pilot related training.
    			mod += getPilotingMod(p);
    			
    			int skill = p.getSkillManager().getEffectiveSkillLevel(SkillType.PILOTING);
    			if (skill <= 5) {
    				mod += .1 * skill / 5;
    	        }
    	        else {
    	            double tempSpeed = 1;
    	            for (int x = 0; x < skill - 5; x++) {
    	                tempSpeed /= 5;
    	                mod += tempSpeed;
    	            }
    	        }
    		}
    		
    		return baseSpeed * Math.min(1, mod);
    	}
    	else
    		return 0;
    }
    
    /**
     * Determines the skill modifier based on the driver's skill level.
     * 
     * @return dimension-less above 1 
     */
    protected double getSkillMod() {
    	double mod = 1;
     
    	if (worker.getUnitType() == UnitType.PERSON) {
			// If the person's current job is pilot
			if (person.getMind().getJob() == JobType.PILOT) {
				mod += 0.25; 
			}
			
			// Look up a person's prior pilot related training.
			mod += getPilotingMod(person) / 2;
    	}
		
        int skill = getEffectiveSkillLevel();
        if (skill <= 5) {
        	mod +=  .1 *  skill / 5;
        }
        else {
        	double tempSpeed = 1;
            for (int x = 0; x < skill - 5; x++) {
                tempSpeed /= 5;
                mod += tempSpeed;
            }
        }
        
        return Math.min(1, mod);
    }
    
	/**
	 * Calculates the piloting modifier for a Person based on their training.
	 * 
	 * @param operator
	 * @return a double between 0 and 1 
	 */
	private static double getPilotingMod(Person operator) {
		List<TrainingType> trainings = operator.getTrainings();
		double mod = 0;
		if (trainings.contains(TrainingType.AVIATION_CERTIFICATION))
			mod = .2;
		if (trainings.contains(TrainingType.FLIGHT_SAFETY))
			mod = .25;
		if (trainings.contains(TrainingType.NASA_DESERT_RATS))
			mod = .15;
		
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
     * Gets the name of the current navpoint.
     * 
     * @return
     */
    private String getNavpointName() {
    	Mission mission = vehicle.getMission();
    
    	if (mission instanceof VehicleMission) {
    		NavPoint np = ((VehicleMission) mission).getCurrentDestination();
    		return np.getDescription();
    	}
    	
    	return "";
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
    

}

/*
 * Mars Simulation Project
 * OperateVehicle.java
 * @date 2024-08-01
 * @author Scott Davis
 */
package com.mars_sim.core.vehicle.task;

import java.util.List;
import java.util.logging.Level;

import com.mars_sim.core.UnitType;
import com.mars_sim.core.computing.ComputingJob;
import com.mars_sim.core.computing.ComputingLoadType;
import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.environment.TerrainElevation;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.Direction;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionStatus;
import com.mars_sim.core.person.ai.mission.NavPoint;
import com.mars_sim.core.person.ai.mission.VehicleMission;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskManager;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.person.ai.training.TrainingType;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Drone;
import com.mars_sim.core.vehicle.Flyer;
import com.mars_sim.core.vehicle.GroundVehicle;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.VehicleType;

/**
 * The OperateVehicle class is an abstract task for operating a vehicle and
 * driving it to a destination.
 */
public abstract class OperateVehicle extends Task {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
	// default logger.
	private static final SimLogger logger = SimLogger.getLogger(OperateVehicle.class.getName());
	
	/** Task name */
	private static final String NAME = Msg.getString("Task.description.operateVehicle"); //$NON-NLS-1$

    /** Task phases. */
    public static final TaskPhase MOBILIZE = new TaskPhase(Msg.getString(
            "Task.phase.mobilize")); //$NON-NLS-1$
 	
    private static final int HRS_TO_SECS = 3600;
    /** Distance buffer for nearly arriving at destination (km). */
    public static final int DISTANCE_BUFFER_ARRIVING = 1;
    /** Distance buffer for nearly arriving at destination (km). */
    public static final int DISTANCE_DOUBLE_BUFFER_ARRIVING = DISTANCE_BUFFER_ARRIVING * 2;
    
 	private static final double THRESHOLD_SUNLIGHT = SurfaceFeatures.MEAN_SOLAR_IRRADIANCE;
 	/** The speed mod percent impacted by sunlight for flyers. */	
 	private static final double MAX_PERCENT_DRONE_SPEED = 60;
 	/** The speed mod percent impacted by sunlight for ground vehicles. */
 	private static final double MAX_PERCENT_GROUND_VEH_SPEED = 30;
 	/** The speed at which the collision phase commence. */
	protected static final double HIGH_SPEED = 100;
	/** The speed at which the obstacle / winching phase commence. */
	public static final double LOW_SPEED = 1;
	/** Conversion factor : 1 m/s = 3.6 km/h (or kph) */
	private static final double KPH_CONV = 3.6;
	/** Half the PI. */
	private static final double HALF_PI = Math.PI / 2D;
	/** Comparison to indicate a small but non-zero amount of fuel (methane) in kg that can still work on the fuel cell to propel the engine. */
    private static final double LEAST_AMOUNT = GroundVehicle.LEAST_AMOUNT;
    /** The ratio of the amount of oxidizer to fuel. */
    public static final double RATIO_OXIDIZER_FUEL = 1.5;
    /** Distance buffer for arriving at destination (km). */
    public static final double DISTANCE_BUFFER_ARRIVED = 0.2;
    /** The base percentage chance of an accident while operating vehicle per millisol. */
    public static final double BASE_ACCIDENT_CHANCE = .01D;
    
    private static final String KM = " km  ";	
    private static final String KPH = " kph  ";
	
	// Data members
	/** The fuel type id of this vehicle. */
	private int fuelTypeID = 0;
	/** The distance [km] to the destination at the start of the trip. */
	private double startTripDistance; 
	/** The last recorded distance [km] to the destination */
	private double lastDist;
	/** The vehicle to operate. */ 
	private Vehicle vehicle;
	/** The location of the destination of the trip. */
	private Coordinates destination;
	/** The timestamp the trip is starting. */
	private MarsTime startTripTime;
	/** The malfunctionManager of this vehicle. */
	private MalfunctionManager malfunctionManager;
	
	private ComputingJob compute;
	
	protected static final ExperienceImpact IMPACT = new ExperienceImpact(2D, NaturalAttributeType.EXPERIENCE_APTITUDE,
								false, 0.2D, SkillType.PILOTING);

	/**
	 * Constructor for a human pilot.
	 * 
	 * @param name the name of the particular task.
	 * @param operator the Worker performing the task.
	 * @param vehicle the vehicle to operate.
	 * @param destination the location of destination of the trip.
	 * @param startTripTime the time/date the trip is starting.
	 * @param startTripDistance the distance (km) to the destination at the start of the trip.
	 * @param duration the time duration (millisols) of the task (or 0 if none).
	 */
	protected OperateVehicle(String name, Worker operator, Vehicle vehicle, Coordinates destination, 
			MarsTime startTripTime, double startTripDistance, double duration) {
		
		// Use Task constructor
		super(name, operator, false, IMPACT, duration);
		
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

		// Check if there is a operator assigned to this vehicle.
		if (vo == null) 
			vehicle.setOperator(operator);
			
		else if (!operator.equals(vo)) {
        	// Remove the task from the last operator
	        clearDrivingTask(vo);
	        // Replace with the new operator
			vehicle.setOperator(operator);
		}
		
		// Walk to operation activity spot in vehicle.
		if (vehicle instanceof Rover rover) {
			walkToActivitySpotInRover(rover, rover.getOperatorActivitySpots(), false);
		}
		
		
        int now = getMarsTime().getMillisolInt();
        
        this.compute = new ComputingJob(worker.getAssociatedSettlement(), ComputingLoadType.MID, now, getDuration(), NAME);

        compute.pickMultipleNodes(0, now);
        
		// Set initial phase
		setPhase(MOBILIZE);
	
	}
	
    @Override
    protected double performMappedPhase(double time) {
    	if (getPhase() == null) {
    	    logger.warning(worker, 20_000, "No longer piloting " + getVehicle() + ".");
    	    return time;
    	}
    	else if (MOBILIZE.equals(getPhase())) {
    	    return moderateTime(time);
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
	 * Moderate the time for heating.
	 * 
	 * @param pulse
	 * @return remaining tme
	 */
	private double moderateTime(double time) {
		double remaining = time;
		double pTime = Task.getStandardPulseTime();
		if (pTime == 0.0) {
			pTime = remaining;
		}
		while (remaining > 0) {
			if (remaining > pTime) {
				// Consume the pulse time.
				double returnTime = mobilizeVehiclePhase(pTime);
				
				if (returnTime == pTime)
					return returnTime + remaining;
				else
					// Reduce the total time by the pulse time
					remaining -= pTime;
			}
			else {
				// Consume the pulse time.
				double returnTime = mobilizeVehiclePhase(remaining);
				
				if (returnTime == pTime)
					return returnTime;
				else
					// Reduce the total time by the pulse time
					remaining = 0;
			}
		}
		
		return 0;
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
  			
        if (vehicle.getSpeed() == 0D) {
        	// Note: must be larger than LOW_SPEED, or else it stays at obstacle phase
        	vehicle.setSpeed(LOW_SPEED * 1.1);
        	
        	logger.info(vehicle, 30_000, "Resetting the speed to " + Math.round(vehicle.getSpeed() * 10.0)/10.0 + " kph.");
        }
        	
        // Mobilize vehicle
        double mobilizedTime = mobilizeVehicle(time);
	   
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
        	
    	if (!vehicle.isBeaconOn() && (vehicle instanceof VehicleMission vm)) {
			MissionStatus status = MissionStatus.createResourceStatus(resource);
			vm.getHelp(status);
    	}
	}
	
	/**
	 * Turns on the emergency beacon to ask for help.
	 * 
	 * @param reason
	 */
	private void turnOnBeacon(String reason) {
    	
    	if (!vehicle.isBeaconOn() && (vehicle instanceof VehicleMission vm)) {
			MissionStatus status = MissionStatus.createResourceStatus(reason);
			vm.getHelp(status);
    	}
	}
	
	/**
	 * Checks if the worker is qualified to operate this vehicle.
	 * 
	 * @param time
	 * @return
	 */
	private boolean checkQualification(double time) {
		if (worker.getUnitType() == UnitType.ROBOT
	        	&& ((Robot)worker).getSystemCondition().isLowPower()) {
	        	logger.log(worker, Level.WARNING, 20_000,
	        			". Unable to pilot " + getVehicle() + " due to low power.");
		        endTask();
		        return false;
		}
        else if (worker.getUnitType() == UnitType.PERSON
            	&& ((Person)worker).isSuperUnfit()){
        	// For humans
        	logger.log(worker, Level.WARNING, 20_000,
        			". Super unfit to pilot " + getVehicle() + ".");
	        endTask();
	        return false;
        }
        
		if (time < 0) {
			logger.severe(vehicle, 20_000, "Negative time: " + time);
        	return false;
		}
       	  
        if (vehicle.isInSettlement()) 
        	return false;
        
        return true;
	}
	
	/**
	 * Moves the vehicle in its direction at its speed for the amount of time given.
	 * Stops if destination reached.
	 * 
	 * @param time the amount of time (ms) to drive.
	 * @return the amount of time (ms) left over after driving (if any)
	 */
	protected double mobilizeVehicle(double time) {
      
		if (!checkQualification(time))
			return time;
  	
        double remainingFuel = -1;
        
        double remainingOxidizer = -1;
        
    	// NOTE: Need to consider if the battery still have power and not just fuel

        double batteryEnergy = vehicle.getController().getBattery().getCurrentEnergy();
           
        boolean batteryOnly = false;
        
        // Case 0a to Case 0d
        if (fuelTypeID != 0) {
        	
        	remainingFuel = vehicle.getSpecificAmountResourceStored(fuelTypeID);
	
	    	if (remainingFuel < LEAST_AMOUNT) {
	    		logger.log(vehicle, Level.SEVERE, 20_000, 
						"Case 0a1: Ran out of fuel.");
	    		// Turn on emergency beacon
	    		turnOnBeacon(ResourceUtil.METHANOL_ID);
				vehicle.addSecondaryStatus(StatusType.OUT_OF_FUEL);
				
		    	if (batteryEnergy < LEAST_AMOUNT) {
		    		logger.log(vehicle, Level.SEVERE, 20_000, 
							"Case 0a2: Ran out of fuel and out of battery power. Cannot continue.");
		    		// Turn on emergency beacon
			    	turnOnBeacon("No battery power");
					vehicle.addSecondaryStatus(StatusType.OUT_OF_BATTERY_POWER);
		        	endTask();
		        	return time;
		    	}
		    	else {
		    		batteryOnly = true;
		    	}
	    	}

	        remainingOxidizer = vehicle.getSpecificAmountResourceStored(ResourceUtil.OXYGEN_ID);
	
	    	if (remainingOxidizer < LEAST_AMOUNT * RATIO_OXIDIZER_FUEL) {
	    		logger.log(vehicle, Level.SEVERE, 20_000, 
						"Case 0b1: Ran out of fuel oxidizer.");
	    		// Turn on emergency beacon
		    	turnOnBeacon(ResourceUtil.OXYGEN_ID);
				vehicle.addSecondaryStatus(StatusType.OUT_OF_OXIDIZER);
	        	
		    	if (batteryEnergy < LEAST_AMOUNT) {
		    		logger.log(vehicle, Level.SEVERE, 20_000, 
							"Case 0b2: Ran out of fuel and out of battery power. Cannot continue.");
		    		// Turn on emergency beacon
			    	turnOnBeacon("No battery power");
					vehicle.addSecondaryStatus(StatusType.OUT_OF_BATTERY_POWER);
		        	endTask();
		        	return time;
		    	}
		    	else {
		    		batteryOnly = true;
		    	}
	        }
        }
        
        else if (batteryEnergy < LEAST_AMOUNT){
        	logger.log(vehicle, Level.SEVERE, 20_000, 
					"Case 0c: Out of battery. Cannot drive.");
    		// Turn on emergency beacon
	    	turnOnBeacon("No battery power");
			vehicle.addSecondaryStatus(StatusType.OUT_OF_BATTERY_POWER);
        	endTask();
        	return time;
        }
        
        // Find the distance to destination.
        double dist2Dest = getDistanceToDestination();

        double speedFactor = 1;
        
        if (Double.isNaN(dist2Dest)) {
    		logger.log(vehicle, Level.SEVERE, 20_000, "Case 0d: Invalid distance.");
    		
        	endTask();
        	
        	return time;
        }
        
        // Look at the distance to be travelled
        return considerDistance(time, dist2Dest, remainingFuel, speedFactor, batteryOnly);
	}
        
	/**
	 * Considers if the vehicle has arrived at a destination.
	 * 
	 * @param time
	 * @param dist2Dest
	 * @param remainingFuel
	 * @param speedFactor
	 * @param batteryOnly
	 * @return
	 */
	private double considerDistance(double time, double dist2Dest, double remainingFuel, double speedFactor, boolean batteryOnly) {
		
        // Convert time from millisols to hours
        double hrsTime = MarsTime.HOURS_PER_MILLISOL * time;
       
        if (lastDist == dist2Dest && vehicle instanceof Drone) {
        	// If the drone is unable to move forward
        	lastDist = dist2Dest;
        	
        	// Set speedFactor to 0.0
        	speedFactor = 0.0;
        	
        	return moveVehicle(hrsTime, dist2Dest, speedFactor, remainingFuel) 
        			/ MarsTime.HOURS_PER_MILLISOL;
        }
        
        // Case 0e: Arriving if within 1 km
        if (batteryOnly) {
        	// No more fuel but still have battery
        	logger.log(vehicle, Level.INFO,  20_000, "Case 0e: Battery only (Out of fuel or oxidizer). Heading " 
        			+ getNavpointName()
        			+ " (dist: " + Math.round(dist2Dest * 1_000.0)/1_000.0 + " km).");

        	// Set speedFactor to 0.5
        	speedFactor = 0.5;
        	
        	lastDist = dist2Dest;
        	
        	return moveVehicle(hrsTime, dist2Dest, speedFactor, remainingFuel) 
        			/ MarsTime.HOURS_PER_MILLISOL;
        }

        // Case Ia: Just arrived if within 200 m
        if (dist2Dest <= DISTANCE_BUFFER_ARRIVED) {
        	logger.log(vehicle, Level.INFO, 20_000, "Case Ia: Arrived at " + getNavpointName()
        			+ " (dist: " + Math.round(dist2Dest * 1_000.0)/1_000.0 + " km).");
        	
        	lastDist = dist2Dest;
        	
        	// Stop the vehicle
        	haltVehicle();
       
            if (isSettlementDestination())
                determineInitialSettlementParkedLocation();
            
			endTask();
			
			return time;
        }
        
        else {
        	
        	lastDist = dist2Dest;
        	
        	// Case II and Case III : Propel the vehicle further
        	// Note: Divide it by HOURS_PER_MILLISOL to convert hour back to millisols
        	return moveVehicle(hrsTime, dist2Dest, speedFactor, remainingFuel) 
        			/ MarsTime.HOURS_PER_MILLISOL;
        }
	}
	
	/**
	 * Sets the last recorded distance travelled.
	 * 
	 * @param dist
	 */
	public void setLastDistance(double dist) {
		lastDist = dist;
	}
	
	/**
	 * Moves the vehicle by engaging its motor controller to compute fuel and energy usage and distance to be traversed.
	 * 
	 * @param hrsTime 			time [in hrs]
	 * @param dist2Dest 		distance to destination [in km]
	 * @param speedFactor
	 * @param remainingFuel
	 * @param remainingOxidizer
	 * @return remaining hours
	 */
	private double moveVehicle(double hrsTime, double dist2Dest, double speedFactor, double remainingFuel) {
		double remainingHrs;
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
    	
    	// Request for computing resources for processing field condition
		compute.process(getTimeCompleted(), getMarsTime().getMillisolInt());

    	if (vehicle instanceof Drone) {
         	// Allow only 50% impact from lightMod
    		topSpeedKPH = vehicle.getBaseSpeed() * (1 + speedFactor) / 2 * getSkillMod() * (0.5 + 0.5 * lightMod);
    	}
    	else {
        	// Gets top speed in kph allowed by this pilot 
    		// Allow only 30% impact from lightMod and 30% from terrain
        	topSpeedKPH = vehicle.getBaseSpeed() * (1 + speedFactor) / 2 * getSkillMod() * (0.4 + 0.3 * lightMod + 0.3 * terrainMod);
    	}
    	
    	// Gets the ideal speed with acceleration. may use v^2 = u^2 + 2*a*d
    	double idealSpeedMS = uMS + (1 + speedFactor) / 2 * maxAccel * hrsTime * HRS_TO_SECS;
    	// Gets the ideal speed in kph
    	double idealSpeedKPH = idealSpeedMS * KPH_CONV;
    	// Gets the next speed to be used in kph      	
    	double nextSpeedKPH = Math.min(idealSpeedKPH, topSpeedKPH);	
		// Find the new possible speed
    	double vKPHProposed = Math.max(uKPH, nextSpeedKPH);
      	// If staying at the same speed
    	double dist2Cover = vKPHProposed * hrsTime * KPH_CONV;
    	
    	if (dist2Dest <= dist2Cover) {
    		// Case II: Will overshoot within this prescribed period of time if not slowing down or changing final velocity
    		// Slowing down the speed to expect to arrive
    		
    		if (VehicleType.isDrone(vehicle.getVehicleType())) {
    			// For flyers
    			if (dist2Dest <= DISTANCE_BUFFER_ARRIVING) {
	    	   		// For drone, need to slow down much more to avoid overshot
	        		vKPHProposed = vKPHProposed / 1.7;
    			}
    			else if (dist2Dest <= DISTANCE_DOUBLE_BUFFER_ARRIVING) {
	    	   		// For drone, need to slow down much more to avoid overshot
	        		vKPHProposed = vKPHProposed / 1.4;
    			}
        	}
        	else if (VehicleType.isRover(vehicle.getVehicleType())) {
        		// For ground vehicles
        		if (dist2Dest <= DISTANCE_BUFFER_ARRIVING) {

	        		vKPHProposed = vKPHProposed / 1.5;
    			}
        		else if (dist2Dest <= DISTANCE_DOUBLE_BUFFER_ARRIVING) {

	        		vKPHProposed = vKPHProposed / 1.25;
    			}
        	}	
    		
          	logger.log(vehicle, Level.INFO, 20_000,  
          		"Case II: Slowing down. Arrive soon at " + getNavpointName() 
	       		+ ". dist2Dest: " + Math.round(dist2Dest * 1_000.0)/1_000.0 + KM
	       		+ "dist2Cover: " + Math.round(dist2Cover * 1_000.0)/1_000.0 + KM
				+ "u -> v: " + + Math.round(uKPH * 100.0)/100.0
					+ " -> " + Math.round(vKPHProposed * 100.0)/100.0 + KPH  			
				+ "hrsTime: " + + Math.round(hrsTime * 1_000.0)/1_000.0 + " hrs  "
	           	+ "Time: " + + Math.round(hrsTime * HRS_TO_SECS * 1_000.0)/1_000.0 + " secs  "
	           	+ "maxAccel: " + Math.round(maxAccel * 1_000.0)/1_000.0 + " m/s2  "
	            + "skillMod: " + Math.round(skillMod * 100.0)/100.0 + "  "  
	            + "lightMod: " + Math.round(lightMod * 100.0)/100.0 + "  "
	            + "terrainMod: " + Math.round(terrainMod * 100.0)/100.0 + "  "
	           	+ "idealSpeedMS: " + Math.round(idealSpeedMS * 1_000.0)/1_000.0 + " m/s  "
	        	+ "topSpeedKPH: " + + Math.round(topSpeedKPH * 1_000.0)/1_000.0 + KPH
	           	+ "idealSpeedKPH: " + + Math.round(idealSpeedKPH * 1_000.0)/1_000.0 + KPH		
				+ "nextSpeedKPH: " + + Math.round(nextSpeedKPH * 1_000.0)/1_000.0 + KPH
    		);
          	
          	remainingHrs = vehicle.getController().consumeFuelEnergy(
          			hrsTime, dist2Dest, vKPHProposed, remainingFuel);
    	}
     	
        else {
        	// Case III: May speed up or slow down to get there, depending on terrain and sunlight
     	
          	logger.log(vehicle, Level.INFO, 20_000,  
          		"Case III: Proceeding to " + getNavpointName() 
	       		+ ". dist2Dest: " + Math.round(dist2Dest * 1_000.0)/1_000.0 + KM
	       		+ "distanceToCover: " + Math.round(dist2Cover * 1_000.0)/1_000.0 + KM
				+ "u -> v: " + + Math.round(uKPH * 100.0)/100.0
				+ " -> " + Math.round(vKPHProposed * 100.0)/100.0 + KPH  			
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
          	
        	remainingHrs = vehicle.getController().consumeFuelEnergy(
        			hrsTime, dist2Cover, vKPHProposed, remainingFuel);	
        }

		return remainingHrs;
	}
	
        
	/**
	 * Stops the vehicle.
	 */
	public void haltVehicle() {
		// Note: instead of wasting the momentum/energy, 
		// Calculate the power stored based on regenerative braking
		
		logger.info(vehicle, "Halted the vehicle.");		
		// Set speed to zero
		vehicle.setSpeed(0D);
		// Determine new position.
		vehicle.setCoordinates(destination);
		// Remove the vehicle operator
		vehicle.setOperator(null);
		
		if (vehicle instanceof Drone drone) {
			// Set to park
			vehicle.setPrimaryStatus(StatusType.PARKED);
			// Reduce the hovering height to zero
			drone.setHoveringHeight(0);
		}
		
		// Update the elevation
		updateVehicleElevationAltitude();
	}	

	/**
	 * Checks if the destination is at the location of a settlement.
	 * 
	 * @return true if destination is at a settlement location.
	 */
	private boolean isSettlementDestination() {
        return unitManager.isSettlement(destination);
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

        double distanceToDestination = getDistanceToDestination();
        
        // Determine average speed so far in km/hr.
        double avgSpeed = (startTripDistance - distanceToDestination) / hoursDiff;

        // Determine estimated speed in km/hr.
        // Assume the crew will drive the overall 50 % of the time (including the time for stopping by various sites)
        double estimatorConstant = .5D;
        double estimatedSpeed = estimatorConstant *  getAverageVehicleSpeed(vehicle, worker);

        // Determine final estimated speed in km/hr.
        double tempAvgSpeed = avgSpeed * ((startTripDistance - distanceToDestination) / startTripDistance);
        double tempEstimatedSpeed = estimatedSpeed * (distanceToDestination / startTripDistance);
        double finalEstimatedSpeed = tempAvgSpeed + tempEstimatedSpeed;

        // Determine time to destination in millisols.
        double hoursToDestination = distanceToDestination / finalEstimatedSpeed;
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
		double light = surfaceFeatures.getSolarIrradiance(getVehicle().getCoordinates())/SurfaceFeatures.MEAN_SOLAR_IRRADIANCE;

		if (vehicle instanceof Flyer) {
			return (1 - MAX_PERCENT_DRONE_SPEED/100) / THRESHOLD_SUNLIGHT * light + MAX_PERCENT_DRONE_SPEED/100;
		}
		else {
			// Assume ground vehicles travel at a max of MAX_PERCENT_SPEED at night.
			return (1 - MAX_PERCENT_GROUND_VEH_SPEED/100) / THRESHOLD_SUNLIGHT * light + MAX_PERCENT_GROUND_VEH_SPEED/100;
		}
	}

	/**
	 * Gets the terrain speed modifier.
	 * 
	 * @param direction the direction of travel.
	 * @return speed modifier (0D - 1D)
	 */
	protected double getTerrainModifier(Direction direction) {
		double angleModifier;
		double terrainGrade;
		if (vehicle instanceof GroundVehicle gvehicle) {			
			// Get vehicle's terrain handling capability.
			double handling = gvehicle.getTerrainHandlingCapability();
	
			// Determine modifier.
			angleModifier = handling - 10 + getEffectiveSkillLevel()/2D;
			terrainGrade = gvehicle.getTerrainGrade(direction);
		}
		
		else if (vehicle instanceof Flyer fvehicle) {
			// Determine modifier.
			angleModifier = getEffectiveSkillLevel()/2D - 5;
			terrainGrade = fvehicle.getTerrainGrade(direction);
		}
		else {
			// THis will only happen if a new Vehicle subtype is created
			// Thi can be convert to a typed switch in Java 21
			throw new IllegalStateException("Cannot operate vehicle of type " + vehicle);
		}

		if (angleModifier < 0D)
			angleModifier = Math.abs(1D / angleModifier);
		else if (angleModifier == 0D) {
			// Will produce a divide by zero otherwise
			angleModifier = 1D;
		}
	
		double tempAngle = Math.abs(terrainGrade / angleModifier);
		if (tempAngle > HALF_PI)
			tempAngle = HALF_PI;

		return Math.cos(tempAngle);
	}
	
    /**
     * Tests the speed.
     * 
     * @param direction Direction may be used by overriding version
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
    		
    		if (operator instanceof Person p) {
    			
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
    	return TerrainElevation.getAverageElevation(vehicle.getCoordinates());
    }
    
    /**
     * Gets the name of the current navpoint.
     * 
     * @return
     */
    private String getNavpointName() {
    	Mission mission = vehicle.getMission();
    
    	if (mission instanceof VehicleMission vm) {
    		NavPoint np = vm.getCurrentDestination();
    		return np.getDescription();
    	}
    	
    	return "a navpoint";
    }
    
    /**
     * Stops the vehicle and removes operator.
     */
	@Override
    protected void clearDown() {
    	if (vehicle != null) {
    		vehicle.setSpeed(0D);
    		// Need to set the vehicle operator to null before clearing the driving task 
        	vehicle.setOperator(null);
    	}

		super.clearDown();
    }
}

/**
 * Mars Simulation Project
 * OperateVehicle.java
 * @version 3.1.0 2017-09-13
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Direction;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.Pilot;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionStatus;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskManager;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleOperator;

/**
 * The OperateVehicle class is an abstract task for operating a vehicle, 
 * driving it to a destination.
 */
public abstract class OperateVehicle extends Task implements Serializable {
	
    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
	private static Logger logger = Logger.getLogger(OperateVehicle.class.getName());
	private static String loggerName = logger.getName();
	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());

    /** Task phases. */
    protected static final TaskPhase MOBILIZE = new TaskPhase(Msg.getString(
            "Task.phase.mobilize")); //$NON-NLS-1$
	
	/** Comparison to indicate a small but non-zero amount of fuel (methane) in kg that can still work on the fuel cell to propel the engine. */
    private static final double LEAST_AMOUNT = RoverMission.LEAST_AMOUNT;
	
    // Distance buffer for arriving at destination (km).
    private final static double DESTINATION_BUFFER = .001D;
    
    // The base percentage chance of an accident while operating vehicle per millisol.
    public static final double BASE_ACCIDENT_CHANCE = .01D; 
	
	// Data members
	private double startTripDistance; // The distance (km) to the destination at the start of the trip.
	   
	private Vehicle vehicle; // The vehicle to operate.
	private Coordinates destination; // The location of the destination of the trip.
	private MarsClock startTripTime; // The time/date the trip is starting.
	
	private MalfunctionManager malfunctionManager;
	

	/**
	 * Default Constructor
	 * @param name the name of the particular task.
	 * @param person the person performing the task.
	 * @param vehicle the vehicle to operate.
	 * @param destination the location of destination of the trip.
	 * @param startTripTime the time/date the trip is starting.
	 * @param startTripDistance the distance (km) to the destination at the start of the trip.
	 * @param stressModifier the modifier for stress on the person performing the task.
	 * @param hasDuration does the task have a time duration?
	 * @param duration the time duration (millisols) of the task (or 0 if none).
	 */
	public OperateVehicle(String name, Person person, Vehicle vehicle, Coordinates destination, 
			MarsClock startTripTime, double startTripDistance, double stressModifier, 
			boolean hasDuration, double duration) {
		
		// Use Task constructor
		super(name, person, false, false, stressModifier, hasDuration, duration);
		
		// Check for valid parameters.
		if (vehicle == null) {
		    throw new IllegalArgumentException("vehicle is null");
		}
		
		// Select the vehicle operator
		VehicleOperator vo = vehicle.getOperator();
		Person driver = (Person) vo;
		
        // Check if person is the vehicle operator.
		if (vo == null) 
			vehicle.setOperator(person);
			
		else if (!person.equals(driver)) {
        	// Remove the task from the last driver
	        clearDrivingTask(vo);
	        // Replace the driver
			vehicle.setOperator(person);
		}	
	
		if (destination == null) {
		    throw new IllegalArgumentException("destination is null");
		}
		
		if (startTripTime == null) {
		    throw new IllegalArgumentException("startTripTime is null");
		}
		
		if (startTripDistance < 0D) {
		    throw new IllegalArgumentException("startTripDistance is < 0");
		}
		
		// Initialize data members.
		this.vehicle = vehicle;
		this.destination = destination;
		this.startTripTime = startTripTime;
		this.startTripDistance = startTripDistance;
		
//		surface = Simulation.instance().getMars().getSurfaceFeatures();
		malfunctionManager = vehicle.getMalfunctionManager();
		
		// Walk to operation activity spot in vehicle.
		if (vehicle instanceof Rover) {
		    walkToOperatorActivitySpotInRover((Rover) vehicle, false);
		}
		
		addPhase(MOBILIZE);
		
		// Set initial phase
		setPhase(MOBILIZE);
	}
	
	public OperateVehicle(String name, Robot robot, Vehicle vehicle, Coordinates destination, 
			MarsClock startTripTime, double startTripDistance, double stressModifier, 
			boolean hasDuration, double duration) {
		
		// Use Task constructor
		super(name, robot, false, false, stressModifier, hasDuration, duration);
		
		// Check for valid parameters.
		if (vehicle == null) {
		    throw new IllegalArgumentException("vehicle is null");
		}
		
		if (destination == null) {
		    throw new IllegalArgumentException("destination is null");
		}
		if (startTripTime == null) {
		    throw new IllegalArgumentException("startTripTime is null");
		}
		if (startTripDistance < 0D) {
		    throw new IllegalArgumentException("startTripDistance is < 0");
		}
		
		// Initialize data members.
		this.vehicle = vehicle;
		this.destination = destination;
		this.startTripTime = startTripTime;
		this.startTripDistance = startTripDistance;
		
//		surface = Simulation.instance().getMars().getSurfaceFeatures();
		malfunctionManager = vehicle.getMalfunctionManager();
		
		// Walk to operation activity spot in vehicle.
		if (vehicle instanceof Rover) {
		    walkToOperatorActivitySpotInRover((Rover) vehicle, false);
		}
		
		addPhase(MOBILIZE);
		
		// Set initial phase
		setPhase(MOBILIZE);
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
	 * @return vehicle
	 */
	public Vehicle getVehicle() {
		return vehicle;
	}
	
	/** 
	 * Gets the location of the destination of the trip.
	 * @return location of destination
	 */
	public Coordinates getDestination() {
		return destination;
	}
	
	/**
	 * Sets the location of the destination of this trip.
	 * @param newDestination location of the destination.
	 */
	public void setDestination(Coordinates newDestination) {
		this.destination = newDestination;
	}
	
	/**
	 * Gets the time/date the trip was started on.
	 * @return start time
	 */
	protected MarsClock getStartTripTime() {
		return startTripTime;
	}
	
	/**
	 * Gets the distance to the destination at the start of the trip.
	 * @return distance (km) to destination.
	 */
	protected double getStartTripDistance() {
		return startTripDistance;
	}
	
	protected void clearDrivingTask(VehicleOperator vo) {
		if (vo != null) {
        	// Clear the OperateVehicle task from the last driver
			TaskManager taskManager = ((Person) vo).getMind().getTaskManager();
			taskManager.clearSpecificTask(DriveGroundVehicle.class.getSimpleName());
			taskManager.clearSpecificTask(OperateVehicle.class.getSimpleName());
        	taskManager.getNewTask();	
    	}
	}
	
	/**
	 * Perform the mobilize vehicle phase for the amount of time given.
	 * @param time the amount of time (ms) to perform the phase.
	 * @return the amount of time left over after performing the phase.
	 */
	protected double mobilizeVehiclePhase(double time) {
		
        // Find current direction and update vehicle.
        vehicle.setDirection(vehicle.getCoordinates().getDirectionToPoint(destination));
        
        // Find current elevation/altitude and update vehicle.
        updateVehicleElevationAltitude();

        // Update vehicle speed.
        double speed = getSpeed(vehicle.getDirection());
        vehicle.setSpeed(speed);
        
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
	
	private void turnOnBeacon() {
		vehicle.setSpeed(0D);
    	if (!vehicle.haveStatusType(StatusType.OUT_OF_FUEL))
    		vehicle.addStatus(StatusType.OUT_OF_FUEL);
    	if (vehicle.haveStatusType(StatusType.MOVING))
    		vehicle.removeStatus(StatusType.MOVING);
    	
    	if (!vehicle.isBeaconOn()) {
    		Mission m = vehicle.getMission();
    		((VehicleMission)m).setEmergencyBeacon(person, vehicle, true, MissionStatus.NO_METHANE.getName());
    		m.addMissionStatus(MissionStatus.NO_METHANE);
    		((VehicleMission)m).getHelp();
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
        double result = 0;
        double distanceTraveled = 0;
        
        // Find starting distance to destination.
        double startingDistanceToDestination = getDistanceToDestination();
		
        Inventory vInv = vehicle.getInventory();
        int fuelType = vehicle.getFuelType();
        
        double remainingFuel = vInv.getAmountResourceStored(fuelType, false);

        if (!vehicle.isInSettlement() && remainingFuel < LEAST_AMOUNT) {
        	// Case 1 : no fuel left
        	// TODO: need to turn on emergency beacon and ask for rescue here or in RoverMission ?
	    	LogConsolidated.log(Level.SEVERE, 30_000, sourceName, "[" + vehicle.getName() + "] " 
					+ "ran out of methane. Cannot drive.");

	    	// Turn on emergency beacon
	    	turnOnBeacon();
        	
        	endTask();
        	return time;
        }
        
        
        // Determine distance traveled in time given.
        double hrsTime = MarsClock.HOURS_PER_MILLISOL * time;

        // Consume fuel for distance traveled.
        double fuelNeeded = distanceTraveled / vehicle.getIFuelEconomy();
        
        if (fuelNeeded > remainingFuel) {
        	// Case 2 : just used up the last drop of fuel 
        	fuelNeeded = remainingFuel;
        	
        	try {
		    	vInv.retrieveAmountResource(fuelType, fuelNeeded);
	    
		    }
		    catch (Exception e) {
		    	LogConsolidated.log(Level.SEVERE, 0, sourceName, "[" + vehicle.getName() + "] " 
						+ "can't retrieve methane. Cannot drive.");

		    	// Turn on emergency beacon
		    	turnOnBeacon();
		    	
	        	endTask();
	        	return time;
		    }
        	
        	// Update and reduce the distanceTraveled since there is not enough fuel
        	distanceTraveled = fuelNeeded * vehicle.getIFuelEconomy();
        	if (!vehicle.haveStatusType(StatusType.MOVING))
        		vehicle.addStatus(StatusType.MOVING);
        	if (vehicle.haveStatusType(StatusType.OUT_OF_FUEL))
        		vehicle.removeStatus(StatusType.OUT_OF_FUEL);
        	
            // Add distance traveled to vehicle's odometer.
//        	vehicle.addOdometerReading(distanceTraveled);
            vehicle.addTotalDistanceTraveled(distanceTraveled);
            vehicle.addDistanceLastMaintenance(distanceTraveled);
        	return time - MarsClock.MILLISOLS_PER_HOUR * distanceTraveled / vehicle.getSpeed();
        	
        }
        
        else {
        	
            if (startingDistanceToDestination <= (distanceTraveled + DESTINATION_BUFFER)) {
                // Case 3 : if starting distance to destination is less than distance traveled, stop at destination.
                
            	distanceTraveled = startingDistanceToDestination;
                
                // Update the fuel needed for distance traveled.
                fuelNeeded = distanceTraveled / vehicle.getIFuelEconomy();
                
                try {
    		    	vInv.retrieveAmountResource(fuelType, fuelNeeded);
   
    		    }
    		    catch (Exception e) {
    		    	LogConsolidated.log(Level.SEVERE, 0, sourceName, "[" + vehicle.getName() + "] " 
    						+ "can't retrieve methane. Cannot drive.");

    		    	// Turn on emergency beacon
    		    	turnOnBeacon();
    		    	
    	        	endTask();
    	        	return time;
    		    }
                
 		    	
		        // Add distance traveled to vehicle's odometer.
		        vehicle.addTotalDistanceTraveled(distanceTraveled);
		        vehicle.addDistanceLastMaintenance(distanceTraveled);
		        
	            vehicle.setCoordinates(destination);
                vehicle.setSpeed(0D);
                if (!vehicle.haveStatusType(StatusType.PARKED))
                	vehicle.addStatus(StatusType.PARKED);
                if (vehicle.haveStatusType(StatusType.MOVING))
                	vehicle.removeStatus(StatusType.MOVING);
                vehicle.setOperator(null);
                updateVehicleElevationAltitude();
                if (isSettlementDestination()) {
                    determineInitialSettlementParkedLocation();
                }
                else {
                    double radDir = vehicle.getDirection().getDirection();
                    double degDir = radDir * 180D / Math.PI;
                    vehicle.setParkedLocation(0D, 0D, degDir);
                }
                
                // Calculate the remaining time
                result = time - MarsClock.MILLISOLS_PER_HOUR * distanceTraveled / vehicle.getSpeed();
//	            endTask();
	                
            }
            
            else {
            	
            	// Case 4 : the rover may use all the prescribed time to drive 
                distanceTraveled = hrsTime * vehicle.getSpeed();
                
             // Update the fuel needed for distance traveled.
                fuelNeeded = distanceTraveled / vehicle.getIFuelEconomy();
                
                try {
    		    	vInv.retrieveAmountResource(fuelType, fuelNeeded);

    		    }
    		    catch (Exception e) {
    		    	LogConsolidated.log(Level.SEVERE, 0, sourceName, "[" + vehicle.getName() + "] " 
    						+ "can't retrieve methane. Cannot drive.");

    		    	// Turn on emergency beacon
    		    	turnOnBeacon();
    		    	
    	        	endTask();
    	        	return time;
    		    }
                
		    	
                // Determine new position.
                vehicle.setCoordinates(vehicle.getCoordinates().getNewLocation(vehicle.getDirection(), distanceTraveled));
                
                // Add distance traveled to vehicle's odometer.
                vehicle.addTotalDistanceTraveled(distanceTraveled);
                vehicle.addDistanceLastMaintenance(distanceTraveled);
                
                // Use up all of the available time
                result = 0; 
            }   
        }

        return result;
	}
	
	/**
	 * Checks if the destination is at the location of a settlement.
	 * @return true if destination is at a settlement location.
	 */
	private boolean isSettlementDestination() {
//	    boolean result = false;
	    if (CollectionUtils.findSettlement(destination) instanceof Settlement)
	    	return true;
//	    if (unitManager == null)
//	    	unitManager = Simulation.instance().getUnitManager();
//	    Iterator<Settlement> i = unitManager.getSettlements().iterator();
//	    Iterator<Settlement> i = CollectionUtils.getSettlement(units).iterator();
//	    while (i.hasNext()) {
//	        Settlement settlement = i.next();
//	        // Note: This settlement does not have to be the vehicle's associated settlement
//	        if (settlement.getCoordinates().equals(destination)) {
//	            result = true;
//	        }
//	    }
//	    return result;
	    return false;
	}
	
	/**
	 * Determine the vehicle's initial parked location while traveling to settlement.
	 */
	private void determineInitialSettlementParkedLocation() {
	    
	    Direction oppDir = new Direction(vehicle.getDirection().getDirection() + Math.PI);
	    double distance = 200D;
	    double xLoc = 0D - (distance * oppDir.getSinDirection());
        double yLoc = distance * oppDir.getCosDirection();
        double degDir = vehicle.getDirection().getDirection() * 180D / Math.PI;
	    
        vehicle.setParkedLocation(xLoc, yLoc, degDir);
	}
	
	/**
	 * Update vehicle with its current elevation or altitude.
	 */
	protected abstract void updateVehicleElevationAltitude();
	
    /** 
     * Determines the ETA (Estimated Time of Arrival) to the destination.
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
        double estimatedSpeed = estimatorConstant * (vehicle.getBaseSpeed() + getSpeedSkillModifier());

        // Determine final estimated speed in km/hr.
        double tempAvgSpeed = avgSpeed * ((startTripDistance - getDistanceToDestination()) / startTripDistance);
        double tempEstimatedSpeed = estimatedSpeed * (getDistanceToDestination() / startTripDistance);
        double finalEstimatedSpeed = tempAvgSpeed + tempEstimatedSpeed;

        // Determine time to destination in millisols.
        double hoursToDestination = getDistanceToDestination() / finalEstimatedSpeed;
        double millisolsToDestination = hoursToDestination / MarsClock.HOURS_PER_MILLISOL;// MarsClock.convertSecondsToMillisols(hoursToDestination * 60D * 60D);

        // Determine ETA
        MarsClock eta = (MarsClock) marsClock.clone();
        eta.addTime(millisolsToDestination);

        return eta;
    }
    
    /**
     * Check if vehicle has had an accident.
     * @param time the amount of time vehicle is driven (millisols)
     */
    protected abstract void checkForAccident(double time);
    
    /** 
     * Determine vehicle speed for a given direction.
     * @param direction the direction of travel
     * @return speed in km/hr
     */
    protected double getSpeed(Direction direction) {

        double speed = vehicle.getBaseSpeed() + getSpeedSkillModifier();
        if (speed < 0D) {
            speed = 0D;
        }

        return speed;
    }
    
    /**
     * Determine the speed modifier based on the driver's skill level.
     * @return speed modifier (km/hr)
     */
    protected double getSpeedSkillModifier() {
    	double mod = 0D;
        double baseSpeed = vehicle.getBaseSpeed();
        if (getEffectiveSkillLevel() <= 5) {
            mod = 0D - ((baseSpeed / 4D) * ((5D - getEffectiveSkillLevel()) / 5D));
        }
        else {
            double tempSpeed = baseSpeed;
            for (int x=0; x < getEffectiveSkillLevel() - 5; x++) {
                tempSpeed /= 2D;
                mod += tempSpeed;
            }
        }
        
        if (person.getJobName().equalsIgnoreCase(Pilot.class.getSimpleName())) {
        	mod += baseSpeed * 0.25; 
		}
		
		// Look up a person's prior pilot related training.
        mod += baseSpeed * person.getPilotingMod();
        	
        // Check for any crew emergency
//        System.out.println("vehicle : " + vehicle);
//        System.out.println("vehicle.getMission() : " + vehicle.getMission());
        if (vehicle.getMission() != null && vehicle.getMission().hasEmergencyAllCrew())
			mod += baseSpeed * 0.25;
		
        return mod;
    }
    
    /**
     * Gets the distance to the destination.
     * @return distance (km)
     */
    protected double getDistanceToDestination() {
    	return vehicle.getCoordinates().getDistance(destination);
    }
    
    /** Returns the elevation at the vehicle's position.
     *  @return elevation in km.
     */
    protected double getVehicleElevation() {
		if (terrainElevation == null)
			terrainElevation = surfaceFeatures.getTerrainElevation();
        return terrainElevation.getMOLAElevation(vehicle.getCoordinates());
    }
    
    /**
     * Ends the task and performs any final actions.
     */
    public void endTask() {
    	if (vehicle != null) {
    		vehicle.setSpeed(0D);
//    		VehicleOperator vo = vehicle.getOperator();
    		// Need to set the vehicle operator to null before clearing the driving task 
        	if (vehicle != null)
        		vehicle.setOperator(null);
//        	if (vo != null)
//        		clearDrivingTask(vo);
    	}
    	
    	super.endTask();
    }
    
    /**
     * Gets the average operating speed of a vehicle for a given operator.
     * @param vehicle the vehicle.
     * @param operator the vehicle operator.
     * @return average operating speed (km/h)
     */
    public static double getAverageVehicleSpeed(Vehicle vehicle, VehicleOperator operator, Mission mission) {
    	if (vehicle != null) {
    		// Need to update this to reflect the particular operator's average speed operating the vehicle.
    		double baseSpeed = vehicle.getBaseSpeed();
    		double mod = 0;
    		Person p = null;
    		if (operator instanceof Person) {
    			p = (Person)operator;
    			if (p.getJobName().equalsIgnoreCase(Pilot.class.getSimpleName())) {
    				mod += baseSpeed * 0.25; 
    			}
    			
    			// Look up a person's prior pilot related training.
    			mod += baseSpeed * p.getPilotingMod();
    			
    			int skill = p.getSkillManager().getEffectiveSkillLevel(SkillType.PILOTING);
    			if (skill <= 5) {
    				mod += 0D - ((baseSpeed / 4D) * ((5D - skill) / 5D));
    	        }
    	        else {
    	            double tempSpeed = baseSpeed;
    	            for (int x=0; x < skill - 5; x++) {
    	                tempSpeed /= 2D;
    	                mod += tempSpeed;
    	            }
    	        }
    			
    			// TODO: Should account for a person's attributes
    			
    			// Check for any crew emergency
    			if (mission.hasEmergencyAllCrew())
    				mod += baseSpeed * 0.25;
    		}
    		
    		return baseSpeed + mod;
    	}
    	else
    		return 0;
    }
    
    @Override
    public void destroy() {
        super.destroy();
        
        vehicle = null;
        destination = null;
        startTripTime = null;
    }
}
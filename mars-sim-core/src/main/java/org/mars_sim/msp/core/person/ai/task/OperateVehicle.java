/**
 * Mars Simulation Project
 * OperateVehicle.java
 * @version 3.1.0 2017-09-13
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.Iterator;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Direction;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleOperator;

/**
 * The OperateVehicle class is an abstract task for operating a vehicle, 
 * driving/flying it to a destination.
 */
public abstract class OperateVehicle extends Task implements Serializable {
	
    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
    /** Task phases. */
    protected static final TaskPhase MOBILIZE = new TaskPhase(Msg.getString(
            "Task.phase.mobilize")); //$NON-NLS-1$
	
    // Distance buffer for arriving at destination (km).
    private final static double DESTINATION_BUFFER = .001D;
    
    // The base percentage chance of an accident while operating vehicle per millisol.
    public static final double BASE_ACCIDENT_CHANCE = .01D; 
	
	// Data members
	private double startTripDistance; // The distance (km) to the destination at the start of the trip.
	   
	private Vehicle vehicle; // The vehicle to operate.
	private Coordinates destination; // The location of the destination of the trip.
	private MarsClock startTripTime; // The time/date the trip is starting.
	
//	private static SurfaceFeatures surface;
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
        addExperience(time);
        
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
	 * Move the vehicle in its direction at its speed for the amount of time given.
	 * Stop if reached destination.
	 * @param time the amount of time (ms) to drive.
	 * @return the amount of time (ms) left over after driving (if any)
	 */
	protected double mobilizeVehicle(double time) {
		
		if (person != null) {
	        // Set person as the vehicle operator if he/she isn't already.
	        if (!person.equals(vehicle.getOperator())) {
        		// If attempting to switch the driver of this vehicle
	        	if (vehicle.getOperator() != null) {
		        	Person lastDriver = (Person) vehicle.getOperator();
		        	lastDriver.getMind().getTaskManager().clearTask();
	        	}
	            vehicle.setOperator(person);
	        }

		}
//		else if (robot != null) {
//			
//	        // Set robot as the vehicle operator if it isn't already.
//	        if (!robot.equals(vehicle.getOperator())) {
//	            vehicle.setOperator(robot);
//	        }
//
//		}		
		
        // Find starting distance to destination.
        double startingDistanceToDestination = getDistanceToDestination();

        // Determine distance traveled in time given.
        double secondsTime = MarsClock.convertMillisolsToSeconds(time);
        double distanceTraveled = secondsTime * ((vehicle.getSpeed() / 60D) / 60D);

        // Consume fuel for distance traveled.
        double fuelConsumed = distanceTraveled / vehicle.getIFuelConsumption();
        Inventory vInv = vehicle.getInventory();
        int fuelType = vehicle.getFuelType();
        double remainingFuel = vInv.getAmountResourceStored(fuelType, false);
        if (fuelConsumed > remainingFuel) {
        	fuelConsumed = remainingFuel;
        }
        try {
        	vInv.retrieveAmountResource(fuelType, fuelConsumed);
        }
        catch (Exception e) {}

        double result = 0;

        // If starting distance to destination is less than distance traveled, stop at destination.
        if (startingDistanceToDestination <= (distanceTraveled + DESTINATION_BUFFER)) {
            distanceTraveled = startingDistanceToDestination;
            vehicle.setCoordinates(destination);
            vehicle.setSpeed(0D);
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
            endTask();
            result = time - MarsClock.convertSecondsToMillisols(distanceTraveled / vehicle.getSpeed() * 60D * 60D);
        }
        else {
            // Determine new position.
            vehicle.setCoordinates(vehicle.getCoordinates().getNewLocation(vehicle.getDirection(), distanceTraveled));
        }
        
        // Add distance traveled to vehicle's odometer.
        vehicle.addTotalDistanceTraveled(distanceTraveled);
        vehicle.addDistanceLastMaintenance(distanceTraveled);

        return result;
	}
	
	/**
	 * Checks if the destination is at the location of a settlement.
	 * @return true if destination is at a settlement location.
	 */
	private boolean isSettlementDestination() {
	    
	    boolean result = false;
	    
	    Iterator<Settlement> i = unitManager.getSettlements().iterator();
	    while (i.hasNext()) {
	        Settlement settlement = i.next();
	        if (settlement.getCoordinates().equals(destination)) {
	            result = true;
	        }
	    }
	    
	    return result;
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
//        MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();

        // Determine time difference from start of trip in millisols.
        double millisolsDiff = MarsClock.getTimeDiff(marsClock, startTripTime);
        double hoursDiff = MarsClock.convertMillisolsToSeconds(millisolsDiff) / 60D / 60D;

        // Determine average speed so far in km/hr.
        double avgSpeed = (startTripDistance - getDistanceToDestination()) / hoursDiff;

        // Determine estimated speed in km/hr.
        double estimatorConstant = .5D;
        double estimatedSpeed = estimatorConstant * (vehicle.getBaseSpeed() + getSpeedSkillModifier());

        // Determine final estimated speed in km/hr.
        double tempAvgSpeed = avgSpeed * ((startTripDistance - getDistanceToDestination()) / startTripDistance);
        double tempEstimatedSpeed = estimatedSpeed * (getDistanceToDestination() / startTripDistance);
        double finalEstimatedSpeed = tempAvgSpeed + tempEstimatedSpeed;

        // Determine time to destination in millisols.
        double hoursToDestination = getDistanceToDestination() / finalEstimatedSpeed;
        double millisolsToDestination = MarsClock.convertSecondsToMillisols(hoursToDestination * 60D * 60D);

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
    	double result = 0D;
        double baseSpeed = vehicle.getBaseSpeed();
        if (getEffectiveSkillLevel() <= 5) {
            result = 0D - ((baseSpeed / 2D) * ((5D - getEffectiveSkillLevel()) / 5D));
        }
        else {
            double tempSpeed = baseSpeed;
            for (int x=0; x < getEffectiveSkillLevel() - 5; x++) {
                tempSpeed /= 2D;
                result += tempSpeed;
            }
        }
        return result;
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
//    	if (surface == null)
//    		surface = Simulation.instance().getMars().getSurfaceFeatures();
        return surface.getTerrainElevation().getElevation(vehicle.getCoordinates());
    }
    
    /**
     * Ends the task and performs any final actions.
     */
    public void endTask() {
    	// TODO Might need to change this for flying vehicles.
        vehicle.setSpeed(0D);
        vehicle.setOperator(null);
    	
    	super.endTask();
    }
    
    /**
     * Gets the average operating speed of a vehicle for a given operator.
     * @param vehicle the vehicle.
     * @param operator the vehicle operator.
     * @return average operating speed (km/h)
     */
    public static double getAverageVehicleSpeed(Vehicle vehicle, VehicleOperator operator) {
    	if (vehicle != null) {
    		// Need to update this to reflect the particular operator's average speed operating the vehicle.
    		double baseSpeed = vehicle.getBaseSpeed();
//    		System.out.println("OperateVehicle : base speed : " + baseSpeed);
    		double speed = 0;
    		double mod = 0;
    		Person p = null;
    		if (operator instanceof Person) {
    			p = (Person)operator;
    			if (p.getJobName().toLowerCase().contains("driver")) {
    				speed = baseSpeed * 1.1; 
    			}
    			
    			int skill = p.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.DRIVING);
    			if (skill <= 5) {
    				mod = 0D - ((baseSpeed / 2D) * ((5D - skill) / 5D));
    	        }
    	        else {
    	            double tempSpeed = baseSpeed;
    	            for (int x=0; x < skill - 5; x++) {
    	                tempSpeed /= 2D;
    	                mod += tempSpeed;
    	            }
    	        }
    		}
    		speed = baseSpeed + mod;
    		return speed;
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
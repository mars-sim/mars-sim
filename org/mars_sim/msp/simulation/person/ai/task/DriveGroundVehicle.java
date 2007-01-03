/**
 * Mars Simulation Project
 * DriveGroundVehicle.java
 * @version 2.79 2006-05-11
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.mars.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.Skill;
import org.mars_sim.msp.simulation.person.ai.SkillManager;
import org.mars_sim.msp.simulation.time.*;
import org.mars_sim.msp.simulation.vehicle.*;

/**
 *  The Drive Ground Vehicle class is a task for driving a ground vehicle to a destination.
 */
public class DriveGroundVehicle extends OperateVehicle implements Serializable {

    // Task phases
    public final static String AVOID_OBSTACLE = "Avoiding Obstacle";
    public final static String WINCH_VEHICLE = "Winching Stuck Vehicle";
    
	private static final double STRESS_MODIFIER = .1D; // The stress modified per millisol.

    // Data members
    private double closestDistance; // Closest distance to destination vehicle has been so far.
    private double obstacleDistance; // Distance travelled in obstacle avoidance path.
    private double obstacleTimeCount; // Amount of time driver has not been any closer to destination. (in millisols)

    /** 
     * Default Constructor
     * @param person the person to perform the task
     * @param vehicle the vehicle to be driven
     * @param destination location to be driven to
     * @param startTripTime the starting time of the trip
     * @param startTripDistance the starting distance to destination for the trip
     * @throws Exception if task cannot be constructed.
     */
    public DriveGroundVehicle(Person person, GroundVehicle vehicle,
            Coordinates destination, MarsClock startTripTime, double startTripDistance) throws Exception {
    	
    	// User OperateVehicle constructor
        super("Driving vehicle", person, vehicle, destination, startTripTime, 
        		startTripDistance, STRESS_MODIFIER, true, (300D + RandomUtil.getRandomDouble(100D)));

        // Set initial parameters
        setDescription("Driving " + vehicle.getName());
        closestDistance = Double.MAX_VALUE;
        obstacleTimeCount = 0D;
        addPhase(AVOID_OBSTACLE);
        addPhase(WINCH_VEHICLE);

        // System.out.println(person.getName() + " is driving " + vehicle.getName());
    }

    /**
     * Constructs with a given starting phase.
     * @param person the person to perform the task
     * @param vehicle the vehicle to be driven
     * @param destination location to be driven to
     * @param startTripTime the starting time of the trip
     * @param startTripDistance the starting distance to destination for the trip
     * @param startingPhase the starting phase for the task
     * @throws Exception if task cannot be constructed.
     */
    public DriveGroundVehicle(Person person, GroundVehicle vehicle, Coordinates destination, 
            MarsClock startTripTime, double startTripDistance, String startingPhase) throws Exception {
    	
        // Use OperateVehicle constuctor
    	super("Driving vehicle", person, vehicle, destination, startTripTime, 
        		startTripDistance, STRESS_MODIFIER, true, (100D + RandomUtil.getRandomDouble(100D)));
    	
        // Set initial parameters
        setDescription("Driving " + vehicle.getName());
        closestDistance = Double.MAX_VALUE;
        obstacleTimeCount = 0D;
        addPhase(AVOID_OBSTACLE);
        addPhase(WINCH_VEHICLE);
		if ((startingPhase != null) && !startingPhase.equals("")) setPhase(startingPhase);

        // System.out.println(person.getName() + " is driving " + vehicle.getName());
    }
    
    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time the phase is to be performed.
     * @return the remaining time after the phase has been performed.
     * @throws Exception if error in performing phase or if phase cannot be found.
     */
    protected double performMappedPhase(double time) throws Exception {
    	time = super.performMappedPhase(time);
    	if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
    	if (AVOID_OBSTACLE.equals(getPhase())) return obstaclePhase(time);
    	if (WINCH_VEHICLE.equals(getPhase())) return winchingPhase(time);
    	else return time;
    }
	
	/**
	 * Move the vehicle in its direction at its speed for the amount of time given.
	 * Stop if reached destination.
	 * @param time the amount of time (ms) to drive.
	 * @return the amount of time (ms) left over after driving (if any)
	 * @throws Exception of error mobilizing vehicle.
	 */
	protected double mobilizeVehicle(double time) throws Exception {
		
		// If vehicle is stuck, try winching.
		if (((GroundVehicle) getVehicle()).isStuck() && (!WINCH_VEHICLE.equals(getPhase()))) {
			setPhase(WINCH_VEHICLE);
			return(time);
		}
		
        // If speed is less the 1 kph, change to avoiding obstacle phase.
        if ((getVehicle().getSpeed() < 1D) && (!AVOID_OBSTACLE.equals(getPhase())) && 
        		(!WINCH_VEHICLE.equals(getPhase()))) {
            setPhase(AVOID_OBSTACLE);
            return(time);
        }
        else return super.mobilizeVehicle(time);
	}

    /** 
     * Perform task in obstace phase.
     * @param time the amount of time to perform the task (in millisols)
     * @return time remaining after performing phase (in millisols)
     * @throws Exception if error performing phase.
     */
    private double obstaclePhase(double time) throws Exception {

        double timeUsed = 0D;
        GroundVehicle vehicle = (GroundVehicle) getVehicle();

        // If driver has failed to get around an obstacle after 100 millisols,
        // vehicle should be considered stuck and needs to be winched free.
        if ((obstacleTimeCount >= 100D) && !vehicle.isStuck()) {
            vehicle.setStuck(true);
            setPhase(WINCH_VEHICLE);
            obstacleTimeCount = 0D;
            obstacleDistance = 0D;
            isBackingUp = false;
            backingUpDistance = 0D;
            return time;
        }

        // If having backup 10 km or more, revert to normal obstacle avoidance.
        if (isBackingUp && (backingUpDistance >= 10D)) {
            backingUpDistance = 0D;
            isBackingUp = false;
        }
	
        // Update vehicle elevation.
        updateVehicleElevationAltitude();
        
        // Update vehicle direction.
        if (isBackingUp) {
        	double backupTweakDirection = Math.PI / 10D;
            double reverseDirection = vehicle.getCoordinates().getDirectionToPoint(getDestination()).getDirection() + 
            		Math.PI + backupTweakDirection;
            vehicle.setDirection(new Direction(reverseDirection));
        }
        else {
        	if (obstacleDistance == 0D) vehicle.setDirection(getObstacleAvoidanceDirection());
        	else if (obstacleDistance > 10D) {
        		obstacleDistance = 0D;
        		setPhase(OperateVehicle.MOBILIZE);
        	}
        }

        // Update vehicle speed.
        if (isBackingUp) vehicle.setSpeed(getSpeed(vehicle.getDirection()) / 2D);
        else vehicle.setSpeed(getSpeed(vehicle.getDirection()));

        // Drive in the direction
        timeUsed = time - mobilizeVehicle(time);
        
        // Update obstacle distance.
        if (!isBackingUp) obstacleDistance += vehicle.getSpeed() * timeUsed;

        // Update closest distance to destination.
        if (getDistanceToDestination() < closestDistance) {
            closestDistance = getDistanceToDestination();
            obstacleTimeCount = 0;
        }
        else obstacleTimeCount += timeUsed;

        // Add experience points
        addExperience(time);

        // Check for accident.
        if (!isDone()) checkForAccident(timeUsed);
        
        // If vehicle has malfunction, end task.
        if (getVehicle().getMalfunctionManager().hasMalfunction()) endTask();

        return time - timeUsed;
    }

    /** 
     * Perform task in winching phase.
     * @param time the amount of time to perform the phase.
     * @return time remaining after performing the phase.
     * @throws Exception if error while performing phase.
     */
    private double winchingPhase(double time) throws Exception {

        double timeUsed = 0D;
        GroundVehicle vehicle = (GroundVehicle) getVehicle();

        // Find current direction and update vehicle.
        vehicle.setDirection(vehicle.getCoordinates().getDirectionToPoint(getDestination()));

        // Update vehicle elevation.
        updateVehicleElevationAltitude();

        // If speed given the terrain would be better than 1kph, return to normal driving.
        // Otherwise, set speed to .2kph for winching speed.
        if (getSpeed(vehicle.getDirection()) > 1D) {
            setPhase(OperateVehicle.MOBILIZE);
            vehicle.setStuck(false);
            return(time);
        }
        else vehicle.setSpeed(.2D);

        // Drive in the direction
        timeUsed = time - mobilizeVehicle(time);

        // Add experience points
        addExperience(time);

        // Check for accident.
        if (!isDone()) checkForAccident(timeUsed);
        
        // If vehicle has malfunction, end task.
        if (getVehicle().getMalfunctionManager().hasMalfunction()) endTask();

        return time - timeUsed;
    }

    /** 
     * Gets the direction for obstacle avoidance.
     * @return direction for obstacle avoidance in radians
     * @throws exception if error in getting direction.
     */
    private Direction getObstacleAvoidanceDirection() throws Exception {
    	GroundVehicle vehicle = (GroundVehicle) getVehicle();
        boolean foundGoodPath = false;

        String sideCheck = "left";
        if (RandomUtil.lessThanRandPercent(50)) sideCheck = "right";

        Direction resultDirection = vehicle.getDirection();

        for (int x=0; (x < 5) && !foundGoodPath; x++) {
            // double modAngle = (double) x * (Math.PI / 6D);
        	double modAngle = Math.PI / 6D;

            if (sideCheck.equals("left"))
                resultDirection.setDirection(resultDirection.getDirection() - modAngle);
            else
                resultDirection.setDirection(resultDirection.getDirection() + modAngle);

            if (getSpeed(resultDirection) > 1D) foundGoodPath = true;
        }

        // if (foundGoodPath) setPhase(OperateVehicle.MOBILIZE);
        // else isBackingUp = true;
        if (!foundGoodPath) isBackingUp = true;

        return resultDirection;
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
     * @param direction the direction of travel
     * @return speed in km/hr
     */
    protected double getSpeed(Direction direction) {
    	double result = super.getSpeed(direction);
    	result *= getSpeedLightConditionModifier();
    	result *= getTerrainModifier();
    	return result;
    }
    
    /**
     * Gets the lighting condition speed modifier.  
     * @return speed modifier (0D - 1D)
     */
    protected double getSpeedLightConditionModifier() {
    	// Ground vehicles travel at 30% speed at night.
    	SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
    	double result = surface.getSurfaceSunlight(getVehicle().getCoordinates());
        result = (result * .7D) + .3D;
        return result;
    }
    
    /**
     * Gets the terrain speed modifier.
     * @return speed modifier (0D - 1D)
     */
    protected double getTerrainModifier() {
    	GroundVehicle vehicle = (GroundVehicle) getVehicle();
        
        // Get vehicle's terrain handling capability.
        double handling = vehicle.getTerrainHandlingCapability();
        
        // Determine modifier.
        double angleModifier = handling + getEffectiveSkillLevel() - 10D;
        if (angleModifier < 0D) angleModifier = Math.abs(1D / angleModifier);
        double tempAngle = Math.abs(vehicle.getTerrainGrade() / angleModifier);
        if (tempAngle > (Math.PI / 2D)) tempAngle = Math.PI / 2D;
        return Math.cos(tempAngle);
    }

    /**
     * Check if vehicle has had an accident.
     * @param time the amount of time vehicle is driven (millisols)
     */
    protected void checkForAccident(double time) {

    	GroundVehicle vehicle = (GroundVehicle) getVehicle();
    	
        double chance = .001D;

        // Driver skill modification.
        int skill = getEffectiveSkillLevel();
        if (skill <= 3) chance *= (4 - skill);
        else chance /= (skill - 2);
	
        // Get task phase modification.
        if (AVOID_OBSTACLE.equals(getPhase())) chance *= 1.2D;
        if (WINCH_VEHICLE.equals(getPhase())) chance *= 1.3D;

        // Terrain modification.
        chance *= (1D + Math.sin(vehicle.getTerrainGrade()));

        // Vehicle handling modification.
        chance /= (1D + vehicle.getTerrainHandlingCapability());

        // Light condition modification.
		SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
        double lightConditions = surface.getSurfaceSunlight(vehicle.getCoordinates());
        chance *= (5D * (1D - lightConditions)) + 1D;

        if (RandomUtil.lessThanRandPercent(chance * time)) {
            // System.out.println(person.getName() + " has accident driving " + vehicle.getName());
	    	vehicle.getMalfunctionManager().accident();
		}
    }
    
	/**
	 * Gets the effective skill level a person has at this task.
	 * @return effective skill level
	 */
	public int getEffectiveSkillLevel() {
		SkillManager manager = person.getMind().getSkillManager();
		return manager.getEffectiveSkillLevel(Skill.DRIVING);
	}
	
	/**
	 * Gets a list of the skills associated with this task.
	 * May be empty list if no associated skills.
	 * @return list of skills as strings
	 */
	public List getAssociatedSkills() {
		List results = new ArrayList();
		results.add(Skill.DRIVING);
		return results;
	}
	
	/**
	 * Adds experience to the person's skills used in this task.
	 * @param time the amount of time (ms) the person performed this task.
	 */
	protected void addExperience(double time) {
        // Add experience points for driver's 'Driving' skill.
        // Add one point for every 100 millisols.
        double newPoints = time / 100D;
        int experienceAptitude = person.getNaturalAttributeManager().getAttribute(
        	NaturalAttributeManager.EXPERIENCE_APTITUDE);
        newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
		newPoints *= getTeachingExperienceModifier();
		double phaseModifier = 1D;
		if (AVOID_OBSTACLE.equals(getPhase())) phaseModifier = 4D;
		newPoints *= phaseModifier;
        person.getMind().getSkillManager().addExperience(Skill.DRIVING, newPoints);
	}
	
    /**
     * Ends the task and performs any final actions.
     */
    public void endTask() {
    	
        // System.out.println(person.getName() + " finished driving " + getVehicle().getName());
        // ((GroundVehicle) getVehicle()).setStuck(false);
    	
    	super.endTask();
    }
}
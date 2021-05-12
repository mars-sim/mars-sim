/**
 * Mars Simulation Project
 * WalkRoverInterior.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.logging.Level;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A subtask for walking between two interior locations in a rover.
 */
public class WalkRoverInterior
extends Task
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(WalkRoverInterior.class.getName());
	
	/** Task phases. */
    private static final TaskPhase WALKING = new TaskPhase(Msg.getString(
            "Task.phase.walking")); //$NON-NLS-1$

	// Static members
	/** km per hour. */
	private static final double WALKING_SPEED = Walk.PERSON_WALKING_SPEED;
	private static final double STRESS_MODIFIER = -.1D;
	private static final double VERY_SMALL_DISTANCE = .00001D;

	// Data members
	private Rover rover;
	private double destXLoc;
	private double destYLoc;

	/*
	 * Constructor 1.
	 */
    public WalkRoverInterior(Person person, Rover rover, double destinationXLocation,
            double destinationYLocation) {
        super("Walking inside a rover", person, false, false, STRESS_MODIFIER, null, 100D);

        // Check that the person is currently inside a rover.
        if (!person.isInVehicle()) {
        	logger.severe(person, "Is supposed to be inside rover "
           			+ rover.getName() + "."); 
    	}
        
//        else if (person.isOutside()) {
//        	LogConsolidated.log(Level.SEVERE, 5000, sourceName, 
//        			person + " is outside but is calling WalkRoverInterior task and NOT in rover " 
//        			+ rover.getName() + "."); 
//            //throw new IllegalStateException(
//            //        "WalkRoverInterior task started when " + person + " is not in a rover.");
//        }

//        else if (person.isInVehicle()) {
//        	LogConsolidated.log(Level.SEVERE, 5000, sourceName, 
//        		"WalkRoverInterior task started when " + person + " is not in rover " 
//        			+ rover.getName() + ".");
//        }
   
        // Initialize data members.
        this.rover = rover;
        this.destXLoc = destinationXLocation;
        this.destYLoc = destinationYLocation;

        // Initialize task phase.
        addPhase(WALKING);
        setPhase(WALKING);

        //logger.finer(person.getName() + " starting to walk to new location in " + rover.getName() +
        //        " to (" + destinationXLocation + ", " + destinationYLocation + ")");
    }

	/*
	 * Constructor 2.
	 */
    public WalkRoverInterior(Robot robot, Rover rover, double destinationXLocation,
            double destinationYLocation) {
        super("Walking Rover Interior", robot, false, false, STRESS_MODIFIER, null, 100D);

        // Check that the robot is currently inside a rover.
//        LocationSituation location = robot.getLocationSituation();
//        if (location != LocationSituation.IN_VEHICLE) {
////            throw new IllegalStateException(
//            	LogConsolidated.log(Level.SEVERE, 5000, sourceName, 
//                    robot + " is not in a vheicle but doing WalkRoverInterior task in rover "
//                    		+ rover.getName() + "."); 
//        }

        if (!robot.isInVehicle()) {
        	logger.severe(robot, "Is supposed to be inside rover "
           			+ rover.getName() + "."); 
    	}
        
        // Initialize data members.
        this.rover = rover;
        this.destXLoc = destinationXLocation;
        this.destYLoc = destinationYLocation;

        // Initialize task phase.
        addPhase(WALKING);
        setPhase(WALKING);

        //logger.finer(robot.getName() + " starting to walk to new location in " + rover.getName() +
        //        " to (" + destinationXLocation + ", " + destinationYLocation + ")");
    }
    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        if (WALKING.equals(getPhase())) {
            return walkingPhase(time);
        }
        else {
            return time;
        }
    }

    /**
     * Performs the walking phase of the task.
     * @param time the amount of time (millisol) to perform the walking phase.
     * @return the amount of time (millisol) left after performing the walking phase.
     */
    double walkingPhase(double time) {

        // Determine walking distance.
        double timeHours = MarsClock.HOURS_PER_MILLISOL * time;
		person.caculateWalkSpeedMod();
		double mod = person.getWalkSpeedMod();
		double speed = WALKING_SPEED  * mod;
		double distanceKm = speed * timeHours;
        double distanceMeters = distanceKm * 1000D;
        double remainingWalkingDistance = Point2D.Double.distance(worker.getXLocation(),
                    worker.getYLocation(), destXLoc, destYLoc);

        double timeLeft = 0D;
        if (remainingWalkingDistance > VERY_SMALL_DISTANCE) {

            // Determine time left after walking.
            if (distanceMeters >= remainingWalkingDistance) {
                distanceMeters = remainingWalkingDistance;
            	timeLeft = time - MarsClock.convertSecondsToMillisols(distanceMeters / 1000D / speed * 60D * 60D);
            }

            if (distanceMeters < remainingWalkingDistance) {
                // Determine direction to destination.
                double direction = determineDirection(destXLoc, destYLoc);
                // Determine person's new location at distance and direction.
                walkInDirection(direction, distanceMeters);
            }
            else {
                // Set person's location at destination.
                worker.setXLocation(destXLoc);
                worker.setYLocation(destYLoc);
        		logger.log(worker, Level.FINER, 5000, "Walked to new location ("
        				+ destXLoc + ", " + destYLoc + ") in " + rover.getName() + ".");
                endTask();
            }
        }
        else {

            timeLeft = time;

            // Set person's location at destination.
            worker.setXLocation(destXLoc);
            worker.setYLocation(destYLoc);
    		logger.log(worker, Level.FINER, 5000, "Walked to new location ("
    				+ destXLoc + ", " + destYLoc + ") in " + rover.getName() + ".");

            endTask();
        }

        return timeLeft;
    }

    /**
     * Determine the direction of travel to a location.
     * @param destinationXLocation the destination X location.
     * @param destinationYLocation the destination Y location.
     * @return direction (radians).
     */
    double determineDirection(double destinationXLocation, double destinationYLocation) {
    	double result = Math.atan2(worker.getXLocation() - destinationXLocation,
    	                destinationYLocation - worker.getYLocation());

        while (result > (Math.PI * 2D)) {
            result -= (Math.PI * 2D);
        }

        while (result < 0D) {
            result += (Math.PI * 2D);
        }

        return result;
    }

    /**
     * Walk in a given direction for a given distance.
     * @param direction the direction (radians) of travel.
     * @param distance the distance (meters) to travel.
     */
    void walkInDirection(double direction, double distance) {
    	double newXLoc = 0 ;
    	double newYLoc = 0;

		newXLoc = (-1D * Math.sin(direction) * distance) + worker.getXLocation();
		newYLoc = (Math.cos(direction) * distance) + worker.getYLocation();
        worker.setXLocation(newXLoc);
        worker.setYLocation(newYLoc);
    }

	/**
	 * Does a change of Phase for this Task generate an entry in the Task Schedule 
	 * @return false
	 */
	@Override
	protected boolean canRecord() {
		return false;
	}
	
    @Override
    public void destroy() {
        super.destroy();

        rover = null;
    }
}

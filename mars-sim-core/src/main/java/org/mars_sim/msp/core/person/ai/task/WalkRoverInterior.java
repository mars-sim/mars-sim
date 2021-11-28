/**
 * Mars Simulation Project
 * WalkRoverInterior.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.logging.Level;

import org.mars_sim.msp.core.LocalPosition;
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
	private static final double STRESS_MODIFIER = -.1D;
	private static final double VERY_SMALL_DISTANCE = .00001D;

	// Data members
	private Rover rover;
	private LocalPosition destLoc;
	/*
	 * Constructor 1.
	 */
    public WalkRoverInterior(Person person, Rover rover, LocalPosition destLoc) {
        super("Walking inside a rover", person, false, false, STRESS_MODIFIER, null, 100D);

        // Check that the person is currently inside a rover.
        if (!person.isInVehicle()) {
        	logger.severe(person, "Not inside rover "
           			+ rover.getName() + "."); 
    	}
        
   
        // Initialize data members.
        this.rover = rover;
        this.destLoc = destLoc;

        // Initialize task phase.
        addPhase(WALKING);
        setPhase(WALKING);

        //logger.finer(person.getName() + " starting to walk to new location in " + rover.getName() +
        //        " to (" + destinationXLocation + ", " + destinationYLocation + ")");
    }

	/*
	 * Constructor 2.
	 */
    public WalkRoverInterior(Robot robot, Rover rover, LocalPosition destLoc) {
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
        this.destLoc = destLoc;

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
		double speed = person.calculateWalkSpeed();
		double distanceKm = speed * timeHours;
        double distanceMeters = distanceKm * 1000D;
        LocalPosition currentPosition = worker.getPosition(); 
        double remainingWalkingDistance = currentPosition.getDistanceTo(destLoc);

        double timeLeft = 0D;
        if (remainingWalkingDistance > VERY_SMALL_DISTANCE) {

            // Determine time left after walking.
            if (distanceMeters >= remainingWalkingDistance) {
                distanceMeters = remainingWalkingDistance;
            	timeLeft = time - MarsClock.convertSecondsToMillisols(distanceMeters / 1000D / speed * 60D * 60D);
            }

            if (distanceMeters < remainingWalkingDistance) {
                // Determine direction to destination.
                double direction = currentPosition.getDirectionTo(destLoc);
                // Determine person's new location at distance and direction.
                worker.setPosition(currentPosition.getPosition(distanceMeters, direction));
            }
            else {
                // Set person's location at destination.
                worker.setPosition(destLoc);
        		logger.log(worker, Level.FINER, 5000, "Walked to new location ("
        				+ destLoc + ") in " + rover.getName() + ".");
                endTask();
            }
        }
        else {

            timeLeft = time;

            // Set person's location at destination.
            worker.setPosition(destLoc);
    		logger.log(worker, Level.FINER, 5000, "Walked to new location ("
    				+ destLoc + ") in " + rover.getName() + ".");

            endTask();
        }

        return timeLeft;
    }

	/**
	 * Does a change of Phase for this Task generate an entry in the Task Schedule 
	 * @return false
	 */
	@Override
	protected boolean canRecord() {
		return false;
	}
}

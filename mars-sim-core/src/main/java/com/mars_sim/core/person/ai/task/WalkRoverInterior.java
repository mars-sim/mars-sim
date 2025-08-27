/*
 * Mars Simulation Project
 * WalkRoverInterior.java
 * @date 2023-09-06
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task;

import java.util.logging.Level;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Rover;

/**
 * A subtask for walking between two interior locations in a rover.
 */
public class WalkRoverInterior extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(WalkRoverInterior.class.getName());
	
	/** Simple Task name */
	public static final String SIMPLE_NAME = WalkRoverInterior.class.getSimpleName();
	
	/** Task name */
	public static final String NAME = Msg.getString("Task.description.walkRoverInterior"); //$NON-NLS-1$
	
	/** Task phases. */
    private static final TaskPhase WALKING = new TaskPhase(Msg.getString(
            "Task.phase.walking")); //$NON-NLS-1$

	// Static members
	/** km per hour. */
	private static final double MIN_PULSE_TIME = Walk.MIN_PULSE_TIME;
	private static final double STRESS_MODIFIER = -.1D;
	private static final double VERY_SMALL_DISTANCE = .00001D;
	/** The minimum pulse time for completing a task phase in this class.  */
	private static double minPulseTime = 0; //Math.min(standardPulseTime, MIN_PULSE_TIME);

	// Data members
	private Rover rover;
	private LocalPosition destLoc;
	
	/**
	 * Constructor 1.
	 */
    public WalkRoverInterior(Person person, Rover rover, LocalPosition destLoc) {
        super(NAME, person, false, false, STRESS_MODIFIER, null, 100D);

        // Check that the person is currently inside a rover.
        if (!person.isInVehicle()) {
        	logger.severe(person, "Not inside rover "
           			+ rover.getName() + "."); 
    	}
        
   
        // Initialize data members.
        this.rover = rover;
        this.destLoc = destLoc;

        // Initialize task phase.
        setPhase(WALKING);
    }

	/**
	 * Constructor 2.
	 */
    public WalkRoverInterior(Robot robot, Rover rover, LocalPosition destLoc) {
        super(NAME, robot, false, false, STRESS_MODIFIER, null, 100D);

        // Check that the robot is currently inside a rover.
        if (!robot.isInVehicle()) {
        	logger.severe(robot, "Is supposed to be inside rover "
           			+ rover.getName() + "."); 
    	}
        
        // Initialize data members.
        this.rover = rover;
        this.destLoc = destLoc;

        // Initialize task phase.
        setPhase(WALKING);
    }
    
    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
			logger.severe(worker, "Task phase is null.");
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
     * 
     * @param time the amount of time (millisol) to perform the walking phase.
     * @return the amount of time (millisol) left after performing the walking phase.
     */
    double walkingPhase(double time) {
		double remainingTime = time - minPulseTime;
        double timeHours = MarsTime.HOURS_PER_MILLISOL * remainingTime;
		double speedKPH = 0;

		if (person != null) {
			speedKPH = Walk.PERSON_WALKING_SPEED;// * person.getWalkSpeedMod();

		}
		else {
			speedKPH =  Walk.ROBOT_WALKING_SPEED;// * robot.getWalkSpeedMod();
		}
		
        LocalPosition currentPosition = worker.getPosition(); 
        double remainingWalkingDistance = currentPosition.getDistanceTo(destLoc);
   
		// Determine walking distance.
		double coveredKm = speedKPH * timeHours;
		double coveredMeters = coveredKm * 1_000;
		
        if (remainingWalkingDistance > VERY_SMALL_DISTANCE) {

            // Determine time left after walking.
            if (coveredMeters >= remainingWalkingDistance) {
            	coveredMeters = remainingWalkingDistance;
 
    			if (speedKPH > 0)
    				remainingTime = remainingTime - MarsTime.convertSecondsToMillisols(coveredMeters / speedKPH * 3.6);
    			if (remainingTime < 0)
    				remainingTime = 0;
            }
            

            if (coveredMeters < remainingWalkingDistance) {
                // Determine direction to destination.
                double direction = currentPosition.getDirectionTo(destLoc);
                // Determine person's new location at distance and direction.
                worker.setPosition(currentPosition.getPosition(coveredMeters, direction));
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
            // Set person's location at destination.
            worker.setPosition(destLoc);
    		logger.log(worker, Level.FINER, 5000, "Walked to new location ("
    				+ destLoc + ") in " + rover.getName() + ".");

            endTask();
        }

        // Warning: see GitHub issue #1039 for details on return a 
        // non-zero value from this method
        
//      return remainingTime;
        return 0;
    }

	/**
	 * Does a change of Phase for this Task generate an entry in the Task Schedule ?
	 * 
	 * @return false
	 */
	@Override
	protected boolean canRecord() {
		return false;
	}
}

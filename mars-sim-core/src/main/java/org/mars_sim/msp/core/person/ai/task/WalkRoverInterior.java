/**
 * Mars Simulation Project
 * WalkRoverInterior.java
 * @version 3.1.0 2017-02-20
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
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
	private static Logger logger = Logger.getLogger(WalkRoverInterior.class.getName());
	private static String loggerName = logger.getName();
	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());
	
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
        super("Walking inside a rover", person, false, false, STRESS_MODIFIER, false, 0D);

        // Check that the person is currently inside a rover.
        if (!person.isInVehicle()) {
        	LogConsolidated.log(Level.SEVERE, 5000, sourceName, 
        			person + " is supposed to be inside rover "
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
        super("Walking Rover Interior", robot, false, false, STRESS_MODIFIER, false, 0D);

        // Check that the robot is currently inside a rover.
//        LocationSituation location = robot.getLocationSituation();
//        if (location != LocationSituation.IN_VEHICLE) {
////            throw new IllegalStateException(
//            	LogConsolidated.log(Level.SEVERE, 5000, sourceName, 
//                    robot + " is not in a vheicle but doing WalkRoverInterior task in rover "
//                    		+ rover.getName() + "."); 
//        }

        if (!robot.isInVehicle()) {
        	LogConsolidated.log(Level.SEVERE, 5000, sourceName, 
        			robot + " is supposed to be inside rover "
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
//		System.out.println("mod : " + mod);
        double distanceKm = WALKING_SPEED * timeHours * mod;
        double distanceMeters = distanceKm * 1000D;
        double remainingWalkingDistance = 0;
        if (person != null) {
            remainingWalkingDistance = Point2D.Double.distance(person.getXLocation(),
                    person.getYLocation(), destXLoc, destYLoc);
        }
        else if (robot != null) {
            remainingWalkingDistance = Point2D.Double.distance(robot.getXLocation(),
                    robot.getYLocation(), destXLoc, destYLoc);
        }

        double timeLeft = 0D;
        if (remainingWalkingDistance > VERY_SMALL_DISTANCE) {

            // Determine time left after walking.
            if (distanceMeters > remainingWalkingDistance) {
                double overDistance = distanceMeters - remainingWalkingDistance;
                timeLeft = MarsClock.convertSecondsToMillisols(overDistance / 1000D / WALKING_SPEED * 60D * 60D);
                distanceMeters = remainingWalkingDistance;
            }

            if (distanceMeters < remainingWalkingDistance) {
                // Determine direction to destination.
                double direction = determineDirection(destXLoc, destYLoc);
                // Determine person's new location at distance and direction.
                walkInDirection(direction, distanceMeters);
            }
            else {
            	if (person != null) {
                    // Set person's location at destination.
                    person.setXLocation(destXLoc);
                    person.setYLocation(destYLoc);
//                    logger.finer(person.getName() + " walked to new location in " + rover.getName());
        			LogConsolidated.log(Level.FINER, 5000, sourceName,
        					"[" + person.getLocationTag().getLocale() + "] "
              						+ person + " was in " + person.getLocationTag().getImmediateLocation()
        					+ " and walked to new location in " + rover.getName() + ".", null);
            	}
            	else if (robot != null) {
                    // Set robot's location at destination.
                    robot.setXLocation(destXLoc);
                    robot.setYLocation(destYLoc);
//                    logger.finer(robot.getName() + " walked to new location in " + rover.getName());
        			LogConsolidated.log(Level.FINER, 5000, sourceName,
        					"[" + robot.getLocationTag().getLocale() + "] "
              						+ robot + " was in " + robot.getLocationTag().getImmediateLocation()
        					+ " and walked to new location in " + rover.getName() + ".", null);
            	}

                endTask();
            }
        }
        else {

            timeLeft = time;

            if (person != null) {
                // Set person's location at destination.
                person.setXLocation(destXLoc);
                person.setYLocation(destYLoc);
//                logger.finer(person.getName() + " walked to new location in " + rover.getName());
    			LogConsolidated.log(Level.FINER, 5000, sourceName,
    					"[" + person.getLocationTag().getLocale() + "] "
          						+ person + " was in " + person.getLocationTag().getImmediateLocation()
    					+ " and walked to new location in " + rover.getName() + ".", null);
            }
            else if (robot != null) {
                // Set robot's location at destination.
                robot.setXLocation(destXLoc);
                robot.setYLocation(destYLoc);
//              logger.finer(robot.getName() + " walked to new location in " + rover.getName());
    			LogConsolidated.log(Level.FINER, 5000, sourceName,
    					"[" + robot.getLocationTag().getLocale() + "] "
          						+ robot + " was in " + robot.getLocationTag().getImmediateLocation()
    					+ " and walked to new location in " + rover.getName() + ".", null);
            }

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
    	double result = 0;

    	if (person != null) {
    	      result = Math.atan2(person.getXLocation() - destinationXLocation,
    	                destinationYLocation - person.getYLocation());
    	}
    	else if (robot != null) {
    	      result = Math.atan2(robot.getXLocation() - destinationXLocation,
    	                destinationYLocation - robot.getYLocation());
    	}


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

    	if (person != null) {
    	       newXLoc = (-1D * Math.sin(direction) * distance) + person.getXLocation();
    	       newYLoc = (Math.cos(direction) * distance) + person.getYLocation();
    	        person.setXLocation(newXLoc);
    	        person.setYLocation(newYLoc);
    	}
    	else if (robot != null) {
    		newXLoc = (-1D * Math.sin(direction) * distance) + robot.getXLocation();
    		newYLoc = (Math.cos(direction) * distance) + robot.getYLocation();
	        robot.setXLocation(newXLoc);
	        robot.setYLocation(newYLoc);
    	}

    }

    @Override
    public int getEffectiveSkillLevel() {
        return 0;
    }

    @Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<SkillType>(0);
        return results;
    }

    @Override
    protected void addExperience(double time) {
        // This task adds no experience.
    }

    @Override
    public void destroy() {
        super.destroy();

        rover = null;
    }
}
/**
 * Mars Simulation Project
 * WalkRoverInterior.java
 * @version 3.06 2014-05-09
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
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

	// Task phase
	private static final String WALKING = "Walking";

	// Static members
	/** km per hour. */
	private static final double WALKING_SPEED = 5D;
	private static final double STRESS_MODIFIER = -.1D;
	private static final double VERY_SMALL_DISTANCE = .00001D;

	// Data members
	private Rover rover;
	private double destXLoc;
	private double destYLoc;

	/** constructor. */
    public WalkRoverInterior(Person person, Rover rover, double destinationXLocation, 
            double destinationYLocation) {
        super("Walking Rover Interior", person, false, false, STRESS_MODIFIER, false, 0D);
        
        // Check that the person is currently inside a rover.
        LocationSituation location = person.getLocationSituation();
        if (location != LocationSituation.IN_VEHICLE) {
            throw new IllegalStateException(
                    "WalkRoverInterior task started when person is not in a rover.");
        }
        
        // Initialize data members.
        this.rover = rover;
        this.destXLoc = destinationXLocation;
        this.destYLoc = destinationYLocation;
        
        // Initialize task phase.
        addPhase(WALKING);
        setPhase(WALKING);
        
        logger.finer(person.getName() + " starting to walk to new location in " + rover.getName() + 
                " to (" + destinationXLocation + ", " + destinationYLocation + ")");
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
        double timeHours = MarsClock.convertMillisolsToSeconds(time) / 60D / 60D;
        double distanceKm = WALKING_SPEED * timeHours;
        double distanceMeters = distanceKm * 1000D;
        double remainingWalkingDistance = Point2D.Double.distance(person.getXLocation(), 
                person.getYLocation(), destXLoc, destYLoc);

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

                // Set person's location at destination.
                person.setXLocation(destXLoc);
                person.setYLocation(destYLoc);
                
                logger.finer(person.getName() + " walked to new location in " + rover.getName());

                endTask();
            }
        }
        else {

            timeLeft = time;
            
            // Set person's location at destination.
            person.setXLocation(destXLoc);
            person.setYLocation(destYLoc);

            logger.finer(person.getName() + " walked to new location in " + rover.getName());

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
        
        double result = Math.atan2(person.getXLocation() - destinationXLocation, 
                destinationYLocation - person.getYLocation());
        
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
        
        double newXLoc = (-1D * Math.sin(direction) * distance) + person.getXLocation();
        double newYLoc = (Math.cos(direction) * distance) + person.getYLocation();
        
        person.setXLocation(newXLoc);
        person.setYLocation(newYLoc);
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
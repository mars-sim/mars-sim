/**
 * Mars Simulation Project
 * WalkRoverInterior.java
 * @version 3.06 2014-02-10
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A subtask for walking between two interior locations in a rover.
 */
public class WalkRoverInterior extends Task implements Serializable {

    private static Logger logger = Logger.getLogger(WalkRoverInterior.class.getName());
    
    // Task phase
    private static final String WALKING = "Walking";
    
    // Static members
    private static final double WALKING_SPEED = 5D; // km / hr.
    
    // Data members
    private Rover rover;
    private double destXLoc;
    private double destYLoc;
    
    public WalkRoverInterior(Person person, Rover rover, double destinationXLocation, 
            double destinationYLocation) {
        super("Walking Settlement Interior", person, false, false, 0D, false, 0D);
        
        // Check that the person is currently inside a rover.
        String location = person.getLocationSituation();
        if (!location.equals(Person.INVEHICLE)) {
            throw new IllegalStateException(
                    "WalkRoverInterior task started when person is not in a rover.");
        }
        
        // Initialize data members.
        this.rover = rover;
        this.destXLoc = destinationXLocation;
        this.destYLoc = destinationYLocation;
        
        // Check that destination location is within rover.
        if (!LocalAreaUtil.checkLocationWithinLocalBoundedObject(destXLoc, destYLoc, rover)) {
            throw new IllegalStateException(
                    "Given destination walking location not within rover.");
        }
        
        // Initialize task phase.
        addPhase(WALKING);
        setPhase(WALKING);
    }
    
    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
        if (WALKING.equals(getPhase())) return walkingPhase(time);
        else return time;
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
        
        // Determine time left after walking.
        double timeLeft = 0D;
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
            
            logger.fine(person.getName() + " walked to new location in " + rover.getName());
            
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
    public List<String> getAssociatedSkills() {
        List<String> results = new ArrayList<String>(0);
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
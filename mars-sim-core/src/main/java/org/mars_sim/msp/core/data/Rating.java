/*
 * Mars Simulation Project
 * Rating.java
 * @date 2023-09-10
 * @author Barry Evans
 */
package org.mars_sim.msp.core.data;

/**
 * This represents a Rating of an entity in terms of a score. The implementing class will 
 * have move details of how this Rating could be used to influence the simulation.
 * @see RatingLog#logSelectedRating(String, String, Rating, java.util.List)
 */
public interface Rating {
    /**
     * Name of this Rating
     * @return
     */
    String getName();

    /**
     * What was the score associated to this Rating?
     * @return
     */
    RatingScore getScore();
}

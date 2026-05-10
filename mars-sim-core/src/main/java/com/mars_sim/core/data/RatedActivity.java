/*
 * Mars Simulation Project
 * RatedActivity.java
 * @date 2023-09-10
 * @author Barry Evans
 */
package com.mars_sim.core.data;

/**
 * This represents a Rating of an activity in terms of a score. The implementing class will 
 * have more details of how this rating could be used to influence the simulation.
 * @see RatingLog#logSelectedRating(String, String, RatedActivity, java.util.List)
 */
public interface RatedActivity {
	
    /**
     * Returns the name of this Rating.
     * 
     * @return
     */
    String getName();

    /**
     * Returns the score associated with this Rating.
     * 
     * @return
     */
    Rating getScore();
}

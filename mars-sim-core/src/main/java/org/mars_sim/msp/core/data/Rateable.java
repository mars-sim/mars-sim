/*
 * Mars Simulation Project
 * Rateable.java
 * @date 2023-08-21
 * @author Barry Evans
 */
package org.mars_sim.msp.core.data;

/**
 * Represents an object that can be scored by a Rating.
 * @see RatingLog#logSelectedRating(String, String, Rateable, java.util.Map)
 */
public interface Rateable {
    public String getName();
}

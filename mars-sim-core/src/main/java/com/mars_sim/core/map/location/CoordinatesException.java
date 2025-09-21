/*
 * Mars Simulation Project
 * CoordinatesException.java
 * @date 2025-09-16
 * @author Barry Evans
 */
package com.mars_sim.core.map.location;

/**
 * Problem parsing coordinates.
 */
public class CoordinatesException extends Exception {
    
    /**
     * This represents an exception with coordinates parsing.
     * @param message
     */
    CoordinatesException(String message) {
        super(message);
    }
}

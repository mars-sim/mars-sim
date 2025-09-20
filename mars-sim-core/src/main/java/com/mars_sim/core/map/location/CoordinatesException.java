/*
 * Mars Simulation Project
 * CoordinatesException.java
 * @date 2025-09-16
 * @author Barry Evans
 */
package com.mars_sim.core.map.location;

// This should be a checked exception
public class CoordinatesException extends RuntimeException {
    
    /**
     * This represents an exception with coordinates parsing.
     * @param message
     */
    CoordinatesException(String message) {
        super(message);
    }
}

/**
 * Mars Simulation Project
 * BuildingException.java
 * @version 3.1.1 2020-07-22
 * @author Scott Davis
 */
 
package org.mars_sim.msp.core.structure.building;

/**
 * An exception related to settlement buildings.
 */
public class BuildingException extends Exception {
   
    /**
     * Default constructor
     *
     * @param message the exception message.
     */
    public BuildingException(String message) {
        // Use Exception constructor
        super(message);
    }
    
    /**
     * Constructor with message and throwable cause.
     * @param message
     * @param cause
     */
    public BuildingException(String message, Throwable cause) {
    	super(message, cause);
    }
}

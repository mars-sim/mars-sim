/**
 * Mars Simulation Project
 * BuildingException.java
 * @version 2.75 2003-03-16
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building;

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
}

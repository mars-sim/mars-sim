/**
 * Mars Simulation Project
 * Weather.java
 * @version 2.74 2002-04-29
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation;

import java.io.Serializable;

/** Weather represents the weather on Mars */
public class Weather implements Serializable {
    
    // Data members 
    private Mars mars;
    
    /** Constructs a Weather object */
    public Weather(Mars mars) {
        
        this.mars = mars;
    }
    
    /**
     * Gets the air pressure at a given location.
     * @return air pressure in atm.
     */
    public double getAirPressure(Coordinates location) {
	// Avg air pressure in atm.
	// We can change this later.
        return .009D;
    }

    /**
     * Gets the surface temperature at a given location.
     * @return temperature in celsius.
     */
    public double getTemperature(Coordinates location) {
        // Avg surface temp in celsius.
	// We can change this later.
	return -40D;
    }
}

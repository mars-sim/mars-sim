/**
 * Mars Simulation Project
 * Weather.java
 * @version 2.75 2003-01-07
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation;

import java.io.Serializable;

/** Weather represents the weather on Mars */
public class Weather implements Serializable {
    
    // Static data
    // Sea level air pressure in atm.
    private static final double SEA_LEVEL_AIR_PRESSURE = .009D;
    // Sea level air density in kg/m^3.
    private static final double SEA_LEVEL_AIR_DENSITY = .0115D;
    // Mar's gravitational acceleration at sea level (meters/sec^2).
    private static final double SEA_LEVEL_GRAVITY = 3.0D;
    
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
	    
        // Get local elevation in meters.
        TerrainElevation terrainElevation = mars.getSurfaceFeatures().getSurfaceTerrain();
        double elevation = terrainElevation.getElevation(location) * 1000D;
        
        // Get air pressure in units of bar.
        double airPressureBar = SEA_LEVEL_AIR_PRESSURE / 1.01325D;
        
        // p = pressure0 * e(-((density0 * gravitation) / pressure0) * h)
        double pressure = airPressureBar * Math.exp(-1D * 
            SEA_LEVEL_AIR_DENSITY * SEA_LEVEL_GRAVITY / airPressureBar * elevation);
        
        return pressure;
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

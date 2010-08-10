/**
 * Mars Simulation Project
 * Weather.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 * @author Hartmut Prochaska
 */
 
package org.mars_sim.msp.core.mars;

import java.io.Serializable;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;

/** Weather represents the weather on Mars */
public class Weather implements Serializable {
    
    // Static data
    // Sea level air pressure in atm.
    private static final double SEA_LEVEL_AIR_PRESSURE = .009D;
    // Sea level air density in kg/m^3.
    private static final double SEA_LEVEL_AIR_DENSITY = .0115D;
    // Mar's gravitational acceleration at sea level in m/sec^2.
    private static final double SEA_LEVEL_GRAVITY = 3.0D;
    // exreme cold temperatures at Mars
    private static final double EXTREME_COLD = -120D;
    
    /** Constructs a Weather object */
    public Weather() {}
    
    /**
     * Gets the air pressure at a given location.
     * @return air pressure in atm.
     */
    public double getAirPressure(Coordinates location) {
	    
        // Get local elevation in meters.
		Mars mars = Simulation.instance().getMars();
        TerrainElevation terrainElevation = mars.getSurfaceFeatures().getSurfaceTerrain();
        double elevation = terrainElevation.getElevation(location);
        
        // p = pressure0 * e(-((density0 * gravitation) / pressure0) * h)
        // P = 0.009 * e(-(0.0155 * 3.0 / 0.009) * elevation)
        double pressure = SEA_LEVEL_AIR_PRESSURE * Math.exp(-1D *
               SEA_LEVEL_AIR_DENSITY * SEA_LEVEL_GRAVITY / (SEA_LEVEL_AIR_PRESSURE * 1000)
               * elevation);

        return pressure;
    }

    /**
     * Gets the surface temperature at a given location.
     * @return temperature in celsius.
     */
    public double getTemperature(Coordinates location) {
        // easy implementation, needs revision for phi
        // We can change this later.
        
        // standard -120D if extreme cold
        
        double temperature = EXTREME_COLD;
		Mars mars = Simulation.instance().getMars();
        SurfaceFeatures surfaceFeatures = mars.getSurfaceFeatures();
        
        if (surfaceFeatures.inDarkPolarRegion(location)){
            
            //known temperature for cold days at the pole
            
            temperature = -150D;
            
        } else {
           
            // + getSurfaceSunlight * (80D / 127D (max sun))
            // if sun full we will get -40D the avg, if night or twilight we will get 
            // a smoth temperature change and in the night -120D
        
            temperature = temperature + surfaceFeatures.getSurfaceSunlight(location) * 0.629921D;
        
            // not correct but guess: - (elevation * 5)
        
            TerrainElevation terrainElevation = surfaceFeatures.getSurfaceTerrain();
            temperature = temperature - (terrainElevation.getElevation(location) * 5D);
        
            // - ((math.pi/2) / (phi of location)) * 20
            // guess, but could work, later we can implement real physic
        
            double piHalf = Math.PI / 2.0;
            double degrees = 0;
            double phi = location.getPhi();
        
            if (phi < piHalf) {
                degrees = ((piHalf - phi) / piHalf);
            } else if (phi > piHalf){
                degrees = ((phi - piHalf) / piHalf);
            }

            temperature = temperature - (20 * degrees) ;
        
        }
	
        return temperature;
    }
}

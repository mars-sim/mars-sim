/**
 * Mars Simulation Project
 * SurfaceFeatures.java
 * @version 2.76 2004-06-01
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation;

import java.util.*;

/** 
 * SurfaceFeatures represents the surface terrain and landmarks of the virtual Mars. 
 */
public class SurfaceFeatures {
    
    // Data members 
    private TerrainElevation surfaceTerrain;
    private List landmarks;
    
    /** 
     * Constructor 
     * @throws Exception when error in creating surface features.
     */
    public SurfaceFeatures() throws Exception {
        
        surfaceTerrain = new TerrainElevation();

		try {
			SimulationConfig simConfig = Simulation.instance().getSimConfig();
			landmarks = simConfig.getLandmarkConfiguration().getLandmarkList();
		}
		catch (Exception e) {
			throw new Exception("Landmarks could not be loaded: " + e.getMessage());
		}
    }
    
    /** Returns the surface terrain
     *  @return surface terrain
     */
    public TerrainElevation getSurfaceTerrain() {
        return surfaceTerrain;
    }

    /** 
     * Returns a float value representing the current sunlight
     * conditions at a particular location.
     *  
     * @return value from 0.0 - 1.0
     * 0.0 represents night time darkness.
     * 1.0 represents daylight. 
     * Values in between 0.0 and 1.0 represent twilight conditions. 
     */
    public double getSurfaceSunlight(Coordinates location) {
        
		Mars mars = Simulation.instance().getMars();
        Coordinates sunDirection = mars.getOrbitInfo().getSunDirection();
        double angleFromSun = sunDirection.getAngle(location);

        double result = 0;
        double twilightzone = .2D; // Angle width of twilight border (radians)
        if (angleFromSun < (Math.PI / 2D) - (twilightzone / 2D)) {
            result = 1D;
        }
        else if (angleFromSun > (Math.PI / 2D) + (twilightzone / 2D)) {
            result = 0D;
        }
        else {
            double twilightAngle = angleFromSun - ((Math.PI / 2D) - (twilightzone / 2D));
            result = 1D - (twilightAngle / twilightzone);
        }

        return result;
    }    

    /** Returns true if location is in a dark polar region.
     *  A dark polar region is where the sun doesn't rise in the current sol.
     *  @return true if location is in dark polar region
     */
    public boolean inDarkPolarRegion(Coordinates location) {
        
        boolean result = false;

		Mars mars = Simulation.instance().getMars();
        Coordinates sunDirection = mars.getOrbitInfo().getSunDirection();
        double sunPhi = sunDirection.getPhi();
        double darkPhi = 0D;

        if (sunPhi < (Math.PI / 2D)) {
            darkPhi = Math.PI - ((Math.PI / 2D) - sunPhi);
            if (location.getPhi() >= darkPhi) result = true;
        }
        else {
            darkPhi = sunPhi - (Math.PI / 2D);
            if (location.getPhi() < darkPhi) result = true;
        }

        return result;
    }
    
    /**
     * Checks if location is within a polar region of Mars.
     * @param location the location to check.
     * @return true if in polar region.
     */
    public boolean inPolarRegion(Coordinates location) {
    	double polarPhi = .1D * Math.PI;
    	
    	if ((location.getPhi() < polarPhi) || (location.getPhi() > Math.PI - polarPhi))
    		return true;
    	else return false;
    }
    
    /**
     * Gets a list of landmarks on Mars.
     * @return list of landmarks.
     */
    public List getLandmarks() {
    	return landmarks;
    }
}
/**
 * Mars Simulation Project
 * SurfaceFeatures.java
 * @version 2.72 2001-08-12
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation;

/** SurfaceFeatures represents the surface terrain and landmarks of the virtual Mars. */
public class SurfaceFeatures {
    
    // Data members 
    private TerrainElevation surfaceTerrain;
    private VirtualMars mars;
    // We can add landmarks here later - Scott
    
    /** Constructs a SurfaceFeatures object */
    public SurfaceFeatures(VirtualMars mars) {
        
        this.mars = mars;
        surfaceTerrain = new TerrainElevation("map_data/TopoMarsMap.dat", "map_data/TopoMarsMap.index",
                "map_data/TopoMarsMap.sum");
    }
    
    /** Returns the surface terrain
     *  @return surface terrain
     */
    public TerrainElevation getSurfaceTerrain() {
        return surfaceTerrain;
    }

    /** Returns a number representing the current sunlight
     *  conditions at a particular location.
     *  
     *  return value is 127 if full daylight
     *  return value is 0 if night time
     *  return value is between 0 and 127 if twilight
     *  
     *  @return number representing the current sunlight conditions
     */
    public int getSurfaceSunlight(Coordinates location) {
        
        Coordinates sunDirection = mars.getOrbitInfo().getSunDirection();
        double angleFromSun = sunDirection.getAngle(location);

        int result = 0;
        double twilightzone = .2D;
        if (angleFromSun < (Math.PI / 2D) - (twilightzone / 2D)) {
            result = 127;
        }
        else if (angleFromSun > (Math.PI / 2D) + (twilightzone / 2D)) {
            result = 0;
        }
        else {
            double twilightAngle = angleFromSun - ((Math.PI / 2D) - (twilightzone / 2D));
            result = (int) Math.round(127D * (1 - (twilightAngle / twilightzone)));
        }

        return result;
    }       

    /** Returns true if location is in a dark polar region.
     *  A dark polar region is where the sun doesn't rise in the current sol.
     *  @return true if location is in dark polar region
     */
    public boolean inDarkPolarRegion(Coordinates location) {
        
        boolean result = false;

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
}

/**
 * Mars Simulation Project
 * SurfaceFeatures.java
 * @version 2.72 2001-05-10
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
}

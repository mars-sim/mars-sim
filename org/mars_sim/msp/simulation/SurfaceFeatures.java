/**
 * Mars Simulation Project
 * SurfaceFeatures.java
 * @version 2.71 2000-10-30
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation;

/** SurfaceFeatures represents the surface terrain and landmarks of the virtual Mars. */
public class SurfaceFeatures {
    
    // Data members 
    private TerrainElevation surfaceTerrain;
    // We can add landmarks here later - Scott
    
    /** Constructs a SurfaceFeatures object */
    public SurfaceFeatures() {
        
        surfaceTerrain = new TerrainElevation("map_data/TopoMarsMap.dat", "map_data/TopoMarsMap.index",
                "map_data/TopoMarsMap.sum");
    }
    
    /** Returns the surface terrain
     *  @return surface terrain
     */
    public TerrainElevation getSurfaceTerrain() {
        return surfaceTerrain;
    }
}

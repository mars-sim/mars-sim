/**
 * Mars Simulation Project
 * SurfaceMapData.java
 * @version 3.07 2014-12-06

 * @author Scott Davis
 */

package org.mars_sim.mapdata;

/**
 * Surface map data.
 */
public class SurfaceMapData extends IntegerMapData {

    // Static members.
    private static final String INDEX_FILE = "SurfaceMarsMap.index";
    private static final String MAP_FILE = "SurfaceMarsMap.dat";
    
    /**
     * Constructor
     */
    public SurfaceMapData() {
        super(INDEX_FILE, MAP_FILE);
    }
}
/**
 * Mars Simulation Project
 * SurfaceMapData.java
 * @version 3.1.0 2018-10-04
 * @author Scott Davis
 */

package org.mars_sim.mapdata;

/**
 * Surface map data.
 */
public class SurfaceMapData extends IntegerMapData {

    // Static members.
    private static final String INDEX_FILE = "SurfaceMarsMap.index";
    private static final String MAP_FILE = "SurfaceMarsMap.dat"; //"SurfaceMarsMap.xz"; //
    
    /**
     * Constructor
     */
    public SurfaceMapData() {
        super(INDEX_FILE, MAP_FILE);
    }
}
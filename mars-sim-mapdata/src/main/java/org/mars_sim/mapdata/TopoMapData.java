/**
 * Mars Simulation Project
 * TopoMapData.java
 * @version 3.1.0 2018-10-04
 * @author Scott Davis
 */

package org.mars_sim.mapdata;

/**
 * Topographical map data.
 */
public class TopoMapData extends IntegerMapData {

    // Static members.
    private static final String INDEX_FILE = "TopoMarsMap.index";
    private static final String MAP_FILE = "TopoMarsMap.dat"; //"TopoMarsMap.xz"; //
    
    /**
     * Constructor
     */
    public TopoMapData() {
        super(INDEX_FILE, MAP_FILE);
    }
}

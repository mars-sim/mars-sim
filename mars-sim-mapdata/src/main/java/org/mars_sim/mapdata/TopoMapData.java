/*
 * Mars Simulation Project
 * TopoMapData.java
 * @date 2022-08-02
 * @author Scott Davis
 */

package org.mars_sim.mapdata;

/**
 * Topographical map data.
 */
public class TopoMapData extends IntegerMapData {

	// Static members.
    private static final String MAP_FILE = "/maps/topo2880x1440.jpg"; //5760x2880.jpg"
    
    /**
     * Constructor
     */
    public TopoMapData() {
        super(MAP_FILE);
    }
}

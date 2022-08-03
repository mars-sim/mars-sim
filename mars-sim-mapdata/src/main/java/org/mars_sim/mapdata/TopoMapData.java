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
	
    /**
     * Constructor
     */
    public TopoMapData() {
        super(TOPO_MAP_FILE);
    }
}

/*
 * Mars Simulation Project
 * ConstructedBuildingLogEntry.java
 * @date 2021-12-15
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.construction;

import org.mars_sim.msp.core.time.MarsClock;

import java.io.Serializable;

/**
 * A log entry representing a constructed building.
 */
public class ConstructedBuildingLogEntry implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// Data members.
    private String buildingName;
    private MarsClock builtTime;
    
    /**
     * Constructor
     * @param buildingName the name of the constructed building.
     * @param builtTime the time the building was constructed.
     */
    ConstructedBuildingLogEntry(String buildingName, MarsClock builtTime) {
        this.buildingName = buildingName;
        this.builtTime = new MarsClock(builtTime);
    }
    
    /**
     * Gets the constructed building name.
     * @return building name.
     */
    public String getBuildingName() {
        return buildingName;
    }
    
    /**
     * Gets the time stamp when the building was constructed.
     * @return time stamp.
     */
    public MarsClock getBuiltTime() {
        return new MarsClock(builtTime);
    }
}

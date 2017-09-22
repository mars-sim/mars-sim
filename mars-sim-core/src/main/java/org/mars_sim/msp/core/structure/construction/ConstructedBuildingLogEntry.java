/**
 * Mars Simulation Project
 * ConstructedBuildingLogEntry.java
 * @version 3.1.0 2017-09-21
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.construction;

import org.mars_sim.msp.core.time.MarsClock;

import java.io.Serializable;

/**
 * A log entry representing a constructed building.
 */
public class ConstructedBuildingLogEntry implements Serializable {

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
        this.builtTime = (MarsClock) builtTime.clone();
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
        return (MarsClock) builtTime.clone();
    }
}
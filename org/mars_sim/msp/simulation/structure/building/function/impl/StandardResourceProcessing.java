/**
 * Mars Simulation Project
 * StandardResourceProcessing.java
 * @version 2.75 2003-04-16
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building.function.impl;
 
import java.io.Serializable;

import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.simulation.structure.building.ResourceProcessManager;
import org.mars_sim.msp.simulation.structure.building.function.ResourceProcessing;
 
/**
 * Standard implementation of the ResourceProcessing building function.
 */
public class StandardResourceProcessing implements ResourceProcessing, Serializable {
    
    private Building building;
    private double powerDownProcessingLevel;
    private ResourceProcessManager manager;
    
    /**
     * Constructor
     *
     * @param building the building this is implemented for.
     */
    public StandardResourceProcessing(Building building, double powerDownProcessingLevel) {
        this.building = building;
        this.powerDownProcessingLevel = powerDownProcessingLevel;
        manager = new ResourceProcessManager(building, building.getInventory());
    }
    
    /**
     * Gets the building's resource process manager.
     * @return resource process manager
     */
    public ResourceProcessManager getResourceProcessManager() {
        return manager;
    }
    
    /**
     * Gets the power down mode resource processing level.
     * @return proportion of max processing rate (0D - 1D)
     */
    public double getPowerDownResourceProcessingLevel() {
        return powerDownProcessingLevel;
    }
}

/**
 * Mars Simulation Project
 * ResourceProcessing.java
 * @version 2.75 2003-02-06
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building.function;

import org.mars_sim.msp.simulation.structure.building.*;
 
/**
 * The ResourceProcessing interface is a building function indicating 
 * that the building has a set of resource processes.
 */
public interface ResourceProcessing extends Function {
        
    /**
     * Gets the building's resource process manager.
     * @return resource process manager
     */
    public ResourceProcessManager getResourceProcessManager();
    
    /**
     * Gets the power down mode resource processing level.
     * @return proportion of max processing rate (0D - 1D)
     */
    public double getPowerDownResourceProcessingLevel();
}

/**
 * Mars Simulation Project
 * Storage.java
 * @version 2.75 2003-02-11
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building.function;
 
import java.util.*;
 
public interface Storage extends Function {
        
    /** 
     * Gets a map of the resources this building is capable of
     * storing and their amounts in kg.
     * @return Map of resource keys and amount Double values.
     */
    public Map getResourceStorageCapacity();
}

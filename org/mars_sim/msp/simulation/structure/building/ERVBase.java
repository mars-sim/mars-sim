/**
 * Mars Simulation Project
 * ERVBase.java
 * @version 2.75 2002-09-03
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building;

import org.mars_sim.msp.simulation.structure.Settlement;

/**
 * The ERVBase class represents the base structure of an Earth Return Vehicle (ERV).
 * It has a Sebateur reactor to generate oxygen, water and methane from Martian air.
 */
public class ERVBase extends Building {
    
    /**
     * Constructor
     * @param manager - building manager.
     */
    public ERVBase(BuildingManager manager) {
        // Use Bulding constructor
        super("Earth Return Vehicle (ERV) Base", manager);
    }
}

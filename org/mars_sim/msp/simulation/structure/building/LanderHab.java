/**
 * Mars Simulation Project
 * LanderHab.java
 * @version 2.75 2002-10-06
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building;

import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.function.*;

/**
 * The LanderHab class represents a lander habitat building.
 */
public class LanderHab extends InhabitableBuilding 
        implements LivingAccommodations, Research, Communication, EVA, 
        Storage, Recreation, Dining {
    
    /**
     * Constructor
     * @param manager - building manager.
     */
    public LanderHab(BuildingManager manager) {
        // Use InhabitableBulding constructor
        super("Lander Hab", manager, 6);
    }
}

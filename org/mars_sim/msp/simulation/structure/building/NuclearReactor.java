/**
 * Mars Simulation Project
 * NuclearReactor.java
 * @version 2.75 2002-09-03
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building;

import org.mars_sim.msp.simulation.structure.building.function.PowerGeneration;

/**
 * The NuclearReactor class represents a 
 * fission nuclear reactor.
 */
public class NuclearReactor extends Building implements PowerGeneration {
    
    /**
     * Constructor
     * @param manager - building manager.
     */
    public NuclearReactor(BuildingManager manager) {
        // Use Bulding constructor
        super("Nuclear Reactor", manager);
    }
}

/**
 * Mars Simulation Project
 * LanderHab.java
 * @version 2.75 2003-01-22
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building;

import org.mars_sim.msp.simulation.structure.building.function.*;

/**
 * The LanderHab class represents a lander habitat building.
 */
public class LanderHab extends InhabitableBuilding 
        implements LivingAccommodations, Research, Communication, EVA, 
        Storage, Recreation, Dining {
    
    private static final int ACCOMMODATION_CAPACITY = 6;
    
    /**
     * Constructor
     * @param manager - building manager.
     */
    public LanderHab(BuildingManager manager) {
        // Use InhabitableBulding constructor
        super("Lander Hab", manager, ACCOMMODATION_CAPACITY);
    }
    
    /**
     * Gets the accommodation capacity of this building.
     *
     * @return number of accomodations.
     */
    public int getAccommodationCapacity() {
        return ACCOMMODATION_CAPACITY;
    }
    
    /**
     * Gets the power this building currently uses.
     * @return power in kW.
     */
    public double getPowerUsed() {
        return 20D;
    }
}

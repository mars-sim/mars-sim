/**
 * Mars Simulation Project
 * LivingAccommodations.java
 * @version 2.75 2003-01-15
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building.function;
 
public interface LivingAccommodations extends Function {
        
    /**
     * Gets the accommodation capacity of this building.
     *
     * @return number of accomodations.
     */
    public int getAccommodationCapacity();
    
}

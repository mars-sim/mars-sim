/**
 * Mars Simulation Project
 * LivingAccommodations.java
 * @version 2.75 2003-02-15
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building.function;
 
public interface LivingAccommodations extends Function {
        
    // Mass of water used per person per Sol for bathing, etc.
    public final static double WASH_WATER_USAGE_PERSON_SOL = 26D;
        
    /**
     * Gets the accommodation capacity of this building.
     *
     * @return number of accomodations.
     */
    public int getAccommodationCapacity();
 
    /** 
     * Utilizes water for bathing, washing, etc. based on population.
     * @param time amount of time passing (millisols)
     */
    public void waterUsage(double time);
}

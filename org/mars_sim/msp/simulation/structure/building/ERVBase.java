/**
 * Mars Simulation Project
 * ERVBase.java
 * @version 2.75 2003-01-22
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building;

/**
 * The ERVBase class represents the base structure of an Earth Return Vehicle (ERV).
 * It has a Sebateur reactor to generate oxygen, water and methane from Martian air.
 */
public class ERVBase extends Building {
    
    // True if ERVBase is processing chemicals.
    private boolean processing;
    
    /**
     * Constructor
     * @param manager - building manager.
     */
    public ERVBase(BuildingManager manager) {
        // Use Bulding constructor
        super("Earth Return Vehicle (ERV) Base", manager);
        
        processing = true;
    }
    
    /**
     * Gets the power this building currently uses.
     * @return power in kW.
     */
    public double getPowerUsed() {
        // ERVBase has its own power supply.
        return 5D;
    }
    
    /**
     * Gets the power this building currently requires for full-power mode.
     * @return power in kW.
     */
    public double getFullPowerRequired() {
        double result = 0D;
        if (processing) result = 10D;
        return result;
    }
    
    /**
     * Gets the power the building requires for power-down mode.
     * @return power in kW.
     */
    public double getPoweredDownPowerRequired() {
        double result = 0D;
        if (processing) result = 5D;
        return result;
    }
}

/**
 * Mars Simulation Project
 * Building.java
 * @version 2.75 2003-02-15
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building;

import java.io.Serializable;

/**
 * The Building class is an abstract class representing a 
 * settlement's building.
 */
public abstract class Building implements Serializable {
    
    // Power Modes
    public static final String FULL_POWER = "Full Power";
    public static final String POWER_DOWN = "Power Down";
    public static final String NO_POWER = "No Power";
    
    // Data members
    protected BuildingManager manager; 
    protected String name;
    protected String powerMode;
    
    /**
     * Constructs a Building object.
     * @param name the building's name.
     * @param manager the building's building manager.
     */
    public Building(String name, BuildingManager manager) {
        
        this.name = name;
        this.manager = manager;
        this.powerMode = FULL_POWER;
    }
    
    /**
     * Gets the building's building manager.
     *
     * @return building manager
     */
    public BuildingManager getBuildingManager() {
        return manager;
    }
    
    /**
     * Gets the building's name.
     *
     * @return building's name as a String.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Time passing for building.
     * Child building should override this method for things
     * that happen over time for the building.
     *
     * @param time amount of time passing (in millisols)
     */
    public void timePassing(double time) {
        
        // Check for valid argument.
        if (time < 0D) throw new IllegalArgumentException("Time must be > 0D");
    }   
    
    /**
     * Gets the power this building currently requires for full-power mode.
     * @return power in kW.
     */
    public abstract double getFullPowerRequired();
    
    /**
     * Gets the power the building requires for power-down mode.
     * @return power in kW.
     */
    public abstract double getPoweredDownPowerRequired();
     
    /**
     * Gets the building's power mode.
     */
    public String getPowerMode() {
        return powerMode;
    }
    
    /**
     * Sets the building's power mode.
     */
    public void setPowerMode(String powerMode) {
        this.powerMode = powerMode;
    }
}

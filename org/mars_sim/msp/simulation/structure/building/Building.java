/**
 * Mars Simulation Project
 * Building.java
 * @version 2.75 2003-01-22
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building;

import java.io.Serializable;

/**
 * The Building class is an abstract class representing a 
 * settlement's building.
 */
public abstract class Building implements Serializable {
    
    // Data members
    protected BuildingManager manager; 
    protected String name;
    
    /**
     * Constructs a Building object.
     * @param name the building's name.
     * @param manager the building's building manager.
     */
    public Building(String name, BuildingManager manager) {
        
        this.name = name;
        this.manager = manager;
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
    }   
    
    /**
     * Gets the power this building currently uses.
     * @return power in kW.
     */
    public abstract double getPowerUsed();
}

/**
 * Mars Simulation Project
 * Building.java
 * @version 2.75 2003-04-25
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building;

import java.io.Serializable;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.*;
import org.mars_sim.msp.simulation.malfunction.*;

/**
 * The Building class is an abstract class representing a 
 * settlement's building.
 */
public abstract class Building implements Malfunctionable, Serializable {
    
    // Power Modes
    public static final String FULL_POWER = "Full Power";
    public static final String POWER_DOWN = "Power Down";
    public static final String NO_POWER = "No Power";
    
    // Data members
    protected BuildingManager manager; 
    protected String name;
    protected String powerMode;
    protected MalfunctionManager malfunctionManager;
    
    /**
     * Constructs a Building object.
     * @param name the building's name.
     * @param manager the building's building manager.
     */
    public Building(String name, BuildingManager manager) {
        
        this.name = name;
        this.manager = manager;
        this.powerMode = FULL_POWER;
        
        // Set up malfunction manager.
        malfunctionManager = new MalfunctionManager(this, manager.getSettlement().getMars());
        malfunctionManager.addScopeString("Building");
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
        
        // Update malfunction manager.
        malfunctionManager.timePassing(time);
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
    
    /**
     * Gets the entity's malfunction manager.
     * @return malfunction manager
     */
    public MalfunctionManager getMalfunctionManager() {
        return malfunctionManager;
    }
    
    /**
     * Gets a collection of people affected by this entity.
     * Children buildings should add additional people as necessary.
     * @return person collection
     */
    public PersonCollection getAffectedPeople() {
        PersonCollection people = new PersonCollection();

        // Check all people in settlement.
        PersonIterator i = manager.getSettlement().getInhabitants().iterator();
        while (i.hasNext()) {
            Person person = i.next();
            Task task = person.getMind().getTaskManager().getTask();

            // Add all people maintaining this building. 
            if (task instanceof Maintenance) {
                if (((Maintenance) task).getEntity() == this) {
                    if (!people.contains(person)) people.add(person);
                }
            }

            // Add all people repairing this facility.
            if (task instanceof Repair) {
                if (((Repair) task).getEntity() == this) {
                    if (!people.contains(person)) people.add(person);
                }
            }
        }

        return people;
    }
    
    /**
     * Gets the inventory associated with this entity.
     * @return inventory
     */
    public Inventory getInventory() {
        return manager.getSettlement().getInventory();
    }
    
    /**
     * String representation of this building.
     * @return The settlement and building name.
     */
    public String toString() {
        return manager.getSettlement().getName() + " " + getName();
    }
}

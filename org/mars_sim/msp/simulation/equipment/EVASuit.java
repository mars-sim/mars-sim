/**
 * Mars Simulation Project
 * EVASuit.java
 * @version 2.74 2002-04-13
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.equipment;

import org.mars_sim.msp.simulation.*;
import java.io.Serializable;

/** 
 * The EVASuit class represents an EVA suit which provides life support
 * for a person during a EVA operation.
 */
public class EVASuit extends Equipment implements LifeSupport, Serializable {

    // Static members
    private double BASE_MASS = 45D;
    private double OXYGEN_CAPACITY = 1D;
    private double WATER_CAPACITY = 4D;
	
    /**
     * Constructs a EVASuit object.
     */
    public EVASuit(Coordinates location, Mars mars) {
    
        // User Equipment constructor.
	super("EVA Suit", location, mars);

        // Add scope to malfunction manager.
	malfunctionManager.addScopeString("EVASuit");
	malfunctionManager.addScopeString("LifeSupport");
	
	// Set the empty mass of the EVA suit in kg.
	baseMass = BASE_MASS;

        // Set the total mass capacity of the EVA suit in kg.
	inventory.setTotalCapacity(100D);
	
	// Set the resource capacities of the EVA suit.
	inventory.setResourceCapacity(Inventory.OXYGEN, OXYGEN_CAPACITY);
	inventory.setResourceCapacity(Inventory.WATER, WATER_CAPACITY);

	// Set the initial quantity of resources in the EVA suit.
	inventory.addResource(Inventory.OXYGEN, OXYGEN_CAPACITY);
	inventory.addResource(Inventory.WATER, WATER_CAPACITY);
    }

    /** 
     * Returns true if life support is working properly and is not out
     * of oxygen or water.
     * @return true if life support is OK
     */
    public boolean lifeSupportCheck() {
        boolean result = true;

        if (inventory.getResourceMass(Inventory.OXYGEN) <= 0D) result = false;
        if (inventory.getResourceMass(Inventory.WATER) <= 0D) result = false;

        // need to also check for temp and air pressure

        return result;
    }

    /** 
     * Gets the number of people the life support can provide for.
     * @return the capacity of the life support system.
     */
    public int getLifeSupportCapacity() {
        return 1;
    }

    /** 
     * Gets oxygen from system.
     * @param amountRequested the amount of oxygen requested from system (kg)
     * @return the amount of oxygen actually received from system (kg)
     */
    public double provideOxygen(double amountRequested) {
        return inventory.removeResource(Inventory.OXYGEN, amountRequested);
    }

    /**
     * Gets water from the system.
     * @param amountRequested the amount of water requested from system (kg)
     * @return the amount of water actually received from system (kg)
     */
    public double provideWater(double amountRequested) {
        return inventory.removeResource(Inventory.WATER, amountRequested);
    }

    /**
     * Gets the air pressure of the life support system.
     * @return air pressure (atm)
     */
    public double getAirPressure() {
        // Return 1 atm for now.
	return 1D;
    }

    /**
     * Gets the temperature of the life support system.
     * @return temperature (degrees C)
     */
    public double getTemperature() {
        // Return 25 degrees celsius for now.
	return 25D;
    }

    /** 
     * Checks to see if the inventory is at full capacity with oxygen and water.
     * @return true if oxygen and water stores at full capacity
     */
    public boolean isFullyLoaded() {
   
        boolean result = true;

	double oxygen = inventory.getResourceMass(Inventory.OXYGEN);
	if (oxygen != OXYGEN_CAPACITY) result = false;

	double water = inventory.getResourceMass(Inventory.WATER);
	if (water != WATER_CAPACITY) result = false;

	return result;
    }
}

/**
 * Mars Simulation Project
 * EVASuit.java
 * @version 2.79 2006-06-13
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.equipment;

import java.io.Serializable;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.malfunction.MalfunctionManager;
import org.mars_sim.msp.simulation.malfunction.Malfunctionable;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.resource.AmountResource;

/** 
 * The EVASuit class represents an EVA suit which provides life support
 * for a person during a EVA operation.
 */
public class EVASuit extends Equipment implements LifeSupport, Serializable, Malfunctionable {

    // Static members
	public static final String TYPE = "EVA Suit";
    private static final double BASE_MASS = 45D; // Unloaded mass of EVA suit (kg.)
    private static final double GENERAL_CAPACITY = 100D; // General capacity.
    private static final double OXYGEN_CAPACITY = 1D; // Oxygen capacity (kg.)
    private static final double WATER_CAPACITY = 4D; // Water capacity (kg.)
    private static final double NORMAL_AIR_PRESSURE = 1D; // Normal air pressure (atm.)
    private static final double NORMAL_TEMP = 25D; // Normal temperature (celsius)

    // Data members
    protected MalfunctionManager malfunctionManager; // The equipment's malfunction manager
    
    /**
     * Constructor
     * @param location the location of the EVA suit.
     * @throws Exception if error creating EVASuit.
     */
    public EVASuit(Coordinates location) throws Exception {
    
        // User Equipment constructor.
        super(TYPE, location);

        // Add scope to malfunction manager.
        malfunctionManager = new MalfunctionManager(this);
        malfunctionManager.addScopeString("EVA Suit");
        malfunctionManager.addScopeString("Life Support");
        
        // Set maintenance work time.
        malfunctionManager.setMaintenanceWorkTime(500D);
	
        // Set the empty mass of the EVA suit in kg.
        baseMass = BASE_MASS;

        // Set the general mass capacity of the EVA suit in kg.
        inventory.addGeneralCapacity(GENERAL_CAPACITY);
	
        // Set the resource capacities of the EVA suit.
        inventory.addAmountResourceTypeCapacity(AmountResource.OXYGEN, OXYGEN_CAPACITY);
        inventory.addAmountResourceTypeCapacity(AmountResource.WATER, WATER_CAPACITY);
    }
    
    /**
     * Gets the unit's malfunction manager.
     * @return malfunction manager
     */
    public MalfunctionManager getMalfunctionManager() {
        return malfunctionManager;
    }

    /** 
     * Returns true if life support is working properly and is not out
     * of oxygen or water.
     * @return true if life support is OK
     */
    public boolean lifeSupportCheck() {
        boolean result = true;

        if (inventory.getAmountResourceStored(AmountResource.OXYGEN) <= 0D) {
            // System.out.println("bad oxygen");
            result = false;
        }
        if (inventory.getAmountResourceStored(AmountResource.WATER) <= 0D) {
            // System.out.println("bad water");	
            result = false;
        }
        if (malfunctionManager.getOxygenFlowModifier() < 100D) {
            // System.out.println("bad oxygen flow");		
            result = false;
        }
        if (malfunctionManager.getWaterFlowModifier() < 100D) {
            // System.out.println("bad water flow");
            result = false;
        }
        if (getAirPressure() != NORMAL_AIR_PRESSURE) {
            // System.out.println("bad air pressure - " + getAirPressure());	
            result = false;
	    }
        if (getTemperature() != NORMAL_TEMP) {
           // System.out.println("bad temperature - " + getTemperature());	
            result = false;
        }

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
    	double oxygenTaken = amountRequested;
    	double oxygenLeft = inventory.getAmountResourceStored(AmountResource.OXYGEN);
    	if (oxygenTaken > oxygenLeft) oxygenTaken = oxygenLeft;
    	try {
    		inventory.retrieveAmountResource(AmountResource.OXYGEN, oxygenTaken);
    	}
    	catch (InventoryException e) {};
        return oxygenTaken * (malfunctionManager.getOxygenFlowModifier() / 100D);
    }

    /**
     * Gets water from the system.
     * @param amountRequested the amount of water requested from system (kg)
     * @return the amount of water actually received from system (kg)
     */
    public double provideWater(double amountRequested) {
    	double waterTaken = amountRequested;
    	double waterLeft = inventory.getAmountResourceStored(AmountResource.WATER);
    	if (waterTaken > waterLeft) waterTaken = waterLeft;
    	try {
    		inventory.retrieveAmountResource(AmountResource.WATER, waterTaken);
    	}
    	catch (InventoryException e) {};
        return waterTaken * (malfunctionManager.getWaterFlowModifier() / 100D);
    }

    /**
     * Gets the air pressure of the life support system.
     * @return air pressure (atm)
     */
    public double getAirPressure() {
        double result = NORMAL_AIR_PRESSURE * 
	        (malfunctionManager.getAirPressureModifier() / 100D);
        double ambient = Simulation.instance().getMars().getWeather().getAirPressure(location);
        if (result < ambient) return ambient;
        else return result;
    }

    /**
     * Gets the temperature of the life support system.
     * @return temperature (degrees C)
     */
    public double getTemperature() {
        double result = NORMAL_TEMP * 
	        (malfunctionManager.getTemperatureModifier() / 100D);
        double ambient = Simulation.instance().getMars().getWeather().getTemperature(location);
        if (result < ambient) return ambient;
        else return result;
    }

    /** 
     * Checks to see if the inventory is at full capacity with oxygen and water.
     * @return true if oxygen and water stores at full capacity
     */
    public boolean isFullyLoaded() {
   
        boolean result = true;

        double oxygen = inventory.getAmountResourceStored(AmountResource.OXYGEN);
        if (oxygen != OXYGEN_CAPACITY) result = false;

        double water = inventory.getAmountResourceStored(AmountResource.WATER);
        if (water != WATER_CAPACITY) result = false;

        return result;
    }

    /**
     * Time passing for EVA suit.
     * @param time the amount of time passing (millisols)
     * @throws Exception if error during time.
     */
    public void timePassing(double time) throws Exception {
    	try {
        	Unit container = getContainerUnit();
        	if (container instanceof Person) malfunctionManager.activeTimePassing(time);
        	malfunctionManager.timePassing(time);
    	}
    	catch (Exception e) {
    		throw new Exception("EVASuit.timePassing(): " + e.getMessage());
    	}
    }

    /**
     * Gets a collection of people affected by this entity.
     * @return person collection
     */
    public PersonCollection getAffectedPeople() {
        PersonCollection people = super.getAffectedPeople();
        if (containerUnit instanceof Person) {
            if (!people.contains((Person) containerUnit))
                people.add((Person) containerUnit);
        }

        return people;
    }
}

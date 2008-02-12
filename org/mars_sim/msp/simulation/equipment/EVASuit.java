/**
 * Mars Simulation Project
 * EVASuit.java
 * @version 2.79 2006-06-13
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.equipment;

import java.io.Serializable;
import java.util.logging.Logger;

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
    
    private static String CLASS_NAME = "org.mars_sim.msp.simulation.equipment.EVASuit";
	
    private static Logger logger = Logger.getLogger(CLASS_NAME);

    // Static members
	public static final String TYPE = "EVA Suit";
    private static final double BASE_MASS = 45D; // Unloaded mass of EVA suit (kg.)
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
        setBaseMass(BASE_MASS);
	
        // Set the resource capacities of the EVA suit.
        getInventory().addAmountResourceTypeCapacity(AmountResource.OXYGEN, OXYGEN_CAPACITY);
        getInventory().addAmountResourceTypeCapacity(AmountResource.WATER, WATER_CAPACITY);
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
     * @throws Exception if error checking life support.
     */
    public boolean lifeSupportCheck() throws Exception {
        boolean result = true;

        if (getInventory().getAmountResourceStored(AmountResource.OXYGEN) <= 0D) {
            logger.info("bad oxygen");
            result = false;
        }
        if (getInventory().getAmountResourceStored(AmountResource.WATER) <= 0D) {
            logger.info("bad water");
            result = false;
        }
        if (malfunctionManager.getOxygenFlowModifier() < 100D) {
            logger.info("bad oxygen flow");	
            result = false;
        }
        if (malfunctionManager.getWaterFlowModifier() < 100D) {
            logger.info("bad water flow");
            result = false;
        }
        if (getAirPressure() != NORMAL_AIR_PRESSURE) {
            logger.info("bad air pressure - " + getAirPressure());	
            result = false;
	    }
        if (getTemperature() != NORMAL_TEMP) {
            logger.info("bad temperature - " + getTemperature());	
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
     * @throws Exception if error providing oxygen.
     */
    public double provideOxygen(double amountRequested) throws Exception {
    	double oxygenTaken = amountRequested;
    	double oxygenLeft = getInventory().getAmountResourceStored(AmountResource.OXYGEN);
    	if (oxygenTaken > oxygenLeft) oxygenTaken = oxygenLeft;
    	try {
    		getInventory().retrieveAmountResource(AmountResource.OXYGEN, oxygenTaken);
    	}
    	catch (InventoryException e) {};
        return oxygenTaken * (malfunctionManager.getOxygenFlowModifier() / 100D);
    }

    /**
     * Gets water from the system.
     * @param amountRequested the amount of water requested from system (kg)
     * @return the amount of water actually received from system (kg)
     * @throws Exception if error providing water.
     */
    public double provideWater(double amountRequested) throws Exception {
    	double waterTaken = amountRequested;
    	double waterLeft = getInventory().getAmountResourceStored(AmountResource.WATER);
    	if (waterTaken > waterLeft) waterTaken = waterLeft;
    	try {
    		getInventory().retrieveAmountResource(AmountResource.WATER, waterTaken);
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
        double ambient = Simulation.instance().getMars().getWeather().getAirPressure(getCoordinates());
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
        double ambient = Simulation.instance().getMars().getWeather().getTemperature(getCoordinates());
        if (result < ambient) return ambient;
        else return result;
    }

    /** 
     * Checks to see if the inventory is at full capacity with oxygen and water.
     * @return true if oxygen and water stores at full capacity
     * @throws Exception if error checking inventory.
     */
    public boolean isFullyLoaded() throws Exception {
        boolean result = true;

        double oxygen = getInventory().getAmountResourceStored(AmountResource.OXYGEN);
        if (oxygen != OXYGEN_CAPACITY) result = false;

        double water = getInventory().getAmountResourceStored(AmountResource.WATER);
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
        if (getContainerUnit() instanceof Person) {
            if (!people.contains((Person) getContainerUnit()))
                people.add((Person) getContainerUnit());
        }

        return people;
    }
}

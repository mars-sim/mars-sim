/**
 * Mars Simulation Project
 * EVASuit.java
 * @version 2.90 2010-01-20
 * @author Scott Davis
 */

package org.mars_sim.msp.core.equipment;

import java.io.Serializable;
import java.util.Collection;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.InventoryException;
import org.mars_sim.msp.core.LifeSupport;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.resource.AmountResource;

/** 
 * The EVASuit class represents an EVA suit which provides life support
 * for a person during a EVA operation.
 */
public class EVASuit extends Equipment implements LifeSupport, Serializable, Malfunctionable {
    
    private static String CLASS_NAME = "org.mars_sim.msp.simulation.equipment.EVASuit";
	
    private static Logger logger = Logger.getLogger(CLASS_NAME);

    // Static members
	public static final String TYPE = "EVA Suit";
    public static final double EMPTY_MASS = 45D; // Unloaded mass of EVA suit (kg.)
    private static final double OXYGEN_CAPACITY = 1D; // Oxygen capacity (kg.)
    private static final double WATER_CAPACITY = 4D; // Water capacity (kg.)
    private static final double NORMAL_AIR_PRESSURE = 1D; // Normal air pressure (atm.)
    private static final double NORMAL_TEMP = 25D; // Normal temperature (celsius)
    private static final double WEAR_LIFETIME = 334000D; // 334 Sols (1/2 orbit)
    private static final double MAINTENANCE_TIME = 250D; // 250 millisols.

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
        malfunctionManager = new MalfunctionManager(this, WEAR_LIFETIME, MAINTENANCE_TIME);
        malfunctionManager.addScopeString("EVA Suit");
        malfunctionManager.addScopeString("Life Support");
        
        // Set maintenance work time.
        malfunctionManager.setMaintenanceWorkTime(500D);
	
        // Set the empty mass of the EVA suit in kg.
        setBaseMass(EMPTY_MASS);
	
        // Set the resource capacities of the EVA suit.
        getInventory().addAmountResourceTypeCapacity(AmountResource.findAmountResource("oxygen"), 
        		OXYGEN_CAPACITY);
        getInventory().addAmountResourceTypeCapacity(AmountResource.findAmountResource("water"), 
        		WATER_CAPACITY);
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

        if (getInventory().getAmountResourceStored(
        		AmountResource.findAmountResource("oxygen")) <= 0D) {
            logger.info("bad oxygen");
            result = false;
        }
        if (getInventory().getAmountResourceStored(
        		AmountResource.findAmountResource("water")) <= 0D) {
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
    	AmountResource oxygen = AmountResource.findAmountResource("oxygen");
    	double oxygenLeft = getInventory().getAmountResourceStored(oxygen);
    	if (oxygenTaken > oxygenLeft) oxygenTaken = oxygenLeft;
    	try {
    		getInventory().retrieveAmountResource(oxygen, oxygenTaken);
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
    	AmountResource water = AmountResource.findAmountResource("water");
    	double waterLeft = getInventory().getAmountResourceStored(water);
    	if (waterTaken > waterLeft) waterTaken = waterLeft;
    	try {
    		getInventory().retrieveAmountResource(water, waterTaken);
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

        AmountResource oxygenResource = AmountResource.findAmountResource("oxygen");
        double oxygen = getInventory().getAmountResourceStored(oxygenResource);
        if (oxygen != OXYGEN_CAPACITY) result = false;

        AmountResource waterResource = AmountResource.findAmountResource("water");
        double water = getInventory().getAmountResourceStored(waterResource);
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

    @Override
    public Collection<Person> getAffectedPeople() {
        Collection<Person> people = super.getAffectedPeople();
        if (getContainerUnit() instanceof Person) {
            if (!people.contains((Person) getContainerUnit()))
                people.add((Person) getContainerUnit());
        }

        return people;
    }
}

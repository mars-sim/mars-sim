/**
 * Mars Simulation Project
 * StandardPowerSource.java
 * @version 2.85 25.20.2008
 * @author Sebastien Venot
 */
package org.mars_sim.msp.simulation.structure.building.function;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.InventoryException;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.resource.ResourceException;
import org.mars_sim.msp.simulation.structure.building.Building;

public class FuelPowerSource extends PowerSource implements Serializable {

    private static final long serialVersionUID = 1L;

    private static String CLASS_NAME = "org.mars_sim.msp.simulation.structure.building.function.FuelPowerSource";

    private static Logger logger = Logger.getLogger(CLASS_NAME);

    private final static String TYPE = "Fuel Power Source";

    // The work time (millisol) required to toggle this power source on or off. 
    public static final double TOGGLE_RUNNING_WORK_TIME_REQUIRED = 10D;
    
    private boolean toggle = false;

    // A fuelpower source works only with one kind of fuel
    // similar to cars
    private AmountResource resource;

    private double consumptionSpeed;
    
    private double toggleRunningWorkTime;

    /**
     * @param type
     * @param maxPower
     */
    public FuelPowerSource(double _maxPower, boolean _toggle, String fuelType,
            double _consumptionSpeed) {
        super(TYPE, _maxPower);
        consumptionSpeed = _consumptionSpeed;
        toggle = _toggle;

        try {
            resource = AmountResource.findAmountResource(fuelType);
        } catch (ResourceException e) {
            logger.log(Level.SEVERE, "Could not get fuel resource", e);
        }
    }

    /*
     * 
     */
    @Override
    public double getCurrentPower(Building building) {
        try {
            if (isToggleON()) {
                double fuelStored = building.getInventory()
                        .getAmountResourceStored(resource);
                if (fuelStored > 0) {
                    return getMaxPower();
                } else {
                    return 0;
                }
            } else {
                return 0;
            }
        } catch (InventoryException e) {
            logger.log(Level.SEVERE,
                    "Issues when getting power frong fuel source", e);
            return 0;
        }
    }

    public void toggleON() {
        toggle = true;
    }

    public void toggleOFF() {
        toggle = false;
    }

    public boolean isToggleON() {
        return toggle;
    }

    public void consumeFuel(double time, Inventory inv) {
        try {
            double consumptionRateMillisol = consumptionSpeed / 1000D;
            double consumedFuel = time * consumptionRateMillisol;
            double fuelStored = inv.getAmountResourceStored(resource);

            if (fuelStored < consumedFuel) {
                consumedFuel = fuelStored;
            }

            inv.retrieveAmountResource(resource, consumedFuel);
        } catch (InventoryException e) {
            logger.log(Level.SEVERE, "Issues when consuming fuel", e);
        }

    }
    
    /**
     * Gets the amount resource used as fuel.
     * @return amount resource.
     */
    public AmountResource getFuelResource() {
        return resource;
    }
    
    /**
     * Gets the rate the fuel is consumed.
     * @return rate (kg/Sol).
     */
    public double getFuelConsumptionRate() {
        return consumptionSpeed;
    }
    
    /**
     * Adds work time to toggling the power source on or off.
     * @param time the amount (millisols) of time to add.
     */
    public void addToggleWorkTime(double time) {
        toggleRunningWorkTime += time;
        if (toggleRunningWorkTime >= TOGGLE_RUNNING_WORK_TIME_REQUIRED) {
            toggleRunningWorkTime = 0D;
            toggle = !toggle;
             if (toggle) logger.info(getType() + " turned on.");
             else logger.info(getType() + " turned off.");
        }
    }
}
/**
 * Mars Simulation Project
 * StandardPowerSource.java
 * @version 3.02 2011-11-26
 * @author Sebastien Venot
 */
package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.goods.Good;
import org.mars_sim.msp.core.structure.goods.GoodsManager;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;

import java.io.Serializable;
import java.util.logging.Logger;

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
     * Constructor
     * @param _maxPower the maximum power (kW) of the power source.
     * @param _toggle if the power source is toggled on or off.
     * @param fuelType the fuel type.
     * @param _consumptionSpeed the rate of fuel consumption (kg/Sol).
     */
    public FuelPowerSource(double _maxPower, boolean _toggle, String fuelType,
            double _consumptionSpeed) {
        super(TYPE, _maxPower);
        consumptionSpeed = _consumptionSpeed;
        toggle = _toggle;
        resource = AmountResource.findAmountResource(fuelType);
    }

    @Override
    public double getCurrentPower(Building building) {

        if (toggle) {
            double fuelStored = building.getInventory().getAmountResourceStored(resource);
            if (fuelStored > 0) {
                return getMaxPower();
            } else {
                return 0;
            }
        } else {
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
        
        double consumptionRateMillisol = consumptionSpeed / 1000D;
        double consumedFuel = time * consumptionRateMillisol;
        double fuelStored = inv.getAmountResourceStored(resource);

        if (fuelStored < consumedFuel) {
            consumedFuel = fuelStored;
        }

        inv.retrieveAmountResource(resource, consumedFuel);
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

    @Override
    public double getAveragePower(Settlement settlement) {
        double fuelPower = getMaxPower();
        AmountResource fuelResource = getFuelResource();
        Good fuelGood = GoodsUtil.getResourceGood(fuelResource);
        GoodsManager goodsManager = settlement.getGoodsManager();
        double fuelValue = goodsManager.getGoodValuePerItem(fuelGood);
        fuelValue *= getFuelConsumptionRate();
        fuelPower -= fuelValue;
        if (fuelPower < 0D) fuelPower = 0D;
        return fuelPower;
    }
    
    @Override
    public void destroy() {
        super.destroy();
        
        resource = null;
    }
}
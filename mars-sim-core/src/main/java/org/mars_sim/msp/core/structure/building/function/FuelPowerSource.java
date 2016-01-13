/**
 * Mars Simulation Project
 * StandardPowerSource.java
 * @version 3.07 2014-12-06
 * @author Sebastien Venot
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.goods.Good;
import org.mars_sim.msp.core.structure.goods.GoodsManager;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;

public class FuelPowerSource
extends PowerSource
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final double MAINTENANCE_FACTOR = 2D;
	
	/** default logger. */
	private static Logger logger = Logger.getLogger(FuelPowerSource.class.getName());

	/** The work time (millisol) required to toggle this power source on or off. */
	public static final double TOGGLE_RUNNING_WORK_TIME_REQUIRED = 10D;
	public static final double kW_PER_FUEL_CELL_STACK = 5D;

	private boolean toggle = false;
	private boolean installed = false;
	/** A fuel power source works only with one kind of fuel similar to cars. */
	private AmountResource resource;
	private ItemResource cellStack;
	private Building building;

	private int numFuelCellStackinUse;

	private double consumptionSpeed;
	private double toggleRunningWorkTime;
	private double _maxPower;

	/**
	 * Constructor.
	 * @param _maxPower the maximum power (kW) of the power source.
	 * @param _toggle if the power source is toggled on or off.
	 * @param fuelType the fuel type.
	 * @param _consumptionSpeed the rate of fuel consumption (kg/Sol).
	 */
	public FuelPowerSource(double _maxPower, boolean _toggle, String fuelType,
			double _consumptionSpeed) {
		super(PowerSourceType.FUEL, _maxPower);
		consumptionSpeed = _consumptionSpeed;
		toggle = _toggle;
		this._maxPower = _maxPower;
		resource = AmountResource.findAmountResource(fuelType);
		// 2015-09-29 Added "fuel cell stack"
		cellStack = ItemResource.findItemResource("fuel cell stack");
		installed = false;
	}

	@Override
	public double getCurrentPower(Building building) {
		this.building = building;
/*
		// 2015-09-28 Retrieved of 3 fuel cell stacks
		if (!installed) {
			double numCellStack = building.getInventory().getItemResourceNum(cellStack);
			numFuelCellStackinUse = (int)Math.round(this._maxPower/kW_PER_FUEL_CELL_STACK); //
			if (numCellStack >= numFuelCellStackinUse) {
				building.getInventory().retrieveItemResources(cellStack, numFuelCellStackinUse);
				installed = true;
				logger.info("getCurrentPower() : just installed " + numFuelCellStackinUse + " fuel cell stack(s) on the Methane Power Generator Building");
			}
		}
*/
		if (toggle) {
			double fuelStored = building.getInventory().getAmountResourceStored(resource, false);
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
		double fuelStored = inv.getAmountResourceStored(resource, false);

		if (fuelStored < consumedFuel) {
			consumedFuel = fuelStored;
		}

		inv.retrieveAmountResource(resource, consumedFuel);

		// 2015-01-09 Added addDemandTotalRequest()
	    inv.addAmountDemandTotalRequest(resource);
		// 2015-01-09 addDemandRealUsage()
	   	inv.addAmountDemand(resource, consumedFuel);
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
			 if (toggle) logger.info(Msg.getString("FuelPowerSource.log.turnedOn",getType().getString())); //$NON-NLS-1$
			 else logger.info(Msg.getString("FuelPowerSource.log.turnedOff",getType().getString())); //$NON-NLS-1$
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
	 public double getMaintenanceTime() {
	     return getMaxPower() * MAINTENANCE_FACTOR;
	 }

	 // 2015-09-28 Added removeFromSettlement() to return the fuel cell stacks to the inventory
	 public void removeFromSettlement() {
/*
		//if (installed) {
			//double numCellStack = building.getInventory().getItemResourceNum(cellStack);
			building.getInventory().storeItemResources(cellStack, numFuelCellStackinUse);
			installed = false;
			logger.info("getCurrentPower() : just returned the " + numFuelCellStackinUse + " fuel cell stack(s) used by the Methane Power Generator Building");
		//}
*/
	 }

	 @Override
	 public void destroy() {
		 super.destroy();
		 cellStack = null;
		 building = null;
		 resource = null;
	 }
}
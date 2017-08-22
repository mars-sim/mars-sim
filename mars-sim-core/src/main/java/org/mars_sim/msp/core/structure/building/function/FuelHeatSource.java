/**
 * Mars Simulation Project
 * FuelHeatSource.java
 * @version 3.07 2014-11-02
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.goods.Good;
import org.mars_sim.msp.core.structure.goods.GoodsManager;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;

public class FuelHeatSource
extends HeatSource
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(FuelHeatSource.class.getName());

	/** The work time (millisol) required to toggle this heat source on or off. */
	public static final double TOGGLE_RUNNING_WORK_TIME_REQUIRED = 10D;

	private boolean toggle = false;
	/** A fuelheat source works only with one kind of fuel similar to cars. */
	private AmountResource resource;
	private double consumptionSpeed;
	private double toggleRunningWorkTime;

	/**
	 * Constructor.
	 * @param _maxHeat the maximum power/heat (kW) of the heat source.
	 * @param _toggle if the heat source is toggled on or off.
	 * @param fuelType the fuel type.
	 * @param _consumptionSpeed the rate of fuel consumption (kg/Sol).
	 */
	public FuelHeatSource(double _maxHeat, boolean _toggle, String fuelType,
			double _consumptionSpeed) {
		super(HeatSourceType.FUEL_HEATING, _maxHeat);
		consumptionSpeed = _consumptionSpeed;
		toggle = _toggle;
		resource = AmountResource.findAmountResource(fuelType);
	}

	@Override
	public double getCurrentHeat(Building building) {

		if (toggle) {
			double fuelStored = building.getSettlementInventory().getAmountResourceStored(resource, false);
			if (fuelStored > 0) {
				return getMaxHeat();
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
	  * Adds work time to toggling the heat source on or off.
	  * @param time the amount (millisols) of time to add.
	  */
	 public void addToggleWorkTime(double time) {
		 toggleRunningWorkTime += time;
		 if (toggleRunningWorkTime >= TOGGLE_RUNNING_WORK_TIME_REQUIRED) {
			 toggleRunningWorkTime = 0D;
			 toggle = !toggle;
			 if (toggle) logger.info(Msg.getString("FuelHeatSource.log.turnedOn",getType().getString())); //$NON-NLS-1$
			 else logger.info(Msg.getString("FuelHeatSource.log.turnedOff",getType().getString())); //$NON-NLS-1$
		 }
	 }

	 @Override
	 public double getAverageHeat(Settlement settlement) {
		 double fuelHeat = getMaxHeat();
		 AmountResource fuelResource = getFuelResource();
		 Good fuelGood = GoodsUtil.getResourceGood(fuelResource);
		 GoodsManager goodsManager = settlement.getGoodsManager();
		 double fuelValue = goodsManager.getGoodValuePerItem(fuelGood);
		 fuelValue *= getFuelConsumptionRate();
		 fuelHeat -= fuelValue;
		 if (fuelHeat < 0D) fuelHeat = 0D;
		 return fuelHeat;
	 }
	 
	 @Override
	 public double getMaintenanceTime() {
	     return getMaxHeat() * 2D;
	 }

	@Override
	public double getEfficiency() {
		return 1;
	}
	

	 @Override
	 public void destroy() {
		 super.destroy();
		 resource = null;
	 }
}
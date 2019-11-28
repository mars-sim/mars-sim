/**
 * Mars Simulation Project
 * FuelPowerSource.java
 * @version 3.1.0 2017-09-07
 * @author Sebastien Venot
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
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
	public static final double TOGGLE_RUNNING_WORK_TIME_REQUIRED = 2D;
	
	//public static final double kW_PER_FUEL_CELL_STACK = 5D;
	
	public static final double ELECTRICAL_EFFICIENCY = .7125;

	//private int numFuelCellStackinUse;

	private double rate;
	
	private double toggleRunningWorkTime;
	
	private double maxFuel;
	
	private double time;
	
	private boolean toggle = false;
	
	private static int oxygenID = ResourceUtil.oxygenID;
	private static int methaneID = ResourceUtil.methaneID;

	/**
	 * Constructor.
	 * @param _maxPower the maximum power (kW) of the power source.
	 * @param _toggle if the power source is toggled on or off.
	 * @param fuelType the fuel type.
	 * @param _consumptionSpeed the rate of fuel consumption (kg/Sol).
	 */
	public FuelPowerSource(double _maxPower, boolean _toggle, String fuelType,
			double _consumptionSpeed) {
		super(PowerSourceType.FUEL_POWER, _maxPower);
		rate = _consumptionSpeed;
		toggle = _toggle;
		
		// Added "fuel cell stack"
		//cellStack = ItemResource.findItemResource("fuel cell stack");
		//installed = false;
	}
	
//	 Note : every mole of methane (16 g) releases 810 KJ of energy if burning with 2 moles of oxygen (64 g)
//	 CH4(g) + 2O2(g) -> CO2(g) + 2 H2O(g), deltaH = -890 kJ 
//	 
//	 CnH2n+2 + (3n + 1)O2 -> nCO2 + (n + 1)H2O + (6n + 2)e- 
//
//	 Assume electric efficiency at 40%,
//	 356kW needs 16 g/s 
//	 60kW needs 2.6966 g/s or 239.3939 g/millisol or kg/sol
//	 1 kW_e <- 3.9899 g/millisol
//	 
//	 Assume electric efficiency at 100%,
//	 1 kW_e <- 1.5960 g/millisol
//	 
//	 SOFC uses methane with 1100 W-hr/kg, 
//	 This translate to 71.25 % efficiency

	/**
	 * Consumes the fuel
	 * @param time
	 * @param inv
	 * @return the amount of fuel consumed
	 */
	public double consumeFuel(double time, Inventory inv) {
		this.time = time;
		 
		double rate_millisol = rate / 1000D;
		
		maxFuel = time * rate_millisol;
		
		double consumed = 0;
		
		double fuelStored = inv.getAmountResourceStored(methaneID, false);
		double o2Stored = inv.getAmountResourceStored(oxygenID, false);

		// Note that 16 g of methane requires 64 g of oxygen, a 1 to 4 ratio
		consumed = Math.min(maxFuel, Math.min(fuelStored, o2Stored/4D));

		inv.retrieveAmountResource(methaneID, consumed);
		inv.retrieveAmountResource(oxygenID, 4D*consumed);
		
	    inv.addAmountDemandTotalRequest(methaneID, consumed);
	   	inv.addAmountDemand(methaneID, consumed);
	   	
	    inv.addAmountDemandTotalRequest(oxygenID, consumed);
	   	inv.addAmountDemand(oxygenID, 4D*consumed);
	   	
		return consumed;
	}
	
	 public void setTime(double time) {
		 this.time = time;
	 }
	 
	 @Override
	 public double getCurrentPower(Building building) {

			// Retrieve of 3 fuel cell stacks
//			if (!installed) {
//				double numCellStack = building.getSettlementInventory().getItemResourceNum(cellStack);
//				numFuelCellStackinUse = (int)Math.round(this._maxPower/kW_PER_FUEL_CELL_STACK); //
//				if (numCellStack >= numFuelCellStackinUse) {
//					building.getSettlementInventory().retrieveItemResources(cellStack, numFuelCellStackinUse);
//					installed = true;
//					logger.info("getCurrentPower() : just installed " + numFuelCellStackinUse + " fuel cell stack(s) on the Methane Power Generator Building");
//				}
//			}
		 
		 if (toggle) {
			 double spentFuel = consumeFuel(time, building.getInventory());
//			 logger.info("getCurrentPower(). spentFuel: " +  Math.round(spentFuel* 100.0)/100.0 + " kW"
//					 + "   spentFuel: " +  Math.round(spentFuel* 100.0)/100.0 + " kW"
//					 + "   getMaxPower(): " +  Math.round(getMaxPower()* 100.0)/100.0 + " kW"
//					 + "   spentFuel/maxFuel * ELECTRICAL_EFFICIENCY: " +  Math.round(spentFuel/maxFuel * ELECTRICAL_EFFICIENCY * 100.0)/100.0 + " kW"
//					 );		 
			 return getMaxPower() * spentFuel/maxFuel * ELECTRICAL_EFFICIENCY;
		 }
		 
		 return 0;
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


//	/**
//	 * Gets the amount resource used as fuel.
//	 * @return amount resource.
//	 */
//	 public AmountResource getFuelResource() {
//		return methaneAR;
//	}

	/**
	 * Gets the amount resource used as fuel.
	 * @return amount resource.
	 */
	 public int getFuelResourceID() {
		return methaneID;
	}
		 
	/**
	 * Gets the rate the fuel is consumed.
	 * @return rate (kg/Sol).
	 */
	 public double getFuelConsumptionRate() {
		 return rate;
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
			 if (toggle) logger.info(Msg.getString("FuelPowerSource.log.turnedOn",getType().getName())); //$NON-NLS-1$
			 else logger.info(Msg.getString("FuelPowerSource.log.turnedOff",getType().getName())); //$NON-NLS-1$
		 }
	 }

	 @Override
	 public double getAveragePower(Settlement settlement) {
		double fuelPower = getMaxPower();
//			Good fuelGood = GoodsUtil.getResourceGood(methaneID);
//			GoodsManager goodsManager = settlement.getGoodsManager();
		double fuelValue = settlement.getGoodsManager().getGoodValuePerItem(GoodsUtil.getResourceGood(methaneID));
		fuelValue *= getFuelConsumptionRate() / 1000D * time;
		fuelPower -= fuelValue;
		if (fuelPower < 0D) fuelPower = 0D;
//		logger.info("getAveragePower(). fuelPower: " +  Math.round(fuelPower* 100.0)/100.0 + " kW");
		return fuelPower;
	 }

	 @Override
	 public double getMaintenanceTime() {
	    return getMaxPower() * MAINTENANCE_FACTOR;
	 }

	 // Return the fuel cell stacks to the inventory
	 public void removeFromSettlement() {
//		//if (installed) {
//			//double numCellStack = building.getSettlementInventory().getItemResourceNum(cellStack);
//			building.getSettlementInventory().storeItemResources(cellStack, numFuelCellStackinUse);
//			installed = false;
//			logger.info("getCurrentPower() : just returned the " + numFuelCellStackinUse + " fuel cell stack(s) used by the Methane Power Generator Building");
//		//}
	 }

	 @Override
	 public void destroy() {
		 super.destroy();
	 }

}
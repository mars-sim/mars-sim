/*
 * Mars Simulation Project
 * FuelPowerSource.java
 * @date 2021-10-21
 * @author Sebastien Venot
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

public class FuelPowerSource
extends PowerSource
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final double MAINTENANCE_FACTOR = 2D;
	
	/** default logger. */
	private static final Logger logger = Logger.getLogger(FuelPowerSource.class.getName());

	/** The work time (millisol) required to toggle this power source on or off. */
	public static final double TOGGLE_RUNNING_WORK_TIME_REQUIRED = 2D;
	
	public static final double ELECTRICAL_EFFICIENCY = .7125;

	private double rate;
	
	private double toggleRunningWorkTime;
	
	private double maxFuel;
	
	private double time;
	
	private boolean toggle = false;
	
	private static final int oxygenID = ResourceUtil.oxygenID;
	private static final int methaneID = ResourceUtil.methaneID;

	/**
	 * Constructor.
	 * 
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
	}
	
//	 Note : every mole of methane (16 g) releases 810 KJ of energy if burning with 2 moles of oxygen (64 g)
//	 CH4(g) + 2O2(g) -> CO2(g) + 2 H2O(g), deltaH = -890 kJ 
//	 
//	 CnH2n+2 + (3n + 1)O2 -> nCO2 + (n + 1)H2O + (6n + 2)e- 

//	it produces 890kW at the consumption rate of 16 g/s
//  or it produces 1kW at .018 g/s
	
//	Since each martian sol has 88775 earth seconds (=24*60*60 + 39*60 + 35.244),
	
//	1kW needs 1.59795 kg/sol. 
//	5kW needs 7.9897 kg/sol. 
//	60kW needs 95.877 kg/sol.
	
//	 SOFC uses methane with 1100 W-hr/kg, 
//	 This translate to 71.25 % efficiency
		
	/**
	 * Consumes the fuel.
	 * 
	 * @param time
	 * @param inv
	 * @return the amount of fuel consumed
	 */
	public double consumeFuel(double time, Settlement settlement) {
		this.time = time;
		 
		double rate_millisol = rate / 1000D;
		
		maxFuel = time * rate_millisol;
		
		double consumed = 0;
		
		double fuelStored = settlement.getAmountResourceStored(methaneID);
		double o2Stored = settlement.getAmountResourceStored(oxygenID);

		// Note that 16 g of methane requires 64 g of oxygen, a 1 to 4 ratio
		consumed = Math.min(maxFuel, Math.min(fuelStored, o2Stored/4D));

		settlement.retrieveAmountResource(methaneID, consumed);
		settlement.retrieveAmountResource(oxygenID, 4 * consumed);

		return consumed;
	}
	
	public void setTime(double time) {
		this.time = time;
	}
	 
	@Override
	public double getCurrentPower(Building building) {
		if (toggle) {
			double spentFuel = consumeFuel(time, building.getSettlement());	 
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

	/**
	 * Gets the amount resource used as fuel.
	 * 
	 * @return amount resource.
	 */
	 public int getFuelResourceID() {
		return methaneID;
	}
		 
	/**
	 * Gets the rate the fuel is consumed.
	 * 
	 * @return rate (kg/Sol).
	 */
	 public double getFuelConsumptionRate() {
		 return rate;
	 }

	 /**
	  * Adds work time to toggling the power source on or off.
	  * Called by ToggleFuelPowerSource.
	  * 
	  * @param time the amount (millisols) of time to add.
	  */
	 public void addToggleWorkTime(double time) {
		 toggleRunningWorkTime += time;
		 if (toggleRunningWorkTime >= TOGGLE_RUNNING_WORK_TIME_REQUIRED) {
			 toggleRunningWorkTime = 0D;
			 toggle = !toggle;
			 if (toggle) logger.fine(Msg.getString("FuelPowerSource.log.turnedOn",getType().getName())); //$NON-NLS-1$
			 else logger.fine(Msg.getString("FuelPowerSource.log.turnedOff",getType().getName())); //$NON-NLS-1$
		 }
	 }

	 @Override
	 public double getAveragePower(Settlement settlement) {
		double fuelPower = getMaxPower();
		double fuelValue = settlement.getGoodsManager().getGoodValuePerItem(methaneID);
		fuelValue *= getFuelConsumptionRate() / 1000D * time;
		fuelPower -= fuelValue;
		if (fuelPower < 0D) fuelPower = 0D;
		return fuelPower;
	 }

	 @Override
	 public double getMaintenanceTime() {
	    return getMaxPower() * MAINTENANCE_FACTOR;
	 }

	
	 // Return the fuel cell stacks to the inventory
	 public void removeFromSettlement() {
	 }

	 @Override
	 public void destroy() {
		 super.destroy();
	 }

}

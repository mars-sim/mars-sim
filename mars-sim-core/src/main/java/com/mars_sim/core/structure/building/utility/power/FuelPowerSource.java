/*
 * Mars Simulation Project
 * FuelPowerSource.java
 * @date 2024-08-03
 * @author Sebastien Venot
 */
package com.mars_sim.core.structure.building.utility.power;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.tools.Msg;

/**
 * A fuel power source that gives a steady supply of power.
 */
public class FuelPowerSource extends PowerSource {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(FuelPowerSource.class.getName());

	private static final double MAINTENANCE_FACTOR = .1;
	
	/** The ratio of fuel to oxidizer by mass. */
	private static final int RATIO = 4;

	private static final int OXYGEN_ID = ResourceUtil.oxygenID;
	private static final int METHANE_ID = ResourceUtil.methaneID;
	
	/** The work time (millisol) required to toggle this power source on or off. */
	private static final double TOGGLE_RUNNING_WORK_TIME_REQUIRED = 2D;
	/** The consumption rate [g/millisol or kg/sol] of methane if burning at 100% efficiency. */
	public static final double CONSUMPTION_RATE = 1.59795;
	
	private boolean toggle = false;
	
	/** The thermal efficiency for this source. */
	public double thermalEfficiency = .9;
	/** The electric efficiency for this source. */
	public double electricEfficiency = .7125;

	/** The running work time. */
	private double toggleRunningWorkTime;
	/** The amount of reserved fuel. */
	private double reserveFuel;
	/** The amount of reserved oxidizer. */
	private double reserveOxidizer;
	/** The time span [in millisol] in releasing the power. */
	private double time;
	/** The tank capacity for holding the fuel [in kg]. */
	private double tankCap;
	/** The percentage for producing electricity. */
	public double percentElectricity = 100;
	
	private Building building;
	
	/**
	 * Constructor.
	 * 
	 * @param maxPower the maximum power (kW) of the power source.
	 * @param toggle if the power source is toggled on or off.
	 * @param fuelType the fuel type.
	 * @param consumptionSpeed the rate of fuel consumption (kg/sol).
	 */
	public FuelPowerSource(Building building, double maxPower, boolean toggle, String fuelType) {
		super(PowerSourceType.FUEL_POWER, maxPower);
		this.toggle = toggle;
		this.building = building;
		
		if (building.isInhabitable()) {
			// e.g. Methane Power Generator
			// Use maxHeat to determine tank size
			tankCap = .4 * maxPower;
		}
		else {
			// this methane generator is co-located within its host building
			tankCap = .2 * maxPower ;
		}
	}
	
	/**
	 * Gets the fuel in kg/millisol/kW.
	 * 
	 * @param isElectric
	 * @return
	 */
	private double getMaxFuelPerMillisolPerkW(boolean isElectric) {
		if (isElectric)
			return CONSUMPTION_RATE / 1000 / thermalEfficiency / electricEfficiency ;
		else
			return CONSUMPTION_RATE / 1000 / thermalEfficiency;
	}
	
	/**
	 * Calculates the amount of fuel to be consumed.
	 * 
	 * @param time
	 * @param onlyRequest. If true, then it won't deduct the fuel
	 * @param percent
	 * @return
	 */
	private double computeFuelConsumption(double time, double percent, 
			boolean isElectric, boolean onlyRequest) {
		double consumed = 0;

		// fuel [kg] = [kW] / [percent] * [kg/millisols/kW] * [millisols]
		double deltaFuel = getMaxPower() / percent * 100.0  * getMaxFuelPerMillisolPerkW(isElectric) * time;

		if (!onlyRequest) {
			
			// Retrieve the fuel and oxidizer from the temporary tanks
			if (deltaFuel <= reserveFuel && deltaFuel * RATIO <= reserveOxidizer) {
				reserveFuel -= deltaFuel;
				reserveOxidizer -= deltaFuel * RATIO;
				
				consumed = deltaFuel;
			}
			else {
				double fuelStored = getSettlement().getAmountResourceStored(METHANE_ID);
				double o2Stored = getSettlement().getAmountResourceStored(OXYGEN_ID);
				
				double transferFuel = tankCap + deltaFuel - reserveFuel;
				
				if (transferFuel <= fuelStored && transferFuel * RATIO <= o2Stored) {
					
					reserveFuel = tankCap;
					reserveOxidizer = tankCap * RATIO;
					
					getSettlement().retrieveAmountResource(METHANE_ID, transferFuel);
					getSettlement().retrieveAmountResource(OXYGEN_ID, transferFuel * RATIO);
					
					consumed = deltaFuel;
				}
			}
		}

		return consumed;
	}
	
	public Settlement getSettlement() {
		return building.getSettlement();
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
	
	@Override
	public void setTime(double time) {
		this.time = time;
	}
	
	/**
	 * Gets the amount resource used as fuel.
	 * 
	 * @return amount resource.
	 */
	 public int getFuelResourceID() {
		return METHANE_ID;
	}
		 
	/**
	 * Gets the rate the fuel is consumed.
	 * 
	 * @return rate (kg/sol).
	 */
	 public double getFuelConsumptionRate() {
		 return CONSUMPTION_RATE;
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

			 String msgKey = (toggle ? "FuelPowerSource.log.turnedOn" : "FuelPowerSource.log.turnedOff");
			 logger.fine(Msg.getString(msgKey,getType().getName())); //$NON-NLS-1$
		 }
	 }

	 @Override
	 public double getAveragePower(Settlement settlement) {
		double fuelPower = getMaxPower();
		double fuelValue = settlement.getGoodsManager().getGoodValuePoint(METHANE_ID);
		fuelValue *= getFuelConsumptionRate() / 1000D * time;
		fuelPower -= fuelValue;
		if (fuelPower < 0D) fuelPower = 0D;
		return fuelPower;
	 }

	 /**
	  * Return the percentage of electricity allocated for this heat source.
	  * 
	  * @return
	  */
	 public double getPercentElectricity() {
		 return percentElectricity;
	 }
		
	 @Override
	 public double getMaintenanceTime() {
	    return getMaxPower() * MAINTENANCE_FACTOR;
	 }
	 
	 @Override
	 public double getCurrentPower(Building building) {
		if (toggle) {
			double spentFuel = computeFuelConsumption(time, getPercentElectricity(), true, false);
			return spentFuel / getMaxFuelPerMillisolPerkW(true) / time;
		}
		return 0;
	 }
	
	 /**
	   * Requests an estimate of the power produced by this power source.
	   * 
	   * @param percent The percentage of capacity of this power source
	   * @return power (kWe)
	   */
	 @Override
	 public double requestPower(double percent) {
		 double spentFuel = computeFuelConsumption(time, percent, true, true);
		 return spentFuel / getMaxFuelPerMillisolPerkW(false) / time;
	 }
}

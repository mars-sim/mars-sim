/*
 * Mars Simulation Project
 * FuelPowerSource.java
 * @date 2024-08-03
 * @author Sebastien Venot
 */
package com.mars_sim.core.building.utility.power;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;

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
	
	/** The work time (millisol) required to toggle this power source on or off. */
	private static final double TOGGLE_RUNNING_WORK_TIME_REQUIRED = 2D;
	
//   Every mole of methane (16 g) releases 810 KJ of energy if burning with 2 moles of oxygen (64 g)
//	 
//	 CH4(g) + 2O2(g) --> CO2(g) + 2 H2O(g), deltaH = -890 kJ 
//	 
//	 CnH2n+2 + (3n + 1)O2 -> nCO2 + (n + 1)H2O + (6n + 2)e- 
//
//	 It produces 890kW at the consumption rate of 16 g/s
// or it produces 1kW at .018 g/s
//	
//	 Since each martian sol has 88775 earth seconds (=24*60*60 + 39*60 + 35.244),
//	
//	 Assume thermal efficiency at 41%,
//	 364.9 kW needs 16 g/s 
//	 1 kW_t <- 3.8926 g/millisol or 3.8926 kg/sol	 
//	 
//	 Assume thermal efficiency at 100%,
//	 1 kW_t <- 1.5960 g/millisol or kg/sol
//	 
//	 1kW needs 1.59795 kg/sol. 
//	 5kW needs 7.9897 kg/sol. 
//	 60kW needs 95.877 kg/sol.
//	
//	 SOFC uses methane with 1100 W-hr/kg, 
//	 This translate to 71.25 % efficiency
//	
//	 Use of heat will push it up to 85%
//	
//	 see http://www.nfcrc.uci.edu/3/FUEL_CELL_INFORMATION/FCexplained/FC_benefits.aspx
//	 or 90% see https://phys.org/news/2017-07-hydrocarbon-fuel-cells-high-efficiency.html 
	
	/** The consumption rate [g/millisol or kg/sol/kW] of methane if burning at 100% efficiency. */
	public static final double CONSUMPTION_RATE = 1.59795;
	/** The rated efficiency of converting to heat. */
	private static final double RATED_THERMAL_EFFICIENCY = .9;
	/** The rated efficiency of converting to electricity. */
	private static final double RATED_ELECTRIC_EFFICIENCY = .7125;

	private boolean toggleOn = false;
	
	/** The current thermal efficiency for this source. */
	public double thermalEfficiency = RATED_THERMAL_EFFICIENCY;
	/** The current electric efficiency for this source. */
	public double electricEfficiency = RATED_ELECTRIC_EFFICIENCY;
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
		this.toggleOn = toggle;
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
			return CONSUMPTION_RATE / 1000 / electricEfficiency ;
		else
			return CONSUMPTION_RATE / 1000 / thermalEfficiency;
	}
	
	public Settlement getSettlement() {
		return building.getSettlement();
	}
	
	public void toggleON() {
		toggleOn = true;
	}

	public void toggleOFF() {
		toggleOn = false;
	}

	public boolean isToggleON() {
		return toggleOn;
	}
	
	@Override
	public void setTime(double time) {
		// Note: Called by PowerGeneration::calculateGeneratedPower
		// Future: need to look for a better way of inserting the time param
		this.time = time;
	}
	
	/**
	 * Gets the amount resource used as fuel.
	 * 
	 * @return amount resource.
	 */
	 public int getFuelResourceID() {
		return ResourceUtil.METHANE_ID;
	}
		 
	/**
	 * Gets the rate the fuel is consumed.
	 * 
	 * @return rate (kg/sol/kW).
	 */
	 public double getFuelConsumptionRate() {
		 return CONSUMPTION_RATE;
	 }

	 /**
	  * Adds work time to toggling the power source on or off.
	  * Called by ToggleFuelPowerSource.
	  * 
	  * @param time the amount (millisols) of time to add.
	  * @return is the work done ?
	  */
	 public boolean addToggleWorkTime(double time) {
		 toggleRunningWorkTime += time;
		 if (toggleRunningWorkTime >= TOGGLE_RUNNING_WORK_TIME_REQUIRED) {
			 toggleRunningWorkTime = 0D;
			 toggleOn = !toggleOn;

			 String msgKey = (toggleOn ? "FuelPowerSource.log.turnedOn" : "FuelPowerSource.log.turnedOff");
			 logger.fine(Msg.getString(msgKey,getType().getName())); //$NON-NLS-1$
			 return true;
		 }
		 return false;
	 }

	 @Override
	 public double getAveragePower(Settlement settlement) {
		double fuelPower = getMaxPower();
		double fuelValue = settlement.getGoodsManager().getGoodValuePoint(ResourceUtil.METHANE_ID);
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
		if (isToggleON()) {
			double spentFuel = computeFuelConsumption(time, 100, true, false);
			return spentFuel / getMaxFuelPerMillisolPerkW(true) / time;
		}
		return 0D;
	 }
	
	 /**
	   * Measures or estimates the power produced by this power source.
	   * 
	   * @param percent The percentage of capacity of this power source
	   * @return power (kWe)
	   */
	 @Override
	 public double measurePower(double percent) {
		 if (time != 0D) {
			 double spentFuel = computeFuelConsumption(time, percent, true, true);
			 return spentFuel / getMaxFuelPerMillisolPerkW(true) / time;
		 }
		 return 0D;
	 }

	 /**
	  * Calculates the amount of fuel to be consumed.
	  * 
	  * @param time
	  * @param percent
	  * @param isElectric
	  * @param measureOnly. If true, then it won't deduct the fuel
	  * @return
	  */
	 private double computeFuelConsumption(double time, double percent, 
			boolean isElectric, boolean measureOnly) {
		double consumed = 0;

		// fuel [kg] = [kW] * [percent] * [kg/millisols/kW] * [millisols]
		double deltaFuel = getMaxPower() * percent / 100.0  * getMaxFuelPerMillisolPerkW(isElectric) * time;

		if (measureOnly) {
			return deltaFuel;
		}
		
		else {
			// Retrieve the fuel and oxidizer from the temporary tanks
			if (deltaFuel <= reserveFuel && deltaFuel * RATIO <= reserveOxidizer) {
				reserveFuel -= deltaFuel;
				reserveOxidizer -= deltaFuel * RATIO;
				
				consumed = deltaFuel;
			}
			else {
				double fuelStored = getSettlement().getSpecificAmountResourceStored(ResourceUtil.METHANE_ID);
				double o2Stored = getSettlement().getSpecificAmountResourceStored(ResourceUtil.OXYGEN_ID);
				
				double transferFuel = tankCap + deltaFuel - reserveFuel;
				
				if (transferFuel <= fuelStored && transferFuel * RATIO <= o2Stored) {
					
					reserveFuel = tankCap;
					reserveOxidizer = tankCap * RATIO;
					
					getSettlement().retrieveAmountResource(ResourceUtil.METHANE_ID, transferFuel);
					getSettlement().retrieveAmountResource(ResourceUtil.OXYGEN_ID, transferFuel * RATIO);
					
					consumed = deltaFuel;
				}
			}
		}

		return consumed;
	}
}

/*
 * Mars Simulation Project
 * FuelHeatSource.java
 * @date 2022-08-08
 * @author Manny Kung
 */
package com.mars_sim.core.building.utility.heating;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;

public class FuelHeatSource extends HeatSource {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(FuelHeatSource.class.getName());
	
	private static final double MAINTENANCE_FACTOR = .1;
	
	/** The ratio of fuel to oxidizer by mass. */
	private static final int RATIO = 4;
	
	/** The work time (millisol) required to toggle this heat source on or off. */
	public static final double TOGGLE_RUNNING_WORK_TIME_REQUIRED = 2D;
	/** The consumption rate [g/millisol or kg/sol] of methane if burning at 100% efficiency. */
	public static final double CONSUMPTION_RATE = 1.59795;
	/** The rated efficiency of converting to heat. */
	private static final double RATED_THERMAL_EFFICIENCY = .9;
	/** The rated efficiency of converting to electricity. */
	private static final double RATED_ELECTRIC_EFFICIENCY = .7125;
	
	/** The toggle for turning this source on or off. */
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
	/** The time span [in millisol] in releasing the heat. */
	private double time;
	/** The tank capacity for holding the fuel [in kg]. */
	private double tankCap;
	
	private Building building;

	/**
	 * Constructor.
	 * 
	 * @param maxHeat          the maximum power/heat (kW) of the heat source.
	 * @param toggle           if the heat source is toggled on or off.
	 * @param fuelType          the fuel type.
	 * @param consumptionSpeed the rate of fuel consumption (kg/sol).
	 */
	public FuelHeatSource(Building building, double maxHeat, boolean toggle, 
			String fuelType) {
		super(HeatSourceType.FUEL_HEATING, maxHeat);
		this.toggle = toggle;
		this.building = building;

		if (building.isInhabitable()) {
			// e.g. Methane Power Generator
			// Use maxHeat to determine tank size
			tankCap = .4 * maxHeat;
		}
		else {
			// this methane generator is co-located within its host building
			tankCap = .2 * maxHeat ;
		}
	}

//   Every mole of methane (16 g) releases 810 KJ of energy if burning with 2 moles of oxygen (64 g)
//	 
//	 CH4(g) + 2O2(g) --> CO2(g) + 2 H2O(g), deltaH = -890 kJ 
//	 
// 	 CnH2n+2 + (3n + 1)O2 -> nCO2 + (n + 1)H2O + (6n + 2)e- 
//
//	 It produces 890kW at the consumption rate of 16 g/s
//   or it produces 1kW at .018 g/s
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
// 	 Use of heat will push it up to 85%
//	
//	 see http://www.nfcrc.uci.edu/3/FUEL_CELL_INFORMATION/FCexplained/FC_benefits.aspx
//	 or 90% see https://phys.org/news/2017-07-hydrocarbon-fuel-cells-high-efficiency.html 

	/**
	 * Gets the fuel in kg/millisol/kW.
	 * 
	 * @param isElectric
	 * @return
	 */
	private double getMaxFuelPerMillisolPerkW(boolean isElectric) {
		if (isElectric)
			return CONSUMPTION_RATE / 1000 / electricEfficiency * RATED_ELECTRIC_EFFICIENCY;
		else
			return CONSUMPTION_RATE / 1000 / thermalEfficiency * RATED_THERMAL_EFFICIENCY;
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
		double deltaFuel = getMaxHeat() / percent * 100.0  
				* getMaxFuelPerMillisolPerkW(isElectric) * time;

		if (!onlyRequest) {
			
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
	 * @return rate (kg/sol or g/millisol).
	 */
	public double getFuelConsumptionRate() {
		return CONSUMPTION_RATE;
	}

	@Override
	public double getMaintenanceTime() {
		return getMaxHeat() * MAINTENANCE_FACTOR;
	}

	public double getThermalEfficiency() {
		return thermalEfficiency;
	}

	public void setThermalEfficiency(double value) {
		thermalEfficiency = value;
	}

	public double getElectricEfficiency() {
		return electricEfficiency;
	}
	
	public void setElectricEfficiency(double value) {
		electricEfficiency = value;
	}
	
	/**
	 * Adds work time to toggling the heat source on or off.
	 * 
	 * @param time the amount (millisols) of time to add.
	 */
	public void addToggleWorkTime(double time) {
		toggleRunningWorkTime += time;
		if (toggleRunningWorkTime >= TOGGLE_RUNNING_WORK_TIME_REQUIRED) {
			toggleRunningWorkTime = toggleRunningWorkTime - TOGGLE_RUNNING_WORK_TIME_REQUIRED;
			toggle = !toggle;
			if (toggle)
				logger.info(building, Msg.getString("FuelHeatSource.log.turnedOn", getType().getName())); //$NON-NLS-1$
			else
				logger.info(building, Msg.getString("FuelHeatSource.log.turnedOff", getType().getName())); //$NON-NLS-1$
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

	public void setTime(double time) {
		this.time = time;
	}

	@Override
	public void setPercentElectricity(double percentage) {
		super.setPercentElectricity(percentage);
		toggle = (percentage != 100.0);
	}
	
	public Settlement getSettlement() {
		return building.getSettlement();
	}
	
	@Override
	public double getCurrentHeat() {
		if (toggle) {
			double spentFuel = computeFuelConsumption(time, getPercentHeat(), false, false);
			return spentFuel / getMaxFuelPerMillisolPerkW(false) / time;
		}
		return 0;
	}

	@Override
	public double getCurrentPower() {
		if (toggle) {
			double spentFuel = computeFuelConsumption(time, getPercentElectricity(), true, false);
			return spentFuel / getMaxFuelPerMillisolPerkW(true) / time;
		}
		return 0;
	}
	
	/**
	 * Requests an estimate of the heat produced by this heat source.
	 * 
	 * @param percent The percentage of capacity of this heat source
	 * @return Heat (kWt)
	 */
	@Override
	public double requestHeat(double percent) {
		double spentFuel = computeFuelConsumption(time, percent, false, true);
		return spentFuel / getMaxFuelPerMillisolPerkW(false) / time;
	}
}

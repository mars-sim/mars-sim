/*
 * Mars Simulation Project
 * FuelHeatSource.java
 * @date 2022-08-08
 * @author Manny Kung
 */
package com.mars_sim.core.structure.building.function;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.tools.Msg;

public class FuelHeatSource extends HeatSource {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(FuelHeatSource.class.getName());
	
	private static final double MAINTENANCE_FACTOR = .1;
	
	/** The ratio of fuel to oxidizer by mass. */
	private static final int RATIO = 4;

	private static final int OXYGEN_ID = ResourceUtil.oxygenID;
	private static final int METHANE_ID = ResourceUtil.methaneID;
	
	/** The work time (millisol) required to toggle this heat source on or off. */
	public static final double TOGGLE_RUNNING_WORK_TIME_REQUIRED = 2D;

	private boolean toggle = false;
	/** The thermal efficiency for this source. */
	public double thermalEfficiency = .9;
	/** The fuel consumption rate [kg/sol]. */
	private double rate;
	/** The fuel consumption rate [kg/millisol]. */
	private double mRate;
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
	public FuelHeatSource(Building building, double maxHeat, boolean toggle, String fuelType, double consumptionSpeed) {
		super(HeatSourceType.FUEL_HEATING, maxHeat);
		this.rate = consumptionSpeed;
		this.toggle = toggle;
		this.building = building;

		if (building.isInhabitable()) {
			// e.g. Methane Power Generator
			// Use the following two params to determine tank size
			tankCap = .5 * (consumptionSpeed + maxHeat);
		}
		else {
			tankCap = maxHeat / 2;
		}
		
		mRate = rate / 1000D;
	}

//     Note : every mole of methane (16 g) releases 810 KJ of energy if burning with 2 moles of oxygen (64 g)
//	 CH4(g) + 2O2(g) --> CO2(g) + 2 H2O(g), deltaH = -890 kJ 
//	 
// 	 CnH2n+2 + (3n + 1)O2 -> nCO2 + (n + 1)H2O + (6n + 2)e- 
//
//	 Assume thermal efficiency at 41%,
//	 364.9 kW needs 16 g/s 
//	 1 kW_t <- 3.8926 g/millisol or 3.8926 kg/sol	 
//	 
//	 Assume thermal efficiency at 100%,
//	 1 kW_t <- 1.5960 g/millisol or kg/sol
//	 
//	 SOFC uses methane with 1100 W-hr/kg, 
//	 This translate to 71.25 % efficiency
//	
// 	 Use of heat will push it up to 85%
//	 see http://www.nfcrc.uci.edu/3/FUEL_CELL_INFORMATION/FCexplained/FC_benefits.aspx
//	 
//	 or 90% see https://phys.org/news/2017-07-hydrocarbon-fuel-cells-high-efficiency.html 

	private double getMaxFuelPerMillisolPerkW() {
		return 1.5960 / thermalEfficiency / 1000;
	}
	
	/**
	 * Consumes the fuel.
	 * 
	 * @param time
	 * @param settlement
	 * @return
	 */
	private double consumeFuel(double time, Settlement settlement) {
		double consumed = 0;
		
		// Use tankCap and floor area as factors to limit fuel
		double fuelkg = tankCap * time * mRate;
		double deltaFuel = fuelkg * getPercentagePower() / 100.0;
		double maxFuel = getMaxHeat() * getMaxFuelPerMillisolPerkW();

		if (deltaFuel > maxFuel) {
			deltaFuel = maxFuel;
		}
		
		if (deltaFuel <= reserveFuel && deltaFuel * RATIO <= reserveOxidizer) {
			reserveFuel -= deltaFuel;
			reserveOxidizer -= deltaFuel * RATIO;
			
			consumed = deltaFuel;
		}
		else {
			double fuelStored = settlement.getAmountResourceStored(METHANE_ID);
			double o2Stored = settlement.getAmountResourceStored(OXYGEN_ID);
			
			double transferFuel = tankCap + deltaFuel - reserveFuel;
			
			if (transferFuel <= fuelStored && transferFuel * RATIO <= o2Stored) {
				
				reserveFuel = tankCap;
				reserveOxidizer = tankCap * RATIO;
				
				settlement.retrieveAmountResource(METHANE_ID, transferFuel);
				settlement.retrieveAmountResource(OXYGEN_ID, transferFuel * RATIO * 1.0);
				
				consumed = deltaFuel;
			}
			
			// Stop using it for heating
			
//			else {
//				// Note that 16 g of methane requires 64 g of oxygen, a 1-to-4 ratio
//				consumed = Math.min(deltaFuel, Math.min(fuelStored, o2Stored / RATIO));
//
//				settlement.retrieveAmountResource(METHANE_ID, consumed);
//				settlement.retrieveAmountResource(OXYGEN_ID, RATIO * consumed);
//			}
		}

//		logger.info(building, 20_000, "deltaFuel: " + Math.round(deltaFuel*1000.0)/1000.0 
//				+ "  maxFuel: " + Math.round(maxFuel*1000.0)/1000.0);
		
		return consumed;
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
		return rate;
	}

	@Override
	public double getCurrentHeat(Building building) {

		if (toggle) {
			double spentFuel = consumeFuel(time, building.getSettlement());
//			logger.info(building, 20_000, "spent fuel: " + spentFuel + "  kW: " + spentFuel / getMaxFuelPerMillisolPerkW());
			return spentFuel / getMaxFuelPerMillisolPerkW();
		}

		return 0;
	}

	@Override
	public double getAverageHeat(Settlement settlement) {
		double fuelHeat = getMaxHeat();
		double fuelValue = settlement.getGoodsManager().getGoodValuePoint(METHANE_ID);
		fuelValue *= getFuelConsumptionRate() / 1000D * time;
		fuelHeat -= fuelValue;
		if (fuelHeat < 0D) fuelHeat = 0D;
		return fuelHeat;
	}

	@Override
	public double getMaintenanceTime() {
		return getMaxHeat() * MAINTENANCE_FACTOR;
	}

	@Override
	public double getEfficiency() {
		return thermalEfficiency;
	}
	
	@Override
	public void setEfficiency(double value) {
		thermalEfficiency = value;
	}

	@Override
	public double getCurrentPower(Building building) {
		return getCurrentHeat(building);
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
				logger.info(building.getNickName() + "- " + Msg.getString("FuelHeatSource.log.turnedOn", getType().getName())); //$NON-NLS-1$
			else
				logger.info(building.getNickName() + "- " + Msg.getString("FuelHeatSource.log.turnedOff", getType().getName())); //$NON-NLS-1$
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

	@Override
	public void setTime(double time) {
		this.time = time;
	}

	@Override
	public void setPercentagePower(double percentage) {
		super.setPercentagePower(percentage);
		toggle = (percentage != 0.0);
	}
}

/*
 * Mars Simulation Project
 * FuelHeatSource.java
 * @date 2022-08-08
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function;

import java.util.logging.Logger;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

public class FuelHeatSource extends HeatSource {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static final Logger logger = Logger.getLogger(FuelHeatSource.class.getName());
	
	/** The ratio of fuel to oxidizer by mass. */
	private static final int RATIO = 4;
	/** The size of the fuel reserve tank. */
	private static final int STANDARD_RESERVE = 30;
	private static final int OXYGEN_ID = ResourceUtil.oxygenID;
	private static final int METHANE_ID = ResourceUtil.methaneID;
	
	/** The work time (millisol) required to toggle this heat source on or off. */
	public static final double TOGGLE_RUNNING_WORK_TIME_REQUIRED = 5D;

	private boolean toggle = false;
	
	public double thermalEfficiency = .9;
	/** The fuel consumption rate [kg/sol]. */
	private double rate;

	private double toggleRunningWorkTime;

	/** The amount of reserved fuel. */
	private double reserveFuel;
	/** The amount of reserved oxidizer. */
	private double reserveOxidizer;

	private double time;

	private double modRate;
	
	private Building building;

	/**
	 * Constructor.
	 * 
	 * @param maxHeat          the maximum power/heat (kW) of the heat source.
	 * @param toggle           if the heat source is toggled on or off.
	 * @param fuelType          the fuel type.
	 * @param consumptionSpeed the rate of fuel consumption (kg/Sol).
	 */
	public FuelHeatSource(Building building, double maxHeat, boolean toggle, String fuelType, double consumptionSpeed) {
		super(HeatSourceType.FUEL_HEATING, maxHeat);
		this.rate = consumptionSpeed;
		this.toggle = toggle;
		this.building = building;

		modRate = rate / 1000D;
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

	private double consumeFuel(double time, Settlement settlement) {
		double consumed = 0;
		
		double deltaFuel = (getPercentagePower() / 100.0) * time * modRate;
		
		if (deltaFuel <= reserveFuel && deltaFuel * RATIO <= reserveOxidizer) {
			reserveFuel -= deltaFuel;
			reserveOxidizer -= deltaFuel * RATIO;
			
			consumed = deltaFuel;
		}
		else {
			double fuelStored = settlement.getAmountResourceStored(METHANE_ID);
			double o2Stored = settlement.getAmountResourceStored(OXYGEN_ID);
			
			if (STANDARD_RESERVE <= fuelStored && STANDARD_RESERVE * RATIO <= o2Stored) {
				reserveFuel += STANDARD_RESERVE;
				reserveOxidizer += STANDARD_RESERVE * RATIO;
				settlement.retrieveAmountResource(METHANE_ID, STANDARD_RESERVE);
				settlement.retrieveAmountResource(OXYGEN_ID, STANDARD_RESERVE * RATIO * 1.0);
				
				reserveFuel -= deltaFuel;
				reserveOxidizer -= deltaFuel * RATIO;
				
				consumed = deltaFuel;
			}
			
			else {
				// Note that 16 g of methane requires 64 g of oxygen, a 1-to-4 ratio
				consumed = Math.min(deltaFuel, Math.min(fuelStored, o2Stored / RATIO));

				settlement.retrieveAmountResource(METHANE_ID, consumed);
				settlement.retrieveAmountResource(OXYGEN_ID, RATIO * consumed);
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
		return METHANE_ID;
	}

	/**
	 * Gets the rate the fuel is consumed.
	 * 
	 * @return rate (kg/Sol).
	 */
	public double getFuelConsumptionRate() {
		return rate;
	}

	@Override
	public double getCurrentHeat(Building building) {

		if (toggle) {
			double spentFuel = consumeFuel(time, building.getSettlement());
			return getMaxHeat() * spentFuel * thermalEfficiency;
		}

		return 0;
	}

	@Override
	public double getAverageHeat(Settlement settlement) {
		double fuelHeat = getMaxHeat();
		double fuelValue = settlement.getGoodsManager().getGoodValuePoint(METHANE_ID);
		fuelValue *= getFuelConsumptionRate();
		fuelHeat -= fuelValue;
		if (fuelHeat < 0D)
			fuelHeat = 0D;
		return fuelHeat;
	}

	@Override
	public double getMaintenanceTime() {
		return getMaxHeat() * 2D;
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

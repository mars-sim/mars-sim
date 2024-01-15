/*
 * Mars Simulation Project
 * FuelPowerSource.java
 * @date 2023-05-31
 * @author Sebastien Venot
 */
package com.mars_sim.core.structure.building.function;

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

	private boolean toggle = false;
	/** The electrical efficiency for this source. */
	private double electricalEfficiency = .7125;
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
	/** The time span [in millisol] in releasing the power. */
	private double time;
	/** The tank capacity for holding the fuel [in kg]. */
	private double tankCap;
	
	private Building building;
	
	/**
	 * Constructor.
	 * 
	 * @param maxPower the maximum power (kW) of the power source.
	 * @param toggle if the power source is toggled on or off.
	 * @param fuelType the fuel type.
	 * @param consumptionSpeed the rate of fuel consumption (kg/sol).
	 */
	public FuelPowerSource(Building building, double maxPower, boolean toggle, String fuelType,
			double consumptionSpeed) {
		super(PowerSourceType.FUEL_POWER, maxPower);
		this.rate = consumptionSpeed;
		this.toggle = toggle;
		this.building = building;
		
		if (building.isInhabitable()) {
			// e.g. Methane Power Generator
			// Use the following two params to determine tank size
			tankCap = .5 * (consumptionSpeed + maxPower);
		}
		else {
			tankCap = maxPower / 2;
		}

		mRate = rate / 1000.0;
	}
	
//	 Note : every mole of methane (16 g) releases 810 KJ of energy if burning with 2 moles of oxygen (64 g)
//	 CH4(g) + 2O2(g) -> CO2(g) + 2 H2O(g), deltaH = -890 kJ 
//	 
//	 CnH2n+2 + (3n + 1)O2 -> nCO2 + (n + 1)H2O + (6n + 2)e- 

//	it produces 890kW at the consumption rate of 16 g/s
//  or it produces 1kW at .018 g/s
	
//	Since each martian sol has 88775 earth seconds (=24*60*60 + 39*60 + 35.244),
	
//	 Assume electrical efficiency at 100%,
//	 1 kW_t <- 1.5960 g/millisol or kg/sol
	
//	1kW needs 1.59795 kg/sol. 
//	5kW needs 7.9897 kg/sol. 
//	60kW needs 95.877 kg/sol.
	
//	 SOFC uses methane with 1100 W-hr/kg, 
//	 This translate to 71.25 % efficiency
		
	/**
	 * Gets the max fuel to be consumed in kg.
	 * 
	 * @return
	 */
	private double getMaxFuelPerMillisolPerkW() {
		return 1.5960 / electricalEfficiency / 1000;
	}
	
	/**
	 * Consumes the fuel.
	 * 
	 * @param time
	 * @param inv
	 * @return the amount of fuel consumed
	 */
	public double consumeFuel(double time, Settlement settlement) {
		double consumed = 0;

		// Use tankCap and floor area as factors to limit fuel
		double deltaFuel = building.getFloorArea() / 5 * time * mRate;
		double maxFuel = getMaxPower() * getMaxFuelPerMillisolPerkW();
	
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
	 
	@Override
	public double getCurrentPower(Building building) {
		
		if (toggle) {
			double spentFuel = consumeFuel(time, building.getSettlement());	
//			logger.info(building, 20_000, "spent fuel: " + spentFuel + "  kW: " + spentFuel / getMaxFuelPerMillisolPerkW());
			return spentFuel / getMaxFuelPerMillisolPerkW();
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

	 @Override
	 public double getMaintenanceTime() {
	    return getMaxPower() * MAINTENANCE_FACTOR;
	 }
}

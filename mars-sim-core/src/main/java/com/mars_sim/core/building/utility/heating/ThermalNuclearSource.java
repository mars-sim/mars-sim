/*
 * Mars Simulation Project
 * ThermalNuclearSource.java
 * @date 2022-07-31
 * @author Manny Kung
 */
package com.mars_sim.core.building.utility.heating;

import com.mars_sim.core.building.Building;

/**
 * This class accounts for the effect of temperature by nuclear reactor.
 */
public class ThermalNuclearSource extends HeatSource {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final double MAINTENANCE_FACTOR = 1.5D;
	/** The rated efficiency of converting to heat. */
	private static final double RATED_THERMAL_EFFICIENCY = .9;
	/** The rated efficiency of converting to electricity. */
	private static final double RATED_ELECTRIC_EFFICIENCY = .7;
	
	/** The efficiency of converting it to heat. */
	private double thermalEfficiency = .9;
	/** The efficiency of converting it to electricity. */
	private double electricEfficiency = .7;

	private Building building;
	
	/**
	 * Constructor.
	 * 
	 * @param maxHeat the maximum generated power.
	 */
	public ThermalNuclearSource(Building building, double maxHeat) {
		// Call HeatSource constructor.
		super(HeatSourceType.THERMAL_NUCLEAR, maxHeat);
		this.building = building;
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
	
	@Override
	public double getMaintenanceTime() {
	    return getMaxHeat() * MAINTENANCE_FACTOR;
	}
	
	/**
	 * Gets the current heat produced by this heat source.
	 * 
	 * @return heat [in kW]
	 */
	@Override
	public double getCurrentHeat() {
		return getMaxHeat() * getPercentHeat() / 100D 
				* thermalEfficiency / RATED_THERMAL_EFFICIENCY;
	}

	/**
	 * Gets the current power produced by this heat source.
	 * 
	 * @return power [in kW]
	 */
	@Override
	public double getCurrentPower() {
		return getMaxHeat() * getPercentElectricity() / 100D 
				* electricEfficiency / RATED_ELECTRIC_EFFICIENCY;
	}

	/**
	 * Requests an estimate of the heat produced by this heat source.
	 * 
	 * @param percent The percentage of capacity of this heat source
	 * @return Heat (kWt)
	 */
	@Override
	public double requestHeat(double percent) {
		return getMaxHeat() * percent / 100 
				* thermalEfficiency / RATED_THERMAL_EFFICIENCY;
	}
	
}

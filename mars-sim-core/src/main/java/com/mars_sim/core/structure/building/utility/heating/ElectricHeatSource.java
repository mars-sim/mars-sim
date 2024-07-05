/*
 * Mars Simulation Project
 * ElectricHeatSource.java
 * @date 2022-07-31
 * @author Manny Kung
 */
package com.mars_sim.core.structure.building.utility.heating;

import com.mars_sim.core.structure.building.Building;

/**
 * An electric heat source is a type of electric furnace.
 */
public class ElectricHeatSource extends HeatSource {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	//private static final Logger logger = Logger.getLogger(ElectricHeatSource.class.getName());
	/** The rated efficiency of converting to heat. */
	private static final double RATED_THERMAL_EFFICIENCY = 1;
	/** The rated efficiency of converting to electricity. */
	private static final double RATED_ELECTRIC_EFFICIENCY = 1;
	
	/** The efficiency of converting to heat. */
	private double thermalEfficiency = 1;
	/** The efficiency of converting to electricity. */
	private double electricEfficiency = 1;
	
	private Building building;

	public ElectricHeatSource(Building building, double maxHeat) {
		// Call HeatSource constructor.
		super(HeatSourceType.ELECTRIC_HEATING, maxHeat);
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
	    return getMaxHeat();
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
		return getCurrentHeat() * getPercentElectricity() / 100D 
				* electricEfficiency / RATED_ELECTRIC_EFFICIENCY;
	}

	@Override
	public double requestHeat(double percent) {
		return getMaxHeat() * percent / 100 
				* thermalEfficiency / RATED_THERMAL_EFFICIENCY;
	}
}

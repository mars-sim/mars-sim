/*
 * Mars Simulation Project
 * ThermalNuclearSource.java
 * @date 2022-07-31
 * @author Manny Kung
 */
package com.mars_sim.core.structure.building.function;

import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;

/**
 * This class accounts for the effect of temperature by nuclear reactor.
 */
public class ThermalNuclearSource extends HeatSource {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final double MAINTENANCE_FACTOR = 1.5D;
	
	private double thermalEfficiency = .9;

	/**
	 * Constructor.
	 * 
	 * @param maxHeat the maximum generated power.
	 */
	public ThermalNuclearSource(double maxHeat) {
		// Call HeatSource constructor.
		super(HeatSourceType.THERMAL_NUCLEAR, maxHeat);
	}

	public double getCurrentHeat(Building building) {
		return getMaxHeat() * getPercentagePower() / 100D * thermalEfficiency;
	}

	public double getCurrentPower(Building building) {
		return getMaxHeat() * getPercentagePower() / 100D;
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
	public double getAverageHeat(Settlement settlement) {
		return getMaxHeat() * getPercentagePower()/ 2D * thermalEfficiency;
	}

	@Override
	public double getMaintenanceTime() {
	    return getMaxHeat() * MAINTENANCE_FACTOR;
	}
}

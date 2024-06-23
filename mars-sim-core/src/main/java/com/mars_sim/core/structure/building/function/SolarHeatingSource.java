/*
 * Mars Simulation Project
 * SolarHeatSource.java
 * @date 2024-06-22
 * @author Manny Kung
 */
package com.mars_sim.core.structure.building.function;

import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;

/**
 * This class accounts for the effect of temperature via 
 * passive solar water heating or passive solar heat collector system.
 */
public class SolarHeatingSource extends HeatSource {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	// Tentatively set to 0.14% or (.0014) efficiency degradation per sol as reported by NASA MER
	public static final double DEGRADATION_RATE_PER_SOL = .0014;
	
	private double efficiencyHeat = .68;
	
	private double efficiencyElectric = .55;

	private Building building;
	
	/**
	 * Constructor.
	 * 
	 * @building the building source
	 * @param maxHeat the maximum generated power.
	 */
	public SolarHeatingSource(Building building, double maxHeat) {
		// Call HeatSource constructor.
		super(HeatSourceType.SOLAR_HEATING, maxHeat);
		this.building = building;
	}
	
	public double getCollected() {
		return surface.getSolarIrradiance(building.getCoordinates()) / 1000D;
	}

	public double getEfficiencyHeat() {
		return efficiencyHeat;
	}

	public double getEfficiencyElectric() {
		return efficiencyElectric;
	}

	public void setEfficiencyToHeat(double value) {
		efficiencyHeat = value;
	}

	public void setEfficiencyToElectricity(double value) {
		efficiencyElectric = value;
	}

	@Override
	public double getCurrentHeat(Building building) {
		double available = getCollected(); 
		double col = getMaxHeat() * getPercentagePower() / 100D;
        return Math.min(available, col);
    }

	@Override
	public double getCurrentPower(Building building) {
		// Future: How to switch from heating mode to electrical mode ?
        return 0;
    }
	
	@Override
	public double getAverageHeat(Settlement settlement) {
		return getMaxHeat() *.707;
	}

	@Override
	public double getMaintenanceTime() {
	    return getMaxHeat();
	}
	
	/**
	 * Reloads instances after loading from a saved sim.
	 * 
	 * @param {@link SurfaceFeatures}
	 */
	public static void initializeInstances(SurfaceFeatures s) {
		surface = s;
	}
}

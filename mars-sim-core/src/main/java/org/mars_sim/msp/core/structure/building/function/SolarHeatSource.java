/*
 * Mars Simulation Project
 * SolarHeatSource.java
 * @date 2022-06-24
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.environment.SurfaceFeatures;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * This class accounts for the effect of temperature via passive solar water heating or passive solar heat collector system.
 */
public class SolarHeatSource extends HeatSource {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	// Tentatively set to 0.14% or (.0014) efficiency degradation per sol as reported by NASA MER
	public static final double DEGRADATION_RATE_PER_SOL = .0014;
	
	private double efficiencySolar2Heat = .68;
	
	private double efficiencySolar2Electricity = .55;

	private Building building;
	
	/**
	 * Constructor.
	 * @param maxHeat the maximum generated power.
	 */
	public SolarHeatSource(Building building, double maxHeat) {
		// Call HeatSource constructor.
		super(HeatSourceType.SOLAR_HEATING, maxHeat);
		this.building = building;
	}
	
	public double getCollected() {
		return surface.getSolarIrradiance(building.getCoordinates()) / 1000D;
	}

	public double getEfficiencySolarHeat() {
		return efficiencySolar2Heat;
	}

	public double getEfficiencyElectricHeat() {
		return efficiencySolar2Electricity;
	}

	public void setEfficiencyToHeat(double value) {
		efficiencySolar2Heat = value;
	}

	public void setEfficiencyToElectricity(double value) {
		efficiencySolar2Electricity = value;
	}

	@Override
	public double getCurrentHeat(Building building) {
		double available = getCollected(); 
		double col = getMaxHeat() * getPercentagePower() / 100D;
        return Math.min(available, col);
    }

	@Override
	public double getCurrentPower(Building building) {
        return getCurrentHeat(building);
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
	 * Reloads instances after loading from a saved sim
	 * 
	 * @param {@link SurfaceFeatures}
	 */
	public static void initializeInstances(SurfaceFeatures s) {
		surface = s;
	}
}

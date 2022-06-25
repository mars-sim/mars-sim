/*
 * Mars Simulation Project
 * SolarHeatSource.java
 * @date 2022-06-24
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;

import org.mars_sim.msp.core.environment.SurfaceFeatures;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * This class accounts for the effect of temperature via passive solar water heating or passive solar heat collector system.
 */
public class SolarHeatSource
extends HeatSource
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	// Tentatively set to 0.14% or (.0014) efficiency degradation per sol as reported by NASA MER
	public static final double DEGRADATION_RATE_PER_SOL = .0014;
	
	public static final double MEAN_SOLAR_IRRADIANCE = SurfaceFeatures.MEAN_SOLAR_IRRADIANCE;
	/**
	 * The dust deposition rates is proportional to the dust loading. Here we use MER program's extended the analysis 
	 * to account for variations in the atmospheric columnar dust amount.
	 */
//	private double dust_deposition_rate = 0;
	
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
	
	/***
	 * Computes and updates the dust deposition rate for a settlement
	 * @param the rate
	 */
//	public void computeDustDeposition(Settlement settlement) {
//
//		if (location == null)
//			location = settlement.getCoordinates();
//
//		double tau = surface.getOpticalDepth(location);		
//	
//		// e.g. The Material Adherence Experiement (MAE) on Pathfinder indicate steady dust accumulation on the Martian 
//		// surface at a rate of ~ 0.28% of the surface area per day (Landis and Jenkins, 1999)
//		dust_deposition_rate = .0018 * tau /.5;
//		
//		// during relatively periods of clear sky, typical values for optical depth were between 0.2 and 0.5
//	}
	
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
		double available = getCollected(); // * efficiency_solar_to_heat;
		double col = getMaxHeat() * getPercentagePower() / 100D;// * efficiency_solar_to_heat)/100D;
        return Math.min(available, col);

    }

	@Override
	public double getCurrentPower(Building building) {
		double available = getCollected();// * efficiency_solar_to_electricity;
		double col = getMaxHeat() * getPercentagePower() / 100D ; //* efficiency_solar_to_electricity)/100D;	
        return Math.min(available, col);

    }
	
	@Override
	public double getAverageHeat(Settlement settlement) {
		return getMaxHeat() *.707;
	}

	@Override
	public double getMaintenanceTime() {
	    return getMaxHeat();
	}

	@Override
	public double getEfficiency() {
		return getEfficiencySolarHeat();
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

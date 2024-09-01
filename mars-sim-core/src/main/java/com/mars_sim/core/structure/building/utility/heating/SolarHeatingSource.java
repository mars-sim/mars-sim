/*
 * Mars Simulation Project
 * SolarHeatSource.java
 * @date 2024-07-03
 * @author Manny Kung
 */
package com.mars_sim.core.structure.building.utility.heating;

import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.map.location.Coordinates;
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
	/** The rated efficiency of converting to heat. */
	private static final double RATED_THERMAL_EFFICIENCY = .68;
	/** The rated efficiency of converting to electricity. */
	private static final double RATED_ELECTRIC_EFFICIENCY = .55;
	
	/** The efficiency of converting it to heat. */
	private double thermalEfficiency = RATED_THERMAL_EFFICIENCY;
	/** The efficiency of converting it to electricity. */
	private double electricEfficiency = RATED_ELECTRIC_EFFICIENCY;

	private Building building;
	
	private transient Coordinates location;
	
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

	public double getSunlight() {
		if (location == null) {
			location = building.getCoordinates();
		}
		
		return surface.getSolarIrradiance(location) ;
	}
	
	@Override
	public double getCurrentHeat() {
		double fraction = getSunlight() / SurfaceFeatures.MEAN_SOLAR_IRRADIANCE; 
		if (fraction == 0)
			return 0;
		double available = thermalEfficiency / RATED_THERMAL_EFFICIENCY * getMaxHeat() * getPercentHeat() / 100D;
        return fraction * available;
    }
	
	@Override
	public double getCurrentPower() {
		double fraction = getSunlight() / SurfaceFeatures.MEAN_SOLAR_IRRADIANCE; 
		if (fraction == 0)
			return 0;
		double available = electricEfficiency / RATED_ELECTRIC_EFFICIENCY * getMaxHeat() * getPercentElectricity() / 100D;
        return fraction * available;
    }
	
	/**
	 * Requests an estimate of the heat produced by this heat source.
	 * 
	 * @param percent The percentage of capacity of this heat source
	 * @return Heat (kWt)
	 */
	@Override
	public double requestHeat(double percent) {
		double fraction = getSunlight() / SurfaceFeatures.MEAN_SOLAR_IRRADIANCE; 
		if (fraction == 0)
			return 0;
		double available = thermalEfficiency / RATED_THERMAL_EFFICIENCY * getMaxHeat() * percent / 100D;
        return fraction * available;
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

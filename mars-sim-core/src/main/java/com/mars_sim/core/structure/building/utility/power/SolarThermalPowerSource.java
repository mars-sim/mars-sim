/*
 * Mars Simulation Project
 * SolarThermalPowerSource.java
 * @date 2024-08-03
 * @author Scott Davis
 */
package com.mars_sim.core.structure.building.utility.power;


import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;

/**
 * A solar thermal power source.
 */
public class SolarThermalPowerSource extends PowerSource {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final double MAINTENANCE_FACTOR = 2.5D;
	
	private static final double DEFAULT_SOLAR_THERMAL_EFFICIENCY = .70;
	
	private Building building;
	
	/**
	 * Constructor.
	 * 
	 * @param maxPower the maximum generated power.
	 */
	public SolarThermalPowerSource(Building building, double maxPower) {
		// Call PowerSource constructor.
		super(PowerSourceType.SOLAR_THERMAL, maxPower);
		
		this.building = building;
	}

	public static double getEfficiency() {
		return DEFAULT_SOLAR_THERMAL_EFFICIENCY;
	}

	@Override
	public double getCurrentPower(Building building) {

		double I = surface.getSolarIrradiance(building.getCoordinates());

		if (I <= 0)
			return 0;
		
		return I / SurfaceFeatures.MEAN_SOLAR_IRRADIANCE * getMaxPower();		
	}

	@Override
	public double getAveragePower(Settlement settlement) {
		return getMaxPower();
	}

	@Override
	public double getMaintenanceTime() {
	    return getMaxPower() * MAINTENANCE_FACTOR;
	}
	
	 /**
	   * Requests an estimate of the power produced by this power source.
	   * 
	   * @param percent The percentage of capacity of this power source
	   * @return power (kWe)
	   */
	 @Override
	 public double requestPower(double percent) {
		 return getCurrentPower(building);
	 }
}

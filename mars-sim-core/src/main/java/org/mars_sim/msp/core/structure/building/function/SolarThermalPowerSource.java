/*
 * Mars Simulation Project
 * SolarThermalPowerSource.java
 * @date 2022-06-24
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;


import org.mars_sim.msp.core.environment.SurfaceFeatures;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * A solar thermal power source.
 */
public class SolarThermalPowerSource extends PowerSource {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final double MAINTENANCE_FACTOR = 2.5D;
	
	private static double efficiencySolarThermal = .70;
	
//	public static double ARRAY_AREA = 100D;		// in square feet
		
	/**
	 * Constructor.
	 * @param maxPower the maximum generated power.
	 */
	public SolarThermalPowerSource(double maxPower) {
		// Call PowerSource constructor.
		super(PowerSourceType.SOLAR_THERMAL, maxPower);
	}

	public static double getEfficiency() {
		return efficiencySolarThermal;
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
		return getMaxPower() * 0.707;
	}

	@Override
	public double getMaintenanceTime() {
	    return getMaxPower() * MAINTENANCE_FACTOR;
	}

	@Override
	public void removeFromSettlement() {
		// May model how to salvage the parts from this power source
	}

	@Override
	public void setTime(double time) {
		// May use this method to control turning on and off this power source
	}
	
	@Override
	public void destroy() {
		super.destroy();
	}
}

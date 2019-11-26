/**
 * Mars Simulation Project
 * SolarThermalPowerSource.java
 * @version 3.1.0 2017-09-06
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * A solar thermal power source.
 */
public class SolarThermalPowerSource
extends PowerSource
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final double MAINTENANCE_FACTOR = 2.5D;
	
	private static double efficiency_solar_thermal = .70;
	
	public static double ARRAY_AREA = 100D;		// in square feet
	
	private Coordinates location;
	
	/**
	 * Constructor.
	 * @param maxPower the maximum generated power.
	 */
	public SolarThermalPowerSource(double maxPower) {
		// Call PowerSource constructor.
		super(PowerSourceType.SOLAR_THERMAL, maxPower);
	}

	public static double getEfficiency() {
		return efficiency_solar_thermal;
	}

	@Override
	public double getCurrentPower(Building building) {
//		BuildingManager manager = building.getBuildingManager();
		if (location == null)
			location = building.getBuildingManager().getSettlement().getCoordinates();

		double sunlight = surface.getSolarIrradiance(location) * efficiency_solar_thermal / 1000D * ARRAY_AREA;
		double max = getMaxPower(); 
		if (sunlight <= max)
			return sunlight;
		else
			return max;
		
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
		// TODO Auto-generated method stub

	}

	@Override
	public void setTime(double time) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void destroy() {
		super.destroy();
		location = null;

	}
}
/**
 * Mars Simulation Project
 * SolarThermalPowerSource.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;

/**
 * A solar thermal power source.
 */
public class SolarThermalPowerSource
extends PowerSource
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * @param maxPower the maximum generated power.
	 */
	public SolarThermalPowerSource(double maxPower) {
		// Call PowerSource constructor.
		super(PowerSourceType.SOLAR_THERMAL, maxPower);
	}

	@Override
	public double getCurrentPower(Building building) {
		BuildingManager manager = building.getBuildingManager();
		Coordinates location = manager.getSettlement().getCoordinates();
		SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
		double sunlight = surface.getSurfaceSunlight(location);

		// Solar thermal mirror only works in direct sunlight.
		if (sunlight == 1D) return getMaxPower();
		else return 0D;
	}

	@Override
	public double getAveragePower(Settlement settlement) {
		return getMaxPower() / 2.5D;
	}
}
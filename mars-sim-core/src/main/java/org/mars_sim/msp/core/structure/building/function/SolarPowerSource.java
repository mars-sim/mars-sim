/**
 * Mars Simulation Project
 * SolarPowerSource.java
 * @version 3.07 2014-12-06
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
 * A power source that gives a supply of power proportional 
 * to the level of sunlight it receives.
 */
public class SolarPowerSource
extends PowerSource
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * @param maxPower the maximum generated power (kW).
	 */
	public SolarPowerSource(double maxPower) {
		// Call PowerSource constructor.
		super(PowerSourceType.SOLAR, maxPower);
	}

	/**
	 * Gets the current power produced by the power source.
	 * @param building the building this power source is for.
	 * @return power (kW)
	 */
	public double getCurrentPower(Building building) {
		BuildingManager manager = building.getBuildingManager();
		Coordinates location = manager.getSettlement().getCoordinates();
		SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
		double sunlight = surface.getSurfaceSunlight(location);
		return sunlight * getMaxPower();
	}

	@Override
	public double getAveragePower(Settlement settlement) {
		return getMaxPower() / 2D;
	}
	
	@Override
	public double getMaintenanceTime() {
	    return getMaxPower() * 1D;
	}
}
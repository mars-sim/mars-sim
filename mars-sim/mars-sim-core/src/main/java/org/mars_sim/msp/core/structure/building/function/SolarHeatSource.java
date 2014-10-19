/**
 * Mars Simulation Project
 * SolarHeatSource.java
 * @version 3.07 2014-10-17
 * @author Manny Kung
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
 * This class accounts for the effect of temperature by direct solar heating .
 */
public class SolarHeatSource
extends HeatSource
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * @param maxHeat the maximum generated power.
	 */
	public SolarHeatSource(double maxHeat) {
		// Call HeatSource constructor.
		super(HeatSourceType.SOLAR_HEAT, maxHeat);
	}

	@Override
	public double getCurrentHeat(Building building) {
		BuildingManager manager = building.getBuildingManager();
		Coordinates location = manager.getSettlement().getCoordinates();
		SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
		double sunlight = surface.getSurfaceSunlight(location);

		// Solar thermal mirror only works in direct sunlight.
		if (sunlight == 1D) return getMaxHeat();
		else return 0D;
	}

	@Override
	public double getAverageHeat(Settlement settlement) {
		return getMaxHeat() / 2D;
	}
	
	@Override
	public double getMaintenanceTime() {
	    return getMaxHeat() * 1D;
	}
}
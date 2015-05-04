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
 * This class accounts for the effect of temperature by passive solar water heating .
 */
public class SolarHeatSource
extends HeatSource
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static double efficiency_solar_heat = .68;
	private static double efficiency_solar_heat_to_electricity = .68;

	private double area = 10;

	private Coordinates location ;
	private SurfaceFeatures surface ;
	private BuildingManager manager;
	/**
	 * Constructor.
	 * @param maxHeat the maximum generated power.
	 */
	public SolarHeatSource(double maxHeat) {
		// Call HeatSource constructor.
		super(HeatSourceType.SOLAR_HEAT, maxHeat);
	}

	public double getCurrentHeat(Building building) {
		if (manager == null)
			manager = building.getBuildingManager();
		//TODO: calculate the amount of heat produced by passive solar heat
		if (location == null)
			location = manager.getSettlement().getCoordinates();
		if (surface == null)
			surface = Simulation.instance().getMars().getSurfaceFeatures();
		//double sunlight = surface.getSurfaceSunlight(location);
		double actual = surface.getSolarIrradiance(location) * area ;
		double effective = getMaxHeat() * efficiency_solar_heat;
		double result = 0;
		if (actual < effective)
			result = actual;
		else
			result = effective;
		//TODO: need to account for other system specs
		return result ;
	}

	public double getCurrentPower(Building building) {
		if (manager == null)
			manager = building.getBuildingManager();
		//TODO: calculate the amount of heat produced by passive solar heat
		if (location == null)
			location = manager.getSettlement().getCoordinates();
		if (surface == null)
			surface = Simulation.instance().getMars().getSurfaceFeatures();
		//double sunlight = surface.getSurfaceSunlight(location);
		double actual = surface.getSolarIrradiance(location) * area ;
		double effective = getMaxHeat() * efficiency_solar_heat_to_electricity ;
		double result = 0;
		if (actual < effective)
			result = actual;
		else
			result = effective;
		//TODO: need to account for other system specs
		return result ;
	}

	public static double getEfficiency() {
		return efficiency_solar_heat;
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
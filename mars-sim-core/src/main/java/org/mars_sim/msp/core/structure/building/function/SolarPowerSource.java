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

	// Tentatively set to 0.14% per sol efficiency degradation as reported by NASA MER
	public static double DEGRADATION_RATE_PER_SOL = .0014;

	private double area;

	private double efficiency_solar_panel = .35;

	private Coordinates location ;
	private SurfaceFeatures surface ;
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
		if (location == null)
			location = manager.getSettlement().getCoordinates();
		if (surface == null)
			surface = Simulation.instance().getMars().getSurfaceFeatures();
		//double sunlight = surface.getSolarIrradiance(location) * efficiency_solar_panel / 600D ; // tentatively normalized to 600 W
		//return sunlight * getMaxPower();
		double actual = surface.getSolarIrradiance(location) * area ;
		double effective = getMaxPower() * efficiency_solar_panel;
		double result = 0;
		if (actual < effective)
			result = actual;
		else
			result = effective;
		//TODO: need to account for other system specs
		return result ;
	}

	@Override
	public double getAveragePower(Settlement settlement) {
		return getMaxPower() / 2D;
	}

	@Override
	public double getMaintenanceTime() {
	    return getMaxPower() * 1D;
	}

	public void setEfficiency(double value) {
		 efficiency_solar_panel = value;
	}

	public double getEfficiency() {
		return efficiency_solar_panel;
	}
}
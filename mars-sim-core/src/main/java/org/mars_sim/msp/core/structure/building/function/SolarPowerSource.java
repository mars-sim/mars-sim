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

	private static final double MAINTENANCE_FACTOR = 2.5D;
	
	/** In terms of solar cell degradation, NASA MER has an observable degradation rate of 0.14% per sol
	 *  on the solar cell (if starting from 100%).
	 	Here we tentatively set to 0.04% per sol instead of 0.14%, since that in 10 earth years,
	 	the efficiency will	drop down to 23.21% of the initial 100%
	 	100*(1-.04/100)^(365*10) = 23.21% */
	public static double DEGRADATION_RATE_PER_SOL = .0004; // assuming it is a constant through its mission

	private double area;

	/** The solar Panel is made of triple-junction solar cells with theoretical max eff of 68% */
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
	    return getMaxPower() * MAINTENANCE_FACTOR;
	}

	public void setEfficiency(double value) {
		 efficiency_solar_panel = value;
	}

	public double getEfficiency() {
		return efficiency_solar_panel;
	}

	@Override
	public void removeFromSettlement() {
		// TODO Auto-generated method stub

	}
}
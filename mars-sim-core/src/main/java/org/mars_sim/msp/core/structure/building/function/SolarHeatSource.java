/**
 * Mars Simulation Project
 * SolarHeatSource.java
 * @version 3.1.0 2017-08-14
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
	// Tentatively set to 0.14% or (.0014) efficiency degradation per sol as reported by NASA MER
	public static double DEGRADATION_RATE_PER_SOL = .0014;

	private double efficiency_solar_heat = .58;
	private double efficiency_solar_heat_to_electricity = .58;

	private double area = 5 ;

	private Coordinates location ;
	private SurfaceFeatures surface ;
	private BuildingManager manager;
	
	private double actual = -1;
	/**
	 * Constructor.
	 * @param maxHeat the maximum generated power.
	 */
	public SolarHeatSource(double maxHeat) {
		// Call HeatSource constructor.
		super(HeatSourceType.SOLAR_HEATING, maxHeat);
	}

	public double getCurrentHeat(Building building) {
		if (actual == -1) 
			computeActual(building);
		
		double effective = getMaxHeat() * efficiency_solar_heat;
		double result = 0;
		if (actual < effective)
			result = actual;
		else
			result = effective;
		//TODO: need to account for other system specs
		return result ;
	}

	public void computeActual(Building building) {
		if (manager == null)
			manager = building.getBuildingManager();
		//TODO: calculate the amount of heat produced by passive solar heat
		if (location == null)
			location = manager.getSettlement().getCoordinates();
		if (surface == null)
			surface = Simulation.instance().getMars().getSurfaceFeatures();
		//double sunlight = surface.getSurfaceSunlight(location);
		actual = surface.getSolarIrradiance(location) * area ;
	}
	
	public double getCurrentPower(Building building) {
		if (actual == -1) 
			computeActual(building);
		
		double effective = getMaxHeat() * efficiency_solar_heat_to_electricity ;
		double result = 0;
		if (actual < effective)
			result = actual;
		else
			result = effective;
		//TODO: need to account for other system specs
		return result ;
	}

	public double getEfficiency() {
		return efficiency_solar_heat;
	}

	public double getEfficiencyElectric() {
		return efficiency_solar_heat_to_electricity;
	}

	public void setEfficiency(double value) {
		efficiency_solar_heat = value;
	}

	public void setEfficiencyElectric(double value) {
		efficiency_solar_heat_to_electricity = value;
	}


	@Override
	public double getAverageHeat(Settlement settlement) {
		// NOTE: why divide by 2 ?
		return getMaxHeat() / 2D;
	}

	@Override
	public double getMaintenanceTime() {
	    return getMaxHeat() * 1D;
	}
}
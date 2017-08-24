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
 * This class accounts for the effect of temperature via passive solar water heating or passive solar heat collector system.
 */
public class SolarHeatSource
extends HeatSource
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	// Tentatively set to 0.14% or (.0014) efficiency degradation per sol as reported by NASA MER
	public static double DEGRADATION_RATE_PER_SOL = .0014;

	private double efficiency_solar_to_heat = .58;
	private double efficiency_solar_to_electricity = .58;

	private SurfaceFeatures surface ;
	
	private double maxHeat;

	/**
	 * Constructor.
	 * @param maxHeat the maximum generated power.
	 */
	public SolarHeatSource(double maxHeat) {
		// Call HeatSource constructor.
		super(HeatSourceType.SOLAR_HEATING, maxHeat);
		this.maxHeat = maxHeat;
	}
	
	public double getCollected(Building building) {
		if (surface == null)
			surface = Simulation.instance().getMars().getSurfaceFeatures();
		return surface.getSolarIrradiance(building.getCoordinates()) / SurfaceFeatures.MEAN_SOLAR_IRRADIANCE * building.getFloorArea() / 1000D ;
	}

	public double getEfficiencySolarHeat() {
		return efficiency_solar_to_heat;
	}

	public double getEfficiencyElectricHeat() {
		return efficiency_solar_to_electricity;
	}

	public void setEfficiencyToHeat(double value) {
		efficiency_solar_to_heat = value;
	}

	public void setEfficiencyToElectricity(double value) {
		efficiency_solar_to_electricity = value;
	}

	@Override
	public double getCurrentHeat(Building building) {
		double collected = getCollected(building) * efficiency_solar_to_heat;
		return maxHeat * collected;
	}

	
	@Override
	public double getCurrentPower(Building building) {
		double collected = getCollected(building) * efficiency_solar_to_electricity;
		//System.out.println(building.getNickName() + "'s maxHeat is " + maxHeat + " collected is " + collected);
		return maxHeat * collected;
	}
	
	@Override
	public double getAverageHeat(Settlement settlement) {
		// NOTE: why divide by 2 ? why settlement ?		
		return getMaxHeat() / 2D;
	}

	@Override
	public double getMaintenanceTime() {
	    return getMaxHeat() * 1D;
	}

	@Override
	public double getEfficiency() {
		return getEfficiencySolarHeat();
	}
	
	 @Override
	 public void destroy() {
		 super.destroy();
		 surface = null;
	 }
	 
}

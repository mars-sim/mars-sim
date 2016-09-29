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
 * This class accounts for the effect of temperature by nuclear reactor.
 */
public class NuclearHeatSource
extends HeatSource
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	// Tentatively set to 0.01% or (.0001) efficiency degradation per sol as reported by NASA ...
	public static double DEGRADATION_RATE_PER_SOL = .0001;

	private double efficiency_heat = .90;
	private double efficiency_heat_to_electricity = .90;

	private double area = 5 ;

	private Coordinates location ;
	private SurfaceFeatures surface ;
	private BuildingManager manager;
	/**
	 * Constructor.
	 * @param maxHeat the maximum generated power.
	 */
	public NuclearHeatSource(double maxHeat) {
		// Call HeatSource constructor.
		super(HeatSourceType.SOLAR_HEATING, maxHeat);
	}

	public double getCurrentHeat(Building building) {
		return getMaxHeat() * efficiency_heat;
	}

	public double getCurrentPower(Building building) {
		return getMaxHeat() * efficiency_heat_to_electricity ;
	}

	public double getEfficiency() {
		return efficiency_heat;
	}

	public double getEfficiencyElectric() {
		return efficiency_heat_to_electricity;
	}

	public void setEfficiency(double value) {
		efficiency_heat = value;
	}

	public void setEfficiencyElectric(double value) {
		efficiency_heat_to_electricity = value;
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
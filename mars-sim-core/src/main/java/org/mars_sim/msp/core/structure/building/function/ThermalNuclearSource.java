/**
 * Mars Simulation Project
 * ThermalNuclearSource.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * This class accounts for the effect of temperature by nuclear reactor.
 */
public class ThermalNuclearSource
extends HeatSource
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private double efficiency_heat = .90;


	/**
	 * Constructor.
	 * @param maxHeat the maximum generated power.
	 */
	public ThermalNuclearSource(double maxHeat) {
		// Call HeatSource constructor.
		super(HeatSourceType.THERMAL_NUCLEAR, maxHeat);
	}

	public double getCurrentHeat(Building building) {
		return getMaxHeat() * getPercentagePower() / 100D * efficiency_heat;
	}

	public double getCurrentPower(Building building) {
		return getMaxHeat() * getPercentagePower() /100D ;
	}

	public double getEfficiency() {
		return efficiency_heat;
	}

	public void setEfficiency(double value) {
		efficiency_heat = value;
	}

	@Override
	public double getAverageHeat(Settlement settlement) {
		return getMaxHeat() / 2D;
	}

	@Override
	public double getMaintenanceTime() {
	    return getMaxHeat();
	}
}

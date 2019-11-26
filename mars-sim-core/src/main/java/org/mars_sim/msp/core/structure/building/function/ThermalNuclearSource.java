/**
 * Mars Simulation Project
 * ThermalNuclearSource.java
 * @version 3.1.0 2017-08-14
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
	// Tentatively set to 0.01% or (.0001) efficiency degradation per sol as reported by NASA ...
	//public static double DEGRADATION_RATE_PER_SOL = .0001;

	private double efficiency_heat = .90;

	private double factor = 1;
	
	//private double area = 5 ;

	/**
	 * Constructor.
	 * @param maxHeat the maximum generated power.
	 */
	public ThermalNuclearSource(double maxHeat) {
		// Call HeatSource constructor.
		super(HeatSourceType.THERMAL_NUCLEAR, maxHeat);
	}

	public double getCurrentHeat(Building building) {
		return getMaxHeat() * factor * efficiency_heat;
	}

	public double getCurrentPower(Building building) {
		return getMaxHeat() * factor * efficiency_heat ;
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
	    return getMaxHeat() * 1D;
	}
	
	@Override
	public void setTime(double time) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void switch2Half() {
		factor = 1/2D;
	}
	
	@Override
	public void switch2OneQuarter() {
		factor = 1/4D;
	}
	
	@Override
	public void switch2Full() {
		factor = 1D;
	}
	
	@Override
	public void switch2ThreeQuarters() {
		factor = .75;
	}
	
	@Override
	public void destroy() {
		super.destroy();
	}

}
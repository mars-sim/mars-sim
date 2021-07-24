/**
 * Mars Simulation Project
 * ElectricHeatSource.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * An electric heat source is a type of electric furnace.
 */
public class ElectricHeatSource
extends HeatSource
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	//private static final Logger logger = Logger.getLogger(ElectricHeatSource.class.getName());

	private static double efficiency_electric_heat =.7;


	public ElectricHeatSource(double maxHeat) {
		// Call HeatSource constructor.
		super(HeatSourceType.ELECTRIC_HEATING, maxHeat);

	}

	/**
	 * Gets the current heat produced by the heat source.
	 * @param building the building this heat source is for.
	 * @return heat [in kW]
	 */
	public double getCurrentHeat(Building building) {		
		return (getMaxHeat() * getPercentagePower())/100D;
	}
	
	public double getEfficiency() {
		return efficiency_electric_heat;
	}


	public double getAverageHeat(Settlement settlement) {
		return getMaxHeat()/2D;
	}

	
	@Override
	public double getCurrentPower(Building building) {
		return 0.0;
	}
	
	@Override
	public double getMaintenanceTime() {
	    return getMaxHeat();
	}
}

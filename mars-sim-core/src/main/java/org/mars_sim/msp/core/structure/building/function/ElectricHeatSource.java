/*
 * Mars Simulation Project
 * ElectricHeatSource.java
 * @date 2022-07-31
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * An electric heat source is a type of electric furnace.
 */
public class ElectricHeatSource extends HeatSource {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	//private static final Logger logger = Logger.getLogger(ElectricHeatSource.class.getName());

	private double thermalEfficiency =.7;


	public ElectricHeatSource(double maxHeat) {
		// Call HeatSource constructor.
		super(HeatSourceType.ELECTRIC_HEATING, maxHeat);
	}

	/**
	 * Gets the current heat produced by the heat source.
	 * 
	 * @param building the building this heat source is for.
	 * @return heat [in kW]
	 */
	public double getCurrentHeat(Building building) {		
		return (getMaxHeat() * getPercentagePower())/100D * thermalEfficiency;
	}
	
	@Override
	public double getEfficiency() {
		return thermalEfficiency;
	}

	@Override
	public void setEfficiency(double value) {
		thermalEfficiency = value;
	}

	public double getAverageHeat(Settlement settlement) {
		return getMaxHeat() * getPercentagePower()/2D * thermalEfficiency;
	}

	
	@Override
	public double getCurrentPower(Building building) {
		return getCurrentHeat(building);
	}
	
	@Override
	public double getMaintenanceTime() {
	    return getMaxHeat();
	}
}

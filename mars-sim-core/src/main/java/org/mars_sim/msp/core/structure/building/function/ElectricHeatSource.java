/**
 * Mars Simulation Project
 * ElectricHeatSource.java
 * @version 3.07 2014-10-17
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.logging.Logger;

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
	private static Logger logger = Logger.getLogger(ElectricHeatSource.class.getName());

	private double maxCapacity;
	private static double efficiency_electric_heat =.7;

	//private static int count;

	public ElectricHeatSource(double maxHeat) {
		// Call HeatSource constructor.
		super(HeatSourceType.ELECTRIC_HEATING, maxHeat);

		this.maxCapacity = maxHeat;
	}

	/**
	 * Gets the current heat produced by the heat source.
	 * @param building the building this heat source is for.
	 * @return heat [in kW]
	 */
	// Called by ThermalGeneration.java
	public double getCurrentHeat(Building building) {		
		return maxCapacity;// * efficiency_electric_heat ;
	}
	
	public double getEfficiency() {
		return efficiency_electric_heat;
	}


	public double getAverageHeat(Settlement settlement) {
		return getMaxHeat()/2D;
	}


	@Override
	public double getCurrentPower(Building building) {
		// TODO Auto-generated method stub
		return 0.0;
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
	public void toggleHalf() {
	}
	
	@Override
	public void destroy() {
		super.destroy();
	}


}
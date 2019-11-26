/**
 * Mars Simulation Project
 * ElectricHeatSource.java
 * @version 3.1.0 2017-09-20
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
	//private static Logger logger = Logger.getLogger(ElectricHeatSource.class.getName());

	private static double efficiency_electric_heat =.7;

	private double factor = 1;
	
	private double max;
	
	//private static int count;

	public ElectricHeatSource(double maxHeat) {
		// Call HeatSource constructor.
		super(HeatSourceType.ELECTRIC_HEATING, maxHeat);

		this.max = maxHeat;
	}

	/**
	 * Gets the current heat produced by the heat source.
	 * @param building the building this heat source is for.
	 * @return heat [in kW]
	 */
	// Called by ThermalGeneration.java
	public double getCurrentHeat(Building building) {		
		return max * factor;// * efficiency_electric_heat ;
	}
	
	public double getEfficiency() {
		return efficiency_electric_heat;
	}


	public double getAverageHeat(Settlement settlement) {
		return getMaxHeat()/2D;
	}

	public double getCurrentHeat() {
		return max * factor;
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
	public void switch2Half() {
		factor = 1/2D;
	}
	
	@Override
	public void switch2OneQuarter() {
		factor = .25;
	}
	
	@Override
	public void switch2Full() {
		factor = 1D;
	}
	
	@Override
	public void destroy() {
		super.destroy();
	}

	@Override
	public void switch2ThreeQuarters() {
		factor = .75;
	}


}
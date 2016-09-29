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

	DecimalFormat fmt = new DecimalFormat("#.#######");

	private double maxCapacity;
	private static double efficiency_electric_heat =.7;

	//private static int count;

	public ElectricHeatSource(double maxHeat) {
		// Call HeatSource constructor.
		super(HeatSourceType.ELECTRIC_HEATING, maxHeat);
		//logger.info("constructor : maxHeat is " + maxHeat);
		//count++;
		//logger.info("constructor : count is " + count);
		this.maxCapacity = maxHeat;
		//BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
		//String name = config.getBuildingNames();
	}

	/**
	 * Gets the current heat produced by the heat source.
	 * @param building the building this heat source is for.
	 * @return heat [in Joules]
	 * Called by ThermalGeneration.java
	 */
	public double getCurrentHeat(Building building) {
		//double HeatGenerated;
		// TODO: adjust secPerTick according to the MarsClock
		//double secPerTick;
		// Note: 1/60/60 =.000277778 hr
		//double hourPerSec = 0.000277778;
		//double interval = Simulation.instance().getMasterClock().getTimePulse() ;
			//logger.info("getCurrentHeat() : TimePulse is " + fmt.format(interval));
			//System.out.println(fmt.format(interval));
		//	logger.info(fmt.format(interval));
		//logger.info("getCurrentHeat() : maxHeat is " +maxHeat);

		// maxCapacity is the capacity of the Electric Furnace [in kW]
		// HeatGenerated is the Heat Gain of a room [in BTU]
		// Note: 1 kW = 3413 BTU / hr
		//HeatGenerated =  maxCapacity * efficiency_electric_heat ;

		//logger.info("getCurrentHeat() : HeatGenerated is "+  fmt.format(HeatGenerated));

		//return HeatGenerated;
		
		return maxCapacity * efficiency_electric_heat ;
	}

	public static double getEfficiency() {
		return efficiency_electric_heat;
	}


	public double getAverageHeat(Settlement settlement) {
		return getMaxHeat();
	}

	@Override
	public double getMaintenanceTime() {
	    return getMaxHeat() * 1D;
	}
}
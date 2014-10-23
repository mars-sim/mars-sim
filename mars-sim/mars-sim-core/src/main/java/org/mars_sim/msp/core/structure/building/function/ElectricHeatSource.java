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

	private double maxHeat;
	private double efficiency =.95;
	private double secPerHour = 3600;
	//private static int count;
	
	public ElectricHeatSource(double maxHeat) {
		// Call HeatSource constructor.
		super(HeatSourceType.ELECTRIC, maxHeat);
		//logger.info("constructor : maxHeat is " + maxHeat); 
		//count++;
		//logger.info("constructor : count is " + count);
		this.maxHeat = maxHeat;	
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
		double HeatGenerated = 0;
		// TODO: adjust secPerTick according to the MarsClock
		double secPerTick; 
		double secPerHour = 0.000277778;
		//double interval = Simulation.instance().getMasterClock().getTimePulse() ;
			//logger.info("getCurrentHeat() : TimePulse is " + fmt.format(interval));
			//System.out.println(fmt.format(interval));
		//	logger.info(fmt.format(interval));
		//logger.info("getCurrentHeat() : maxHeat is " +maxHeat);
		
		// maxHeat is the capacity of the Electric Furnace [in kW]
		// HeatGenerated is the Heat Gain of a room [in BTU]
		HeatGenerated = 3400.0 * maxHeat * efficiency * secPerHour;
		
		//logger.info("getCurrentHeat() : HeatGenerated is "+  fmt.format(HeatGenerated));
		
		return HeatGenerated;
	}


	public double getAverageHeat(Settlement settlement) {
		return getMaxHeat();
	}
	
	@Override
	public double getMaintenanceTime() {
	    return getMaxHeat() * 2D;
	}
}
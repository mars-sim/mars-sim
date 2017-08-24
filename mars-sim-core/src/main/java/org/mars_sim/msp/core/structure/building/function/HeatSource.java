/**
 * Mars Simulation Project
 * HeatSource.java
 * @version 3.07 2014-12-06
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * The HeatSource class represents a heat generator for a building.
 */
public abstract class HeatSource
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	//private static Logger logger = Logger.getLogger(HeatSource.class.getName());

	// Data members
	private HeatSourceType type;
	private double maxHeat;
	//private static int count;

	/**
	 * Constructor.
	 * @param type the type of Heat source.
	 * @param maxHeat the max heat generated.
	 */
	public HeatSource(HeatSourceType type, double maxHeat) {
		this.type = type;
		this.maxHeat = maxHeat;
		//logger.info("constructor : maxHeat is " + maxHeat);
		//count++;
		//logger.info("constructor : count is " + count);

	}

	/**
	 * Gets the type of Heat source.
	 * @return type
	 */
	public HeatSourceType getType() {
		return type;
	}

	/**
	 * Gets the max heat generated.
	 * @return Heat
	 */
	public double getMaxHeat() {
		return maxHeat;
	}

	/**
	 * Gets the current Heat produced by the heat source.
	 * @param building the building this heat source is for.
	 * @return Heat (kW)
	 */
	public abstract double getCurrentHeat(Building building);

	/**
	 * Gets the average Heat produced by the heat source.
	 * @param settlement the settlement this heat source is at.
	 * @return heat(kW)
	 */
	public abstract double getAverageHeat(Settlement settlement);

	/**
	 * Gets the efficiency by the heat source.
	 * @return efficiency (max is 1)
	 */
	public abstract double getEfficiency();
	
	/**
     * Gets the maintenance time for this heat source.
     * @return maintenance work time (millisols).
     */
	public abstract double getMaintenanceTime();

	/**
	 * Gets the current Power produced by the heat source.
	 * @param building the building this heat source is for.
	 * @return power (kW)
	 */
	public abstract double getCurrentPower(Building building);

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		type = null;
	}
}
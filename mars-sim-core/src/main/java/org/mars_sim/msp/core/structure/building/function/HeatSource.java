/*
 * Mars Simulation Project
 * HeatSource.java
 * @date 2022-07-31
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.environment.SurfaceFeatures;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * The HeatSource class represents a heat generator for a building.
 */
public abstract class HeatSource implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	// private static final Logger logger = Logger.getLogger(HeatSource.class.getName());

	// Data members
	private double maxHeat;

	private double percent;
	
	private HeatSourceType type;

	protected static SurfaceFeatures surface;
	
	
	/**
	 * Constructor.
	 * 
	 * @param type    the type of Heat source.
	 * @param maxHeat the max heat generated.
	 */
	public HeatSource(HeatSourceType type, double maxHeat) {
		this.type = type;
		this.maxHeat = maxHeat;
		this.percent = 0;
		
		if (surface == null)
			surface = Simulation.instance().getSurfaceFeatures();
	}

	/**
	 * Gets the type of Heat source.
	 * 
	 * @return type
	 */
	public HeatSourceType getType() {
		return type;
	}

	/**
	 * Gets the max heat generated.
	 * 
	 * @return Heat
	 */
	public double getMaxHeat() {
		return maxHeat;
	}

	/**
	 * Return the percentage of full power for this heat source.
	 * 
	 * @return
	 */
	public double getPercentagePower() {
		return percent ;
	}

	/**
	 * Sets the percentage of the power for this heat source.
	 * 
	 * @param percentage
	 */
	public void setPercentagePower(double percentage) {
		this.percent = percentage;
	}

	/**
	 * Gets the current Heat produced by the heat source.
	 * 
	 * @param building the building this heat source is for.
	 * @return Heat (kW)
	 */
	public abstract double getCurrentHeat(Building building);

	/**
	 * Gets the average Heat produced by the heat source.
	 * 
	 * @param settlement the settlement this heat source is at.
	 * @return heat(kW)
	 */
	public abstract double getAverageHeat(Settlement settlement);

	/**
	 * Gets the efficiency by the heat source.
	 * 
	 * @return efficiency (max is 1)
	 */
	public double getEfficiency() {
		// To be overridden
		return 1;
	}

	/**
	 * Sets the thermal efficiency of the heat source.
	 */
	public void setEfficiency(double value) {
		// To be overridden
	}
	
	/**
	 * Gets the maintenance time for this heat source.
	 * 
	 * @return maintenance work time (millisols).
	 */
	public abstract double getMaintenanceTime();

	/**
	 * Gets the current Power produced by the heat source.
	 * 
	 * @param building the building this heat source is for.
	 * @return power (kW)
	 */
	public abstract double getCurrentPower(Building building);

	/**
	 * Sets the time for burning the fuel
	 * 
	 * @param time
	 */
	public void setTime(double time) {
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		type = null;
	}
}

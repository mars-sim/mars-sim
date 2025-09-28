/*
 * Mars Simulation Project
 * HeatSource.java
 * @date 2025-09-28
 * @author Manny Kung
 */
package com.mars_sim.core.building.utility.heating;

import java.io.Serializable;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.environment.SurfaceFeatures;

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

	private double percentElectricity;
	
	private double percentHeat;
	
	private HeatSourceType type;

	private HeatMode heatModeCache;
	
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
		this.percentElectricity = 50;
		this.percentHeat = 50;
		this.heatModeCache = HeatMode.HALF_HEAT;
		
		if (surface == null)
			surface = Simulation.instance().getSurfaceFeatures();
	}


	/**
	 * Gets the building's heat mode.
	 */
	public HeatMode getHeatMode() {
		return heatModeCache;
	}

	/**
	 * Sets the heat source's heat mode and percentages.
	 */
	public void setHeatMode(HeatMode heatMode, Building building) {
		heatModeCache = heatMode;
		
		if (heatMode == HeatMode.HEAT_OFF) {
			// Set heat percent to zero
			setPercentHeat(0);
			// Q: is it safe to assume one can set electricity percent to (100 minus the heat percent) ?
			double percentElectricity = 100 - percentHeat;
			setPercentElectricity(percentElectricity);
			
			building.fireUnitUpdate(UnitEventType.HEAT_MODE_EVENT);
			return;
		}
		
		if (heatMode == HeatMode.OFFLINE) {
			// Since the source is offline, set both heat and electricity percent to zero
			setPercentHeat(0);
			setPercentElectricity(0);
			
			building.fireUnitUpdate(UnitEventType.HEAT_MODE_EVENT);
			return;
		}
		
		// Heat percent
		double percentHeat = heatMode.getPercentage();
		setPercentHeat(percentHeat);
		
		// Electricity percent
		double percentElectricity = 100 - percentHeat;
		setPercentElectricity(percentElectricity);
		building.fireUnitUpdate(UnitEventType.HEAT_MODE_EVENT);
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
	 * Return the percentage of electricity allocated for this heat source.
	 * 
	 * @return
	 */
	public double getPercentElectricity() {
		return percentElectricity;
	}

	/**
	 * Sets the percentage of electricity allocated for this heat source.
	 * 
	 * @param percentage
	 */
	public void setPercentElectricity(double percentage) {
		this.percentElectricity = percentage;
	}


	/**
	 * Return the percentage of heat allocated for this heat source.
	 * 
	 * @return
	 */
	public double getPercentHeat() {
		return percentHeat;
	}

	/**
	 * Sets the percentage of heat allocated for this heat source.
	 * 
	 * @param percentage
	 */
	public void setPercentHeat(double percentage) {
		this.percentHeat = percentage;
	}
	
	/**
	 * Gets the current Heat produced by this heat source.
	 * 
	 * @param building the building this heat source is for.
	 * @return Heat (kW)
	 */
	public abstract double getCurrentHeat();

	/**
	 * Measures or estimates the heat produced by this heat source.
	 * 
	 * @param percent The percentage of capacity of this heat source
	 * @return Heat (kWt)
	 */
	public abstract double measureHeat(double percent);
	
	/**
	 * Gets the current Power produced by this heat source.
	 * 
	 * @param building the building this heat source is for.
	 * @return power (kW)
	 */
	public abstract double getCurrentPower();

	
	/**
	 * Gets the efficiency by this heat source.
	 * 
	 * @return efficiency (max is 1)
	 */
	public double getEfficiency() {
		// To be overridden
		return 1;
	}

	/**
	 * Sets the thermal efficiency of this heat source.
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
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		type = null;
		heatModeCache = null;
	}
}

/*
 * Mars Simulation Project
 * AdjustablePowerSource.java
 * @date 2025-09-26
 * @author Manny Kung
 */
package com.mars_sim.core.building.utility.power;

/**
 * Represents a Power Source that can be adjusted.
 */
public interface AdjustablePowerSource {
	
    /**
	 * Increases the power load capacity.
	 */
	public void increaseLoadCapacity();

    /**
	 * Decreases the power load capacity.
	 */
	public void decreaseLoadCapacity();
	
	/**
	 * Gets the current load capacity.
	 * 
	 * @return percent
	 */
	public double getCurrentLoadCapacity();
}

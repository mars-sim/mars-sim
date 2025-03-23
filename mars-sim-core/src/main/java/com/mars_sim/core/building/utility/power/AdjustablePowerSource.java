/*
 * Mars Simulation Project
 * AdjustablePowerSource.java
 * @date 2023-06-02
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
}

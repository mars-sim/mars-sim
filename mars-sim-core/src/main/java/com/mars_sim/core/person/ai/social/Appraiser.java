/*
 * Mars Simulation Project
 * Appraiser.java
 * @date 2024-02-04
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.social;

import com.mars_sim.core.Entity;

/**
 * This interface allows a subject to give opinion on another subject
 */
public interface Appraiser extends Entity {

	/**
	 * Gets the identifier of this unit.
	 */
	int getIdentifier();
}

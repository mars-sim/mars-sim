/*
 * Mars Simulation Project
 * ContainerInterface.java
 * @date 2021-10-03
 * @author Scott Davis
 */
package org.mars_sim.msp.core.equipment;

import org.mars_sim.msp.core.resource.PhaseType;

/**
 * This interface accounts for units that are considered container for resources
 */
public interface ContainerInterface {

	/**
	 * Gets the phase of resources this container can hold.
	 * 
	 * @return resource phase.
	 */
	public PhaseType getContainingResourcePhase();

	/**
	 * Gets the total capacity of resource that this container can hold.
	 * 
	 * @return total capacity (kg).
	 */
	public double getTotalCapacity();
}

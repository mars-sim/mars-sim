/**
 * Mars Simulation Project
 * Container.java
 * @version 3.1.1 2020-07-22
 * @author Scott Davis
 */
package org.mars_sim.msp.core.equipment;

import org.mars_sim.msp.core.resource.PhaseType;

/**
 * This interface accounts for units that are considered container for resources
 */
public interface Container {

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

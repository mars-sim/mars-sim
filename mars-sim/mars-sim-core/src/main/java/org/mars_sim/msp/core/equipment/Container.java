/**
 * Mars Simulation Project
 * Container.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core.equipment;

import org.mars_sim.msp.core.resource.Phase;

/**
 * Equipment classes that serve only as containers.
 */
public interface Container {

	/**
	 * Gets the phase of resources this container can hold.
	 * @return resource phase.
	 */
	public Phase getContainingResourcePhase();

	/**
	 * Gets the total capacity of resource that this container can hold.
	 * @return total capacity (kg).
	 */
	public double getTotalCapacity();
}
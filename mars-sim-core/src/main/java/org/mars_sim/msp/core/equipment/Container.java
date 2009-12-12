/**
 * Mars Simulation Project
 * Container.java
 * @version 2.81 2007-07-08
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
}
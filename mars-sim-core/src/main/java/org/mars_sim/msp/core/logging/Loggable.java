/*
 * Mars Simulation Project
 * Loggable.java
 * @date 2022-06-27
 * @author Barry Evans
 */

package org.mars_sim.msp.core.logging;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * This is a simulation entity that can trigger a log message.
 */
public interface Loggable {
	
	/**
	 * Returns the container unit where this entity is held.
	 * 
	 * @return
	 */
	Unit getContainerUnit();

	/**
	 * Returns the name of the entity to be logged.
	 * 
	 * @return
	 */
	String getName();

	/**
	 * Returns the physical location the surface.
	 * 
	 * @return
	 */
	Coordinates getCoordinates();
}

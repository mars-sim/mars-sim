/**
 * Mars Simulation Project
 * Loggable.java
 * @version 3.2.0 2021-06-20
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

	/*
	 * What is the settlement of this entity.
	 */
	Settlement getAssociatedSettlement();

	/**
	 * What is this entity 
	 * 
	 * @return
	 */
	Unit getUnit();
	
	/**
	 * Where is this entity held
	 * 
	 * @return
	 */
	Unit getContainerUnit();

	/**
	 * What is the name of the entity to be logged
	 * 
	 * @return
	 */
	String getNickName();

	/**
	 * Physical location the surface
	 * 
	 * @return
	 */
	Coordinates getCoordinates();

}

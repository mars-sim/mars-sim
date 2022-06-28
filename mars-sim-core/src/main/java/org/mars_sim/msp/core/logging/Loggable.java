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
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * This is a simulation entity that can trigger a log message.
 */
public interface Loggable {

	/*
	 * What is the settlement of this entity.
	 */
	Settlement getAssociatedSettlement();
	
	/*
	 * What is the vehicle of this entity.
	 */
	Vehicle getVehicle();
	
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
	String getName();

	/**
	 * Physical location the surface
	 * 
	 * @return
	 */
	Coordinates getCoordinates();

}

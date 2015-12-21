/**
 * Mars Simulation Project
 * LocationState.java
 * @version 3.08 2015-12-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.location;

import org.mars_sim.msp.core.Unit;

public interface LocationState {

	String getName();

	/**
	 * Catch unit update event.
	 * @param event the unit event.
	 */
	//public void unitUpdate(UnitEvent event);
}
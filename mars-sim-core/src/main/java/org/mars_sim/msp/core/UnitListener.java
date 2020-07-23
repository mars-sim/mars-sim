/**
 * Mars Simulation Project
 * UnitListener.java
 * @version 3.1.1 2020-07-22
 * @author Scott Davis
 */
package org.mars_sim.msp.core;

public interface UnitListener {

	/**
	 * Catch unit update event.
	 * 
	 * @param event the unit event.
	 */
	public void unitUpdate(UnitEvent event);
}

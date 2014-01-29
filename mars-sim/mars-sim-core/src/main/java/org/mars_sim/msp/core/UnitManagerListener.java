/**
 * Mars Simulation Project
 * UnitListener.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core;

public interface UnitManagerListener {

	/**
	 * Catch unit manager update event.
	 * @param event the unit event.
	 */
	public void unitManagerUpdate(UnitManagerEvent event);
	
}
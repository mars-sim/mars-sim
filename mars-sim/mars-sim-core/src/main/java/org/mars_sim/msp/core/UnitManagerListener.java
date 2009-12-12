/**
 * Mars Simulation Project
 * UnitListener.java
 * @version 2.80 2006-12-02
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
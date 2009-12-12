/**
 * Mars Simulation Project
 * UnitListener.java
 * @version 2.80 2006-09-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core;

public interface UnitListener {

	/**
	 * Catch unit update event.
	 * @param event the unit event.
	 */
	public void unitUpdate(UnitEvent event);
}
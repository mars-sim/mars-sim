/*
 * Mars Simulation Project
 * UnitManagerListener.java
 * @date 2024-08-10
 * @author Scott Davis
 */
package com.mars_sim.core;

public interface UnitManagerListener {

	/**
	 * Catches unit manager update event.
	 * 
	 * @param event the unit event.
	 */
	public void unitManagerUpdate(UnitManagerEvent event);

}

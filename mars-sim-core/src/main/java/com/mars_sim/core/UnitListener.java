/*
 * Mars Simulation Project
 * UnitListener.java
 * @date 2024-08-10
 * @author Scott Davis
 */
package com.mars_sim.core;

public interface UnitListener {

	/**
	 * Catches unit update event.
	 * 
	 * @param event the unit event.
	 */
	public void unitUpdate(UnitEvent event);
}

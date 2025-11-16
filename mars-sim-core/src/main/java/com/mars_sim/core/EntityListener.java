/*
 * Mars Simulation Project
 * EntityListener.java
 * @date 2025-11-16
 * @author Scott Davis
 */
package com.mars_sim.core;

public interface EntityListener {

	/**
	 * Catches entity update event.
	 * 
	 * @param event the entity event.
	 */
	public void entityUpdate(EntityEvent event);
}

/*
 * Mars Simulation Project
 * MissionListener.java
 * @date 2024-08-10
 * @author Scott Davis
 */

package com.mars_sim.core.goods;

/**
 * Interface for a credit event listener.
 */
public interface CreditListener {

	/**
	 * Catches credit update event.
	 * 
	 * @param event the credit event.
	 */
	public void creditUpdate(CreditEvent event);
}

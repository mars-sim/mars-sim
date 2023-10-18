/**
 * Mars Simulation Project
 * MissionListener.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */

package com.mars_sim.core.goods;

/**
 * Interface for a credit event listener.
 */
public interface CreditListener {

	/**
	 * Catch credit update event.
	 * 
	 * @param event the credit event.
	 */
	public void creditUpdate(CreditEvent event);
}

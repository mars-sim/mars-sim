/**
 * Mars Simulation Project
 * MissionListener.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure.goods;

/**
 * Interface for a credit event listener.
 */
public interface CreditListener {

	/**
	 * Catch credit update event.
	 * @param event the credit event.
	 */
	public void creditUpdate(CreditEvent event);
}
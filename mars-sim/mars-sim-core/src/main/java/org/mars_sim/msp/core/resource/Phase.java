/**
 * Mars Simulation Project
 * Phase.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */

package org.mars_sim.msp.core.resource;

import org.mars_sim.msp.core.Msg;


/**
 * A phase of an amount resource.
 */
public enum Phase {

	GAS (Msg.getString("Phase.gas")), //$NON-NLS-1$
	LIQUID (Msg.getString("Phase.liquid")), //$NON-NLS-1$
	SOLID (Msg.getString("Phase.solid")); //$NON-NLS-1$

	private String name;

	/**
	 * Private constructor.
	 * @param name the name of the phase.
	 */
	private Phase(String name) {
		this.name = name;
	}

	/**
	 * Gets the internationalized name of the phase for display in user interface.
	 * @return name
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
}
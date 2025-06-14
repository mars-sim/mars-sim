/**
 * Mars Simulation Project
 * PhaseType.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */

package com.mars_sim.core.resource;

import com.mars_sim.core.tool.Msg;


/**
 * The phase type of an amount resource.
 */
public enum PhaseType {

	GAS,LIQUID,SOLID;

	private String name;

	/**
	 * Private constructor.
	 */
	private PhaseType() {
        this.name = Msg.getStringOptional("PhaseType", name());
	}

	/**
	 * Gets the internationalized name of the phase for display in user interface.
	 * @return name
	 */
	public String getName() {
		return name;
	}
}

/*
 * Mars Simulation Project
 * PowerMode.java
 * @date 2023-05-25
 * @author stpa
 */

package org.mars_sim.msp.core.structure.building.function;

import org.mars.sim.tools.Msg;

public enum PowerMode {

	FULL_POWER (Msg.getString("PowerMode.fullPower")), //$NON-NLS-1$
	POWER_DOWN (Msg.getString("PowerMode.powerDown")), //$NON-NLS-1$
	NO_POWER (Msg.getString("PowerMode.noPower")), //$NON-NLS-1$
	POWER_UP ("Power up");

	private String name;

	/** hidden constructor. */
	private PowerMode(String name) {
		this.name = name;
	}

	/** gives back an internationalized {@link String} for display in user interface. */
	public String getName() {
		return this.name;
	}
}

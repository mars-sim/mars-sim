/**
 * Mars Simulation Project
 * HeatMode.java
 * @version 3.08 2015-05-04
 * @author Manny Kung
 */

package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.Msg;

public enum HeatMode {

	ONLINE (Msg.getString("HeatMode.online")), //$NON-NLS-1$
	HALF_HEAT (Msg.getString("HeatMode.halfHeat")), //$NON-NLS-1$
	HEAT_OFF (Msg.getString("HeatMode.heatOff")), //$NON-NLS-1$
	OFFLINE (Msg.getString("HeatMode.offline")), //$NON-NLS-1$
	POWER_UP ("Power up"); // meaning "OPERATIONAL"

	private String name;

	/** hidden constructor. */
	private HeatMode(String name) {
		this.name = name;

	}

	/** gives back an internationalized {@link String} for display in user interface. */
	public String getName() {
		return this.name;
	}
}

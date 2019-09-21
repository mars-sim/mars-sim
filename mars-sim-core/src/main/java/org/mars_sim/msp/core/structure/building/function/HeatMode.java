/**
 * Mars Simulation Project
 * HeatMode.java
 * @version 3.1.0 2019-09-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.Msg;

public enum HeatMode {

	FULL_HEAT (Msg.getString("HeatMode.fullHeat")), //$NON-NLS-1$
	THREE_QUARTER_HEAT (Msg.getString("HeatMode.threeQuarterHeat")), //$NON-NLS-1$
	HALF_HEAT (Msg.getString("HeatMode.halfHeat")), //$NON-NLS-1$
	QUARTER_HEAT (Msg.getString("HeatMode.quarterHeat")), //$NON-NLS-1$
	HEAT_OFF (Msg.getString("HeatMode.heatOff")), //$NON-NLS-1$
	OFFLINE (Msg.getString("HeatMode.offline")); //$NON-NLS-1$

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

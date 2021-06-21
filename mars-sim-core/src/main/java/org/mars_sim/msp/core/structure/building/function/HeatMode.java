/**
 * Mars Simulation Project
 * HeatMode.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.Msg;

public enum HeatMode {

	FULL_HEAT (Msg.getString("HeatMode.fullHeat"), 100), //$NON-NLS-1$
	THREE_QUARTER_HEAT (Msg.getString("HeatMode.threeQuarterHeat"), 75), //$NON-NLS-1$
	HALF_HEAT (Msg.getString("HeatMode.halfHeat"), 50), //$NON-NLS-1$
	QUARTER_HEAT (Msg.getString("HeatMode.quarterHeat"), 25), //$NON-NLS-1$
	HEAT_OFF (Msg.getString("HeatMode.heatOff"), 0), //$NON-NLS-1$
	OFFLINE (Msg.getString("HeatMode.offline"), 0); //$NON-NLS-1$

	private String name;
	private int percentage;

	/** hidden constructor. */
	private HeatMode(String name, int percentage) {
		this.name = name;
		this.percentage = percentage;

	}

	/** gives back an internationalized {@link String} for display in user interface. */
	public String getName() {
		return this.name;
	}
	
	public int getPercentage() {
		return this.percentage;
	}
}

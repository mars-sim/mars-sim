package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.Msg;

/**
 * @author stpa
 * 2014-03-03
 */
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

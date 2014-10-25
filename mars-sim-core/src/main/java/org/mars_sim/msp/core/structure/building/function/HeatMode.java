package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.Msg;

/**
 * @author Manny Kung
 * 2014-10-17
 */
public enum HeatMode {

	FULL_POWER (Msg.getString("HeatMode.fullPower")), //$NON-NLS-1$
	POWER_DOWN (Msg.getString("HeatMode.powerDown")), //$NON-NLS-1$
	NO_POWER (Msg.getString("HeatMode.noPower")), //$NON-NLS-1$
	POWER_UP ("Power up");
	//OPERATIONAL ("Operational");

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

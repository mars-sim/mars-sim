/**
 * Mars Simulation Project
 * LocationSituation.java
 * @version 3.1.0 2017-02-20
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person;

import org.mars_sim.msp.core.Msg;

/**
 * status indicating where a person is situated.
 * @author stpa
 * 2014-03-08
 */
public enum LocationSituation {

	IN_SETTLEMENT (Msg.getString("LocationSituation.inSettlement")), //$NON-NLS-1$
	IN_VEHICLE (Msg.getString("LocationSituation.inVehicle")), //$NON-NLS-1$
	OUTSIDE (Msg.getString("LocationSituation.outside")), //$NON-NLS-1$
	BURIED (Msg.getString("LocationSituation.buried")), //$NON-NLS-1$
	DECOMMISSIONED (Msg.getString("LocationSituation.decommissioned")), //$NON-NLS-1$
	UNKNOWN (Msg.getString("LocationSituation.unknown")); //$NON-NLS-1$
	
	private String name;

	/** hidden constructor. */
	private LocationSituation(String name) {
		this.name = name;
	}

	/** gives back an internationalized string for display in user interface. */
	public String getName() {
		return this.name;
	}
}

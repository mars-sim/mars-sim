/**
 * Mars Simulation Project
 * SystemType.java
 * @version 3.1.2 2020-09-02
 * @author stpa				
 */
package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.Msg;

public enum SystemType {

	BUILDING		(Msg.getString("SystemType.building")), //$NON-NLS-1$
    EVA_SUIT        (Msg.getString("SystemType.EVASuit")), //$NON-NLS=1$ 
	ROBOT			(Msg.getString("SystemType.robot")), //$NON-NLS-1$
    ROVER			(Msg.getString("SystemType.rover")), //$NON-NLS-1$
	VEHICLE			(Msg.getString("SystemType.vehicle")), //$NON-NLS-1$
    ;
	
	private String name;

	/** hidden constructor. */
	private SystemType(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
}

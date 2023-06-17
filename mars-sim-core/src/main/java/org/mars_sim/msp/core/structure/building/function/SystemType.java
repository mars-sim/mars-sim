/*
 * Mars Simulation Project
 * SystemType.java
 * @date 2023-06-16
 * @author stpa				
 */
package org.mars_sim.msp.core.structure.building.function;

import java.util.Set;

import org.mars_sim.msp.core.Msg;

public enum SystemType {

	BUILDING		(Msg.getString("SystemType.building")), //$NON-NLS-1$
    EVA_SUIT        (Msg.getString("SystemType.EVASuit")), //$NON-NLS=1$ 
	ROBOT			(Msg.getString("SystemType.robot")), //$NON-NLS-1$
    ROVER			(Msg.getString("SystemType.rover")), //$NON-NLS-1$
	VEHICLE			(Msg.getString("SystemType.vehicle")), //$NON-NLS-1$
    ;
	
	/**
	 * Returns a set of all system types.
	 */
	public static final Set<SystemType> ALL_SYSTEMS =
				Set.of(SystemType.BUILDING,
						SystemType.EVA_SUIT,
						SystemType.ROBOT,
						SystemType.ROVER,
						SystemType.VEHICLE);
	
	private String name;

	/** hidden constructor. */
	private SystemType(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
}

/*
 * Mars Simulation Project
 * BuildingCategory.java
 * @date 2020-07-05
 * @author Barry Evans
 */
package org.mars_sim.msp.core.structure.building;

import org.mars.sim.tools.Msg;

public enum BuildingCategory {

    // Note the order of these enums represent their importance
    COMMAND, 
    MEDICAL,
    ERV,
    FARMING, 
    LABORATORY, 
    LIVING,
    PROCESSING,
    WORKSHOP,
    VEHICLE,
    STORAGE, 
    EVA_AIRLOCK,
    HALLWAY,
    POWER;

	private String name;

	/** hidden constructor. */
	private BuildingCategory() {
		this.name = Msg.getString("BuildingCategory." + name()); //$NON-NLS-1$
	}

	/** gives back an internationalized {@link String} for display in user interface. */
	public String getName() {
		return this.name;
	}
}

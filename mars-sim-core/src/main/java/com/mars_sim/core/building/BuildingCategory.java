/*
 * Mars Simulation Project
 * BuildingCategory.java
 * @date 2024-07-04
 * @author Barry Evans
 */
package com.mars_sim.core.building;

import com.mars_sim.core.tool.Msg;

public enum BuildingCategory {

    // Note the order of these enums represent their importance
    COMMAND, 
    COMMUNICATION,
    MEDICAL,
    ERV,
    FARMING, 
    ASTRONOMY,
    WORKSHOP,
    LABORATORY, 
    LIVING,
    PROCESSING,
    VEHICLE,
    STORAGE, 
    EVA,
    CONNECTION,
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

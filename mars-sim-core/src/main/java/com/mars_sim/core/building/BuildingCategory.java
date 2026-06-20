/*
 * Mars Simulation Project
 * BuildingCategory.java
 * @date 2024-07-04
 * @author Barry Evans
 */
package com.mars_sim.core.building;

import com.mars_sim.core.Named;

import com.mars_sim.core.tool.Msg;

public enum BuildingCategory implements Named {

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
		this.name = Msg.getStringOptional("BuildingCategory", name());
	}

	/** gives back an internationalized {@link String} for display in user interface. */
    @Override
	public String getName() {
		return this.name;
	}
}

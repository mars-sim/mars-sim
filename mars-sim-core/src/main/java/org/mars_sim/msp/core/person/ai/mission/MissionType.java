/**
 * Mars Simulation Project
 * MissionType.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.mission;

import org.mars.sim.tools.Msg;

public enum MissionType {

	AREOLOGY,
	BIOLOGY,
	COLLECT_ICE,
	COLLECT_REGOLITH,
	DELIVERY,

	EMERGENCY_SUPPLY,
	EXPLORATION,
	METEOROLOGY,
	MINING,
	RESCUE_SALVAGE_VEHICLE,

	TRADE,
	TRAVEL_TO_SETTLEMENT,
	CONSTRUCTION,
	SALVAGE
	;

	private String name;

	/** hidden constructor. */
	private MissionType() {
		this.name = Msg.getString("MissionType." + name().toLowerCase());
	}

	/** gives the internationalized name of this skill for display in user interface. */
	public String getName() {
		return this.name;
	}

    public static MissionType lookup(String name) {
    	for (MissionType t : MissionType.values()) {
    		if (t.getName().equalsIgnoreCase(name))
    			return t;
    	}
    	return null;
    }
}

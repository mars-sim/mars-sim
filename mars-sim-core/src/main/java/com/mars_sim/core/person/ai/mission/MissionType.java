/**
 * Mars Simulation Project
 * MissionType.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.mission;

import com.mars_sim.core.Named;
import com.mars_sim.core.tool.Msg;

public enum MissionType implements Named {

	AREOLOGY("ARO"),
	BIOLOGY("BIO"),
	COLLECT_ICE("ICE"),
	COLLECT_REGOLITH("REG"),
	DELIVERY("DEL"),

	EMERGENCY_SUPPLY("EMS"),
	EXPLORATION("EXP"),
	METEOROLOGY("MET"),
	MINING("MIN"),
	RESCUE_SALVAGE_VEHICLE("RSV"),

	TRADE("TRA"),
	TRAVEL_TO_SETTLEMENT("TTS"),
	CONSTRUCTION("CON"),
	TEST_DRIVE("TDR")
	;

	private String name;
	private String shortCode;

	/** hidden constructor. */
	private MissionType(String shortCode) {
		this.name = Msg.getStringOptional("missiontype", name());
		this.shortCode = shortCode;
	}

	/** gives the internationalized name of this skill for display in user interface. */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the associated short code.
	 * @return Short code
	 */
    public String getShortCode() {
        return this.shortCode;
    }
}

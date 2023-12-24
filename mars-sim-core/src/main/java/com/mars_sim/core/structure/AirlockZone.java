/*
 * Mars Simulation Project
 * AirlockZone.java
 * @date 2023-12-23
 * @author Manny Kung
 */
package com.mars_sim.core.structure;

public enum AirlockZone {
	ZONE_0 (0),
	ZONE_1 (1),
	ZONE_2 (2),
	ZONE_3 (3),
	ZONE_4 (4);
	
	private int zoneNum;

	/** Hidden constructor. */
	private AirlockZone(int zoneNum) {
		this.zoneNum = zoneNum;
	}

	public int getZoneNum() {
		return this.zoneNum;
	}
	
	/**
	 * Converts an int to an airlock zone.
	 * 
	 * @param zoneNum
	 * @return
	 */
	public static AirlockZone convert2Zone(int zoneNum) {
		return valueOf("ZONE_" + zoneNum);
	}
}


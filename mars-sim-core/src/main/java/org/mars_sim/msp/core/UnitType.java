/**
 * Mars Simulation Project
 * UnitType.java
 * @version 3.1.2 2020-09-02
 * @author stpa
 */

package org.mars_sim.msp.core;

public enum UnitType {

	PLANET("UnitType.planet"),
	SETTLEMENT ("UnitType.settlement"),
	PERSON ("UnitType.person"),
	VEHICLE ("UnitType.vehicle"),
	EQUIPMENT ("UnitType.equipment"),
	ROBOT ("UnitType.robot"),
	BUILDING ("UnitType.building"),
	CONSTRUCTION ("UnitType.construction");

	private String msgKey;

	/** hidden constructor. */
	private UnitType(String msgKey) {
		this.msgKey = msgKey;
	}

	public String getMsgKey() {
		return this.msgKey;
	}
}

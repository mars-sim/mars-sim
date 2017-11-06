/**
 * Mars Simulation Project
 * UnitType.java
 * @version 3.1.0 2017-11-06
 * @author stpa
 */

package org.mars_sim.msp.core;

public enum UnitType {

	SETTLEMENT ("UnitType.settlement"),
	PERSON ("UnitType.person"),
	VEHICLE ("UnitType.vehicle"),
	EQUIPMENT ("UnitType.equipment"),
	ROBOT ("UnitType.robot"),
	BUILDING ("UnitType.building");

	private String msgKey;

	/** hidden constructor. */
	private UnitType(String msgKey) {
		this.msgKey = msgKey;
	}

	public String getMsgKey() {
		return this.msgKey;
	}
}

package org.mars_sim.msp.core;

/**
 * @author stpa
 * 2014-03-02
 */
public enum UnitType {

	SETTLEMENT ("UnitType.settlement"),
	PERSON ("UnitType.person"),
	VEHICLE ("UnitType.vehicle"),
	EQUIPMENT ("UnitType.equipment");

	private String msgKey;

	/** hidden constructor. */
	private UnitType(String msgKey) {
		this.msgKey = msgKey;
	}

	public String getMsgKey() {
		return this.msgKey;
	}
}

/**
 * Mars Simulation Project
 * UnitType.java
 * @version 3.2.0 2021-06-20
 * @author stpa
 */

package org.mars_sim.msp.core;

public enum UnitType {

	PLANET			("UnitType.planet"),
	SETTLEMENT 		("UnitType.settlement"),
	PERSON 			("UnitType.person"),
	VEHICLE 		("UnitType.vehicle"),
//	EQUIPMENT 		("UnitType.equipment"),
	CONTAINER		("UnitType.container"),
	EVA_SUIT 		("UnitType.evasuit"),
	ROBOT 			("UnitType.robot"),
	BUILDING 		("UnitType.building"),
	CONSTRUCTION 	("UnitType.construction");

	private String name;

	/** hidden constructor. */
	private UnitType(String msgKey) {
		this.name = Msg.getString(msgKey);
	}

	public String getName() {
		return this.name;
	}
}

/**
 * Mars Simulation Project
 * RoleType.java
 * @version 3.08 2015-04-28
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person;

import org.mars_sim.msp.core.Msg;

public enum RoleType {

	PRESIDENT					(Msg.getString("RoleType.president")), //$NON-NLS-1$
	COMMANDER					(Msg.getString("RoleType.commander")), //$NON-NLS-1$
	SUB_COMMANDER				(Msg.getString("RoleType.subCommander")), //$NON-NLS-1$
	CHIEF_OF_ENGINEERING		(Msg.getString("RoleType.chiefOfEngineering")), //$NON-NLS-1$
	CHIEF_OF_MISSION_PLANNING	(Msg.getString("RoleType.chiefOfMissionPlanning")), //$NON-NLS-1$
	CHIEF_OF_TRANSPORTATION		(Msg.getString("RoleType.chiefOfTransportation")), //$NON-NLS-1$
	CHIEF_OF_AGRICULTURE		(Msg.getString("RoleType.chiefOfAgriculture")), //$NON-NLS-1$
	CHIEF_OF_SCIENCE			(Msg.getString("RoleType.chiefOfScience")), //$NON-NLS-1$
	CHIEF_OF_SAFETY				(Msg.getString("RoleType.chiefOfSafety")), //$NON-NLS-1$
	CHIEF_OF_SUPPLY				(Msg.getString("RoleType.chiefOfSupply")), //$NON-NLS-1$
	ENGINEERING_SPECIALIST		(Msg.getString("RoleType.engineeringSpecialist")), //$NON-NLS-1$
	MISSION_SPECIALIST			(Msg.getString("RoleType.missionSpecialist")), //$NON-NLS-1$
	PAYLOAD_SPECIALIST			(Msg.getString("RoleType.payloadSpecialist")), //$NON-NLS-1$
	AGRICULTURE_SPECIALIST		(Msg.getString("RoleType.agricultureSpecialist")), //$NON-NLS-1$
	RESOURCE_SPECIALIST			(Msg.getString("RoleType.resourceSpecialist")), //$NON-NLS-1$
	SAFETY_SPECIALIST			(Msg.getString("RoleType.safetySpecialist")), //$NON-NLS-1$
	SCIENCE_SPECIALIST			(Msg.getString("RoleType.scienceSpecialist")), //$NON-NLS-1$
	;

	private String name;

	/** hidden constructor. */
	private RoleType(String name) {
		this.name = name;
	}

	public final String getName() {
		return this.name;
	}

	@Override
	public final String toString() {
		return getName();
	}
}

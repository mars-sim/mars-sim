/*
 * Mars Simulation Project
 * RoleType.java
 * @date 2021-09-27
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.ai.role;

import org.mars.sim.tools.Msg;

public enum RoleType {

	AGRICULTURE_SPECIALIST				(RoleLevel.SPECIALIST),
	ENGINEERING_SPECIALIST				(RoleLevel.SPECIALIST),
	MISSION_SPECIALIST					(RoleLevel.SPECIALIST),
	LOGISTIC_SPECIALIST					(RoleLevel.SPECIALIST),
	RESOURCE_SPECIALIST					(RoleLevel.SPECIALIST),
	SAFETY_SPECIALIST					(RoleLevel.SPECIALIST),
	SCIENCE_SPECIALIST					(RoleLevel.SPECIALIST),
	COMPUTING_SPECIALIST				(RoleLevel.SPECIALIST),
	
	CHIEF_OF_AGRICULTURE				(RoleLevel.CHIEF),
	CHIEF_OF_COMPUTING					(RoleLevel.CHIEF),
	CHIEF_OF_ENGINEERING				(RoleLevel.CHIEF),
	CHIEF_OF_MISSION_PLANNING			(RoleLevel.CHIEF),
	CHIEF_OF_LOGISTICS_N_OPERATIONS		(RoleLevel.CHIEF),
	CHIEF_OF_SAFETY_N_HEALTH			(RoleLevel.CHIEF),
	CHIEF_OF_SCIENCE					(RoleLevel.CHIEF),
	CHIEF_OF_SUPPLY_N_RESOURCES			(RoleLevel.CHIEF),
	
	PRESIDENT							(RoleLevel.COUNCIL),
	MAYOR								(RoleLevel.COUNCIL),
	COMMANDER							(RoleLevel.COUNCIL),
	SUB_COMMANDER						(RoleLevel.COUNCIL)
	;
	
	private String name;
	private RoleLevel level;

	/** hidden constructor. */
	private RoleType(RoleLevel level) {
		this.name = Msg.getString("RoleType." + name().toLowerCase());
		this.level = level;
	}

	public final String getName() {
		return this.name;
	}

	@Override
	public final String toString() {
		return getName();
	}
	

	public static RoleType getType(String name) {
		if (name != null) {
	    	for (RoleType r : RoleType.values()) {
	    		if (name.equalsIgnoreCase(r.name)) {
	    			return r;
	    		}
	    	}
		}
		
		return null;
	}
	
	public boolean isChief() {
		return level == RoleLevel.CHIEF;
	}
	
	public boolean isSpecialist() {
		return level == RoleLevel.SPECIALIST;
	}

	public boolean isCouncil() {
		return level == RoleLevel.COUNCIL;
	}
	
	public boolean isLeadership() {
		return isChief() || isCouncil();
	}
}

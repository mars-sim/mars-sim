/*
 * Mars Simulation Project
 * RoleType.java
 * @date 2023-11-14
 * @author Manny Kung
 */

package com.mars_sim.core.person.ai.role;

import com.mars_sim.core.Named;
import com.mars_sim.core.tool.Msg;

public enum RoleType implements Named {

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
	CHIEF_OF_LOGISTIC_OPERATION			(RoleLevel.CHIEF),
	CHIEF_OF_SAFETY_HEALTH_SECURITY		(RoleLevel.CHIEF),
	CHIEF_OF_SCIENCE					(RoleLevel.CHIEF),
	CHIEF_OF_SUPPLY_RESOURCE			(RoleLevel.CHIEF),
	
	CREW_ENGINEER						(RoleLevel.CREW),
	CREW_SAFETY_OFFICER					(RoleLevel.CREW),
	CREW_OPERATION_OFFICER				(RoleLevel.CREW),
	CREW_SCIENTIST						(RoleLevel.CREW),
	
	PRESIDENT							(RoleLevel.COUNCIL),
	MAYOR								(RoleLevel.COUNCIL),
	ADMINISTRATOR						(RoleLevel.COUNCIL),
	COMMANDER							(RoleLevel.COUNCIL),
	SUB_COMMANDER						(RoleLevel.COUNCIL)
	;
	
	private String name;
	private RoleLevel level;

	/** hidden constructor. */
	private RoleType(RoleLevel level) {
        this.name = Msg.getStringOptional("RoleType", name());
		this.level = level;
	}

	public final String getName() {
		return this.name;
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

	public boolean isCrew() {
		return level == RoleLevel.CREW;
	}
	
	public boolean isCouncil() {
		return level == RoleLevel.COUNCIL;
	}
	
	public boolean isLeadership() {
		return isChief() || isCouncil();
	}
}

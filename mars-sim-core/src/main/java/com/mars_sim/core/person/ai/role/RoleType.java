/*
 * Mars Simulation Project
 * RoleType.java
 * @date 2025-10-12
 * @author Manny Kung
 */

package com.mars_sim.core.person.ai.role;

import java.util.Collections;
import java.util.List;

import com.mars_sim.core.Named;
import com.mars_sim.core.person.ai.SkillType;
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
	DEPUTY_ADMINISTRATOR				(RoleLevel.COUNCIL),
	COMMANDER							(RoleLevel.COUNCIL),
	SUB_COMMANDER						(RoleLevel.COUNCIL),
	
	GUEST								(RoleLevel.VISITOR)
	;
	
	private String name;
	private RoleLevel level;

	/** hidden constructor. */
	private RoleType(RoleLevel level) {
        this.name = Msg.getStringOptional("RoleType", name());
		this.level = level;
	}

	public RoleLevel getLevel() {
		return this.level;
	}

	@Override
	public final String getName() {
		return this.name;
	}
	
	public boolean isChief() {
		return level == RoleLevel.CHIEF;
	}

	public boolean isCouncil() {
		return level == RoleLevel.COUNCIL;
	}
	
	public boolean isVisitor() {
		return level == RoleLevel.VISITOR;
	}
	
	public boolean isLeadership() {
		return isChief() || isCouncil();
	}

	
	/**
	 * Takes a Chief role type and return the associated specialty role.
	 * If the input role is not a Chief a null is returned.
	 * 
	 * @param roleType
	 * @return
	 */
	public static RoleType getChiefSpeciality(RoleType roleType) {
		return switch (roleType) {
	        case CHIEF_OF_AGRICULTURE -> RoleType.AGRICULTURE_SPECIALIST;
	        case CHIEF_OF_COMPUTING -> RoleType.COMPUTING_SPECIALIST;
	        case CHIEF_OF_ENGINEERING -> RoleType.ENGINEERING_SPECIALIST;
	        case CHIEF_OF_LOGISTIC_OPERATION -> RoleType.LOGISTIC_SPECIALIST;				
	        case CHIEF_OF_MISSION_PLANNING -> RoleType.MISSION_SPECIALIST;
	        case CHIEF_OF_SAFETY_HEALTH_SECURITY -> RoleType.SAFETY_SPECIALIST;
	        case CHIEF_OF_SCIENCE -> RoleType.SCIENCE_SPECIALIST;
	        case CHIEF_OF_SUPPLY_RESOURCE -> RoleType.RESOURCE_SPECIALIST;
	        default -> null;
	    };
	}

	/**
	 * Returns the list of required skills for the given role type.
	 * If the role type is not a Chief, null is returned.
	 * @param roleType
	 * @return
	 */
	public static List<SkillType> getRequiredSkills(RoleType roleType) {
		return switch(roleType) {
			case CHIEF_OF_AGRICULTURE -> List.of(
									SkillType.BOTANY,
									SkillType.ASTROBIOLOGY,
									SkillType.CHEMISTRY,
									SkillType.COOKING,
									SkillType.TRADING
									);
			case CHIEF_OF_COMPUTING -> List.of(
									SkillType.ASTRONOMY,
									SkillType.COMPUTING,
									SkillType.CHEMISTRY,
									SkillType.MATHEMATICS,
									SkillType.PHYSICS
									);
			case CHIEF_OF_ENGINEERING -> List.of(
									SkillType.MATERIALS_SCIENCE,
									SkillType.COMPUTING,
									SkillType.PHYSICS,
									SkillType.MECHANICS,
									SkillType.CONSTRUCTION
									);
			case CHIEF_OF_LOGISTIC_OPERATION -> List.of(
									SkillType.COMPUTING,
									SkillType.EVA_OPERATIONS,
									SkillType.MATHEMATICS,					
									SkillType.METEOROLOGY,
									SkillType.MECHANICS,
									SkillType.PILOTING,
									SkillType.TRADING									
									);
			case CHIEF_OF_MISSION_PLANNING -> List.of(
									SkillType.AREOLOGY,
									SkillType.COMPUTING,
									SkillType.EVA_OPERATIONS,
									SkillType.MATHEMATICS,
									SkillType.MANAGEMENT,									
									SkillType.PILOTING,									
									SkillType.PSYCHOLOGY,
									SkillType.TRADING									
									);
			case CHIEF_OF_SAFETY_HEALTH_SECURITY -> List.of(
									SkillType.AREOLOGY,
									SkillType.ASTROBIOLOGY,
									SkillType.CONSTRUCTION,
									SkillType.EVA_OPERATIONS,
									SkillType.MEDICINE,
									SkillType.PSYCHOLOGY,
									SkillType.TRADING
									);
			case CHIEF_OF_SCIENCE -> List.of(
									SkillType.AREOLOGY,
									SkillType.ASTRONOMY,
									SkillType.ASTROBIOLOGY,
									SkillType.BOTANY,									
									SkillType.CHEMISTRY,
									SkillType.COMPUTING,
									SkillType.MATERIALS_SCIENCE,
									SkillType.MATHEMATICS,
									SkillType.MEDICINE,
									SkillType.PHYSICS,
									SkillType.PSYCHOLOGY
									);
			case CHIEF_OF_SUPPLY_RESOURCE -> List.of(
									SkillType.COOKING,
									SkillType.MATHEMATICS,
									SkillType.MANAGEMENT,
									SkillType.MATERIALS_SCIENCE,
									SkillType.TRADING
									);
			default -> Collections.emptyList();
		};
	}
}

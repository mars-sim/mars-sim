/*
 * Mars Simulation Project
 * SkillType.java
 * @date 2022-07-16
 * @author stpa
 */

package com.mars_sim.core.person.ai;

import com.mars_sim.core.tool.Msg;

/**
 * The skill of a settler. A total of 21 types is available
 */
public enum SkillType {

	
	// The following is organized in accordance with the subject matter affinity : 
	
	TRADING,CONSTRUCTION,MECHANICS,PILOTING,EVA_OPERATIONS,
	PROSPECTING,METEOROLOGY,AREOLOGY,ASTRONOMY,CHEMISTRY,
	MATHEMATICS,COMPUTING,PHYSICS,MATERIALS_SCIENCE,COOKING,
	BOTANY,BIOLOGY,MEDICINE,PSYCHOLOGY,MANAGEMENT,
	REPORTING,ORGANISATION;
	
	private String name;

	/** hidden constructor. */
	private SkillType() {
        this.name = Msg.getStringOptional("SkillType", name());
	}

	/** gives the internationalized name of this skill for display in user interface. */
	public String getName() {
		return this.name;
	}
}

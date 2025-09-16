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

	
	// Note: The following listing is organized in accordance with the subject matter affinity : 
	// DO NOT alter the order
	TRADING, CONSTRUCTION, MECHANICS, PILOTING, EVA_OPERATIONS,
	PROSPECTING, METEOROLOGY, AREOLOGY, ASTRONOMY, CHEMISTRY,
	MATHEMATICS, COMPUTING, PHYSICS, MATERIALS_SCIENCE, COOKING,
	BOTANY, ASTROBIOLOGY, MEDICINE, PSYCHOLOGY, MANAGEMENT,
	REPORTING, ORGANISATION;
	// Note: The above listing is organized in accordance with the subject matter affinity 
	
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

/*
 * Mars Simulation Project
 * SkillType.java
 * @date 2025-10-12
 * @author stpa
 */

package com.mars_sim.core.person.ai;

import com.mars_sim.core.tool.Msg;

/**
 * The skill of a settler. A total of 22 types is available
 */
public enum SkillType {

	
	// The following listing is organized in accordance with the subject matter affinity.
	// It's intentional to be placed in this particular order.
	////////////////////////
	// Warning: Please DO NOT alter the order in which each enum show up
	TRADING, CONSTRUCTION, MECHANICS, PILOTING, EVA_OPERATIONS,
	PROSPECTING, METEOROLOGY, AREOLOGY, ASTRONOMY, CHEMISTRY,
	MATHEMATICS, COMPUTING, PHYSICS, MATERIALS_SCIENCE, COOKING,
	BOTANY, ASTROBIOLOGY, MEDICINE, PSYCHOLOGY, MANAGEMENT,
	REPORTING, ORGANISATION;
	////////////////////////

	private String name;

	/** 
	 * Hidden constructor. 
	 */
	private SkillType() {
        this.name = Msg.getStringOptional("SkillType", name());
	}

	/** 
	 * Gives the internationalized name of this skill for display in user interface. 
	 */
	public String getName() {
		return this.name;
	}
}

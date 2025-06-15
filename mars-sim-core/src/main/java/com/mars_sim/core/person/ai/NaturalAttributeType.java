/*
 * Mars Simulation Project
 * NaturalAttributeType.java
 * @date 2023-05-01
 * @author stpa
 */

package com.mars_sim.core.person.ai;

import com.mars_sim.core.tool.Msg;

public enum NaturalAttributeType {

	ACADEMIC_APTITUDE, AGILITY, ARTISTRY, ATTRACTIVENESS,
	COURAGE, CONVERSATION, CREATIVITY, DISCIPLINE,
	EMOTIONAL_STABILITY, ENDURANCE, EXPERIENCE_APTITUDE, KINDNESS,
	LEADERSHIP, METICULOUSNESS, ORGANIZATION, SPIRITUALITY,
	STRENGTH, STRESS_RESILIENCE, TEACHING;

	private String name;

	/** Hidden constructor. */
	private NaturalAttributeType() {
        this.name = Msg.getStringOptional("NaturalAttributeType", name());
	}

	/**
	 * Gives an internationalized string for display in user interface.
	 * 
	 * @return {@link String}
	 */
	public String getName() {
		return this.name;
	}
}

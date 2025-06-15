/**
 * Mars Simulation Project
 * PersonalityTraitType.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package com.mars_sim.core.person.ai;

import com.mars_sim.core.tool.Msg;

public enum PersonalityTraitType {

//          Openness - The willingness to make a shift of standards in new situations and appreciation for a variety of experience.
// Conscientiousness - Planning ahead rather than being spontaneous.
//      Extraversion - The willingness to communicate and socialize with people. Being energetic with people.
//     Agreeableness - The adaptiveness to other people. Adopt goals in favor of others.
//       Neuroticism - The extent to which one's emotion are sensitive to his environment
//                     The degree of worrying or feeling vulnerable
//	                   The emotional sensitivity and sense of security to the situation.

	OPENNESS, CONSCIENTIOUSNESS, EXTRAVERSION, AGREEABLENESS, NEUROTICISM;

	private String name;

	/** hidden constructor. */
	private PersonalityTraitType() {
        this.name = Msg.getStringOptional("PersonalityTraitType", name());
	}

	/**
	 * gives an internationalized string for display in user interface.
	 * 
	 * @return {@link String}
	 */
	public String getName() {
		return this.name;
	}

	public static PersonalityTraitType fromString(String name) {
		if (name != null) {
			for (PersonalityTraitType t : PersonalityTraitType.values()) {
				if (name.equalsIgnoreCase(t.name)) {
					return t;
				}
			}
		}
		return null;
	}
}

/**
 * Mars Simulation Project
 * PersonalityTraitType.java
 * @version 3.1.0 2016-11-05
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mars_sim.msp.core.Msg;

public enum PersonalityTraitType {

//          Openness - The willingness to make a shift of standards in new situations and appreciation for a variety of experience.
// Conscientiousness - Planning ahead rather than being spontaneous.
//      Extraversion - The willingness to communicate and socialize with people. Being energetic with people.
//     Agreeableness - The adaptiveness to other people. Adopt goals in favor of others.
//       Neuroticism - The extent to which one's emotion are sensitive to his environment; 
//                     The degree of worrying or feeling vulnerable;
//	                   The emotional sensitivity and sense of security to the situation.

	OPENNESS			(Msg.getString("PersonalityTraitType.openness")), //$NON-NLS-1$
	CONSCIENTIOUSNESS	(Msg.getString("PersonalityTraitType.conscientiousness")), //$NON-NLS-1$
	EXTRAVERSION		(Msg.getString("PersonalityTraitType.extraversion")), //$NON-NLS-1$
	AGREEABLENESS		(Msg.getString("PersonalityTraitType.agreeableness")), //$NON-NLS-1$
	NEUROTICISM			(Msg.getString("PersonalityTraitType.neuroticism")); //$NON-NLS-1$

// Note: PersonalityTraitType.values()

	private String name;

	/** hidden constructor. */
	private PersonalityTraitType(String name) {
		this.name = name;
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

	public static PersonalityTraitType valueOfIgnoreCase(String attributeName) {
		return PersonalityTraitType.valueOf(attributeName);
	}

	/**
	 * gets an array of internationalized attribute names for display in user
	 * interface. the array is in alphabetical order.
	 */
	public static String[] getNames() {
		List<String> list = new ArrayList<String>();
		for (PersonalityTraitType value : PersonalityTraitType.values()) {
			list.add(value.getName());
		}
		Collections.sort(list);
		return list.toArray(new String[] {});
	}
}

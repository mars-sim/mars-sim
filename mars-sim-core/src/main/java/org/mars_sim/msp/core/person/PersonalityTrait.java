/**
 * Mars Simulation Project
 * PersonalityTrait.java
  * @version 3.1.0 2016-10-31
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mars_sim.msp.core.Msg;

public enum PersonalityTrait {
/*
	Openness is appreciation for a variety of experience.
	Conscientiousness is planning ahead rather than being spontaneous.
	Extraversion involves going out with friends and being energetic.
	Agreeableness is, as it says, being agreeable.
	Neuroticism is the extent to which a person's emotion are sensitive
			to the individual's environment, if he's worrying or feeling vulnerable.

*/	
	OPENNESS (Msg.getString("PersonalityTrait.openness")), //$NON-NLS-1$
	CONSCIENTIOUSNESS (Msg.getString("PersonalityTrait.conscientiousness")), //$NON-NLS-1$
	EXTRAVERSION (Msg.getString("PersonalityTrait.extraversion")), //$NON-NLS-1$
	AGREEABLENESS (Msg.getString("PersonalityTrait.agreeableness")), //$NON-NLS-1$
	NEUROTICISM (Msg.getString("PersonalityTrait.neuroticism")); //$NON-NLS-1$


	private String name;

	/** hidden constructor. */
	private PersonalityTrait(String name) {
		this.name = name;
	}

	/**
	 * gives an internationalized string for display in user interface.
	 * @return {@link String}
	 */
	public String getName() {
		return this.name;
	}

	public static PersonalityTrait valueOfIgnoreCase(String attributeName) {
		return PersonalityTrait.valueOf(attributeName.toUpperCase().replace(' ','_'));
	}

	/**
	 * gets an array of internationalized attribute
	 * names for display in user interface. the
	 * array is in alphabetical order.
	 */
	public static String[] getNames() {
		List<String> list = new ArrayList<String>();
		for (PersonalityTrait value : PersonalityTrait.values()) {
			list.add(value.getName());
		}
		Collections.sort(list);
		return list.toArray(new String[] {});
	}
}

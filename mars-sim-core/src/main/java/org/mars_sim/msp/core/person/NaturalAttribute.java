package org.mars_sim.msp.core.person;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mars_sim.msp.core.Msg;

/**
 * @author stpa
 * 2014-03-10
 */
public enum NaturalAttribute {

	STRENGTH (Msg.getString("NaturalAttribute.strength")), //$NON-NLS-1$
	ENDURANCE (Msg.getString("NaturalAttribute.endurance")), //$NON-NLS-1$
	AGILITY (Msg.getString("NaturalAttribute.agility")), //$NON-NLS-1$
	TEACHING (Msg.getString("NaturalAttribute.teaching")), //$NON-NLS-1$
	ACADEMIC_APTITUDE (Msg.getString("NaturalAttribute.academicAptitude")), //$NON-NLS-1$
	EXPERIENCE_APTITUDE (Msg.getString("NaturalAttribute.experienceAptitude")), //$NON-NLS-1$
	STRESS_RESILIENCE (Msg.getString("NaturalAttribute.stressResilience")), //$NON-NLS-1$
	ATTRACTIVENESS (Msg.getString("NaturalAttribute.attractiveness")), //$NON-NLS-1$
	LEADERSHIP (Msg.getString("NaturalAttribute.leadership")), //$NON-NLS-1$
	CONVERSATION (Msg.getString("NaturalAttribute.conversation")); //$NON-NLS-1$

	private String name;

	/** hidden constructor. */
	private NaturalAttribute(String name) {
		this.name = name;
	}

	/**
	 * gives an internationalized string for display in user interface.
	 * @return {@link String}
	 */
	public String getName() {
		return this.name;
	}

	public static NaturalAttribute valueOfIgnoreCase(String attributeName) {
		return NaturalAttribute.valueOf(attributeName.toUpperCase().replace(' ','_'));
	}

	/**
	 * gets an array of internationalized attribute
	 * names for display in user interface. the
	 * array is in alphabetical order.
	 */
	public static String[] getNames() {
		List<String> list = new ArrayList<String>();
		for (NaturalAttribute value : NaturalAttribute.values()) {
			list.add(value.getName());
		}
		Collections.sort(list);
		return list.toArray(new String[] {});
	}
}

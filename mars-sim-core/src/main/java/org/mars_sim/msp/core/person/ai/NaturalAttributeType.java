/**
 * Mars Simulation Project
 * NaturalAttributeType.java
 * @version 3.1.0 2017-10-03
 * @author stpa
 */

package org.mars_sim.msp.core.person.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mars_sim.msp.core.Msg;

public enum NaturalAttributeType {

	ACADEMIC_APTITUDE (Msg.getString("NaturalAttributeType.academicAptitude")), //$NON-NLS-1$
	AGILITY (Msg.getString("NaturalAttributeType.agility")), //$NON-NLS-1$
	ARTISTRY (Msg.getString("NaturalAttributeType.artistry")), //$NON-NLS-1$
	ATTRACTIVENESS (Msg.getString("NaturalAttributeType.attractiveness")), //$NON-NLS-1$
	COURAGE (Msg.getString("NaturalAttributeType.courage")), //$NON-NLS-1$
	
	CONVERSATION (Msg.getString("NaturalAttributeType.conversation")), //$NON-NLS-1$
	EMOTIONAL_STABILITY (Msg.getString("NaturalAttributeType.emotionalStability")), //$NON-NLS-1$
	ENDURANCE (Msg.getString("NaturalAttributeType.endurance")), //$NON-NLS-1$
	EXPERIENCE_APTITUDE (Msg.getString("NaturalAttributeType.experienceAptitude")), //$NON-NLS-1$
	LEADERSHIP (Msg.getString("NaturalAttributeType.leadership")), //$NON-NLS-1$
	
	SPIRITUALITY (Msg.getString("NaturalAttributeType.spirituality")), //$NON-NLS-1$
	STRENGTH (Msg.getString("NaturalAttributeType.strength")), //$NON-NLS-1$
	STRESS_RESILIENCE (Msg.getString("NaturalAttributeType.stressResilience")), //$NON-NLS-1$
	TEACHING (Msg.getString("NaturalAttributeType.teaching")); //$NON-NLS-1$


	private String name;

	/** hidden constructor. */
	private NaturalAttributeType(String name) {
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

	public static NaturalAttributeType valueOfIgnoreCase(String attributeName) {
		return NaturalAttributeType.valueOf(attributeName.toUpperCase().replace(' ','_'));
	}

	/**
	 * gets an array of internationalized attribute
	 * names for display in user interface. the
	 * array is in alphabetical order.
	 */
	public static String[] getNames() {
		List<String> list = new ArrayList<String>();
		for (NaturalAttributeType value : NaturalAttributeType.values()) {
			list.add(value.getName());
		}
		Collections.sort(list);
		return list.toArray(new String[] {});
	}
	
//	public List<NaturalAttributeType> getNaturalAttributeTypes() {
//		List<NaturalAttributeType> list = new ArrayList<NaturalAttributeType>();
//		for (NaturalAttributeType value : NaturalAttributeType.values()) {
//			list.add(value);
//		}
//		
//		return list;
//	}
}

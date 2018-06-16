/**
 * Mars Simulation Project
 * RoboticAttributeType.java
 * @version 3.1.0 2016-05-04
 * @author Manny Kung
 */

package org.mars_sim.msp.core.robot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mars_sim.msp.core.Msg;

public enum RoboticAttributeType {

	AGILITY (Msg.getString("RoboticAttributeType.agility")), //$NON-NLS-1$
	CONVERSATION (Msg.getString("RoboticAttributeType.conversation")), //$NON-NLS-1$
	ENDURANCE (Msg.getString("RoboticAttributeType.endurance")), //$NON-NLS-1$
	EXPERIENCE_APTITUDE (Msg.getString("RoboticAttributeType.experienceAptitude")), //$NON-NLS-1$
	STRENGTH (Msg.getString("RoboticAttributeType.strength")), //$NON-NLS-1$
	TEACHING (Msg.getString("RoboticAttributeType.teaching")); //$NON-NLS-1$


	private String name;

	/** hidden constructor. */
	private RoboticAttributeType(String name) {
		this.name = name;
	}

	/**
	 * gives an internationalized string for display in user interface.
	 * @return {@link String}
	 */
	public String getName() {
		return this.name;
	}

	public static RoboticAttributeType valueOfIgnoreCase(String attributeName) {
		return RoboticAttributeType.valueOf(attributeName.toUpperCase().replace(' ','_'));
	}

	/**
	 * gets an array of internationalized robotic attribute type
	 * in alphabetical order.
	 */
	public static String[] getNames() {
		List<String> list = new ArrayList<String>();
		for (RoboticAttributeType value : RoboticAttributeType.values()) {
			list.add(value.getName());
		}
		Collections.sort(list);
		return list.toArray(new String[] {});
	}
}

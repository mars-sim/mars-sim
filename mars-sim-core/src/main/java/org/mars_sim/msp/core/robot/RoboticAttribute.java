/**
 * Mars Simulation Project
 * RoboticAttribute.java
 * @version 3.08 2016-05-04
 * @author Manny Kung
 */

package org.mars_sim.msp.core.robot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mars_sim.msp.core.Msg;

public enum RoboticAttribute {

	AGILITY (Msg.getString("RoboticAttribute.agility")), //$NON-NLS-1$
	CONVERSATION (Msg.getString("RoboticAttribute.conversation")), //$NON-NLS-1$
	ENDURANCE (Msg.getString("RoboticAttribute.endurance")), //$NON-NLS-1$
	EXPERIENCE_APTITUDE (Msg.getString("RoboticAttribute.experienceAptitude")), //$NON-NLS-1$
	STRENGTH (Msg.getString("RoboticAttribute.strength")), //$NON-NLS-1$
	TEACHING (Msg.getString("RoboticAttribute.teaching")); //$NON-NLS-1$


	private String name;

	/** hidden constructor. */
	private RoboticAttribute(String name) {
		this.name = name;
	}

	/**
	 * gives an internationalized string for display in user interface.
	 * @return {@link String}
	 */
	public String getName() {
		return this.name;
	}

	public static RoboticAttribute valueOfIgnoreCase(String attributeName) {
		return RoboticAttribute.valueOf(attributeName.toUpperCase().replace(' ','_'));
	}

	/**
	 * gets an array of internationalized attribute
	 * names for display in user interface. the
	 * array is in alphabetical order.
	 */
	public static String[] getNames() {
		List<String> list = new ArrayList<String>();
		for (RoboticAttribute value : RoboticAttribute.values()) {
			list.add(value.getName());
		}
		Collections.sort(list);
		return list.toArray(new String[] {});
	}
}

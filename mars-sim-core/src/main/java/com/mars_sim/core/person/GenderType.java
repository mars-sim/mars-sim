/*
 * Mars Simulation Project
 * GenderType.java
 * @date 2024-08-10
 * @author stpa
 */

package com.mars_sim.core.person;

import com.mars_sim.tools.Msg;

public enum GenderType {

	MALE (Msg.getString("PersonGender.male")), //$NON-NLS-1$
	FEMALE (Msg.getString("PersonGender.female")), //$NON-NLS-1$
	UNKNOWN (Msg.getString("unknown")); //$NON-NLS-1$

	private String name;

	/** hidden constructor. */
	private GenderType(String name) {
		this.name = name;
	}

	/**
	 * Gets the gender string, an internationalized translation 
	 * for display in user interface.
	 * 
	 * @return {@link String}
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Gets the matched gender type.
	 * 
	 * @param s the gender string
	 */	
	public static GenderType valueOfIgnoreCase(String s) {
		return valueOf(s.toUpperCase().replace(' ','_'));
	}
	
	public static String getPossessivePronoun(GenderType gender) {
		if (gender == GenderType.MALE)
			return "his";
		else if (gender == GenderType.FEMALE)
			return "her";
		
		return "its";
	}
}

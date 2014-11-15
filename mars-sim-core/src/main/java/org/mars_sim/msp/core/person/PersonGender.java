package org.mars_sim.msp.core.person;

import org.mars_sim.msp.core.Msg;

/**
 * @author stpa
 * 2014-03-04
 */
public enum PersonGender {

	MALE (Msg.getString("PersonGender.male")), //$NON-NLS-1$
	FEMALE (Msg.getString("PersonGender.female")), //$NON-NLS-1$
	UNKNOWN (Msg.getString("unknown")); //$NON-NLS-1$

	private String name;

	/** hidden constructor. */
	private PersonGender(String name) {
		this.name = name;
	}

	/**
	 * an internationalized translation for display in user interface.
	 * @return {@link String}
	 */
	public String getName() {
		return this.name;
	}

	public static PersonGender valueOfIgnoreCase(String s) {
		return valueOf(s.toUpperCase().replace(' ','_'));
	}
}

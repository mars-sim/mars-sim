/**
 * Mars Simulation Project
 * ShiftType.java
 * @version 3.08 2015-11-02
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person;

import org.mars_sim.msp.core.Msg;

public enum ShiftType {

	A								(Msg.getString("ShiftType.A")), //$NON-NLS-1$
	B								(Msg.getString("ShiftType.B")), //$NON-NLS-1$
	X								(Msg.getString("ShiftType.X")), //$NON-NLS-1$
	Y								(Msg.getString("ShiftType.Y")), //$NON-NLS-1$
	Z								(Msg.getString("ShiftType.Z")), //$NON-NLS-1$
	OFF								(Msg.getString("ShiftType.off")), //$NON-NLS-1$
	ON_CALL							(Msg.getString("ShiftType.onCall")), //$NON-NLS-1$
	;

	private String name;

	/** hidden constructor. */
	private ShiftType(String name) {
		this.name = name;
	}

	public final String getName() {
		return this.name;
	}

	@Override
	public final String toString() {
		return getName();
	}
}

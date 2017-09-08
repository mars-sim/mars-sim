/**
 * Mars Simulation Project
 * ShiftType.java
 * @version 3.1.0 2017-08-30
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person;

import org.mars_sim.msp.core.Msg;

public enum ShiftType {

	A			(Msg.getString("ShiftType.A")), //$NON-NLS-1$
	B			(Msg.getString("ShiftType.B")), //$NON-NLS-1$
	X			(Msg.getString("ShiftType.X")), //$NON-NLS-1$
	Y			(Msg.getString("ShiftType.Y")), //$NON-NLS-1$
	Z			(Msg.getString("ShiftType.Z")), //$NON-NLS-1$
	OFF			(Msg.getString("ShiftType.off")), //$NON-NLS-1$ 
	ON_CALL		(Msg.getString("ShiftType.onCall")), //$NON-NLS-1$
	;

	// if a person is having OFF shift type, he's on vacation 
	
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

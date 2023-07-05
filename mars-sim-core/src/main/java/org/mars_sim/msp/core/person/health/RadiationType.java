/*
 * Mars Simulation Project
 * RadiationType.java
 * @date 2022-09-24
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.health;

import org.mars.sim.tools.Msg;

public enum RadiationType {

	BASELINE	(Msg.getString("RadiationType.baseline")),	//$NON-NLS-1$
	GCR			(Msg.getString("RadiationType.gcr")), 		//$NON-NLS-1$
	SEP			(Msg.getString("RadiationType.sep")), 		//$NON-NLS-1$
	;

	private String name;

	/** hidden constructor. */
	private RadiationType(String name) {
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

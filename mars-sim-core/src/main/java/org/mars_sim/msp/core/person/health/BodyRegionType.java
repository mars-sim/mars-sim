/*
 * Mars Simulation Project
 * BodyRegionType.java
 * @date 2022-09-24
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.health;

import org.mars_sim.msp.core.Msg;

public enum BodyRegionType {

	BFO		(Msg.getString("BodyRegionType.BFO")), //$NON-NLS-1$
	OCULAR	(Msg.getString("BodyRegionType.ocular")), //$NON-NLS-1$
	SKIN	(Msg.getString("BodyRegionType.skin")), //$NON-NLS-1$
	;

	private String name;

	/** hidden constructor. */
	private BodyRegionType(String name) {
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

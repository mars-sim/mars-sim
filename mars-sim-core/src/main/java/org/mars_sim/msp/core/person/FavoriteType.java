/**
 * Mars Simulation Project
 * FavoriteType.java
 * @version 3.1.0 2017-10-03
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person;

import org.mars_sim.msp.core.Msg;

public enum FavoriteType {

	TENDING_PLANTS				(Msg.getString("FavoriteType.tendingPlants")), //$NON-NLS-1$
	ASTRONOMY	 				(Msg.getString("FavoriteType.astronomy")), //$NON-NLS-1$
	WORKOUT		 				(Msg.getString("FavoriteType.workout")), //$NON-NLS-1$
	RESEARCH 	 				(Msg.getString("FavoriteType.research")), //$NON-NLS-1$
	FIELD_WORK	 				(Msg.getString("FavoriteType.fieldWork")), //$NON-NLS-1$
	TINKERING	 				(Msg.getString("FavoriteType.inkering")), //$NON-NLS-1$
	LAB_EXPERIMENTATION			(Msg.getString("FavoriteType.labExperimentation")), //$NON-NLS-1$
	COOKING						(Msg.getString("FavoriteType.cooking")), //$NON-NLS-1$
	;

	private String name;

	/** hidden constructor. */
	private FavoriteType(String name) {
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

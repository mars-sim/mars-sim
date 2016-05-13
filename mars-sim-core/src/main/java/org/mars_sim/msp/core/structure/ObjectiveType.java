/**
 * Mars Simulation Project
 * ObjectiveType.java
 * @version 3.08 2016-01-16
 * @author Manny Kung
 */

package org.mars_sim.msp.core.structure;

import org.mars_sim.msp.core.Msg;

public enum ObjectiveType {

	CROP_FARM 			(Msg.getString("ObjectiveType.crop")), //$NON-NLS-1$
	MANUFACTURING		(Msg.getString("ObjectiveType.manu")), //$NON-NLS-1$
	RESEARCH_CENTER		(Msg.getString("ObjectiveType.research")), //$NON-NLS-1$
	TRANSPORTATION_HUB	(Msg.getString("ObjectiveType.transportation")), //$NON-NLS-1$
	TRADE_TOWN			(Msg.getString("ObjectiveType.trade")), //$NON-NLS-1$

	;

	private String objectiveName;

	/** hidden constructor. */
	private ObjectiveType(String objectiveName) {
		this.objectiveName = objectiveName;
	}
	
	public final String getObjectiveName() {
		return this.objectiveName;
	}

	@Override
	public final String toString() {
		return getObjectiveName();
	}
}

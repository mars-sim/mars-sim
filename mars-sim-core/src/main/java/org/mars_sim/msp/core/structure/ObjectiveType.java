/**
 * Mars Simulation Project
 * ObjectiveType.java
 * @version 3.1.0 2017-11-04
 * @author Manny Kung
 */

package org.mars_sim.msp.core.structure;

import org.mars_sim.msp.core.Msg;

public enum ObjectiveType {

	CROP_FARM 				(Msg.getString("ObjectiveType.crop")), //$NON-NLS-1$
	MANUFACTURING_DEPOT		(Msg.getString("ObjectiveType.manu")), //$NON-NLS-1$
	RESEARCH_CAMPUS			(Msg.getString("ObjectiveType.research")), //$NON-NLS-1$
	TRANSPORTATION_HUB		(Msg.getString("ObjectiveType.transportation")), //$NON-NLS-1$
	TRADE_CENTER			(Msg.getString("ObjectiveType.trade")), //$NON-NLS-1$
	TOURISM					(Msg.getString("ObjectiveType.tourism")), //$NON-NLS-1$
	//FREE_MARKET			(Msg.getString("ObjectiveType.freeMarket")), //$NON-NLS-1$
	;

	private String name;

	/** hidden constructor. */
	private ObjectiveType(String name) {
		this.name = name;
	}
	
	public final String getName() {
		return this.name;
	}

	@Override
	public final String toString() {
		return getName();
	}
	
	public static ObjectiveType getType(String s) {
		return valueOf(s.toUpperCase().replace(" ", "_"));
	}
}

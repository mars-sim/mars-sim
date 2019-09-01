/**
 * Mars Simulation Project
 * FavoriteType.java
 * @version 3.1.0 2017-10-03
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person;

import java.util.Arrays;
import java.util.List;

import org.mars_sim.msp.core.Msg;

public enum FavoriteType {

	ASTRONOMY	 				(Msg.getString("FavoriteType.astronomy")), //$NON-NLS-1$
	COOKING						(Msg.getString("FavoriteType.cooking")), //$NON-NLS-1$
	FIELD_WORK	 				(Msg.getString("FavoriteType.fieldWork")), //$NON-NLS-1$
	GAMING						(Msg.getString("FavoriteType.gaming")), //$NON-NLS-1$
	LAB_EXPERIMENTATION			(Msg.getString("FavoriteType.labExperimentation")), //$NON-NLS-1$
	OPERATION		 			(Msg.getString("FavoriteType.operation")), //$NON-NLS-1$
	RESEARCH 	 				(Msg.getString("FavoriteType.research")), //$NON-NLS-1$
	SPORT 		 				(Msg.getString("FavoriteType.sport")), //$NON-NLS-1$
	TENDING_PLANTS				(Msg.getString("FavoriteType.tendingPlants")), //$NON-NLS-1$
	TINKERING	 				(Msg.getString("FavoriteType.tinkering")), //$NON-NLS-1$
	;

	static FavoriteType[] availableFavoriteTypes = new FavoriteType[] { 	
			ASTRONOMY,
			COOKING,
			FIELD_WORK,
			GAMING,
			LAB_EXPERIMENTATION,
			OPERATION,
			RESEARCH,
			SPORT,
			TENDING_PLANTS,	
			TINKERING
			};
	
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
	
	public static FavoriteType fromString(String name) {
		if (name != null) {
	    	for (FavoriteType f : FavoriteType.values()) {
	    		if (name.equalsIgnoreCase(f.name)) {
	    			return f;
	    		}
	    	}
		}
		
		return null;
	}

	/**
	 * gives back a list of all valid values for the FavoriteType enum.
	 */
	public static List<FavoriteType> valuesList() {
		return Arrays.asList(FavoriteType.values());
		// Arrays.asList() returns an ArrayList which is a private static class inside Arrays. 
		// It is not an java.util.ArrayList class.
		// Could possibly reconfigure this method as follows: 
		// public ArrayList<FavoriteType> valuesList() {
		// 	return new ArrayList<FavoriteType>(Arrays.asList(FavoriteType.values())); }
	}
}

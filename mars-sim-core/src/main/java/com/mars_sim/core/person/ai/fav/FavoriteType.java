/*
 * Mars Simulation Project
 * FavoriteType.java
 * @date 2022-08-01
 * @author Manny Kung
 */

package com.mars_sim.core.person.ai.fav;

import com.mars_sim.core.tool.Msg;

public enum FavoriteType {

	ASTRONOMY,COOKING,FIELD_WORK,GAMING,LAB_EXPERIMENTATION,
	OPERATION,RESEARCH,SPORT,TENDING_FARM,TINKERING;

	static FavoriteType[] availableFavoriteTypes = new FavoriteType[] { 	
			ASTRONOMY,
			COOKING,
			FIELD_WORK,
			GAMING,
			LAB_EXPERIMENTATION,
			OPERATION,
			RESEARCH,
			SPORT,
			TENDING_FARM,	
			TINKERING
			};
	
	private String name;

	/** hidden constructor. */
	private FavoriteType() {
        this.name = Msg.getStringOptional("FavoriteType", name());
	}

	public final String getName() {
		return this.name;
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
}

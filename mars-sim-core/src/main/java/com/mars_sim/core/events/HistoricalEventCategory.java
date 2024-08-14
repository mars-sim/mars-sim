/*
 * Mars Simulation Project
 * HistoricalEventCategory.java
 * @date 2024-08-10
 * @author stpa
 */

package com.mars_sim.core.events;

import java.util.Arrays;
import java.util.List;

import com.mars_sim.core.tool.Msg;

public enum HistoricalEventCategory {

	MEDICAL (Msg.getString("HistoricalEventType.medical")), //$NON-NLS-1$
	MALFUNCTION (Msg.getString("HistoricalEventType.malfunction")), //$NON-NLS-1$
	MISSION (Msg.getString("HistoricalEventType.mission")), //$NON-NLS-1$
	TASK (Msg.getString("HistoricalEventType.task")), //$NON-NLS-1$
	TRANSPORT (Msg.getString("HistoricalEventType.transport")), //$NON-NLS-1$
	HAZARD (Msg.getString("HistoricalEventType.hazard")); //$NON-NLS-1$
	
	private String name;

	/** hidden constructor. */
	private HistoricalEventCategory(String name) {
		this.name = name;
	}

	/** 
	 * Gets the internationalized name for display in user interface.
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Gives back a list of all valid values for the HistoricalEventCategory enum.
	 */
	public static List<HistoricalEventCategory> valuesList() {
		return Arrays.asList(HistoricalEventCategory.values());
	}
	
	public static HistoricalEventCategory int2enum(int ordinal) {
		return HistoricalEventCategory.values()[ordinal];
	}
	
	public static HistoricalEventCategory str2enum(String name) {
		if (name != null) {
	    	for (HistoricalEventCategory c : HistoricalEventCategory.values()) {
	    		if (name.equalsIgnoreCase(c.name)) {
	    			return c;
	    		}
	    	}
		}
		
		return null;
	}
	
	public static int str2int(String name) {
		if (name != null) {
	    	for (HistoricalEventCategory c : HistoricalEventCategory.values()) {
	    		if (name.equalsIgnoreCase(c.name)) {
	    			return c.ordinal();
	    		}
	    	}
		}
		
		return -1;
	}
}

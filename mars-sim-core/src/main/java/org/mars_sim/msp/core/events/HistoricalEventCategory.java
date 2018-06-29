/**
 * Mars Simulation Project
 * HistoricalEventCategory.java
 * @version 3.1.0 2017-09-16
 * @author stpa
 */
package org.mars_sim.msp.core.events;

import java.util.Arrays;
import java.util.List;

import org.mars_sim.msp.core.Msg;

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

	/** gets the internationalized name for display in user interface. */
	public String getName() {
		return this.name;
	}
	
	/**
	 * gives back a list of all valid values for the HistoricalEventCategory enum.
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

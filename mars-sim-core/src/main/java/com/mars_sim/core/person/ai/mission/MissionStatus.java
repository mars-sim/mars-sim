/*
 * Mars Simulation Project
 * MissionStatus.java
 * @date 2023-07-02
 * @author Manny Kung
 */

package com.mars_sim.core.person.ai.mission;

import com.mars_sim.core.events.HistoricalEventType;
import com.mars_sim.core.goods.GoodsUtil;
import com.mars_sim.core.tool.Msg;

import java.io.Serializable;

public class MissionStatus implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final String MSG_KEY_PREFIX = "mission.status.";
	private static final String MSG_KEY_OLDPREFIX = "Mission.status.";

	/**
	 * Factory helper method to create a status based on a resource.
	 */
	public static MissionStatus createResourceStatus(int missingResourceId) {
		String resourceName = GoodsUtil.getGood(missingResourceId).getName();
		return new MissionStatus("mission.status.noResources", HistoricalEventType.MISSION_NOT_ENOUGH_RESOURCES, resourceName);
	}

	/**
	 * Factory helper method to create a status.
	 */
	public static MissionStatus createProblemStatus(String reason) {
		return new MissionStatus("mission.status.abortedReason", HistoricalEventType.MISSION_PROBLEM, reason);
	}
	
	private String name;
	private HistoricalEventType eventType;

	public MissionStatus(String key) {
		this(key, null);
	}

	public MissionStatus(String key, HistoricalEventType associatedEvent) {
		this.name = Msg.getString(correctKey(key));
		this.eventType = associatedEvent;
	}
	
	public MissionStatus(String key, HistoricalEventType associatedEvent, String argument) {
		this.name  = Msg.getString(correctKey(key), argument);
		this.eventType = associatedEvent;
	}
	
	/**
	 * Corrects the message key to handle old prefixes.
	 * @param key Requested key
	 * @return Correct key to lower case
	 */
	private static String correctKey(String key) {	
		// Hack for the transition phase
		if (key.startsWith(MSG_KEY_OLDPREFIX)) {
			key = MSG_KEY_PREFIX + key.substring(MSG_KEY_OLDPREFIX.length());
		} 
		else if (key.startsWith(MSG_KEY_PREFIX)) {
			// do nothing
		} 
		else {
			key = MSG_KEY_PREFIX + key;
		}
		
		return key;
	}

	public String getName() {
		return this.name;
	}

	/**
	 * Is there an associated historical event type for this mission status.
	 * @return the associated historical event type, or null if none.
	 */
	public HistoricalEventType getEventType() {
		return this.eventType;
	}

	@Override
	public String toString() {
		return this.name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MissionStatus other = (MissionStatus) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}

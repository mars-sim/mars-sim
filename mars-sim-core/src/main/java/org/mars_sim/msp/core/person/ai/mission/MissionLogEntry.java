/*
 * Mars Simulation Project
 * MissionLogEntry.java
 * @date 2021-11-24
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.io.Serializable;

/**
 * Represents a log entry for a mission where the phase changes at a specific time
 */
public class MissionLogEntry implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String time;
	private MissionPhase phase;
	
	public MissionLogEntry(String time, MissionPhase phase) {
		super();
		this.time = time;
		this.phase = phase;
	}

	public String getTime() {
		return time;
	}

	public MissionPhase getPhase() {
		return phase;
	}

	@Override
	public String toString() {
		return "MissionLogEntry [time=" + time + ", phase=" + phase + "]";
	}		
}

/**
 * Mars Simulation Project
 * MissionListener.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission;

/**
 * Interface for a mission event listener.
 */
public interface MissionListener {

	/**
	 * Catch mission update event.
	 * 
	 * @param event the mission event.
	 */
	public void missionUpdate(MissionEvent event);
}

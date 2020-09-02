/**
 * Mars Simulation Project
 * MissionListener.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

/**
 * Listener interface for the mission manager.
 */
public interface MissionManagerListener {

	/**
	 * Adds a new mission.
	 * 
	 * @param mission the new mission.
	 */
	public void addMission(Mission mission);

	/**
	 * Removes an old mission.
	 * 
	 * @param mission the old mission.
	 */
	public void removeMission(Mission mission);
}

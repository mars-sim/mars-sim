/**
 * Mars Simulation Project
 * MissionListener.java
 * @version 2.80 26-08-2006
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.mission;

/**
 * Listener interface for the mission manager.
 */
public interface MissionListener {

	/**
	 * Adds a new mission.
	 * @param mission the new mission.
	 */
	public void addMission(Mission mission);
	
	/**
	 * Removes an old mission.
	 * @param mission the old mission.
	 */
	public void removeMission(Mission mission);
}
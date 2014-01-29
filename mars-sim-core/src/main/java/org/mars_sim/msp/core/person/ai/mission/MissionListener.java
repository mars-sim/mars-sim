/**
 * Mars Simulation Project
 * MissionListener.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

/**
 * Interface for a mission event listener.
 */
public interface MissionListener {
	
	/**
	 * Catch mission update event.
	 * @param event the mission event.
	 */
	public void missionUpdate(MissionEvent event);
}
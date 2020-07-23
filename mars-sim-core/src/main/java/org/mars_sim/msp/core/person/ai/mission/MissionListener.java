/**
 * Mars Simulation Project
 * MissionListener.java
 * @version 3.1.1 2020-07-22
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

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

/**
 * Mars Simulation Project
 * MissionListener.java
 * @version 2.80 06-09-2006
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.mission;

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
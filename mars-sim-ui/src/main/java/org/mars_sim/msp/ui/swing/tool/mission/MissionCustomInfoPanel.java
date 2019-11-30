/**
 * Mars Simulation Project
 * MissionCustomInfoPanel.java
 * @version 3.1.0 2017-11-01
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.mission;

import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionEvent;

import com.alee.laf.panel.WebPanel;

/**
 * A panel for displaying custom mission information.
 */
@SuppressWarnings("serial")
public abstract class MissionCustomInfoPanel
extends WebPanel {

	/**
	 * Updates the panel based on a mission event.
	 * @param e the mission event.
	 */
	public abstract void updateMissionEvent(MissionEvent e);

	/**
	 * Updates the panel based on a new mission to display.
	 * @param mission the mission to display.
	 */
	public abstract void updateMission(Mission mission);
}
/*
 * Mars Simulation Project
 * MissionCustomInfoPanel.java
 * @date 2021-12-20
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.mission;

import javax.swing.JPanel;

import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionEvent;


/**
 * A panel for displaying custom mission information.
 */
@SuppressWarnings("serial")
public abstract class MissionCustomInfoPanel
extends JPanel {

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

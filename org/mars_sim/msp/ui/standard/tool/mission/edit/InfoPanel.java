package org.mars_sim.msp.ui.standard.tool.mission.edit;

import javax.swing.JPanel;

import org.mars_sim.msp.simulation.person.ai.mission.Mission;

public class InfoPanel extends JPanel {

	private Mission mission;
	
	InfoPanel(Mission mission) {
		// Use JPanel constructor.
		super();
		
		this.mission = mission;
	}
}
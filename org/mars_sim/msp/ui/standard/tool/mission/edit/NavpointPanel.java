package org.mars_sim.msp.ui.standard.tool.mission.edit;

import javax.swing.JPanel;

import org.mars_sim.msp.simulation.person.ai.mission.Mission;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;

public class NavpointPanel extends JPanel {
	
	NavpointPanel(Mission mission) {
		// Use JPanel constructor.
		super();
		
		setBorder(new MarsPanelBorder());
	}
}
package org.mars_sim.msp.ui.standard.tool.mission;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;

public class CreateMissionWizard extends JInternalFrame {

	CreateMissionWizard() {
		// Use JInternalFrame constructor
		super("Create Mission Wizard");
		
		add(new JLabel("Create Mission Wizard"));
		
		pack();
		
		setVisible(true);
	}
}
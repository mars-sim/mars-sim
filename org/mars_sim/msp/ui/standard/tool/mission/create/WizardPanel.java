package org.mars_sim.msp.ui.standard.tool.mission.create;

import javax.swing.JPanel;


abstract class WizardPanel extends JPanel {

	private CreateMissionWizard wizard;
	
	WizardPanel(CreateMissionWizard wizard) {
		// Use JPanel constructor.
		super();
		
		this.wizard = wizard;
	}
	
	protected CreateMissionWizard getWizard() {
		return wizard;
	}
	
	abstract String getPanelName();
	
	abstract void commitChanges();
	
	abstract void clearInfo();
	
	abstract void updatePanel();
}
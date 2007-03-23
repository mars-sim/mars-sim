/**
 * Mars Simulation Project
 * WizardPanel.java
 * @version 2.80 2007-03-22
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.mission.create;

import javax.swing.JPanel;

/**
 * An abstract panel for the create mission wizard.
 */
abstract class WizardPanel extends JPanel {

	// Data members.
	private CreateMissionWizard wizard;
	
	/**
	 * Constructor
	 * @param wizard the create mission wizard.
	 */
	WizardPanel(CreateMissionWizard wizard) {
		// Use JPanel constructor.
		super();
		
		// Initialize data members.
		this.wizard = wizard;
	}
	
	/**
	 * Gets the create mission wizard.
	 * @return wizard.
	 */
	protected CreateMissionWizard getWizard() {
		return wizard;
	}
	
	/**
	 * Gets the wizard panel name.
	 * @return panel name.
	 */
	abstract String getPanelName();
	
	/**
	 * Commits changes from this wizard panel.
	 */
	abstract void commitChanges();
	
	/**
	 * Clear information on the wizard panel.
	 */
	abstract void clearInfo();
	
	/**
	 * Updates the wizard panel information.
	 */
	abstract void updatePanel();
}
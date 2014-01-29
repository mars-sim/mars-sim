/**
 * Mars Simulation Project
 * WizardPanel.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.mission.create;

import javax.swing.*;

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
	 * @retun true if changes can be committed.
	 */
	abstract boolean commitChanges();
	
	/**
	 * Clear information on the wizard panel.
	 */
	abstract void clearInfo();
	
	/**
	 * Updates the wizard panel information.
	 */
	abstract void updatePanel();
}
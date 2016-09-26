/**
 * Mars Simulation Project
 * WizardPanel.java
 * @version 3.07 2014-12-06

 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.mission.create;

import javax.swing.*;

/**
 * An abstract panel for the create mission wizard.
 */
abstract class WizardPanel
extends JPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members.
	protected CreateMissionWizard wizard;
	
	/**
	 * Constructor.
	 * @param wizard the create mission wizard.
	 */
	public WizardPanel(CreateMissionWizard wizard) {
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
	 * @deprecated
	 * TODO internationalize the wizzard panel names.
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
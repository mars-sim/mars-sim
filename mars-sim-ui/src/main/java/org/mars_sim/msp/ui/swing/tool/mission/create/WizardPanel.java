/**
 * Mars Simulation Project
 * WizardPanel.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.mission.create;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.ui.swing.StyleManager;

/**
 * An abstract panel for the create mission wizard.
 */
@SuppressWarnings("serial")
abstract class WizardPanel extends JPanel {

	// Static members
	private Simulation sim;

	// Data members.
	protected CreateMissionWizard wizard;

	/**
	 * Constructor.
	 * 
	 * @param wizard the create mission wizard.
	 */
	public WizardPanel(CreateMissionWizard wizard) {
		// Use JPanel constructor.
		super();

		// Initialize data members.
		this.wizard = wizard;
		this.sim = wizard.getDesktop().getSimulation();
	}

	/**
	 * Get the parent simulation
	 */
	protected Simulation getSimulation() {
		return sim;
	}

	/**
	 * Gets the create mission wizard.
	 * 
	 * @return wizard.
	 */
	protected CreateMissionWizard getWizard() {
		return wizard;
	}

	/**
	 * Create a label to be used as the title of a Wizard panel
	 */
	protected static JLabel createTitleLabel(String text) {
		JLabel label = new JLabel(text,	JLabel.CENTER);
		StyleManager.applySubHeading(label);
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
		return label;
	}

	/**
	 * Create an empty label to be used as an error
	 */
	protected static JLabel createErrorLabel() {
		JLabel errorMessageLabel = new JLabel(" ", JLabel.CENTER);
        errorMessageLabel.setForeground(Color.RED);
        errorMessageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		return errorMessageLabel;
	}
	/**
	 * Gets the wizard panel name.
	 * 
	 * @return panel name.
	 */
	abstract String getPanelName();

	/**
	 * Commits changes from this wizard panel.
	 * 
	 * @return true if changes can be committed.
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

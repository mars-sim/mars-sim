/**
 * Mars Simulation Project
 * WizardPanel.java
 * @version 3.1.0 2017-10-05
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.mission.create;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;

import com.alee.laf.panel.WebPanel;

/**
 * An abstract panel for the create mission wizard.
 */
@SuppressWarnings("serial")
abstract class WizardPanel extends WebPanel {
	
	// Static members
	protected static Simulation sim = Simulation.instance();
	protected static UnitManager unitManager = sim.getUnitManager();
	protected static MissionManager missionManager = sim.getMissionManager();
	protected static SurfaceFeatures surfaceFeatures = sim.getMars().getSurfaceFeatures();


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
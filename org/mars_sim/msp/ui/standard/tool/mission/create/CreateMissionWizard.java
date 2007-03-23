/**
 * Mars Simulation Project
 * CreateMissionWizard.java
 * @version 2.80 2007-03-21
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.mission.create;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;

/**
 * A dialog wizard for creating new missions.
 */
public class CreateMissionWizard extends JDialog {
	
	// Static members.
	final static String PREVIOUS_BUTTON = "previous button";
	final static String NEXT_BUTTON = "next button";
	final static String FINAL_BUTTON = "final button";
	
	// Data members
	private JPanel infoPane;
	private JButton prevButton;
	private JButton nextButton;
	private JButton finalButton;
	private MissionDataBean missionBean;
	private List wizardPanels;
	private int displayPanelIndex;
	
	/**
	 * Constructor
	 * @param owner The owner frame.
	 */
	public CreateMissionWizard(Frame owner) {
		// Use JDialog constructor
		super(owner, "Create Mission Wizard", true);
		
		// Set mission data bean.
		missionBean = new MissionDataBean();
		
		// Create info panel.
		infoPane = new JPanel(new CardLayout());
		infoPane.setBorder(new MarsPanelBorder());
		add(infoPane, BorderLayout.CENTER);
		
		// Create wizard panels list.
		wizardPanels = new ArrayList();
		displayPanelIndex = 0;
		
		// Create initial set of wizard panels.
		addWizardPanel(new TypePanel(this));
		addWizardPanel(new StartingSettlementPanel(this));
		addWizardPanel(new VehiclePanel(this));
		addWizardPanel(new MembersPanel(this));
		
		// Create bottom button panel.
		JPanel bottomButtonPane = new JPanel();
		add(bottomButtonPane, BorderLayout.SOUTH);
		
		// Create prevous button.
		prevButton = new JButton("Previous");
		prevButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// Go to previous wizard panel.
						getCurrentWizardPanel().clearInfo();
						displayPanelIndex--;
						CardLayout layout = (CardLayout) infoPane.getLayout();
						layout.show(infoPane, getCurrentWizardPanel().getPanelName());
						nextButton.setEnabled(true);
						if (displayPanelIndex == 0) prevButton.setEnabled(false);
					}
				});
		prevButton.setEnabled(false);
		bottomButtonPane.add(prevButton);
		
		// Create next button.
		nextButton = new JButton("Next");
		nextButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// Go to next wizard panel.
						getCurrentWizardPanel().commitChanges();
						displayPanelIndex++;
						CardLayout layout = (CardLayout) infoPane.getLayout();
						WizardPanel currentPanel = getCurrentWizardPanel();
						currentPanel.updatePanel();
						layout.show(infoPane, currentPanel.getPanelName());
						prevButton.setEnabled(true);
						nextButton.setEnabled(false);
					}
				});
		nextButton.setEnabled(false);
		bottomButtonPane.add(nextButton);
		
		// Create final button.
		finalButton = new JButton("Final");
		finalButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// Create mission and dispose this dialog.
						getCurrentWizardPanel().commitChanges();
						dispose();
					}
				});
		finalButton.setEnabled(false);
		bottomButtonPane.add(finalButton);
		
		// Create cancel button.
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(
				new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
        				// Dispose this dialog.
        				dispose();
        			}
				});
		bottomButtonPane.add(cancelButton);
		
		// Finish and display wizard.
		pack();
		setLocationRelativeTo(owner);
		setResizable(false);
		setVisible(true);
	}
	
	/**
	 * Gets the current displayed wizard panel.
	 * @return wizard panel.
	 */
	private WizardPanel getCurrentWizardPanel() {
		return (WizardPanel) wizardPanels.get(displayPanelIndex);
	}
	
	/**
	 * Sets the final wizard panel for the mission type.
	 */
	void setFinalWizardPanels() {
		// Remove old final panels if any.
		for (int x = 4; x < wizardPanels.size(); x++) wizardPanels.remove(x);
		
		// Add mission type appropriate final panels.
		if (missionBean.getType().equals(MissionDataBean.TRAVEL_MISSION)) 
			addWizardPanel(new DestinationSettlementPanel(this));
		else if (missionBean.getType().equals(MissionDataBean.RESCUE_MISSION)) 
			addWizardPanel(new RendezvousVehiclePanel(this));
		else if (missionBean.getType().equals(MissionDataBean.ICE_MISSION))
			addWizardPanel(new ProspectingSitePanel(this));
		else if (missionBean.getType().equals(MissionDataBean.EXPLORATION_MISSION))
			addWizardPanel(new ExplorationSitesPanel(this));
	}
	
	/**
	 * Adds a wizard panel to the list.
	 * @param newWizardPanel the wizard panel to add.
	 */
	private void addWizardPanel(WizardPanel newWizardPanel) {
		wizardPanels.add(newWizardPanel);
		infoPane.add(newWizardPanel, newWizardPanel.getPanelName());
	}
	
	/** 
	 * Get the mission data bean.
	 * @return mission data bean.
	 */
	MissionDataBean getMissionData() {
		return missionBean;
	}
	
	/**
	 * Enables a button.
	 * @param buttonType the type of button.
	 * @param enabled true if button is enabled.
	 */
	void setButtonEnabled(String buttonType, boolean enabled) {
		if (PREVIOUS_BUTTON.equals(buttonType)) prevButton.setEnabled(enabled);
		else if (NEXT_BUTTON.equals(buttonType)) nextButton.setEnabled(enabled);
		else if (FINAL_BUTTON.equals(buttonType)) finalButton.setEnabled(enabled);
	}
}
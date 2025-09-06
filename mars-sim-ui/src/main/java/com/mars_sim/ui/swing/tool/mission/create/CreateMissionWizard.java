/*
 * Mars Simulation Project
 * CreateMissionWizard.java
 * @date 2021-08-28
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.tool.mission.create;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.MarsPanelBorder;
import com.mars_sim.ui.swing.ModalInternalFrame;
import com.mars_sim.ui.swing.tool.mission.MissionWindow;

/**
 * A wizard for creating new missions.
 */
@SuppressWarnings("serial")
public class CreateMissionWizard
extends ModalInternalFrame
implements ActionListener {

	// Data members
	private int displayPanelIndex;
	
	private JPanel infoPane;
	private JButton prevButton;
	private JButton nextButton;
	private JButton finalButton;
	
	private MissionDataBean missionBean;
	private TypePanel typePanel;
	private MissionWindow missionWindow;
	
	private MainDesktopPane desktop;
	
	private List<WizardPanel> wizardPanels;

	/**
	 * Constructor.
	 * 
	 * @param missionWindow The owner frame.
	 */
	public CreateMissionWizard(MainDesktopPane desktop, MissionWindow missionWindow) {
		// Use ModalInternalFrame constructor
        super("Create Mission Wizard");

        this.missionWindow = missionWindow;
        this.desktop = desktop;
        
		// Set mission data bean.
		missionBean = new MissionDataBean();

		// Create info panel.
		infoPane = new JPanel(new CardLayout());
		infoPane.setBorder(new MarsPanelBorder());

		add(infoPane, BorderLayout.CENTER);

		// Create wizard panels list.
		wizardPanels = new ArrayList<>();
		displayPanelIndex = 0;

		// Create initial set of wizard panels.
		typePanel = new TypePanel(this);
		addWizardPanel(typePanel);

        // Note: This panel is added so that next and final buttons are
        // enabled/disabled properly initially.
        addWizardPanel(new StartingSettlementPanel(this));

		// Create bottom button panel.
		JPanel bottomButtonPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		add(bottomButtonPane, BorderLayout.SOUTH);

		// Create previous button.
		prevButton = new JButton("Previous");
		prevButton.addActionListener(this);
		prevButton.setEnabled(false);
		bottomButtonPane.add(prevButton);

		// Create next button.
		nextButton = new JButton("Next");
		nextButton.addActionListener(this);
		nextButton.setEnabled(false);
		bottomButtonPane.add(nextButton);

		// Create final button.
		finalButton = new JButton("Final");
		finalButton.addActionListener(this);
		finalButton.setEnabled(false);
		bottomButtonPane.add(finalButton);

		// Create cancel button.
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e -> dispose());
		bottomButtonPane.add(cancelButton);

		// Finish and display wizard.
        setSize(new Dimension(700, 600));
        
		// Set the icon
		setIconImage();
		
        desktop.add(this); 
        	
		Dimension desktopSize = desktop.getParent().getSize();
	    Dimension jInternalFrameSize = this.getSize();
	    int width = (desktopSize.width - jInternalFrameSize.width) / 2;
	    int height = (desktopSize.height - jInternalFrameSize.height) / 2;
	    setLocation(width, height);
	    
	    setModal(true);
	    setVisible(true);
	}

	/**
	 * Gets the current displayed wizard panel.
	 * 
	 * @return wizard panel.
	 */
	private WizardPanel getCurrentWizardPanel() {
		return wizardPanels.get(displayPanelIndex);
	}

	/**
	 * Sets the final wizard panel for the mission type.
	 */
	void setFinalWizardPanels() {
		// Remove old final panels if any.
		int numPanels = wizardPanels.size();
		for (int x = 1; x < numPanels; x++)
			wizardPanels.remove(1);

		// Add mission type appropriate final panels.
		if (missionBean.isRemoteMission()) {
			addWizardPanel(new StartingSettlementPanel(this));
		}
		else {
			addWizardPanel(new ConstructionSettlementPanel(this));
		}

		if (missionBean.isScientificMission()) {
			addWizardPanel(new StudyPanel(this));
			addWizardPanel(new LeadResearcherPanel(this));
		}
		if (missionBean.isRemoteMission()) {
			if (missionBean.isDeliveryMission())
				addWizardPanel(new FlyerPanel(this));
			else
				addWizardPanel(new VehiclePanel(this));
		}
		
		// Note: Change members panel to use lead researcher as member.
		addWizardPanel(new MembersPanel(this));
		
		if (missionBean.isDeliveryMission()) {
			addWizardPanel(new BotMembersPanel(this));
		}
	
		// Choose the remote location of the mission
		if (missionBean.requiresFieldSite()) {
			addWizardPanel(new FieldSitePanel(this));
		} 
		
		if (missionBean.isMiningMission() ) {
			addWizardPanel(new MiningSitePanel(this));
	    } else if (missionBean.isProspectingMission()) {
			addWizardPanel(new ProspectingSitePanel(this));
		} else if(missionBean.isExplorationMission()) {
			addWizardPanel(new ExplorationSitesPanel(this));
		} else if (missionBean.requiresDestinationSettlement()) {
			addWizardPanel(new DestinationSettlementPanel(this));
		} else if(missionBean.isRescueRendezvousMission()) {
			addWizardPanel(new RendezvousVehiclePanel(this));
		}
		
		// The cargo of the mission
		if (missionBean.isEmergencySupplyMission()) {
			addWizardPanel(new EmergencySupplyPanel(this));
		} else if (missionBean.isTradeMission()) {
			addWizardPanel(new TradeGoodsPanel(this, false));
			addWizardPanel(new TradeGoodsPanel(this, true));
		} else if (missionBean.isDeliveryMission()) {
			addWizardPanel(new DeliveryGoodsPanel(this, false));
			addWizardPanel(new DeliveryGoodsPanel(this, true));			
		}
		
		// Set building construction or building salvage projects
		else if (missionBean.isConstructionMission()) {
			addWizardPanel(new ConstructionProjectPanel(this));
            addWizardPanel(new ConstructionVehiclePanel(this));
		}
	}

	/**
	 * Adds a wizard panel to the list.
	 * 
	 * @param newWizardPanel the wizard panel to add.
	 */
	private void addWizardPanel(WizardPanel newWizardPanel) {
		wizardPanels.add(newWizardPanel);
		infoPane.add(newWizardPanel, newWizardPanel.getPanelName());
	}

	/**
	 * Gets the mission data bean.
	 * 
	 * @return mission data bean.
	 */
	MissionDataBean getMissionData() {
		return missionBean;
	}

	/**
	 * Sets previous, next and final buttons to be enabled or disabled.
	 * 
	 * @param nextEnabled true if next/final button is enabled.
	 */
	void setButtons(boolean nextEnabled) {

		// Enable previous button if after first panel.
		prevButton.setEnabled(displayPanelIndex > 0);

		if (nextEnabled) {
			nextButton.setEnabled(displayPanelIndex < (wizardPanels.size() - 1));
			finalButton.setEnabled(displayPanelIndex == (wizardPanels.size() - 1));
		}
		else {
			nextButton.setEnabled(false);
			finalButton.setEnabled(false);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == prevButton) buttonClickedPrev();
		else if (source == nextButton) buttonClickedNext();
		else if (source == finalButton) buttonClickedFinal();
	}

	/** 
	 * Goes to previous wizard panel. 
	 */
	public void buttonClickedPrev() {
		getCurrentWizardPanel().clearInfo();
		displayPanelIndex--;
		CardLayout layout = (CardLayout) infoPane.getLayout();
		layout.show(infoPane, getCurrentWizardPanel().getPanelName());
		nextButton.setEnabled(true);
		if (displayPanelIndex == 0) prevButton.setEnabled(false);
	}

	/** 
	 * Goes to next wizard panel. 
	 */
	public void buttonClickedNext() {
		if (getCurrentWizardPanel().commitChanges(true)) {
			displayPanelIndex++;
			setButtons(false);
			CardLayout layout = (CardLayout) infoPane.getLayout();
			WizardPanel currentPanel = getCurrentWizardPanel();
			currentPanel.updatePanel();
			layout.show(infoPane, currentPanel.getPanelName());
		}
	}

	/**
	 * Finalizes the button click.
	 */
	public void buttonClickedFinal() {
		if (getCurrentWizardPanel().commitChanges(false)) {
			missionBean.createMission();
			dispose();
		}
	}
	
	
	public TypePanel getTypePanel() {
		return typePanel;
	}
	
	public MissionWindow getMissionWindow() {
		return missionWindow;
	}

	//Does the same as getMissionData, what is the difference?
	public MissionDataBean getMissionBean() {
		return missionBean;
	}
	
	public MainDesktopPane getDesktop() {
		return desktop;
	}
}

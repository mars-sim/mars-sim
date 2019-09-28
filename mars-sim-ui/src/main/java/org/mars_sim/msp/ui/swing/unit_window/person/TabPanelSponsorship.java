/**
 * Mars Simulation Project
 * TabPanelSponsorship.java
 * @version 3.1.0 2017-10-18
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityType;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

//import com.alee.managers.language.data.TooltipWay;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;

/**
 * The TabPanelSponsorship is a tab panel for showing the Reporting Authority and mission objective of a settler.
 */
@SuppressWarnings("serial")
public class TabPanelSponsorship
extends TabPanel {

	/** Is UI constructed. */
	private boolean uiDone = false;
	
	/** The Person instance. */
	private Person person = null;
	
	/**
	 * Constructor.
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelSponsorship(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelSponsorship.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelSponsorship.tooltip"), //$NON-NLS-1$
			unit, desktop
		);

		//TabPanelSponsorship.missionControl		= Sponsored Organization : {0}
		//TabPanelSponsorship.missionObjective		= Mission Objective : {0}

		person = (Person) unit;
	}
	
	public boolean isUIDone() {
		return uiDone;
	}
	
	public void initializeUI() {
		uiDone = true;
		
		// Create general label panel.
		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(labelPanel);

		// Prepare general label
		JLabel titleLabel = new JLabel(Msg.getString("TabPanelSponsorship.label"), JLabel.CENTER); //$NON-NLS-1$
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		labelPanel.add(titleLabel);

		// Prepare spring layout info panel.
		JPanel infoPanel = new JPanel(new SpringLayout());//GridLayout(2, 2, 0, 0));
//		infoPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(infoPanel, BorderLayout.NORTH);

		// Prepare sponsor name label
		JLabel sponsorNameLabel = new JLabel(Msg.getString("TabPanelSponsorship.sponsor"), JLabel.RIGHT); //$NON-NLS-1$
		//sponsorNameLabel.setSize(2, 2);
		infoPanel.add(sponsorNameLabel);

		// Prepare sponsor label
		JTextField sponsorTF = new JTextField();
		ReportingAuthorityType sponsor = null;
		if (person.getReportingAuthority() != null) {
		    sponsor = person.getReportingAuthority().getOrg();
		    sponsorTF.setText(sponsor+""); // Conversion.capitalize(sponsor)
		}
		sponsorTF.setEditable(false);
		sponsorTF.setColumns(16);
		sponsorTF.setCaretPosition(0);
		if (person.getReportingAuthority() != null) {
		    //sponsorTF.setToolTipText(person.getReportingAuthority().getToolTipStr());
			TooltipManager.setTooltip (sponsorTF, person.getReportingAuthority().getToolTipStr(), TooltipWay.down);
		}
		//JLabel sponsorLabel = new JLabel(sponsor, JLabel.RIGHT);
		infoPanel.add(sponsorTF);


		// Prepare birth location name label
		JLabel objectiveNameLabel = new JLabel(Msg.getString("TabPanelSponsorship.objective"), JLabel.RIGHT); //$NON-NLS-1$
		//objectiveNameLabel.setSize(2, 2);
		infoPanel.add(objectiveNameLabel);

		// Prepare birth location label
		String objective = null;
		JTextField objectiveTF = new JTextField();
		if (person.getReportingAuthority() != null) {
			objective = person.getReportingAuthority().getMissionAgenda().getObjectiveName();
		}
		//JLabel objectiveLabel = new JLabel(objective, JLabel.RIGHT);
		objectiveTF.setText(Conversion.capitalize(objective));
		objectiveTF.setEditable(false);
		objectiveTF.setColumns(16);
		objectiveTF.setCaretPosition(0);
		infoPanel.add(objectiveTF);

		//Lay out the spring panel.
		SpringUtilities.makeCompactGrid(infoPanel,
		                                2, 2, //rows, cols
		                                20, 10,        //initX, initY
		                                10, 10);       //xPad, yPad
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		if (!uiDone)
			initializeUI();
		
		// Person person = (Person) unit;
		// Fill in as we have more to update on this panel.
	}
}
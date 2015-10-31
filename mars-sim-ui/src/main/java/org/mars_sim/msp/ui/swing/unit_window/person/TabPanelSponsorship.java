/**
 * Mars Simulation Project
 * TabPanelSponsorship.java
 * @version 3.08 2015-10-05
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

/**
 * The TabPanelTabPanel is a tab panel for showing the Reporting Authority and mission objective of a settler.
 */
public class TabPanelSponsorship
extends TabPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

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

		// TabPanelSponsorship.missionControl		= Sponsored Organization : {0}
		//TabPanelSponsorship.missionObjective		= Mission Objective : {0}

		Person person = (Person) unit;

		// Create general label panel.
		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(labelPanel);

		// Prepare general label
		JLabel titleLabel = new JLabel(Msg.getString("TabPanelSponsorship.label"), JLabel.CENTER); //$NON-NLS-1$
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		labelPanel.add(titleLabel);

		// Prepare info panel.
		JPanel infoPanel = new JPanel(new GridLayout(2, 2, 0, 0));
		infoPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(infoPanel, BorderLayout.NORTH);

		// Prepare sponsor name label
		JLabel sponsorNameLabel = new JLabel(Msg.getString("TabPanelSponsorship.sponsor"), JLabel.RIGHT); //$NON-NLS-1$
		sponsorNameLabel.setSize(5, 2);
		infoPanel.add(sponsorNameLabel);

		// Prepare sponsor label
		String sponsor = person.getReportingAuthority().getName();
		JTextField sponsorTF = new JTextField(sponsor); // Conversion.capitalize(sponsor)
		sponsorTF.setEditable(false);
		sponsorTF.setColumns(20);
		sponsorTF.setCaretPosition(0);
		sponsorTF.setToolTipText(person.getReportingAuthority().getToolTipStr());
		//JLabel sponsorLabel = new JLabel(sponsor, JLabel.RIGHT);
		infoPanel.add(sponsorTF);


		// Prepare birth location name label
		JLabel objectiveNameLabel = new JLabel(Msg.getString("TabPanelSponsorship.objective"), JLabel.RIGHT); //$NON-NLS-1$
		objectiveNameLabel.setSize(5, 2);
		infoPanel.add(objectiveNameLabel);

		// Prepare birth location label
		String objective = person.getReportingAuthority().getMissionAgenda().getObjectiveName();
		//JLabel objectiveLabel = new JLabel(objective, JLabel.RIGHT);
		JTextField objectiveTF = new JTextField(Conversion.capitalize(objective));
		objectiveTF.setEditable(false);
		objectiveTF.setColumns(20);
		objectiveTF.setCaretPosition(0);
		infoPanel.add(objectiveTF);

	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		// Person person = (Person) unit;
		// Fill in as we have more to update on this panel.
	}
}
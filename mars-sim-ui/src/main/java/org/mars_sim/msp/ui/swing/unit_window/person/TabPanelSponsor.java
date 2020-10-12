/**
 * Mars Simulation Project
 * TabPanelSponsor.java
 * @version 3.1.2 2020-09-02
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
 * The TabPanelSponsor is a tab panel for showing the Reporting Authority and mission objective of a settler.
 */
@SuppressWarnings("serial")
public class TabPanelSponsor
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
	public TabPanelSponsor(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelSponsor.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelSponsor.tooltip"), //$NON-NLS-1$
			unit, desktop
		);

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
		JLabel titleLabel = new JLabel(Msg.getString("TabPanelSponsor.label"), JLabel.CENTER); //$NON-NLS-1$
		titleLabel.setFont(new Font("Serif", Font.BOLD, 14));
		labelPanel.add(titleLabel);

		// Prepare spring layout info panel.
		JPanel infoPanel = new JPanel(new SpringLayout());//GridLayout(2, 2, 0, 0));
//		infoPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(infoPanel, BorderLayout.NORTH);

		// Prepare sponsor name label
		JLabel sponsorNameLabel = new JLabel(Msg.getString("TabPanelSponsor.sponsor"), JLabel.RIGHT); //$NON-NLS-1$
		//sponsorNameLabel.setSize(2, 2);
		infoPanel.add(sponsorNameLabel);

		// Prepare sponsor label
		JTextField sponsorTF = new JTextField();
		ReportingAuthorityType sponsor = null;
		if (person.getReportingAuthority() != null) {
		    sponsor = person.getReportingAuthority().getOrg();
		    sponsorTF.setText(sponsor + ""); 
		}
		sponsorTF.setEditable(false);
		sponsorTF.setColumns(8);
		sponsorTF.setCaretPosition(0);
		if (person.getReportingAuthority() != null) {
			TooltipManager.setTooltip (sponsorTF, person.getReportingAuthority().getToolTipStr() 
					+ " (" + sponsor + ")", TooltipWay.down);
		}
		infoPanel.add(sponsorTF);

		// Prepare birth location name label
		JLabel objectiveNameLabel = new JLabel(Msg.getString("TabPanelSponsor.objective"), JLabel.RIGHT); //$NON-NLS-1$
		//objectiveNameLabel.setSize(2, 2);
		infoPanel.add(objectiveNameLabel);

		// Prepare birth location label
		String objective = null;
		JTextField objectiveTF = new JTextField();
		if (person.getReportingAuthority() != null) {
			objective = person.getReportingAuthority().getMissionAgenda().getObjectiveName();
			TooltipManager.setTooltip (objectiveTF, Conversion.capitalize(objective), TooltipWay.down);

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
		                                10, 4);       //xPad, yPad
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		if (!uiDone)
			initializeUI();

		// Fill in as we have more to update on this panel.
	}
}

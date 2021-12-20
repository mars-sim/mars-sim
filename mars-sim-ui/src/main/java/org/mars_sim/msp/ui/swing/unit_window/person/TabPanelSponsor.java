/**
 * Mars Simulation Project
 * TabPanelSponsor.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthority;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;

/**
 * The TabPanelSponsor is a tab panel for showing the Reporting Authority and mission objective of a settler.
 */
@SuppressWarnings("serial")
public class TabPanelSponsor
extends TabPanel {

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
	
	@Override
	protected void buildUI(JPanel content) {

		// Prepare spring layout info panel.
		JPanel infoPanel = new JPanel(new SpringLayout());
		content.add(infoPanel, BorderLayout.NORTH);

		// Prepare sponsor name label
		JLabel sponsorNameLabel = new JLabel(Msg.getString("TabPanelSponsor.sponsor"), JLabel.RIGHT); //$NON-NLS-1$
		infoPanel.add(sponsorNameLabel);

		// Prepare sponsor label
		JTextField sponsorTF = new JTextField();
		ReportingAuthority sponsor = person.getReportingAuthority();
		if (sponsor != null) {
		    sponsorTF.setText(sponsor.getName()); 
			TooltipManager.setTooltip (sponsorTF, 
					sponsor.getDescription(),
					TooltipWay.down);
		}
		sponsorTF.setEditable(false);
		sponsorTF.setColumns(8);
		sponsorTF.setCaretPosition(0);
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
}

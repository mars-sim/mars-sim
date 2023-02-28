/*
 * Mars Simulation Project
 * TabPanelSponsorship.java
 * @date 2022-07-09
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.stream.Collectors;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.reportingAuthority.MissionSubAgenda;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthority;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;

/**
 * The TabPanelSponsorship is a tab panel for showing the settlement's 
 * sponsor and its objective.
 */
@SuppressWarnings("serial")
public class SponsorTabPanel extends TabPanel {
	
	private static final String SPONSOR_ICON = "sponsor";
	private ReportingAuthority ra;
	
	
	/**
	 * Constructor.
	 * @param settlement the settlement.
	 * @param desktop the main desktop.
	 */
	public SponsorTabPanel(ReportingAuthority ra, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			null,
			ImageLoader.getIconByName(SPONSOR_ICON),
			Msg.getString("TabPanelSponsorship.title"), //$NON-NLS-1$
			desktop
		);

		this.ra = ra;
	}

	@Override
	protected void buildUI(JPanel content) {

		// Prepare spring layout info panel.
		JPanel infoPanel = new JPanel(new SpringLayout());
		content.add(infoPanel, BorderLayout.NORTH);
		
		// Prepare sponsor name label
		JLabel sponsorNameLabel = new JLabel(Msg.getString("TabPanelSponsorship.sponsor"), JLabel.RIGHT); //$NON-NLS-1$
		infoPanel.add(sponsorNameLabel);

		// Prepare sponsor label
		JTextField sponsorTF = new JTextField();
		sponsorTF.setText(ra.getName()); 
		sponsorTF.setEditable(false);
		sponsorTF.setColumns(8);
		sponsorTF.setCaretPosition(0);
		sponsorTF.setToolTipText (ra.getDescription());
		infoPanel.add(sponsorTF);
		
		// Prepare obj name label
		JLabel objectiveNameLabel = new JLabel(Msg.getString("TabPanelSponsorship.objective"), JLabel.RIGHT); //$NON-NLS-1$
		infoPanel.add(objectiveNameLabel);

		// Prepare obj tf
		JTextField objectiveTF = new JTextField();
		String objective = ra.getMissionAgenda().getObjectiveName();
		
		objectiveTF.setText(objective);
		objectiveTF.setEditable(false);
		objectiveTF.setColumns(16);
		objectiveTF.setCaretPosition(0);
		infoPanel.add(objectiveTF);
		
		//Lay out the spring panel.
		SpringUtilities.makeCompactGrid(infoPanel,
		                                2, 2, //rows, cols
		                                20, 10,        //initX, initY
		                                10, 4);       //xPad, yPad
		
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		addBorder(panel, "Mission Agendas");
		
		JTextArea ta = new JTextArea();
		ta.setEditable(false);
		ta.setColumns (30);
		ta.setLineWrap (true);
		ta.setBorder(new MarsPanelBorder());
		panel.add(ta);
		
		// For each phase, add to the text area.
		ta.append(ra.getMissionAgenda().getAgendas().stream()
				.map(MissionSubAgenda::getDescription)
				.collect(Collectors.joining("\n")));

		content.add(panel, BorderLayout.CENTER);
	}
}

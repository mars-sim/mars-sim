/*
 * Mars Simulation Project
 * SponsorTabPanel.java
 * @date 2023-05-31
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextArea;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.reportingAuthority.MissionCapability;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthority;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;

/**
 * The SponsorTabPanel is a tab panel for showing the settlement's 
 * sponsor and its objective.
 */
@SuppressWarnings("serial")
public class SponsorTabPanel extends TabPanel {
	
	private static final String AGENCY_ICON = "agency/";
	private static final String TAB_ICON = "sponsor";
	
	private Icon agencyIcon;
	private ReportingAuthority ra;
	
	
	/**
	 * Constructor.
	 * 
	 * @param settlement the settlement.
	 * @param desktop the main desktop.
	 */
	public SponsorTabPanel(ReportingAuthority ra, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("SponsorTabPanel.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(TAB_ICON),
			Msg.getString("SponsorTabPanel.title"), //$NON-NLS-1$
			desktop
		);

		String agencyStr = ra.getName();
	
		agencyIcon = ImageLoader.getIconByName(AGENCY_ICON + agencyStr);
		this.ra = ra;
	}

	@Override
	protected void buildUI(JPanel content) {

		JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		content.add(iconPanel, BorderLayout.NORTH);
		
		JLabel agencyLabel = new JLabel(agencyIcon);
		agencyLabel.setSize(150, 150);
		iconPanel.add(agencyLabel);
		
		// Prepare info panel.
		AttributePanel infoPanel = new AttributePanel(3);
		content.add(infoPanel, BorderLayout.CENTER);
		
		// Prepare sponsor name label
		infoPanel.addTextField(Msg.getString("SponsorTabPanel.sponsor"), ra.getDescription(), null);
		// Prepare country name label
		infoPanel.addTextField(Msg.getString("SponsorTabPanel.country"), ra.getDefaultCountry(), null);
		// Prepare agenda name label
		infoPanel.addTextField(Msg.getString("SponsorTabPanel.agenda"), ra.getMissionAgenda().getName(), null);
		


//		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
//		content.add(panel, BorderLayout.SOUTH);
		
		JPanel subPanel = new JPanel(new BorderLayout());
		content.add(subPanel, BorderLayout.SOUTH);
//		panel.add(subPanel);
		
		JPanel panelNorth = new JPanel(new BorderLayout());
		subPanel.add(panelNorth, BorderLayout.NORTH);
		
		JPanel panelCenter = new JPanel(new BorderLayout());
		subPanel.add(panelCenter, BorderLayout.CENTER);
		
		
		JPanel panel0 = new JPanel(new BorderLayout());
		panelNorth.add(panel0, BorderLayout.NORTH);
		
		addBorder(panel0, Msg.getString("SponsorTabPanel.objective"));
		
		// For each phase, add to the text area.
		createTA(panel0).append(ra.getMissionAgenda().getObjectiveName());
		
		
		JPanel panel1 = new JPanel(new BorderLayout());
		panelNorth.add(panel1, BorderLayout.CENTER);
		
		addBorder(panel1, Msg.getString("SponsorTabPanel.report"));
		
		// For each phase, add to the text area.
		createTA(panel1).append(ra.getMissionAgenda().getReports());
		
		
		JPanel panel2 = new JPanel(new BorderLayout());
		panelNorth.add(panel2, BorderLayout.SOUTH);
		
		addBorder(panel2, Msg.getString("SponsorTabPanel.data"));
		
		// For each phase, add to the text area.
		createTA(panel2).append(ra.getMissionAgenda().getData());
		
		
		
		JPanel panelCap = new JPanel(new BorderLayout());
		panelCenter.add(panelCap, BorderLayout.SOUTH);
		
		addBorder(panelCap, Msg.getString("SponsorTabPanel.capability"));
		
		// For each phase, add to the text area.
		createTA(panelCap).append(ra.getMissionAgenda().getCapabilities().stream()
				.map(MissionCapability::getDescription)
				.collect(Collectors.joining("\n")));
	}
	
	private JTextArea createTA(JPanel panel) {
		JTextArea ta = new JTextArea();
		ta.setEditable(false);
		ta.setColumns (30);
		ta.setLineWrap (true);
//		ta.setBorder(new MarsPanelBorder());
		panel.add(ta);
		return ta;
	}
}

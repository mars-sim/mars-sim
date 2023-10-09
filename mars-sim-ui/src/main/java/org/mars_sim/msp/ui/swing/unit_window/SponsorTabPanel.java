/*
 * Mars Simulation Project
 * SponsorTabPanel.java
 * @date 2023-05-31
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import org.mars.sim.tools.Msg;
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
	
	private static final String AGENCY_FOLDER = "agency/";
	private static final String TAB_ICON = "sponsor";

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

		this.ra = ra;
	}

	@Override
	protected void buildUI(JPanel content) {

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		content.add(mainPanel, BorderLayout.NORTH);
		
		//////////////////////////////////////////////////
		
		JPanel namePanel = new JPanel(new BorderLayout());
		
		namePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		mainPanel.add(namePanel, BorderLayout.NORTH);
		
		addBorder(namePanel, Msg.getString("SponsorTabPanel.sponsor"));
		
		//////////////////////////////////////////////////
		
		JLabel longNameLabel = new JLabel(ra.getDescription(), JLabel.CENTER);
		longNameLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
		namePanel.add(longNameLabel, BorderLayout.NORTH);
		
		JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
//		iconPanel.setPreferredSize(new Dimension(90, 90));	
		iconPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		namePanel.add(iconPanel, BorderLayout.CENTER);
		
		String agencyStr = ra.getName();
		Image img = (ImageLoader.getImage(AGENCY_FOLDER + agencyStr))
				.getScaledInstance(90, 90,
		        Image.SCALE_SMOOTH);
		
		JLabel agencyLabel = new JLabel(new ImageIcon(img));
//		agencyLabel.setPreferredSize(new Dimension(90, 90));
		iconPanel.add(agencyLabel);
		
		// Prepare info name panel.
		AttributePanel infoNamePanel = new AttributePanel(3);
		namePanel.add(infoNamePanel, BorderLayout.SOUTH);

		// Prepare sponsor long name label
//		infoNamePanel.addRow(Msg.getString("SponsorTabPanel.sponsorLong"), ra.getDescription());
		// Prepare sponsor short name label
		infoNamePanel.addRow(Msg.getString("SponsorTabPanel.sponsorShort"), ra.getName());
		
		boolean isCorp = ra.isCorporation();
		String corpLabel = "No";
		if (isCorp)
			corpLabel = "Yes";
		// Prepare corporation label
		infoNamePanel.addRow(Msg.getString("SponsorTabPanel.corporation"), corpLabel);
		// Prepare agenda name label
		infoNamePanel.addRow(Msg.getString("SponsorTabPanel.agenda"), ra.getMissionAgenda().getName());
		
		//////////////////////////////////////////////////
		
		JPanel subPanel = new JPanel(new BorderLayout());
		mainPanel.add(subPanel, BorderLayout.SOUTH);
	
		JPanel panelNorth = new JPanel(new GridLayout(3, 1));
		subPanel.add(panelNorth, BorderLayout.NORTH);
		
		//////////////////////////////////////////////////
		
		
		JPanel panel0 = new JPanel(new FlowLayout());
		panelNorth.add(panel0);
		
		addBorder(panel0, Msg.getString("SponsorTabPanel.objective"));
		
		// For each phase, add to the text area.
		createTA(panel0).append("- " + ra.getMissionAgenda().getObjectiveName());
		
		
		JPanel panel1 = new JPanel(new FlowLayout());
		panelNorth.add(panel1);
		
		addBorder(panel1, Msg.getString("SponsorTabPanel.report"));
		
		// For each phase, add to the text area.
		createTA(panel1).append("- " + ra.getMissionAgenda().getReports());
		
		
		JPanel panel2 = new JPanel(new FlowLayout());
		panelNorth.add(panel2);
		
		addBorder(panel2, Msg.getString("SponsorTabPanel.data"));
		
		// For each phase, add to the text area.
		createTA(panel2).append("- " + ra.getMissionAgenda().getData());
		
		/////////////////////////////////////////////////////////
		
		JPanel panelCenter = new JPanel(new BorderLayout());
		subPanel.add(panelCenter, BorderLayout.CENTER);
		
		JPanel panelCap = new JPanel(new FlowLayout());
		panelCenter.add(panelCap);
		
		addBorder(panelCap, Msg.getString("SponsorTabPanel.capability"));
		
		// For each phase, add to the text area.
		createTA(panelCap).append(ra.getMissionAgenda().getCapabilities()
				.stream()
				.map(MissionCapability::getHyphenatedDescription)
				.collect(Collectors.joining("\n")));
	}
	
	private JTextArea createTA(JPanel panel) {
		JTextArea ta = new JTextArea();
		ta.setEditable(false);
		ta.setColumns(35);
		ta.setLineWrap(true);
		ta.setWrapStyleWord(true);
		panel.add(ta);
		return ta;
	}
}

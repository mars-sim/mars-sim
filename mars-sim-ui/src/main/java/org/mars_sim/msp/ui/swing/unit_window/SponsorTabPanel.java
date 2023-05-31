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

import javax.swing.JPanel;
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
			Msg.getString("SponsorTabPanel.title"), //$NON-NLS-1$
			desktop
		);

		this.ra = ra;
	}

	@Override
	protected void buildUI(JPanel content) {

		// Prepare spring layout info panel.
		AttributePanel infoPanel = new AttributePanel(2);
		content.add(infoPanel, BorderLayout.NORTH);
		
		// Prepare sponsor name label
		infoPanel.addTextField(Msg.getString("SponsorTabPanel.sponsor"), ra.getName(), ra.getDescription());
		
		// Prepare agenda name label
		infoPanel.addTextField(Msg.getString("SponsorTabPanel.agenda"), ra.getMissionAgenda().getName(), null);
		
		
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		content.add(panel, BorderLayout.CENTER);
		
		JPanel subPanel = new JPanel(new BorderLayout());
		panel.add(subPanel);
		
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

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

import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.reportingAuthority.MissionSubAgenda;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthority;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;

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
		AttributePanel infoPanel = new AttributePanel(2);
		content.add(infoPanel, BorderLayout.NORTH);
		
		// Prepare sponsor name label
		infoPanel.addTextField(Msg.getString("TabPanelSponsorship.sponsor"), ra.getName(), ra.getDescription());
		
		// Prepare obj name label
		infoPanel.addTextField(Msg.getString("TabPanelSponsorship.objective"), ra.getMissionAgenda().getObjectiveName(),
									null);
		
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

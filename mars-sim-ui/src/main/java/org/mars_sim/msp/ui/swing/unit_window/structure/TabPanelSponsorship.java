/*
 * Mars Simulation Project
 * TabPanelSponsorship.java
 * @date 2022-07-09
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.stream.Collectors;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.reportingAuthority.MissionSubAgenda;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthority;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.text.WebTextArea;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;

/**
 * The TabPanelSponsorship is a tab panel for showing the settlement's 
 * sponsor and its objective.
 */
@SuppressWarnings("serial")
public class TabPanelSponsorship extends TabPanel {
	
	private static final String EARTH_ICON = Msg.getString("icon.earth"); //$NON-NLS-1$
	
	/** The Settlement instance. */
	private Settlement settlement;
	
	/**
	 * Constructor.
	 * @param settlement the settlement.
	 * @param desktop the main desktop.
	 */
	public TabPanelSponsorship(Settlement settlement, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			null,
			ImageLoader.getNewIcon(EARTH_ICON),
			Msg.getString("TabPanelSponsorship.title"), //$NON-NLS-1$
			settlement, desktop
		);

		this.settlement = settlement;
		
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
		ReportingAuthority ra = settlement.getSponsor();
		sponsorTF.setText(ra.getName()); 
		sponsorTF.setEditable(false);
		sponsorTF.setColumns(8);
		sponsorTF.setCaretPosition(0);
		TooltipManager.setTooltip (sponsorTF, 
				ra.getDescription(),
				TooltipWay.down);
		infoPanel.add(sponsorTF);
		
		// Prepare obj name label
		JLabel objectiveNameLabel = new JLabel(Msg.getString("TabPanelSponsorship.objective"), JLabel.RIGHT); //$NON-NLS-1$
		infoPanel.add(objectiveNameLabel);

		// Prepare obj tf
		JTextField objectiveTF = new JTextField();
		String objective = settlement.getSponsor().getMissionAgenda().getObjectiveName();
		
		objectiveTF.setText(objective);
		objectiveTF.setEditable(false);
		objectiveTF.setColumns(16);
		objectiveTF.setCaretPosition(0);
		infoPanel.add(objectiveTF);

		// Prepare template label
		JLabel templateLabel = new JLabel(Msg.getString("TabPanelSponsorship.template"), JLabel.RIGHT); //$NON-NLS-1$
		infoPanel.add(templateLabel);

		// Prepare template tf
		String template = null;
		JTextField templateTF = new JTextField();
		if (settlement.getTemplate() != null) {
			template = settlement.getTemplate();
		}
		templateTF.setText(template);
		templateTF.setEditable(false);
		templateTF.setColumns(8);
		templateTF.setCaretPosition(0);
		infoPanel.add(templateTF);
		
		//Lay out the spring panel.
		SpringUtilities.makeCompactGrid(infoPanel,
		                                3, 2, //rows, cols
		                                20, 10,        //initX, initY
		                                10, 4);       //xPad, yPad
		
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		addBorder(panel, "Mission Agendas");
		
		WebTextArea ta = new WebTextArea();
		ta.setEditable(false);
		ta.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
		ta.setColumns (25);
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

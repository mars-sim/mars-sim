/**
 * Mars Simulation Project
 * TabPanelSponsorship.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.TitledBorder;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.reportingAuthority.MissionSubAgenda;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthority;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.text.WebTextArea;
//import com.alee.managers.language.data.TooltipWay;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;

/**
 * The TabPanelSponsorship is a tab panel for showing the settlement's 
 * sponsor and its objective.
 */
@SuppressWarnings("serial")
public class TabPanelSponsorship
extends TabPanel {

	/** Is UI constructed. */
	private boolean uiDone = false;
	
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
			Msg.getString("TabPanelSponsorship.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelSponsorship.tooltip"), //$NON-NLS-1$
			settlement, desktop
		);

		this.settlement = settlement;
		
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
		JPanel infoPanel = new JPanel(new SpringLayout());
//		infoPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(infoPanel, BorderLayout.NORTH);
		
		// Prepare sponsor name label
		JLabel sponsorNameLabel = new JLabel(Msg.getString("TabPanelSponsorship.sponsor"), JLabel.RIGHT); //$NON-NLS-1$
		//sponsorNameLabel.setSize(2, 2);
		infoPanel.add(sponsorNameLabel);

		// Prepare sponsor label
		JTextField sponsorTF = new JTextField();
		ReportingAuthority ra = settlement.getSponsor();
		sponsorTF.setText(ra.getCode()); 
		sponsorTF.setEditable(false);
		sponsorTF.setColumns(8);
		sponsorTF.setCaretPosition(0);
		TooltipManager.setTooltip (sponsorTF, 
				ra.getDescription(),
				TooltipWay.down);
		infoPanel.add(sponsorTF);
		
		// Prepare obj name label
		JLabel objectiveNameLabel = new JLabel(Msg.getString("TabPanelSponsorship.objective"), JLabel.RIGHT); //$NON-NLS-1$
		//objectiveNameLabel.setSize(2, 2);
		infoPanel.add(objectiveNameLabel);

		// Prepare obj tf
		JTextField objectiveTF = new JTextField();
		String objective = settlement.getSponsor().getMissionAgenda().getObjectiveName();
		
		//JLabel objectiveLabel = new JLabel(objective, JLabel.RIGHT);
		objectiveTF.setText(Conversion.capitalize(objective));
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
		templateTF.setText(Conversion.capitalize(template));
		templateTF.setEditable(false);
		templateTF.setColumns(16);
		templateTF.setCaretPosition(0);
		infoPanel.add(templateTF);
		
		//Lay out the spring panel.
		SpringUtilities.makeCompactGrid(infoPanel,
		                                3, 2, //rows, cols
		                                20, 10,        //initX, initY
		                                10, 4);       //xPad, yPad
		
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		TitledBorder border = BorderFactory.createTitledBorder(null, "Mission Agendas",
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new Font(Font.MONOSPACED, Font.BOLD, 14), java.awt.Color.darkGray);
		panel.setBorder(border);
		
		WebTextArea ta = new WebTextArea();
		ta.setEditable(false);
		ta.setFont(new Font(Font.DIALOG, Font.PLAIN, 10));
		ta.setColumns(7);
		ta.setBorder(new MarsPanelBorder());
		panel.add(ta);
		
		// For each phase, add to the text area.
		ta.append(ra.getMissionAgenda().getAgendas().stream()
				.map(MissionSubAgenda::getDescription)
				.collect(Collectors.joining("\n")));

		centerContentPanel.add(panel, BorderLayout.CENTER);
		
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

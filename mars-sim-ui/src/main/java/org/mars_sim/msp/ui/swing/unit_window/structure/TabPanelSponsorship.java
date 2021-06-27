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
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.TitledBorder;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthority;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
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
		JPanel infoPanel = new JPanel(new SpringLayout());//GridLayout(2, 2, 0, 0));
//		infoPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(infoPanel, BorderLayout.NORTH);

		// Prepare obj name label
		JLabel objectiveNameLabel = new JLabel(Msg.getString("TabPanelSponsorship.objective"), JLabel.RIGHT); //$NON-NLS-1$
		//objectiveNameLabel.setSize(2, 2);
		infoPanel.add(objectiveNameLabel);

		// Prepare obj tf
		String objective = null;
		JTextField objectiveTF = new JTextField();
		if (settlement.getReportingAuthority() != null) {
			objective = settlement.getReportingAuthority().getMissionAgenda().getObjectiveName();
		}
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
		
		// Prepare sponsor name label
		JLabel sponsorNameLabel = new JLabel(Msg.getString("TabPanelSponsorship.sponsor"), JLabel.RIGHT); //$NON-NLS-1$
		//sponsorNameLabel.setSize(2, 2);
		infoPanel.add(sponsorNameLabel);

		// Prepare sponsor label
		JTextField sponsorTF = new JTextField();
		ReportingAuthorityType sponsor = null;
		ReportingAuthority ra = settlement.getReportingAuthority();
		if (ra != null) {
		    sponsor = ra.getOrg();
		    sponsorTF.setText(sponsor.getShortName()); 
		}
		sponsorTF.setEditable(false);
		sponsorTF.setColumns(8);
		sponsorTF.setCaretPosition(0);
		if (settlement.getReportingAuthority() != null) {
			TooltipManager.setTooltip (sponsorTF, 
					sponsor.getLongName(),
					TooltipWay.down);
		}
		//JLabel sponsorLabel = new JLabel(sponsor, JLabel.RIGHT);
		infoPanel.add(sponsorTF);
		
		//Lay out the spring panel.
		SpringUtilities.makeCompactGrid(infoPanel,
		                                3, 2, //rows, cols
		                                20, 10,        //initX, initY
		                                10, 4);       //xPad, yPad
		
		JPanel m = new JPanel(new FlowLayout(FlowLayout.CENTER));
		TitledBorder border = BorderFactory.createTitledBorder(null, "Mission Agendas",
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new Font(Font.MONOSPACED, Font.BOLD, 14), java.awt.Color.darkGray);
		m.setBorder(border);
		
		WebTextArea ta = new WebTextArea();
		ta.setEditable(false);
		ta.setFont(new Font("SansSerif", Font.ITALIC, 12));
		ta.setColumns(7);
		m.add(ta);
		
		// For each phase, add to the text area.
		String[] phases = ra.getMissionAgenda().getAgendas();
		for (String s : phases) {
			ta.append(" " + s + " ");
			if (!s.equals(phases[phases.length-1]))
				//if it's NOT the last one
				ta.append("\n");
		}
		
		centerContentPanel.add(m, BorderLayout.CENTER);
		
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

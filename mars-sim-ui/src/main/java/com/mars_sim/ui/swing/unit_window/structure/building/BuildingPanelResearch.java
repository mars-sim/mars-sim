/*
 * Mars Simulation Project
 * BuildingPanelResearch.java
 * @date 2023-08-11
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.structure.building.function.Research;
import com.mars_sim.tools.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.utils.AttributePanel;


/**
 * This class is a building function panel representing
 * the research aspects of a building.
 */
@SuppressWarnings("serial")
public class BuildingPanelResearch extends BuildingFunctionPanel {

	private static final String SCIENCE_ICON = "science";

	private static final String MILLISOLS = " millisols";

	
	// Data members
	/** The research building function. */
	private Research lab;

	// Data cache
	/** The number of researchers cache. */
	private int researchersCache;

	private JLabel researchersLabel;
	
	private JLabel dailyAverageLabel;
	
	private JLabel cumulativeTotalLabel;

	private JLabel entropyLabel;
	
	/**
	 * Constructor.
	 * 
	 * @param lab the research building this panel is for.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelResearch(Research lab, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelResearch.title"), 
			ImageLoader.getIconByName(SCIENCE_ICON), 
			lab.getBuilding(), 
			desktop
		);

		// Initialize data members
		this.lab = lab;
	}
	
	/**
	 * Builds the UI.
	 */
	@Override
	protected void buildUI(JPanel center) {

		// Prepare label panel
		AttributePanel labelPanel = new AttributePanel(5);
		center.add(labelPanel, BorderLayout.NORTH);
	
		// Prepare researcher number label
		researchersCache = lab.getResearcherNum();
		researchersLabel = labelPanel.addTextField(Msg.getString("BuildingPanelResearch.numberOfResearchers"),
										Integer.toString(researchersCache), null);

		// Prepare researcher capacityLabel
		labelPanel.addTextField(Msg.getString("BuildingPanelResearch.researcherCapacity"),
					 					Integer.toString(lab.getLaboratorySize()), null);

		double[] tally = lab.getTotCumulativeDailyAverage();
		dailyAverageLabel = labelPanel.addTextField(Msg.getString("BuildingPanelResearch.dailyAverage"),
				Double.toString(Math.round(tally[1] * 10.0)/10.0) + MILLISOLS, null);
		
		cumulativeTotalLabel = labelPanel.addTextField(Msg.getString("BuildingPanelResearch.cumulativeTotal"),
				Double.toString(Math.round(tally[0] * 10.0)/10.0) + MILLISOLS, null);
		
		// Entropy
		double entropy = building.getResearch().getEntropy();
		entropyLabel = labelPanel.addTextField(Msg.getString("BuildingPanelResearch.entropy"),
	 			Math.round(entropy * 1_000.0)/1_000.0 + "", Msg.getString("BuildingPanelResearch.entropy.tooltip"));

		
		// Get the research specialties of the building.
		ScienceType[] specialties = lab.getTechSpecialties();
		int size = specialties.length;

		JTextArea specialtyTA = new JTextArea();
		specialtyTA.setEditable(false);
		specialtyTA.setColumns(5);

		// For each specialty, add specialty name panel.
		for (ScienceType specialty : specialties) {
			specialtyTA.append(" " + specialty.getName()+ " ");
			if (!specialty.equals(specialties[size-1]))
				//if it's NOT the last one
				specialtyTA.append("\n");
		}

		JPanel listPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		listPanel.add(specialtyTA);
		addBorder(listPanel, Msg.getString("BuildingPanelResearch.namesOfSpecialties"));
		center.add(listPanel, BorderLayout.CENTER);
	}

	/**
	 * Updates this panel.
	 */
	@Override
	public void update() {
		// Update researchers label if necessary.
		if (researchersCache != lab.getResearcherNum()) {
			researchersCache = lab.getResearcherNum();
			researchersLabel.setText(Integer.toString(researchersCache));
		}
		
		double[] tally = lab.getTotCumulativeDailyAverage();
		dailyAverageLabel.setText(Double.toString(Math.round(tally[1] * 10.0)/10.0) + MILLISOLS);
		cumulativeTotalLabel.setText(Double.toString(Math.round(tally[0] * 10.0)/10.0) + MILLISOLS);
		
		// Update entropy
		String entropy = Math.round(building.getResearch().getEntropy() * 1_000.0)/1_000.0 + "";
		if (!entropyLabel.getText().equalsIgnoreCase(entropy))
			entropyLabel.setText(entropy);
	}
	
	@Override
	public void destroy() {
		researchersLabel = null;
		dailyAverageLabel = null;
		cumulativeTotalLabel = null;
		entropyLabel = null;
		lab = null;
	}
}

/*
 * Mars Simulation Project
 * BuildingPanelResearch.java
 * @date 2023-08-11
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.entitywindow.building;

import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.mars_sim.core.Named;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.Research;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.SwingHelper;


/**
 * This class is a building function panel representing
 * the research aspects of a building.
 */
@SuppressWarnings("serial")
class BuildingPanelResearch extends EntityTabPanel<Building> implements TemporalComponent {

	private static final String SCIENCE_ICON = "science";

	private static final String MILLISOLS = " millisols";

	// Data cache
	/** The number of researchers cache. */
	private int researchersCache;

	private JLabel researchersLabel;
	private JLabel dailyAverageLabel;
	private JLabel cumulativeTotalLabel;
	private JLabel entropyLabel;
	private JLabel entropyPenaltyLabel;
	
	/** The research building function. */
	private Research lab;
	/**
	 * Constructor.
	 * 
	 * @param lab the research building this panel is for.
	 * @param context the UI context
	 */
	public BuildingPanelResearch(Research lab, UIContext context) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelResearch.title"), 
			ImageLoader.getIconByName(SCIENCE_ICON), null,
			context, lab.getBuilding()
		);

		// Initialize data members
		this.lab = lab;
	}
	
	/**
	 * Builds the UI.
	 */
	@Override
	protected void buildUI(JPanel center) {

		var building = getEntity();

		JPanel topPanel = new JPanel(new BorderLayout(5, 5));
		center.add(topPanel, BorderLayout.NORTH);
		
		// Prepare label panel
		AttributePanel labelPanel = new AttributePanel(6);
		topPanel.add(labelPanel, BorderLayout.NORTH);
	
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

		// Entropy
		double entropyPenalty = building.getResearch().getEntropyPenalty();
		entropyPenaltyLabel = labelPanel.addTextField(Msg.getString("BuildingPanelResearch.entropyPenalty"),
			 			Math.round(entropyPenalty * 1_000.0)/1_000.0 + "", Msg.getString("BuildingPanelResearch.entropyPenalty.tooltip"));
			
		// Get the research specialties of the building.
		ScienceType[] specialties = lab.getTechSpecialties();
		var specialText = Arrays.stream(specialties)
							.map(Named::getName)
							.collect(Collectors.joining(", "));

		JTextArea specialtyTA = SwingHelper.createTextBlock(Msg.getString("BuildingPanelResearch.namesOfSpecialties"),
						specialText);

		topPanel.add(specialtyTA, BorderLayout.CENTER);
	}


	@Override
	public void clockUpdate(ClockPulse pulse) {
		var building = getEntity();

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
		
		// Update entropy Penalty
		String entropyPenalty = Math.round(building.getResearch().getEntropyPenalty() * 1_000.0)/1_000.0 + "";
		if (!entropyPenaltyLabel.getText().equalsIgnoreCase(entropyPenalty))
			entropyPenaltyLabel.setText(entropyPenalty);
		
	}
}

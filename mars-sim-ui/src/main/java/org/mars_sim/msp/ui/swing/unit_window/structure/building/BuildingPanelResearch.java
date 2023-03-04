/*
 * Mars Simulation Project
 * BuildingPanelResearch.java
 * @date 2022-07-10
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.building.function.Research;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;


/**
 * The ResearchBuildingPanel class is a building function panel representing
 * the research info of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelResearch extends BuildingFunctionPanel {

	private static final String SCIENCE_ICON = "science";

	// Data members
	/** The research building. */
	private Research lab;

	// Data cache
	/** The number of researchers cache. */
	private int researchersCache;

	private JLabel researchersLabel;

	/**
	 * Constructor.
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
	 * Build the UI
	 */
	@Override
	protected void buildUI(JPanel center) {

		// Prepare label panel
		AttributePanel labelPanel = new AttributePanel(2);
		center.add(labelPanel, BorderLayout.NORTH);
	
		// Prepare researcher number label
		researchersCache = lab.getResearcherNum();
		researchersLabel = labelPanel.addTextField(Msg.getString("BuildingPanelResearch.numberOfResearchers"),
										Integer.toString(researchersCache), null);

		// Prepare researcher capacityLabel
		labelPanel.addTextField(Msg.getString("BuildingPanelResearch.researcherCapacity"),
					 					Integer.toString(lab.getLaboratorySize()), null);

		// Get the research specialties of the building.
		ScienceType[] specialties = lab.getTechSpecialties();
		int size = specialties.length;

		JTextArea specialtyTA = new JTextArea();
		specialtyTA.setEditable(false);
		specialtyTA.setColumns(15);

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
	 * Update this panel.
	 */
	@Override
	public void update() {
		// Update researchers label if necessary.
		if (researchersCache != lab.getResearcherNum()) {
			researchersCache = lab.getResearcherNum();
			researchersLabel.setText(Integer.toString(researchersCache));
		}
	}
}

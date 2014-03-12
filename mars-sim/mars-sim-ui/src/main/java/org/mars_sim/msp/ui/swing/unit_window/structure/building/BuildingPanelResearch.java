/**
 * Mars Simulation Project
 * ResearchBuildingPanel.java
 * @version 3.06 2014-02-27
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.building.function.Research;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

import javax.swing.*;

import java.awt.*;


/**
 * The ResearchBuildingPanel class is a building function panel representing 
 * the research info of a settlement building.
 */
public class BuildingPanelResearch
extends BuildingFunctionPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	/** The research building. */
	private Research lab;
	/** The number of researchers label. */
	private JLabel researchersLabel;

	// Data cache
	/** The number of researchers cache. */
	private int researchersCache;

	/**
	 * Constructor.
	 * @param lab the research building this panel is for.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelResearch(Research lab, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(lab.getBuilding(), desktop);

		// Initialize data members
		this.lab = lab;

		// Set panel layout
		setLayout(new BorderLayout());

		// Prepare label panel
		JPanel labelPanel = new JPanel(new GridLayout(4, 1, 0, 0));
		add(labelPanel, BorderLayout.NORTH);

		// Prepare research label
		JLabel researchLabel = new JLabel("Research", JLabel.CENTER);
		labelPanel.add(researchLabel);

		// Prepare researcher number label
		researchersCache = lab.getResearcherNum();
		researchersLabel = new JLabel("Number of Researchers: " + researchersCache, JLabel.CENTER);
		labelPanel.add(researchersLabel);

		// Prepare researcher capacityLabel
		JLabel researcherCapacityLabel = new JLabel("Researcher Capacity: " + lab.getLaboratorySize(),
				JLabel.CENTER);
		labelPanel.add(researcherCapacityLabel);

		// Prepare specialties label
		JLabel specialtiesLabel = new JLabel("Specialties: ", JLabel.CENTER);
		labelPanel.add(specialtiesLabel);

		// Get the research specialties of the building.
		ScienceType[] specialties = lab.getTechSpecialties();

		// Prepare specialtiesListPanel
		JPanel specialtiesListPanel = new JPanel(new GridLayout(specialties.length, 1, 0, 0));
		specialtiesListPanel.setBorder(new MarsPanelBorder());
		add(specialtiesListPanel, BorderLayout.CENTER);

		// For each specialty, add specialty name panel.
		for (ScienceType specialty : specialties) {
			JLabel specialtyLabel = new JLabel(specialty.getName(), JLabel.CENTER);
			specialtiesListPanel.add(specialtyLabel);
		}
	}

	/**
	 * Update this panel.
	 */
	@Override
	public void update() {
		// Update researchers label if necessary.
		if (researchersCache != lab.getResearcherNum()) {
			researchersCache = lab.getResearcherNum();
			researchersLabel.setText("Number of Researchers: " + researchersCache);
		}
	}
}

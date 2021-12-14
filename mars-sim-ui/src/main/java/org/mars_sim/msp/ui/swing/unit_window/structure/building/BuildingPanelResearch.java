/*
 * Mars Simulation Project
 * BuildingPanelResearch.java
 * @date 2021-10-07
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.building.function.Research;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

import com.alee.laf.panel.WebPanel;
import com.alee.laf.text.WebTextArea;


/**
 * The ResearchBuildingPanel class is a building function panel representing
 * the research info of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelResearch
extends BuildingFunctionPanel {

	// Data members
	/** The research building. */
	private Research lab;

	// Data cache
	/** The number of researchers cache. */
	private int researchersCache;

	private JTextField researchersLabel;

	/**
	 * Constructor.
	 * @param lab the research building this panel is for.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelResearch(Research lab, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(Msg.getString("BuildingPanelResearch.title"), lab.getBuilding(), desktop);

		// Initialize data members
		this.lab = lab;
	}
	
	/**
	 * Build the UI
	 */
	@Override
	protected void buildUI(JPanel center) {

		// Prepare label panel
		WebPanel labelPanel = new WebPanel(new GridLayout(2, 2, 3, 1));
		center.add(labelPanel, BorderLayout.NORTH);
	
		// Prepare researcher number label
		researchersCache = lab.getResearcherNum();
		researchersLabel = addTextField(labelPanel, Msg.getString("BuildingPanelResearch.numberOfResearchers"),
										researchersCache, null);

		// Prepare researcher capacityLabel
		addTextField(labelPanel, Msg.getString("BuildingPanelResearch.researcherCapacity"),
					 lab.getLaboratorySize(), null);

		// Get the research specialties of the building.
		ScienceType[] specialties = lab.getTechSpecialties();
		int size = specialties.length;

		WebTextArea specialtyTA = new WebTextArea();
		specialtyTA.setEditable(false);
		specialtyTA.setFont(new Font("SansSerif", Font.ITALIC, 12));
		specialtyTA.setColumns(7);

		// For each specialty, add specialty name panel.
		for (ScienceType specialty : specialties) {
			specialtyTA.append(" " + specialty.getName()+ " ");
			if (!specialty.equals(specialties[size-1]))
				//if it's NOT the last one
				specialtyTA.append("\n");
		}

		WebPanel listPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		listPanel.add(specialtyTA);
		specialtyTA.setBorder(new TitledBorder(Msg.getString("BuildingPanelResearch.namesOfSpecialties")));
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

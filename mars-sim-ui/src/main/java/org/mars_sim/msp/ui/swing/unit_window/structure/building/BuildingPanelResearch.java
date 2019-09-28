/**
 * Mars Simulation Project
 * BuildingPanelResearch.java
 * @version 3.1.0 2017-09-15
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.building.function.Research;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

import com.alee.laf.label.WebLabel;
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

	private WebLabel researchersLabel;

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
		WebPanel labelPanel = new WebPanel(new GridLayout(4, 1, 0, 0));
		add(labelPanel, BorderLayout.NORTH);
		labelPanel.setOpaque(false);
		labelPanel.setBackground(new Color(0,0,0,128));

		// Prepare research label
		WebLabel researchLabel = new WebLabel(Msg.getString("BuildingPanelResearch.title"), WebLabel.CENTER); //$NON-NLS-1$
		researchLabel.setFont(new Font("Serif", Font.BOLD, 16));
		//researchLabel.setForeground(new Color(102, 51, 0)); // dark brown
		labelPanel.add(researchLabel);

		// Prepare researcher number label
		researchersCache = lab.getResearcherNum();
		researchersLabel = new WebLabel(Msg.getString("BuildingPanelResearch.numberOfResearchers", researchersCache), WebLabel.CENTER);
		labelPanel.add(researchersLabel);

		// Prepare researcher capacityLabel
		WebLabel researcherCapacityLabel = new WebLabel(Msg.getString("BuildingPanelResearch.researcherCapacity",
				lab.getLaboratorySize()),
				WebLabel.CENTER);
		labelPanel.add(researcherCapacityLabel);

		// Prepare specialties label
		WebLabel specialtiesLabel = new WebLabel(Msg.getString("BuildingPanelResearch.namesOfSpecialties"), WebLabel.CENTER);
		labelPanel.add(specialtiesLabel);

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
		specialtyTA.setBorder(new MarsPanelBorder());

		add(listPanel, BorderLayout.CENTER);
		listPanel.setOpaque(false);
		listPanel.setBackground(new Color(0,0,0,128));

	}

	/**
	 * Update this panel.
	 */
	@Override
	public void update() {
		// Update researchers label if necessary.
		if (researchersCache != lab.getResearcherNum()) {
			researchersCache = lab.getResearcherNum();
			researchersLabel.setText(
				Msg.getString("BuildingPanelResearch.numberOfResearchers",
						researchersCache));
		}
	}
}

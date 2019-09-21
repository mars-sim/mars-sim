/**
 * Mars Simulation Project
 * LaboratoryTabPanel.java
 * @version 3.1.0 2019-09-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Lab;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;

/** 
 * The LaboratoryTabPanel is a tab panel for an explorer rover's lab information.
 */
@SuppressWarnings("serial")
public class LaboratoryTabPanel extends TabPanel {

	// Data members
	/** The number of researchers label. */
	private WebLabel researchersLabel;

	// Data cache
	/** The number of researchers cache. */
	private int researchersCache;

	/**
	 * Constructor.
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public LaboratoryTabPanel(Unit unit, MainDesktopPane desktop) { 
		// Use the TabPanel constructor
		super("Lab", null, "Laboratory", unit, desktop);

		Rover rover = (Rover) unit;
		Lab lab = rover.getLab();

		// Prepare laboratory panel
		WebPanel laboratoryPanel = new WebPanel(new BorderLayout());
		topContentPanel.add(laboratoryPanel);

		// Prepare name panel
		WebPanel namePanel = new WebPanel();
		laboratoryPanel.add(namePanel, BorderLayout.NORTH);

		// Prepare laboratory label
		WebLabel laboratoryLabel = new WebLabel("Laboratory", WebLabel.CENTER);
		laboratoryLabel.setFont(new Font("Serif", Font.BOLD, 16));
		namePanel.add(laboratoryLabel);

		// Prepare label panel
		WebPanel labelPanel = new WebPanel(new GridLayout(3, 1));
		laboratoryPanel.add(labelPanel, BorderLayout.CENTER);

		// Prepare researcher number label
		researchersCache = lab.getResearcherNum();
		researchersLabel = new WebLabel("Number of Researchers: " + researchersCache, WebLabel.CENTER);
		labelPanel.add(researchersLabel);

		// Prepare researcher capacityLabel
		WebLabel researcherCapacityLabel = new WebLabel("Researcher Capacity: " + lab.getLaboratorySize(),
				WebLabel.CENTER);
		labelPanel.add(researcherCapacityLabel);

		// Prepare specialties label
		WebLabel specialtiesLabel = new WebLabel("Specialties: ", WebLabel.CENTER);
		labelPanel.add(specialtiesLabel);

		// Get the research specialties of the building.
		ScienceType[] specialties = lab.getTechSpecialties();

		// Prepare specialitiesListPanel
		WebPanel specialtiesListPanel = new WebPanel(new GridLayout(specialties.length, 1, 0, 0));
		specialtiesListPanel.setBorder(new MarsPanelBorder());
		laboratoryPanel.add(specialtiesListPanel, BorderLayout.SOUTH);

		// For each specialty, add specialty name panel.
		for (ScienceType specialty : specialties) {
			WebLabel specialtyLabel = new WebLabel(specialty.getName(), WebLabel.CENTER);
			specialtiesListPanel.add(specialtyLabel);
		}
	}

	/**
	 * Update this panel
	 */
	public void update() {

		Rover rover = (Rover) unit;
		Lab lab = rover.getLab();

		// Update researchers label if necessary.
		if (researchersCache != lab.getResearcherNum()) {
			researchersCache = lab.getResearcherNum();
			researchersLabel.setText("Number of Researchers: " + researchersCache);
		}
	}
	
	public void destroy() {
	    researchersLabel = null; 
	}
}

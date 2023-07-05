/*
 * Mars Simulation Project
 * LaboratoryTabPanel.java
 * @date 2023-01-09
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Lab;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;

/** 
 * The LaboratoryTabPanel is a tab panel for an explorer rover's lab information.
 */
@SuppressWarnings("serial")
public class LaboratoryTabPanel extends TabPanel {
	
	private static final String SCIENCE_ICON = "science"; //$NON-NLS-1$

	/** The Rover instance. */
	private Rover rover;
	
	/** The number of researchers label. */
	private JLabel researchersLabel;

	// Data cache
	/** The number of researchers cache. */
	private int researchersCache;

	/**
	 * Constructor.
	 * 
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public LaboratoryTabPanel(Rover unit, MainDesktopPane desktop) { 
		// Use the TabPanel constructor
		super(
			Msg.getString("LaboratoryTabPanel.title"),	
			Msg.getString("LaboratoryTabPanel.title"),
			ImageLoader.getIconByName(SCIENCE_ICON),
			Msg.getString("LaboratoryTabPanel.title"),
			desktop
		);
		
		rover = unit;
	}

	@Override
	protected void buildUI(JPanel content) {
		Lab lab = rover.getLab();
		
		// Prepare laboratory panel
		JPanel laboratoryPanel = new JPanel(new BorderLayout());
		content.add(laboratoryPanel, BorderLayout.NORTH);
		
		// Prepare the top panel using spring layout.
		AttributePanel springPanel = new AttributePanel(2);
		laboratoryPanel.add(springPanel, BorderLayout.CENTER);
		
		// Prepare researcher number label
		researchersCache = lab.getResearcherNum();
		researchersLabel = springPanel.addTextField("Number of Researchers", Integer.toString(researchersCache), null);

		// Prepare researcher capacityLabel
		springPanel.addTextField("Researcher Capacity", Integer.toString(lab.getLaboratorySize()), null);

		// Get the research specialties of the building.
		ScienceType[] specialties = lab.getTechSpecialties();
		int size = specialties.length;
		
		// Prepare specialty text area
		JTextArea specialtyTA = new JTextArea();
		specialtyTA.setEditable(false);
		specialtyTA.setColumns(10);
		specialtyTA.setBorder(new MarsPanelBorder());
		
		
		JPanel listPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		listPanel.setSize(150, 80);
		listPanel.add(specialtyTA);
		listPanel.setBorder(StyleManager.createLabelBorder("Specialties"));
		
		// Prepare specialties label	
		laboratoryPanel.add(listPanel, BorderLayout.SOUTH);
		
		// For each specialty, add specialty name panel.
		for (ScienceType specialty : specialties) {
			specialtyTA.append(" " + specialty.getName()+ " ");
			if (!specialty.equals(specialties[size-1]))
				//if it's NOT the last one
				specialtyTA.append("\n");
		}
	}

	/**
	 * Update this panel
	 */
	@Override
	public void update() {
		Lab lab = rover.getLab();

		// Update researchers label if necessary.
		if (researchersCache != lab.getResearcherNum()) {
			researchersCache = lab.getResearcherNum();
			researchersLabel.setText("" + researchersCache);
		}
	}

	@Override
	public void destroy() {
	    super.destroy();
	    
		researchersLabel = null; 
	    rover = null;
	}
}
/*
 * Mars Simulation Project
 * LaboratoryTabPanel.java
 * @date 2023-01-09
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.mars_sim.core.Named;
import com.mars_sim.core.structure.Lab;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.unit_window.TabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.SwingHelper;

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
			ImageLoader.getIconByName(SCIENCE_ICON),
			Msg.getString("LaboratoryTabPanel.title"),
			unit, desktop
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

		// Get the research specialties of the lab.
		var txt = Arrays.stream(lab.getTechSpecialties()).map(Named::getName).collect(Collectors.joining(", "));
		JTextArea specialtyTA = SwingHelper.createTextBlock("Specialties", txt);
		laboratoryPanel.add(specialtyTA, BorderLayout.SOUTH);
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
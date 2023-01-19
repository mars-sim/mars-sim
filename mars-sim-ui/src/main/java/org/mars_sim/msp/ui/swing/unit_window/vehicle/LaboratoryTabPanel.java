/*
 * Mars Simulation Project
 * LaboratoryTabPanel.java
 * @date 2023-01-09
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.TitledBorder;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Lab;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

/** 
 * The LaboratoryTabPanel is a tab panel for an explorer rover's lab information.
 */
@SuppressWarnings("serial")
public class LaboratoryTabPanel extends TabPanel {
	
	private static final String SCIENCE_ICON = Msg.getString("icon.science"); //$NON-NLS-1$

	/** The Rover instance. */
	private Rover rover;
	
	/** The number of researchers label. */
	private JTextField researchersLabel;

	// Data cache
	/** The number of researchers cache. */
	private int researchersCache;

	/**
	 * Constructor.
	 * 
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public LaboratoryTabPanel(Unit unit, MainDesktopPane desktop) { 
		// Use the TabPanel constructor
		super(
			Msg.getString("LaboratoryTabPanel.title"),	
			Msg.getString("LaboratoryTabPanel.title"),
			ImageLoader.getNewIcon(SCIENCE_ICON),
			Msg.getString("LaboratoryTabPanel.title"),
			unit, desktop
		);
		
		rover = (Rover) unit;
	}

	@Override
	protected void buildUI(JPanel content) {
		Lab lab = rover.getLab();
		
		// Prepare laboratory panel
		JPanel laboratoryPanel = new JPanel(new BorderLayout());
		content.add(laboratoryPanel, BorderLayout.NORTH);
		
		// Prepare the top panel using spring layout.
		JPanel springPanel = new JPanel(new SpringLayout());
		laboratoryPanel.add(springPanel, BorderLayout.CENTER);
		
		// Prepare researcher number label
		researchersCache = lab.getResearcherNum();
		researchersLabel = addTextField(springPanel, "Number of Researchers:", researchersCache, 4, null);

		// Prepare researcher capacityLabel
		addTextField(springPanel, "Researcher Capacity:", lab.getLaboratorySize(), 4, null);


        // Lay out the spring panel.
     	SpringUtilities.makeCompactGrid(springPanel,
     		                                2, 2, //rows, cols
     		                               90, 10,        //initX, initY
    		                               XPAD_DEFAULT, YPAD_DEFAULT);       //xPad, yPad
				
		// Get the research specialties of the building.
		ScienceType[] specialties = lab.getTechSpecialties();
		int size = specialties.length;
		
		// Prepare specialty text area
		JTextArea specialtyTA = new JTextArea();
		specialtyTA.setEditable(false);
		specialtyTA.setFont(new Font("SansSerif", Font.ITALIC, 12));
		specialtyTA.setColumns(10);
//		specialtyTA.setSize(100, 60);
		specialtyTA.setBorder(new MarsPanelBorder());
		
		
		JPanel listPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		listPanel.setSize(150, 80);
		listPanel.add(specialtyTA);
		
		TitledBorder titledBorder = BorderFactory.createTitledBorder(null, "Specialties",
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new Font("Serif", Font.BOLD, 14), java.awt.Color.darkGray);
		listPanel.setBorder(titledBorder);
		
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
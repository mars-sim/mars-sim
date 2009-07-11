/**
 * Mars Simulation Project
 * ResearchBuildingPanel.java
 * @version 2.75 2004-04-05
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.standard.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mars_sim.msp.simulation.structure.building.function.Research;
import org.mars_sim.msp.ui.standard.MainDesktopPane;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;


/**
 * The ResearchBuildingPanel class is a building function panel representing 
 * the research info of a settlement building.
 */
public class ResearchBuildingPanel extends BuildingFunctionPanel {
    
    // Data members
    private Research lab; // The research building.
    private JLabel researchersLabel; // The number of researchers label.
    
    // Data cache
    private int researchersCache;  // The number of researchers cache.
    
    /**
     * Constructor
     *
     * @param lab the research building this panel is for.
     * @param desktop The main desktop.
     */
    public ResearchBuildingPanel(Research lab, MainDesktopPane desktop) {
        
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
        
        // Prepare specialities label
        JLabel specialitiesLabel = new JLabel("Specialities: ", JLabel.CENTER);
        labelPanel.add(specialitiesLabel);
        
        // Get the research specialities of the building.
        String[] specialities = lab.getTechSpecialities();
        
        // Prepare specialitiesListPanel
        JPanel specialitiesListPanel = new JPanel(new GridLayout(specialities.length, 1, 0, 0));
        specialitiesListPanel.setBorder(new MarsPanelBorder());
        add(specialitiesListPanel, BorderLayout.CENTER);
        
        // For each speciality, add speciality name panel.
        
        for (int x=0; x < specialities.length; x++) {
            JLabel specialityLabel = new JLabel(specialities[x], JLabel.CENTER);
            specialitiesListPanel.add(specialityLabel);
        }
    }
    
    /**
     * Update this panel
     */
    public void update() {
        
        // Update researchers label if necessary.
        if (researchersCache != lab.getResearcherNum()) {
            researchersCache = lab.getResearcherNum();
            researchersLabel.setText("Number of Researchers: " + researchersCache);
        }
    }
}

/**
 * Mars Simulation Project
 * BuildingPanel.java
 * @version 2.75 2003-05-21
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window.structure.building;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.*;

/**
 * The BuildingPanel class is a panel representing a settlement building.
 */
public class BuildingPanel extends JPanel {
    
    private String panelName; // The name of the panel.
    private Building building; // The building this panel is for.
    private java.util.List functionPanels; // The function panels
    
    /**
     * Constructor
     *
     * @param panelName the name of the panel.
     * @param building the building this panel is for.
     */
    public BuildingPanel(String panelName, Building building) {
        super();
        
        // Initialize data members
        this.panelName = panelName;
        this.building = building;
        this.functionPanels = new ArrayList();
        
        // Set layout
        setLayout(new BorderLayout(0, 0));
    }
    
    /**
     * Gets the panel's name.
     *
     * @return panel name
     */
    public String getPanelName() {
        return panelName;
    }
    
    /**
     * Gets the panel's building.
     *
     * @return building
     */
    public Building getBuilding() {
        return building;
    }
    
    /**
     * Update this panel
     */
    public void update() {}
}
        
    

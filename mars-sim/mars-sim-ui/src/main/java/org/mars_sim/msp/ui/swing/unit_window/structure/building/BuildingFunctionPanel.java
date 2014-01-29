/**
 * Mars Simulation Project
 * BuildingFunctionPanel.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

import javax.swing.*;



/**
 * The BuildingFunctionPanel class is a panel representing a function for 
 * a settlement building.
 */
public abstract class BuildingFunctionPanel extends JPanel {
    
    protected Building building; // The building this panel is for.
    protected MainDesktopPane desktop; // The main desktop.
    
    /**
     * Constructor
     *
     * @param building The building this panel is for.
     * @param desktop The main desktop.
     */
    public BuildingFunctionPanel(Building building, MainDesktopPane desktop) {
        // User JPanel constructor
        super();
        
        // Initialize data members
        this.building = building;
        this.desktop = desktop;
        
        setBorder(new MarsPanelBorder());
    }
    
    /**
     * Update this panel
     */
    public abstract void update();
}

/**
 * Mars Simulation Project
 * BuildingFunctionPanel.java
 * @version 2.75 2003-09-10
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window.structure.building;

import javax.swing.*;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.ui.standard.*;

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

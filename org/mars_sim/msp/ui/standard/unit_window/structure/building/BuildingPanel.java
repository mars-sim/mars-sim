/**
 * Mars Simulation Project
 * BuildingPanel.java
 * @version 2.75 2003-06-02
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window.structure.building;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import org.mars_sim.msp.simulation.malfunction.Malfunctionable;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.*;
import org.mars_sim.msp.ui.standard.MainDesktopPane;

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
     * @param desktop the main desktop.
     */
    public BuildingPanel(String panelName, Building building, MainDesktopPane desktop) {
        super();
        
        // Initialize data members
        this.panelName = panelName;
        this.building = building;
        this.functionPanels = new ArrayList();
        
        // Set layout
        setLayout(new BorderLayout(0, 0));
        
        // Prepare function scroll panel.
        JScrollPane functionScrollPanel = new JScrollPane();
        functionScrollPanel.setPreferredSize(new Dimension(200, 220));
        add(functionScrollPanel, BorderLayout.NORTH);
        
        // Prepare function list panel.
        JPanel functionListPanel = new JPanel();
        functionListPanel.setLayout(new BoxLayout(functionListPanel, BoxLayout.Y_AXIS));
        functionScrollPanel.setViewportView(functionListPanel);
        
        // Prepare inhabitable panel if building is inhabitable.
        if (building instanceof InhabitableBuilding) {
            BuildingFunctionPanel inhabitablePanel = new InhabitableBuildingPanel((InhabitableBuilding) building, desktop);
            functionPanels.add(inhabitablePanel);
            functionListPanel.add(inhabitablePanel);
        }
        
        // Prepare power panel.
        BuildingFunctionPanel powerPanel = new PowerBuildingPanel(building, desktop);
        functionPanels.add(powerPanel);
        functionListPanel.add(powerPanel);
        
        // Prepare malfunctionable panel.
        BuildingFunctionPanel malfunctionPanel = new MalfunctionableBuildingPanel((Malfunctionable) building, desktop);
        functionPanels.add(malfunctionPanel);
        functionListPanel.add(malfunctionPanel);
        
        // Prepare maintenance panel.
        BuildingFunctionPanel maintenancePanel = new MaintenanceBuildingPanel((Malfunctionable) building, desktop);
        functionPanels.add(maintenancePanel);
        functionListPanel.add(maintenancePanel);
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
    public void update() {
    
        // Update each building function panel.
        Iterator i = functionPanels.iterator();
        while (i.hasNext()) {
            BuildingFunctionPanel panel = (BuildingFunctionPanel) i.next();
            panel.update();
        }
    }
}
        
    

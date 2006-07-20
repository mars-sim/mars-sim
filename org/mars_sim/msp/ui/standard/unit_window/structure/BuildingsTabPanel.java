/**
 * Mars Simulation Project
 * BuildingsTabPanel.java
 * @version 2.75 2003-07-16
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window.structure;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.ui.standard.*;
import org.mars_sim.msp.ui.standard.unit_window.*;
import org.mars_sim.msp.ui.standard.unit_window.structure.building.BuildingPanel;

/**
 * The BuildingsTabPanel is a tab panel containing building panels.
 */
public class BuildingsTabPanel extends TabPanel implements ActionListener {
    
    private DefaultComboBoxModel buildingComboBoxModel;
    private JComboBox buildingComboBox;
    private java.util.List buildingsCache;
    private JPanel buildingDisplayPanel;
    private CardLayout buildingLayout;
    private java.util.List buildingPanels;
    private int count;
    
    /**
     * Constructor
     *
     * @param unit the unit to display.
     * @param desktop the main desktop.
     */
    public BuildingsTabPanel(Unit unit, MainDesktopPane desktop) {
        // Use the TabPanel constructor
        super("Buildings", null, "Settlement Buildings", unit, desktop);
        
        Settlement settlement = (Settlement) unit;
        java.util.List buildings = settlement.getBuildingManager().getBuildings();
        
        // Create building select panel.
        JPanel buildingSelectPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buildingSelectPanel.setBorder(new MarsPanelBorder());
        topContentPanel.add(buildingSelectPanel);
        
        // Create building combo box model.
        buildingComboBoxModel = new DefaultComboBoxModel();
        buildingsCache = new ArrayList(buildings);
        Iterator i = buildingsCache.iterator();
        while (i.hasNext()) buildingComboBoxModel.addElement(i.next());
        
        // Create building list.
        buildingComboBox = new JComboBox(buildingComboBoxModel);
        buildingComboBox.addActionListener(this);
        buildingComboBox.setMaximumRowCount(10);
        buildingSelectPanel.add(buildingComboBox);
        
        // Create building display panel.
        buildingDisplayPanel = new JPanel();
        buildingLayout = new CardLayout();
        buildingDisplayPanel.setLayout(buildingLayout);
        buildingDisplayPanel.setBorder(new MarsPanelBorder());
        centerContentPanel.add(buildingDisplayPanel);
        
        // Create building panels
        buildingPanels = new ArrayList();
        count = 0;
        Iterator iter = buildings.iterator();
        while (iter.hasNext()) {
            Building b = (Building) iter.next();
            BuildingPanel panel = new BuildingPanel(String.valueOf(count), b, desktop);
            buildingPanels.add(panel);
            buildingDisplayPanel.add(panel, panel.getPanelName());
            count++;
        }
    }
    
    /**
     * Updates the info on this panel.
     */
    public void update() {
        
        Settlement settlement = (Settlement) unit;
        java.util.List buildings = settlement.getBuildingManager().getBuildings();
        
        // Update buildings if necessary.
        if (!buildingsCache.equals(buildings)) {
        	
            // Add building panels for new buildings.
            Iterator iter1 = buildings.iterator();
            while (iter1.hasNext()) {
                Building building = (Building) iter1.next();
                if (!buildingsCache.contains(building)) {
                    BuildingPanel panel = new BuildingPanel(String.valueOf(count), building, desktop);
                    buildingPanels.add(panel);
                    buildingDisplayPanel.add(panel, panel.getPanelName());
					buildingComboBoxModel.addElement(building);
                    count++;
                }
            }
            
            // Remove building panels for destroyed buildings.
            Iterator iter2 = buildingsCache.iterator();
            while (iter2.hasNext()) {
                Building building = (Building) iter2.next();
                if (!buildings.contains(building)) {
                    BuildingPanel panel = getBuildingPanel(building);
                    if (panel != null) {
                        buildingPanels.remove(panel);
                        buildingDisplayPanel.remove(panel);
						buildingComboBoxModel.removeElement(building);
                    }
                }
            }
            
            // Update buildings cache.
            buildingsCache = buildings;
        }
    
        // Have each building panel update.
        Iterator i = buildingPanels.iterator();
        while (i.hasNext()) ((BuildingPanel) i.next()).update();
    }
    
    /** 
     * Action event occurs.
     *
     * @param event the action event
     */
    public void actionPerformed(ActionEvent event) {
        Building building = (Building) buildingComboBox.getSelectedItem();
        BuildingPanel panel = getBuildingPanel(building);
        if (panel != null) buildingLayout.show(buildingDisplayPanel, panel.getPanelName());
        else System.err.println("Couldn't fine panel for " + building);
    }
    
    /**
     * Gets the building panel for a given building.
     *
     * @param building the given building
     * @return the building panel or null if none.
     */
    private BuildingPanel getBuildingPanel(Building building) {
        BuildingPanel result = null;
        Iterator i = buildingPanels.iterator();
        while (i.hasNext()) {
            BuildingPanel panel = (BuildingPanel) i.next();
            if (panel.getBuilding() == building) result = panel;
        }
        
        return result;
    }
}
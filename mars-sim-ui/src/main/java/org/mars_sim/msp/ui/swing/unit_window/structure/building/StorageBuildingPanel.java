/**
 * Mars Simulation Project
 * StorageBuildingPanel.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.building.function.Storage;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;
import java.util.Map;

/**
 * The StorageBuildingPanel class is a building function panel representing 
 * the storage capacity of a settlement building.
 */
public class StorageBuildingPanel extends BuildingFunctionPanel {

    /**
     * Constructor
     * @param storage the storage building function.
     * @param desktop the main desktop.
     */
    public StorageBuildingPanel(Storage storage, MainDesktopPane desktop) {
        
        // Use BuildingFunctionPanel constructor
        super(storage.getBuilding(), desktop);
        
        setLayout(new BorderLayout(0, 0));
        
        // Create storage label.
        JLabel storageLabel = new JLabel("Storage Capacity", JLabel.CENTER);
        add(storageLabel, BorderLayout.NORTH);
        
        Map<AmountResource, Double> resourceStorage = storage.getResourceStorageCapacity();
        
        // Create resource storage panel.
        JPanel resourceStoragePanel = new JPanel(new GridLayout(resourceStorage.size(), 2, 0, 0));
        add(resourceStoragePanel, BorderLayout.CENTER);
        Iterator<AmountResource> i = resourceStorage.keySet().iterator();
        while (i.hasNext()) {
            AmountResource resource = i.next();
            
            // Create resource label.
            JLabel resourceLabel = new JLabel(resource.getName() + ":", JLabel.LEFT);
            resourceStoragePanel.add(resourceLabel);
            
            double capacity = resourceStorage.get(resource);
            JLabel capacityLabel = new JLabel((int) capacity + " kg", JLabel.RIGHT);
            resourceStoragePanel.add(capacityLabel);
        }
    }
    
    @Override
    public void update() {
        // Storage capacity doesn't change so nothing to update.
    }
}
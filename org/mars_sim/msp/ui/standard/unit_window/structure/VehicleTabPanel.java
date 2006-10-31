/**
 * Mars Simulation Project
 * VehicleTabPanel.java
 * @version 2.75 2003-09-10
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window.structure;

import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.simulation.vehicle.*;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.ui.standard.*;
import org.mars_sim.msp.ui.standard.unit_window.TabPanel;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/** 
 * The VehicleTabPanel is a tab panel for parked vehicle information.
 */
public class VehicleTabPanel extends TabPanel implements MouseListener {
    
    private DefaultListModel vehicleListModel;
    private JList vehicleList;
    private VehicleCollection vehicleCache;
    
    /**
     * Constructor
     *
     * @param unit the unit to display
     * @param desktop the main desktop.
     */
    public VehicleTabPanel(Unit unit, MainDesktopPane desktop) { 
        // Use the TabPanel constructor
        super("Vehicles", null, "Vehicles parked at the settlement", unit, desktop);
        
        Settlement settlement = (Settlement) unit;
        
        // Create vehicle label panel
        JPanel vehicleLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topContentPanel.add(vehicleLabelPanel);
        
        // Create vehicle label
        JLabel vehicleLabel = new JLabel("Parked Vehicles", JLabel.CENTER);
        vehicleLabelPanel.add(vehicleLabel);
        
        // Create vehicle display panel
        JPanel vehicleDisplayPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        vehicleDisplayPanel.setBorder(new MarsPanelBorder());
        topContentPanel.add(vehicleDisplayPanel);
        
        // Create scroll panel for vehicle list.
        JScrollPane vehicleScrollPanel = new JScrollPane();
        vehicleScrollPanel.setPreferredSize(new Dimension(175, 100));
        vehicleDisplayPanel.add(vehicleScrollPanel);
        
        // Create vehicle list model
        vehicleListModel = new DefaultListModel();
        vehicleCache = new VehicleCollection(settlement.getParkedVehicles());
        VehicleIterator i = vehicleCache.iterator();
        while (i.hasNext()) vehicleListModel.addElement(i.next());
        
        // Create vehicle list
        vehicleList = new JList(vehicleListModel);
        vehicleList.addMouseListener(this);
        vehicleScrollPanel.setViewportView(vehicleList);
    }
    
    /**
     * Updates the info on this panel.
     */
    public void update() {
        Settlement settlement = (Settlement) unit;
        
        // Update vehicle list
        if (!vehicleCache.equals(settlement.getParkedVehicles())) {
            vehicleCache = new VehicleCollection(settlement.getParkedVehicles());
            vehicleListModel.clear();
            VehicleIterator i = vehicleCache.iterator();
            while (i.hasNext()) vehicleListModel.addElement(i.next());
        }
    }
    
    /** 
     * Mouse clicked event occurs.
     *
     * @param event the mouse event
     */
    public void mouseClicked(MouseEvent event) {

        // If double-click, open person window.
        if (event.getClickCount() >= 2) 
            desktop.openUnitWindow((Vehicle) vehicleList.getSelectedValue());
    }

    public void mousePressed(MouseEvent event) {}
    public void mouseReleased(MouseEvent event) {}
    public void mouseEntered(MouseEvent event) {}
    public void mouseExited(MouseEvent event) {}
}

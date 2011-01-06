/**
 * Mars Simulation Project
 * VehicleTabPanel.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window.structure;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/** 
 * The VehicleTabPanel is a tab panel for parked vehicle information.
 */
public class VehicleTabPanel extends TabPanel implements MouseListener {
    
    private DefaultListModel vehicleListModel;
    private JList vehicleList;
    private Collection<Vehicle> vehicleCache;
    
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
        vehicleCache = settlement.getParkedVehicles();
        Iterator i = vehicleCache.iterator();
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
        if (!Arrays.equals(vehicleCache.toArray(), settlement.getParkedVehicles().toArray())) {
            vehicleCache = new ArrayList<Vehicle>(settlement.getParkedVehicles());
            vehicleListModel.clear();
            Iterator<Vehicle> i = vehicleCache.iterator();
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
            desktop.openUnitWindow((Vehicle) vehicleList.getSelectedValue(), false);
    }

    public void mousePressed(MouseEvent event) {}
    public void mouseReleased(MouseEvent event) {}
    public void mouseEntered(MouseEvent event) {}
    public void mouseExited(MouseEvent event) {}
}

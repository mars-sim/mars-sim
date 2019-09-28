/**
 * Mars Simulation Project
 * TabPanelVehicles.java
 * @version 3.1.0 2017-10-18
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;


import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

/** 
 * The VehicleTabPanel is a tab panel for parked vehicle information.
 */
@SuppressWarnings("serial")
public class TabPanelVehicles
extends TabPanel
implements MouseListener {

	/** Is UI constructed. */
	private boolean uiDone = false;
	
	/** The Settlement instance. */
	private Settlement settlement;
	
	private VehicleListModel vehicleListModel;
	private JList<Vehicle> vehicleList;
	private JScrollPane vehicleScrollPanel;

	/**
	 * Constructor.
	 * @param unit the unit to display
	 * @param desktop the main desktop.
	 */
	public TabPanelVehicles(Unit unit, MainDesktopPane desktop) { 
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelVehicles.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelVehicles.tooltip"), //$NON-NLS-1$
			unit, desktop
		);

		settlement = (Settlement) unit;

	}
	
	public boolean isUIDone() {
		return uiDone;
	}
	
	public void initializeUI() {
		uiDone = true;
		
		// Create vehicle label panel
		JPanel vehicleLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(vehicleLabelPanel);

		// Create vehicle label
		JLabel label = new JLabel(Msg.getString("TabPanelVehicles.parkedVehicles"), JLabel.CENTER); //$NON-NLS-1$
		label.setFont(new Font("Serif", Font.BOLD, 16));
		//label.setForeground(new Color(102, 51, 0)); // dark brown
		vehicleLabelPanel.add(label);

		// Create vehicle display panel
		JPanel vehicleDisplayPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
//		vehicleDisplayPanel.setBorder(new MarsPanelBorder());
		topContentPanel.add(vehicleDisplayPanel);

		// Create scroll panel for vehicle list.
		vehicleScrollPanel = new JScrollPane();
		vehicleScrollPanel.setPreferredSize(new Dimension(175, 200));
		vehicleDisplayPanel.add(vehicleScrollPanel);

		// Create vehicle list model
		vehicleListModel = new VehicleListModel(settlement);

		// Create vehicle list
		vehicleList = new JList<Vehicle>(vehicleListModel);
		vehicleList.addMouseListener(this);
		vehicleScrollPanel.setViewportView(vehicleList);
	}

	/**
	 * Updates the info on this panel.
	 */
	public void update() {
		if (!uiDone)
			initializeUI();
		
		// Update vehicle list
		vehicleListModel.update();
		vehicleScrollPanel.validate();
	}
	
	/**
     * List model for settlement vehicles.
     */
    private class VehicleListModel extends AbstractListModel<Vehicle> {

        /** default serial id. */
        private static final long serialVersionUID = 1L;
        
        private Settlement settlement;
        private List<Vehicle> vehicleList;
        
        private VehicleListModel(Settlement settlement) {
            this.settlement = settlement;
            
            vehicleList = new ArrayList<Vehicle>(settlement.getParkedVehicles());
            Collections.sort(vehicleList);
        }
        
        @Override
        public Vehicle getElementAt(int index) {
            
            Vehicle result = null;
            
            if ((index >= 0) && (index < vehicleList.size())) {
                result = vehicleList.get(index);
            }
            
            return result;
        }

        @Override
        public int getSize() {
            return vehicleList.size();
        }
        
        /**
         * Update the population list model.
         */
        public void update() {
            
            if (!vehicleList.containsAll(settlement.getParkedVehicles()) || 
                    !settlement.getParkedVehicles().containsAll(vehicleList)) {
                
                List<Vehicle> oldVehicleList = vehicleList;
                
                List<Vehicle> tempVehicleList = new ArrayList<Vehicle>(settlement.getParkedVehicles());
                Collections.sort(tempVehicleList);
                
                vehicleList = tempVehicleList;
                fireContentsChanged(this, 0, getSize());
                
                oldVehicleList.clear();
            }
        }
    }

	/** 
	 * Mouse clicked event occurs.
	 * @param event the mouse event
	 */
	public void mouseClicked(MouseEvent event) {
		// If double-click, open person window.
		if (event.getClickCount() >= 2) {
			Vehicle vehicle = (Vehicle) vehicleList.getSelectedValue();
			if (vehicle != null) {
				desktop.openUnitWindow(vehicle, false);
			}
		}
	}

	public void mousePressed(MouseEvent event) {}
	public void mouseReleased(MouseEvent event) {}
	public void mouseEntered(MouseEvent event) {}
	public void mouseExited(MouseEvent event) {}
	
	/**
     * Prepare object for garbage collection.
     */
    public void destroy() {
    	vehicleListModel = null;
    	vehicleList = null;
    	vehicleScrollPanel = null;
    }
}

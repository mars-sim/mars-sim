/**
 * Mars Simulation Project
 * VehicleMaintenanceBuildingPanel.java
 * @version 2.77 2004-09-27
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.standard.unit_window.structure.building;

import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import javax.swing.*;
import org.mars_sim.msp.simulation.structure.building.function.VehicleMaintenance;
import org.mars_sim.msp.simulation.vehicle.*;
import org.mars_sim.msp.ui.standard.MainDesktopPane;

/**
 * The VehicleMaintenanceBuildingPanel class is a building function panel representing 
 * the vehicle maintenance capabilities of the building.
 */
public class VehicleMaintenanceBuildingPanel extends BuildingFunctionPanel implements MouseListener {

	private VehicleMaintenance garage;
	private DecimalFormat formatter = new DecimalFormat("0.0");  // Decimal formatter.
	private JLabel vehicleMassLabel;
	private double vehicleMassCache = 0D;
	private JList vehicleList;
	private DefaultListModel vehicleListModel;
	private VehicleCollection vehicleCache;

	/**
	 * Constructor
	 * @param garage the vehicle maintenance function
	 * @param desktop the main desktop
	 */
	public VehicleMaintenanceBuildingPanel(VehicleMaintenance garage, MainDesktopPane desktop) {
		
		// Use BuildingFunctionPanel constructor
		super(garage.getBuilding(), desktop);
		
		// Initialize data members
		this.garage = garage;
		
		// Set panel layout
		setLayout(new BorderLayout());
		
		// Create label panel
		JPanel labelPanel = new JPanel(new GridLayout(3, 1, 0, 0));
		add(labelPanel, BorderLayout.NORTH);
        
		// Create vehicle maintenance label
		JLabel vehicleMaintenanceLabel = new JLabel("Vehicle Maintenance", JLabel.CENTER);
		labelPanel.add(vehicleMaintenanceLabel);
        
		// Create vehicle mass label
		vehicleMassCache = garage.getCurrentVehicleMass();
		vehicleMassLabel = new JLabel("Vehicle Mass: " + formatter.format(vehicleMassCache) + " kg.", JLabel.CENTER);
		labelPanel.add(vehicleMassLabel);
        
		// Create mass capacity label
		double massCapacity = garage.getVehicleCapacity();
		JLabel massCapacityLabel = new JLabel("Vehicle Mass Capacity: " + formatter.format(massCapacity) + " kg.", JLabel.CENTER);
		labelPanel.add(massCapacityLabel);	
		
		// Create vehicle list panel
		JPanel vehicleListPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		add(vehicleListPanel, BorderLayout.CENTER);
        
		// Create scroll panel for vehicle list
		JScrollPane vehicleScrollPanel = new JScrollPane();
		vehicleScrollPanel.setPreferredSize(new Dimension(160, 60));
		vehicleListPanel.add(vehicleScrollPanel);
        
		// Create vehicle list model
		vehicleListModel = new DefaultListModel();
		vehicleCache = garage.getVehicles();
		VehicleIterator i = vehicleCache.iterator();
		while (i.hasNext()) vehicleListModel.addElement(i.next());
        
		// Create vehicle list
		vehicleList = new JList(vehicleListModel);
		vehicleList.addMouseListener(this);
		vehicleScrollPanel.setViewportView(vehicleList);
	}
	
	/**
	 * Update this panel
	 */
	public void update() {
		// Update vehicle list and vehicle mass label
		if (!vehicleCache.equals(garage.getVehicles())) {
			vehicleCache = new VehicleCollection(garage.getVehicles());
			vehicleListModel.clear();
			VehicleIterator i = vehicleCache.iterator();
			while (i.hasNext()) vehicleListModel.addElement(i.next());
            
            vehicleMassCache = garage.getCurrentVehicleMass();
			vehicleMassLabel.setText("Vehicle Mass: " + formatter.format(vehicleMassCache) + " kg.");
		}
	}
	
	/** 
	 * Mouse clicked event occurs.
	 * @param event the mouse event
	 */
	public void mouseClicked(MouseEvent event) {
		// If double-click, open vehicle window.
		if (event.getClickCount() >= 2) 
			desktop.openUnitWindow((Vehicle) vehicleList.getSelectedValue());
	}

	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
}
/*
 * Mars Simulation Project
 * BuildingPanelVehicleMaintenance.java
 * @date 2021-09-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;

import org.apache.commons.collections.CollectionUtils;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.function.VehicleMaintenance;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;

/**
 * The BuildingPanelVehicleMaintenance class is a building function panel representing 
 * the vehicle maintenance capabilities of the building.
 */
@SuppressWarnings("serial")
public class BuildingPanelVehicleMaintenance
extends BuildingFunctionPanel
implements MouseListener {

	private VehicleMaintenance garage;
	private WebLabel vehicleNumberLabel;
	private int vehicleNumberCache = 0;
	private JList<Vehicle> vehicleList;
	private DefaultListModel<Vehicle> vehicleListModel;
	private Collection<Vehicle> vehicleCache;

	/**
	 * Constructor.
	 * @param garage the vehicle maintenance function
	 * @param desktop the main desktop
	 */
	public BuildingPanelVehicleMaintenance(VehicleMaintenance garage, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(Msg.getString("BuildingPanelVehicleMaintenance.title"), garage.getBuilding(), desktop);

		// Initialize data members
		this.garage = garage;
	}
	
	/**
	 * Build the UI
	 */
	@Override
	protected void buildUI(JPanel center) {

		// Create label panel
		WebPanel labelPanel = new WebPanel(new GridLayout(2, 1, 0, 0));
		center.add(labelPanel, BorderLayout.NORTH);
		labelPanel.setOpaque(false);
		labelPanel.setBackground(new Color(0,0,0,128));

		// Create vehicle number label
		vehicleNumberCache = garage.getCurrentVehicleNumber();
		vehicleNumberLabel = new WebLabel(Msg.getString("BuildingPanelVehicleMaintenance.numberOfVehicles",
				vehicleNumberCache), WebLabel.CENTER);
		labelPanel.add(vehicleNumberLabel);

		// Create vehicle capacity label
		int vehicleCapacity = garage.getVehicleCapacity();
		WebLabel vehicleCapacityLabel = new WebLabel(Msg.getString("BuildingPanelVehicleMaintenance.vehicleCapacity",
				vehicleCapacity), WebLabel.CENTER);
		labelPanel.add(vehicleCapacityLabel);	

		// Create vehicle list panel
		WebPanel vehicleListPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		center.add(vehicleListPanel, BorderLayout.CENTER);
		vehicleListPanel.setOpaque(false);
		vehicleListPanel.setBackground(new Color(0,0,0,128));
		
		// Create scroll panel for vehicle list
		WebScrollPane vehicleScrollPanel = new WebScrollPane();
		vehicleScrollPanel.setPreferredSize(new Dimension(160, 60));
		vehicleListPanel.add(vehicleScrollPanel);
		vehicleScrollPanel.setOpaque(false);
		vehicleScrollPanel.setBackground(new Color(0,0,0,128));
		vehicleScrollPanel.getViewport().setOpaque(false);
		vehicleScrollPanel.getViewport().setBackground(new Color(0,0,0,128));
		
		// Create vehicle list model
		vehicleListModel = new DefaultListModel<Vehicle>();
		vehicleCache = new ArrayList<Vehicle>(garage.getVehicles());
		Iterator<Vehicle> i = vehicleCache.iterator();
		while (i.hasNext()) vehicleListModel.addElement(i.next());

		// Create vehicle list
		vehicleList = new JList<Vehicle>(vehicleListModel);
		vehicleList.addMouseListener(this);
		vehicleScrollPanel.setViewportView(vehicleList);
	}

	/**
	 * Update this panel
	 */
	@Override
	public void update() {
		// Update vehicle list and vehicle mass label

		if (!CollectionUtils.isEqualCollection(vehicleCache, garage.getVehicles())) {
			vehicleCache = new ArrayList<Vehicle>(garage.getVehicles());
			vehicleListModel.clear();
			Iterator<Vehicle> i = vehicleCache.iterator();
			while (i.hasNext()) vehicleListModel.addElement(i.next());

			vehicleNumberCache = garage.getCurrentVehicleNumber();
			vehicleNumberLabel.setText(Msg.getString("BuildingPanelVehicleMaintenance.numberOfVehicles",
					vehicleNumberCache));
		}
	}

	/** 
	 * Mouse clicked event occurs.
	 * @param event the mouse event
	 */
	@Override
	public void mouseClicked(MouseEvent event) {
		// If double-click, open vehicle window.
		if (event.getClickCount() >= 2) {
			Vehicle vehicle = (Vehicle) vehicleList.getSelectedValue();
			if (vehicle != null) {
				desktop.openUnitWindow(vehicle, false);
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent arg0) {}
	
	@Override
	public void mouseReleased(MouseEvent arg0) {}
	
	@Override
	public void mouseEntered(MouseEvent arg0) {}
	
	@Override
	public void mouseExited(MouseEvent arg0) {}
}

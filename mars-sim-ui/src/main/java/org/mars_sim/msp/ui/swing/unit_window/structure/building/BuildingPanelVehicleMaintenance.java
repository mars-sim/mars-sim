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
import java.awt.GridLayout;
import java.util.Collection;

import javax.swing.JPanel;
import javax.swing.JTextField;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.function.VehicleMaintenance;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.UnitListPanel;

import com.alee.laf.panel.WebPanel;

/**
 * The BuildingPanelVehicleMaintenance class is a building function panel representing 
 * the vehicle maintenance capabilities of the building.
 */
@SuppressWarnings("serial")
public class BuildingPanelVehicleMaintenance
extends BuildingFunctionPanel {

	private VehicleMaintenance garage;
	private JTextField vehicleNumberLabel;
	private UnitListPanel<Vehicle> vehicleList;

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
		WebPanel labelPanel = new WebPanel(new GridLayout(2, 2, 0, 0));
		center.add(labelPanel, BorderLayout.NORTH);
		labelPanel.setOpaque(false);
		labelPanel.setBackground(new Color(0,0,0,128));

		// Create vehicle number label
		vehicleNumberLabel = addTextField(labelPanel, Msg.getString("BuildingPanelVehicleMaintenance.numberOfVehicles"),
				garage.getCurrentVehicleNumber(), null);

		// Create vehicle capacity label
		int vehicleCapacity = garage.getVehicleCapacity();
		addTextField(labelPanel, Msg.getString("BuildingPanelVehicleMaintenance.vehicleCapacity"),
				vehicleCapacity, null);

		// Create vehicle list panel
		vehicleList = new UnitListPanel<>(getDesktop(), new Dimension(160, 60)) {

			@Override
			protected Collection<Vehicle> getData() {
				return garage.getVehicles();
			}
		};
		addBorder(vehicleList, "Vehicles");
		center.add(vehicleList, BorderLayout.CENTER);
	}

	/**
	 * Update this panel
	 */
	@Override
	public void update() {
		// Update vehicle list and vehicle mass label

		if (vehicleList.update()) {
			vehicleNumberLabel.setText(Integer.toString(vehicleList.getUnitCount()));
		}
	}
}

/*
 * Mars Simulation Project
 * BuildingPanelVehicleMaintenance.java
 * @date 2022-09-21
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
import org.mars_sim.msp.core.vehicle.Flyer;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.UnitListPanel;

import com.alee.laf.panel.WebPanel;

/**
 * The BuildingPanelVehicleMaintenance class is a building function panel representing 
 * the vehicle maintenance capabilities of the building.
 */
@SuppressWarnings("serial")
public class BuildingPanelVehicleMaintenance extends BuildingFunctionPanel {

	private static final String SUV_ICON = Msg.getString("icon.suv"); //$NON-NLS-1$
	
	private VehicleMaintenance garage;
	
	private JTextField vehicleNumberLabel;
	private JTextField flyerNumberLabel;
	
	private UnitListPanel<Vehicle> vehicleList;
	private UnitListPanel<Flyer> flyerList;

	/**
	 * Constructor.
	 * @param garage the vehicle maintenance function
	 * @param desktop the main desktop
	 */
	public BuildingPanelVehicleMaintenance(VehicleMaintenance garage, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelVehicleMaintenance.title"),
			ImageLoader.getNewIcon(SUV_ICON),
			garage.getBuilding(), 
			desktop
		);

		// Initialize data members
		this.garage = garage;
	}
	
	/**
	 * Build the UI
	 */
	@Override
	protected void buildUI(JPanel center) {

		// Create label panel
		WebPanel labelPanel = new WebPanel(new GridLayout(4, 2, 0, 0));
		center.add(labelPanel, BorderLayout.NORTH);
		labelPanel.setOpaque(false);
		labelPanel.setBackground(new Color(0,0,0,128));

		// Create vehicle number label
		vehicleNumberLabel = addTextField(labelPanel, Msg.getString("BuildingPanelVehicleMaintenance.numberOfVehicles"),
				garage.getCurrentVehicleNumber(), 3, null);

		// Create vehicle capacity label
		int vehicleCapacity = garage.getVehicleCapacity();
		addTextField(labelPanel, Msg.getString("BuildingPanelVehicleMaintenance.vehicleCapacity"),
				vehicleCapacity, 3, null);

		// Create drone number label
		flyerNumberLabel = addTextField(labelPanel, Msg.getString("BuildingPanelVehicleMaintenance.numberOfFlyers"),
				garage.getCurrentFlyerNumber(), 3, null);

		// Create drone capacity label
		int droneCapacity = garage.getFlyerCapacity();
		addTextField(labelPanel, Msg.getString("BuildingPanelVehicleMaintenance.flyerCapacity"),
				droneCapacity, 3, null);
		
		// Create vehicle list panel
		vehicleList = new UnitListPanel<>(getDesktop(), new Dimension(260, 60)) {

			@Override
			protected Collection<Vehicle> getData() {
				return garage.getVehicles();
			}
		};
		
		JPanel listPanel = new JPanel(new BorderLayout());
		center.add(listPanel, BorderLayout.CENTER);
		
		JPanel vehiclePanel = new JPanel();
		vehiclePanel.add(vehicleList);
		addBorder(vehicleList, "Vehicles");
		listPanel.add(vehiclePanel, BorderLayout.NORTH);

		// Create drone list panel
		flyerList = new UnitListPanel<>(getDesktop(), new Dimension(260, 60)) {

			@Override
			protected Collection<Flyer> getData() {
				return garage.getFlyers();
			}
		};
		JPanel flyerPanel = new JPanel();
		flyerPanel.add(flyerList);
		addBorder(flyerList, "Drones");
		listPanel.add(flyerPanel, BorderLayout.CENTER);
	}

	/**
	 * Update this panel
	 */
	@Override
	public void update() {
		// Update vehicle and flyer list
		if (vehicleList.update()) {
			vehicleNumberLabel.setText(Integer.toString(vehicleList.getUnitCount()));
		}
		if (flyerList.update()) {
			flyerNumberLabel.setText(Integer.toString(flyerList.getUnitCount()));
		}
	}
}

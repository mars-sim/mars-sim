/*
 * Mars Simulation Project
 * BuildingPanelVehicleMaintenance.java
 * @date 2022-09-21
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.structure.building.function.VehicleMaintenance;
import org.mars_sim.msp.core.vehicle.Flyer;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.UnitListPanel;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;


/**
 * The BuildingPanelVehicleMaintenance class is a building function panel representing 
 * the vehicle maintenance capabilities of the building.
 */
@SuppressWarnings("serial")
public class BuildingPanelVehicleMaintenance extends BuildingFunctionPanel {

	private static final String SUV_ICON = "vehicle";
	
	private VehicleMaintenance garage;
	
	private JLabel vehicleNumberLabel;
	private JLabel flyerNumberLabel;
	
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
			ImageLoader.getIconByName(SUV_ICON),
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
		AttributePanel labelPanel = new AttributePanel(2,2);
		center.add(labelPanel, BorderLayout.NORTH);
		labelPanel.setOpaque(false);

		// Create vehicle number label
		vehicleNumberLabel = labelPanel.addTextField(Msg.getString("BuildingPanelVehicleMaintenance.numberOfVehicles"),
									Integer.toString(garage.getCurrentVehicleNumber()), null);

		// Create vehicle capacity label
		int vehicleCapacity = garage.getVehicleCapacity();
		labelPanel.addTextField(Msg.getString("BuildingPanelVehicleMaintenance.vehicleCapacity"),
									Integer.toString(vehicleCapacity), null);

		// Create drone number label
		flyerNumberLabel = labelPanel.addTextField(Msg.getString("BuildingPanelVehicleMaintenance.numberOfFlyers"),
									Integer.toString(garage.getCurrentFlyerNumber()), null);

		// Create drone capacity label
		int droneCapacity = garage.getFlyerCapacity();
		labelPanel.addTextField(Msg.getString("BuildingPanelVehicleMaintenance.flyerCapacity"),
									Integer.toString(droneCapacity), null);
		
		// Create vehicle list panel
		vehicleList = new UnitListPanel<>(getDesktop()) {

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
		flyerList = new UnitListPanel<>(getDesktop()) {

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

/*
 * Mars Simulation Project
 * BuildingPanelVehicleMaintenance.java
 * @date 2025-07-20
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mars_sim.core.building.function.VehicleMaintenance;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Flyer;
import com.mars_sim.core.vehicle.LightUtilityVehicle;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.unit_window.UnitListPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;


/**
 * The BuildingPanelVehicleMaintenance class is a building function panel representing 
 * the vehicle maintenance capabilities of the building.
 */
@SuppressWarnings("serial")
public class BuildingPanelVehicleMaintenance extends BuildingFunctionPanel {

	private static final String SUV_ICON = "vehicle";

	/** Is UI constructed. */
	private boolean uiDone = false;

	private VehicleMaintenance garage;
	
	private JLabel roverNumberLabel;
	private JLabel luvNumberLabel;
	private JLabel flyerNumberLabel;
	
	private UnitListPanel<Rover> roverList;
	private UnitListPanel<LightUtilityVehicle> luvList;
	private UnitListPanel<Flyer> flyerList;

	/**
	 * Constructor.
	 * 
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
	 * Builds the UI.
	 */
	@Override
	protected void buildUI(JPanel center) {

		// Create label panel
		AttributePanel labelPanel = new AttributePanel(3,2);
		center.add(labelPanel, BorderLayout.NORTH);
		labelPanel.setOpaque(false);

		// Create vehicle number label
		roverNumberLabel = labelPanel.addTextField(Msg.getString("BuildingPanelVehicleMaintenance.numberOfRovers"),
									Integer.toString(garage.getCurrentRoverNumber()), null);

		// Create rover capacity label
		int roverCapacity = garage.getRoverCapacity();
		labelPanel.addTextField(Msg.getString("BuildingPanelVehicleMaintenance.roverCapacity"),
									Integer.toString(roverCapacity), null);

	
		// Create drone number label
		luvNumberLabel = labelPanel.addTextField(Msg.getString("BuildingPanelVehicleMaintenance.numberOfLUVs"),
									Integer.toString(garage.getCurrentUtilityVehicleNumber()), null);

		// Create drone capacity label
		int luvCapacity = garage.getUtilityVehicleCapacity();
		labelPanel.addTextField(Msg.getString("BuildingPanelVehicleMaintenance.luvCapacity"),
									Integer.toString(luvCapacity), null);
		
		// Create drone number label
		flyerNumberLabel = labelPanel.addTextField(Msg.getString("BuildingPanelVehicleMaintenance.numberOfFlyers"),
									Integer.toString(garage.getCurrentFlyerNumber()), null);

		// Create drone capacity label
		int droneCapacity = garage.getFlyerCapacity();
		labelPanel.addTextField(Msg.getString("BuildingPanelVehicleMaintenance.flyerCapacity"),
									Integer.toString(droneCapacity), null);
		
		// Create vehicle list panel
		roverList = new UnitListPanel<>(getDesktop()) {

			@Override
			protected Collection<Rover> getData() {
				return garage.getRovers();
			}
		};
		
		JPanel listPanel = new JPanel(new BorderLayout());
		center.add(listPanel, BorderLayout.CENTER);
		
		JPanel vehiclePanel = new JPanel();
		vehiclePanel.add(roverList);
		addBorder(roverList, "Vehicles");
		listPanel.add(vehiclePanel, BorderLayout.NORTH);

		// Create luv list panel
		luvList = new UnitListPanel<>(getDesktop()) {

			@Override
			protected Collection<LightUtilityVehicle> getData() {
				return garage.getUtilityVehicles();
			}
		};
		JPanel luvPanel = new JPanel();
		luvPanel.add(luvList);
		addBorder(luvList, "LUVs");
		listPanel.add(luvPanel, BorderLayout.CENTER);
		
		// Create drone list panel
		flyerList = new UnitListPanel<>(getDesktop()) {

			@Override
			protected Collection<Flyer> getData() {
				return garage.getFlyers();
			}
		};
		JPanel flyerPanel = new JPanel();
		flyerPanel.add(flyerList);
		addBorder(flyerList, "Flyers");
		listPanel.add(flyerPanel, BorderLayout.SOUTH);
	}

	/**
	 * Updates this panel.
	 */
	@Override
	public void update() {	
		if (!uiDone)
			initializeUI();
		
		// Update the 3 lists
		if (roverList.update()) {
			roverNumberLabel.setText(Integer.toString(roverList.getUnitCount()));
		}
		if (luvList.update()) {
			luvNumberLabel.setText(Integer.toString(luvList.getUnitCount()));
		}
		if (flyerList.update()) {
			flyerNumberLabel.setText(Integer.toString(flyerList.getUnitCount()));
		}
	}
}

/*
 * Mars Simulation Project
 * BuildingPanelVehicleMaintenance.java
 * @date 2025-07-20
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.entitywindow.building;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import com.mars_sim.core.Entity;
import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.VehicleMaintenance;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Flyer;
import com.mars_sim.core.vehicle.LightUtilityVehicle;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTableTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.EntityModel;


/**
 * The BuildingPanelVehicleMaintenance class is a building function panel representing 
 * the vehicle maintenance capabilities of the building.
 */
@SuppressWarnings("serial")
class BuildingPanelVehicleMaintenance extends EntityTableTabPanel<Building>
     implements EntityListener {

	private static final String SUV_ICON = "vehicle";

	private VehicleMaintenance garage;
	
	private VehicleModel model;

	/**
	 * Constructor.
	 * 
	 * @param garage the vehicle maintenance function
	 * @param context the UI context
	 */
	public BuildingPanelVehicleMaintenance(VehicleMaintenance garage, UIContext context) {

		super(
			Msg.getString("BuildingPanelVehicleMaintenance.title"),
			ImageLoader.getIconByName(SUV_ICON), null,
			garage.getBuilding(), context
		);

		this.garage = garage;
		setTableTitle(Msg.getString("vehicle.plural"));
	}
	
	
	@Override
	protected TableModel createModel() {
		model = new VehicleModel(garage);
		return model;
	}

	/**
	 * Create the fixed info panel showing capabilities of Garage.
	 */
	@Override
	protected JPanel createInfoPanel(){

		// Create label panel
		AttributePanel labelPanel = new AttributePanel();

		// Create rover capacity label
		int roverCapacity = garage.getRoverCapacity();
		labelPanel.addTextField(Msg.getString("BuildingPanelVehicleMaintenance.roverCapacity"),
									Integer.toString(roverCapacity), null);

		// Create drone capacity label
		int luvCapacity = garage.getUtilityVehicleCapacity();
		labelPanel.addTextField(Msg.getString("BuildingPanelVehicleMaintenance.luvCapacity"),
									Integer.toString(luvCapacity), null);

		// Create drone capacity label
		int droneCapacity = garage.getFlyerCapacity();
		labelPanel.addTextField(Msg.getString("BuildingPanelVehicleMaintenance.flyerCapacity"),
									Integer.toString(droneCapacity), null);		
		return labelPanel;
	}

	/**
	 * When any event happens on the Building, update the lists.
	 */
	@Override
	public void entityUpdate(EntityEvent event) {
		model.update();
	}

	/**
	 * Simple table model for vehicles in the garage.
	 */
	private static class VehicleModel  extends AbstractTableModel implements EntityModel {

		private static final String NAME = Msg.getString("entity.name");
		private static final String TYPE = Msg.getString("vehicle.type");

		private static final long serialVersionUID = 1L;
		private static final int NAME_COL = 0;
		private static final int TYPE_COL = 1;

		private List<Vehicle> vehicles;
		private VehicleMaintenance garage;
		
		public VehicleModel(VehicleMaintenance garage) {
			this.garage = garage;
			vehicles = loadVehicles();
		}

		/**
		 * Update if there is a change in the number fo vehicles
		 */
		public void update() {
			var newVehicles = loadVehicles();
			if (newVehicles.size() != vehicles.size()) {
				vehicles = newVehicles;
				fireTableDataChanged();
			}
		}

		private List<Vehicle> loadVehicles() {
			List<Vehicle> list = new ArrayList<>();
			list.addAll(garage.getRovers());
			list.addAll(garage.getUtilityVehicles());
			list.addAll(garage.getFlyers());
			return list;
		}

		@Override
		public int getRowCount() {
			return vehicles.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(int column) {
			return switch(column) {
				case NAME_COL -> NAME;
				case TYPE_COL -> TYPE;
				default -> "";
			};
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Vehicle vehicle = vehicles.get(rowIndex);
			return switch(columnIndex) {
				case NAME_COL -> vehicle.getName();
				case TYPE_COL -> getType(vehicle);
				default -> "";
			};
		}

		private static String getType(Vehicle v) {
			if (v instanceof Rover) {
				return Msg.getString("rover.singular");
			} else if (v instanceof LightUtilityVehicle) {
				return Msg.getString("lightUtilityVehicle.singular");
			} else if (v instanceof Flyer) {
				return Msg.getString("flyer.singular");
			} else {
				return "Unknown";
			}
		}

		@Override
		public Entity getAssociatedEntity(int row) {
			return vehicles.get(row);
		}
	}
}

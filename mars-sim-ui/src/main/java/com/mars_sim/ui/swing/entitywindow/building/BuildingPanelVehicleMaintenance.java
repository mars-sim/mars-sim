/*
 * Mars Simulation Project
 * BuildingPanelVehicleMaintenance.java
 * @date 2025-07-20
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.entitywindow.building;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.table.TableModel;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.VehicleMaintenance;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.AttributePanel;
import com.mars_sim.ui.swing.entitywindow.EntityTableTabPanel;
import com.mars_sim.ui.swing.utils.model.BaseVehicleModel;


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
		if (event.getType().equals(VehicleMaintenance.GARAGED) && model != null) {
			model.update();
		}
	}

	/**
	 * Simple table model for vehicles in the garage.
	 */
	private static class VehicleModel  extends BaseVehicleModel {

		private static final long serialVersionUID = 1L;

		private VehicleMaintenance garage;
		
		public VehicleModel(VehicleMaintenance garage) {
			super(NAME, TYPE, STATUS);
			this.garage = garage;
			update();
		}

		/**
		 * Update if there is a change in the number fo vehicles
		 */
		public void update() {
			Set<Vehicle> list = new HashSet<>();
			list.addAll(garage.getRovers());
			list.addAll(garage.getUtilityVehicles());
			list.addAll(garage.getFlyers());
			setEntities(list);
		}
	}
}

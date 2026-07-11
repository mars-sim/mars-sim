/*
 * Mars Simulation Project
 * RescueVehiclePanel.java
 * @date 2026-02-09
 * @author Barry Evans
 */

package com.mars_sim.ui.swing.tool.missionwizard;

import java.util.List;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.person.ai.mission.RescueSalvageVehicle;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.utils.model.BaseVehicleModel;
import com.mars_sim.ui.swing.utils.wizard.WizardItemModel;
import com.mars_sim.ui.swing.utils.wizard.WizardItemStep;
import com.mars_sim.ui.swing.utils.wizard.WizardPane;

/**
 * A wizard panel for selecting a Vehicle in need of Rescue
 */
@SuppressWarnings("serial")
class RescueVehiclePanel extends WizardItemStep<MissionDataBean, Vehicle> {

	/** The wizard panel name. */
	public static final String ID = "rescue_vehicle";

	/**
	 * Constructor.
	 * 
	 * @param wizard the create mission wizard.
	 */
	RescueVehiclePanel(WizardPane<MissionDataBean> parent, MissionDataBean state) {
		// Use WizardPanel constructor.
		super(ID, parent, new VehicleTableModel(state));
	}

	/**
	 * Clear information on the wizard panel.
	 */
	@Override
	public void clearState(MissionDataBean state) {
		state.setRescueVehicle(null);
		super.clearState(state);
	}

	/**
	 * Update the state with the selected vehicle.
	 */
	@Override
	protected void updateState(MissionDataBean state, List<Vehicle> sel) {
		state.setRescueVehicle(sel.get(0));
	}

	/**
	 * A table model for vehicles.
	 */
	private static class VehicleTableModel extends BaseVehicleModel
			implements WizardItemModel<Vehicle> {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private static final int RANGE_VAL = 101;
		private static final EntityColumnSpec RANGE = new EntityColumnSpec(new ColumnSpec(RANGE_VAL, "Distance", Double.class,
									ColumnSpec.STYLE_DIGIT1), null);
		private static final int RESCUE_VAL = 102;
		private static final EntityColumnSpec RESCUE = new EntityColumnSpec(new ColumnSpec(RESCUE_VAL, "Rescuing Rover",
									String.class), null);

		private MissionDataBean state;

		/**
		 * Constructor
		 */
		private VehicleTableModel(MissionDataBean state) {
			super(NAME, RANGE, ONBOARD, STATUS, RESCUE);

			addResourceColumns(List.of(ResourceUtil.OXYGEN_ID, ResourceUtil.WATER_ID, ResourceUtil.FOOD_ID));
			this.state = state;
		
			// Bit mess but calling is rare
			var unitMgr = Simulation.instance().getUnitManager();

			var r = unitMgr.getVehicles().stream()
					.filter(Vehicle::isBeaconOn)
					.toList();

			setEntities(r);
			enableListeners(true);
		}

		@Override
		public Vehicle getItem(int row) {
			return (Vehicle) getAssociatedEntity(row);
		}

		/**	
		 * Returns the value for the table cell.
		 * @param vehicle the vehicle.
		 * @param column the column value.
		 * @return the cell value.
		 */
		@Override
		protected Object getEntityValue(Vehicle vehicle, int column) {
			return switch(column) {
				case RANGE_VAL -> state.getStartingSettlement().getCoordinates().getDistance(vehicle.getCoordinates());
				case RESCUE_VAL -> {
					var helper = RescueSalvageVehicle.getRescueingVehicle(vehicle);
					yield helper != null ? helper.getName() : "";
				}
				default -> super.getEntityValue(vehicle, column);
			};
		}	

		/**
		 * Check for failure cells.
		 */
		@Override
		public String isFailureCell(int row, int column) {
			var colSpec = getColumnSpec(column);
			var vehicle = getItem(row);
			if (colSpec.equals(RANGE.column())) {
				var distance = state.getStartingSettlement().getCoordinates().getDistance(vehicle.getCoordinates())
						* 2.2D; // Round trip distance
				return (distance > state.getRover().getRange()) ? MissionCreate.VEHICLE_OUT_OF_RANGE : null;
			}
			else if (colSpec.equals(RESCUE.column())) {
				return RescueSalvageVehicle.getRescueingVehicle(vehicle) != null ? "Already being rescued" : null;
			}
			return null;
		}
	}
}
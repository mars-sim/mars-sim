/*
 * Mars Simulation Project
 * RescueVehiclePanel.java
 * @date 2026-02-09
 * @author Barry Evans
 */

package com.mars_sim.ui.swing.tool.missionwizard;

import java.util.Comparator;
import java.util.List;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.person.ai.mission.RescueSalvageVehicle;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.tool.mission.create.MissionDataBean;
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
	private static class VehicleTableModel extends WizardItemModel<Vehicle> {

		/** default serial id. */
		private static final long serialVersionUID = 1L;
		private static final List<ColumnSpec> COLUMNS = List.of(
				new ColumnSpec(Msg.getString("entity.name"), String.class),
				new ColumnSpec("Distance", Double.class, ColumnSpec.STYLE_DIGIT1),
				new ColumnSpec("Crew", Integer.class),
				new ColumnSpec(Msg.getString("vehicle.status"), String.class),
				new ColumnSpec("Oxygen", Double.class, ColumnSpec.STYLE_DIGIT1),
				new ColumnSpec("Water", Double.class, ColumnSpec.STYLE_DIGIT1),
				new ColumnSpec("Food", Double.class, ColumnSpec.STYLE_DIGIT1),
				new ColumnSpec("Rescuing Rover", String.class)
		);
				
		private MissionDataBean state;

		/**
		 * Constructor
		 */
		private VehicleTableModel(MissionDataBean state) {
			super(COLUMNS);
			this.state = state;
		
			// Bit mess but calling is rare
			var unitMgr = Simulation.instance().getUnitManager();

			var r = unitMgr.getVehicles().stream()
					.filter(Vehicle::isBeaconOn)
					.sorted(Comparator.comparing(Vehicle::getName))
					.toList();

			setItems(r);
		}

		/**	
		 * Returns the value for the table cell.
		 * @param vehicle the vehicle.
		 * @param column the table column.
		 * @return the cell value.
		 */
		@Override
		protected Object getItemValue(Vehicle vehicle, int column) {
			return switch(column) {
				case 0 -> vehicle.getName();
				case 1 -> state.getStartingSettlement().getCoordinates().getDistance(vehicle.getCoordinates());
				case 2 -> {
					if (vehicle instanceof Rover r)
						yield r.getCrewNum();
					else
						yield 0;
				} 
				case 3 -> vehicle.printStatusTypes();
				case 4 -> vehicle.getSpecificAmountResourceStored(ResourceUtil.OXYGEN_ID);
				case 5 -> vehicle.getSpecificAmountResourceStored(ResourceUtil.WATER_ID);
				case 6 -> vehicle.getSpecificAmountResourceStored(ResourceUtil.FOOD_ID);
				case 7 -> { 
					var helper = RescueSalvageVehicle.getRescueingVehicle(vehicle);
					yield helper != null ? helper.getName() : "";
				}
				default -> null;
			};
		}	

		/**
		 * Check for failure cells.
		 */
		@Override
		protected String isFailureCell(Vehicle vehicle, int column) {
			return switch(column) {
				case 7 -> RescueSalvageVehicle.getRescueingVehicle(vehicle) != null ? "Already being rescued" : null;
				case 1 -> {
    				var distance = state.getStartingSettlement().getCoordinates().getDistance(vehicle.getCoordinates())
							* 2.2D; // Round trip distance
    				yield (distance > state.getRover().getRange()) ? MissionCreate.VEHICLE_OUT_OF_RANGE : null;
				}
				default -> null;
			};
		}
	}
}
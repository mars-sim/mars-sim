/*
 * Mars Simulation Project
 * DronePanel.java
 * @date 2026-02-08
 * @author Barry Evans
 */

package com.mars_sim.ui.swing.tool.missionwizard;

import java.util.Comparator;
import java.util.List;

import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Drone;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.tool.mission.create.MissionDataBean;
import com.mars_sim.ui.swing.utils.wizard.AbstractWizardItemModel;
import com.mars_sim.ui.swing.utils.wizard.WizardItemStep;
import com.mars_sim.ui.swing.utils.wizard.WizardPane;

/**
 * A wizard panel for selecting the mission Drone.
 */
@SuppressWarnings("serial")
class DronePanel extends WizardItemStep<MissionDataBean, Drone> {

	/** The wizard panel name. */
	public static final String ID = "Drone";

	/**
	 * Constructor.
	 * 
	 * @param wizard the create mission wizard.
	 */
	DronePanel(WizardPane<MissionDataBean> parent, MissionDataBean state) {
		super(ID, parent, new DroneTableModel(state));
	}

	/**
	 * Clear information on the wizard panel.
	 */
	@Override
	public void clearState(MissionDataBean state) {
		state.setDrone(null);
		super.clearState(state);
	}

	/**
	 * Update the state with the selected vehicle.
	 */
	@Override
	protected void updateState(MissionDataBean state, List<Drone> sel) {
		state.setDrone(sel.get(0));
	}

	/**
	 * A table model for vehicles.
	 */
	private static class DroneTableModel extends AbstractWizardItemModel<Drone> {

		/** default serial id. */
		private static final long serialVersionUID = 1L;
		private static final List<ColumnSpec> COLUMNS = List.of(
				new ColumnSpec(Msg.getString("entity.name"), String.class),
				new ColumnSpec(Msg.getString("vehicle.type"), String.class),
				new ColumnSpec(Msg.getString("vehicle.range"), Double.class, ColumnSpec.STYLE_DIGIT1),
				new ColumnSpec("Capacity", Double.class, ColumnSpec.STYLE_DIGIT1),
				new ColumnSpec(Msg.getString("vehicle.status"), String.class),
				new ColumnSpec("Reserved", Boolean.class),
				new ColumnSpec(Msg.getString("mission.singular"), String.class)
		);
				
		private MissionDataBean state;

		/**
		 * Constructor
		 */
		private DroneTableModel(MissionDataBean state) {
			super(COLUMNS);
			this.state = state;
		
			var startingSettlement = state.getStartingSettlement();
			var r = startingSettlement.getParkedGaragedDrones().stream()
					.filter(Drone.class::isInstance)
					.map(Drone.class::cast)
					.sorted(Comparator.comparing(Drone::getName))
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
		protected Object getItemValue(Drone vehicle, int column) {
			return switch(column) {
				case 0 -> vehicle.getName();
				case 1 -> vehicle.getVehicleSpec().getName();
				case 2 -> vehicle.getEstimatedRange();
				case 3 -> vehicle.getCargoCapacity();
				case 4 -> vehicle.printStatusTypes();
				case 5 -> vehicle.isReserved();
				case 6 -> {
					Mission mission = vehicle.getMission();
					yield (mission != null ? mission.getName() : null);
				}
				default -> null;
			};
		}

		/**
		 * Check for failure cells.
		 */
		@Override
		protected String isFailureCell(Drone vehicle, int column) {
			String result = null;

			if (column == 5) {
				return (vehicle.isReserved() ? "Reserved" : null);
			} else if (column == 4) {
				if ((vehicle.getPrimaryStatus() != StatusType.PARKED) 
						&& (vehicle.getPrimaryStatus() != StatusType.GARAGED))
					return MissionCreate.VEHICLE_WRONG_STATUS;

				// Allow rescue/salvage mission to use vehicle undergoing maintenance.
				if (MissionType.RESCUE_SALVAGE_VEHICLE == state.getMissionType()) {
                    result = vehicle.haveStatusType(StatusType.MAINTENANCE) ? "Vehicle is undergoing maintenance" : null;
					}
			} else if (column == 6) {
				Mission mission = vehicle.getMission();
				return (mission != null) ? MissionCreate.ALREADY_ON_MISSION : null;
			}

			return result;
		}
	}
}
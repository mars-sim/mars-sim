/*
 * Mars Simulation Project
 * VehiclePanel.java
 * @date 2024-07-30
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.tool.missionwizard;

import java.util.List;

import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.utils.model.BaseVehicleModel;
import com.mars_sim.ui.swing.utils.wizard.WizardItemModel;
import com.mars_sim.ui.swing.utils.wizard.WizardItemStep;
import com.mars_sim.ui.swing.utils.wizard.WizardPane;

/**
 * A wizard panel for selecting the mission vehicle.
 */
@SuppressWarnings("serial")
class VehiclePanel extends WizardItemStep<MissionDataBean, Vehicle> {

	/** The wizard panel name. */
	public static final String ID = "Vehicle";

	/**
	 * Constructor.
	 * 
	 * @param wizard the create mission wizard.
	 */
	VehiclePanel(WizardPane<MissionDataBean> parent, MissionDataBean state) {
		// Use WizardPanel constructor.
		super(ID, parent, new VehicleTableModel(state));
	}

	/**
	 * Clear information on the wizard panel.
	 */
	@Override
	public void clearState(MissionDataBean state) {
		state.setVehicle(null);
		super.clearState(state);
	}

	/**
	 * Update the state with the selected vehicle.
	 */
	@Override
	protected void updateState(MissionDataBean state, List<Vehicle> sel) {
		state.setVehicle(sel.get(0));
	}

	/**
	 * A table model for vehicles.
	 */
	private static class VehicleTableModel extends BaseVehicleModel
		implements WizardItemModel<Vehicle> {

		/** default serial id. */
		private static final long serialVersionUID = 1L;
	
		/**
		 * Constructor
		 */
		private VehicleTableModel(MissionDataBean state) {
			super(NAME, TYPE, EST_RANGE, CARGO_CAPACITY, STATUS, RESERVED, MISSION,
					CREW_CAPACITY, HAS_LAB);
		
			var prefered = state.getMetaMission().getPreferredVehicle();

			var startingSettlement = state.getStartingSettlement();
			var r = startingSettlement.getParkedNGaragedVehicles().stream()
					.filter(v -> prefered.contains(v.getVehicleType()))
					.toList();
			setEntities(r);
			enableListeners(true);
		}

		@Override
		public Vehicle getItem(int row) {
			return (Vehicle) getAssociatedEntity(row);
		}

		/**
		 * Check for failure cells.
		 */
		@Override
		public String isFailureCell(int row, int column) {
			var colSpec = getColumnSpec(column);
			var vehicle = getItem(row);

			if (colSpec.equals(RESERVED.column())) {
				return (vehicle.isReserved() ? "Reserved" : null);
			}
			else if (colSpec.equals(STATUS.column())) {
				if ((vehicle.getPrimaryStatus() != StatusType.PARKED) 
						&& (vehicle.getPrimaryStatus() != StatusType.GARAGED))
					return MissionCreate.VEHICLE_WRONG_STATUS;
			}
			else if (colSpec.equals(MISSION.column())) {
				Mission mission = vehicle.getMission();
				return (mission != null) ? MissionCreate.ALREADY_ON_MISSION : null;
			}
			return null;
		}
	}
}
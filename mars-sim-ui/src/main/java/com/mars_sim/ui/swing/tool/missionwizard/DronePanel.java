/*
 * Mars Simulation Project
 * DronePanel.java
 * @date 2026-02-08
 * @author Barry Evans
 */

package com.mars_sim.ui.swing.tool.missionwizard;

import java.util.List;

import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.vehicle.Drone;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.ui.swing.utils.model.BaseVehicleModel;
import com.mars_sim.ui.swing.utils.wizard.WizardItemModel;
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
	private static class DroneTableModel extends BaseVehicleModel implements WizardItemModel<Drone> {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		/**
		 * Constructor
		 */
		private DroneTableModel(MissionDataBean state) {
			super(NAME, TYPE, EST_RANGE, CARGO_CAPACITY, STATUS, RESERVED, MISSION);
		
			var startingSettlement = state.getStartingSettlement();
			var r = startingSettlement.getParkedGaragedDrones().stream()
					.filter(Drone.class::isInstance)
					.toList();
			setEntities(r);
			enableListeners(true);
		}

		@Override
		public Drone getItem(int row) {
			return (Drone)getAssociatedEntity(row);
		}

		/**
		 * Check for failure cells.
		 */
		@Override
		public String isFailureCell(int row, int column) {
			var colSpec = getColumnSpec(column);
			var vehicle = (Drone)getAssociatedEntity(row);

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
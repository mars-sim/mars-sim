/**
 * Mars Simulation Project
 * LightUtilityVehiclePanel.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.tool.missionwizard;

import java.util.List;

import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.vehicle.LightUtilityVehicle;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.ui.swing.utils.model.BaseVehicleModel;
import com.mars_sim.ui.swing.utils.wizard.WizardItemModel;
import com.mars_sim.ui.swing.utils.wizard.WizardItemStep;

/**
 * A wizard panel for selecting the mission light utility vehicle.
 */
@SuppressWarnings("serial")
class LightUtilityVehiclePanel extends WizardItemStep<MissionDataBean,LightUtilityVehicle> {

	// The wizard panel name.
	public static final String ID = "LUV";

	/**
	 * Constructor.
	 * @param wizard the create mission wizard.
	 */
	public LightUtilityVehiclePanel(MissionCreate wizard, MissionDataBean state) {
		super(ID, wizard, new VehicleTableModel(state));
	
	}
	

	@Override
	protected void updateState(MissionDataBean state, List<LightUtilityVehicle> selectedItems) {
		state.setLUV(selectedItems.get(0));
	}

	/**
	 * Clear information on the wizard panel.
	 */
	@Override
	public void clearState(MissionDataBean state) {
		super.clearState(state);
		state.setLUV(null);
	}

	/**
	 * A table model for vehicles.
	 */
	private static class VehicleTableModel extends BaseVehicleModel
			implements WizardItemModel<LightUtilityVehicle> {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		/** hidden Constructor. */
		private VehicleTableModel(MissionDataBean state) {
			super(NAME, STATUS, MISSION);

			var l = state.getStartingSettlement().getParkedGaragedVehicles().stream()
						.filter(LightUtilityVehicle.class::isInstance)
						.map(LightUtilityVehicle.class::cast)
						.toList();
			setEntities(l);
			enableListeners(true);
		}

		@Override
		public LightUtilityVehicle getItem(int row) {
			return (LightUtilityVehicle) getAssociatedEntity(row);
		}

		@Override
		public String isFailureCell(int row, int column) {
			var colSpec = getColumnSpec(column);
			var vehicle = getItem(row);

			if (colSpec.equals(STATUS.column())) {
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

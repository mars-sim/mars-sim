/**
 * Mars Simulation Project
 * LightUtilityVehiclePanel.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.tool.missionwizard;

import java.util.List;

import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.LightUtilityVehicle;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.tool.mission.create.MissionDataBean;
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
	private static class VehicleTableModel extends WizardItemModel<LightUtilityVehicle> {

		/** default serial id. */
		private static final long serialVersionUID = 1L;
		private static final List<ColumnSpec> COLUMNS = List.of(
				new ColumnSpec(Msg.getString("entity.name"), String.class),
				new ColumnSpec(Msg.getString("vehicle.status"), String.class),
				new ColumnSpec(Msg.getString("mission.singular"), String.class)
		);

		/** hidden Constructor. */
		private VehicleTableModel(MissionDataBean state) {
			super(COLUMNS);

			var l = state.getStartingSettlement().getParkedGaragedVehicles().stream()
						.filter(LightUtilityVehicle.class::isInstance)
						.map(LightUtilityVehicle.class::cast)
						.toList();
			setItems(l);
		}


		@Override
		protected Object getItemValue(LightUtilityVehicle vehicle, int column) {
			return switch(column) {
				case 0 -> vehicle.getName();
				case 1 -> vehicle.printStatusTypes();
				case 2 -> {
							var m = vehicle.getMission();
							yield (m != null ? m.getName() : "");
				}
				default -> null;
			};
		}

		@Override
		protected boolean isFailureCell(LightUtilityVehicle vehicle, int column) {
			boolean result = false;

			if (column == 1) {
				result = (vehicle.getPrimaryStatus() != StatusType.PARKED) && (vehicle.getPrimaryStatus() != StatusType.GARAGED);
			}
			else if (column == 2) {
				result = vehicle.getMission() != null;
			}

			return result;
		}
	}
}

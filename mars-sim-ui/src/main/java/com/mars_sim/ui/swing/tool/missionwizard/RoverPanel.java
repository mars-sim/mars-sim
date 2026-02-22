/*
 * Mars Simulation Project
 * RoverPanel.java
 * @date 2024-07-30
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.tool.missionwizard;

import java.util.Comparator;
import java.util.List;

import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.utils.wizard.AbstractWizardItemModel;
import com.mars_sim.ui.swing.utils.wizard.WizardItemStep;
import com.mars_sim.ui.swing.utils.wizard.WizardPane;

/**
 * A wizard panel for selecting the mission Rover.
 */
@SuppressWarnings("serial")
class RoverPanel extends WizardItemStep<MissionDataBean, Rover> {

	/** The wizard panel name. */
	public static final String ID = "Rover";

	/**
	 * Constructor.
	 * 
	 * @param wizard the create mission wizard.
	 */
	RoverPanel(WizardPane<MissionDataBean> parent, MissionDataBean state) {
		// Use WizardPanel constructor.
		super(ID, parent, new VehicleTableModel(state));
	}

	/**
	 * Clear information on the wizard panel.
	 */
	@Override
	public void clearState(MissionDataBean state) {
		state.setRover(null);
		super.clearState(state);
	}

	/**
	 * Update the state with the selected vehicle.
	 */
	@Override
	protected void updateState(MissionDataBean state, List<Rover> sel) {
		state.setRover(sel.get(0));
	}

	/**
	 * A table model for vehicles.
	 */
	private static class VehicleTableModel extends AbstractWizardItemModel<Rover> {

		/** default serial id. */
		private static final long serialVersionUID = 1L;
		private static final List<ColumnSpec> COLUMNS = List.of(
				new ColumnSpec(Msg.getString("entity.name"), String.class),
				new ColumnSpec(Msg.getString("vehicle.type"), String.class),
				new ColumnSpec(Msg.getString("vehicle.range"), Double.class, ColumnSpec.STYLE_DIGIT1),
				new ColumnSpec("Capacity", Double.class, ColumnSpec.STYLE_DIGIT1),
				new ColumnSpec(Msg.getString("vehicle.status"), String.class),
				new ColumnSpec("Reserved", Boolean.class),
				new ColumnSpec(Msg.getString("mission.singular"), String.class),
				new ColumnSpec("Crew", Integer.class),
				new ColumnSpec("Has Lab", Boolean.class),
				new ColumnSpec("Has Sickbay", Boolean.class)
		);
				
		/**
		 * Constructor
		 */
		private VehicleTableModel(MissionDataBean state) {
			super(COLUMNS);
		
			var startingSettlement = state.getStartingSettlement();
			var r = startingSettlement.getParkedGaragedVehicles().stream()
					.filter(Rover.class::isInstance)
					.map(Rover.class::cast)
					.sorted(Comparator.comparing(Rover::getName))
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
		protected Object getItemValue(Rover vehicle, int column) {
			return switch(column) {
				case 0 -> vehicle.getName();
				case 1 -> vehicle.getVehicleSpec().getName();
				case 2 -> vehicle.getEstimatedRange();
				case 3 -> vehicle.getCargoCapacity();
				case 4 -> vehicle.printStatusTypes();
				case 5 -> vehicle.isReserved();
				case 6 -> {
					Mission mission = vehicle.getMission();
					if (mission != null)
						yield mission.getName();
					else
						yield "None";
				}
				case 7 -> vehicle.getCrewCapacity();
				case 8 -> vehicle.hasLab();
				case 9 -> vehicle.hasSickBay();
				default -> null;
			};
		}

		/**
		 * Check for failure cells.
		 */
		@Override
		protected String isFailureCell(Rover vehicle, int column) {
			return switch(column) {
				case 5 -> vehicle.isReserved() ? "Reserved" : null;
				case 4 -> (vehicle.getPrimaryStatus() != StatusType.PARKED) 
						&& (vehicle.getPrimaryStatus() != StatusType.GARAGED) ? MissionCreate.VEHICLE_WRONG_STATUS : null;
				case 6 -> vehicle.getMission() != null ? MissionCreate.ALREADY_ON_MISSION : null;
				default -> null;
			};
		}
	}
}
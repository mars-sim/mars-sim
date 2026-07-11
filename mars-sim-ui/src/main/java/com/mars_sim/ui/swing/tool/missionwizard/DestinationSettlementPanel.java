/*
 * Mars Simulation Project
 * DestinationSettlementPanel.java
 * @date 2021-09-20
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.tool.missionwizard;

import java.util.List;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.utils.model.BaseSettlementModel;
import com.mars_sim.ui.swing.utils.wizard.WizardItemModel;
import com.mars_sim.ui.swing.utils.wizard.WizardItemStep;

/**
 * This is a wizard panel for selecting the mission destination settlement.
 */
@SuppressWarnings("serial")
class DestinationSettlementPanel extends WizardItemStep<MissionDataBean,Settlement> {

	/** Wizard panel name. */
	public static final String ID = "Destination_Settlement";
	
	/**
	 * Constructor.
	 * @param wizard the create mission wizard.
	 */
	public DestinationSettlementPanel(MissionCreate wizard, MissionDataBean state) {
		super(ID, wizard, new SettlementTableModel(state));
	
	}

	/**
	 * Update the state with the selected destination.
	 */
	@Override
	protected void updateState(MissionDataBean state, List<Settlement> selectedItems) {
		state.setDestinationSettlement(selectedItems.get(0));
	}

	/**
	 * Clear information on the wizard panel.
	 */
	@Override
	public void clearState(MissionDataBean state) {
		state.setDestinationSettlement(null);
		super.clearState(state);
	}
	
	/**
	 * A table model for settlements base on tehBaseSettlementModel.
	 */
    private static class SettlementTableModel extends BaseSettlementModel
				implements WizardItemModel<Settlement> {
    	
    	/** default serial id. */
    	private static final long serialVersionUID = 1L;

		private static final int RANGE_VAL = 1001;
		private static final EntityColumnSpec RANGE = new EntityColumnSpec(new ColumnSpec(RANGE_VAL, "Distance (km)", Double.class, ColumnSpec.STYLE_DIGIT1), null);
		private static final int CAP_VAL = 1002;
		private static final EntityColumnSpec POP_CAPACITY = new EntityColumnSpec(new ColumnSpec(CAP_VAL, "Pop. Capacity", Integer.class), null);

		private MissionDataBean state;

    	/**
    	 * hidden constructor.
    	 */
    	private SettlementTableModel(MissionDataBean state) {
    		super(NAME, RANGE, POPULATION, POP_CAPACITY);

			var baseSettlement = state.getStartingSettlement();
			this.state = state;

			var options = Simulation.instance().getUnitManager().getSettlements().stream()
					.filter(s -> !s.equals(baseSettlement))
					.toList();
			setEntities(options);

			enableListeners(true);
    	}
    	

		@Override
		protected Object getEntityValue(Settlement settlement, int columnVal) {
			return switch(columnVal) {
				case RANGE_VAL -> state.getStartingSettlement().getCoordinates().getDistance(settlement.getCoordinates());
				case CAP_VAL -> settlement.getPopulationCapacity();
				default -> super.getEntityValue(settlement, columnVal);
			};
        }
    	
		@Override
		public String isFailureCell(int row, int column) {
			var colSpec = getColumnSpec(column);
			if (colSpec.equals(RANGE.column())) {		
				Settlement startingSettlement = state.getStartingSettlement();
				var settlement = getItem(row);
				double distance = startingSettlement.getCoordinates().getDistance(settlement.getCoordinates());
				Vehicle v = state.getRover();
				if (v == null) {
					v = state.getDrone();
				}
				double vRange = v.getRange();
				return (vRange < distance) ? MissionCreate.VEHICLE_OUT_OF_RANGE : null;
			}
    		
    		return null;
    	}

		@Override
		public Settlement getItem(int row) {
			return (Settlement)getAssociatedEntity(row);
		}
    }
}

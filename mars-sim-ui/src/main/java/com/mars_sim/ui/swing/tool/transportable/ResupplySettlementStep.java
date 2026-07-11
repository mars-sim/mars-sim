/*
 * Mars Simulation Project
 * ResupplySettlementStep.java
 * @date 2023-04-16
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.tool.transportable;

import java.util.List;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.ui.swing.utils.model.BaseSettlementModel;
import com.mars_sim.ui.swing.utils.wizard.WizardItemModel;
import com.mars_sim.ui.swing.utils.wizard.WizardItemStep;
import com.mars_sim.ui.swing.utils.wizard.WizardPane;

/**
 * A wizard panel for selecting the mission's starting settlement.
 */
@SuppressWarnings("serial")
class ResupplySettlementStep extends WizardItemStep<TransportState, Settlement> {

	public static final String ID = "Resupply_Settlement";

	/**
	 * Constructor.
	 * @param wizard the create mission wizard.
	 */
	ResupplySettlementStep(WizardPane<TransportState> parent, TransportState state) {
		super(ID, parent, new SettlementTableModel());
	}

	/**
	 * Clear information on the wizard panel.
	 */
	@Override
	public void clearState(TransportState state) {
		state.setLandingSettlement(null);
		super.clearState(state);
	}

	@Override
	protected void updateState(TransportState state, List<Settlement> sel) {
		state.setLandingSettlement(sel.get(0));
	}
	
	/**
	 * A table model for settlements.
	 */
	private static class SettlementTableModel extends BaseSettlementModel
								implements WizardItemModel<Settlement> {

		/**
		 * Constructor.
		 */
		private SettlementTableModel() {
			super(NAME, POPULATION);

			addResourceColumns(List.of(ResourceUtil.OXYGEN_ID, ResourceUtil.WATER_ID, ResourceUtil.FOOD_ID,
							ResourceUtil.METHANE_ID, ResourceUtil.METHANOL_ID));		

			// Add all settlements to table sorted by name.
			var settlements = Simulation.instance().getUnitManager().getSettlements();
			setEntities(settlements);
			enableListeners(true);
		}

		@Override
		public Settlement getItem(int row) {
			return (Settlement) getAssociatedEntity(row);
		}

		@Override
		public String isFailureCell(int row, int column) {
			return null;
		}
	}
}

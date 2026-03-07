/*
 * Mars Simulation Project
 * ResupplySettlementStep.java
 * @date 2023-04-16
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.tool.transportable;

import java.util.Comparator;
import java.util.List;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.utils.wizard.AbstractWizardItemModel;
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

	// Base column for all settlement models.
	private static final ColumnSpec NAME = new ColumnSpec(0, Msg.getString("entity.name"), String.class, ColumnSpec.STYLE_DEFAULT);
	private static final ColumnSpec POP = new ColumnSpec(1, Msg.getString("settlement.population"), Integer.class, ColumnSpec.STYLE_DEFAULT);
	private static final ColumnSpec ROVERS = new ColumnSpec(2, Msg.getString("rover.plural"), Integer.class, ColumnSpec.STYLE_DEFAULT);
	private static final ColumnSpec OXYGEN = new ColumnSpec(3, "Oxygen", Double.class, ColumnSpec.STYLE_DIGIT1);
	private static final ColumnSpec WATER = new ColumnSpec(4, "Water", Double.class, ColumnSpec.STYLE_DIGIT1);		
	private static final ColumnSpec FOOD = new ColumnSpec(5, "Food", Double.class, ColumnSpec.STYLE_DIGIT1);
	private static final ColumnSpec METHANE = new ColumnSpec(6, "Methane", Double.class, ColumnSpec.STYLE_DIGIT1);
	private static final ColumnSpec METHANOL = new ColumnSpec(7, "Methanol", Double.class, ColumnSpec.STYLE_DIGIT1);

	private static final List<ColumnSpec> BASE_COLS = List.of(NAME, POP, ROVERS, OXYGEN, WATER, FOOD, METHANE, METHANOL);

	
	/**
	 * A table model for settlements.
	 */
	private static class SettlementTableModel extends AbstractWizardItemModel<Settlement> {

		/**
		 * Constructor.
		 */
		private SettlementTableModel() {
			super(BASE_COLS);

			// Add all settlements to table sorted by name.
			var settlements = Simulation.instance().getUnitManager().getSettlements().stream()
				.sorted(Comparator.comparing(Settlement::getName)).toList();
			setItems(settlements);
		}

		/**
		 * Returns the value for the cell at columnIndex and rowIndex.
		 * @param item the item whose value is to be queried
		 * @param column the column whose value is to be queried
		 * @return the value Object at the specified cell
		 */
		@Override
		protected Object getItemValue(Settlement settlement, int column) {
			var spec = getColumnSpec(column);
			return switch(spec.id()) {
				case 0 -> settlement.getName();
				case 1 -> settlement.getIndoorPeopleCount();
				case 2 -> settlement.findNumParkedRovers();
				case 3 -> settlement.getSpecificAmountResourceStored(ResourceUtil.OXYGEN_ID);
				case 4 -> settlement.getSpecificAmountResourceStored(ResourceUtil.WATER_ID);
				case 5 -> settlement.getSpecificAmountResourceStored(ResourceUtil.FOOD_ID);
				case 6 -> settlement.getSpecificAmountResourceStored(ResourceUtil.METHANE_ID);
				case 7 -> settlement.getSpecificAmountResourceStored(ResourceUtil.METHANOL_ID);
				default -> null;
			};
		}
	}
}

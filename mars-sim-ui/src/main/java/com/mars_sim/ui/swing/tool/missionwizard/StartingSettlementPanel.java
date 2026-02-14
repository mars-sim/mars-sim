/*
 * Mars Simulation Project
 * StartingSettlementPanel.java
 * @date 2023-04-16
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.tool.missionwizard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.person.ai.mission.CollectIce;
import com.mars_sim.core.person.ai.mission.Exploration;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.VehicleType;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.tool.mission.create.MissionDataBean;
import com.mars_sim.ui.swing.utils.wizard.WizardItemModel;
import com.mars_sim.ui.swing.utils.wizard.WizardItemStep;
import com.mars_sim.ui.swing.utils.wizard.WizardPane;

/**
 * A wizard panel for selecting the mission's starting settlement.
 */
@SuppressWarnings("serial")
class StartingSettlementPanel extends WizardItemStep<MissionDataBean, Settlement> {

	public static final String ID = "Starting_Settlement";

	/**
	 * Constructor.
	 * @param wizard the create mission wizard.
	 */
	StartingSettlementPanel(WizardPane<MissionDataBean> parent, MissionDataBean state) {
		super(ID, parent, createModel(state));
	}

	/**
	 * Clear information on the wizard panel.
	 */
	@Override
	public void clearState(MissionDataBean state) {
		state.setStartingSettlement(null);
		super.clearState(state);
	}

	@Override
	protected void updateState(MissionDataBean state, List<Settlement> sel) {
		state.setStartingSettlement(sel.get(0));
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
	private static final ColumnSpec EVA_SUITS = new ColumnSpec(8, "EVA Suits", Integer.class, ColumnSpec.STYLE_DEFAULT);
	private static final ColumnSpec LUV = new ColumnSpec(10, Msg.getString("lightutilityvehicle.plural"), Integer.class, ColumnSpec.STYLE_DEFAULT);

	private static final List<ColumnSpec> BASE_COLS = List.of(NAME, POP, ROVERS, OXYGEN, WATER, FOOD, METHANE, METHANOL, EVA_SUITS);

	private static SettlementTableModel createModel(MissionDataBean state) {
		List<ColumnSpec> cols;
		EquipmentType eType = null;
		int eMin = 0;
		switch(state.getMissionType()) {
			case EXPLORATION -> {
				cols =  new ArrayList<>(BASE_COLS);
				cols.add(new ColumnSpec(9, "Specimen Boxes", Integer.class, ColumnSpec.STYLE_DEFAULT));
				eType = EquipmentType.SPECIMEN_BOX;
				eMin = Exploration.REQUIRED_SPECIMEN_CONTAINERS;
			}
			case COLLECT_ICE, COLLECT_REGOLITH -> {
				cols = new ArrayList<>(BASE_COLS);
				cols.add(new ColumnSpec(9, "Bags", Integer.class, ColumnSpec.STYLE_DEFAULT));
				eType = EquipmentType.BAG;
				eMin = CollectIce.REQUIRED_BARRELS;
			}
			case MINING -> {
				cols = new ArrayList<>(BASE_COLS);
				cols.add(new ColumnSpec(9, "Bags", Integer.class, ColumnSpec.STYLE_DEFAULT));
				cols.add(LUV);
				cols.add(new ColumnSpec(12, "Backhoes", Integer.class, ColumnSpec.STYLE_DEFAULT	));
				eType = EquipmentType.BAG; 
				eMin = CollectIce.REQUIRED_BARRELS;
			}
			case CONSTRUCTION -> {
				// Add columns.
				cols = new ArrayList<>();
				cols.add(NAME);
				cols.add(POP);
				cols.add(new ColumnSpec(13, Msg.getString("constructionsite.plural"), Integer.class, ColumnSpec.STYLE_DEFAULT));
				cols.add(LUV);
				cols.add(EVA_SUITS);
			}
			default -> cols = BASE_COLS;
		}
		return new SettlementTableModel(cols, eType, eMin);
	}

	/**
	 * A table model for settlements.
	 */
	private static class SettlementTableModel extends WizardItemModel<Settlement> {

		private static final String NONE_AVAILABLE = "None available";
		/** default serial id. */
		private static final long serialVersionUID = 1L;
		private EquipmentType containerType;
		private int containerMin;

		/**
		 * Constructor.
		 */
		private SettlementTableModel(List<ColumnSpec> columns, EquipmentType containerType, int containerMin) {
			super(columns);
			this.containerType = containerType;
			this.containerMin = containerMin;

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
				case 8 -> settlement.getNumEVASuit();
				case 9 -> settlement.findNumContainersOfType(containerType);
				case 10 ->settlement.findNumVehiclesOfType(VehicleType.LUV);
				case 11 ->settlement.getItemResourceStored(ItemResourceUtil.PNEUMATIC_DRILL_ID);
				case 12 ->settlement.getItemResourceStored(ItemResourceUtil.BACKHOE_ID);
				case 13 ->settlement.getConstructionManager().getConstructionSites().size();
				default -> null;
			};
		}

		/**
		 * Check for failure cells.
		 */
		@Override
		protected String isFailureCell(Settlement settlement, int column) {
			var spec = getColumnSpec(column);
			return switch(spec.id()) {
				case 1 -> settlement.getIndoorPeopleCount() == 0 ? "No indoor people" : null;
				case 2 -> settlement.findNumParkedRovers() == 0 ? "No parked rovers" : null;
				case 3 -> checkResources(settlement, ResourceUtil.OXYGEN_ID, 100D);
				case 4 -> checkResources(settlement, ResourceUtil.WATER_ID, 100D);
				case 5 -> checkResources(settlement, ResourceUtil.FOOD_ID, 100D);
				case 6 -> checkResources(settlement, ResourceUtil.METHANE_ID, 100D);
				case 7 -> checkResources(settlement, ResourceUtil.METHANOL_ID, 100D);
				case 8 -> settlement.getNumEVASuit() == 0 ? NONE_AVAILABLE: null;
				case 9 -> settlement.findNumContainersOfType(containerType) < containerMin
										? "Insufficient containers : " + containerMin : null;
				case 10 -> settlement.findNumVehiclesOfType(VehicleType.LUV) == 0 ? NONE_AVAILABLE : null;
				case 11 -> settlement.getItemResourceStored(ItemResourceUtil.PNEUMATIC_DRILL_ID) == 0 ? NONE_AVAILABLE : null;
				case 12 -> settlement.getItemResourceStored(ItemResourceUtil.BACKHOE_ID) == 0 ? NONE_AVAILABLE : null;
				case 13 -> settlement.getConstructionManager().getConstructionSites().isEmpty() ? NONE_AVAILABLE : null;
				default -> null;
			};
		}

		private final static String checkResources(Settlement settlement, int resId, double min) {
			return settlement.getSpecificAmountResourceStored(resId) < min ? "Less than minimum: " + min : null;
		}
	}
}

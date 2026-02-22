/*
 * Mars Simulation Project
 * DestinationSettlementPanel.java
 * @date 2021-09-20
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.tool.missionwizard;

import java.util.Comparator;
import java.util.List;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.tool.mission.create.MissionDataBean;
import com.mars_sim.ui.swing.utils.wizard.AbstractWizardItemModel;
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
	 * A table model for settlements.
	 */
    private static class SettlementTableModel extends AbstractWizardItemModel<Settlement> {
    	
    	/** default serial id. */
    	private static final long serialVersionUID = 1L;
		private static final List<ColumnSpec> COLUMNS = List.of(
				new ColumnSpec(Msg.getString("entity.name"), String.class),
				new ColumnSpec("Distance (km)", Double.class, ColumnSpec.STYLE_DIGIT1),
				new ColumnSpec(Msg.getString("settlement.population"), Integer.class),
				new ColumnSpec("Pop. Capacity", Integer.class)
		);
		private MissionDataBean state;

    	/**
    	 * hidden constructor.
    	 */
    	private SettlementTableModel(MissionDataBean state) {
    		super(COLUMNS);

			var baseSettlement = state.getStartingSettlement();
			this.state = state;

			var options = Simulation.instance().getUnitManager().getSettlements().stream()
					.filter(s -> !s.equals(baseSettlement))
					.sorted(Comparator.comparing(Settlement::getName))
					.toList();
			setItems(options);
    	}
    	

		@Override
		protected Object getItemValue(Settlement settlement, int column) {
			return switch(column) {
				case 0 -> settlement.getName();
				case 1 -> state.getStartingSettlement().getCoordinates().getDistance(settlement.getCoordinates());
				case 2 -> settlement.getIndoorPeopleCount();
				case 3 -> settlement.getPopulationCapacity();
				default -> null;
			};
        }
    	

		@Override
		protected String isFailureCell(Settlement settlement, int column) {    		
    		if (column == 1) {
				Settlement startingSettlement = state.getStartingSettlement();
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
    }
}

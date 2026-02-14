/*
 * Mars Simulation Project
 * ConstructionPanel.java
 * @date 2026-02-08
 * @author Barry Evans
 */

package com.mars_sim.ui.swing.tool.missionwizard;

import java.util.Comparator;
import java.util.List;

import com.mars_sim.core.building.construction.ConstructionSite;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.tool.mission.create.MissionDataBean;
import com.mars_sim.ui.swing.utils.wizard.WizardItemModel;
import com.mars_sim.ui.swing.utils.wizard.WizardItemStep;
/**
 * A wizard panel for selecting settlers.
 */
@SuppressWarnings("serial")
class ConstructionPanel extends WizardItemStep<MissionDataBean, ConstructionSite>
{

	/** The wizard panel name. */
	public static final String ID = "Construction";
	
	/**
	 * Constructor.
	 * 
	 * @param wizard the create mission wizard
	 */
	public ConstructionPanel(MissionCreate wizard, MissionDataBean state) {
		// Use WizardPanel constructor
		super(ID, wizard, new SiteTableModel(state), 1, 1);
	}

	/**
	 * Update 
	 */
	@Override
	protected void updateState(MissionDataBean state, List<ConstructionSite> selection) {
		state.setConstructionSite(selection.get(0));
	}

	/**
	 * Clears information on the wizard panel.
	 */
	@Override
	public void clearState(MissionDataBean state) {
		state.setConstructionSite(null);
		super.clearState(state);
	}

	/**
	 * Table model for sites.
	 */
	private static class SiteTableModel extends WizardItemModel<ConstructionSite> {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private static final List<ColumnSpec> COLUMNS = List.of(
				new ColumnSpec(Msg.getString("entity.name"), String.class),
				new ColumnSpec(Msg.getString("building.singular"), String.class),
				new ColumnSpec(Msg.getString("constructionsite.stage"), String.class),
				new ColumnSpec(Msg.getString("mission.singular"), String.class)
		);

		/** Constructor. */
		private SiteTableModel(MissionDataBean state) {
			super(COLUMNS);

			List<ConstructionSite> people = state.getStartingSettlement().getConstructionManager().getConstructionSites().stream()
					.sorted(Comparator.comparing(ConstructionSite::getName))
					.toList();
			setItems(people);
		}

		/**
		 * Failure is if the Person is already assigned to a mission.
		 */
		@Override
		protected String isFailureCell(ConstructionSite item, int column) {
			return switch (column) {
				case 3 -> item.getWorkOnSite() != null ? MissionCreate.ALREADY_ON_MISSION : null;
				default -> null;
			};
		}

		/**
		 * Gets the value for the cell at columnIndex and rowIndex.
		 * 
		 * @param item the item.
		 * @param column the column index.
		 * @return Rendered values
		 */
		@Override
		protected Object getItemValue(ConstructionSite item, int column) {
			return switch (column) {
				case 0 -> item.getName();
				case 1 -> item.getBuildingName();
				case 2 -> item.getDescription();
				case 3 -> {
					var onSite = item.getWorkOnSite();
					yield onSite != null ? onSite.getName() : null;
				}
				default -> null;
			}; 
		}
	}
}

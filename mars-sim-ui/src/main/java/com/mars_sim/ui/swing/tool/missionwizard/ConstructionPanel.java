/*
 * Mars Simulation Project
 * ConstructionPanel.java
 * @date 2026-02-08
 * @author Barry Evans
 */

package com.mars_sim.ui.swing.tool.missionwizard;

import java.util.List;

import com.mars_sim.core.building.construction.ConstructionSite;
import com.mars_sim.ui.swing.utils.model.BaseConstructionSiteModel;
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
	private static class SiteTableModel extends BaseConstructionSiteModel
				implements WizardItemModel<ConstructionSite> {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		/** Constructor. */
		private SiteTableModel(MissionDataBean state) {
			super(NAME, BUILDING, STAGE, MISSION);

			List<ConstructionSite> people = state.getStartingSettlement().getConstructionManager().getConstructionSites();
			setEntities(people);
			enableListeners(true);
		}

		@Override
		public ConstructionSite getItem(int row) {
			return (ConstructionSite) getAssociatedEntity(row);
		}

		/**
		 * Failure is if the Person is already assigned to a mission.
		 */
		@Override
		public String isFailureCell(int row, int column) {
			var colSpec = getColumnSpec(column);
			if (colSpec.equals(MISSION.column())) {
				var item = getItem(row);
				if (item.getWorkOnSite() != null) {
					return MissionCreate.ALREADY_ON_MISSION;
				}
			}
			return null;
		}
	}
}

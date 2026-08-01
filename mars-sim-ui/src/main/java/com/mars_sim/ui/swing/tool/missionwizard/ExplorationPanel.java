/*
 * Mars Simulation Project
 * ExplorationPanel.java
 * @date 2026-02-08
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.missionwizard;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import com.mars_sim.core.environment.MineralSite;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.utils.wizard.AbstractWizardItemModel;
import com.mars_sim.ui.swing.utils.wizard.WizardItemStep;
import com.mars_sim.ui.swing.utils.wizard.WizardPane;

/**
 * A wizard panel for selecting the mission exploration site.
 */
@SuppressWarnings("serial")
class ExplorationPanel extends WizardItemStep<MissionDataBean, MineralSite> {

	/** The wizard panel name. */
	public static final String ID = "exploration";
	private MineralPanel mineralPanel;

	/**
	 * Constructor.
	 * 
	 * @param wizard the create mission wizard.
	 */
	ExplorationPanel(WizardPane<MissionDataBean> parent, MissionDataBean state) {
		// Use WizardPanel constructor.
		super(ID, parent, new SiteTableModel(state), 1, 3);
	}

	/**
	 * Clear information on the wizard panel.
	 */
	@Override
	public void clearState(MissionDataBean state) {
		state.setExplorationSites(null);
		super.clearState(state);
	}

	/**
	 * Update the state with the selected site
	 */
	@Override
	protected void updateState(MissionDataBean state, List<MineralSite> sel) {
		state.setExplorationSites(sel);
	}

	/**
	 * Build the information panel for the mineral site selection step.
	 * It shows the estimated mineral concentrations of the selected site.
	 */
	@Override
	protected JComponent buildInfoPanel() {
		mineralPanel = new MineralPanel();
		return mineralPanel;

	}

	/**
	 * The mineral site has changed, update the mineral concentration table.
	 * @param sel the selected mineral sites.
	 */
	@Override
	protected void selectionChanged(List<MineralSite> sel) {
		var site = sel.isEmpty() ? null : sel.get(0);
		mineralPanel.setSelection(site);

		super.selectionChanged(sel);
	}

	/**
	 * A table model for mining site.
	 */
	private static class SiteTableModel extends AbstractWizardItemModel<MineralSite> {

		/** default serial id. */
		private static final long serialVersionUID = 1L;
		private static final List<ColumnSpec> COLUMNS = List.of(
				new ColumnSpec(Msg.getString("entity.name"), String.class),
				new ColumnSpec("Reviews", Integer.class),
				new ColumnSpec("Owner", String.class),
				new ColumnSpec("Explored", Boolean.class),
				new ColumnSpec(Msg.getString("entity.coordinates"), String.class),
				new ColumnSpec("Distance", Double.class, ColumnSpec.STYLE_DIGIT1));
		private Coordinates startPoint;
				
		/**
		 * Constructor
		 */
		private SiteTableModel(MissionDataBean state) {
			super(COLUMNS);
		
			var explorationManager = state.getStartingSettlement().getExplorations();
			startPoint = state.getStartingSettlement().getLocation();

			var withinRange = explorationManager.getDeclaredROIs();
			setItems(new ArrayList<>(withinRange));
		}

		/**	
		 * Returns the value for the table cell.
		 * @param site the mineral site.
		 * @param column the table column.
		 * @return the cell value.
		 */
		@Override
		protected Object getItemValue(MineralSite site, int column) {
			return switch(column) {
				case 0 -> site.getName();
				case 1 -> site.getNumEstimationImprovement();
				case 2 -> (site.getOwner() != null ? site.getOwner().getName() : null);
				case 3 -> site.isExplored();
				case 4 -> site.getCoordinates().getFormattedString();
				case 5 -> site.getCoordinates().getDistance(startPoint);
				default -> null;
			};
		}

		/**
		 * Check for failure cells.
		 * @param site the mineral site.
		 * @param column the table column.
		 */
		@Override
		protected String isFailureCell(MineralSite site, int column) {
			return null;
		}
	}
}
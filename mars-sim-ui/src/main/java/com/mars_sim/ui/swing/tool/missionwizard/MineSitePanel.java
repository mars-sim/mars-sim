/*
 * Mars Simulation Project
 * MineSitePanel.java
 * @date 2026-02-08
 * @author Barry Evans
 */

package com.mars_sim.ui.swing.tool.missionwizard;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.environment.MineralSite;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.components.PercentageTableCellRenderer;
import com.mars_sim.ui.swing.tool.mission.create.MissionDataBean;
import com.mars_sim.ui.swing.utils.SwingHelper;
import com.mars_sim.ui.swing.utils.wizard.WizardItemModel;
import com.mars_sim.ui.swing.utils.wizard.WizardItemStep;
import com.mars_sim.ui.swing.utils.wizard.WizardPane;

/**
 * A wizard panel for selecting the mission Rover.
 */
@SuppressWarnings("serial")
class MineSitePanel extends WizardItemStep<MissionDataBean, MineralSite> {

	/** The wizard panel name. */
	public static final String ID = "mine_site";
	private MineralModel model;

	/**
	 * Constructor.
	 * 
	 * @param wizard the create mission wizard.
	 */
	MineSitePanel(WizardPane<MissionDataBean> parent, MissionDataBean state) {
		// Use WizardPanel constructor.
		super(ID, parent, new SiteTableModel(state));
	}

	/**
	 * Clear information on the wizard panel.
	 */
	@Override
	public void clearState(MissionDataBean state) {
		state.setMiningSite(null);
		super.clearState(state);
	}

	/**
	 * Update the state with the selected site
	 */
	@Override
	protected void updateState(MissionDataBean state, List<MineralSite> sel) {
		state.setMiningSite(sel.get(0));
	}

	/**
	 * Build the information panel for the mineral site selection step.
	 * It shows the estimated mineral concentrations of the selected site.
	 */
	@Override
	protected JComponent buildInfoPanel() {
		model = new MineralModel();

		var itemTable = new JTable(model);
		itemTable.setRowSelectionAllowed(false);	
		itemTable.setPreferredScrollableViewportSize(itemTable.getPreferredSize());
		itemTable.getColumnModel().getColumn(1).setCellRenderer(
							new PercentageTableCellRenderer(false));
		itemTable.getColumnModel().getColumn(2).setCellRenderer(
							new PercentageTableCellRenderer(false));
		JScrollPane tableScrollPane = new JScrollPane();
		tableScrollPane.setViewportView(itemTable);
		tableScrollPane.setBorder(SwingHelper.createLabelBorder("Minerals"));
		return tableScrollPane;
	}

	/**
	 * The mineral site has changed, update the mineral concentration table.
	 * @param sel the selected mineral sites.
	 */
	@Override
	protected void selectionChanged(List<MineralSite> sel) {
		var site = sel.isEmpty() ? null : sel.get(0);
		model.update(site);

		super.selectionChanged(sel);
	}

	/**
	 * Show the mineral concentration when a site is selected.
	 */
	private static class MineralModel extends AbstractTableModel {

		private record MineralId(String name, int id) {}

		/** default serial id. */
		private static final long serialVersionUID = 1L;
		private MineralSite site;
		private List<MineralId> minerals = Collections.emptyList();
	
		private void update(MineralSite newSite) {
			if (newSite == null) {
				minerals = Collections.emptyList();
			}
			else {
				minerals = newSite.getMinerals().keySet().stream()
						.map(m ->  new MineralId(ResourceUtil.findAmountResourceName(m), m))
						.sorted(Comparator.comparing(MineralId::name))
						.toList();
			}

			site = newSite;
			fireTableDataChanged();
		}

		@Override
		public int getRowCount() {
			return minerals.size();
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			var mineral = minerals.get(rowIndex);
			return switch(columnIndex) {
				case 0 -> mineral.name;
				case 1 -> site.getMinerals().get(mineral.id).concentration();
				case 2 -> site.getMinerals().get(mineral.id).certainty();
				default -> null;
			};
		}
		
		@Override
		public String getColumnName(int column) {
			return switch(column) {
				case 0 -> "Mineral";
				case 1 -> "Estimated Concentration";
				case 2 -> "Degree of Certainty";
				default -> null;
			};
		}
	}

	/**
	 * A table model for mining site.
	 */
	private static class SiteTableModel extends WizardItemModel<MineralSite> {

		/** default serial id. */
		private static final long serialVersionUID = 1L;
		private static final List<ColumnSpec> COLUMNS = List.of(
				new ColumnSpec(Msg.getString("entity.name"), String.class),
				new ColumnSpec("Remaining Mass", Double.class, ColumnSpec.STYLE_DIGIT1),
				new ColumnSpec("Certainity", Double.class, ColumnSpec.STYLE_PERCENTAGE),
				new ColumnSpec(Msg.getString("entity.coordinates"), String.class),
				new ColumnSpec("Distance", Double.class, ColumnSpec.STYLE_DIGIT1));
		private Coordinates startPoint;
				
		/**
		 * Constructor
		 */
		private SiteTableModel(MissionDataBean state) {
			super(COLUMNS);
		
			var owner = state.getStartingSettlement().getReportingAuthority();
			startPoint = state.getStartingSettlement().getLocation();
			var range = state.getRover().getEstimatedRange();

			var surfaceFeatures = Simulation.instance().getSurfaceFeatures();
			var withinRange = surfaceFeatures.getAllPossibleRegionOfInterestLocations().stream()
					.filter(s -> s.isMinable() && owner.equals(s.getOwner()))
					.filter(s -> s.getLocation().getDistance(startPoint) < range)
					.toList();
	
			setItems(withinRange);
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
				case 1 -> site.getRemainingMass();
				case 2 -> site.getAverageCertainty();
				case 3 -> site.getCoordinates().getFormattedString();
				case 4 -> site.getCoordinates().getDistance(startPoint);
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
			return ((column == 1 && site.getRemainingMass() < 100) ? "Remaining mass is below 100" : null);
		}
	}
}
/*
 * Mars Simulation Project
 * TabPanelMaintenance.java
 * @date 2024-07-22
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.structure;

import javax.swing.table.TableModel;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.entitywindow.EntityTableTabPanel;
import com.mars_sim.ui.swing.utils.model.BaseBuildingModel;

/**
 * The TabPanelMaintenance is a tab panel for settlement's building maintenance.
 */
@SuppressWarnings("serial")
class TabPanelMaintenance extends EntityTableTabPanel<Settlement> implements TemporalComponent{

	private static final String SPANNER_ICON = "maintenance";

	private BuildingMaintModel tableModel;
		
	/**
	 * Constructor.
	 * 
	 * @param unit    the unit (currently for settlements only)
	 * @param context the UI context.
	 */
	public TabPanelMaintenance(Settlement settlement, UIContext context) {
		// Use the TabPanel constructor
		super(
				Msg.getString("TabPanelMaintenance.title"),
				ImageLoader.getIconByName(SPANNER_ICON), null,
				settlement, context
			);
	}

	@Override
	protected TableModel createModel() {
		tableModel = new BuildingMaintModel(getEntity());
		return tableModel;
	}
	
	@Override
	public void clockUpdate(ClockPulse pulse) {
		tableModel.update();
	}

	/**
	 * The BuildingMaintModel is a table model for showing the maintenance status of the buildings in a settlement.
	 */
	private static class BuildingMaintModel extends BaseBuildingModel {

		private static final int HEALTH_VAL = 101;
		private static final int LAST_INSPECT_VAL = 102;
		private static final int INSPECT_WINDOW_VAL = 103;
		private static final int MAINT_TIME_VAL = 104;
		private static final int DONE_VAL = 105;

		private static final EntityColumnSpec HEALTH = new EntityColumnSpec(new ColumnSpec(HEALTH_VAL, "Health", Integer.class,
													ColumnSpec.STYLE_PERCENTAGE), null);
		private static final EntityColumnSpec LAST_INSPECT = new EntityColumnSpec(new ColumnSpec(LAST_INSPECT_VAL, "Last Inspect", Double.class,
													ColumnSpec.STYLE_DIGIT2), null);
		private static final EntityColumnSpec INSPECT_WINDOW = new EntityColumnSpec(new ColumnSpec(INSPECT_WINDOW_VAL, "Inspect window", Double.class,
													ColumnSpec.STYLE_DIGIT2), null);
		private static final EntityColumnSpec MAINT_TIME = new EntityColumnSpec(new ColumnSpec(MAINT_TIME_VAL, "Maint time", Double.class,
													ColumnSpec.STYLE_DIGIT3), null);
		private static final EntityColumnSpec DONE = new EntityColumnSpec(new ColumnSpec(DONE_VAL, "Done", Integer.class,
													ColumnSpec.STYLE_PERCENTAGE), null);

		public BuildingMaintModel(Settlement settlement) {
			super(NAME, HEALTH, LAST_INSPECT, INSPECT_WINDOW, MAINT_TIME, DONE);
			setEntities(settlement.getBuildingManager().getBuildingSet());
		}

		public void update() {
			if (getRowCount() > 0) {
				fireTableRowsUpdated(0, getRowCount() - 1);
			}
		}

		@Override
		protected Object getEntityValue(Building building, int valueIndex) {
			MalfunctionManager manager = building.getMalfunctionManager();
			return switch (valueIndex) {
				case HEALTH_VAL -> (int) manager.getWearCondition();
				case LAST_INSPECT_VAL -> manager.getEffectiveTimeSinceLastMaintenance() / 1000D;
				case INSPECT_WINDOW_VAL -> manager.getStandardInspectionWindow() / 1000D;
				case MAINT_TIME_VAL -> manager.getBaseMaintenanceWorkTime() / 1000D;
				case DONE_VAL -> {
					double completed = manager.getInspectionWorkTimeCompleted();
					double total = manager.getBaseMaintenanceWorkTime();
					yield (int) (Math.round(1000.0 * completed / total) / 10.0);
				}
				default -> super.getEntityValue(building, valueIndex);
			};
		}
	}
}

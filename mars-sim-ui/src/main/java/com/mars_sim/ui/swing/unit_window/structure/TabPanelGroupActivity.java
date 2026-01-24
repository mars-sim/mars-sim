/*
 * Mars Simulation Project
 * TabPanelGroupActivity.java
 * @date 2024-03-31
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.mars_sim.core.Entity;
import com.mars_sim.core.activities.GroupActivity;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTableTabPanel;
import com.mars_sim.ui.swing.utils.EntityModel;

/**
 * This is a tab panel for settlement's computing capability.
 */
@SuppressWarnings("serial")
class TabPanelGroupActivity extends EntityTableTabPanel<Settlement> {

	private static final String ICON = "schedule";
	
	private TableModel tableModel;
	
	/**
	 * Constructor.
	 * 
	 * @param unit the unit to display.
	 * @param context the UI context.
	 */
	public TabPanelGroupActivity(Settlement unit, UIContext context) {
		// Use the TabPanel constructor
		super(
			"Group Activity",
			ImageLoader.getIconByName(ICON),
			"Scheduled Group Activities",
			unit, context
		);
	}
	
	/**
	 * Create a table model that shows the comuting details of the Buildings
	 * in the settlement.
	 * 
	 * @return Table model.
	 */
	@Override
	protected TableModel createModel() {
		tableModel = new TableModel(getEntity());

		return tableModel;
	}

	/**
	 * Updates the info on this panel when it is selected.
	 * A slow moving data table is used so that the data is only updated
	 * when the tab is selected.
	 */
	@Override
	public void refreshUI() {

		// Update  table.
		tableModel.update();
	}

	/**
	 * Internal class used as model for the table.
	 */
	private static class TableModel extends AbstractTableModel implements EntityModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;
		private static final String NAME = Msg.getString("entity.name");

		private List<GroupActivity> activities;
		private MarsTime earliest = null;
		private Settlement settlement;

		private TableModel(Settlement settlement) {
			activities = settlement.getGroupActivities(false);
			if (!activities.isEmpty()) {
				earliest = activities.get(0).getStartTime();
			}
			this.settlement = settlement;
		}

		@Override
		public int getRowCount() {
			return activities.size();
		}

		@Override
		public int getColumnCount() {
			return 4;
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 2) {
				return MarsTime.class;
			}
			return String.class;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return switch(columnIndex) {
				case 0 -> NAME;
				case 1 -> "State";
				case 2 -> "When";
				case 3 -> "Where";
				default -> "";
			};
		}

		@Override
		public Object getValueAt(int row, int column) {
			var ga = activities.get(row);
			return switch(column) {
				case 0 -> ga.getName();
				case 1 -> ga.getState().name();
				case 2 -> ga.getStartTime();
				case 3 -> (ga.getMeetingPlace() != null ? ga.getMeetingPlace().getName() : null);
				default -> "";
			};
		}

		public void update() {
			var newActivities = settlement.getGroupActivities(false);
			MarsTime newEarliest = null;
			if (!newActivities.isEmpty()) {
				newEarliest = newActivities.get(0).getStartTime();
			}

			// Update table if new activities or earliest time has changed
			if (!activities.equals(newActivities) || 
					((earliest!= null && newEarliest!= null) && !earliest.equals(newEarliest))) {
				activities = newActivities;
				activities = settlement.getGroupActivities(false);
				earliest = newEarliest;
				fireTableDataChanged();
			}
		}

		@Override
		public Entity getAssociatedEntity(int row) {
			return activities.get(row).getMeetingPlace();
		}
	}
}

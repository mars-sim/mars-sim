/*
 * Mars Simulation Project
 * TabPanelGroupActivity.java
 * @date 2024-03-31
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.mars_sim.core.Unit;
import com.mars_sim.core.activities.GroupActivity;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.unit_window.TabPanelTable;
import com.mars_sim.ui.swing.utils.UnitModel;

/**
 * This is a tab panel for settlement's computing capability.
 */
@SuppressWarnings("serial")
public class TabPanelGroupActivity extends TabPanelTable {

	// default logger.

	private static final String ICON = "schedule";
	
	/** The Settlement instance. */
	private Settlement settlement;
	
	private TableModel tableModel;
	
	/**
	 * Constructor.
	 * 
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelGroupActivity(Settlement unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			"Group Activity",
			ImageLoader.getIconByName(ICON),
			"Scheduled Group Activities",
			desktop
		);
		settlement = unit;
	}
	
	/**
	 * Create a table model that shows the comuting details of the Buildings
	 * in the settlement.
	 * 
	 * @return Table model.
	 */
	protected TableModel createModel() {
		tableModel = new TableModel(settlement);

		return tableModel;
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {

		// Update  table.
		tableModel.update();
	}

	/**
	 * Internal class used as model for the table.
	 */
	private class TableModel extends AbstractTableModel implements UnitModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private List<GroupActivity> activities;
		private MarsTime earliest = null;

		private TableModel(Settlement settlement) {
			activities = settlement.getGroupActivities(false);
			if (!activities.isEmpty()) {
				earliest = activities.get(0).getStartTime();
			}
		}

		public int getRowCount() {
			return activities.size();
		}

		public int getColumnCount() {
			return 4;
		}
		
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 2) {
				return MarsTime.class;
			}
			return String.class;
		}

		public String getColumnName(int columnIndex) {
			return switch(columnIndex) {
				case 0 -> "Name";
				case 1 -> "State";
				case 2 -> "When";
				case 3 -> "Where";
				default -> "";
			};
		}

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
		public Unit getAssociatedUnit(int row) {
			return activities.get(row).getMeetingPlace();
		}
	}
}

/*
 * Mars Simulation Project
 * BacklogTableModel.java
 * @date 2022-12-02
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;

import com.mars_sim.core.Entity;
import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.person.ai.task.util.SettlementTask;
import com.mars_sim.core.person.ai.task.util.SettlementTaskManager;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.utils.ColumnSpec;
import com.mars_sim.ui.swing.utils.RatingScoreRenderer;

/**
 * This class models how SettlementTasks are organized and displayed
 * within the Monitor Window for a settlement.
 */
@SuppressWarnings("serial")
public class BacklogTableModel extends AbstractMonitorModel
					implements EntityListener {
	// Represents a row in the table
	private record BacklogEntry(Settlement owner, SettlementTask task) implements Serializable {}

	private static final ColumnSpec[] COLUMNS;

	private static final int DESC_COL = 0;
	private static final int SETTLEMENT_COL = DESC_COL+1;
	private static final int ENTITY_COL = SETTLEMENT_COL+1;
	private static final int EVA_COL = ENTITY_COL+1;
	private static final int SCOPE_COL = EVA_COL+1;
	private static final int DEMAND_COL = SCOPE_COL+1;
	static final int SCORE_COL = DEMAND_COL+1;
	
	private static final String ANY = "Any";
	private static final String OFF_DUTY_ONLY = "Off-Duty Only";
	private static final String ON_DUTY_ONLY = "On-Duty Only";
	private static final String YES = "Yes";
	private static final String NO = "No";
					

	static {
		COLUMNS = new ColumnSpec[SCORE_COL+1];
		COLUMNS[ENTITY_COL] = new ColumnSpec("Entity", String.class);
		COLUMNS[SETTLEMENT_COL] = new ColumnSpec("Settlement", String.class);
		COLUMNS[DESC_COL] = new ColumnSpec("Description", String.class);
		COLUMNS[DEMAND_COL]  = new ColumnSpec("Demand", Integer.class);
		COLUMNS[EVA_COL]  = new ColumnSpec("EVA", String.class);
		COLUMNS[SCOPE_COL]  = new ColumnSpec("Scope", String.class);
		COLUMNS[SCORE_COL] = new ColumnSpec("Score", Double.class, ColumnSpec.STYLE_DIGIT2);
	}

	private boolean monitorSettlement = false;
	
	private Set<Settlement> selectedSettlements = Collections.emptySet();
	private List<BacklogEntry> tasks = Collections.emptyList();

	/**
	 * Constructor.
	 */
	public BacklogTableModel() {
		super(Msg.getString("BacklogTableModel.tabName"),
							"BacklogTableModel.counting",
							COLUMNS);	
		setSettlementColumn(SETTLEMENT_COL);	
	}

	/**
	 * Catches unit update event.
	 *
	 * @param event the unit event.
	 */
	@Override
	public void entityUpdate(EntityEvent event) {
		if (event.getTarget() instanceof Settlement settlement
				&& event.getSource() instanceof Settlement) {
			String eventType = event.getType();
			if ((SettlementTaskManager.BACKLOG_EVENT.equals(eventType)) && selectedSettlements.contains(settlement)) {
				var newTasks = getTasks();
	
				// Reset the Tasks asynchronously in the Swing Dispatcher to avoid sorting clashes
				SwingUtilities.invokeLater(() -> resetTasks(newTasks));
			}
		}
	}

	/**
	 * Gets the Object.
	 */
	@Override
	public Object getObject(int row) {
		return tasks.get(row).task().getFocus();
	}

	/**
	 * Sets whether the changes to the Entities should be monitor for change. Sets up the 
	 * unit listeners for the selected Settlement where Food comes from for the table.
	 * 
	 * @param activate 
	 */
	@Override
    public void setMonitorEntites(boolean activate) {
		if (activate != monitorSettlement) {
			if (activate) {
				selectedSettlements.forEach(s -> s.addEntityListener(this));
			}
			else {
				selectedSettlements.forEach(s -> s.removeEntityListener(this));
			}
			monitorSettlement = activate;
		}
	}

	/**
	 * Prepares the model for deletion.
	 */
	@Override
	public void destroy() {
		// Remove as listener for all settlements.
		selectedSettlements.forEach(s -> s.removeEntityListener(this));

		super.destroy();
	}

	/**
	 * Sets the Settlement filter.
	 * 
	 * @param filter Settlement
	 */
	@Override
    public boolean setSettlementFilter(Set<Settlement> filter) {
		selectedSettlements.forEach(s -> s.removeEntityListener(this));

		// Initialize settlements.
		selectedSettlements = filter;	

		// Initialize task list; backlog maybe null.
		tasks = getTasks();
		fireTableDataChanged();
			
		// Add table as listener to each settlement.
		if (monitorSettlement) {
			selectedSettlements.forEach(s -> s.addEntityListener(this));
		}

		return true;
    }

	/**
	 * Creates a list of backlog entries for all the monitored settlements.
	 * The SettlementTask does hold the Settlement reference so this is record in
	 * the artificial BacklogEntry record.
	 */
	private List<BacklogEntry> getTasks() {
		return selectedSettlements.stream()
					.flatMap(s -> s.getTaskManager().getAvailableTasks().stream()
					.map(e -> new BacklogEntry(s, e)))
					.toList();
	}

    /**
     * Resets tasks.
     */
	private void resetTasks(List<BacklogEntry> newTasks) {
		List<BacklogEntry> oldTasks = tasks;
		tasks = newTasks;

		// Find out how many rows have been added/deleted
		int delta = tasks.size() - oldTasks.size();
		if (delta < 0) {
			// Row deleted
			fireTableRowsDeleted(tasks.size(), oldTasks.size()-1);
		}
		else if (delta > 0) {
			// Rows added
			fireTableRowsInserted(oldTasks.size(), tasks.size()-1);
		}

		if (!tasks.isEmpty())
			fireTableRowsUpdated(0, tasks.size()-1);
	}

	@Override
	public int getRowCount() {
		return tasks.size();
	}

    /**
     * Default implementation return null as no tooltips are supported by default
     * @param rowIndex Row index of cell
     * @param columnIndex Column index of cell
     * @return Return null by default
     */
    @Override
    public String getToolTipAt(int rowIndex, int columnIndex) {
		String result = null;
		if ((columnIndex == SCORE_COL) && (rowIndex < tasks.size())) {
			SettlementTask selectedTask = tasks.get(rowIndex).task;

			StringBuilder builder = new StringBuilder();
			builder.append("<html><b>Task:").append(selectedTask.getName()).append("</b><br>");
			builder.append(RatingScoreRenderer.getHTMLFragment(selectedTask.getScore()));
			builder.append("</html>");

			result = builder.toString();
		}
        return result;
    }

	/**
	 * Gets the value of a cell of a SettlementTask.
	 * 
     * @param rowIndex Row index of cell
     * @param columnIndex Column index of cell
     * @return Object value of the cell
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (tasks.size() <= rowIndex) {
			// Request is in the middle of an updating
			return null;
		}

		var selectedRow = tasks.get(rowIndex);
		var selectedTask = selectedRow.task;
		switch(columnIndex) {
			case ENTITY_COL:
				Entity des = selectedTask.getFocus();
				if (des != null)
					return des.getName();
				return null;
			case SETTLEMENT_COL:
				return selectedRow.owner.getName();
			case DESC_COL:
				return selectedTask.getShortName();
			case EVA_COL:
				return (selectedTask.isEVA() ? YES: NO);
			case SCOPE_COL:
				return switch(selectedTask.getScope()) {
					case ANY_HOUR -> ANY;
					case NONWORK_HOUR -> OFF_DUTY_ONLY;
					case WORK_HOUR -> ON_DUTY_ONLY;
				};
			case DEMAND_COL:
				return selectedTask.getDemand();
			case SCORE_COL:
				return selectedTask.getScore().getScore();
			default:
				return null;
		}
	}
}
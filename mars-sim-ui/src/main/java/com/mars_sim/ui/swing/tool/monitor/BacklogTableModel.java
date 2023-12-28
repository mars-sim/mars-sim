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
import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitEvent;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.UnitListener;
import com.mars_sim.core.person.ai.task.util.SettlementTask;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.tools.Msg;

/**
 * This class models how SettlementTasks are organized and displayed
 * within the Monitor Window for a settlement.
 */
@SuppressWarnings("serial")
public class BacklogTableModel extends AbstractMonitorModel
					implements UnitListener {
	// Represents a row in the table
	private record BacklogEntry(Settlement owner, SettlementTask task) implements Serializable {}

	private static final ColumnSpec[] COLUMNS;

	private static final int DESC_COL = 0;
	private static final int SETTLEMENT_COL = DESC_COL+1;
	private static final int ENTITY_COL = SETTLEMENT_COL+1;
	private static final int EVA_COL = ENTITY_COL+1;
	private static final int DEMAND_COL = EVA_COL+1;
	static final int SCORE_COL = DEMAND_COL+1;

	static {
		COLUMNS = new ColumnSpec[SCORE_COL+1];
		COLUMNS[ENTITY_COL] = new ColumnSpec("Entity", String.class);
		COLUMNS[SETTLEMENT_COL] = new ColumnSpec("Settlement", String.class);
		COLUMNS[DESC_COL] = new ColumnSpec("Description", String.class);
		COLUMNS[DEMAND_COL]  = new ColumnSpec("Demand", Integer.class);
		COLUMNS[EVA_COL]  = new ColumnSpec("EVA", String.class);
		COLUMNS[SCORE_COL] = new ColumnSpec("Score", Double.class);
	}

	private Set<Settlement> selectedSettlements = Collections.emptySet();
	private boolean monitorSettlement = false;
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
	public void unitUpdate(UnitEvent event) {
		Unit unit = (Unit) event.getSource();
		UnitEventType eventType = event.getType();
		if ((eventType == UnitEventType.BACKLOG_EVENT) && selectedSettlements.contains(unit)) {
			var newTasks = getTasks();

			// Reset the Tasks asynchronously in teh Swing Dispatcher to avoid sorting clashes
			SwingUtilities.invokeLater(() -> resetTasks(newTasks));
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
				selectedSettlements.forEach(s -> s.addUnitListener(this));
			}
			else {
				selectedSettlements.forEach(s -> s.removeUnitListener(this));
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
		selectedSettlements.forEach(s -> s.removeUnitListener(this));

		super.destroy();
	}

	/**
	 * Sets the Settlement filter.
	 * 
	 * @param filter Settlement
	 */
	@Override
    public boolean setSettlementFilter(Set<Settlement> filter) {
		selectedSettlements.forEach(s -> s.removeUnitListener(this));

		// Initialize settlements.
		selectedSettlements = filter;	

		// Initialize task list; backlog maybe null.
		tasks = getTasks();
		fireTableDataChanged();
			
		// Add table as listener to each settlement.
		if (monitorSettlement) {
			selectedSettlements.forEach(s -> s.addUnitListener(this));
		}

		return true;
    }

	/**
	 * Create a list od backlog entries for all the monitored settlements.
	 * The SettlementTask does holdthe Settlement refernece so this is record in
	 * the artifical BacklogEntry record.
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

			result = selectedTask.getScore().getHTMLOutput();
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
				return (selectedTask.isEVA() ? "Yes" : "No");
			case DEMAND_COL:
				return selectedTask.getDemand();
			case SCORE_COL:
				return selectedTask.getScore().getScore();
			default:
				return null;
		}
	}
}
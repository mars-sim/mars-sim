/*
 * Mars Simulation Project
 * BacklogTableModel.java
 * @date 2022-12-02
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.Entity;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.person.ai.task.util.SettlementTask;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * This class models how SettlementTasks are organized and displayed
 * within the Monitor Window for a settlement.
 */
public class BacklogTableModel extends AbstractMonitorModel
					implements UnitListener {

	private static final ColumnSpec[] COLUMNS;

	private static final int DESC_COL = 0;
	private static final int ENTITY_COL = 1;
	private static final int EVA_COL = 2;
	private static final int DEMAND_COL = 3;
	static final int SCORE_COL = 4;

	static {
		COLUMNS = new ColumnSpec[SCORE_COL+1];
		COLUMNS[ENTITY_COL] = new ColumnSpec("Entity", String.class);
		COLUMNS[DESC_COL] = new ColumnSpec("Description", String.class);
		COLUMNS[DEMAND_COL]  = new ColumnSpec("Demand", Integer.class);
		COLUMNS[EVA_COL]  = new ColumnSpec("EVA", String.class);
		COLUMNS[SCORE_COL] = new ColumnSpec("Score", Double.class);
	}

	private Settlement selectedSettlement;
	private boolean monitorSettlement = false;
	private List<SettlementTask> tasks;

	/**
	 * Constructor.
	 */
	public BacklogTableModel(Settlement selectedSettlement) {
		super(Msg.getString("BacklogTableModel.tabName"),
							"BacklogTableModel.counting",
							COLUMNS);
		
		setSettlementFilter(selectedSettlement);
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
		if ((eventType == UnitEventType.BACKLOG_EVENT) && unit.equals(selectedSettlement)) {
			// Reset the Tasks
			resetTasks();
		}
	}

	/**
	 * The Object 
	 */
	@Override
	public Object getObject(int row) {
		return tasks.get(row).getFocus();
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
				selectedSettlement.addUnitListener(this);
			}
			else {
				selectedSettlement.removeUnitListener(this);
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
		selectedSettlement.removeUnitListener(this);

		super.destroy();
	}

	/**
	 * Sets the Settlement filter.
	 * 
	 * @param filter Settlement
	 */
	@Override
    public boolean setSettlementFilter(Settlement filter) {
		if (selectedSettlement != null) {
			selectedSettlement.removeUnitListener(this);
		}

		// Initialize settlements.
		selectedSettlement = filter;	

		// Initialize task list; backlog maybe null.
		List<SettlementTask> newTasks = selectedSettlement.getTaskManager().getAvailableTasks();
		if (newTasks == null) {
			tasks = Collections.emptyList();
		}
		else {
			tasks = new ArrayList<>(newTasks);
		}
		fireTableDataChanged();
			
		// Add table as listener to each settlement.
		if (monitorSettlement) {
			selectedSettlement.addUnitListener(this);
		}

		return true;
    }

    /**
     * Resets tasks.
     */
	private void resetTasks() {
		List<SettlementTask> oldTasks = tasks;
		tasks = new ArrayList<>(selectedSettlement.getTaskManager().getAvailableTasks());

		Set<SettlementTask> common = tasks.stream()
						.filter(oldTasks::contains)
						.collect(Collectors.toSet());
		
		// CHeck for deleted Task
		int i = 0;
		for(SettlementTask old : oldTasks) {
			if (!common.contains(old)) {
				fireTableRowsDeleted(i, i);
			}
			else {
				i++;
			}
		}
		i = 0;
		for(SettlementTask newTask : tasks) {
			if (!common.contains(newTask)) {
				fireTableRowsInserted(i, i);
			}
			i++;
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
			SettlementTask selectedTask = tasks.get(rowIndex);

			result = selectedTask.getScore().getHTMLOutput();
		}
        return result;
    }

	/**
	 * Get the value of a cell of a SettlementTask
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

		SettlementTask selectedTask = tasks.get(rowIndex);
		switch(columnIndex) {
			case ENTITY_COL:
				Entity des = selectedTask.getFocus();
				if (des != null)
					return des.getName();
				return null;
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
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

import javax.swing.table.AbstractTableModel;

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
@SuppressWarnings("serial")
public class BacklogTableModel extends AbstractTableModel
					implements MonitorModel, UnitListener {

	
	private static final int DESC_COL = 0;
	private static final int ENTITY_COL = 1;
	private static final int DEMAND_COL = 2;
	static final int SCORE_COL = 3;

	private String name = null;
	private Settlement selectedSettlement;
	private boolean monitorSettlement = false;
	private List<SettlementTask> tasks;

	/**
	 * Constructor.
	 */
	public BacklogTableModel(Settlement selectedSettlement) {
		name = Msg.getString("BacklogTableModel.tabName");
		
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
	 * Has this model got a natural order that the model conforms to ?
	 * If true, then it implies that the user should not be allowed to order.
	 */
	public boolean getOrdered() {
		return false;
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

	@Override
	public int getColumnCount() {
		return SCORE_COL+1;
	}

	@Override
	public String getColumnName(int index) {
		return switch(index) {
			case ENTITY_COL -> "Entity";
			case DESC_COL -> "Description";
			case DEMAND_COL -> "Demand";
			case SCORE_COL -> "Score";
			default -> throw new IllegalArgumentException("Unexpected value: " + index);
		};
	}

	@Override
	public Class<?> getColumnClass(int index) {
		return switch(index) {
			case ENTITY_COL ->  String.class;
			case DESC_COL ->  String.class;
			case DEMAND_COL ->  Integer.class;
			case SCORE_COL ->  Double.class;
			default -> throw new IllegalArgumentException("Unexpected value: " + index);
		};
	}

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
				return selectedTask.getName();
			case DEMAND_COL:
				return selectedTask.getDemand();
			case SCORE_COL:
				return selectedTask.getScore().getScore();
			default:
				return null;
		}
	}

	@Override
	public String getName() {
		return name;
	}

    /**
	 * Gets the model count string.
	 */
	@Override
	public String getCountString() {
		return Msg.getString("BacklogTableModel.counting", tasks.size());
	}

}
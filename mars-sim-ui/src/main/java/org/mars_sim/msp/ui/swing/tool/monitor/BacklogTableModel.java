/*
 * Mars Simulation Project
 * BacklogTableModel.java
 * @date 2022-12-02
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.util.Collections;
import java.util.List;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.person.ai.task.util.SettlementTask;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * This class model how SettlementTasks are organized and displayed
 * within the Monitor Window for a settlement.
 */
@SuppressWarnings("serial")
public class BacklogTableModel extends EntityTableModel<SettlementTask>
implements UnitListener {
	
	protected static final int NUM_INITIAL_COLUMNS = 2;
	protected static final int NUM_DATA_COL = 7;

	/** Names of Columns. */
	private static final String[] columnNames;
	/** Types of columns. */
	private static final Class<?>[] columnTypes;

	private static final int DESC_COL = 0;
	private static final int DEMAND_COL = 1;
	static final int SCORE_COL = 2;

	
	static {
		columnNames = new String[SCORE_COL+1];
		columnTypes = new Class[SCORE_COL+1];

		columnNames[DESC_COL] = "Description";
		columnTypes[DESC_COL] = String.class;
		columnNames[DEMAND_COL] =  "Demand";
		columnTypes[DEMAND_COL] = Integer.class;
		columnNames[SCORE_COL] = "Score";
		columnTypes[SCORE_COL] = Double.class;
	};

	private Settlement selectedSettlement;
	private boolean monitorSettlement = false;

	/**
	 * Constructor.
	 */
	public BacklogTableModel(Settlement selectedSettlement) {
		super(Msg.getString("BacklogTableModel.tabName"), "BacklogTableModel.counting",
							columnNames, columnTypes);
		

		setSettlementFilter(selectedSettlement);
	}

	/**
	 * Catch unit update event.
	 *
	 * @param event the unit event.
	 */
	@Override
	public void unitUpdate(UnitEvent event) {
		Unit unit = (Unit) event.getSource();
		UnitEventType eventType = event.getType();
		if (eventType == UnitEventType.BACKLOG_EVENT) {
			if (unit.equals(selectedSettlement)) {
				// Reset the Tasks
				resetTasks();
			}
		}
	}

	/**
	 * Has this model got a natural order that the model conforms to. If this value
	 * is true, then it implies that the user should not be allowed to order.
	 */
	public boolean getOrdered() {
		return false;
	}

	protected Object getEntityValue(SettlementTask selectedTask, int columnIndex) {
		switch(columnIndex) {
			case DESC_COL:
				return selectedTask.getDescription();
			case DEMAND_COL:
				return selectedTask.getDemand();
			case SCORE_COL:
				return selectedTask.getScore();
			default:
				return null;
		}
	}
    
	/**
	 * Set whether the changes to the Entities should be monitor for change. Set up the 
	 * Unitlisteners for the selected Settlement where Food comes from for the table.
	 * @param activate 
	 */
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
		super.destroy();

		// Remove as listener for all settlements.
		selectedSettlement.removeUnitListener(this);
	}

	/**
	 * Set the Settlement filter
	 * @param filter Settlement
	 */
    public boolean setSettlementFilter(Settlement filter) {
		if (selectedSettlement != null) {
			selectedSettlement.removeUnitListener(this);
		}

		// Initialize settlements.
		selectedSettlement = filter;	

		resetTasks();
			
		// Add table as listener to each settlement.
		if (monitorSettlement) {
			selectedSettlement.addUnitListener(this);
		}

		return true;
    }

	private void resetTasks() {
		// Initialize task list; backlog maybe null.
		List<SettlementTask> tasks = selectedSettlement.getTaskManager().getAvailableTasks();
		if (tasks == null) {
			tasks = Collections.emptyList();
		}
		resetEntities(tasks);
	}
}
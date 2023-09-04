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
 * This class models how SettlementTasks are organized and displayed
 * within the Monitor Window for a settlement.
 */
@SuppressWarnings("serial")
public class BacklogTableModel extends EntityTableModel<SettlementTask>
implements UnitListener {

	/** Names of Columns. */
	private static final String[] columnNames;
	/** Types of columns. */
	private static final Class<?>[] columnTypes;
	
	private static final int DESC_COL = 0;
	private static final int ENTITY_COL = 1;
	private static final int DEMAND_COL = 2;
	static final int SCORE_COL = 3;

	
	static {
		columnNames = new String[SCORE_COL + 1];
		columnTypes = new Class[SCORE_COL + 1];
		columnNames[ENTITY_COL] = "Entity";
		columnTypes[ENTITY_COL] = String.class;
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
	 * Catches unit update event.
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
	 * Has this model got a natural order that the model conforms to ?
	 * If true, then it implies that the user should not be allowed to order.
	 */
	public boolean getOrdered() {
		return false;
	}

	/**
	 * Gets the value of the object.
	 */
	protected Object getEntityValue(SettlementTask selectedTask, int columnIndex) {
		switch(columnIndex) {
			case ENTITY_COL:
				String des = selectedTask.getDescription();
				int index = des.indexOf(" @");
				if (index == -1)
					return "None";
				else
					return des.substring(index + 3).replace("@", "");
			case DESC_COL:
				des = selectedTask.getDescription();
				index = des.indexOf(" @");
				if (index == -1)
					return des;
				else
					return des.substring(0, index);
			case DEMAND_COL:
				return selectedTask.getDemand();
			case SCORE_COL:
				return selectedTask.getScore();
			default:
				return null;
		}
	}
    
	/**
	 * Sets whether the changes to the Entities should be monitor for change. Sets up the 
	 * unit listeners for the selected Settlement where Food comes from for the table.
	 * 
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
	 * Sets the Settlement filter.
	 * 
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

    /**
     * Resets tasks.
     */
	private void resetTasks() {
		// Initialize task list; backlog maybe null.
		List<SettlementTask> tasks = selectedSettlement.getTaskManager().getAvailableTasks();
		if (tasks == null) {
			tasks = Collections.emptyList();
		}
		resetEntities(tasks);
	}
}
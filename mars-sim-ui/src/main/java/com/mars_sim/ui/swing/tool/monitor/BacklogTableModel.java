/*
 * Mars Simulation Project
 * BacklogTableModel.java
 * @date 2022-12-02
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.Entity;
import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.person.ai.task.util.SettlementTask;
import com.mars_sim.core.person.ai.task.util.SettlementTaskManager;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.utils.EntityModel;
import com.mars_sim.ui.swing.utils.RatingScoreRenderer;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * This class models how SettlementTasks are organized and displayed
 * within the Monitor Window for a settlement.
 */
@SuppressWarnings("serial")
public class BacklogTableModel extends AbstractMonitorModel
					implements EntityListener, EntityModel {
	
	private static final ColumnSpec[] COLUMNS;

	private static final int DESC_COL = 0;
	private static final int SETTLEMENT_COL = DESC_COL+1;
	private static final int ENTITY_COL = SETTLEMENT_COL+1;
	private static final int CREATED_COL = ENTITY_COL+1;
	private static final int EVA_COL = CREATED_COL+1;
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
		COLUMNS[ENTITY_COL] = new ColumnSpec(Msg.getString("entity.name"), String.class);
		COLUMNS[SETTLEMENT_COL] = new ColumnSpec(Msg.getString("settlement.singular"), String.class);
		COLUMNS[DESC_COL] = new ColumnSpec("Description", String.class);
		COLUMNS[CREATED_COL]  = new ColumnSpec("Created", MarsTime.class);
		COLUMNS[DEMAND_COL]  = new ColumnSpec("Demand", Integer.class);
		COLUMNS[EVA_COL]  = new ColumnSpec("EVA", String.class);
		COLUMNS[SCOPE_COL]  = new ColumnSpec("Scope", String.class);
		COLUMNS[SCORE_COL] = new ColumnSpec("Score", Double.class, ColumnSpec.STYLE_DIGIT2);
	}
	
	private List<SettlementTask> tasks = Collections.emptyList();

	/**
	 * Constructor.
	 */
	public BacklogTableModel() {
		super(Msg.getString("BacklogTableModel.tabName"),
							COLUMNS);	
		setSettlementColumn(SETTLEMENT_COL);
		setCountingMsgKey("BacklogTableModel.counting");
	}

	/**
	 * Catches unit update event.
	 *
	 * @param event the unit event.
	 */
	@Override
	public void entityUpdate(EntityEvent event) {
		if (event.getTarget() instanceof SettlementTask st
				&& event.getSource() instanceof Settlement s
				&& getSelectedSettlements().contains(s)) {
			switch(event.getType()) {
				case SettlementTaskManager.NEWTASK_EVENT -> SwingHelper.runInEDT(() -> addTask(st));
				case SettlementTaskManager.REMOVETASK_EVENT -> SwingHelper.runInEDT(() -> removeTask(st));
				case SettlementTaskManager.UPDATETASK_EVENT -> SwingHelper.runInEDT(() -> updateTask(st));
				default -> {
					// Ignore other events
				}
			}
		}
	}

	private void addTask(SettlementTask st) {
		tasks.add(st);
		fireTableRowsInserted(tasks.size()-1, tasks.size()-1);
	}

	private void removeTask(SettlementTask st) {
		int index = tasks.indexOf(st);
		if (index != -1) {
			tasks.remove(index);
			fireTableRowsDeleted(index, index);
		}
	}

	private void updateTask(SettlementTask st) {
		int index = tasks.indexOf(st);
		if (index != -1) {
			tasks.set(index, st);

			// Just update the changed cells, i.e. parameters
			fireTableCellUpdated(index, SCORE_COL);
			fireTableCellUpdated(index, DEMAND_COL);
			fireTableCellUpdated(index, CREATED_COL);
		}
	}

	/**
	 * Gets the Object.
	 */
	@Override
	public Entity getAssociatedEntity(int row) {
		return tasks.get(row).getFocus();
	}

	/**
	 * Sets whether the changes to the Entities should be monitor for change. Sets up the 
	 * unit listeners for the selected Settlement where Food comes from for the table.
	 * 
	 * @param activate 
	 */
	@Override
    public void enableListeners(boolean activate) {
		if (activate) {
			getSelectedSettlements().forEach(s -> s.addEntityListener(this));
		}
		else {
			getSelectedSettlements().forEach(s -> s.removeEntityListener(this));
		}
	}

	/**
	 * Prepares the model for deletion.
	 */
	@Override
	public void release() {
		// Remove as listener for all settlements.
		getSelectedSettlements().forEach(s -> s.removeEntityListener(this));

		super.release();
	}

	/**
	 * Sets the Settlement filter.
	 * 
	 * @param selectedSettlements Settlement
	 */
	@Override
    protected boolean applySettlementFilter(Set<Settlement> selectedSettlements) {
		getSelectedSettlements().forEach(s -> s.removeEntityListener(this));

		// Initialize task list; backlog maybe null.
		tasks = getTasks();
		fireTableDataChanged();
			
		// Add table as listener to each settlement.
		selectedSettlements.forEach(s -> s.addEntityListener(this));

		return true;
    }

	/**
	 * Creates a list of backlog entries for all the monitored settlements.
	 * The SettlementTask does hold the Settlement reference so this is record in
	 * the artificial BacklogEntry record.
	 */
	private List<SettlementTask> getTasks() {
		List<SettlementTask> newTasks = new ArrayList<>();
		getSelectedSettlements().forEach(s -> newTasks.addAll(s.getTaskManager().getAvailableTasks()));
		return newTasks;
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

		var selectedTask = tasks.get(rowIndex);
		switch(columnIndex) {
			case ENTITY_COL:
				Entity des = selectedTask.getFocus();
				if (des != null)
					return des.getName();
				return null;
			case SETTLEMENT_COL:
				return selectedTask.getOwner().getName();
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
			case CREATED_COL:
				return selectedTask.getCreatedOn();
			case DEMAND_COL:
				return selectedTask.getDemand();
			case SCORE_COL:
				return selectedTask.getScore().getScore();
			default:
				return null;
		}
	}
}
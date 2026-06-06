/*
 * Mars Simulation Project
 * BaseWorkerModel.java
 * @date 2026-06-04
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils.model;

import java.util.Set;

import com.mars_sim.core.person.ai.task.util.TaskManager;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.components.ColumnSpec;

/**
 * A generic table model showing Workers. It provides a number of predefined available columns.
 * The subclass defines which columns are to be rendered.
 * The model automatically monitors the Worker for changes and updates the table as needed.
 */
public abstract class BaseWorkerModel extends AbstractEntityModel<Worker> {

    private static final int NAME_VAL = 0;
    private static final int TASK_VAL = 1;
    private static final int SETTLEMENT_VAL = 2;

    // Show the Worker name, passive and unchanging
    protected static final EntityColumnSpec NAME = new EntityColumnSpec(new ColumnSpec(NAME_VAL, Msg.getString("entity.name"),
                                                        String.class), null);
    // Show the Worker task, reacts to events
    protected static final EntityColumnSpec TASK = new EntityColumnSpec(new ColumnSpec(TASK_VAL, Msg.getString("task.singular"), 
                                                        String.class), Set.of(TaskManager.TASK_EVENT));

    // Show the Worker's settlement, paassive and unchanging
    protected static final EntityColumnSpec SETTLEMENT = new EntityColumnSpec(new ColumnSpec(SETTLEMENT_VAL, Msg.getString("settlement.singular"),
                                                    String.class), null);   
    /**
     * Create a generic worker model with the specified columns.
     * @param columns Columns to show.
     */
    protected BaseWorkerModel(EntityColumnSpec... columns) {
        super(columns);
    }

    /**
     * Get a cell value for the associated Worker. Column index maps to the associated ColumnSpec where the id
     * is used to determine the value to return.
     * @param entity The Worker entity.
     * @param valueIndex Column index. 
     * @return Associated value.
     */
    @Override
    protected Object getEntityValue(Worker entity, int valueIndex) {
        return getWorkerValue(entity, valueIndex);
    }

    /**
     * Get a cell value for the associated Worker. Column index maps to the associated ColumnSpec where the id
     * is used to determine the value to return.
     * @param entity The Worker entity. 
     * @param valueIndex Column index. 
     * @return Associated value.        
     * @see #getEntityValue(Worker, int)
     */
    public static Object getWorkerValue(Worker entity, int valueIndex) {
        return switch(valueIndex) {
            case NAME_VAL-> entity.getName();
            case TASK_VAL -> entity.getTaskManager().getTaskName();
            case SETTLEMENT_VAL -> entity.getAssociatedSettlement().getName();
            default -> null;
        };
    }
}
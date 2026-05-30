/*
 * Mars Simulation Project
 * GenericRobotModel.java
 * @date 2026-05-23
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils.model;

import java.util.Set;

import com.mars_sim.core.person.ai.task.util.TaskManager;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.components.ColumnSpec;

/**
 * A generic table model showing Robots. It provides a number of predefined available columns.
 * The subclass defines which columns are to be rendered.
 * The model automatically monitors the Robot for changes and updates the table as needed.
 */
public abstract class BaseRobotModel extends AbstractEntityModel<Robot> {

    private static final int NAME_VAL = 0;
    private static final int TASK_VAL = 1;

    // Show the Robot name, passive and unchanging
    protected static final EntityColumnSpec NAME = new EntityColumnSpec(new ColumnSpec(NAME_VAL, Msg.getString("entity.name"),
                                                        String.class), null);
    // Show the Robot task, reacts to events
    protected static final EntityColumnSpec TASK = new EntityColumnSpec(new ColumnSpec(TASK_VAL, Msg.getString("task.singular"), 
                                                        String.class), Set.of(TaskManager.TASK_EVENT));

    /**
     * Create a generic robot model with the specified columns.
     * @param columns Columns to show.
     */
    protected BaseRobotModel(EntityColumnSpec... columns) {
        super(columns);
    }

    /**
     * Get a cell value for the associated Robot. Column index maps to the associated ColumnSpec where the id
     * is used to determine the value to return.
     * @param entity The Robot entity.
     * @param valueIndex Column index. 
     * @return Associated value.
     */
    @Override
    protected Object getEntityValue(Robot entity, int valueIndex) {
        return switch(valueIndex) {
            case NAME_VAL-> entity.getName();
            case TASK_VAL -> entity.getBotMind().getBotTaskManager().getTaskName();
            default -> "";
        };
    }
}
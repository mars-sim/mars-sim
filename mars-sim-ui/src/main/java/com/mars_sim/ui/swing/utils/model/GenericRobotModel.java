/*
 * Mars Simulation Project
 * GenericRobotModel.java
 * @date 2026-05-23
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils.model;

import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.components.ColumnSpec;

/**
 * A generic table model showing Robots. It provides a number of predefined available columns.
 * The subclass defines which columns are to be rendered.
 * The model automatically monitors the Robot for changes and updates the table as needed.
 */
public abstract class GenericRobotModel extends AbstractEntityModel<Robot> {

    private static final int NAME_VAL = 0;
    private static final int TASK_VAL = 1;

    protected static final ColumnSpec NAME = new ColumnSpec(NAME_VAL, Msg.getString("entity.name"), String.class, ColumnSpec.STYLE_DEFAULT);
    protected static final ColumnSpec TASK = new ColumnSpec(TASK_VAL, Msg.getString("task.singular"), String.class, ColumnSpec.STYLE_DEFAULT);

    /**
     * Create a generic robot model with the specified columns.
     * @param columns Columns to show.
     */
    protected GenericRobotModel(ColumnSpec... columns) {
        super(columns);
    }

    /**
     * Get a cell value for the associated Robot. Column index maps to the assoicated ColumnSpec where the id
     * is used to determine the value to return.
     * @param rowIndex Row index
     * @param columnIndex Column index. 
     * @return Associated value.
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        var spec = getColumnSpec(columnIndex);
        var r = (Robot) getAssociatedEntity(rowIndex);
        return switch(spec.id()) {
            case NAME_VAL-> r.getName();
            case TASK_VAL -> r.getBotMind().getBotTaskManager().getTaskName();
            default -> "";
        };
    }
}
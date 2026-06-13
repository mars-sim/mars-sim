/*
 * Mars Simulation Project
 * BaseRobotModel.java
 * @date 2026-05-23
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils.model;

import com.mars_sim.core.robot.Robot;

/**
 * A generic table model showing Robots. It provides a number of predefined available columns.
 * The subclass defines which columns are to be rendered. This reuses the BaseWorkerModel for common columns.
 * The model automatically monitors the Robot for changes and updates the table as needed.
 */
public abstract class BaseRobotModel extends AbstractEntityModel<Robot> {

    // Show the Robot name, passive and unchanging
    protected static final EntityColumnSpec NAME = BaseWorkerModel.NAME;
    // Show the Robot task, reacts to events
    protected static final EntityColumnSpec TASK = BaseWorkerModel.TASK;

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
        return BaseWorkerModel.getWorkerValue(entity, valueIndex);
    }
}
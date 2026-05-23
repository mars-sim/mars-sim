/*
 * Mars Simulation Project
 * GenericPersonModel.java
 * @date 2026-05-21
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils.model;

import com.mars_sim.core.location.LocationStateType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.components.ColumnSpec;

/**
 * A generic table model showing Persons. It provides a number of predefined available columns.
 * The subclass defines which columns are to be rendered.
 * The model automatically monitors the Person for changes and updates the table as needed.
 */
public abstract class GenericPersonModel extends AbstractEntityModel<Person> {

    private static final int NAME_VAL = 0;
    private static final int INSIDE_VAL = 1;
    private static final int TASK_VAL = 2;

    protected static final ColumnSpec NAME = new ColumnSpec(NAME_VAL, Msg.getString("entity.name"), String.class, ColumnSpec.STYLE_DEFAULT);
    protected static final ColumnSpec INSIDE = new ColumnSpec(INSIDE_VAL, "Inside", Boolean.class, ColumnSpec.STYLE_DEFAULT);
    protected static final ColumnSpec TASK = new ColumnSpec(TASK_VAL, Msg.getString("task.singular"), String.class, ColumnSpec.STYLE_DEFAULT);

    /**
     * Create a generic person model with the specified columns.
     * @param columns Columns to show.
     */
    protected GenericPersonModel(ColumnSpec... columns) {
        super(columns);
    }

    /**
     * Get a cell value for the associated Person. Column index maps to the assoicated ColumnSpec where the id
     * is used to determine the value to return.
     * @param rowIndex Row index
     * @param columnIndex Column index. 
     * @return Associated value.
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        var spec = getColumnSpec(columnIndex);
        Person c = (Person) getAssociatedEntity(rowIndex);
        return switch(spec.id()) {
            case NAME_VAL-> c.getName();
            case INSIDE_VAL -> c.getLocationStateType() == LocationStateType.INSIDE_SETTLEMENT;
            case TASK_VAL -> c.getMind().getTaskManager().getTaskName();
            default -> "";
        };
    }
}
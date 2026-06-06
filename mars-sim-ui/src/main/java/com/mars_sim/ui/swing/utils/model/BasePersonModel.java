/*
 * Mars Simulation Project
 * BasePersonModel.java
 * @date 2026-05-21
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils.model;

import java.util.Set;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.unit.MobileUnit;
import com.mars_sim.ui.swing.components.ColumnSpec;

/**
 * A generic table model showing Persons. It provides a number of predefined available columns.
 * The subclass defines which columns are to be rendered.
 * The model automatically monitors the Person for changes and updates the table as needed.
 */
public abstract class BasePersonModel extends AbstractEntityModel<Person> {

    private static final int INSIDE_VAL = 100;

    // Columns based on Worker
    protected static final EntityColumnSpec NAME = BaseWorkerModel.NAME;
    protected static final EntityColumnSpec TASK = BaseWorkerModel.TASK;
    protected static final EntityColumnSpec SETTLEMENT = BaseWorkerModel.SETTLEMENT;

    // Display whether the Person is inside based on a changed of container
    protected static final EntityColumnSpec INSIDE = new EntityColumnSpec(new ColumnSpec(INSIDE_VAL, "Inside", Boolean.class),
                                                            Set.of(MobileUnit.CONTAINER_EVENT));

    /**
     * Create a generic person model with the specified columns.
     * @param columns Columns to show.
     */
    protected BasePersonModel(EntityColumnSpec... columns) {
        super(columns);
    }

    /**
     * Get a cell value for the associated Person. Column index maps to the associated ColumnSpec where the id
     * is used to determine the value to return.
     * @param entity The Person entity.
     * @param valueIndex Column index. 
     * @return Associated value.
     */
    @Override
    protected Object getEntityValue(Person entity, int valueIndex) {
        return switch(valueIndex) {
            case INSIDE_VAL -> entity.isInside();        
            default -> BaseWorkerModel.getWorkerValue(entity, valueIndex);
        };
    }
}
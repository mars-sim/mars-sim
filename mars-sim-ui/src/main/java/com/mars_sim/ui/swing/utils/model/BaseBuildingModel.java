/*
 * Mars Simulation Project
 * BaseBuildingModel.java
 * @date 2026-06-08
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils.model;

import java.util.Set;

import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.components.ColumnSpec;

/**
 * A generic table model showing Buildings. It provides a number of predefined available columns.
 * The subclass defines which columns are to be rendered.
 * The model automatically monitors the Building for changes and updates the table as needed.
 */
public abstract class BaseBuildingModel extends AbstractEntityModel<Building> {

    private static final int NAME_VAL = 0;

    // Basic fixed properties of a Building
    protected static final EntityColumnSpec NAME = new EntityColumnSpec(new ColumnSpec(NAME_VAL, Msg.getString("entity.name"), String.class),
                                Set.of(EntityEventType.NAME_EVENT));

    /**
     * Create a generic building model with the specified columns.
     * @param columns Columns to show.
     */
    protected BaseBuildingModel(EntityColumnSpec... columns) {
        super(columns);
    }

    /**
     * Get a cell value for the associated Building. Column index maps to the associated ColumnSpec where the id
     * is used to determine the value to return.
     * @param entity The Building entity.
     * @param valueIndex Column index.
     * @return Associated value.
     */
    @Override
    protected Object getEntityValue(Building entity, int valueIndex) {
        return switch (valueIndex) {
            case NAME_VAL -> entity.getName();
            default -> "";
        };
    }
}
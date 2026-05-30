/*
 * Mars Simulation Project
 * BaseVehicleModel.java
 * @date 2026-05-23
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils.model;

import java.util.Set;

import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.components.ColumnSpec;

/**
 * A generic table model showing Vehicles. It provides a number of predefined available columns.
 * The subclass defines which columns are to be rendered.
 * The model automatically monitors the Vehicle for changes and updates the table as needed.
 */
public abstract class BaseVehicleModel extends AbstractEntityModel<Vehicle> {

    private static final int NAME_VAL = 0;
    private static final int MISSION_VAL = 1;
    private static final int TYPE_VAL = 2;

    // Basic fixed properties of a Vehicle
    protected static final EntityColumnSpec NAME = new EntityColumnSpec(new ColumnSpec(NAME_VAL, Msg.getString("entity.name"), String.class), null);
    protected static final EntityColumnSpec TYPE = new EntityColumnSpec(new ColumnSpec(TYPE_VAL, Msg.getString("vehicle.type"), String.class), null);

    // Mission property that changes via events
    protected static final EntityColumnSpec MISSION = new EntityColumnSpec(new ColumnSpec(MISSION_VAL, Msg.getString("mission.singular"),
                                String.class), Set.of(Vehicle.MISSION_EVENT));

    /**
     * Create a generic vehicle model with the specified columns.
     * @param columns Columns to show.
     */
    protected BaseVehicleModel(EntityColumnSpec... columns) {
        super(columns);
    }

    /**
     * Get a cell value for the associated Vehicle. Column index maps to the associated ColumnSpec where the id
     * is used to determine the value to return.
     * @param entity The Vehicle entity.
     * @param valueIndex Column index. 
     * @return Associated value.
     */
    @Override
    protected Object getEntityValue(Vehicle entity, int valueIndex) {
        return switch(valueIndex) {
            case NAME_VAL -> entity.getName();
            case MISSION_VAL -> (entity.getMission() != null) ? entity.getMission().getName() : "";
            case TYPE_VAL -> entity.getVehicleType().getName();
            default -> "";
        };
    }
}
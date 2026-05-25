/*
 * Mars Simulation Project
 * GenericVehicleModel.java
 * @date 2026-05-23
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils.model;

import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.components.ColumnSpec;

/**
 * A generic table model showing Vehicles. It provides a number of predefined available columns.
 * The subclass defines which columns are to be rendered.
 * The model automatically monitors the Vehicle for changes and updates the table as needed.
 */
public abstract class GenericVehicleModel extends AbstractEntityModel<Vehicle> {

    private static final int NAME_VAL = 0;
    private static final int MISSION_VAL = 1;
    private static final int TYPE_VAL = 2;

    protected static final ColumnSpec NAME = new ColumnSpec(NAME_VAL, Msg.getString("entity.name"), String.class, ColumnSpec.STYLE_DEFAULT);
    protected static final ColumnSpec MISSION = new ColumnSpec(MISSION_VAL, Msg.getString("mission.singular"), String.class, ColumnSpec.STYLE_DEFAULT);
    protected static final ColumnSpec TYPE = new ColumnSpec(TYPE_VAL, Msg.getString("vehicle.type"), String.class, ColumnSpec.STYLE_DEFAULT);

    /**
     * Create a generic vehicle model with the specified columns.
     * @param columns Columns to show.
     */
    protected GenericVehicleModel(ColumnSpec... columns) {
        super(columns);
    }

    /**
     * Get a cell value for the associated Vehicle. Column index maps to the assoicated ColumnSpec where the id
     * is used to determine the value to return.
     * @param rowIndex Row index
     * @param columnIndex Column index. 
     * @return Associated value.
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        var spec = getColumnSpec(columnIndex);
        var r = (Vehicle) getAssociatedEntity(rowIndex);
        return switch(spec.id()) {
            case NAME_VAL-> r.getName();
            case MISSION_VAL -> (r.getMission() != null) ? r.getMission().getName() : "";
            case TYPE_VAL -> r.getVehicleType().getName();
            default -> "";
        };
    }
}
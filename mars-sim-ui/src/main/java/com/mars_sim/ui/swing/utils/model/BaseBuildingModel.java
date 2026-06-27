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
import com.mars_sim.core.building.utility.heating.Heating;
import com.mars_sim.core.building.utility.power.PowerGrid;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.components.ColumnSpec;

/**
 * A generic table model showing Buildings. It provides a number of predefined available columns.
 * The subclass defines which columns are to be rendered.
 * The model automatically monitors the Building for changes and updates the table as needed.
 */
public abstract class BaseBuildingModel extends AbstractEntityModel<Building> {

    private static final int NAME_VAL = 0;
    private static final int SETTLEMENT_VAL = 1;
    private static final int TYPE_VAL = 2;
    private static final int CATEGORY_VAL = 3;
    private static final int PWR_MODE_VAL = 4;
    private static final int PWR_REQ_VAL = 5;  
    private static final int PWR_GEN_VAL = 6;
    private static final int TEMP_VAL = 7;

    // Basic fixed properties of a Building
    protected static final EntityColumnSpec NAME = new EntityColumnSpec(new ColumnSpec(NAME_VAL, Msg.getString("entity.name"), String.class),
                                Set.of(EntityEventType.NAME_EVENT));
    protected static final EntityColumnSpec SETTLEMENT = new EntityColumnSpec(new ColumnSpec(SETTLEMENT_VAL, Msg.getString("settlement.singular"), String.class),
                                null);
    protected static final EntityColumnSpec TYPE = new EntityColumnSpec(new ColumnSpec(TYPE_VAL, Msg.getString("building.type"), String.class),
                                null);
    protected static final EntityColumnSpec CATEGORY = new EntityColumnSpec(new ColumnSpec(CATEGORY_VAL, Msg.getString("building.category"), String.class),
                                null);
    protected static final EntityColumnSpec PWR_MODE = new EntityColumnSpec(new ColumnSpec(PWR_MODE_VAL, Msg.getString("building.powermode"), String.class),
                                Set.of(Building.POWER_MODE_EVENT));
    protected static final EntityColumnSpec PWR_REQ = new EntityColumnSpec(new ColumnSpec(PWR_REQ_VAL, Msg.getString("building.powerreq"), Double.class),
                                Set.of(PowerGrid.POWER_LOAD_EVENT));   
    protected static final EntityColumnSpec PWR_GEN = new EntityColumnSpec(new ColumnSpec(PWR_GEN_VAL, Msg.getString("building.powergen"), Double.class),
                                Set.of(PowerGrid.GENERATED_POWER_EVENT));
    protected static final EntityColumnSpec TEMP = new EntityColumnSpec(new ColumnSpec(TEMP_VAL, Msg.getString("temperature.shortlabel"), Double.class),
                                Set.of(Heating.TEMPERATURE_EVENT));


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
            case SETTLEMENT_VAL -> entity.getSettlement().getName();
            case TYPE_VAL -> entity.getBuildingType();
            case CATEGORY_VAL -> entity.getCategory().getName();
            case PWR_MODE_VAL -> entity.getPowerMode() != null ? entity.getPowerMode().getName() : null;
			case PWR_REQ_VAL -> entity.getFullPowerLoad();
			case PWR_GEN_VAL -> entity.getGeneratedPower();
            case TEMP_VAL -> entity.getCurrentTemperature();
            default -> "";
        };
    }
}
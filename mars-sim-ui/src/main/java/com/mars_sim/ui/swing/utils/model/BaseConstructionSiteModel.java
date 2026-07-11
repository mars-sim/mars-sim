/*
 * Mars Simulation Project
 * BaseConstructionSiteModel.java
 * @date 2026-07-10
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils.model;

import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.components.ColumnSpec;

import java.util.Set;

import com.mars_sim.core.building.construction.ConstructionSite;

/**
 * This is a model for ConstructionSite entities. It provides a number of predefined available columns.
 * The subclass defines which columns are to be rendered.
 */
public class BaseConstructionSiteModel extends AbstractEntityModel<ConstructionSite> {

    private static final int NAME_VAL = 0;
    private static final int BUILDING_VAL = 1;
    private static final int MISSION_VAL = 2;
    private static final int STAGE_VAL = 3;

    // Basic fixed properties of a ConstructionSite
    protected static final EntityColumnSpec NAME = new EntityColumnSpec(
                    new ColumnSpec(NAME_VAL, Msg.getString("entity.name"), String.class), null);
    protected static final EntityColumnSpec BUILDING = new EntityColumnSpec(
                    new ColumnSpec(BUILDING_VAL, Msg.getString("building.singular"), String.class), null);
    protected static final EntityColumnSpec MISSION = new EntityColumnSpec(
                    new ColumnSpec(MISSION_VAL, Msg.getString("mission.singular"), String.class), 
                            Set.of(ConstructionSite.START_CONSTRUCTION_SITE_EVENT, ConstructionSite.END_CONSTRUCTION_SITE_EVENT));
    protected static final EntityColumnSpec STAGE = new EntityColumnSpec(
                    new ColumnSpec(STAGE_VAL, Msg.getString("constructionstage.singular"), String.class),
                            Set.of(ConstructionSite.ADD_CONSTRUCTION_STAGE_EVENT));

    /**
     * Create a generic ConstructionSite model with the specified columns.
     * @param columns Columns to show.
     */
    public BaseConstructionSiteModel(EntityColumnSpec... columns) {
        super(columns);
    }

    @Override
    protected Object getEntityValue(ConstructionSite entity, int valueIndex) {
        return switch (valueIndex) {
            case NAME_VAL -> entity.getName();
            case BUILDING_VAL -> entity.getBuildingName();
            case MISSION_VAL -> {
                var onSite = entity.getWorkOnSite();
                yield onSite != null ? onSite.getName() : null;
            }
            case STAGE_VAL -> entity.getDescription();
            default -> null;
        };
    }
}

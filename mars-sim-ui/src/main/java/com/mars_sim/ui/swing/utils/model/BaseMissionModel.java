/*
 * Mars Simulation Project
 * BaseMissionModel.java
 * @date 2026-05-23
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils.model;

import java.util.Set;

import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.components.ColumnSpec;

/**
 * A generic table model showing Missions. It provides a number of predefined available columns.
 * The subclass defines which columns are to be rendered.
 * The model automatically monitors the Mission for changes and updates the table as needed.
 */
public abstract class BaseMissionModel extends AbstractEntityModel<Mission> {

    private static final int NAME_VAL = 0;
    private static final int PHASE_VAL = 1;

    // Show Mission name, passive and unchanging
    protected static final EntityColumnSpec NAME = new EntityColumnSpec(new ColumnSpec(NAME_VAL, Msg.getString("entity.name"), String.class),
                                                            null);
    // Show Mission phase description and reacts to events
    protected static final EntityColumnSpec PHASE = new EntityColumnSpec(new ColumnSpec(PHASE_VAL, Msg.getString("mission.phase"), String.class),
                                                            Set.of(Mission.PHASE_EVENT, Mission.PHASE_DESCRIPTION_EVENT));

    /**
     * Create a generic mission model with the specified columns.
     * @param columns Columns to show.
     */
    protected BaseMissionModel(EntityColumnSpec... columns) {
        super(columns);
    }

    /**
     * Get a cell value for the associated Mission. Column index maps to the associated ColumnSpec where the id
     * is used to determine the value to return.
     * @param entity The Mission entity.
     * @param valueIndex Column index. 
     * @return Associated value.
     */
    @Override
    protected Object getEntityValue(Mission entity, int valueIndex) {
        return switch(valueIndex) {
            case NAME_VAL-> entity.getName();
            case PHASE_VAL -> entity.getPhaseDescription();
            default -> "";
        };
    }
}
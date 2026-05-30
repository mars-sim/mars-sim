/*
 * Mars Simulation Project
 * GenericMissionModel.java
 * @date 2026-05-23
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils.model;

import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.components.ColumnSpec;

/**
 * A generic table model showing Missions. It provides a number of predefined available columns.
 * The subclass defines which columns are to be rendered.
 * The model automatically monitors the Mission for changes and updates the table as needed.
 */
public abstract class GenericMissionModel extends AbstractEntityModel<Mission> {

    private static final int NAME_VAL = 0;
    private static final int PHASE_VAL = 1;
    private static final int STAGE_VAL = 2;
    protected static final ColumnSpec NAME = new ColumnSpec(NAME_VAL, Msg.getString("entity.name"), String.class, ColumnSpec.STYLE_DEFAULT);
    protected static final ColumnSpec PHASE = new ColumnSpec(PHASE_VAL, Msg.getString("mission.phase"), String.class, ColumnSpec.STYLE_DEFAULT);
    protected static final ColumnSpec STAGE = new ColumnSpec(STAGE_VAL, Msg.getString("mission.stage"), String.class, ColumnSpec.STYLE_DEFAULT);

    /**
     * Create a generic mission model with the specified columns.
     * @param columns Columns to show.
     */
    protected GenericMissionModel(ColumnSpec... columns) {
        super(columns);
    }

    /**
     * Get a cell value for the associated Mission. Column index maps to the assoicated ColumnSpec where the id
     * is used to determine the value to return.
     * @param rowIndex Row index
     * @param columnIndex Column index. 
     * @return Associated value.
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        var spec = getColumnSpec(columnIndex);
        var m = (Mission) getAssociatedEntity(rowIndex);
        return switch(spec.id()) {
            case NAME_VAL-> m.getName();
            case PHASE_VAL -> m.getPhaseDescription();
            case STAGE_VAL -> m.getStage().name();
            default -> "";
        };
    }
}
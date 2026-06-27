/*
 * Mars Simulation Project
 * BaseScienceStudyModel.java
 * @date 2026-06-09
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils.model;

import java.util.Set;

import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.components.ColumnSpec;

/**
 * A generic table model showing Scientific Studies. It provides a number of predefined available columns.
 * Subclasses can define new columns to be rendered.
 * The model automatically monitors the ScientificStudy for changes and updates the table as needed.
 */
public class BaseScienceStudyModel extends AbstractEntityModel<ScientificStudy> {

    private static final int NAME_VAL = 0;
    private static final int PHASE_VAL = 1;
    private static final int SCIENCE_VAL = 2;
    private static final int LEVEL_VAL = 3;
    private static final int LEAD_VAL = 4;
    private static final int SETTLEMENT_VAL = 5;
    private static final int COMPLETED_VAL = 6;
    
    // Basic fixed properties of a ScientificStudy
    public static final EntityColumnSpec NAME = new EntityColumnSpec(
                    new ColumnSpec(NAME_VAL, Msg.getString("entity.name"), String.class), null);
    public static final EntityColumnSpec SCIENCE = new EntityColumnSpec(
                    new ColumnSpec(SCIENCE_VAL, Msg.getString("scientificstudy.science"), String.class), null);
    public static final EntityColumnSpec LEVEL = new EntityColumnSpec(
                    new ColumnSpec(LEVEL_VAL, Msg.getString("scientificstudy.level"), String.class), null);
    public static final EntityColumnSpec LEAD = new EntityColumnSpec(
                    new ColumnSpec(LEAD_VAL, Msg.getString("scientificstudy.lead"), String.class), null);
    public static final EntityColumnSpec SETTLEMENT = new EntityColumnSpec(
                    new ColumnSpec(SETTLEMENT_VAL, Msg.getString("settlement.singular"), String.class), null);

    // Phase property that changes via events
    public static final EntityColumnSpec PHASE = new EntityColumnSpec(
                    new ColumnSpec(PHASE_VAL, Msg.getString("scientificstudy.phase"), String.class),
                    Set.of(ScientificStudy.PHASE_CHANGE_EVENT));
    public static final EntityColumnSpec COMPLETED = new EntityColumnSpec(
                    new ColumnSpec(COMPLETED_VAL, Msg.getString("scientificstudy.completed"), Double.class, ColumnSpec.STYLE_PERCENTAGE),
                    Set.of(ScientificStudy.PHASE_CHANGE_EVENT));    

    /**
     * Create a generic ScientificStudy model with the specified columns.
     * @param columns Columns to show.
     */
    public BaseScienceStudyModel(EntityColumnSpec... columns) {
        super(columns);
    }

    /**
     * Get a cell value for the associated ScientificStudy. Column index maps to the associated ColumnSpec where the id
     * is used to determine the value to return.
     * @param entity The ScientificStudy entity.
     * @param valueIndex Column index.
     * @return Associated value.
     */
    @Override
    protected Object getEntityValue(ScientificStudy entity, int valueIndex) {
        return switch (valueIndex) {
            case NAME_VAL -> entity.getName();
            case PHASE_VAL -> entity.getPhase().getName();
            case SCIENCE_VAL -> entity.getScience().getName();
            case LEVEL_VAL -> Integer.toString(entity.getDifficultyLevel());
            case LEAD_VAL -> (entity.getPrimaryResearcher() != null) ? entity.getPrimaryResearcher().getName() : "";
            case SETTLEMENT_VAL -> entity.getPrimarySettlement().getName();
            case COMPLETED_VAL -> entity.getPhaseProgress() * 100D;
            default -> "";
        };
    }
}
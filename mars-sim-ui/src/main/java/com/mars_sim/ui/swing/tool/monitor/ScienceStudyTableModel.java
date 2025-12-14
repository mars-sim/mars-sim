/*
 * Mars Simulation Project
 * ScienceStudyTableModel.java
 * @date 2025-12-14
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.ScientificStudyManager;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.utils.ColumnSpec;

/**
 * Table model for Scientific Studies.
 */
public class ScienceStudyTableModel extends EntityMonitorModel<ScientificStudy> {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    private static final int NAME = 0;
	private static final int SETTLEMENT = NAME + 1;
    private static final int SCIENCE = SETTLEMENT + 1;
    private static final int LEVEL = SCIENCE + 1;
    private static final int PHASE = LEVEL + 1;
    private static final int COMPLETED = PHASE + 1;


    private static final ColumnSpec[] COLUMNS;
    static {
        COLUMNS = new ColumnSpec[COMPLETED + 1];

        COLUMNS[NAME] = new ColumnSpec(Msg.getString("Entity.name"), String.class);
        COLUMNS[SETTLEMENT] = new ColumnSpec(Msg.getString("Entity.settlement"), String.class);
        COLUMNS[PHASE] = new ColumnSpec(Msg.getString("ScientificStudy.phase"), String.class);
        COLUMNS[SCIENCE] = new ColumnSpec(Msg.getString("ScientificStudy.science"), String.class);
        COLUMNS[LEVEL] = new ColumnSpec(Msg.getString("ScientificStudy.level"), Integer.class);
        COLUMNS[COMPLETED] = new ColumnSpec(Msg.getString("ScientificStudy.completed"), Double.class, ColumnSpec.STYLE_PERCENTAGE);
    }

    private ScientificStudyManager mgr;

    /**
     * Constructor.
     * 
     * @param settlement the settlement to monitor.
     */
    public ScienceStudyTableModel(ScientificStudyManager mgr) {
		super(Msg.getString("Entity.scientificstudy.pural"),
				COLUMNS);	
		this.mgr = mgr;

		setSettlementColumn(SETTLEMENT);
    }

    /**
     * Update the time dependent values.
     */
    @Override
    public void entityUpdate(EntityEvent event) {
        if (event.getSource() instanceof ScientificStudy ss) {
            entityValueUpdated(ss, PHASE, COMPLETED);
        }
    }

    /**
     * Load the Sceintific Studies for the selected settlements.
     */
    @Override
    public boolean setSettlementFilter(Set<Settlement> selectedSettlement) {
        List<ScientificStudy> studies = new ArrayList<>();
        for (Settlement settlement : selectedSettlement) {
            studies.addAll(mgr.getAllStudies(settlement));
        }

		resetItems(studies);

		return true;
    }

    /**
     * Get the value for the given column.
     */
    @Override
    protected Object getItemValue(ScientificStudy entity, int column) {
        switch (column) {
            case NAME:
                return entity.getName();
            case SETTLEMENT:
                return entity.getPrimarySettlement();
            case PHASE:
                return entity.getPhase().getName();
            case SCIENCE:
                return entity.getScience().getName();
            case LEVEL:
                return entity.getDifficultyLevel();
            case COMPLETED:
                return entity.getPhaseProgress();
            default:
                return null;
        }
    }
}

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

import com.mars_sim.core.Entity;
import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityManagerListener;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.ScientificStudyManager;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.utils.ColumnSpec;

/**
 * Table model for Scientific Studies.
 */
public class ScienceStudyTableModel extends EntityMonitorModel<ScientificStudy>
       implements EntityManagerListener {

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
        COLUMNS[SETTLEMENT] = new ColumnSpec(Msg.getString("settlement.singular"), String.class);
        COLUMNS[PHASE] = new ColumnSpec(Msg.getString("scientificstudy.phase"), String.class);
        COLUMNS[SCIENCE] = new ColumnSpec(Msg.getString("scientificstudy.science"), String.class);
        COLUMNS[LEVEL] = new ColumnSpec(Msg.getString("scientificstudy.level"), Integer.class);
        COLUMNS[COMPLETED] = new ColumnSpec(Msg.getString("scientificstudy.completed"), Double.class, ColumnSpec.STYLE_PERCENTAGE);
    }

    private ScientificStudyManager mgr;

    /**
     * Constructor.
     * 
     * @param mgr the Scientific Study Manager
     */
    public ScienceStudyTableModel(ScientificStudyManager mgr) {
		super(Msg.getString("scientificStudy.plural"),
				COLUMNS);	
		this.mgr = mgr;

		setSettlementColumn(SETTLEMENT);
        mgr.addListener(this);
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
     * Load the Scientific Studies for the selected settlements.
     */
    @Override
    protected boolean applySettlementFilter(Set<Settlement> selectedSettlement) {
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
                return entity.getPrimarySettlement().getName();
            case PHASE:
                return entity.getPhase().getName();
            case SCIENCE:
                return entity.getScience().getName();
            case LEVEL:
                return entity.getDifficultyLevel();
            case COMPLETED:
                return entity.getPhaseProgress() * 100D;
            default:
                return null;
        }
    }

    @Override
    public void entityAdded(Entity newEntity) {
        if (newEntity instanceof ScientificStudy ss
                && getSelectedSettlements().contains(ss.getPrimarySettlement())) {
            addItem(ss);
        } 
    }

    @Override
    public void entityRemoved(Entity removedEntity) {
        if (removedEntity instanceof ScientificStudy ss) {
            // The Study may not be visible but remove it just in case
            removeItem(ss);
        }
    }

    /**
     * Remove listener on Scientific Study Manager when destroying the model.
     */
    @Override
    public void destroy() {
        mgr.removeListener(this);
        super.destroy();
    }
}

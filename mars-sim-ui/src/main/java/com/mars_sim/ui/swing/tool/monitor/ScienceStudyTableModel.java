/*
 * Mars Simulation Project
 * ScienceStudyTableModel.java
 * @date 2025-12-14
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.Entity;
import com.mars_sim.core.EntityManagerListener;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.ScientificStudyManager;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.utils.model.BaseScienceStudyModel;

/**
 * Table model for Scientific Studies.
 */
public class ScienceStudyTableModel extends BaseScienceStudyModel
       implements EntityManagerListener, MonitorModel {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    private static final String STUDIES = Msg.getString("scientificstudy.plural");

    private Set<Settlement> settlements = Collections.emptySet();
    private ScientificStudyManager mgr;

    /**
     * Constructor.
     * 
     * @param mgr the Scientific Study Manager
     */
    public ScienceStudyTableModel(ScientificStudyManager mgr) {
        super(NAME, SETTLEMENT, PHASE, SCIENCE, LEVEL, COMPLETED);
        this.mgr = mgr;
        mgr.addListener(this);
    }

    /**
     * Set the settlement filter for the model. This will select the Scientific Studies associated with the selected settlements.
     * @param selectedSettlement Selected settlements to filter by.
     * @return true if the filter was applied, false otherwise.
     */
    @Override
    public boolean setSettlementFilter(Set<Settlement> selectedSettlement) {
        List<ScientificStudy> studies = new ArrayList<>();
        for (Settlement settlement : selectedSettlement) {
            studies.addAll(mgr.getAllStudies(settlement));
        }

		setEntities(studies);
        settlements = selectedSettlement;
		return true;
    }

    @Override
    public void entityAdded(Entity newEntity) {
        if (newEntity instanceof ScientificStudy ss
                && settlements.contains(ss.getPrimarySettlement())) {
            addEntity(ss);
        } 
    }

    @Override
    public void entityRemoved(Entity removedEntity) {
        if (removedEntity instanceof ScientificStudy ss) {
            // The Study may not be visible but remove it just in case
            removeEntity(ss);
        }
    }

    /**
     * Remove listener on Scientific Study Manager when releasing the model.
     */
    @Override
    public void release() {
        mgr.removeListener(this);
        super.release();
    }

    @Override
    public String getName() {
        return STUDIES;
    }

    /**
     * Settlement column is the second column in the model.
     */
    @Override
    public int getSettlementColumn() {
        return 1;
    }
}

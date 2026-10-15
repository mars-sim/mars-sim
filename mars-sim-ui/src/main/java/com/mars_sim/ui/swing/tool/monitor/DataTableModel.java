/*
 * Mars Simulation Project
 * DataTableModel.java
 * @date 2026-10-15
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.Entity;
import com.mars_sim.core.EntityManagerListener;
import com.mars_sim.core.data.collection.DataManager;
import com.mars_sim.core.data.collection.FieldDataSet;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.utils.model.BaseDataModel;

/**
 * Table model for handling data.
 */
public class DataTableModel extends BaseDataModel
       implements EntityManagerListener, MonitorModel {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    private static final String DATA = Msg.getString("data.plural");

    private Set<Settlement> settlements = Collections.emptySet();
    private DataManager mgr;

    /**
     * Constructor.
     * 
     * @param mgr the data manager
     */
    public DataTableModel(DataManager mgr) {
    	super(ID, SETTLEMENT, TYPE, TIME, QUALITY, OWNER, WORKTIME, COORD);
        this.mgr = mgr;
        mgr.addListener(this);
    }

	/**
     * Sets the settlement filter for the model. This will select a list of FieldDataSet associated with the selected settlements.
     * 
     * @param selectedSettlement Selected settlements to filter by.
     * @return true if the filter was applied, false otherwise.
     */
    @Override
    public boolean setSettlementFilter(Set<Settlement> selectedSettlement) {
        List<FieldDataSet> set = new ArrayList<>();
        if (!selectedSettlement.isEmpty() && !mgr.getSettlementSet().isEmpty()) {
	        for (Settlement settlement : selectedSettlement) {
	        	if (mgr.getSettlementSet().contains(settlement)) {
	        		set.addAll(mgr.getDataArchive().get(settlement));
	        	}
	        }
        }

		setEntities(set);
        settlements = selectedSettlement;
		return true;
    }

    @Override
    public void entityAdded(Entity newEntity) {
        if (newEntity instanceof FieldDataSet d
                && settlements.contains(d.getOwningSettlement())) {
            addEntity(d);
        } 
    }

    @Override
    public void entityRemoved(Entity removedEntity) {
        if (removedEntity instanceof FieldDataSet d) {
            removeEntity(d);
        }
    }

    /**
     * Removes listener on the manager when releasing the model.
     */
    @Override
    public void release() {
        mgr.removeListener(this);
        super.release();
    }

    @Override
    public String getName() {
        return DATA;
    }

    /**
     * Gets the settlement column. Here it is the second column of the model.
     */
    @Override
    public int getSettlementColumn() {
        return 1;
    }
}

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

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.data.collection.DataManager;
import com.mars_sim.core.data.collection.FieldDataSet;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.utils.model.BaseDataModel;

/**
 * Table model for handling data.
 */
public class DataTableModel extends BaseDataModel
       implements MonitorModel {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    private static final String DATA = Msg.getString("data.plural");

    private Set<Settlement> settlements = Collections.emptySet();

    /**
     * Constructor.
     * 
     */
    public DataTableModel() {
    	super(ID, SETTLEMENT, TYPE, TIME, QUALITY, OWNER, WORKTIME, COORD);
    }

	/**
     * Sets the settlement filter for the model. This will select a list of FieldDataSet associated with the selected settlements.
     * 
     * @param selectedSettlement Selected settlements to filter by.
     * @return true if the filter was applied, false otherwise.
     */
    @Override
    public boolean setSettlementFilter(Set<Settlement> selectedSettlement) {
        settlements.forEach(s -> s.removeEntityListener(this));

        List<FieldDataSet> set = new ArrayList<>();
        selectedSettlement.forEach(s -> {
            set.addAll(s.getFieldDataManager().getDataArchive());
        });

		setEntities(set);
        settlements = selectedSettlement;
        settlements.forEach(s -> s.addEntityListener(this));

		return true;
    }

    
    /**
     * Handle entity updates for a Settlement.
     * This is used to update the model when a settlement is added or removed from the filter.
     * If Event source is not a Settlement, then the event is passed to the super class for handling.
     * @param event The entity event.
     */
    @Override
    public void entityUpdate(EntityEvent event) {
        if (event.getSource() instanceof Settlement settlement) {
            // Events from Settlemetns go no further
            if ((settlements.contains(settlement))
                && (event.getType().equals(DataManager.FIELDDATA_ADDED))) {
                addEntity((FieldDataSet) event.getTarget());
            }
        }
        else {
            super.entityUpdate(event);
        }
    }

    /**
     * Removes listener on the manager when releasing the model.
     */
    @Override
    public void release() {
        settlements.forEach(s -> s.removeEntityListener(this));
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

/*
 * Mars Simulation Project
 * CategoryTableModel.java
 * @date 2023-12-27
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.UnitListener;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.ui.swing.utils.ColumnSpec;

/**
 * A table model that displays a list of categories in a table. The categories are
 * entities that are independent of a Settlement. This model creates one row per Category per Settlement.
 * The row has a unique key as CategoryKey.
 */
@SuppressWarnings("serial")
public abstract class CategoryTableModel<T> extends EntityTableModel<CategoryKey<T>>
            implements UnitListener {

    private Set<Settlement> selectedSettlements = Collections.emptySet();
	private boolean monitorSettlement = false;
    private List<T> categories;

    protected CategoryTableModel(String name, String countingMsgKey, ColumnSpec[] names, List<T> cats) {
        super(name, countingMsgKey, names);
        this.categories = cats;
    }

        
	/**
	 * Sets whether the changes to the Entities should be monitor for change. 
	 * Sets up the Unitlisteners for the selected Settlement where Food comes 
	 * from for the table.
	 * 
	 * @param activate 
	 */
    public void setMonitorEntites(boolean activate) {
		if (activate != monitorSettlement) {
			if (activate) {
				selectedSettlements.forEach(s ->s.addUnitListener(this));
			}
			else {
				selectedSettlements.forEach(s ->s.removeUnitListener(this));
			}
			monitorSettlement = activate;
		}
	}

	/**
	 * Prepares the model for deletion.
	 */
	@Override
	public void destroy() {
		selectedSettlements.forEach(s ->s.removeUnitListener(this));
		super.destroy();
	}

	/**
	 * Sets the Settlement filter.
	 * 
	 * @param filter Settlement
	 */
    public boolean setSettlementFilter(Set<Settlement> filter) {
		selectedSettlements.forEach(s ->s.removeUnitListener(this));

		// Initialize settlements.
		selectedSettlements = filter;	

        // Create a new row key for each combination of Settlement and Catogory
		Collection<CategoryKey<T>> newRows = new ArrayList<>();
        for(var s : filter) {
            newRows.addAll(categories.stream()
            		.map(c -> new CategoryKey<>(s, c))
            		.sorted(Comparator.comparing(CategoryKey::getName))
            		.toList());
        }

        // Initialize goods list.
		resetEntities(newRows);
			
		// Add table as listener to each settlement.
		if (monitorSettlement) {
			selectedSettlements.forEach(s ->s.addUnitListener(this));
		}

		return true;
    }
}

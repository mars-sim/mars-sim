/*
 * Mars Simulation Project
 * FilteredTableModel.java
 * @date 2025-12-13
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.util.List;

/**
 * Represents a table model that can be filtered
 */
interface FilteredTableModel {
    /**
     * Filter definition
     */
    record Filter(String id, String name, boolean isActive) {}

    /**
     * Get a list of the supported filters and their active state.
     * @return
     */
    List<Filter> getActiveFilters();

    /**
     * Enable/disable a filter.
     * @param id The filter id being changed
     * @param selected
     */
    void setFilter(String id, boolean selected);

}

/*
 * Mars Simulation Project
 * FilteredTableModel.java
 * @date 2025-12-13
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.util.List;
import java.util.function.Consumer;

/**
 * Represents a table model that can be filtered
 */
interface FilteredTableModel {
    /**
     * Filter definition
     */
    record Filter(String name, boolean isActive, Consumer<Boolean> updater) {
    }

    /**
     * Gets a list of the supported filters and their active state.
     * 
     * @return
     */
    List<Filter> getActiveFilters();
}

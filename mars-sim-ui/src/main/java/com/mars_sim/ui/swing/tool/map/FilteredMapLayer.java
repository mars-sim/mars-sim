/*
 * Mars Simulation Project
 * FilteredMapLayer.java
 * @date 2025-06-02
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.map;

import java.util.List;
import java.util.Set;

import javax.swing.Icon;

/**
 * This is a specialised Map Layer that supports filtering
 */
public interface FilteredMapLayer extends MapLayer {
    // Details of a MapFilter
    public record MapFilter(String name, boolean enabled, Icon symbol) {}
    
    /**
     * Get the details of all supported filters
     * @return
     */
    public List<MapFilter> getFilterDetails();

    /**
     * Set a filter to be displayed or not
     * @param name
     * @param display
     */
    public void displayFilter(String name, boolean display);

    /**
     * Get the names of the active filters.
     * @return
     */
    public Set<String> getActiveFilters();

}

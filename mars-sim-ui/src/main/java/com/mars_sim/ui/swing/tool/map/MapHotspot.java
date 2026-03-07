/*
 * Mars Simulation Project
 * MapHotspot.java
 * @date 2024-10-13
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.map;

import com.mars_sim.core.map.location.IntPoint;

/**
 * Represents a hotspot on the map.
 */
public abstract class MapHotspot {
    private IntPoint center;
    private int radius;

    /**
     * Create a circular map hotspot that has a cener and a radius of focus
     */
    protected MapHotspot(IntPoint center, int radius) {
        this.center = center;
        this.radius = radius;
    }
    
    /**
     * Is the position on the Map within this hotspot?.
     * Checks the point is within the radius of the hotspot center
     * @param target Point to check
     * @return
     */
    public boolean isWithin(IntPoint target) {
        return center.getDistance(target) <= radius;
    }

    /**
     * The user has clicked on the hotspot within the map.
     * The default implementation does nothing but subclasses can override
     * to provide the required logic.
     */
    public void clicked() {
        // No nothing by default
    }

    /**
     * Get the tooltip text for this hotspot. 
     * The method should e overriden by subclasses as the default implementaton return a null.
     * @return Return a text or a null if no tooltip is required
     */
    public String getTooltipText() {
        // This should be overriden by subclasses
        return null;
    }

    @Override
    public String toString() {
        return center.toString();
    }
}

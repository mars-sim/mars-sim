package com.mars_sim.ui.swing.tool.map;

import com.mars_sim.core.map.location.IntPoint;

/**
 * Represents a hotspot on the map.
 */
public abstract class MapHotspot {
    private IntPoint center;
    private int radius;

    protected MapHotspot(IntPoint center, int radius) {
        this.center = center;
        this.radius = radius;
    }
    
    /**
     * Is the position on the Map within this hotspot?.
     * Checks the point is within the radius of the hotspot center
     * @param x
     * @param y
     * @return
     */
    public boolean isWithin(int x, int y) {
        int xDiff = center.getiX() - x;
        int yDiff = center.getiY() - y;
        double dist = Math.sqrt((double)(xDiff * xDiff) + (yDiff * yDiff));
        return dist <= radius;
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

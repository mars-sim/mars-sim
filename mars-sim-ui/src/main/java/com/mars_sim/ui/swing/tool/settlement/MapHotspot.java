/*
 * Mars Simulation Project
 * MapHotspot.java
 * @date 2026-09-05
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.settlement;

import java.util.Collections;
import java.util.List;

import com.mars_sim.core.Entity;
import com.mars_sim.core.map.location.LocalBoundedObject;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.ui.swing.tool.settlement.UnitInfoPanel.UnitSummary;

/**
 * Represents a hotspot on the settlement map that is associated with a specific target entity.
 * This class provides methods to determine if a point is within the bounds of the target entity, retrieve descriptions and summaries for display, and manage actions that can be applied to the target entity.
 * @param <T> Type held.
 */
abstract class MapHotspot<T extends Entity> {
    protected final T target;

    protected MapHotspot(T target) {
        this.target = target;
    }

    abstract boolean isSelected(LocalPosition point);

    /**
	 * Is a position within the bounds of an Object ?
	 * This should be in a common class.
	 *
	 * @param pos the mouse pointer position under settlement positioning system
	 * @param obj Object to check against
	 * @return Is within
	 */
    protected static boolean isWithin(LocalPosition pos, LocalBoundedObject obj) {
        double objectWidth = obj.getWidth();
        double objectLength = obj.getLength();
        int facing = (int) obj.getFacing();
        double objectX = obj.getPosition().getX();
        double objectY = obj.getPosition().getY();
        double halfWidth = 0;
        double halfLength = 0;

        if (facing == 0) {
            halfWidth = objectWidth / 2D;
            halfLength = objectLength / 2D;
        } else if (facing == 90) {
            halfLength = objectWidth / 2D;
            halfWidth = objectLength / 2D;
        }

        if (facing == 180 || facing == -180) {
            halfWidth = objectWidth / 2D;
            halfLength = objectLength / 2D;
        } else if (facing == 270 || facing == -90) {
            halfLength = objectWidth / 2D;
            halfWidth = objectLength / 2D;
        }
        else if (facing == 45) {
            halfLength = objectWidth / 2D;
            halfWidth = objectLength / 2D;
        } else if (facing == 135) {
            halfLength = objectWidth / 2D;
            halfWidth = objectLength / 2D;
        }

        double rangeX = Math.round((pos.getX() - objectX) * 100.0) / 100.0;
        double rangeY = Math.round((pos.getY() - objectY) * 100.0) / 100.0;

        return Math.abs(rangeX) <= Math.abs(halfWidth) && Math.abs(rangeY) <= Math.abs(halfLength);
    }

    /**
     * Get a HTML description of the target for display in a tooltip.
     * Subclasses should override this method to provide a description of the target.
     * @return By default this is null.
     */
    String getDescription() {
        return null;
    }

    /**
     * Get the key summary details of the target for display.
     * By default this method returns null as there is no summary for the target.
     * @return Null by default.
     */
    UnitSummary getSummary() {
        return null;
    }

    /**
     * Get the list of actions available for the target.
     * @return List of actino keys available for the target. By default this is an empty list.
     */
    List<String> getActions() {
        return Collections.emptyList();
    }

    /**
     * Apply the action to the target.
     * @param action Action command key.
     */
    void applyAction(String action) {
        // Do nothing by default
    }
}

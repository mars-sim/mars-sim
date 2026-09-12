/*
 * Mars Simulation Project
 * MapViewPoint.java
 * @date 2023-12-16
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.settlement;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import com.mars_sim.core.map.location.LocalPosition;

/**
 * Represents the map view point and can be used for rendering.
 */
record MapViewPoint(Graphics2D graphics, LocalPosition center,
		int mapWidth, int mapHeight, double rotation, float scale, 
        double mapRadius) {

    /**
     * This prepares the graphics for rendering in the map by translating and
     * rotating the graphics to the correct position.
     * 
     * @return Saves the original graphics transform so it can be restored later.
     */
    AffineTransform prepareGraphics() {
        
        // Save original graphics transforms.
        AffineTransform saveTransform = graphics.getTransform();

        double xPos = center.getX();
        double yPos = center.getY();

        // Translate map from settlement center point.
        graphics.translate(mapWidth / 2D + (xPos * scale), mapHeight / 2D + (yPos * scale));

        // Rotate map from North.
        graphics.rotate(rotation, 0D - (xPos * scale), 0D - (yPos * scale));

        return saveTransform;
    }

    /** 
     * Is a LocalPosition within the map view point?
     * @param pos the LocalPosition to check.
     * @return true if the LocalPosition is within the map view point, false otherwise.
     */
    boolean isVisible(LocalPosition pos) {
        return (pos.getDistanceTo(center) < mapRadius);
    }
}
/*
 * Mars Simulation Project
 * MapViewPoint.java
 * @date 2023-12-16
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.settlement;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

/**
 * Represents the map view point and can be used for rendering.
 */
record MapViewPoint(Graphics2D graphics, double xPos, double yPos,
		int mapWidth, int mapHeight, double rotation, float scale, float scaleMod) {

    /**
     * This prepares the graphics for rendering in the map by translating and
     * rotating the graphics to the correct position.
     * 
     * @return
     */
    AffineTransform prepareGraphics() {
        
        // Save original graphics transforms.
        AffineTransform saveTransform = graphics.getTransform();

        // Translate map from settlement center point.
        graphics.translate(mapWidth / 2D + (xPos * scale), mapHeight / 2D + (yPos * scale));

        // Rotate map from North.
        graphics.rotate(rotation, 0D - (xPos * scale), 0D - (yPos * scale));

        return saveTransform;
    }
}
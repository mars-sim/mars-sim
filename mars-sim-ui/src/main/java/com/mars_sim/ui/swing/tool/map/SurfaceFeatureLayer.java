/*
 * Mars Simulation Project
 * SurfaceFeatureLayer.java
 * @date 2024-10-11
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.map;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.IntPoint;
import com.mars_sim.core.map.location.SurfacePOI;
import com.mars_sim.core.tool.AverageTimer;

/**
 * This is a map layer for the MapPanel that renders surface feature defed as SurfacePOIs
 * to the map taking into account the viewpoint on the map.
 */
public abstract class SurfaceFeatureLayer<T extends SurfacePOI> implements MapLayer {

    private AverageTimer timer;

    protected SurfaceFeatureLayer(String name) {
        timer = new AverageTimer(name, -1);  //Disabled
    }

    /**
     * Return a list of features that are within the focus specified
     * @param center Center of the viewpoint
     * @param arcAngle Angle of the viewpoint
     * @return
     */
    protected abstract List<T> getFeatures(Coordinates center, double arcAngle);

    /**
	 * Displays the layer on the map image.
	 *
	 * @param mapCenter the location of the center of the map.
	 * @param baseMap   the type of map.
	 * @param g2d         graphics context of the map display.
     * @return Any hotspots this layer creates.
	 */
	@Override
	public List<MapHotspot> displayLayer(Coordinates mapCenter, MapDisplay baseMap, Graphics2D g2d, Dimension d) {
        List<MapHotspot> hotspots = new ArrayList<>();

        timer.startTimer();

        prepareGraphics(g2d);
        
        List<T> features = getFeatures(mapCenter, baseMap.getHalfAngle() * 1.1);

        boolean isColourful = baseMap.getMapMetaData().isColourful();
		for (var f : features) {
            // Determine display location of feature on the map.
            IntPoint pointOnMap = MapUtils.getRectPosition(f.getCoordinates(), mapCenter, baseMap, d);
            if ((pointOnMap.getiX() >= 0) && (pointOnMap.getiY() >= 0)) {
                var h = displayFeature(f, pointOnMap, g2d, isColourful);
                if (h != null) {
                    hotspots.add(h);
                }
            }
		}

        timer.stopTimer();

        return hotspots;
	}
        
    /**
     * Display a feature on the map using a Graphic at a particular point.
     * @param f Feature to display
     * @param location Locatino on the Graphic
     * @param g Graphic for drawing
     * @param isColourful Is the destination a colourful map
     * @return Does this feature create a hotspot?
     */
    protected abstract MapHotspot displayFeature(T f, IntPoint location, Graphics2D g, boolean isColourful);

    /**
     * Setup the graphic context to draw this layer. By default there is no change.
     * @param g2d
     */
    protected void prepareGraphics(Graphics2D g2d) {
        // By default no preparation is needed
    }
}

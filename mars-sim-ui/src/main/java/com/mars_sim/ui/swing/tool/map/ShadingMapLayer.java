/*
 * Mars Simulation Project
 * ShadingMapLayer.java
 * @date 2022-08-02
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.tool.map;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Collections;
import java.util.List;

import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.map.location.Coordinates;

/**
 * The ShadingMapLayer is a graphics layer to display twilight and night time
 * shading.
 */
public class ShadingMapLayer implements MapLayer {
	
    private static final int LIGHT_THRESHOLD = 196;
 
	private SurfaceFeatures surface;

	/**
	 * Constructor
	 * 
	 * @param displayComponent the display component.
	 */
	public ShadingMapLayer(MapPanel displayComponent) {
		surface = displayComponent.getDesktop().getSimulation().getSurfaceFeatures();
	}

	/**
	 * Displays the layer on the map image.
	 * 
	 * @param mapCenter the location of the center of the map.
	 * @param baseMap   the type of map.
	 * @param g2d         graphics context of the map display.
	 */
	@Override
	public List<MapHotspot> displayLayer(Coordinates mapCenter, MapDisplay baseMap, Graphics2D g2d, Dimension d) {

		// Need to determine which side of Mars is facing the sun
		
        // sunlight normalized between 0 and 1 
        double sunlight = surface.getSunlightRatio(mapCenter);
        int sunlightInt = (int) (LIGHT_THRESHOLD * sunlight);

        if (sunlight < 0.85) {	        
        	int opacity = LIGHT_THRESHOLD - sunlightInt;
            g2d.setColor(new Color(5, 0, 0, opacity));
            g2d.fillRect(0, 0, (int)d.getWidth(), (int)d.getHeight());
		}

		return Collections.emptyList();
	}
}

/*
 * Mars Simulation Project
 * ShadingMapLayer.java
 * @date 2022-08-02
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.map;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import org.mars.sim.mapdata.location.Coordinates;
import org.mars.sim.mapdata.map.Map;
import org.mars.sim.mapdata.map.MapLayer;
import org.mars_sim.msp.core.tool.SimulationConstants;

/**
 * The ShadingMapLayer is a graphics layer to display twilight and night time
 * shading.
 */
public class ShadingMapLayer implements MapLayer, SimulationConstants {
	
    private static final int LIGHT_THRESHOLD = 196;
 
	/**
	 * Constructor
	 * 
	 * @param displayComponent the display component.
	 */
	public ShadingMapLayer(Component displayComponent) {
		// nothing
	}

	/**
	 * Displays the layer on the map image.
	 * 
	 * @param mapCenter the location of the center of the map.
	 * @param baseMap   the type of map.
	 * @param g         graphics context of the map display.
	 */
	@Override
	public void displayLayer(Coordinates mapCenter, Map baseMap, Graphics g) {
		Graphics2D g2d = (Graphics2D) g;

		// Need to determine which side of Mars is facing the sun
		
        // sunlight normalized between 0 and 1 
        double sunlight = surfaceFeatures.getSunlightRatio(mapCenter);
        int sunlightInt = (int) (LIGHT_THRESHOLD * sunlight);

        if (sunlight < 0.85) {	        
        	int opacity = LIGHT_THRESHOLD - sunlightInt;
            g2d.setColor(new Color(5, 0, 0, opacity)); //(0, 0, 0, 196));
            g2d.fillRect(0, 0, Map.MAP_BOX_WIDTH, Map.MAP_BOX_HEIGHT);
		}
	}
}

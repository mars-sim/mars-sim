/*
 * Mars Simulation Project
 * MapLayer.java
 * @date 2022-08-02
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.tool.map;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.List;

import com.mars_sim.core.map.location.Coordinates;


/**
 * The MapLayer interface is a graphics layer painted on the map display.
 */
public interface MapLayer {
	/**
	 * Displays the layer on the map image.
	 * 
	 * @param mapCenter the location of the center of the map.
	 * @param baseMap   the base map controlling coordinate frame
	 * @param g         graphics context of the map display.
	 * @param d			Size of region being drawn
	 */
	public List<MapHotspot> displayLayer(Coordinates mapCenter, MapDisplay baseMap, Graphics2D g, Dimension d);
}

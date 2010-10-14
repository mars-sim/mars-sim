/**
 * Mars Simulation Project
 * MapLayer.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.map;

import java.awt.Graphics;

import org.mars_sim.msp.core.Coordinates;

/**
 * The MapLayer interface is a graphics layer painted on the map display.
 */
public interface MapLayer {
	public final int MAP_X_OFFSET = 300;
	public final int MAP_Y_OFFSET = 300;
	/**
     * Displays the layer on the map image.
     * @param mapCenter the location of the center of the map.
     * @param mapType the type of map.
     * @param g graphics context of the map display.
     */
    public void displayLayer(Coordinates mapCenter, String mapType, Graphics g) ;
}
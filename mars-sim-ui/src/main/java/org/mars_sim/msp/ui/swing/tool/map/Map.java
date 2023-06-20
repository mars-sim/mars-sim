/*
 * Mars Simulation Project
 * Map.java
 * @date 2022-08-02
 * @author Greg Whelan
 */

package org.mars_sim.msp.ui.swing.tool.map;

import java.awt.Image;

import org.mars_sim.mapdata.MapMetaData;
import org.mars_sim.msp.core.Coordinates;

/**
 * The Map interface represents a map usable by the MapDisplay class.
 */
public interface Map {

	/** The display box map height (for scrolling) */
	public static final int DISPLAY_HEIGHT = MapPanel.MAP_BOX_HEIGHT;
	/** The display box map width (for scrolling) */
	public static final int DISPLAY_WIDTH = MapPanel.MAP_BOX_WIDTH;
	/** Map display width in pixels. */
	public static final int MAP_VIS_WIDTH = DISPLAY_WIDTH;
	/** Map display height in pixels. */
	public static final int MAP_VIS_HEIGHT = DISPLAY_HEIGHT;
	/** Half of the display box map height. */
	public static final int HALF_MAP_BOX = (int) (0.5 * DISPLAY_HEIGHT);
	/** this is a mysterious variable. */
	public static final double HALF_MAP_ANGLE = 0.48587;

	public static final double QUARTER_HALF_MAP_ANGLE = HALF_MAP_ANGLE / 4;
	
	public static final double TWO_PI = Math.PI * 2D;
	
	/**
	 * Creates a 2D map at a given center point.
	 * 
	 * @param newCenter 	The new center location
	 * @param scale 		The new map scale
	 * @throws Exception if error in drawing map.
	 */
	public void drawMap(Coordinates newCenter, double scale);

	/**
	 * Checks if a requested map is complete.
	 * 
	 * @return true if requested map is complete
	 */
	public boolean isImageDone();

	/**
	 * Gets the constructed map image.
	 * 
	 * @return constructed map image
	 */
	public Image getMapImage();

	/**
	 * Gets the scale of the Mars surface map (height pixels divided by pi).
	 * 
	 * @return
	 */
	public double getScale();

	/**
	 * Gets the scale of the Mars surface map.
	 * 
	 * @param value
	 */
 	public void setMapScale(double value);
 	
	/**
	 * Gets the name type of this map.
	 * 
	 * @return
	 */
	public MapMetaData getType();

	/**
	 * Gets the height of this map in pixels.
	 * 
	 * @return
	 */
    public int getPixelHeight();

	/**
	 * Gets the width of this map in pixels.
	 * 
	 * @return
	 */
    public int getPixelWidth();
}

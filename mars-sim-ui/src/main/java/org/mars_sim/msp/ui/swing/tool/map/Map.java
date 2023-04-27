/*
 * Mars Simulation Project
 * Map.java
 * @date 2022-08-02
 * @author Greg Whelan
 */

package org.mars_sim.msp.ui.swing.tool.map;

import java.awt.Image;

import org.mars_sim.mapdata.MapDataUtil;
import org.mars_sim.msp.core.Coordinates;

/**
 * The Map interface represents a map usable by the MapDisplay class.
 */
public interface Map {

	/** The display box map height (for scrolling) */
	public static final int DISPLAY_HEIGHT = MapDataUtil.GLOBE_BOX_HEIGHT;
	/** The display box map width (for scrolling) */
	public static final int DISPLAY_WIDTH = MapDataUtil.GLOBE_BOX_WIDTH;
	/** Map display width in pixels. */
	public static final int MAP_VIS_WIDTH = DISPLAY_WIDTH;
	/** Map display height in pixels. */
	public static final int MAP_VIS_HEIGHT = DISPLAY_HEIGHT;
	/** this is a mysterious variable. */
	public static final double HALF_MAP_ANGLE = 0.48587;

	public static final double QUARTER_HALF_MAP_ANGLE = HALF_MAP_ANGLE/4;
	
	public static final double PIXEL_RHO = MapDataUtil.MAP_HEIGHT / Math.PI;
	
	public static final double TWO_PI = Math.PI * 2D;
	
	/** how far off center in the surface map are things placed */
//	public static final int SCREEN_OFFSET_X=300;
	/** how far off center in the surface map are things placed */
//	public static final int SCREEN_OFFSET_Y=300;

	/**
	 * Creates a 2D map at a given center point.
	 * 
	 * @param newCenter the new center location
	 * @throws Exception if error in drawing map.
	 */
	public void drawMap(Coordinates newCenter);

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
}

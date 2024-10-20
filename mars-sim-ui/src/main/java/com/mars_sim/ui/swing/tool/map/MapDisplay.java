/*
 * Mars Simulation Project
 * MapDisplay.java
 * @date 2024-09-30
 * @author Greg Whelan
 */

package com.mars_sim.ui.swing.tool.map;

import java.awt.Image;

import com.mars_sim.core.data.Range;
import com.mars_sim.core.map.MapMetaData;
import com.mars_sim.core.map.location.Coordinates;

/**
 * The MapData that can be displayed represents a map usable by the CannedMarsMap.
 */
public interface MapDisplay {

	/** The display box map dimensions (for scrolling) */
	public static final int MAP_BOX_HEIGHT = 512;
	public static final int MAP_BOX_WIDTH = MAP_BOX_HEIGHT;
	public static final int HALF_MAP_BOX = (int) (0.5 * MAP_BOX_HEIGHT);

	public static final double HALF_MAP_ANGLE = 0.48587;
	public static final double QUARTER_HALF_MAP_ANGLE = HALF_MAP_ANGLE / 4;

	public static final double TWO_PI = Math.PI * 2D;
	
	/**
	 * Creates a 2D map at a given center point.
	 * 
	 * @param newCenter 	The new center location
	 * @param rho 		The new map rho
	 * @throws Exception if error in drawing map.
	 */
	public void drawMap(Coordinates newCenter, double rho);

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
	 * Gets the rho of the Mars surface map (height pixels divided by pi).
	 * Dervied from last map image cretion
	 * 
	 * @return
	 */
	public double getRho();

	/**
     * Gets the magnification of the Mars surface map.
     * 
     * @return
     */
    public double getScale();
    
	/**
     * Gets the half angle of the Mars surface map.
     * 
     * @return
     */
    public double getHalfAngle();
    
	/**
	 * Gets the name type of this map.
	 * 
	 * @return
	 */
	public MapMetaData getMapMetaData();

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

    public int getResolution();

    public Range getRhoRange();

    public double getRhoDefault();
}

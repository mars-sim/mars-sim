/*
 * Mars Simulation Project
 * MapDisplay.java
 * @date 2024-09-30
 * @author Greg Whelan
 */

package com.mars_sim.ui.swing.tool.map;

import java.awt.Dimension;
import java.awt.Image;

import com.mars_sim.core.data.Range;
import com.mars_sim.core.map.MapMetaData;
import com.mars_sim.core.map.location.Coordinates;

/**
 * The MapData that can be displayed represents a map usable by the CannedMarsMap.
 */
public interface MapDisplay {

	public static final double HALF_MAP_ANGLE = 0.48587;
	public static final double QUARTER_HALF_MAP_ANGLE = HALF_MAP_ANGLE / 4;

	public static final double TWO_PI = Math.PI * 2D;
	
	/**
	 * Creates a 2D map at a given center point.
	 * 
	 * @param newCenter 	The new center location
	 * @param rho 		The new map rho
	 * @param d			Size of the map to draw
	 */
	public void drawMap(Coordinates newCenter, double rho, Dimension d);

	/**
	 * Gets an image for this details.
	 * 
	 * @param newCenter 	The new center location
	 * @param rho 		The new map rho
	 * @param d			Size of the map to draw
	 */
	public Image getMapImage(Coordinates newCenter, double rho, Dimension d);

	/**
	 * Gets the rho of the Mars surface map (height pixels divided by pi).
	 * Derived from last map image creation.
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

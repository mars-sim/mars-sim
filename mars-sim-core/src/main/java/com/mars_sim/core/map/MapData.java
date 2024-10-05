/*
 * Mars Simulation Project
 * MapData.java
 * @date 2024-09-30
 * @author Scott Davis
 */

package com.mars_sim.core.map;

import java.awt.Image;

import com.mars_sim.core.data.Range;
import com.mars_sim.core.map.location.Coordinates;

 /**
  * An interface for map data.
  */
 public interface MapData {
   // Represents the state of whether the data has loaded
   enum MapState { PENDING, LOADED, FAILED}

	/**
	 * Generates and returns a map image with the given parameters.
	 * 
	 * @param center The center location of the map
	 * @param mapBoxWidth 	The width of the map box
	 * @param mapBoxHieght 	The height of the map box
	 * @param rho 		The map scale
	 * @return Image		The map image
	 */
	public Image createMapImage(Coordinates center, int mapBoxWidth, int mapBoxHeight, double rho);
    
   /**
    * Gets the number of pixels height.
    * 
    * @return
    */
   public int getHeight();

   /**
    * Gets the number of pixels width.
    * 
    * @return
    */
   public int getWidth();
 
   /**
    * Gets the Meta data of the map.
    * 
    * @return
    */
   public MapMetaData getMetaData();

   /**
	 * Get the resolution layer of this data in the parent Map Meta Data stack.
	 */
   public int getResolution();
 	
   /**
	 * Get the min and max value of rho supported by this map data.
	 */
	public Range getRhoRange();
	
 	/**
 	 * Gets the RGB map color as an integer at a given location.
 	 * 
 	 * @param phi   the phi location.
 	 * @param theta the theta location.
 	 * @return the RGB map color.
 	 */
 	public int getRGBColorInt(double phi, double theta);

   /**
    * Is this map data ready to be used? The data may require loading in the background ?
    * 
    * @return
    */
	public MapState getStatus();

  public double getRhoDefault();

  /**
   * Get the point on the last map image created.
   * @param index
   * @return
   */
	public MapPoint getMapBoxPoint(int index);

 }

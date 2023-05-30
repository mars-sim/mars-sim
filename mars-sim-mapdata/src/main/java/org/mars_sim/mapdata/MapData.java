/*
 * Mars Simulation Project
 * MapData.java
 * @date 2023-05-04
 * @author Scott Davis
 */

 package org.mars_sim.mapdata;

 import java.awt.Color;
 import java.awt.Image;

 /**
  * An interface for map data.
  */
 public interface MapData {

     /**
      * Generates and returns a map image with the given parameters.
      * 
      * @param centerPhi the phi center location of the map
      * @param centerTheta the theta center location of the map
      * @param mapBoxWidth The width of the map box
	  * @param mapBoxHieght The height of the map box
      * @return The map image.
      */
     public Image getMapImage(double centerPhi, double centerTheta, int mapBoxWidth, int mapBoxHeight);
     
     /**
      * Gets the RGB map color at a given location.
      * 
      * @param phi the phi location.
      * @param theta the theta location.
      * @return the RGB map color.
      */
     public Color getRGBColor(double phi, double theta);

    /**
     * Gets the scale of pixel to Mars surface degree.
     * 
     * @return
     */
    public double getScale();

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
 }

/*
 * Mars Simulation Project
 * MapData.java
 * @date 2022-07-15
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
      * @param centerPhi the phi center location of the map.
      * @param centerTheta the theta center location of the map.
      * @param imageWidth The Width of the requested image
	  * @param imageHieght The Height of the requested image
      * @return The map image.
      */
     public Image getMapImage(double centerPhi, double centerTheta, int imageWidth, int imageHeight);
     
     /**
      * Gets the RGB map color at a given location.
      * 
      * @param phi the phi location.
      * @param theta the theta location.
      * @return the RGB map color.
      */
     public Color getRGBColor(double phi, double theta);

    /**
     * Get the scale of pixel to Mars surface degree
     * @return
     */
    public double getScale();
 }

/*
 * Mars Simulation Project
 * MapData.java
 * @date 2023-05-04
 * @author Scott Davis
 */

 package org.mars_sim.mapdata;

 import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

 /**
  * An interface for map data.
  */
 public interface MapData {

	/**
	 * Generates and returns a map image with the given parameters.
	 * 
	 * @param centerPhi 	The phi center location of the map
	 * @param centerTheta 	The theta center location of the map
	 * @param mapBoxWidth 	The width of the map box
	 * @param mapBoxHieght 	The height of the map box
	 * @param scale 		The map scale
	 * @return Image		The map image
	 */
	public Image getMapImage(double centerPhi, double centerTheta, int mapBoxWidth, int mapBoxHeight, double scale);
	
    /**
     * Gets the scale of the Mars surface map.
     * 
     * @return
     */
    public double getScale();

    /**
     * Gets the magnification of the Mars surface map.
     * 
     * @return
     */
    public double getMagnification();
    
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
     * Gets the unmodified cylindrical map image.
     * 
     * @return
     */
 	public BufferedImage getCylindricalMapImage();
 	
 	
	/**
 	 * Loads the 2-D integer map data set pixel array.
 	 * 
 	 * @param imageName
 	 * @return
 	 * @throws IOException
 	 */
 	public int[][] getPixels();
 	
 }

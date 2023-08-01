/*
 * Mars Simulation Project
 * MapData.java
 * @date 2023-05-04
 * @author Scott Davis
 */

 package org.mars.sim.mapdata;

 import java.awt.Image;
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
     * Gets the rho of the Mars surface map.
     * 
     * @return
     */
    public double getRho();

	/**
	 * sets the rho of the Mars surface map (height pixels divided by pi).
	 * 
	 * @return
	 */
	public void setRho(double rho);
	
	/**
     * Gets the half angle of the Mars surface map.
     * 
     * @return
     */
    public double getHalfAngle();
    
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
 	 * Loads the 2-D integer map data set pixel array.
 	 * 
 	 * @param imageName
 	 * @return
 	 * @throws IOException
 	 */
 	public int[][] getPixels();
 	
	
 	/**
 	 * Gets the RGB map color as an integer at a given location.
 	 * 
 	 * @param phi   the phi location.
 	 * @param theta the theta location.
 	 * @return the RGB map color.
 	 */
 	public int getRGBColorInt(double phi, double theta);

 }

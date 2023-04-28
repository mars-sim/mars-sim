/*
 * Mars Simulation Project
 * IntegerMapData.java
 * @date 2023-04-27
 * @author Scott Davis
 */
 package org.mars_sim.mapdata;

import java.awt.Color;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

 /**
  * A map that uses integer data stored in files to represent colors.
  */
 public class IntegerMapData implements MapData {

 	// Static members.
 	private static Logger logger = Logger.getLogger(IntegerMapData.class.getName());

 	
	/** Note: Make sure this param matches the number of vertical pixels of the surface map. */ 
	public static final int MAP_PIXEL_HEIGHT = 4096; //1024; 1440; 2048; 2880; 4096
	/** Note: Make sure this param matches the number of horizontal pixels of the surface map. */ 
	public static final int MAP_PIXEL_WIDTH = 8192; //2048; 2880; 4096; 5760; 8192
	
 	public static final int GLOBE_BOX_HEIGHT = 300;
 	public static final int GLOBE_BOX_WIDTH = GLOBE_BOX_HEIGHT;
 	public static final int MAP_BOX_HEIGHT = GLOBE_BOX_HEIGHT;
 	public static final int MAP_BOX_WIDTH = GLOBE_BOX_WIDTH;
 	
 	// The diameter of Mars in pixels
	public static final double RHO = MAP_PIXEL_HEIGHT / Math.PI;
	// The half map's height in pixels
 	public static final int HALF_MAP = MAP_PIXEL_HEIGHT / 2;
 	// The lower edge of map in pixels
 	public static final int LOW_EDGE = HALF_MAP - MAP_BOX_HEIGHT / 2;
 
 	private static final double TWO_PI = Math.PI * 2;

 	// The value of PHI_ITERATION_PADDING is derived from testing.
 	private static final double PHI_ITERATION_PADDING = 1.26;
 	// The value of PHI_PADDING is derived from testing.
 	private static final double PHI_PADDING = 1.46; 	 	
 	
 	// The value of THETA_ITERATION_PADDING is derived from testing.
 	private static final double THETA_ITERATION_PADDING = 1.46;
 	
 	// The value of MIN_THETA_PADDING is derived from testing.
 	private static final double MIN_THETA_PADDING = 1.02;

 	// The value of POLAR_CAP_FACTOR is derived from testing.
 	private static final double POLAR_CAP_FACTOR = 6.54;
 	private static final double POLAR_CAP_PI_ANGLE = Math.PI / POLAR_CAP_FACTOR;
 	private static final double POLAR_CAP_RANGE = Math.PI - POLAR_CAP_PI_ANGLE;
 	 	
 	// Data members.
 	private int[][] pixels = null;
	private int width;
	private int height;
	private double rho;
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param mapFileName   the map data file name.
 	 */
 	public IntegerMapData(String mapFileName) {
 	
 		try {
 			// Load data files
 			pixels = loadMapData(mapFileName);

			rho =  height / Math.PI;
			logger.info("Loaded " + mapFileName + " with size " + width + "x" + height);
 			
 		} catch (IOException e) {
 			logger.log(Level.SEVERE, "Could not find the map file.", e);
 		}
 	}

	/**
	 * Get the scale of pixel to Mars surface degree
	 * @return
	 */
	@Override
	public double getScale() {
		return rho;
	}

 	/**
 	 * Loads the whole map data set into an 2-D integer array.
 	 * 
 	 * @param imageURL
 	 * @return
 	 * @throws IOException
 	 */
 	private int[][] loadMapData(String imageURL) throws IOException {

 		URL imageMapURL = IntegerMapData.class.getResource(imageURL);
 		BufferedImage image = ImageIO.read(imageMapURL);

 		final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
 		width = image.getWidth();
 		height = image.getHeight();
 		final boolean hasAlphaChannel = image.getAlphaRaster() != null;

 		int[][] result = new int[height][width];
 		if (hasAlphaChannel) {
 			final int pixelLength = 4;
 			for (int pixel = 0, row = 0, col = 0; pixel + 3 < pixels.length; pixel += pixelLength) {
 				int argb = 0;
 				argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
 				argb += ((int) pixels[pixel + 1] & 0xff); // blue
 				argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
 				argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
 				
// 				The Red and Blue channel comments are flipped. 
// 				Red should be +1 and blue should be +3 (or +0 and +2 respectively in the No Alpha code).
 				
// 				You could also make a final int pixel_offset = hasAlpha?1:0; and 
// 				do ((int) pixels[pixel + pixel_offset + 1] & 0xff); // green; 
// 				and merge the two loops into one. – Tomáš Zato Mar 23 '15 at 23:02
 						
 				result[row][col] = argb;
 				col++;
 				if (col == width) {
 					col = 0;
 					row++;
 				}
 			}
 		} else {
 			final int pixelLength = 3;
 			for (int pixel = 0, row = 0, col = 0; pixel + 2 < pixels.length; pixel += pixelLength) {
 				int argb = 0;
 				argb += -16777216; // 255 alpha
 				argb += ((int) pixels[pixel] & 0xff); // blue
 				argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
 				argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
 				result[row][col] = argb;
 				col++;
 				if (col == width) {
 					col = 0;
 					row++;
 				}
 			}
 		}

 		return result;
 	}
 	
 	/**
 	 * Gets the map image based on the center phi and theta coordinates given.
 	 * 
 	 * @param centerPhi Center phi value on the image
 	 * @param centerTheta
	 * @param imageWidth The Width of the requested image
	 * @param imageHieght The Height of the requested image
 	 */
 	@Override
 	public Image getMapImage(double centerPhi, double centerTheta, int imageWidth, int imageHeight) {

		double phiRange = Math.PI * PHI_PADDING * (1.0 * imageHeight / height);
		double phiIterationAngle = Math.PI / (height * PHI_ITERATION_PADDING);
		double thetaIterationFactor = width * THETA_ITERATION_PADDING;
		double minThetaDisplay = TWO_PI * (1.0 * imageWidth / width) * MIN_THETA_PADDING;

 		// Create a new buffered image to draw the map on.
 		BufferedImage result = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_4BYTE_ABGR);

 		// The map data is PI offset from the center theta.
 		double correctedTheta = centerTheta - Math.PI;
 		while (correctedTheta < 0D)
 			correctedTheta += TWO_PI;
 		while (correctedTheta > TWO_PI)
 			correctedTheta -= TWO_PI;

 		// Create an array of int RGB color values to create the map image from.
 		int[] mapArray = new int[imageWidth * imageHeight];

 		// Determine starting and ending phi values.
 		double startPhi = centerPhi - (phiRange / 2);
 		if (startPhi < 0D)
 			startPhi = 0D;
 		double endPhi = centerPhi + (phiRange / 2);
 		if (endPhi > Math.PI)
 			endPhi = Math.PI;
 		
 		// Loop through each phi value.
 		for (double x = startPhi; x <= endPhi; x += phiIterationAngle) {
 			// Determine theta iteration angle.
 			double thetaIterationAngle = TWO_PI / (thetaIterationFactor * Math.sin(x) + 1);

 			double thetaRange = ((1 - Math.sin(x)) * TWO_PI) + minThetaDisplay;
 			
 			if ((x < POLAR_CAP_PI_ANGLE) || (x > (POLAR_CAP_RANGE)))
 				thetaRange = TWO_PI;
 			if (thetaRange > TWO_PI)
 				thetaRange = TWO_PI;

 			// Determine the theta starting and ending values.
 			double startTheta = centerTheta - (thetaRange / 2);
 			double endTheta = centerTheta + (thetaRange / 2);

 			// Loop through each theta value.
 			for (double y = startTheta; y <= endTheta; y += thetaIterationAngle) {

 				// Correct y value to make sure it is within bounds. (0 to 2PI)
 				double yCorrected = y;
 				while (yCorrected < 0)
 					yCorrected += TWO_PI;
 				while (yCorrected > TWO_PI)
 					yCorrected -= TWO_PI;
 				
 				// Determine the rectangular offset of the pixel in the image.
 				Point location = findRectPosition(centerPhi, centerTheta, x, yCorrected);

 				// Determine the display x and y coordinates for the pixel in the image.
 				int displayX = imageWidth - location.x;
 				int displayY = imageHeight - location.y;

 				// Check that the x and y coordinates are within the display area.
 				boolean leftBounds = displayX >= 0;
 				boolean rightBounds = displayX < imageWidth;
 				boolean topBounds = displayY >= 0;
 				boolean bottomBounds = displayY < imageHeight;
 				if (leftBounds && rightBounds && topBounds && bottomBounds) {

 					// Determine array index for the display location.
 					int index = (imageWidth - displayX) + ((imageHeight - displayY) * imageWidth);

 					// Put color in array at index.
 					if ((index >= 0) && (index < mapArray.length))
 						mapArray[index] = getRGBColorInt(x, yCorrected);
 				}
 			}
 		}

 		// Create new map image.
 		result.setRGB(0, 0, imageWidth, imageHeight, mapArray, 0, imageWidth);

 		return result;
 	}

 	@Override
 	public Color getRGBColor(double phi, double theta) {
 		return new Color(getRGBColorInt(phi, theta));
 	}

 	/**
 	 * Gets the RGB map color as an integer at a given location.
 	 * 
 	 * @param phi   the phi location.
 	 * @param theta the theta location.
 	 * @return the RGB map color as an integer.
 	 */
 	private int getRGBColorInt(double phi, double theta) {
 		// Make sure phi is between 0 and PI.
 		while (phi > Math.PI)
 			phi -= Math.PI;
 		while (phi < 0)
 			phi += Math.PI;

 		// Adjust theta with PI for the map offset.
 		// Note: the center of the map is when theta = 0
 		if (theta > Math.PI)
 			theta -= Math.PI;
 		else
 			theta += Math.PI;

 		// Make sure theta is between 0 and 2 PI.
 		while (theta > TWO_PI)
 			theta -= TWO_PI;
 		while (theta < 0)
 			theta += TWO_PI;

// 		int row = (int) Math.round(phi * (MAP_HEIGHT / Math.PI));
// 		if (row == mapColors.size())
// 			row--;
 //
// 		int[] colorRow = mapColors.get(row);
// 		int column = (int) Math.round(theta * ((double) colorRow.length / TWO_PI));
// 		if (column == colorRow.length)
// 			column--;

 		int row = (int) Math.round(phi * (pixels.length / Math.PI));
 		if (row == pixels.length)
 			row--;

 		int column = (int) Math.round(theta * ((double) pixels[0].length / TWO_PI));
 		if (column == pixels[0].length)
 			column--;
 		
 		return pixels[row][column];
 	}

 	
 	
 	/**
 	 * Converts spherical coordinates to rectangular coordinates. Returns integer x
 	 * and y display coordinates for spherical location.
 	 *
 	 * @param newPhi   the new phi coordinate
 	 * @param newTheta the new theta coordinate
 	 * @return pixel offset value for map
 	 */
 	private Point findRectPosition(double oldPhi, double oldTheta, double newPhi, double newTheta) {

 		final double temp_col = newTheta + ((Math.PI / -2D) - oldTheta);
 		final double temp_buff_x = rho * Math.sin(newPhi);
 		int buff_x = ((int) Math.round(temp_buff_x * Math.cos(temp_col)) + (height/2)) - LOW_EDGE;
 		int buff_y = ((int) Math.round(((temp_buff_x * (0D - Math.cos(oldPhi))) * Math.sin(temp_col))
 				+ (rho * Math.cos(newPhi) * (0D - Math.sin(oldPhi)))) + (height/2)) - LOW_EDGE;
 		return new Point(buff_x, buff_y);
 	}
 }

/**
 * Mars Simulation Project
 * IntegerMapData.java
 * @version 3.1.0 2018-10-04
 * @author Scott Davis
 */

package org.mars_sim.mapdata;

import java.awt.Color;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

/**
 * A map that uses integer data stored in files to represent colors.
 */
abstract class IntegerMapData implements MapData {

	// Static members.
	private static Logger logger = Logger.getLogger(IntegerMapData.class.getName());
	
	public static int IMAGE_WIDTH = MapDataUtil.IMAGE_WIDTH;
	public static int IMAGE_HEIGHT = MapDataUtil.IMAGE_HEIGHT;
	
	public static final int MAP_HEIGHT = 1440;	// 2048; //1024; Source map height in pixels.
	public static final int MAP_WIDTH = 2880;	// 4096; //2048; Source map width in pixels.
	
	public static final double MAP_RATIO = 1;
	
	public static final double HALF_MAP_ANGLE = .48587D;
	public static final double PIXEL_RHO = (double) MAP_HEIGHT / Math.PI;
	private static final double TWO_PI = Math.PI * 2D;

	// Data members.
	private int[][] pixels = null;
	
	/**
	 * Constructor
	 * 
	 * @param mapFileName   the map data file name.
	 */
	public IntegerMapData(String mapFileName) {
	
		try {
			// Load data files
			pixels = loadMapData(mapFileName);
			
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Could not find the map file.", e);
		}
	}

	private int[][] loadMapData(String imageURL) throws IOException {

		URL imageMapURL = IntegerMapData.class.getResource(imageURL);
		BufferedImage image = ImageIO.read(imageMapURL);
		
//		reproduceImage(image);

//		logger.log(Level.CONFIG, "Type : " + image.getType()); // TYPE_4BYTE_ABGR : 6  , // TYPE_3BYTE_BGR : 5
	
		final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		final int width = image.getWidth();
		final int height = image.getHeight();
		final boolean hasAlphaChannel = image.getAlphaRaster() != null;

//		logger.log(Level.CONFIG, "hasAlphaChannel : " + hasAlphaChannel);
		
		int[][] result = new int[height][width];
		if (hasAlphaChannel) {
			final int pixelLength = 4;
			for (int pixel = 0, row = 0, col = 0; pixel + 3 < pixels.length; pixel += pixelLength) {
				int argb = 0;
				argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
				argb += ((int) pixels[pixel + 1] & 0xff); // blue
				argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
				argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
				
//				The Red and Blue channel comments are flipped. 
//				Red should be +1 and blue should be +3 (or +0 and +2 respectively in the No Alpha code).
				
//				You could also make a final int pixel_offset = hasAlpha?1:0; and 
//				do ((int) pixels[pixel + pixel_offset + 1] & 0xff); // green; 
//				and merge the two loops into one. – Tomáš Zato Mar 23 '15 at 23:02
						
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
	
//	/**
//	 * Loads the index data from a file.
//	 *
//	 * @param file name
//	 * @return array of index data
//	 * @throws IOException if file cannot be loaded.
//	 */
//	private int[] loadIndexData(String filename) throws IOException {
//
//		// Load index data from map_data jar file.
//		ClassLoader loader = getClass().getClassLoader();
//		InputStream indexStream = loader.getResourceAsStream(filename);
//		if (indexStream == null)
//			throw new IOException("Can not load " + filename);
//
//		// Read stream into an array.
//		BufferedInputStream indexBuff = new BufferedInputStream(indexStream);
//		DataInputStream indexReader = new DataInputStream(indexBuff);
//		int index[] = new int[MAP_HEIGHT];
//		for (int x = 0; x < index.length; x++) {
//			index[x] = indexReader.readInt();
//		}
//		indexReader.close();
//		indexBuff.close();
//
//		return index;
//	}

//	/**
//	 * Loads the map data from a file.
//	 *
//	 * @param filename the map data file
//	 * @param index    the index array
//	 * @return array list of map data
//	 * @throws IOException if map data cannot be loaded.
//	 */
//	private int[][] loadMapData(String filename, int[] index) throws IOException {
//
//		// Load map data from map_data jar file.
//		ClassLoader loader = getClass().getClassLoader();
//		InputStream mapStream = loader.getResourceAsStream(filename);
//		if (mapStream == null)
//			throw new IOException("Can not load " + filename);
//
//		// Decompress the xz file
//		// new DecompressXZ(filename);
//
//		// Read stream into an array.
//		BufferedInputStream mapBuff = new BufferedInputStream(mapStream);
//		DataInputStream mapReader = new DataInputStream(mapBuff);
//
//		// Create map colors array list.
////		ArrayList<int[]> mapColors = new ArrayList<int[]>(MAP_HEIGHT);
//		
//		int[][] pixels = new int[MAP_HEIGHT][MAP_WIDTH];
//
//		// Create an array of colors for each pixel in map height.
//		for (int i = 0; i < MAP_HEIGHT; i++) {
//			int[] colors = new int[index[i]];
//			
//			for (int j = 0; j < colors.length; j++) {
//				int red = mapReader.readByte();
//				red <<= 16;
//				red &= 0x00FF0000;
//				int green = mapReader.readByte();
//				green <<= 8;
//				green &= 0x0000FF00;
//				int blue = mapReader.readByte();
//				blue &= 0x000000FF;
//				int totalColor = 0xFF000000 | red | green | blue;
//				colors[j] = (new Color(totalColor)).getRGB();
////				System.out.println("totalColor: " + totalColor + "  colors[y]: " + colors[y]);
//			}
//
//			pixels[i] = colors;
//		
//		}
//		
//		return pixels;
//	}

	@Override
	public Image getMapImage(double centerPhi, double centerTheta) {

		// Create a new buffered image to draw the map on.
		BufferedImage result = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_4BYTE_ABGR);//BufferedImage.TYPE_INT_ARGB);

		// The map data is PI offset from the center theta.
		double correctedTheta = centerTheta - Math.PI;
		while (correctedTheta < 0D)
			correctedTheta += TWO_PI;
		while (correctedTheta > TWO_PI)
			correctedTheta -= TWO_PI;

		// Create an array of int RGB color values to create the map image from.
		int[] mapArray = new int[IMAGE_WIDTH * IMAGE_HEIGHT];

		// Determine phi iteration angle.
		double phiIterationPadding = 1.26D; // Derived from testing.
		double phiIterationAngle = Math.PI / (MAP_HEIGHT / MAP_RATIO * phiIterationPadding);

		// Determine phi range.
		double phiPadding = 1.46D; // Derived from testing.
		double phiRange = Math.PI * phiPadding * IMAGE_HEIGHT / MAP_HEIGHT * MAP_RATIO;

		// Determine starting and ending phi values.
		double startPhi = centerPhi - (phiRange / 2D);
		if (startPhi < 0D)
			startPhi = 0D;
		double endPhi = centerPhi + (phiRange / 2D);
		if (endPhi > Math.PI)
			endPhi = Math.PI;

		double ratio = TWO_PI * IMAGE_WIDTH / MAP_WIDTH * MAP_RATIO;
		// Note : Polar cap phi values must display 2 PI theta range. 
		// (derived from testing)
		double polarCapRange = Math.PI / 6.54D; 
		// Determine theta iteration angle.
		double thetaIterationPadding = 1.46D; // Derived from testing.
		// Theta padding, derived from testing.
		double minThetaPadding = 1.02D; 
		// Determine theta range.
		double minThetaDisplay = ratio * minThetaPadding;
		
		// Loop through each phi value.
		for (double x = startPhi; x <= endPhi; x += phiIterationAngle) {

			double thetaIterationAngle = TWO_PI / (((double) MAP_WIDTH / MAP_RATIO * Math.sin(x) * thetaIterationPadding) + 1D);

			double thetaRange = ((1D - Math.sin(x)) * TWO_PI) + minThetaDisplay;
			
			if ((x < polarCapRange) || (x > (Math.PI - polarCapRange)))
				thetaRange = TWO_PI;
			if (thetaRange > TWO_PI)
				thetaRange = TWO_PI;

			// Determine the theta starting and ending values.
			double startTheta = centerTheta - (thetaRange / 2D);
			double endTheta = centerTheta + (thetaRange / 2D);

			// Loop through each theta value.
			for (double y = startTheta; y <= endTheta; y += thetaIterationAngle) {

				// Correct y value to make sure it is within bounds. (0 to 2PI)
				double yCorrected = y;
				while (yCorrected < 0)
					yCorrected += TWO_PI;
				while (yCorrected > TWO_PI)
					yCorrected -= TWO_PI;

				// Determine the rectangular offset of the pixel in the image.
				Point location = findRectPosition(centerPhi, centerTheta, x, yCorrected, 1440D / Math.PI, 720,
						720 - 150);

				// Determine the display x and y coordinates for the pixel in the image.
				int displayX = IMAGE_WIDTH - location.x;
				int displayY = IMAGE_HEIGHT - location.y;

				// Check that the x and y coordinates are within the display area.
				boolean leftBounds = displayX >= 0;
				boolean rightBounds = displayX < IMAGE_WIDTH;
				boolean topBounds = displayY >= 0;
				boolean bottomBounds = displayY < IMAGE_HEIGHT;
				if (leftBounds && rightBounds && topBounds && bottomBounds) {

					// Determine array index for the display location.
					int index = (IMAGE_WIDTH - displayX) + ((IMAGE_HEIGHT - displayY) * IMAGE_WIDTH);

					// Put color in array at index.
					if ((index >= 0) && (index < mapArray.length))
						mapArray[index] = getRGBColorInt(x, yCorrected);
				}
			}
		}

		// Create new map image.
		result.setRGB(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, mapArray, 0, IMAGE_WIDTH);

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

//		int row = (int) Math.round(phi * (MAP_HEIGHT / Math.PI));
//		if (row == mapColors.size())
//			row--;
//
//		int[] colorRow = mapColors.get(row);
//		int column = (int) Math.round(theta * ((double) colorRow.length / TWO_PI));
//		if (column == colorRow.length)
//			column--;

		int row = (int) Math.round(phi * (MAP_HEIGHT / Math.PI));
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
	 * @param rho      diameter of planet (in km)
	 * @param half_map half the map's width (in pixels)
	 * @param low_edge lower edge of map (in pixels)
	 * @return pixel offset value for map
	 */
	public Point findRectPosition(double oldPhi, double oldTheta, double newPhi, double newTheta, double rho,
			int half_map, int low_edge) {

		final double temp_col = newTheta + ((Math.PI / -2D) - oldTheta);
		final double temp_buff_x = rho * Math.sin(newPhi);
		int buff_x = ((int) Math.round(temp_buff_x * Math.cos(temp_col)) + half_map) - low_edge;
		int buff_y = ((int) Math.round(((temp_buff_x * (0D - Math.cos(oldPhi))) * Math.sin(temp_col))
				+ (rho * Math.cos(newPhi) * (0D - Math.sin(oldPhi)))) + half_map) - low_edge;
		return new Point(buff_x, buff_y);
	}
	
	public static void reproduceImage(BufferedImage image) {
		int pixels[][] = convertTo2DWithoutUsingGetRGB(image);//convertTo2DUsingFastRGB(image);//convertTo2DUsingGetRGB(image);//convertTo2DWithoutUsingGetRGB(image);
		 
		boolean withAlpha = false; // if you need the alpha channel change this to true
		
		logger.log(Level.CONFIG, "withAlpha : " + withAlpha);
		
		String imgFormat = "jpg";  // if you need the alpha channel change this to png
		String imgPath   = "testImage." + imgFormat;
		 
		BufferedImage newImg = getCustomImage(pixels, withAlpha);
		 
		File location = new File(System.getProperty("user.home") + "/.mars-sim/" + imgPath);
		
		try {
			ImageIO.write(newImg, imgFormat,location);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static int[][] convertTo2DWithoutUsingGetRGB(BufferedImage image) {

		final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		final int width = image.getWidth();
		final int height = image.getHeight();
		final boolean hasAlphaChannel = image.getAlphaRaster() != null;

		logger.log(Level.CONFIG, "hasAlphaChannel : " + hasAlphaChannel);
		
		int[][] result = new int[height][width];
		if (hasAlphaChannel) {
			final int pixelLength = 4;
			for (int pixel = 0, row = 0, col = 0; pixel + 3 < pixels.length; pixel += pixelLength) {
				int argb = 0;
				argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
				argb += ((int) pixels[pixel + 1] & 0xff); // blue
				argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
				argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
				
//				The Red and Blue channel comments are flipped. 
//				Red should be +1 and blue should be +3 (or +0 and +2 respectively in the No Alpha code).
				
//				You could also make a final int pixel_offset = hasAlpha?1:0; and 
//				do ((int) pixels[pixel + pixel_offset + 1] & 0xff); // green; 
//				and merge the two loops into one. – Tomáš Zato Mar 23 '15 at 23:02
				
				
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
	
	private static BufferedImage getCustomImage(int[][] pixels, final boolean withAlpha)
	{
	  // Assuming pixels was taken from convertTo2DWithoutUsingGetRGB
	  // i.e. img.length == pixels.length and img.width == pixels[x].length
	  BufferedImage img = new BufferedImage(pixels[0].length, pixels.length, withAlpha ? BufferedImage.TYPE_4BYTE_ABGR : BufferedImage.TYPE_3BYTE_BGR);
//	  BufferedImage img = new BufferedImage(pixels[0].length, pixels.length, withAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_BGR);
		
	  
	  for (int y = 0; y < pixels.length; y++)
	  {
	     for (int x = 0; x < pixels[y].length; x++)
	     {
	        if (withAlpha)
	           img.setRGB(x, y, pixels[y][x]);
	        else {
	           int pixel = pixels[y][x];
//	           int alpha = (pixel >> 24 & 0xff);
	           int red   = (pixel >> 16 & 0xff);
	           int green = (pixel >> 8 & 0xff);
	           int blue  = (pixel & 0xff);
	           int rgb = (red << 16) | (green << 8) | blue;
	           img.setRGB(x, y, rgb);
	        }
	     }
	  }
	  return img;
	}
	
	private static String toString(long nanoSecs) {
		int minutes = (int) (nanoSecs / 60000000000.0);
		int seconds = (int) (nanoSecs / 1000000000.0) - (minutes * 60);
		int millisecs = (int) (((nanoSecs / 1000000000.0) - (seconds + minutes * 60)) * 1000);

		if (minutes == 0 && seconds == 0)
			return millisecs + "ms";
		else if (minutes == 0 && millisecs == 0)
			return seconds + "s";
		else if (seconds == 0 && millisecs == 0)
			return minutes + "min";
		else if (minutes == 0)
			return seconds + "s " + millisecs + "ms";
		else if (seconds == 0)
			return minutes + "min " + millisecs + "ms";
		else if (millisecs == 0)
			return minutes + "min " + seconds + "s";

		return minutes + "min " + seconds + "s " + millisecs + "ms";
	}
	
	public void destroy() {
		pixels = null;
		logger = null;
	}
}
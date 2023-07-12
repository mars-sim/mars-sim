/*
 * Mars Simulation Project
 * IntegerMapData.java
 * @date 2023-06-22
 * @author Scott Davis
 */
 package org.mars.sim.mapdata;

import static com.jogamp.opencl.CLMemory.Mem.WRITE_ONLY;
import static org.mars.sim.mapdata.OpenCL.getGlobalSize;
import static org.mars.sim.mapdata.OpenCL.getKernel;
import static org.mars.sim.mapdata.OpenCL.getLocalSize;
import static org.mars.sim.mapdata.OpenCL.getProgram;
import static org.mars.sim.mapdata.OpenCL.getQueue;

import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.MemoryImageSource;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.mars.sim.mapdata.common.FileLocator;
import org.mars.sim.tools.util.MoreMath;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLProgram;

 /**
  * A map that uses integer data stored in files to represent colors.
  */
 public class IntegerMapData implements MapData {

	// Static members.
 	private static Logger logger = Logger.getLogger(IntegerMapData.class.getName());

	private static boolean HARDWARE_ACCELERATION = true;

 	public final double MAX_RHO;

 	public final double MIN_RHO;

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
 	
	private static final double PI_HALF = Math.PI / 2;
 	private static final double TWO_PI = Math.PI * 2;
	private static final double COL_ARRAY_MODIFIER = 1 / TWO_PI;
	// The default rho at the start of the sim
 	private final double RHO_DEFAULT;

 	// Data members.
 	private int[][] pixels = null;
 	// # of pixels in the width of the map image
	private int pixelWidth;
 	// # of pixels in the height of the map image
	private int pixelHeight;
	// height pixels divided by pi (equals to pixelHeight / Math.PI)
	private double rho;
	
	private double centerPhiCache;
	
	private double centerThetaCache;

	// Name of the map
	private MapMetaData meta;
	
	private BufferedImage cylindricalMapImage;

	private CLProgram program;
	
	private CLKernel kernel;
 	
	private transient Image mapImage = null;
	
 	/**
 	 * Constructor.
 	 * 
	 * @param name   the name/description of the data
 	 * @param filename   the map data file name.
 	 * @throws IOException Problem loading map data
 	 */
 	IntegerMapData(MapMetaData newMeta) throws IOException {
		this.meta = newMeta;
		
		// Load data files
		pixels = loadMapData(newMeta.getHiResFile());
		
		rho =  pixelHeight / Math.PI;
		RHO_DEFAULT = rho;
		MAX_RHO = RHO_DEFAULT * 6;
		MIN_RHO = RHO_DEFAULT / 6;
		
		logger.info("Loaded " + meta.getHiResFile() + " with pixels " + pixelWidth + " by " + pixelHeight + ".");
		
		setKernel();
 	}
 	
 	/**
 	 * Sets up the JOCL kernel program.
 	 */
	private void setKernel() {
 
		try {
			program = getProgram("MapDataFast.cl");
			kernel = getKernel(program, "getMapImage")
					.setArg(11, (float) TWO_PI)
					.setArg(12, (float) getRho());
		} catch(Exception e) {
			HARDWARE_ACCELERATION = false;
			logger.log(Level.SEVERE, "Disabling hardware acceleration due to exception caused while compiling: " + e.getMessage());
		}
 	}

	/**
	 * Gets the meta data of the map.
	 * 
	 * @return
	 */
	@Override
	public MapMetaData getMetaData() {
		return meta;
	}
	
	/**
	 * Gets the scale of the Mars surface map.
	 * 
	 * @return
	 */
	@Override
	public double getRho() {
		return rho;
	}
	
    /**
     * Gets the magnification of the Mars surface map.
     * 
     * @return
     */
    public double getMagnification() {
    	return rho / RHO_DEFAULT;
    }
    
	/**
	 * Sets the rho of the Mars surface map.
	 * 
	 * @param value
	 */
	public void setRho(double value) {
		double newRho = value;
		if (newRho > MAX_RHO) {
			newRho = MAX_RHO;
		}
		else if (newRho < MIN_RHO) {
			newRho = MIN_RHO;
		}
		
		if (rho != newRho) {
			rho = newRho;
//	 		logger.info("rho: " + rho + "  newRho: " + newRho);
		}
	}	

	
	/**
     * Gets the number of pixels width.
     * 
     * @return
     */
	@Override
    public int getWidth() {
		return pixelWidth;
	}

	/**
     * Gets the number of pixels height.
     * 
     * @return
     */
	@Override
    public int getHeight() {
		return pixelHeight;
	}

 	/**
 	 * Loads the whole map data set into an 2-D integer array.
 	 * 
 	 * @param imageName
 	 * @return
 	 * @throws IOException
 	 */
 	private int[][] loadMapData(String imageName) throws IOException {

 		File imageFile = FileLocator.locateFile(imageName);
 		cylindricalMapImage = null;
		try {
			cylindricalMapImage = ImageIO.read(imageFile);
		} catch (IOException e) {
			logger.severe("Can't read image file: " + imageFile + ".");
		}

 		final byte[] pixels = ((DataBufferByte) cylindricalMapImage.getRaster().getDataBuffer()).getData();
 		pixelWidth = cylindricalMapImage.getWidth();
 		pixelHeight = cylindricalMapImage.getHeight();
 		
 		final boolean hasAlphaChannel = cylindricalMapImage.getAlphaRaster() != null;
 		logger.config("hasAlphaChannel: " + hasAlphaChannel);
 		
 		int[][] result = new int[pixelHeight][pixelWidth];
 		
 		if (hasAlphaChannel) {
 			final int pixelLength = 4;
 			for (int pixel = 0, row = 0, col = 0; pixel + 3 < pixels.length; pixel += pixelLength) {
 				int argb = 0;
 				
 				// See https://stackoverflow.com/questions/11380062/what-does-value-0xff-do-in-java
 				// When applying '& 0xff', it would end up with the value ff ff ff fe instead of 00 00 00 fe. 
 				// A further subtlety is that '&' is defined to operate only on int values. As a result, 
 				//
 				// 1. value is promoted to an int (ff ff ff fe).
 				// 2. 0xff is an int literal (00 00 00 ff).
 				// 3. The '&' is applied to yield the desired value for result.

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
 				if (col == pixelWidth) {
 					col = 0;
 					row++;
 				}
 			}
 		}
 		
 		else {
 			final int pixelLength = 3;
 			for (int pixel = 0, row = 0, col = 0; pixel + 2 < pixels.length; pixel += pixelLength) {
 				int argb = 0;
 				
 				argb += -16777216; // 255 alpha
 				argb += ((int) pixels[pixel] & 0xff); // blue
 				argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
 				argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
 				
 				result[row][col] = argb;
 				col++;
 				if (col == pixelWidth) {
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
 	 * @param centerTheta Center theta value on the image
	 * @param mapBoxWidth The Width of the requested image
	 * @param mapBoxHeight The Height of the requested image
	 * @param newRho The map rho
 	 */
 	@Override
 	public Image getMapImage(double centerPhi, double centerTheta, int mapBoxWidth, int mapBoxHeight, double newRho) {
	
		 boolean invalid = Double.isNaN(centerPhi) || Double.isNaN(centerTheta);
		 if (invalid) {
			 logger.log(Level.SEVERE, "centerPhi and/or centerTheta is invalid.");
			 return null;
		 }
	 
 		if (mapImage != null 
 				&& (centerPhiCache == centerPhi && centerThetaCache == centerTheta && newRho == rho)) {
 			// No need to recreate the mapImage when the mouse has not moved
 			return mapImage;
 		}
 		
		// Set the new phi		
 		centerPhiCache = centerPhi;
		// Set the new theta 		
 		centerThetaCache = centerTheta;
		// Set the new map rho
		setRho(newRho);
 		
		logger.log(Level.INFO, "centerPhiCache: " + centerPhiCache + "  centerThetaCache: " + centerThetaCache
				+ "  scale: " + newRho);
		
 		// Create a new buffered image to draw the map on.
 		BufferedImage result = new BufferedImage(mapBoxWidth, mapBoxHeight, 
 				BufferedImage.TYPE_INT_ARGB);//.TYPE_INT_ARGB);//TYPE_4BYTE_ABGR);

 		// May experiment with BufferedImage.getSubimage(int x, int y, int w, int h);

// 		logger.config("transparency: " + result.getTransparency());
 		
 		// Create an array of int RGB color values to create the map image from.
 		int[] mapArray = new int[mapBoxWidth * mapBoxHeight];
 
		if(HARDWARE_ACCELERATION || program == null || kernel == null) {
			try {
				gpu(centerPhi, centerTheta, mapBoxWidth, mapBoxHeight, mapArray);
			} catch(Exception e) {
				HARDWARE_ACCELERATION = false;
				logger.log(Level.SEVERE, "Disabling GPU OpenCL acceleration due to exception caused while rendering: " + e.getMessage());
			}
		}
		else {
			cpu0(centerPhi, centerTheta, mapBoxWidth, mapBoxHeight, mapArray);
		}

//		mapImage = displayComponent.createImage(new MemoryImageSource(mapBoxWidth,
//				mapBoxHeight, mapArray, 0, mapBoxWidth));
//
//		MediaTracker mt = new MediaTracker(displayComponent);
//		mt.addImage(mapImage, 0);
//		try {
//			mt.waitForID(0);
//		} catch (InterruptedException e) {
//			logger.log(Level.SEVERE, "MineralMapLayer interrupted: " + e);
//			// Restore interrupted state
//		    Thread.currentThread().interrupt();
//		}
//		
//		return mapImage;
		
 		// Create new map image.
 		result.setRGB(0, 0, mapBoxWidth, mapBoxHeight, mapArray, 0, mapBoxWidth);

 		mapImage = result;
		
 		return result;
 	}


 	/**
 	 * Constructs a map array for display with CPU.
 	 * 
 	 * @param centerPhi
 	 * @param centerTheta
 	 * @param mapBoxWidth
 	 * @param mapBoxHeight
 	 * @param mapArray
 	 * @param scale
 	 */
	 private void cpu0(double centerPhi, double centerTheta, int mapBoxWidth, int mapBoxHeight, int[] mapArray) {
		 int halfWidth = mapBoxWidth / 2;
		 int halfHeight = mapBoxHeight / 2;

		 for(int y = 0; y < mapBoxHeight; y++) {
			 for(int x = 0; x < mapBoxWidth; x++) {
				 int index = x + (y * mapBoxWidth);

				 Point2D loc = convertRectToSpherical(x - halfWidth, y - halfHeight, centerPhi, centerTheta, getRho());
				 mapArray[index] = getRGBColorInt(loc.getX(), loc.getY());
			 }
		 }
	 }

	/**
 	 * Constructs a map array for display with CPU.
 	 * 
 	 * @param centerPhi
 	 * @param centerTheta
 	 * @param mapBoxWidth
 	 * @param mapBoxHeight
 	 * @param mapArray
 	 * @param scale
 	 */
	 private void cpu1(double centerPhi, double centerTheta, int mapBoxWidth, int mapBoxHeight, int[] mapArray) {
		 
		double phiRange = Math.PI * PHI_PADDING * (1.0 * mapBoxHeight / pixelHeight);
		double phiIterationAngle = Math.PI / (pixelHeight * PHI_ITERATION_PADDING);
		double thetaIterationFactor = pixelWidth * THETA_ITERATION_PADDING;
		double minThetaDisplay = TWO_PI * (1.0 * mapBoxWidth / pixelWidth) * MIN_THETA_PADDING;
		// lower edge = pixel height / 2 - map box height / 2
		int lowEdge = (pixelHeight/2) - (mapBoxHeight / 2);

 		// The map data is PI offset from the center theta.
 		double correctedTheta = centerTheta - Math.PI;
 		while (correctedTheta < 0D)
 			correctedTheta += TWO_PI;
 		while (correctedTheta > TWO_PI)
 			correctedTheta -= TWO_PI;

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
 				Point2D location = findRectPosition(centerPhi, centerTheta, x, yCorrected,  lowEdge);
// 				Point2D location = convertRectToSpherical(x, yCorrected, centerPhi, centerTheta, lowEdge);//getRho());
 						
 				// Determine the display x and y coordinates for the pixel in the image.
 				int displayX = (int) Math.round(mapBoxWidth - location.getX());
 				int displayY = (int) Math.round(mapBoxHeight - location.getY());

 				// Check that the x and y coordinates are within the display area.
 				boolean leftBounds = displayX >= 0;
 				boolean rightBounds = displayX < mapBoxWidth;
 				boolean topBounds = displayY >= 0;
 				boolean bottomBounds = displayY < mapBoxHeight;
 				if (leftBounds && rightBounds && topBounds && bottomBounds) {

 					// Determine array index for the display location.
 					int index = (mapBoxWidth - displayX) + ((mapBoxHeight - displayY) * mapBoxWidth);

 					// Put color in array at index.
 					if ((index >= 0) && (index < mapArray.length))
 						mapArray[index] = getRGBColorInt(x, yCorrected);
 				}
 			}
 		}
	 }
	 
	/**
 	 * Converts spherical coordinates to rectangular coordinates. Returns integer x
 	 * and y display coordinates for spherical location.
 	 *
 	 * @param newPhi   the new phi coordinate
 	 * @param newTheta the new theta coordinate
 	 * @return pixel offset value for map
 	 */
 	private Point2D findRectPosition(double oldPhi, double oldTheta, double newPhi, double newTheta, int lowEdge) {

 		final double temp_col = newTheta + ((Math.PI / -2D) - oldTheta);
 		final double temp_buff_x = rho * Math.sin(newPhi);
 		int buff_x = ((int) Math.round(temp_buff_x * Math.cos(temp_col)) + (pixelHeight/2)) - lowEdge;
 		int buff_y = ((int) Math.round(((temp_buff_x * (0D - Math.cos(oldPhi))) * Math.sin(temp_col))
 				+ (rho * Math.cos(newPhi) * (0D - Math.sin(oldPhi)))) + (pixelHeight/2)) - lowEdge;
 		return new Point2D.Double(buff_x, buff_y);
 	}
	 
	/**
 	 * Constructs a map array for display with CPU.
 	 * 
 	 * @param centerPhi
 	 * @param centerTheta
 	 * @param mapBoxWidth
 	 * @param mapBoxHeight
 	 * @param mapArray
 	 * @param scale
 	 */
	 private void cpu2(double centerPhi, double centerTheta, int mapBoxWidth, int mapBoxHeight, int[] mapArray) {

		 // Initialize variables
		 int row, col_num, map_col;
		 double phi, theta;
		 double circum, offset;
		 double ih_d = (double) mapBoxHeight;
	
		 // Initialize color arrays
		 @SuppressWarnings("unchecked")
		 Vector<Integer>[] sphereColor = new Vector[mapBoxHeight];
		 int[] pixelsColorArray = new int[mapBoxWidth * mapBoxHeight];
		 int[][] mapPixelsArray = new int[mapBoxWidth][mapBoxHeight];
	
		 // Transfer contents of 1-dimensional pixels_color into 2-dimensional map_pixels
		 for (int x = 0; x < mapBoxWidth; x++)
			 for (int y = 0; y < mapBoxHeight; y++)
				 mapPixelsArray[x][y] = pixelsColorArray[x + (y * mapBoxWidth)];
		
		 // Initialize variables
		 offset = PI_HALF / ih_d;
		 
		 // Go through each row and create Sphere_Color vector with it
		 for (phi = offset; phi < Math.PI; phi += (Math.PI / ih_d)) {
			 row = MoreMath.floor((float) ((phi / Math.PI) * ih_d));//(int) Math.floor((phi / Math.PI) * ih_d);
			 circum = TWO_PI * (getRho() * Math.sin(phi));
			 col_num = (int) Math.round(circum);
			 sphereColor[row] = new Vector<Integer>(col_num);
	
			 // Fill vector with colors
			 for (theta = 0; theta < TWO_PI; theta += (TWO_PI / circum)) {
				 if (theta == 0) {
					 map_col = 0;
				 } else {
					 map_col = MoreMath.floor((float)((theta / Math.PI) * ih_d));
				 }
	
				 sphereColor[row].addElement(mapPixelsArray[map_col][row]);
			 }
		 }
		 
//		 logger.config("sphereColor: " + sphereColor);
		 
		 createArray(centerPhi, centerTheta, mapBoxWidth, mapBoxHeight, mapArray, sphereColor);
	 }

	 /**
	  * Creates the map image array.
	  * 
	  * @param centerPhi
	  * @param centerTheta
	  * @param mapBoxWidth
	  * @param mapBoxHeight
	  * @param mapArray
	  */
	 public void createArray(double centerPhi, double centerTheta, int mapBoxWidth, int mapBoxHeight, int[] mapArray, Vector<Integer>[] sphereColor) {
		 double phi = centerPhi;
		double theta = centerTheta;
		double end_row = phi - PI_HALF;
		double start_row = end_row + Math.PI;
		double row_iterate;
		boolean north;

		// Determine if sphere should be created from north-south, or from south-north
		if (phi <= PI_HALF) {
			north = true;
			end_row = phi - PI_HALF;
			start_row = end_row + Math.PI;
			row_iterate = 0D - Math.PI / mapBoxHeight;
		} else {
			north = false;
			start_row = phi - PI_HALF;
			end_row = start_row + Math.PI;
			row_iterate = Math.PI / mapBoxHeight;
		}

		// More variable initializations
		double col_correction = -PI_HALF - theta;
		// double rho = map_height / Math.PI;
		double sin_offset = Math.sin(phi + Math.PI);
		double cos_offset = Math.cos(phi + Math.PI);

		// Go through each row of the sphere
		for (double row = start_row; (((north) && (row >= end_row))
				|| ((!north) && (row <= end_row))); row += row_iterate) {
			if (row < 0)
				continue;
			if (row >= Math.PI)
				continue;
			int array_y = (int) Math.round(mapBoxHeight * row / Math.PI);
			if (array_y >= mapBoxHeight)
				continue;

			// Determine circumference of this row
			
//			if (sphereColor != null) {
				System.out.println("array_y: " + array_y + "  sphereColor is " + sphereColor.length + " by " + sphereColor[0].size());
//			}
			
			int circum = sphereColor[array_y].size();
			double row_cos =  Math.cos(row);

			// Determine visible boundary of row
			double col_boundry = Math.PI;
			if (phi <= PI_HALF) {
				if ((row >= PI_HALF *  Math.cos(phi)) && (row < PI_HALF)) {
					col_boundry = PI_HALF * (1D + row_cos);
				} else if (row >= PI_HALF) {
					col_boundry = PI_HALF;
				}
			} else {
				if ((row <= PI_HALF *  Math.cos(phi)) && (row > PI_HALF)) {
					col_boundry = PI_HALF * (1D - row_cos);
				} else if (row <= PI_HALF) {
					col_boundry = PI_HALF;
				}
			}
			if (phi == PI_HALF) {
				col_boundry = PI_HALF;
			}

			double col_iterate = Math.PI / circum;

			// Error adjustment for theta center close to PI_half
			double error_correction = phi - PI_HALF;

			if (error_correction > 0D) {
				if (error_correction < row_iterate) {
					col_boundry = PI_HALF;
				}
			} else if (error_correction > 0D - row_iterate) {
				col_boundry = PI_HALF;
			}

			// Determine column starting and stopping points for row
			double start_col = theta - col_boundry;
			double end_col = theta + col_boundry;
			if (col_boundry == Math.PI)
				end_col -= col_iterate;

			double temp_buff_x = getRho() * Math.sin(row);
			double temp_buff_y1 = temp_buff_x * cos_offset;
			double temp_buff_y2 = getRho() * row_cos * sin_offset;

			double col_array_modifier2 = COL_ARRAY_MODIFIER * circum;

			// Go through each column in row
			for (double col = start_col; col <= end_col; col += col_iterate) {
				int array_x = (int) (col_array_modifier2 * col);

				if (array_x < 0) {
					array_x += circum;
				} else if (array_x >= circum) {
					array_x -= circum;
				}

				double temp_col = col + col_correction;

				// Determine x and y position of point on image
				int buff_x = (int) Math.round(temp_buff_x *  Math.cos(temp_col) + mapBoxHeight / 2.0);
				int buff_y = (int) Math.round((temp_buff_y1 * Math.sin(temp_col) + temp_buff_y2) + mapBoxHeight / 2.0);

				// Put point in buffer array
				mapArray[buff_x + (mapBoxHeight * buff_y)] = (int) sphereColor[array_y].elementAt(array_x);
				// buffer_array[buff_x + (map_height * buff_y)] = 0xFFFFFFFF; // if in gray
				// scale
			}
		}
	 }
		 
	 /**
	  * Constructs a map array for display with GPU via JOCL.
	  * 
	  * @param centerPhi
	  * @param centerTheta
	  * @param mapBoxWidth
	  * @param mapBoxHeight
	  * @param mapArray
	  * @param scale
	  */
	 private synchronized void gpu(double centerPhi, double centerTheta, int mapBoxWidth, int mapBoxHeight, int[] mapArray) {
		 
		 // Set the new scale arg again
		 kernel.setArg(12, (float) getRho());
		 
		 int size = mapArray.length;
		 int globalSize = getGlobalSize(size);

		 CLBuffer<IntBuffer> rowBuffer = OpenCL.getContext().createIntBuffer(size, WRITE_ONLY);
		 CLBuffer<IntBuffer> colBuffer = OpenCL.getContext().createIntBuffer(size, WRITE_ONLY);

		 kernel.rewind();
		 kernel.putArg((float)centerPhi)
				 .putArg((float)centerTheta)
				 .putArg(mapBoxWidth)
				 .putArg(mapBoxHeight)
				 .putArg(pixelWidth)
				 .putArg(pixelHeight)
				 .putArg(mapBoxWidth/2)
				 .putArg(mapBoxHeight/2)
				 .putArg(size)
				 .putArgs(colBuffer, rowBuffer);

		 getQueue().put1DRangeKernel(kernel, 0, globalSize, getLocalSize())
				 .putReadBuffer(rowBuffer, false)
				 .putReadBuffer(colBuffer, true);

		 int[] rows = new int[size];
		 rowBuffer.getBuffer().get(rows);
		 int[] cols = new int[size];
		 colBuffer.getBuffer().get(cols);

		 for(int i = 0; i < size; i++) {
			 mapArray[i] = pixels[rows[i]][cols[i]];
		 }

		 rowBuffer.release();
		 colBuffer.release();
	 }



	 /**
      * Converts linear rectangular XY position change to spherical coordinates with
      * rho value for map.
      *
      * @param x              change in x value (# of pixels or km)
      * @param y              change in y value (# of pixels or km)
      * @param phi			  center phi value (radians)
      * @param theta		  center theta value (radians)
      * @param rho            radius (in km) or map box height divided by pi (# of pixels)
      * @return a point2d of phi and theta
      */
	 public static Point2D convertRectToSpherical(double x, double y, double phi, double theta, double rho) {
		 double sinPhi = Math.sin(phi);
		 double sinTheta = Math.sin(theta);
		 double cosPhi = Math.cos(phi);
		 double cosTheta = Math.cos(theta);

		 double z = Math.sqrt((rho * rho) - (x * x) - (y * y));

		 double x2 = x;
		 double y2 = (y * cosPhi) + (z * sinPhi);
		 double z2 = (z * cosPhi) - (y * sinPhi);

		 double x3 = (x2 * cosTheta) + (y2 * sinTheta);
		 double y3 = (y2 * cosTheta) - (x2 * sinTheta);
		 double z3 = z2;

		 double phiNew = Math.acos(z3 / rho);
		 double thetaNew = Math.asin(x3 / (rho * Math.sin(phiNew)));

		 if (x3 >= 0) {
			 if (y3 < 0)
				 thetaNew = Math.PI - thetaNew;
		 } else {
			 if (y3 < 0)
				 thetaNew = Math.PI - thetaNew;
			 else
				 thetaNew = TWO_PI + thetaNew;
		 }

		 return new Point2D.Double(phiNew, thetaNew);
	 }

// 	@Override
// 	public Color getRGBColor(double phi, double theta) {
// 		return new Color(getRGBColorInt(phi, theta));
// 	}

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
// 		if (theta > Math.PI)
// 			theta -= Math.PI;
// 		else if (theta < -Math.PI)
// 			theta += Math.PI;

 		// Make sure theta is between 0 and 2 PI.
 		while (theta > TWO_PI)
 			theta -= TWO_PI;
 		while (theta < 0)
 			theta += TWO_PI;

 		int row = (int) Math.round(phi * ((double) pixels.length / Math.PI));
 		if (row >= pixels.length - 1)
 			row = pixels.length - 1;

 		int column = (int) Math.round(theta * ((double) pixels[0].length / TWO_PI));
 		if (column >= pixels[0].length - 1)
 			column = pixels[0].length - 1;
 		
 		return pixels[row][column];
 	}

 	
 	public BufferedImage getCylindricalMapImage() {
 		return cylindricalMapImage;
 	}
 	
 	public int[][] getPixels() {
 		return pixels;
 	}
 	
// 	public static List<Point2D> getLocations() {
// 		return locations;
// 	}
 	
	/**
	 * Prepares map panel for deletion.
	 */
	public void destroy() {
	 	pixels = null;
	 	meta = null;
		cylindricalMapImage = null;
		program = null;
		kernel = null;
	}
 	
 }

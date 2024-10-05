/*
 * Mars Simulation Project
 * IntegerMapData.java
 * @date 2024-09-05
 * @author Scott Davis
 */
 package com.mars_sim.core.map;

import static com.jogamp.opencl.CLMemory.Mem.WRITE_ONLY;
import static com.mars_sim.core.map.OpenCL.getGlobalSize;
import static com.mars_sim.core.map.OpenCL.getKernel;
import static com.mars_sim.core.map.OpenCL.getLocalSize;
import static com.mars_sim.core.map.OpenCL.getProgram;
import static com.mars_sim.core.map.OpenCL.getQueue;

import java.awt.Image;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLKernel;
import com.mars_sim.core.data.Range;
import com.mars_sim.core.map.common.FileLocator;
import com.mars_sim.core.map.location.Coordinates;

/**
 * For png image tiling, see https://github.com/leonbloy/pngj/wiki/Snippets
 */

 /**
  * A map that uses integer data stored in files to represent colors.
  */
 public class IntegerMapData implements MapData {

	// Static members.
 	private static Logger logger = Logger.getLogger(IntegerMapData.class.getName());
	
 	private static final double TWO_PI = Math.PI * 2;
 	
 	private static boolean isGPUCapable = true;
 	private static boolean hardwareAccel = true;
	
	// The max rho
 	private Range rhoRange; 	
 	
 	// The factor to apply for calaculate the min & max rho
  	private static final double MAX_RHO_MULTIPLER = 5;
  	private static final double MIN_RHO_MULTIPLER = 0.9;
  	
 	private static final double HALF_PI = Math.PI / 2D;
 	private static final String CL_FILE = "MapDataFast.cl";
 	private static final String KERNEL_NAME = "getMapImage";
 	
  	/* # of pixels in the width of the map image. */
	private int pixelWidth;
	/* # of pixels in the height of the map image. */
	private int pixelHeight;
	/* The cache for the last rho */
	private double rhoCache;
	/* The cache for the last center. */
	private Coordinates centerCache;
	/* The base map color pixels double array. */
 	private int[][] colorPixels = new int[0][0];
	/* The array of points for generating mineral map in a mapbox. */	
 	private static Point2D[] mapBoxArray;
 	
 	/* The meta data of the map. */
	private MapMetaData meta;
 	/* The OpenCL kernel instance. */
	private CLKernel kernel;
	
	private BufferedImage bImageCache;
	private int resolution;
	private MapState loaded = MapState.PENDING;

	private double rhoDefault;
 	
 	/**
 	 * Constructor.
 	 * 
	 * @param mapMetaData Meta data describing this map stack
	 * @param res The Resolution level in the map stack
	 * @param dataFilename Golds the source data
 	 * @throws IOException Problem loading map data
 	 */
 	IntegerMapData(MapMetaData mapMetaData, int res, String dataFilename) throws IOException {
		this.meta = mapMetaData;
		this.resolution = res;

		// Load data files async
		var dataFile = FileLocator.locateFileAsync(MapDataFactory.MAPS_FOLDER + dataFilename,
						this::loadMapData);
		
		// Data file is already available
		if (dataFile != null) {
			loadMapData(dataFile);
		}

		// Check if OpenCL is supported
		isGPUCapable = OpenCL.initCompute();
		hardwareAccel = isGPUAvailable();
		if (isGPUCapable) {
			setKernel();
		}
 	}
 	
 	/**
 	 * Sets up the JOCL kernel program.
 	 */
	private void setKernel() {
 
		try {
			var program = getProgram(CL_FILE);
			kernel = getKernel(program, KERNEL_NAME)
					.setArg(11, (float) TWO_PI)
					.setArg(12, 1F); // This last arguments get changed every call
			logger.config("GPU OpenCL accel enabled.");
		} catch(Exception e) {
			hardwareAccel = false;
			logger.log(Level.SEVERE, "Exception with GPU OpenCL accel when loading kernel program.", e);
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
	 * Get the resolution layer of this data in the parent Map Meta Data stack.
	 */
	@Override
	public int getResolution() {
		return resolution;
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
	 * Get the min and max value of rho supported by this mao data.
	 */
	@Override
	public Range getRhoRange() {
		return rhoRange;
	}

	/**
	 * Get the default rho for this map data
	 */
	@Override
	public double getRhoDefault() {
		return rhoDefault;
	}

 	/**
 	 * Loads the whole map data set into an 2-D integer array.
 	 * 
 	 * @param dataFile File containing the data to be loaded
 	 * @return
 	 */
 	private void loadMapData(File dataFile) {

 		BufferedImage cylindricalMapImage = null;
		try {
			cylindricalMapImage = ImageIO.read(dataFile);
			
	 		pixelWidth = cylindricalMapImage.getWidth();
	 		pixelHeight = cylindricalMapImage.getHeight();
	 				
	 		if (!meta.isColourful()) {
	 			colorPixels = loadMonoImage(cylindricalMapImage);
			}
	 		else { 
				final byte[] pixels = ((DataBufferByte) cylindricalMapImage.getRaster().getDataBuffer()).getData();
				final boolean hasAlphaChannel = cylindricalMapImage.getAlphaRaster() != null;
				if (hasAlphaChannel) {
					colorPixels = loadAlphaImage(pixels);
		 		}
		 		else {
					colorPixels = loadColourImage(pixels);
				}
	 		}

			// Update as ready
	 		loaded = MapState.LOADED;
			meta.setLocallyAvailable(resolution);
		} catch (IOException e) {
			logger.severe("Can't read image file : " + dataFile.getName());
			loaded = MapState.FAILED;
		}
	
		rhoDefault = pixelHeight / Math.PI;
		rhoRange = new Range(rhoDefault * MIN_RHO_MULTIPLER,
							 rhoDefault * MAX_RHO_MULTIPLER);
 	}
 	
	/**
	 * Parse the byte data from a colour image into a color array
	 * @param pixels Source mono byte values
	 * @return
	 */
 	private int[][] loadColourImage(byte[] pixels) {

		var loadPixels = new int[pixelHeight][pixelWidth];

		final int pixelLength = 3;
		for (int pixel = 0, row = 0, col = 0; pixel + 2 < pixels.length; pixel += pixelLength) {
			int argb = 0;
								 
			// Note: The color is a 32-bit integer in ARGB format. 
			//           Fully Opaque = 0xFF______
			//      Fully Transparent = 0x00______
			// Also,
			//	Red   = 0xFFFF0000
			//	Green = 0xFF00FF00
			//	Blue  = 0xFF0000FF
			//		 				
			argb += -16777216; // 255 alpha
			argb += (pixels[pixel] & 0xff); // blue
			argb += ((pixels[pixel + 1] & 0xff) << 8); // green
			argb += ((pixels[pixel + 2] & 0xff) << 16); // red
			
			loadPixels[row][col] = argb;
			col++;
			if (col == pixelWidth) {
				col = 0;
				row++;
			}
		}
		return loadPixels;
	}

	/**
	 * Parse image data of an image with an Alpha channel into a set ofcolour pixels
	 * @param pixels
	 * @return
	 */
	private int[][] loadAlphaImage(byte[] pixels) {
		// Note: 'Viking Geologic' and 'MOLA Shade' have alpha channel.
		var loadPixels = new int[pixelHeight][pixelWidth];

		final int pixelLength = 4;

		for (int pos = 0, row = 0, col = 0; pos + 3 < pixels.length; pos += pixelLength) {
			int argb = 0;

			// Note: The color is a 32-bit integer in ARGB format. 
			//           Fully Opaque = 0xFF______
			//      Fully Transparent = 0x00______
			// Also,
			//	   Red   = 0xFFFF0000
			//	   Green = 0xFF00FF00
			//	   Blue  = 0xFF0000FF
			//
			
			// See https://stackoverflow.com/questions/11380062/what-does-value-0xff-do-in-java
			// When applying '& 0xff', it would end up with the value ff ff ff fe instead of 00 00 00 fe. 
			// A further subtlety is that '&' is defined to operate only on int values. As a result, 
			//
			// 1. value is promoted to an int (ff ff ff fe).
			// 2. 0xff is an int literal (00 00 00 ff).
			// 3. The '&' is applied to yield the desired value for result.

			argb += ((pixels[pos] & 0xff) << 24); // alpha
			argb += (pixels[pos + 1] & 0xff); // blue
			argb += ((pixels[pos + 2] & 0xff) << 8); // green
			argb += ((pixels[pos + 3] & 0xff) << 16); // red
			
			// The Red and Blue channel comments are flipped. 
			// Red should be +1 and blue should be +3 (or +0 and +2 respectively in the No Alpha code).
				
			// You could also make a final int pixel_offset = hasAlpha?1:0; and 
			// do ((int) pixels[pixel + pixel_offset + 1] & 0xff); // green
			// and merge the two loops into one. – Tomáš Zato Mar 23 '15 at 23:02
					
			loadPixels[row][col] = argb;
			col++;
			if (col == pixelWidth) {
				col = 0;
				row++;
			}
		}

		return loadPixels;
	}

	/**
	 * Convert monochromomatic shared image into coloured pixels
	 * @param cylindricalMapImage
	 * @return
	 */
	private int[][] loadMonoImage(BufferedImage cylindricalMapImage) {
		// Note: May use the shade map to get height values
		var loadPixels = new int[pixelHeight][pixelWidth];

		Raster raster = cylindricalMapImage.getData();
		int h = raster.getHeight();
		int w = raster.getWidth();
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				loadPixels[j][i] = raster.getSample(i, j, 0);
			}
		}

		return loadPixels;
	}

	/**
 	 * Gets the map image based on the center phi and theta coordinates given.
 	 * 
 	 * @param center Center  value on the image
	 * @param mapBoxWidth The Width of the requested image
	 * @param mapBoxHeight The Height of the requested image
	 * @param newRho The map rho
 	 */
 	@Override
 	public Image createMapImage(Coordinates center, int mapBoxWidth, int mapBoxHeight, double newRho) {
		 
 		if (bImageCache != null && newRho == rhoCache
				&& centerCache.equals(center))
 			return bImageCache;
		double centerPhi = center.getPhi();
		double centerTheta = center.getTheta();


 		mapBoxArray = new Point2D[mapBoxHeight * mapBoxWidth];
 
 		// Update cache identifiers
		centerCache = center;
		rhoCache = newRho;

 		// Create a new buffered image to draw the map on.
 		BufferedImage bImage 
			= new BufferedImage(mapBoxWidth, mapBoxHeight, BufferedImage.TYPE_INT_RGB); 
 		
 		// Not using TYPE_INT_ARGB or TYPE_4BYTE_ABGR
 				
 		// Note: it turns out TYPE_INT_RGB works the best for gray map
 		
 		// Create an array of int RGB color values to create the map image from.
 		int[] mapArray = new int[mapBoxWidth * mapBoxHeight];
		var rendered = hardwareAccel;
		if (hardwareAccel) {
			try {
				gpu(centerPhi, centerTheta, mapBoxWidth, mapBoxHeight, newRho, mapArray);
			} catch(Exception e) {
				hardwareAccel = false;
				rendered = false; // Fallback to CPU
				logger.log(Level.SEVERE, "Exception with GPU OpenCL accel when running gpu(). " + e.getMessage());
			}
		}
		if (!rendered) {
			cpu0(centerPhi, centerTheta, mapBoxWidth, mapBoxHeight, newRho, mapArray);
		}

	 	// Gets the color pixels ready for the new projected map image in Mars Navigator.
	 	setRGB(bImage, 0, 0, mapBoxWidth, mapBoxHeight, mapArray, 0, mapBoxHeight);
	
	 	bImageCache = bImage;
	 	
 		return bImage;
 	}

 	/**
 	 * Sets up the RGB values of the buffer image.
 	 * 
 	 * @param bImage
 	 * @param startX
 	 * @param startY
 	 * @param w
 	 * @param h
 	 * @param rgbArray
 	 * @param offset
 	 * @param scansize
 	 */
    private void setRGB(BufferedImage bImage, int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize) {

    	// Note: Reference https://stackoverflow.com/questions/61130264/how-can-i-process-bufferedimage-faster
    	//       when attempting to speed up the processing
    	
		if (!meta.isColourful()) {
	        // Convert to grayscale

	        for (int i = 0; i < rgbArray.length; i++) {
	 
	            // Here i denotes the index of array of pixels
	            // for modifying the pixel value.
	            int p = rgbArray[i];
	 
	            int a = (p >> 24) & 0xff;
	            int r = (p >> 16) & 0xff;
	            int g = (p >> 8) & 0xff;
	            int b = p & 0xff;
	 
	            // calculate average
	            int avg = (r + g + b);
	            // Note: dividing avg by 3 will make it too dark
	 
	            // replace RGB value with avg
	            p = (a << 24) | (avg << 16) | (avg << 8) | avg;
            	
	            rgbArray[i] = p;
	        }
	        
	        bImage.setRGB(0, 0, w, h, rgbArray, 0, w);

		}
			
		else {
			int yoff  = offset;
			int off;
			Object pixel = null;
				
			for (int y = startY; y < startY + h; y++, yoff += scansize) {
				off = yoff;
				for (int x = startX; x < startX + w; x++) {
				    pixel = bImage.getColorModel().getDataElements(rgbArray[off++], pixel);
				    bImage.getRaster().setDataElements(x, y, pixel);
				}
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
	 private synchronized void gpu(double centerPhi, double centerTheta, int mapBoxWidth, int mapBoxHeight, double rho, int[] mapArray) {
		 
		 // Set the rho this way to avoid artifacts in a quarter of the globe map. Reason unknown.
		 kernel.setArg(12, (float) rho);
		 
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
				 .putArgs(colBuffer, rowBuffer)
				 ;

		 getQueue().put1DRangeKernel(kernel, 0, globalSize, getLocalSize())
				 .putReadBuffer(rowBuffer, false)
				 .putReadBuffer(colBuffer, true);

		 int[] rows = new int[size];
		 rowBuffer.getBuffer().get(rows);
		 int[] cols = new int[size];
		 colBuffer.getBuffer().get(cols);
	 
		 // Note that maxIndex = 262144
		 int maxIndex = mapBoxArray.length;
		 for (int i = 0; i < size; i++) {
			 int x = cols[i];
			 int y = rows[i];
			 int index = x + y * mapBoxWidth;
			 if (index < maxIndex)
				 mapBoxArray[index] = new Point2D.Double(x, y);
			 
			 boolean invalid = Double.isNaN(x) || Double.isInfinite(x) || Double.isNaN(y) || Double.isInfinite(y) ;
			 if (invalid) {
				 // Set the color to black
				 mapArray[i] = 0;
			 }
			 else {
				 mapArray[i] = colorPixels[x][y];
			 }
		 }

		 rowBuffer.release();
		 colBuffer.release();
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
	 private void cpu0(double centerPhi, double centerTheta, int mapBoxWidth, int mapBoxHeight, double rho, int[] mapArray) {
		 int halfWidth = mapBoxWidth / 2;
		 int halfHeight = mapBoxHeight / 2;

		 for(int y = 0; y < mapBoxHeight; y++) {
			 for(int x = 0; x < mapBoxWidth; x++) {
				 int index = x + (y * mapBoxWidth);
				 Point2D loc = convertRectIntToSpherical(x - halfWidth, y - halfHeight, centerPhi, centerTheta, rho);
				 mapBoxArray[index] = loc;
				 mapArray[index] = getRGBColorInt(loc.getX(), loc.getY());
			 }
		 }
	 }
	 
 	/**
 	 * Constructs a map array for display with CPU without the projected background issue.
 	 * 
 	 * @Note: this method cpu1 will replace cpu0 method. Currently not working. Retain for further debugging.
 	 * 
 	 * @param centerPhi
 	 * @param centerTheta
 	 * @param mapBoxWidth
 	 * @param mapBoxHeight
 	 * @param mapArray
 	 * @param scale
 	 */
	 private void cpu1(double centerPhi, double centerTheta, int mapBoxWidth, int mapBoxHeight, double rho, int[] mapArray) {
		 int halfWidth = mapBoxWidth / 2;

		 // The map data is PI offset from the center theta.
		 double correctedTheta = centerTheta - Math.PI;
		 while (correctedTheta < 0D)
			 correctedTheta += TWO_PI;
		 while (correctedTheta > TWO_PI)
			 correctedTheta -= TWO_PI;
		
		 // Determine phi iteration angle.
		 double phiIterationPadding = 1.26D; // Derived from testing.
		 double phiIterationAngle = Math.PI / (mapBoxHeight * phiIterationPadding);

		 // Determine phi range.
		 double phiPadding = 1.46D; // Derived from testing.
		 double phiRange = Math.PI * phiPadding * pixelHeight / mapBoxHeight;

		 // Determine starting and ending phi values.
		 double startPhi = centerPhi - (phiRange / 2D);
		 if (startPhi < 0D)
			 startPhi = 0D;
		 double endPhi = centerPhi + (phiRange / 2D);
		 if (endPhi > Math.PI)
			 endPhi = Math.PI;

		 double ratio = TWO_PI * pixelWidth / mapBoxWidth;
		 // Note : Polar cap phi values must display 2 PI theta range. 
		 // (derived from testing)
		 double polarCapRange = Math.PI / 6.54D; 
		 // Determine theta iteration angle.
		 double thetaIterationPadding = 1.46D; // Derived from testing.
		 // Theta padding, derived from testing.
		 double minThetaPadding = 1.02D; 
		 // Determine theta range.
		 double minThetaDisplay = ratio * minThetaPadding;
			
		 for (double x = startPhi; x <= endPhi; x += phiIterationAngle) {
			 
			 double thetaIterationAngle = TWO_PI / ((mapBoxWidth * Math.sin(x) * thetaIterationPadding) + 1D);

			 double thetaRange = ((1D - Math.sin(x)) * TWO_PI) + minThetaDisplay;
			
			 if ((x < polarCapRange) || (x > (Math.PI - polarCapRange)))
				thetaRange = TWO_PI;
			 if (thetaRange > TWO_PI)
				thetaRange = TWO_PI;

			 // Determine the theta starting and ending values.
			 double startTheta = centerTheta - (thetaRange / 2D);
			 double endTheta = centerTheta + (thetaRange / 2D);
			
			 for (double y = startTheta; y <= endTheta; y += thetaIterationAngle) {
			 
				 // Correct y value to make sure it is within bounds. (0 to 2PI)
				 double yCorrected = y;
				 while (yCorrected < 0)
					 yCorrected += TWO_PI;
				 while (yCorrected > TWO_PI)
					 yCorrected -= TWO_PI;
				 
				 int index = (int)x + (int)y * mapBoxWidth;
				 Point loc = findRectPosition(centerPhi, centerTheta, x, yCorrected, rho, halfWidth, halfWidth);
				 mapBoxArray[index] = loc;
				 
				 // Determine the display x and y coordinates for the pixel in the image.
				 int xx = pixelWidth - (int)loc.getX();
				 int yy = pixelHeight - (int)loc.getY();
				
				 // Check that the x and y coordinates are within the display area.
				 boolean leftBounds = xx >= 0;
				 boolean rightBounds = xx < pixelWidth;
				 boolean topBounds = yy >= 0;
				 boolean bottomBounds = yy < pixelHeight;
				 
				 if (leftBounds && rightBounds && topBounds && bottomBounds) {
					// Determine array index for the display location.
					int index1 = (pixelWidth - xx) + ((pixelHeight - yy) * pixelWidth);			
					// Put color in array at index.
					if ((index1 >= 0) && (index1 < mapArray.length))
						mapArray[index1] = getRGBColorInt(x, yCorrected);
				 }
			 }
		 }
	 }
		 
	/**
	 * Converts spherical coordinates to rectangular coordinates. Returns integer x
	 * and y display coordinates for spherical location.
	 * 
 	 * @Note: this method will be used by cpu1(). Retain for further debugging.
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
	
		final double col = newTheta + (- HALF_PI - oldTheta);
		final double xx = rho * Math.sin(newPhi);
		int x = ((int) Math.round(xx * Math.cos(col)) + half_map) - low_edge;
		int y = ((int) Math.round(((xx * (0D - Math.cos(oldPhi))) * Math.sin(col))
				+ (rho * Math.cos(newPhi) * (0D - Math.sin(oldPhi)))) + half_map) - low_edge;
		return new Point(x, y);
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
	 
	 /**
      * Converts linear rectangular XY position change to spherical coordinates with
      * rho value for map.
      *
      * @param x              change in x value (# of pixels)
      * @param y              change in y value (# of pixels)
      * @param phi			  center phi value (radians)
      * @param theta		  center theta value (radians)
      * @param rho            radius (in km) or map box height divided by pi (# of pixels)
      * @return a point2d of phi and theta
      */
	 public static Point2D convertRectIntToSpherical(int x, int y, double phi, double theta, double rho) {
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
	 
 	/**
 	 * Gets the RGB map color as an integer at a given location.
 	 * 
 	 * @param phi   the phi location.
 	 * @param theta the theta location.
 	 * @return the RGB map color as an integer.
 	 */
	@Override
 	public int getRGBColorInt(double phi, double theta) {
		
		 boolean invalid = Double.isNaN(phi) || Double.isInfinite(phi) || Double.isNaN(theta) || Double.isInfinite(theta) ;
		 if (invalid) {
			 // Set the color to black
			 return 0;
		 }
	
 		// Make sure phi is between 0 and PI.
 		while (phi > Math.PI)
 			phi -= Math.PI;
 		while (phi < 0)
 			phi += Math.PI;

 		// Adjust theta with PI for the map offset.
 		// Note: the center of the map is when theta = 0
 		// Make sure theta is between 0 and 2 PI.
 		while (theta > TWO_PI)
 			theta -= TWO_PI;
 		while (theta < 0)
 			theta += TWO_PI;

 		int row = (int) Math.round(phi * (colorPixels.length / Math.PI));
 		if (row > colorPixels.length - 1)
 	 		row--;
 			
 		int column = (int) Math.round(theta * (colorPixels[0].length / TWO_PI));
 		if (column > colorPixels[0].length - 1)
 			column--;
 		
 		return colorPixels[row][column];
 	}
 	
	/**
	 * Gets the point for generating a mineral map. 
	 */	
 	public static Point2D getMapBoxPoint(int index) {
 		return mapBoxArray[index];
 	}
 	
 	
 	/**
 	 * Sets the value of GPU hardware accel.
 	 * 
 	 * @param value
 	 */
 	public static void setHardwareAccel(boolean value) {
 		hardwareAccel = value;
 	}
 	
	/**
	 * What is the setting to use hardware acceleration
	 * @return
	 */
	public static boolean isHardwareAccel() {
		return hardwareAccel;
	}

 	/**
 	 * Checks if GPU hardware is capable.
 	 * 
 	 * @return
 	 */
 	public static boolean isGPUAvailable() {
 		return isGPUCapable;
 	}
 	
	/**
	 * Has the data loaded
	 */
	@Override
	public MapState getStatus() {
		return loaded;
	}

	/**
	 * Prepares map panel for deletion.
	 */
	public void destroy() {
	 	colorPixels = null;
	 	meta = null;
		kernel = null;
	}
 }

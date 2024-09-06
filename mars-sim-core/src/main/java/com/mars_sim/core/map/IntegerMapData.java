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
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLProgram;
import com.mars_sim.core.map.common.FileLocator;

/**
 * For png image tiling, see https://github.com/leonbloy/pngj/wiki/Snippets
 */

 /**
  * A map that uses integer data stored in files to represent colors.
  */
 public class IntegerMapData implements MapData {

	// Static members.
 	private static Logger logger = Logger.getLogger(IntegerMapData.class.getName());

	public static final double HALF_MAP_ANGLE = 0.48587;
	
	public static final double QUARTER_HALF_MAP_ANGLE = HALF_MAP_ANGLE / 4;
	
 	private static final double TWO_PI = Math.PI * 2;
 	
 	private static final double HALF_PI = Math.PI / 2D;
 	
 	private static final String CL_FILE = "MapDataFast.cl";
 	
 	private static final String KERNEL_NAME = "getMapImage";
 	
	private static boolean HARDWARE_ACCELERATION = true;
	// The max rho
 	public static double MAX_RHO;
 	// The min rho
 	public static double MIN_RHO;
	// The default rho at the start of the sim
 	public static double RHO_DEFAULT;
 	// The max rho multiplier allowed
  	public static double MAX_RHO_MULTIPLIER = 5;
  	// The min rho fraction allowed
  	public static double MIN_RHO_FRACTION = 3;
  	/* The width of the map box image in pixels. */
	private static int mapWidth;
	
 	// Data members.
  	/* # of pixels in the width of the map image. */
	private int pixelWidth;
	/* # of pixels in the height of the map image. */
	private int pixelHeight;
	/* The radius of Mars in # of pixels (pixelHeight / PI). */
	private double rho;
	/* The base map color pixels double array. */
 	private int[][] colorPixels = new int[0][0];
	/* The array of points for generating mineral map in a mapbox. */	
 	private static Point2D[] mapBoxArray;
 	
 	/* The meta data of the map. */
	private MapMetaData meta;
 	/* The OpenCL program instance. */
	private CLProgram program;
 	/* The OpenCL kernel instance. */
	private CLKernel kernel;
 	
 	/**
 	 * Constructor.
 	 * 
	 * @param name   the name/description of the data
 	 * @param filename   the map data file name.
 	 * @throws IOException Problem loading map data
 	 */
 	IntegerMapData(MapMetaData mapMetaData, double rho) throws IOException {
		this.meta = mapMetaData;
		this.rho = rho;

		// Load data files
		String metaFile = mapMetaData.getFile();
		
		try {
			if (metaFile == null || metaFile.equals("")) {
				logger.log(Level.SEVERE, "Map file not found.");
				return;
			}
			else {
				loadMapData(metaFile);
			}
		} catch(Exception e) {
			logger.log(Level.SEVERE, "Unable to load map. " + e.getMessage());
			return;
		}
		
		RHO_DEFAULT = pixelHeight / Math.PI;
//		rho = RHO_DEFAULT;
		MAX_RHO = RHO_DEFAULT * MAX_RHO_MULTIPLIER;
		MIN_RHO = RHO_DEFAULT / MIN_RHO_FRACTION;

		logger.config("new IntegerMapData - rho : " + Math.round(rho *10.0)/10.0 + " RHO_DEFAULT : " + Math.round(RHO_DEFAULT *10.0)/10.0);
		
		// Exclude mac from use openCL
		if (System.getProperty("os.name").toLowerCase().contains("mac")) {
			HARDWARE_ACCELERATION = false;
		}
		else {
			setKernel();
		}
 	}
 	
 	/**
 	 * Sets up the JOCL kernel program.
 	 */
	private void setKernel() {
 
		try {
			program = getProgram(CL_FILE);
			kernel = getKernel(program, KERNEL_NAME)
					.setArg(11, (float) TWO_PI)
					.setArg(12, (float) getRho());
			logger.config("GPU OpenCL accel enabled.");
		} catch(Exception e) {
			HARDWARE_ACCELERATION = false;
			logger.log(Level.SEVERE, "Disabling GPU OpenCL accel when loading kernel. Exception caused by " + e.getMessage());
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
    public double getScale() {
    	return rho / RHO_DEFAULT;
    }
    
	/**
	 * Gets the rho of the Mars surface map.
	 * 
	 * @return
	 */
	@Override
	public double getRho() {
		return rho;
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
     * Gets the half angle of the Mars surface map.
     * 
     * @return
     */
    public double getHalfAngle() {
    	double ha = Math.sqrt(HALF_MAP_ANGLE / getScale() / (0.25 + meta.getResolution()));
    	return Math.min(Math.PI, ha);
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

	public static int getMapBoxWidth() {
		return mapWidth;
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
 	private void loadMapData(String imageName) throws IOException {

 		BufferedImage cylindricalMapImage = null;
 		
		try {
			cylindricalMapImage = 
			// Unable to get BigBufferedImage to work : BigBufferedImage.create(FileLocator.locateFile(MapDataFactory.MAPS_FOLDER + imageName), BufferedImage.TYPE_INT_ARGB); // TYPE_INT_RGB
				ImageIO.read(FileLocator.locateFile(MapDataFactory.MAPS_FOLDER + imageName));
			
			// Use getRaster() - the fastest
		    // See https://stackoverflow.com/questions/10954389/which-amongst-pixelgrabber-vs-getrgb-is-faster/12062932#12062932
			// See https://stackoverflow.com/questions/6524196/java-get-pixel-array-from-image
			
	 		pixelWidth = cylindricalMapImage.getWidth();
	 		pixelHeight = cylindricalMapImage.getHeight();
//	 		logger.config("loadMapData - " +  imageName + " : " + pixelWidth + " x " + pixelHeight + ".");
	 		
	 		final boolean hasAlphaChannel = cylindricalMapImage.getAlphaRaster() != null;
		
	 		colorPixels = new int[pixelHeight][pixelWidth];
	 		
	 		if (!meta.isColourful()) {
	 			
	 			// Note: May use the shade map to get height values
	 			
//	 			boolean done = false;
//	 			boolean alreadyWentToNextByte = false;
// 			    int byteIndex = 0;
// 			    int row = 0;
// 			    int col = 0;
// 			    int numBits = 0;
// 			    byte currentByte = pixels[byteIndex];
// 			    while (!done) {
// 			        alreadyWentToNextByte = false;
//	        
// 			        // See https://stackoverflow.com/questions/32804784/weird-rgb-value-from-java-bufferedimage-getrgb/32824569#32824569
// 			    	   
// 			        // Using 0x10101 mask will turn it into monochromic 0 or 1
// 			        
//			        // Use 0xff mask because byte is a signed type in Java, and we want an unsigned value
// 			        int grayValue = (currentByte & 0xff);        
//// 			        int monoValue = (currentByte & 0x80) >> 7;
//// 			        System.out.print(grayValue + " ");
// 			        
//	        		baseMapPixels[row][col] = grayValue;
//	        
// 			        currentByte = (byte) (((int) currentByte) << 1);
// 			        numBits++;
//
// 			        if ((row == pixelHeight - 1) && (col == pixelWidth - 1)) {
// 			            done = true;
// 			        }
// 			        else {
// 			            col++;
//
// 			            if (numBits == 8) {
// 			                currentByte = pixels[++byteIndex];
// 			                numBits = 0;
// 			                alreadyWentToNextByte = true;
// 			            }
//
// 			            if (col == pixelWidth) {
// 			                row++;
// 			                col = 0;
//
// 			                if (!alreadyWentToNextByte) {
// 			                    currentByte = pixels[++byteIndex];
// 			                    numBits = 0;
// 			                }
// 			            }
// 			        }
// 			    } 			
	 			// https://stackoverflow.com/questions/30951726/reading-a-grayscale-image-in-java		
	 			Raster raster = cylindricalMapImage.getData();
	 			int h = raster.getHeight();
	 			int w = raster.getWidth();
	 		    for (int i = 0; i < w; i++) {
	 		        for (int j = 0; j < h; j++) {
	 		        	colorPixels[j][i] = raster.getSample(i, j, 0);
	 		        }
	 		    }
	 		}
	 		
	 		else { 
	 			
				final byte[] pixels = ((DataBufferByte) cylindricalMapImage.getRaster().getDataBuffer()).getData();
//				May try final int[] pixels = ((DataBufferInt)cylindricalMapImage.getRaster().getDataBuffer()).getData();

	 			if (hasAlphaChannel) {
		 			// Note: 'Viking Geologic' and 'MOLA Shade' have alpha channel.
		 			logger.info("hasAlphaChannel: " + hasAlphaChannel);
		 					
		 			final int pixelLength = 4;
	 			
		 			// Note: int pos = (y * pixelLength * width) + (x * pixelLength);

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
		 				//  int blueMask = 0xFF0000, greenMask = 0xFF00, redMask = 0xFF;
		 				
		 				// See https://stackoverflow.com/questions/11380062/what-does-value-0xff-do-in-java
		 				// When applying '& 0xff', it would end up with the value ff ff ff fe instead of 00 00 00 fe. 
		 				// A further subtlety is that '&' is defined to operate only on int values. As a result, 
		 				//
		 				// 1. value is promoted to an int (ff ff ff fe).
		 				// 2. 0xff is an int literal (00 00 00 ff).
		 				// 3. The '&' is applied to yield the desired value for result.
	
		 				argb += (((int) pixels[pos] & 0xff) << 24); // alpha
		 				argb += ((int) pixels[pos + 1] & 0xff); // blue
		 				argb += (((int) pixels[pos + 2] & 0xff) << 8); // green
		 				argb += (((int) pixels[pos + 3] & 0xff) << 16); // red
		 				
	//	 				The Red and Blue channel comments are flipped. 
	//	 				Red should be +1 and blue should be +3 (or +0 and +2 respectively in the No Alpha code).
		 				
	//	 				You could also make a final int pixel_offset = hasAlpha?1:0; and 
	//	 				do ((int) pixels[pixel + pixel_offset + 1] & 0xff); // green; 
	//	 				and merge the two loops into one. – Tomáš Zato Mar 23 '15 at 23:02
		 						
		 				colorPixels[row][col] = argb;
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
		 					 				
		 				// Note: The color is a 32-bit integer in ARGB format. 
		 				//           Fully Opaque = 0xFF______
		 				//      Fully Transparent = 0x00______
		 				// Also,
		 				//	Red   = 0xFFFF0000
		 				//	Green = 0xFF00FF00
		 				//	Blue  = 0xFF0000FF
		 				//
		 				//  int blueMask = 0xFF0000, greenMask = 0xFF00, redMask = 0xFF;
		 				
		 				argb += -16777216; // 255 alpha
		 				argb += ((int) pixels[pixel] & 0xff); // blue
		 				argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
		 				argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
		 				
		 				colorPixels[row][col] = argb;
		 				col++;
		 				if (col == pixelWidth) {
		 					col = 0;
		 					row++;
		 				}
		 			}
		 		}
	 		}
	 		
		} catch (IOException e) {
			logger.severe("Can't read image file '" + imageName + "'.");
		}
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
 	public Image createMapImage(double centerPhi, double centerTheta, int mapBoxWidth, int mapBoxHeight, double newRho) {
	
 		mapWidth = mapBoxWidth;
 		mapBoxArray = new Point2D[mapBoxHeight * mapBoxWidth];
 		
		 boolean invalid = Double.isNaN(centerPhi) || Double.isNaN(centerTheta);
		 if (invalid) {
			 logger.log(Level.SEVERE, "centerPhi and/or centerTheta is invalid.");
			 return null;
		 }

		// Set the new map rho
		setRho(newRho);

 		// Create a new buffered image to draw the map on.
 		BufferedImage bImage // BigBufferedImage.create(mapBoxWidth, mapBoxHeight, BufferedImage.TYPE_INT_RGB);
			= new BufferedImage(mapBoxWidth, mapBoxHeight, BufferedImage.TYPE_INT_RGB); 
 		
 		// Not using TYPE_INT_ARGB or TYPE_4BYTE_ABGR
 				
 		// Note: it turns out TYPE_INT_RGB works the best for gray map

 		// Future: May experiment with BufferedImage.getSubimage(int x, int y, int w, int h);
 		
 		// Create an array of int RGB color values to create the map image from.
 		int[] mapArray = new int[mapBoxWidth * mapBoxHeight];
 
		if (HARDWARE_ACCELERATION) {
			try {
				gpu(centerPhi, centerTheta, mapBoxWidth, mapBoxHeight, mapArray);
			} catch(Exception e) {
				HARDWARE_ACCELERATION = false;
				logger.log(Level.SEVERE, "Disabling GPU OpenCL accel when running gpu(). Exception caused by " + e.getMessage());
			}
		}
		else {
			cpu0(centerPhi, centerTheta, mapBoxWidth, mapBoxHeight, mapArray);
		}

	 	// Create new map image.
	 	setRGB(bImage, 0, 0, mapBoxWidth, mapBoxHeight, mapArray, 0, mapBoxHeight);
 		
 		// If alpha value is 255, it is fully opaque.
 		//  A value of 1 would mean it is (almost) fully transparent.
 		// May try setAlpha((byte)127, result);
	
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
    public void setRGB(BufferedImage bImage, int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize) {

    	// Note: Reference https://stackoverflow.com/questions/61130264/how-can-i-process-bufferedimage-faster
    	//       when attempting to speed up the processing
    	
		if (!meta.isColourful()) {
	        // Convert to grayscale

			// Not working below (Retain for further debugging) :
//			// Create a lookup table for whitening
//			float[] lut = new float[256];
//			for (int i = 0; i < 256; i++) {
//			    lut[i] = (float) Math.min(Math.max(i / 255.0 * 2.0 + 0.5, 0.0), 1.0); // Adjust as needed
//			}
//			
//			// Apply the lookup table to the image
//			for (int y = 0; y < h; y++) {
//			    for (int x = 0; x < w; x++) {
//			        int p = bImage.getRGB(x, y);
//			        
//			        int a = (p >> 24) & 0xFF;
//			        int r = (p >> 16) & 0xFF;
//			        int g = (p >> 8) & 0xFF;
//			        int b = p & 0xFF;
//			        
//			        int part0 = (int) (lut[r] * 255) << 16;
//			        int part1 = (int) (lut[g] * 255) << 8;
//			        int part2 = (int) lut[b];
//			        int whitenedPixel = part0 | part1 | part2;
//			        bImage.setRGB(x, y, whitenedPixel | a << 24);
//			    }
//			}
			
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
	 
//	            int gray = (int) (r * 0.299 + g * 0.587 + b * 0.114); // Original grayscale calculation
//            	int enhancedGray = Math.min(255, p + 15); // Brightness enhancement (10-20)
            	
	            rgbArray[i] = p;
	        }
	        
	        bImage.setRGB(0, 0, w, h, rgbArray, 0, w);
	        
	        // Not working below (Retain for further debugging) :
//			for (int i = 0; i < h; i++) {
//			    for (int j = 0; j < w; j++) {
//			        Color c = new Color(bImage.getRGB(j, i));
////			        int red = (int) (c.getRed() * 0.299);
////			        int green = (int) (c.getGreen() * 0.587);
////			        int blue = (int) (c.getBlue() * 0.114);
//	        
//			        int gray = (int) (c.getRed() * 0.299 + c.getGreen() * 0.587 + c.getBlue() * 0.114); // Original grayscale calculation
//		            int enhancedGray = Math.min(255, gray + 15); // Brightness enhancement (10-20)
//		
//		            Color newColor = new Color(enhancedGray, enhancedGray, enhancedGray);
////			        Color newColor = new Color(red + green + blue, red + green + blue, red + green + blue);
//
//			        bImage.setRGB(j, i, newColor.getRGB());
//			    }
//			}
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
    
 	private int toRGB(int grayValue) {
// 	    int part = Math.round(value * 255);
 	    return grayValue * 0x10101;
 	}
 	
 	public void setAlpha(byte alpha, BufferedImage image) {       
 		// alpha is in 0-255 range
 		alpha %= 0xff; 
 	    for (int i = 0; i < image.getWidth(); i++) {          
 	        for (int j = 0; j < image.getHeight(); j++) {
 	        	int color = image.getRGB(i, j);

 	        	// According to Java API, the alpha value is at 24-31 bit.
 	            int mc = (alpha << 24) | 0x00ffffff; // shift blue to alpha
 	            int newcolor = color & mc;
 	            image.setRGB(i, j, newcolor);            
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
		 
		 // Set the rho this way to avoid global map artifact. Reason unknown.
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
				 .putArgs(colBuffer, rowBuffer)
//				 .putArg((float) getRho())
				 ;

		 getQueue().put1DRangeKernel(kernel, 0, globalSize, getLocalSize())
				 .putReadBuffer(rowBuffer, false)
				 .putReadBuffer(colBuffer, true);

		 int[] rows = new int[size];
		 rowBuffer.getBuffer().get(rows);
		 int[] cols = new int[size];
		 colBuffer.getBuffer().get(cols);
	 
		 for (int i = 0; i < size; i++) {
			 int x = rows[i];
			 int y = cols[i];
			 int index = (int)Math.round(x + (y * mapBoxWidth));
			 mapBoxArray[index] = new Point2D.Double(x, y);
			 mapArray[i] = colorPixels[x][y];
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
	 private void cpu0(double centerPhi, double centerTheta, int mapBoxWidth, int mapBoxHeight, int[] mapArray) {
		 int halfWidth = mapBoxWidth / 2;
		 int halfHeight = mapBoxHeight / 2;

		 for(int y = 0; y < mapBoxHeight; y++) {
			 for(int x = 0; x < mapBoxWidth; x++) {
				 int index = x + (y * mapBoxWidth);

				 Point2D loc = convertRectIntToSpherical(x - halfWidth, y - halfHeight, centerPhi, centerTheta, getRho());
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
	 private void cpu1(double centerPhi, double centerTheta, int mapBoxWidth, int mapBoxHeight, int[] mapArray) {
		 int halfWidth = mapBoxWidth / 2;
//		 int halfHeight = mapBoxHeight / 2;

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
			 
			 double thetaIterationAngle = TWO_PI / (((double) mapBoxWidth * Math.sin(x) * thetaIterationPadding) + 1D);

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
				 
				 int index = (int)Math.round(x + (y * mapBoxWidth));
				 Point loc = findRectPosition(centerPhi, centerTheta, x, yCorrected, getRho(), halfWidth, halfWidth);
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

 		int row = (int) Math.round(phi * ((double) colorPixels.length / Math.PI));
 		if (row > colorPixels.length - 1)
 	 		row--;
 			
 		int column = (int) Math.round(theta * ((double) colorPixels[0].length / TWO_PI));
 		if (column > colorPixels[0].length - 1)
 			column--;
 		
// 		int pixel = baseMapPixels[row][column];
// 		int pixelWithAlpha = (pixel >> 24) & 0xFF; 
// 		return pixelWithAlpha;
 		
 		return colorPixels[row][column];
 	}

// 	/**
// 	 * Gets the RGB map color as an integer at a given location.
// 	 * 
// 	 * @param phi   the phi location.
// 	 * @param theta the theta location.
// 	 * @return the RGB map color.
// 	 * @Override
//   *	public Color getRGBColor(double phi, double theta) {
// 	 *	    return new Color(getRGBColorInt(phi, theta));
// 	 *  }
// 	 */
	
 	public int[][] getPixels() {
 		return colorPixels;
 	}

 	
	/**
	 * Gets the point for generating a mineral map. 
	 */	
 	public static Point2D getMapBoxPoint(int index) {
 		return mapBoxArray[index];
 	}
 	
	/**
	 * Prepares map panel for deletion.
	 */
	public void destroy() {
	 	colorPixels = null;
	 	meta = null;
		program = null;
		kernel = null;
	}
 	
 }

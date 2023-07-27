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
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.mars.sim.mapdata.common.FileLocator;

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

 	private static final double TWO_PI = Math.PI * 2;
	// The default rho at the start of the sim
 	private final double RHO_DEFAULT;

 	// Data members.
 	private int[][] baseMapPixels = null;
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
		String metaMap = newMeta.getFile();
		
		baseMapPixels = loadMapData(metaMap);
		
		rho =  pixelHeight / Math.PI;
		RHO_DEFAULT = rho;
		MAX_RHO = RHO_DEFAULT * 6;
		MIN_RHO = RHO_DEFAULT / 6;
		
		logger.info("Loaded " + metaMap + " with pixels " + pixelWidth + " by " + pixelHeight + ".");
		
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
 		
 		int[][] mapPixels = null;
 		
		try {
			cylindricalMapImage = ImageIO.read(imageFile);
			
			// Use getRaster() is fastest
		    // See https://stackoverflow.com/questions/10954389/which-amongst-pixelgrabber-vs-getrgb-is-faster/12062932#12062932
			// See https://stackoverflow.com/questions/6524196/java-get-pixel-array-from-image
			
			final byte[] pixels = ((DataBufferByte) cylindricalMapImage.getRaster().getDataBuffer()).getData();
//	 		int[] srcPixels = ((DataBufferInt)cylindricalMapImage.getRaster().getDataBuffer()).getData();

	 		pixelWidth = cylindricalMapImage.getWidth();
	 		pixelHeight = cylindricalMapImage.getHeight();
	 		
	 		final boolean hasAlphaChannel = cylindricalMapImage.getAlphaRaster() != null;
		
	 		mapPixels = new int[pixelHeight][pixelWidth];
	 		
	 		if (hasAlphaChannel) {
	 			final int pixelLength = 4;
	 			
	 			// Note: int pos = (y * pixelLength * width) + (x * pixelLength);
	 			
	 			for (int pos = 0, row = 0, col = 0; pos + 3 < pixels.length; pos += pixelLength) {
	 				int argb = 0;
	 				
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
	 						
	 				mapPixels[row][col] = argb;
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
	 				
	 				mapPixels[row][col] = argb;
	 				col++;
	 				if (col == pixelWidth) {
	 					col = 0;
	 					row++;
	 				}
	 			}
	 		}
	 		
		} catch (IOException e) {
			logger.severe("Can't read image file: " + imageFile + ".");
		}

 		return mapPixels;
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
 				BufferedImage.TYPE_INT_ARGB);//.TYPE_INT_ARGB);//TYPE_4BYTE_ABGR);TYPE_INT_ARGB_PRE);

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
		
//		mapArray = result.getRGB(0, 0, mapBoxWidth, mapBoxHeight, mapArray, 0, mapBoxWidth);

 		// Create new map image.
 		result.setRGB(0, 0, mapBoxWidth, mapBoxHeight, mapArray, 0, mapBoxWidth);
 		
 		// If alpha value is 255, it is fully opaque.
 		//  A value of 1 would mean it is (almost) fully transparent.
// 		setAlpha((byte)127, result);
 		
 		mapImage = result;
		
 		return result;
 	}

 	
 	public void setAlpha(byte alpha, BufferedImage image) {       
 		// alpha is in 0-255 range
 		alpha %= 0xff; 
 	    for (int i = 0; i < image.getWidth(); i++) {          
 	        for (int j = 0; j < image.getHeight(); j++) {
 	        	int color = image.getRGB(i, j);

 	        	// According to Java API, the alpha value is at 24-31 bit.
 	            int mc = (alpha << 24) | 0x00ffffff; // shift blue tp alpha
 	            int newcolor = color & mc;
 	            image.setRGB(i, j, newcolor);            
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
			 mapArray[i] = baseMapPixels[rows[i]][cols[i]];
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
// 		if (theta > Math.PI)
// 			theta -= Math.PI;
// 		else if (theta < -Math.PI)
// 			theta += Math.PI;

 		// Make sure theta is between 0 and 2 PI.
 		while (theta > TWO_PI)
 			theta -= TWO_PI;
 		while (theta < 0)
 			theta += TWO_PI;

 		int row = (int) Math.round(phi * ((double) baseMapPixels.length / Math.PI));
 		if (row >= baseMapPixels.length - 1)
 			row = baseMapPixels.length - 1;

 		int column = (int) Math.round(theta * ((double) baseMapPixels[0].length / TWO_PI));
 		if (column >= baseMapPixels[0].length - 1)
 			column = baseMapPixels[0].length - 1;
 		
// 		int pixel = baseMapPixels[row][column];
// 		int pixelWithAlpha = (pixel >> 24) & 0xFF; 
// 		return pixelWithAlpha;
 		
 		return baseMapPixels[row][column];
 	}

// 	/**
// 	 * Gets the RGB map color as an integer at a given location.
// 	 * 
// 	 * @param phi   the phi location.
// 	 * @param theta the theta location.
// 	 * @return the RGB map color.
// 	 */
// 	@Override
// 	public Color getRGBColor(double phi, double theta) {
// 		return new Color(getRGBColorInt(phi, theta));
// 	}
// 	
 	public BufferedImage getCylindricalMapImage() {
 		return cylindricalMapImage;
 	}
 	
 	public int[][] getPixels() {
 		return baseMapPixels;
 	}

	/**
	 * Prepares map panel for deletion.
	 */
	public void destroy() {
	 	baseMapPixels = null;
	 	meta = null;
		cylindricalMapImage = null;
		program = null;
		kernel = null;
	}
 	
 }

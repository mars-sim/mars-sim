/**
 * Mars Simulation Project
 * MarsGlobe.java
 * @version 3.1.0 2018-07-23
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.navigator;

import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.ImageObserver;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.ui.swing.ImageLoader;

/**
 * The MarsGlobe class generates the Martian globe for the
 * GlobeDisplay object. It can center the globe at any set of
 * coordinates.
 */
public class MarsGlobe {

	/** default logger. */
	private static Logger logger = Logger.getLogger(MarsGlobe.class.getName());

	// Constant data members
	/** Height of map source image (pixels). */
	//private final static int map_height = 150;
	public final static int map_height = 300;//GlobeDisplay.GLOBE_BOX_HEIGHT;
	/** Width of map source image (pixels). */
	public final static int map_width = map_height * 2 ;

	private final double PI_half = Math.PI / 2D;
	private final double PI_double = Math.PI * 2D;
	
	private double rho = map_height / Math.PI;
	private double col_array_modifier = 1D / PI_double;
	private int half_map = map_height / 2;
	
	// Data members
	/** Center position of globe. */
	private Coordinates centerCoords;
	/** point colors in variably-sized vectors. */
	private Vector<Integer>[] sphereColor;
	//	/** "surface" or "topo" */
	//	private String globeType;
	/** cylindrical map image. */
	private Image marsMap;
	/** finished image of sphere with transparency. */
	private Image globeImage;
	/** true when image is done. */
	private boolean imageDone;
	/** parent display area. */
	private JComponent displayArea;

	/**
	 * Constructs a MarsGlobe object
	 * @param globeType the type of globe: "surface" or "topo"
	 * @param displayArea the display component for the globe
	 */
	@SuppressWarnings("unchecked")
	public MarsGlobe (MarsGlobeType globeType, JComponent displayArea) {

		// Initialize Variables
		// this.globeType = globeType;
		this.displayArea = displayArea;
		sphereColor = new Vector[map_height];
		centerCoords = new Coordinates(Math.PI / 2, Math.PI / 2);

		// Load Surface Map Image, which is now part of the globe enum
		String imageName = globeType.getPath();
/*		switch (globeType) {
			case SURFACE : imageName = "SurfaceMarsMapSmall.jpg"; break;
			case TOPO : imageName = "TopoMarsMapSmall.jpg"; break;
			default : imageName = "";
		}
*/
		MediaTracker mtrack = new MediaTracker(displayArea);
		marsMap = ImageLoader.getImage(imageName);
		mtrack.addImage(marsMap, 0);
		try {
			mtrack.waitForAll();
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE,Msg.getString("MarsGlobe.log.mediaTrackerError", e.toString())); //$NON-NLS-1$
		}

		// Prepare Sphere
		setup_sphere();
	}

	/** Creates a Sphere Image at given center point
	 *  @param newCenter new center location
	 */
	public synchronized void drawSphere(Coordinates newCenter) {
		//System.out.println("drawSphere()");
		// Adjust coordinates
		Coordinates adjNewCenter =
				new Coordinates(newCenter.getPhi(), newCenter.getTheta() + Math.PI);

		// If current center point equals new center point, don't recreate sphere
		if (centerCoords.equals(adjNewCenter)) {
			return;
		}

		// Initialize variables
		imageDone = false;

		centerCoords.setCoords(adjNewCenter);

		//double PI_half = Math.PI / 2D;
		//double PI_double = Math.PI * 2D;

		double end_row = centerCoords.getPhi() - PI_half;
		double start_row = end_row + Math.PI;
		double row_iterate;
		boolean north;

		// Determine if sphere should be created from north-south, or from south-north
		if (centerCoords.getPhi() <= PI_half) {
			north = true;
			end_row = centerCoords.getPhi() - PI_half;
			start_row = end_row + Math.PI;
			row_iterate = 0D - (Math.PI / (double) map_height);
		} else {
			north = false;
			start_row = centerCoords.getPhi() - PI_half;
			end_row = start_row + Math.PI;
			row_iterate = (Math.PI / (double) map_height);
		}

		// More variable initializations
		double col_correction = -PI_half - centerCoords.getTheta();
		//double rho = map_height / Math.PI;
		double sin_offset = Math.sin(centerCoords.getPhi() + Math.PI);
		double cos_offset = Math.cos(centerCoords.getPhi() + Math.PI);
		//double col_array_modifier = 1D / PI_double;
		//int half_map = map_height / 2;

		// Create array to hold image
		int[] buffer_array = new int[map_height * map_height];

		// Go through each row of the sphere
		for (double row = start_row; (((north) && (row >= end_row)) ||
				((!north) && (row <= end_row))); row += row_iterate) {
			if (row < 0)
				continue;
			if (row >= Math.PI)
				continue;
			int array_y = (int) Math.round(((double) map_height * row) / Math.PI);
			if (array_y >= map_height)
				continue;

			// Determine circumference of this row
			int circum = sphereColor[array_y].size();
			double row_cos = Math.cos(row);

			// Determine visible boundry of row
			double col_boundry = Math.PI;
			if (centerCoords.getPhi() <= PI_half) {
				if ((row >= PI_half * Math.cos(centerCoords.getPhi())) &&
						(row < PI_half)) {
					col_boundry = PI_half * (1D + row_cos);
				} else if (row >= PI_half) {
					col_boundry = PI_half;
				}
			} else {
				if ((row <= PI_half * Math.cos(centerCoords.getPhi())) &&
						(row > PI_half)) {
					col_boundry = PI_half * (1D - row_cos);
				} else if (row <= PI_half) {
					col_boundry = PI_half;
				}
			}
			if (centerCoords.getPhi() == PI_half) {
				col_boundry = PI_half;
			}

			double col_iterate = Math.PI / (double) circum;

			// Error adjustment for theta center close to PI_half
			double error_correction = centerCoords.getPhi() - PI_half;
			
			if (error_correction > 0D) {
				if (error_correction < row_iterate) {
					col_boundry = PI_half;
				}
			} else if (error_correction > 0D - row_iterate) {
				col_boundry = PI_half;
			}

			// Determine column starting and stopping points for row
			double start_col = centerCoords.getTheta() - col_boundry;
			double end_col = centerCoords.getTheta() + col_boundry;
			if (col_boundry == Math.PI)
				end_col -= col_iterate;

			double temp_buff_x = rho * Math.sin(row);
			double temp_buff_y1 = temp_buff_x * cos_offset;
			double temp_buff_y2 = rho * row_cos * sin_offset;

			double col_array_modifier2 = col_array_modifier * circum;

			// Go through each column in row
			for (double col = start_col; col <= end_col; col += col_iterate) {
				int array_x = (int)(col_array_modifier2 * col);

				if (array_x < 0) {
					array_x += circum;
				} else if (array_x >= circum) {
					array_x -= circum;
				}

				double temp_col = col + col_correction;

				// Determine x and y position of point on image
				int buff_x = (int) Math.round(temp_buff_x * Math.cos(temp_col)) +
						half_map;
				int buff_y = (int) Math.round((temp_buff_y1 * Math.sin(temp_col)) +
						temp_buff_y2) + half_map;

				// Put point in buffer array
				buffer_array[buff_x + (map_height * buff_y)] = sphereColor[array_y].elementAt(array_x);
				//buffer_array[buff_x + (map_height * buff_y)] = 0xFFFFFFFF; // if in gray scale
			}
		}

		// Create image out of buffer array
		globeImage = displayArea.createImage(
				new MemoryImageSource(map_height, map_height, buffer_array, 0,
						map_height));

		MediaTracker mt = new MediaTracker(displayArea);
		mt.addImage(globeImage, 0);
		//System.out.println("mt.addImage(globeImage, 0)");
		try {
			mt.waitForID(0);
			// Indicate that image is complete
			imageDone = true;
			//System.out.println("mt.waitForID(0)");
			
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE,Msg.getString("MarsGlobe.log.mediaTrackerError", e.toString())); //$NON-NLS-1$
		}


	}

	/** Returns globe image
	 *  @return globe image
	 */
	public Image getGlobeImage() {
		return globeImage;
	}

	/** Sets up Points and Colors for Sphere */
	private void setup_sphere() {

		// Initialize variables
		int row, col_num, map_col;
		double phi, theta;
		double circum, offset;
		double ih_d = (double) map_height;

		// Initialize color arrays
		int[] pixels_color = new int[map_height * map_width];
		int[][] map_pixels = new int[map_width][map_height];

		// Grab mars_surface image into pixels_color array using PixelGrabber
		PixelGrabber pg_color = new PixelGrabber(marsMap, 0, 0, map_width, map_height,
				pixels_color, 0, map_width);
		try {
			pg_color.grabPixels();
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE,Msg.getString("MarsGlobe.log.grabberError") + e); //$NON-NLS-1$
		}
		if ((pg_color.status() & ImageObserver.ABORT) != 0)
			logger.info(Msg.getString("MarsGlobe.log.grabberError")); //$NON-NLS-1$

		// Transfer contents of 1-dimensional pixels_color into 2-dimensional map_pixels
		for (int x = 0; x < map_width; x++)
			for (int y = 0; y < map_height; y++)
				map_pixels[x][y] = pixels_color[x + (y * map_width)];

		// Initialize variables
		//rho = map_height / Math.PI;
		offset = Math.PI / (2 * ih_d);

		// Go through each row and create Sphere_Color vector with it
		for (phi = offset; phi < Math.PI; phi += (Math.PI / ih_d)) {
			row = (int) Math.floor((phi / Math.PI) * ih_d);
			circum = 2 * Math.PI * (rho * Math.sin(phi));
			col_num = (int) Math.round(circum);
			sphereColor[row] = new Vector<Integer>(col_num);

			// Fill vector with colors
			for (theta = 0; theta < (2 * Math.PI);
					theta += ((Math.PI * 2) / circum)) {
				if (theta == 0) {
					map_col = 0;
				} else {
					map_col = (int) Math.floor((theta / Math.PI) * ih_d);
				}

				sphereColor[row].addElement(map_pixels[map_col][row]);
			}
		}
	}

	/** determines if a requested sphere is complete
	 *  @return true if image is done
	 */
	public boolean isImageDone() {
		return imageDone;
	}
	
	/**
	 * Prepare globe for deletion.
	 */
	public void destroy() {

		centerCoords = null;
		sphereColor = null;
		marsMap = null;
		globeImage = null;
		displayArea= null;
	}

}
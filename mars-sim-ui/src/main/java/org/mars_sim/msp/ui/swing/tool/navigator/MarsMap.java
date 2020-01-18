/**
 * Mars Simulation Project
 * MarsMap.java
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
import org.mars_sim.msp.core.tool.MoreMath;
import org.mars_sim.msp.ui.swing.ImageLoader;

/**
 * The MarsMap class generates the surface, the topographical and 
 * the geological map for GlobeDisplay.
 */

public class MarsMap {

	/** default logger. */
	private static Logger logger = Logger.getLogger(MarsMap.class.getName());

	// Constant data members
	/** Height of map source image (pixels). */
	// private final static int map_height = 150;
	public final static int MAP_H = NavigatorWindow.HORIZONTAL_SURFACE_MAP; //HORIZONTAL_LEFT_HALF; //
	/** Width of map source image (pixels). */
	public final static int MAP_W = MAP_H * 2;

	private final double PI_half = Math.PI / 2D;
	private final double PI_double = Math.PI * 2D;

	private double rho = MAP_H / Math.PI;
	private double col_array_modifier = 1D / PI_double;
	private int half_map_height = MAP_H / 2;

	// Data members
	/** Center position of globe. */
	private Coordinates centerCoords;
	/** point colors in variably-sized vectors. */
	@SuppressWarnings("unchecked")
	private Vector<Integer>[] sphereColor = new Vector[MAP_H];
	/** cylindrical map image. */
	private Image marsMap;
	/** finished image of sphere with transparency. */
	private Image globeImage;
	/** true when image is done. */
	private boolean imageDone;
	/** parent display area. */
	private JComponent displayArea;

	/**
	 * Constructs a MarsMap object
	 * 
	 * @param globeType   the type of globe: surface, topo or geo
	 * @param displayArea the display component for the map
	 */
	public MarsMap(MarsMapType globeType, JComponent displayArea) {

		// Initialize Variables
		// this.globeType = globeType;
		this.displayArea = displayArea;
		centerCoords = new Coordinates(PI_half, PI_half);

		// Load Surface Map Image, which is now part of the globe enum
		String imageName = globeType.getPath();

		MediaTracker mtrack = new MediaTracker(displayArea);
		marsMap = ImageLoader.getImage(imageName);
		mtrack.addImage(marsMap, 0);
		try {
			mtrack.waitForAll();
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, Msg.getString("MarsMap.log.mediaTrackerError", e.toString())); //$NON-NLS-1$
		}

		// Prepare Sphere
		setupSphere();
	}

	/**
	 * Creates a Sphere Image at given center point
	 * 
	 * @param newCenter new center location
	 */
	public synchronized void drawSphere(Coordinates newCenter) {
		// Adjust coordinates
		Coordinates adjNewCenter = new Coordinates(newCenter.getPhi(), newCenter.getTheta() + Math.PI);

		// If current center point equals new center point, don't recreate sphere
		if (centerCoords.equals(adjNewCenter)) {
			return;
		}

		// Initialize variables
		imageDone = false;

		centerCoords.setCoords(adjNewCenter);

		// double PI_half = Math.PI / 2D;
		// double PI_double = Math.PI * 2D;
		double phi = centerCoords.getPhi();
		double theta = centerCoords.getTheta();
		double end_row = phi - PI_half;
		double start_row = end_row + Math.PI;
		double row_iterate;
		boolean north;

		// Determine if sphere should be created from north-south, or from south-north
		if (phi <= PI_half) {
			north = true;
			end_row = phi - PI_half;
			start_row = end_row + Math.PI;
			row_iterate = 0D - (Math.PI / (double) MAP_H);
		} else {
			north = false;
			start_row = phi - PI_half;
			end_row = start_row + Math.PI;
			row_iterate = (Math.PI / (double) MAP_H);
		}

		// More variable initializations
		double col_correction = -PI_half - theta;
		// double rho = map_height / Math.PI;
		double sin_offset = MoreMath.sin(phi + Math.PI);
		double cos_offset = MoreMath.cos(phi + Math.PI);
		// double col_array_modifier = 1D / PI_double;
		// int half_map = map_height / 2;

		// Create array to hold image
		int[] buffer_array = new int[MAP_H * MAP_H];

		// Go through each row of the sphere
		for (double row = start_row; (((north) && (row >= end_row))
				|| ((!north) && (row <= end_row))); row += row_iterate) {
			if (row < 0)
				continue;
			if (row >= Math.PI)
				continue;
			int array_y = (int) Math.round(((double) MAP_H * row) / Math.PI);
			if (array_y >= MAP_H)
				continue;

			// Determine circumference of this row
			int circum = sphereColor[array_y].size();
			double row_cos =  MoreMath.cos(row);

			// Determine visible boundry of row
			double col_boundry = Math.PI;
			if (phi <= PI_half) {
				if ((row >= PI_half *  MoreMath.cos(phi)) && (row < PI_half)) {
					col_boundry = PI_half * (1D + row_cos);
				} else if (row >= PI_half) {
					col_boundry = PI_half;
				}
			} else {
				if ((row <= PI_half *  MoreMath.cos(phi)) && (row > PI_half)) {
					col_boundry = PI_half * (1D - row_cos);
				} else if (row <= PI_half) {
					col_boundry = PI_half;
				}
			}
			if (phi == PI_half) {
				col_boundry = PI_half;
			}

			double col_iterate = Math.PI / (double) circum;

			// Error adjustment for theta center close to PI_half
			double error_correction = phi - PI_half;

			if (error_correction > 0D) {
				if (error_correction < row_iterate) {
					col_boundry = PI_half;
				}
			} else if (error_correction > 0D - row_iterate) {
				col_boundry = PI_half;
			}

			// Determine column starting and stopping points for row
			double start_col = theta - col_boundry;
			double end_col = theta + col_boundry;
			if (col_boundry == Math.PI)
				end_col -= col_iterate;

			double temp_buff_x = rho * MoreMath.sin(row);
			double temp_buff_y1 = temp_buff_x * cos_offset;
			double temp_buff_y2 = rho * row_cos * sin_offset;

			double col_array_modifier2 = col_array_modifier * circum;

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
				int buff_x = (int) Math.round(temp_buff_x *  MoreMath.cos(temp_col)) + half_map_height;
				int buff_y = (int) Math.round((temp_buff_y1 * MoreMath.sin(temp_col)) + temp_buff_y2) + half_map_height;

				// Put point in buffer array
				buffer_array[buff_x + (MAP_H * buff_y)] = (int) sphereColor[array_y].elementAt(array_x);
				// buffer_array[buff_x + (map_height * buff_y)] = 0xFFFFFFFF; // if in gray
				// scale
			}
		}

		// Create image out of buffer array
		globeImage = displayArea
				.createImage(new MemoryImageSource(MAP_H, MAP_H, buffer_array, 0, MAP_H));

		MediaTracker mt = new MediaTracker(displayArea);
		mt.addImage(globeImage, 0);
		// System.out.println("mt.addImage(globeImage, 0)");
		try {
			mt.waitForID(0);
			// Indicate that image is complete
			imageDone = true;
			// System.out.println("mt.waitForID(0)");

		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, Msg.getString("MarsMap.log.mediaTrackerError", e.toString())); //$NON-NLS-1$
		}

	}

	/**
	 * Returns globe image
	 * 
	 * @return globe image
	 */
	public Image getGlobeImage() {
		return globeImage;
	}

	/** Sets up Points and Colors for Sphere */
	private void setupSphere() {

		// Initialize variables
		int row, col_num, map_col;
		double phi, theta;
		double circum, offset;
		double ih_d = (double) MAP_H;

		// Initialize color arrays
		int[] pixels_color = new int[MAP_H * MAP_W];
		int[][] map_pixels = new int[MAP_W][MAP_H];

		// Grab mars_surface image into pixels_color array using PixelGrabber
		PixelGrabber pg_color = new PixelGrabber(marsMap, 0, 0, MAP_W, MAP_H, pixels_color, 0, MAP_W);
		try {
			pg_color.grabPixels();
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, Msg.getString("MarsMap.log.grabberError") + e); //$NON-NLS-1$
		}
		if ((pg_color.status() & ImageObserver.ABORT) != 0)
			logger.info(Msg.getString("MarsMap.log.grabberError")); //$NON-NLS-1$

		// Transfer contents of 1-dimensional pixels_color into 2-dimensional map_pixels
		for (int x = 0; x < MAP_W; x++)
			for (int y = 0; y < MAP_H; y++)
				map_pixels[x][y] = pixels_color[x + (y * MAP_W)];

		// Initialize variables
		// rho = map_height / Math.PI;
		offset = Math.PI / (2 * ih_d);

		// Go through each row and create Sphere_Color vector with it
		for (phi = offset; phi < Math.PI; phi += (Math.PI / ih_d)) {
			row = MoreMath.floor((float) ((phi / Math.PI) * ih_d));//(int) Math.floor((phi / Math.PI) * ih_d);
			circum = 2 * Math.PI * (rho * MoreMath.sin(phi));
			col_num = (int) Math.round(circum);
			sphereColor[row] = new Vector<Integer>(col_num);

			// Fill vector with colors
			for (theta = 0; theta < (2 * Math.PI); theta += ((Math.PI * 2) / circum)) {
				if (theta == 0) {
					map_col = 0;
				} else {
					map_col = MoreMath.floor((float)((theta / Math.PI) * ih_d));
				}

				sphereColor[row].addElement(map_pixels[map_col][row]);
			}
		}
	}

	/**
	 * determines if a requested sphere is complete
	 * 
	 * @return true if image is done
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
		displayArea = null;
		imageDone = true;
	}
	

}
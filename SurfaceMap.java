//******************************** Surface Map Generator ********************************
// Last Modified: 2/23/00

// The SurfaceMap class generates a 300x300 pixel map of the surface of Mars 
// for the MapDisplay object.
// It can center the map at any set of coordinates.

import java.awt.*;
import java.awt.image.*;
import java.io.*; 
import java.util.*;
import javax.swing.*;

public class SurfaceMap {

	// Constant data members

	private final static int map_height = 1440;          // Height of source map in pixels.
	private final static int map_width = map_height * 2; // Width of source map in pixels.
	
	// Data members
	
	private String mapType;                   // Either "surface" or "topo"
	private Image mapImage;                   // Finished image of sphere
	private Coordinates centerCoords;         // Center coordinates
	public boolean image_done;                // True if image is complete
	private JComponent displayArea;           // Parent display area
	private int[] index;                      // Map index information
	private long[] sum;                       // Map sum information

	// Constructor

	public SurfaceMap(String mapType, JComponent displayArea) {

		// Initialize data members

		this.mapType = new String(mapType);
		this.displayArea = displayArea;
		image_done = false;
		centerCoords = new Coordinates(0D, 0D);
		
		try {
			// Load index array
		
			BufferedInputStream indexBuff = new BufferedInputStream(new FileInputStream("TopoMarsMap.index"));
			DataInputStream indexReader = new DataInputStream(indexBuff);
			index = new int[1440];
			for (int x=0; x < 1440; x++) index[x] = indexReader.readInt();
			indexReader.close();
			indexBuff.close();
			
			// Load sum array
			
			BufferedInputStream sumBuff = new BufferedInputStream(new FileInputStream("TopoMarsMap.sum"));
			DataInputStream sumReader = new DataInputStream(sumBuff);
			sum = new long[1440];
			for (int x=0; x < 1440; x++) sum[x] = sumReader.readLong();
			sumReader.close();
			sumBuff.close();
		}
		catch(IOException e) { System.out.println(e.toString()); }
	}

	// Creates a map at a given center point
	
	public synchronized void drawMap(Coordinates newCenter) {

		// Adjust coordinates

		Coordinates adjNewCenter = new Coordinates(newCenter.getPhi(), newCenter.getTheta() + Math.PI);

		// If current center point equals new center point, don't recreate sphere

		if (centerCoords.equals(adjNewCenter)) return;

		// Initialize Variables

		image_done = false;

		centerCoords.setCoords(adjNewCenter);

		double PI_half = Math.PI / 2D;
		double PI_double = Math.PI * 2D;
		double PI_piece = Math.PI * (.153D - (.04D * Math.sin(centerCoords.getPhi())));

		double col_correction = (Math.PI / -2D) - centerCoords.getTheta();
		double rho = map_height / Math.PI;
		double sin_offset = Math.sin(centerCoords.getPhi() + Math.PI);
		double cos_offset = Math.cos(centerCoords.getPhi() + Math.PI); 
		double col_array_modifier = 1D / PI_double;

		int circum = 0;
		int half_map = map_height / 2;
		int low_edge = half_map - 150;
		int high_edge = half_map + 150;

		int[] buffer_array = new int[300 * 300];
		byte[] line_data = new byte[map_width * 3];
		int array_x_old = 0;
		int array_y_old = 0;
		long summer;

		try {

			String mapName;

			if (mapType.equals("surface")) mapName = new String("SurfaceMarsMap");
			else mapName = new String("TopoMarsMap");

			// Open map file

			RandomAccessFile map = new RandomAccessFile(mapName + ".dat", "r");

			// Initialize row variables

			double start_row = centerCoords.getPhi() - PI_piece;
			double end_row = centerCoords.getPhi() + PI_piece;
			double row_iterate = (double) (Math.PI / map_height);

			boolean row_flag = false;
			boolean row_iterate_flag = true;

			// Go through each row

			for (double row = start_row; row <= end_row; row += row_iterate) {
				if (row < 0) {continue;}
				if (row >= Math.PI) {continue;}
				int array_y = (int) Math.round((double)(map_height * row) / Math.PI);
				if (array_y < 0) {continue;}
				if (array_y >= map_height) {continue;}

				// If starting row, read row info from files

				if (row_flag == false) {
					array_y_old = array_y;
					circum = index[array_y];
					summer = sum[array_y];
					map.seek((long)(summer * 3));
					map.read(line_data, 0, (circum * 3));
					row_iterate_flag = true;
				}

				// If new row, read row info from files

				if (array_y != array_y_old) {
					circum = index[array_y];
					map.read(line_data, 0, (circum * 3));
					row_iterate_flag = true;
				}
				else { 
					if (row_flag == false) row_flag = true; 
					else row_iterate_flag = false; 
				}
				array_y_old = array_y;

				// Initialize row variables

				double temp_buff_x = rho * Math.sin(row);
				double temp_buff_y1 = temp_buff_x * cos_offset;
				double temp_buff_y2 = rho * Math.cos(row) * sin_offset;
				double col_array_modifier2 = (col_array_modifier * circum);

				// Determine displayable boundries for row

				double col_boundry = Math.PI * (1.42D - (1.29D * Math.sin(row)));
				if (col_boundry > Math.PI) col_boundry = Math.PI;

				if ((centerCoords.getPhi() > Math.PI / 5D) && (centerCoords.getPhi() < Math.PI - (Math.PI / 5D))) 
					col_boundry -= Math.PI - (Math.PI * Math.sin(row));
				else if ((centerCoords.getPhi() > Math.PI / 8D) && (centerCoords.getPhi() < Math.PI - (Math.PI / 8D)))
					col_boundry -= (.75D * Math.PI) - (.75D * Math.PI * Math.sin(row));

				// Determine row starting and stopping points

				double start_col = centerCoords.getTheta() - col_boundry;
				double end_col = centerCoords.getTheta() + col_boundry;
				double col_iterate = Math.PI / (double) circum;

				boolean col_flag = false;

				// Go through each column

				for (double col = start_col; col <= end_col; col += col_iterate) {
					int array_x = (int) Math.round(col_array_modifier2 * col);
					while (array_x < 0) {array_x += circum;}
					while (array_x >= circum) {array_x -= circum;}

					double temp_col = col + col_correction;

					// Determine position of point, and put in buffer if in display area

					int buff_x = (int) Math.round(temp_buff_x * Math.cos(temp_col)) + half_map;
					if ((buff_x > low_edge) && (buff_x < high_edge)) {
						int buff_y = (int) Math.round((temp_buff_y1 * Math.sin(temp_col)) + temp_buff_y2) + half_map;
						if ((buff_y > low_edge) && (buff_y < high_edge)) {
							buff_x -= low_edge;
							buff_y -= low_edge;

							int position = array_x * 3;

							// Get color from line_data

							int bit1 = (int) line_data[position];
							bit1 <<= 16;
							bit1 &= 0x00FF0000;
							int bit2 = (int) line_data[position + 1];
							bit2 <<= 8;
							bit2 &= 0x0000FF00;
							int bit3 = (int) line_data[position + 2];
							bit3 &= 0x000000FF;

							// Put color at point in buffer

							buffer_array[buff_x + (300 * buff_y)] = 0xFF000000 | bit1 | bit2 | bit3;
						}
					}
				}
			}

			// Close map file

			map.close();
		}
		catch(IOException e) { System.out.println("File read error: " + e); }	 

		// Create image from buffer array

		mapImage = displayArea.createImage(new MemoryImageSource(300, 300, buffer_array, 0, 300));		
			
		MediaTracker mt = new MediaTracker(displayArea);
		mt.addImage(mapImage, 0);
		try { mt.waitForID(0); }
		catch (InterruptedException e) { System.out.println("Media Tracker Error " + e); }
		image_done = true;
	}
	
	// Returns map image

	public Image getMapImage() { return mapImage; }
}


// Mars Simulation Project
// Copyright (C) 1999 Scott Davis
//
// For questions or comments on this project, contact:
//
// Scott Davis
// 1725 W. Timber Ridge Ln. #6206
// Oak Creek, WI  53154
// scud1@execpc.com
// http://www.execpc.com/~scud1/
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
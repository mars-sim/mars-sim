/**
 * Mars Simulation Project
 * CannedMarsMap.java
 * @version 2.75 2003-08-03
 * @author Greg Whelan
 */

package org.mars_sim.msp.ui.standard.tool.navigator;

import org.mars_sim.msp.simulation.Coordinates;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;

/** 
 * The CannedMarsMap class reads in data from files on the local
 * filesystem in order to generate a map of the surface of Mars for
 * the MapDisplay object (that's the main display of the surface).
 */
public abstract class CannedMarsMap implements Map {

    // Data members
    private Image mapImage; // Finished image of sphere
    private Coordinates centerCoords; // Center coordinates
    private boolean imageDone; // True if image is complete
    private JComponent displayArea; // Parent display area
    // <fragile>
    private static int[] index; // Map index information
    private static long[] sum; // Map sum information
    // </fragile>
    // why is this fragile? <Scott>
    private int viewHeight = 300;
    private int viewWidth = 300;

    // constants
    protected final static int mapHeight = 1440; // Height of source map in pixels.
    protected final static int mapWidth = mapHeight * 2; // Width of source map in pixels.

    /** Constructs a CannedMarsMap object
     *  @param displayArea component containing the map
     */
    public CannedMarsMap(JComponent displayArea) {
        this.displayArea = displayArea;
        imageDone = false;
        centerCoords = new Coordinates(0D, 0D);
    }

    /** creates a 2D map at a given center point
     *  @param newCenter new map center location
     */
    public void drawMap(Coordinates newCenter) {
        createMapImage(newCenter);
        waitForMapLoaded();
    }

    /** determines if a requested map is complete
     *  @return true if requested map is complete
     */
    public boolean isImageDone() {
        return imageDone;
    }

    /** returns constructed map image
     *  @return constructed map image
     */
    public Image getMapImage() {
        return mapImage;
    }

    abstract public RandomAccessFile getMapFile();

    /** constructs a rendering of the map centered about the given
     *  coordinates. The map is written to the private variable
     *  mapImage and can be obtained using the getMapImage() method.
     *  @param newCenter map center location
     */
    private void createMapImage(Coordinates newCenter) {
        // Adjust coordinates
        Coordinates adjNewCenter =
                new Coordinates(newCenter.getPhi(), newCenter.getTheta() + Math.PI);

        // If current center point equals new center point, don't recreate sphere
        if (centerCoords.equals(adjNewCenter))
            return;

        // Initialize Variables
        imageDone = false;

        centerCoords.setCoords(adjNewCenter);

        double PI_half = Math.PI / 2D;
        double PI_double = Math.PI * 2D;
        double PI_piece =
                Math.PI * (.153D - (.04D * Math.sin(centerCoords.getPhi())));

        double col_correction = (Math.PI / -2D) - centerCoords.getTheta();
        double rho = mapHeight / Math.PI;
        double sin_offset = Math.sin(centerCoords.getPhi() + Math.PI);
        double cos_offset = Math.cos(centerCoords.getPhi() + Math.PI);
        double col_array_modifier = 1D / PI_double;

        int circum = 0;
        int half_map = mapHeight / 2;
        int low_edge = half_map - 150;
        int high_edge = half_map + 150;

        int[] buffer_array = new int[viewWidth * viewHeight];
        byte[] line_data = new byte[mapWidth * 3];
        int array_x_old = 0;
        int array_y_old = 0;
        long summer;

        try {
            RandomAccessFile mapFile = getMapFile();

            // Initialize row variables
            double start_row = centerCoords.getPhi() - PI_piece;
            double end_row = centerCoords.getPhi() + PI_piece;
            double row_iterate = (double)(Math.PI / mapHeight);

            boolean row_flag = false;
            boolean row_iterate_flag = true;

            // Go through each row
            for (double row = start_row; row <= end_row; row += row_iterate) {
                if (row < 0) continue;
                if (row >= Math.PI) continue;
                int array_y = (int) Math.round((double)(mapHeight * row) / Math.PI);
                if (array_y < 0) continue;
                if (array_y >= mapHeight) continue;

                // If starting row, read row info from files
                if (row_flag == false) {
                    array_y_old = array_y;
                    circum = index[array_y];
                    summer = sum[array_y];
                    mapFile.seek((long)(summer * 3));
                    mapFile.read(line_data, 0, (circum * 3));
                    row_iterate_flag = true;
                }

                // If new row, read row info from files
                if (array_y != array_y_old) {
                    circum = index[array_y];
                    mapFile.read(line_data, 0, (circum * 3));
                    row_iterate_flag = true;
                } else {
                    if (row_flag == false) {
                        row_flag = true;
                    } else {
                        row_iterate_flag = false;
                    }
                }
                array_y_old = array_y;

                // Initialize row variables
                double temp_buff_x = rho * Math.sin(row);
                double temp_buff_y1 = temp_buff_x * cos_offset;
                double temp_buff_y2 = rho * Math.cos(row) * sin_offset;
                double col_array_modifier2 = (col_array_modifier * circum);

                // Determine displayable boundries for row
                double col_boundry = Math.PI * (1.42D - (1.29D * Math.sin(row)));
                if (col_boundry > Math.PI)
                    col_boundry = Math.PI;

                if ((centerCoords.getPhi() > Math.PI / 5D) &&
                        (centerCoords.getPhi() < Math.PI - (Math.PI / 5D)))
                    col_boundry -= Math.PI - (Math.PI * Math.sin(row));
                else if ((centerCoords.getPhi() > Math.PI / 8D) &&
                        (centerCoords.getPhi() < Math.PI - (Math.PI / 8D)))
                    col_boundry -=
                            (.75D * Math.PI) - (.75D * Math.PI * Math.sin(row));

                // Determine row starting and stopping points
                double start_col = centerCoords.getTheta() - col_boundry;
                double end_col = centerCoords.getTheta() + col_boundry;
                double col_iterate = Math.PI / (double) circum;

                boolean col_flag = false;

                // Go through each column
                for (double col = start_col; col <= end_col; col += col_iterate) {
                    int array_x = (int) Math.round(col_array_modifier2 * col);
                    while (array_x < 0) {
                        array_x += circum;
                    }
                    while (array_x >= circum) {
                        array_x -= circum;
                    }

                    double temp_col = col + col_correction;

                    // Determine position of point, and put in buffer if in display area
                    int buff_x = (int) Math.round(temp_buff_x * Math.cos(temp_col)) +
                            half_map;
                    if ((buff_x > low_edge) && (buff_x < high_edge)) {
                        int buff_y = (int) Math.round(
                                (temp_buff_y1 * Math.sin(temp_col)) +
                                temp_buff_y2) + half_map;
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
                            buffer_array[buff_x + (viewHeight * buff_y)] =
                                    0xFF000000 | bit1 | bit2 | bit3;
                        }
                    }
                }
            }
        }
        catch (IOException e) {
            System.out.println("File read error: " + e);
        }

        // Create image from buffer array
        mapImage = displayArea.createImage(
                new MemoryImageSource(viewWidth, viewHeight, buffer_array, 0, 300));
    }

    protected void setArrays(int[] newIndex, long[] newSum) {
        index = newIndex;
        sum = newSum;
    }

    private void waitForMapLoaded() {
        MediaTracker mt = new MediaTracker(displayArea);
        mt.addImage(mapImage, 0);
        try {
            mt.waitForID(0);
        } catch (InterruptedException e) {
            System.out.println("MediaTracker interrupted " + e);
        }
        imageDone = true;
    }
}

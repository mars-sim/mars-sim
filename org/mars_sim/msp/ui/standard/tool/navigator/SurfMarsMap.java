/**
 * Mars Simulation Project
 * SurfMarsMap.java
 * @version 2.75 2003-08-05
 * @author Scott Davis
 * @author Greg Whelan
 */

package org.mars_sim.msp.ui.standard.tool.navigator;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import javax.swing.JComponent;
import org.mars_sim.msp.simulation.*;

/**
 *  The SurfMarsMap class is a map of the surface of Mars that can be generated
 *  for the map display.  Map data is retrieved from a data file and stored in memory.
 */
public class SurfMarsMap implements Map {

    private static final int MAP_HEIGHT = 1440;
    private static final int MAP_WIDTH = 2880;
    private static final int DISPLAY_HEIGHT = 300;
    private static final int DISPLAY_WIDTH = 300;
    private static final String MAP_DATA_DIRECTORY = "map_data";
    private static final String INDEX_FILE = "SurfMarsMap.index";
    private static final String SUM_FILE = "SurfMarsMap.sum";
    private static final String MAP_FILE = "SurfMarsMap.dat";
    
    // Data members
    private ArrayList surfaceColors;
    private JComponent displayArea;

    /** 
     * Constructor
     *
     * @param displayArea the component display area.
     */
    public SurfMarsMap(JComponent displayArea) {

        this.displayArea = displayArea;

        try {
            int[] index = loadIndexData(INDEX_FILE);
            long[] sum = loadSumData(SUM_FILE);
            surfaceColors = loadMapData(MAP_FILE, index, sum);
        }
        catch (IOException e) {
            System.out.println("Could not find surface map data files.");
            System.out.println(e.toString());
        }
    }
    
    /**
     * Loads the index data from a file.
     *
     * @param file name
     * @return array of index data
     * @throws IOException if file cannot be loaded.
     */
    private int[] loadIndexData(String filename) throws IOException {
     
        // Create file input stream.
        ClassLoader loader = getClass().getClassLoader();
        InputStream indexStream = loader.getResourceAsStream(MAP_DATA_DIRECTORY + File.separator + filename);
        if (indexStream == null) throw new IOException("Can not load " + filename);

        // Read stream into an array.
        BufferedInputStream indexBuff = new BufferedInputStream(indexStream);
        DataInputStream indexReader = new DataInputStream(indexBuff);
        int index[] = new int[MAP_HEIGHT];
        for (int x = 0; x < index.length; x++) index[x] = indexReader.readInt();
        indexReader.close();
        indexBuff.close();
    }
        
    /**
     * Loads the sum data from a file.
     *
     * @param filename the sum data file
     * @return array of sum data
     * @throws IOException if file cannot be loaded.
     */
    private long[] loadSumData(String filename) throws IOException {
        
        // Create file input stream.
        ClassLoader loader = getClass().getClassLoader();
        InputStream sumStream = loader.getResourceAsStream(MAP_DATA_DIRECTORY + File.separator + filename);
        if (sumStream == null) throw new IOException("Can not load " + filename);

        // Read stream into an array.
        BufferedInputStream sumBuff = new BufferedInputStream(sumStream);
        DataInputStream sumReader = new DataInputStream(sumBuff);
        long sum[] = new long[MAP_HEIGHT];
        for (int x = 0; x < sum.length; x++) sum[x] = sumReader.readLong();
        sumReader.close();
        sumBuff.close();
    }
     
    /** 
     * Loads the map data from a file.
     *
     * @param filename the map data file
     * @param index the index array
     * @param sum the sum array
     * @return array list of map data
     * @throws IOException if map data cannot be loaded.
     */
    private ArrayList loadMapData(String filename, int[] index, long[] sum) throws IOException {
     
        // Load map data from file.
        ClassLoader loader = getClass().getClassLoader();
        InputStream mapStream = loader.getResourceAsStream(MAP_DATA_DIRECTORY + File.separator + filename);
        if (mapStream == null) throw new IOException("Can not load " + filename);
        
        // Read stream into an array.
        BufferedInputStream mapBuff = new BufferedInputStream(mapStream);
        DataInputStream mapReader = new DataInputStream(mapBuff);
        
        // Create map colors array list.
        ArrayList mapColors = new ArrayList(MAP_HEIGHT);
        
        // Create an array of colors for each pixel in map height.
        for (int x=0; x < MAP_HEIGHT; x++) {
            int[] colors = new int[index[x]];
            for (int y=0; y < colors.length; y++) {
                int red = (int) mapReader.readByte();
                int green = (int) mapReader.readByte();
                int blue = (int) mapReader.readByte();
                colors[y] = (new Color(red, green, blue)).getRGB();
            }
            mapColors.add(colors);
        }
        
        return mapColors;
    }
    
    private int getRGBColor(double phi, double theta) {
        
        // Make sure phi is between 0 and PI.
        while (phi > Math.PI) phi-= Math.PI;
        while (phi < 0) phi+= Math.PI;
        
        // Make sure theta is between 0 and 2 PI.
        while (theta > (Math.PI * 2D)) theta-= (Math.PI * 2D);
        while (theta < 0) theta+= (Math.PI * 2D);
        
        int row = (int) Math.round(phi * (MAP_HEIGHT / Math.PI));
        if (row == surfaceColors.size()) row--;
        
        int[] colorRow = (int[]) surfaceColors.get(row);
        int column = (int) Math.round(theta * (colorRow.length / (2 * Math.PI)));
        if (column == colorRow.length) column--;
        
        return colorRow[column];
    }
    
    private Image createMapImage(Coordinates center) {
        
        int[] mapArray = new int[DISPLAY_WIDTH * DISPLAY_HEIGHT];
        
        double twoPI = Math.PI * 2D;
        double halfPI = Math.PI / 2D;
        
        double phiIterationAngle = Math.PI / MAP_HEIGHT;
        double phiRange = .96D; // Value derived from testing.
        
        double startPhi = center.getPhi() - (phiRange / 2D);
        double endPhi = center.getPhi() + (phiRange / 2D);
        
        for (double x = startPhi; x <= endPhi; x+= phiIterationAngle) {
            
            // Determine theta iteration to check against data.
            double thetaIterationAngle = (twoPI / MAP_WIDTH) * Math.sin(x);
            
            // Determine theta range to display for this phi.
            double minThetaDisplay = twoPI * ((double) DISPLAY_WIDTH / (double) MAP_WIDTH);
            double thetaRange = Math.cos(x) + minThetaDisplay;
            if (thetaRange > twoPI) thetaRange = twoPI;
            
            // Determine the theta starting and ending angles.
            double startTheta = center.getTheta() - (thetaRange / 2D);
            double endTheta = center.getTheta() + (thetaRange / 2D);
            
            for (double y = startTheta; y <= endTheta; y+= thetaIterationAngle) {
               
                // Determine if the rectangular position of the coordinate is within display area.
                IntPoint location = center.findRectPosition(x, y, Coordinates.MARS_RADIUS_KM, 720, 0);
                boolean leftBounds = location.getiX() >= 0 - (DISPLAY_WIDTH / 2);
                boolean rightBounds = location.getiX() < DISPLAY_WIDTH / 2;
                boolean topBounds = location.getiY() >= 0 - (DISPLAY_HEIGHT / 2);
                boolean bottomBounds = location.getiY() < DISPLAY_HEIGHT / 2;
                if (leftBounds && rightBounds && topBounds && bottomBounds) {
               
                    // Get color as an int for this phi and theta.
                    int color = getRGBColor(x, y);
                
                    // Determine array index for location.
                    int index = location.getiX() + (location.getiY() * DISPLAY_WIDTH);
                    
                    // Put color in array at index.
                    mapArray[index] = color;
                }
            }
        }
        
        // Create new map image.
        return displayArea.createImage(new MemoryImageSource(DISPLAY_WIDTH, DISPLAY_WIDTH, mapArray, 0, DISPLAY_WIDTH));
    }
}

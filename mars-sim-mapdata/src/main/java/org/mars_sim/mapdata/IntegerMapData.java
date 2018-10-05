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
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A map that uses integer data stored in files to represent colors.
 */
abstract class IntegerMapData implements MapData {

    // Static members.
    private static Logger logger = Logger.getLogger(IntegerMapData.class.getName());
    private static int IMAGE_WIDTH = 300;
    private static int IMAGE_HEIGHT = 300;
    public static final double HALF_MAP_ANGLE = .48587D;
    public static final int MAP_HEIGHT = 1440; // Source map height in pixels.
    public static final int MAP_WIDTH = 2880; // Source map width in pixels.
    public static final double PIXEL_RHO = (double) MAP_HEIGHT / Math.PI;
    private static final double TWO_PI = Math.PI * 2D;
     
    // Data members.
    private List<int[]> mapColors = null;
    
    /**
     * Constructor
     * @param indexFileName the index data file name.
     * @param mapFileName the map data file name.
     */
    public IntegerMapData(String indexFileName, String mapFileName) {
        
        // Load data files
        try {
            int[] index = loadIndexData(indexFileName);       
            mapColors = loadMapData(mapFileName, index);
        }
        catch (IOException e) {
            logger.log(Level.SEVERE,"Could not find .index or .dat files.", e) ;
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
    
        // Load index data from map_data jar file.
        ClassLoader loader = getClass().getClassLoader();
        InputStream indexStream = loader.getResourceAsStream(filename);
        if (indexStream == null) throw new IOException("Can not load " + filename);
  
        // Read stream into an array.
        BufferedInputStream indexBuff = new BufferedInputStream(indexStream);
        DataInputStream indexReader = new DataInputStream(indexBuff);
        int index[] = new int[MAP_HEIGHT];
        for (int x = 0; x < index.length; x++) {
            index[x] = indexReader.readInt();
        }
        indexReader.close();
        indexBuff.close();
       
        return index;
    }

    
    /** 
     * Loads the map data from a file.
     *
     * @param filename the map data file
     * @param index the index array
     * @return array list of map data
     * @throws IOException if map data cannot be loaded.
     */
    private ArrayList<int[]> loadMapData(String filename, int[] index) throws IOException {
     
        // Load map data from map_data jar file.
        ClassLoader loader = getClass().getClassLoader();
        InputStream mapStream = loader.getResourceAsStream(filename);
        if (mapStream == null) throw new IOException("Can not load " + filename);
              
        // Decompress the xz file
        //new DecompressXZ(filename);
        
        // Read stream into an array.
        BufferedInputStream mapBuff = new BufferedInputStream(mapStream);
        DataInputStream mapReader = new DataInputStream(mapBuff);
        
        // Create map colors array list.
        ArrayList<int[]> mapColors = new ArrayList<int[]>(MAP_HEIGHT);
        
        // Create an array of colors for each pixel in map height.
        for (int x=0; x < MAP_HEIGHT; x++) {
            int[] colors = new int[index[x]];
            for (int y=0; y < colors.length; y++) {
                int red = mapReader.readByte();
                red <<= 16;
                red &= 0x00FF0000;
                int green = mapReader.readByte();
                green <<= 8;
                green &= 0x0000FF00;
                int blue = mapReader.readByte();
                blue &= 0x000000FF;
                int totalColor = 0xFF000000 | red | green | blue;
                colors[y] = (new Color(totalColor)).getRGB();
            }
            mapColors.add(colors);
        }
       
        return mapColors;
    }
    
    @Override
    public Image getMapImage(double centerPhi, double centerTheta) {
        
        // Create a new buffered image to draw the map on.
        BufferedImage result = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        
        // The map data is PI offset from the center theta.    
        double correctedTheta = centerTheta - Math.PI;
        while (correctedTheta < 0D) correctedTheta += TWO_PI;
        while (correctedTheta > TWO_PI) correctedTheta -= TWO_PI;
        
        // Create an array of int RGB color values to create the map image from. 
        int[] mapArray = new int[IMAGE_WIDTH * IMAGE_HEIGHT];
        
        // Determine phi iteration angle.
        double phiIterationPadding = 1.26D; //Derived from testing.
        double phiIterationAngle = Math.PI / ((double) MAP_HEIGHT * phiIterationPadding);
        
        // Determine phi range.
        double phiPadding = 1.46D; // Derived from testing.
        double phiRange = ((double) IMAGE_HEIGHT / (double) MAP_HEIGHT) * Math.PI * phiPadding;
        
        // Determine starting and ending phi values.
        double startPhi = centerPhi - (phiRange / 2D);
        if (startPhi < 0D) startPhi = 0D;
        double endPhi = centerPhi + (phiRange / 2D);
        if (endPhi > Math.PI) endPhi = Math.PI;
        
        // Loop through each phi value.
        for (double x = startPhi; x <= endPhi; x+= phiIterationAngle) {
            
            // Determine theta iteration angle.
            double thetaIterationPadding = 1.46D; // Derived from testing.
            double thetaIterationAngle = TWO_PI / (((double) MAP_WIDTH * Math.sin(x) * thetaIterationPadding) + 1D);
            
            // Determine theta range.
            double minThetaPadding = 1.02D;  // Derived from testing.
            double minThetaDisplay = TWO_PI * ((double) IMAGE_WIDTH / (double) MAP_WIDTH) * minThetaPadding;          
            double thetaRange = ((1D - Math.sin(x)) * TWO_PI) + minThetaDisplay;
            double polarCapRange = Math.PI / 6.54D; // Polar cap phi values must display 2 PI theta range. (derived from testing)
            if ((x < polarCapRange) || (x > (Math.PI - polarCapRange))) thetaRange = TWO_PI;
            if (thetaRange > TWO_PI) thetaRange = TWO_PI;
            
            // Determine the theta starting and ending values.
            double startTheta = centerTheta - (thetaRange / 2D);
            double endTheta = centerTheta + (thetaRange / 2D);
            
            // Loop through each theta value.
            for (double y = startTheta; y <= endTheta; y+= thetaIterationAngle) {
               
                // Correct y value to make sure it is within bounds. (0 to 2PI)
                double yCorrected = y;
                while (yCorrected < 0) yCorrected+= TWO_PI;
                while (yCorrected > TWO_PI) yCorrected-= TWO_PI;
               
                // Determine the rectangular offset of the pixel in the image.
                Point location = findRectPosition(centerPhi, centerTheta, x, yCorrected, 1440D / Math.PI, 720, 720 - 150);
                
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
                    if ((index >= 0) && (index < mapArray.length)) mapArray[index] = getRGBColorInt(x, yCorrected);
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
     * @param phi the phi location.
     * @param theta the theta location.
     * @return the RGB map color as an integer.
     */
    private int getRGBColorInt(double phi, double theta) {
        // Make sure phi is between 0 and PI.
        while (phi > Math.PI) phi-= Math.PI;
        while (phi < 0) phi+= Math.PI;
        
        // Add PI to theta for offset.
        theta+= Math.PI;
        
        // Make sure theta is between 0 and 2 PI.
        while (theta > TWO_PI) theta-= TWO_PI;
        while (theta < 0) theta+= TWO_PI;
        
        int row = (int) Math.round(phi * (MAP_HEIGHT / Math.PI));
        if (row == mapColors.size()) row--;
        
        int[] colorRow = mapColors.get(row);
        int column = (int) Math.round(theta * ((double) colorRow.length / TWO_PI));
        if (column == colorRow.length) column--;
        
        return colorRow[column];
    }
    
    /**
     * Converts spherical coordinates to rectangular coordinates.
     * Returns integer x and y display coordinates for spherical
     * location.
     *
     * @param newPhi the new phi coordinate
     * @param newTheta the new theta coordinate
     * @param rho diameter of planet (in km)
     * @param half_map half the map's width (in pixels)
     * @param low_edge lower edge of map (in pixels)
     * @return pixel offset value for map
     */
    public Point findRectPosition(double oldPhi, double oldTheta, double newPhi, double newTheta, 
            double rho, int half_map, int low_edge) {
        
        final double temp_col = newTheta + ((Math.PI / -2D) - oldTheta);
        final double temp_buff_x = rho * Math.sin(newPhi);
        int buff_x = ((int) Math.round(temp_buff_x * Math.cos(temp_col)) + half_map) - low_edge;
        int buff_y = ((int) Math.round(((temp_buff_x * (0D - Math.cos(oldPhi))) * Math.sin(temp_col)) + 
                (rho * Math.cos(newPhi) * (0D - Math.sin(oldPhi)))) + half_map) - low_edge;
        return new Point(buff_x, buff_y);
    }
}
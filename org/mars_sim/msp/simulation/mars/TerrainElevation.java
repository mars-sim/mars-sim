/**
 * Mars Simulation Project
 * TerrainElevation.java
 * @version 2.81 2007-08-26
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.mars;

import java.awt.*;
import java.io.*;
import java.util.*;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.Direction;

/** The TerrainElevation class represents the surface terrain of the
 *  virtual Mars. It can provide information about elevation and
 *  terrain ruggedness at any location on the surface of virtual Mars.
 */
public class TerrainElevation {

	private static final String INDEX_FILE = "TopoMarsMap.index";
	private static final String MAP_FILE = "TopoMarsMap.dat";
	private static final int MAP_HEIGHT = 1440; // Source map height in pixels.
	private static final double TWO_PI = Math.PI * 2D;
	private static final double OLYMPUS_MONS_CALDERA_PHI = 1.246165D;
	private static final double OLYMPUS_MONS_CALDERA_THETA = 3.944444D;
	private static final double ASCRAEUS_MONS_PHI = 1.363102D;
	private static final double ASCRAEUS_MONS_THETA = 4.459316D;

	//	Data members
	private static ArrayList<int[]> topoColors = null;

    /** 
     * Constructor
     */
    TerrainElevation() {
    	
    	if (topoColors == null) {
    		// Load data files
    		try {
    			int[] index = loadIndexData(INDEX_FILE);
    			topoColors = loadMapData(MAP_FILE, index);
    		}
    		catch (IOException e) {
    			System.err.println("Could not find map data files.");
    			System.err.println(e.toString());
    		}
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
		for (int x = 0; x < index.length; x++) index[x] = indexReader.readInt();
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
	
	/**
	 * Gets an RGB color for a given location on the map.
	 * @param phi the phi value of the location.
	 * @param theta the theta value of the location.
	 * @return the RGB color encoded as an int value. 
	 */
	private int getRGBColor(double phi, double theta) {
       
		// Make sure phi is between 0 and PI.
		while (phi > Math.PI) phi-= Math.PI;
		while (phi < 0) phi+= Math.PI;
        
        // Add PI to theta for offset.
        theta+= Math.PI;
        
		// Make sure theta is between 0 and 2 PI.
		while (theta > TWO_PI) theta-= TWO_PI;
		while (theta < 0) theta+= TWO_PI;
        
		int row = (int) Math.round(phi * (MAP_HEIGHT / Math.PI));
		if (row == topoColors.size()) row--;
        
		int[] colorRow = (int[]) topoColors.get(row);
		int column = (int) Math.round(theta * ((double) colorRow.length / TWO_PI));
		if (column == colorRow.length) column--;
        
		return colorRow[column];
	}

    /** Returns terrain steepness angle from location by sampling 11.1
      *  km in given direction
      *  @param currentLocation the coordinates of the current location
      *  @param currentDirection the current direction (in radians)
      *  @return terrain steepness angle (in radians)
      */
    public double determineTerrainDifficulty(Coordinates currentLocation, Direction currentDirection) {
        double newY = -1.5D * currentDirection.getCosDirection();
        double newX = 1.5D * currentDirection.getSinDirection();
        Coordinates sampleLocation = currentLocation.convertRectToSpherical(newX, newY);
        double elevationChange = getElevation(sampleLocation) - getElevation(currentLocation);
        double result = Math.atan(elevationChange / 11.1D);

        return result;
    }

    /** Returns elevation in km at the given location
     *  @param location the location in question
     *  @return the elevation at the location (in km)
     */
    public double getElevation(Coordinates location) {
    	
    	// Find hue and saturation color components at location.
    	Color color = new Color(getRGBColor(location.getPhi(), location.getTheta()));
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        float hue = hsb[0];
        float saturation = hsb[1];

		// Determine elevation in meters.
		// Note: This code needs updating.
        double elevation = 0D;
        if ((hue < .792F) && (hue > .033F)) elevation = (-13801.99D * hue) + 2500D;
        else elevation = (-21527.78D * saturation) + 19375D + 2500D;
        
        // Determine elevation in kilometers.
        elevation = elevation / 1000D;
        
        // Patch elevation problems at certain locations.
		elevation = patchElevation(elevation, location);

        return elevation;
    }
    
    /**
     * Patches elevation errors around mountain tops.
     * @param elevation the original elevation for the location.
     * @param location the coordinates
     * @return the patched elevation for the location
     */
    private double patchElevation(double elevation, Coordinates location) {
    	double result = elevation;
    	
    	// Patch errors at Olympus Mons caldera.
		if (Math.abs(location.getTheta() - OLYMPUS_MONS_CALDERA_THETA) < .04D) {
			if (Math.abs(location.getPhi() - OLYMPUS_MONS_CALDERA_PHI) < .04D) {
				if (elevation < 3D) result = 20D;
			}
    	}
    	
    	// Patch errors at Ascraeus Mons.
		if (Math.abs(location.getTheta() - ASCRAEUS_MONS_THETA) < .02D) {
			if (Math.abs(location.getPhi() - ASCRAEUS_MONS_PHI) < .02D) {
				if (elevation < 3D) result = 20D;
			}
		}
    	
    	return result;
    }
    
    /**
     * Gets the cached topographical colors.
     * @return array list of colors.
     */
    public ArrayList getTopoColors() {
    	return topoColors;
    }
}
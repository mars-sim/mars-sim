/**
 * Mars Simulation Project
 * TerrainElevation.java
 * @version 2.71 2000-10-30
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.awt.*;
import java.net.URL;
import java.io.*;

/** The TerrainElevation class represents the surface terrain of the
 *  virtual Mars. It can provide information about elevation and
 *  terrain ruggedness at any location on the surface of virtual Mars.
 */
public class TerrainElevation {

    // Data members
    private RandomAccessFile map; // File containing elevation data for virtual Mars.
    private int[] index; // An cached array for row count indexing of the elevation data.
    private long[] sum; // An cached array to help find rows of the elevation data.

    // constants
    private final static int MAP_HEIGHT = 1440; // Height of source map in pixels.

    /** Constructs a TerrainElevation object
     *  @param topoData the file URL for the topographical map data
     *  @param topoIndex the file URL for the topographical map index
     *  @param topoSum the file URL for the topographical map sum
     */
    TerrainElevation(String topoData, String topoIndex, String topoSum) {
		try {
			URL found = getClass().getClassLoader().getResource(topoData);
			map = new RandomAccessFile(found.getFile(), "r");
		}
		catch (Exception e) {
			System.out.println("Could not open " + topoData);
			System.out.println("  You can find it at: http://mars-sim.sourceforge.net/TopoDat.zip");
			System.out.println("  Download and then unzip in the directory mars-sim/map_data");
			System.exit(0);
		}
        loadArrays(topoIndex, topoSum);
    }

    /** note that this functionality is duplicated in TopoMarsMap.java
     *  @param indexFile the file URL for the topographical map index
     *  @param sumFile the file URL for the topographical map sum
     */
    private void loadArrays(String indexFile, String sumFile) {
        ClassLoader loader = getClass().getClassLoader();
        try {
            // Load index array
            InputStream indexStream = loader.getResourceAsStream(indexFile);
            if (indexStream == null) {
                System.err.println("Can not load " + indexFile);
            }
            else
            {
                BufferedInputStream indexBuff = new BufferedInputStream(indexStream);
                DataInputStream indexReader = new DataInputStream(indexBuff);
                index = new int[MAP_HEIGHT];
                for (int x = 0; x < MAP_HEIGHT; x++)
                    index[x] = indexReader.readInt();
                indexReader.close();
                indexBuff.close();
            }

            // Load sum array
            InputStream sumStream = loader.getResourceAsStream(sumFile);
            if (sumStream == null) {
                System.err.println("Can not load " + sumFile);
            }
            else {
                BufferedInputStream sumBuff = new BufferedInputStream(sumStream);
                DataInputStream sumReader = new DataInputStream(sumBuff);
                sum = new long[MAP_HEIGHT];
                for (int x = 0; x < MAP_HEIGHT; x++)
                    sum[x] = sumReader.readLong();
                sumReader.close();
                sumBuff.close();
            }
        } catch (IOException e) {
            System.out.println(e);
            System.exit(0);
        }
    }

    protected void finalize() throws Throwable {
        // close large file
        map.close();
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
        int red = 0;
        int green = 0;
        int blue = 0;
        double tempPhi = location.getPhi();
        double tempTheta = location.getTheta();

        try {
            int row = (int) Math.round((tempPhi / Math.PI) * 1439D);
            int rowLength = index[row];
            long summer = sum[row];

            tempTheta += Math.PI;
            if (tempTheta >= (2D * Math.PI))
                tempTheta -= (2D * Math.PI);
            int col = (int) Math.round((tempTheta / (2D * Math.PI)) * rowLength);

            map.seek((long)((summer + col) * 3));

            red = (int) map.readByte();
            red &= 0x000000FF;
            green = (int) map.readByte();
            green &= 0x000000FF;
            blue = (int) map.readByte();
            blue &= 0x000000FF;
        } catch (IOException e) {
            System.out.println(e.toString());
        }

        float[] hsb = new float[3];
        hsb = Color.RGBtoHSB(red, green, blue, null);
        float hue = hsb[0];
        float saturation = hsb[1];

        double elevation = 0D;
        // holy magic numbers batman
        if ((hue < .792F) && (hue > .033F)) {
            elevation = (-13801.99D * hue) + 2500D;
        } else {
            elevation = (-21527.78D * saturation) + 19375D + 2500D;
        }
        elevation = elevation / 1000D;

        return elevation;
    }
}

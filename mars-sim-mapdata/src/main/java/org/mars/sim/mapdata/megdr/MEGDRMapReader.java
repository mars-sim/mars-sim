/*
 * Mars Simulation Project
 * MEGDRMapReader.java
 * @date 2023-06-17
 * @author Manny Kung
 */

package org.mars.sim.mapdata.megdr;

/**
 * This class reads the topographical or elevation data of MOLA Mission Experiment 
 * Gridded Data Records (MEGDRs) acquired by MGS mission. 
 * @See https://pds-geosciences.wustl.edu/missions/mgs/megdr.html.
 */
public abstract class MEGDRMapReader {
		


//	NOTE: (Do not delete)
//	
//	LEVEL 0 :
//  File name: megt90n000cb.img  
//	Pixels: 1440 x 720
//	Resolution: 4 pixels per degree (or 0.25 by 0.25 degrees)
//	Scale: 14.818 km per pixel (=1/4)
//  File size: 2.025 MB
//	
//	LEVEL 1 :
//  File name: megt90n000eb.img
//	Pixels: 5760 x 2880
//	Resolution: 16 pixels per degree (or 0.0625by 0.0625 degrees)
//	Scale: 3.705 km per pixel (=1/16)	
//  File size: 32.4 MB
//	
//	LEVEL 2 :
//  File name: megt90n000fb.img
//	Pixels: 11520 x 5760
//	Resolution: 32 pixels per degree (or 0.03125 by 0.03125 degrees)
//	Scale: 1.853 km per pixel (=1/32) 	
//  File size: 129.6 MB
//	
//	LEVEL 3 :
//  Divided into 4 quadrants
//  File name: 4 separate files with `megt__n___gb.img
//	Pixels: 23040 x 5760
//	Resolution: 64 pixels per degree (or 0.015625 by 0.015625 degrees)
//  Scale: 0.926 km per pixel (=1/64)		
//  File size: 132.7 MB
//	
//	LEVEL 4 :
//  Divided into 16 quadrants
//  File name: 16 separate files with `megt__n___hb.img
//	Pixels: 23040 x 5632
//	Resolution: 128 pixels per degree (or 0.00781 by 0.00781 degrees)
//  Scale: 0.463 km per pixel (=1/128)		
//  File size: 129.8 MB
	
 	private static final double TWO_PI = Math.PI * 2D;

	public static final String DEFAULT_MEGDR_FILE = "/elevation/megt90n000eb.img";
	
	private short mapHeight;
	private short mapWidth;
	
    /**
	 * Sets the size of the map that is loaded.
	 * 
	 * @param width New width
	 * @param height New height
	 */
	protected void setSize(short width, short height) {
		this.mapWidth = width;
		this.mapHeight = height;
    }

   /**
	 * Gets the elevation as a short integer at a given location.
	 * 
	 * @param phi   the phi location.
	 * @param theta the theta location.
	 * @return the elevation as an integer.
	 */
	public short getElevation(double phi, double theta) {
		// Note that row 0 and column 0 are at top left 
		int row = (int)Math.round(phi * mapHeight / Math.PI);
		
		if (row == mapHeight) 
			row--;
		
		int column = (int)Math.round(theta * mapWidth / TWO_PI);

		if (column == mapWidth)
			column--;

		int index = row * mapWidth + column;
		
		if (index > mapHeight * mapWidth - 1)
			index = mapHeight * mapWidth - 1;
		
		return getElevation(index);
	}

	/**
	 * Gets the elevation at an index in the source file.
	 * 
	 * @param index
	 * @return
	 */
	protected abstract short getElevation(int index);
}

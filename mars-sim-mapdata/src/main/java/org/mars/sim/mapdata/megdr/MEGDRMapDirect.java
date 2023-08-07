/*
 * Mars Simulation Project
 * MEGDRMapDirect.java
 * @date 2023-08-06
 * @author Barry Evans
 */
package org.mars.sim.mapdata.megdr;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Logger;

import org.mars.sim.mapdata.common.FileLocator;

/**
 * Get elevation data by using direct access to the selected MEGDR file.
 */
public class MEGDRMapDirect extends MEGDRCachedReader 
		implements Closeable {

    private static final Logger logger = Logger.getLogger(MEGDRMapDirect.class.getName());
    	
	private RandomAccessFile mapData;

    public MEGDRMapDirect(String imageName) throws IOException {
		super(1000, 5);
		prepareDirectFile(FileLocator.locateFile(imageName));
	}

	/**
	 * Load a short value from the directly access file
	 * @param index
	 * @return
	 */
    protected synchronized short loadElevation(int index) {
		byte []data = new byte[2];

		try {
			mapData.seek(index * 2);
			mapData.read(data, 0, 2);
			
			return (short)( // NOTE: type cast not necessary for int
					(data[0]) << 8  |
					(0xff & data[1])
					);
		}
		catch (IOException ioe) {
			logger.severe("Problem reading map source " + ioe.getMessage());
		}
		return 0;
	}

	/**
	 * Open the map file for access
	 * @throws IOException
	 */
	private void prepareDirectFile(File mapSource) throws IOException {

		// Get the file
		mapData = new RandomAccessFile(mapSource, "r");
		long fileSize = mapData.length();
						
		short mapHeight = (short) Math.sqrt(fileSize / 4);
		short mapWidth = (short) (mapHeight * 2);
		
		logger.info("Reading elevation directly from '" + mapSource.getName() + "' (" + mapWidth + " by " + mapHeight + ").");

		setSize(mapWidth, mapHeight);
	}

	@Override
	public void close() throws IOException {
		mapData.close();
	}
}

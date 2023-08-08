/*
 * Mars Simulation Project
 * MEGDRMapMemory.java
 * @date 2023-08-06
 * @author Barry Evans
 */
package org.mars.sim.mapdata.megdr;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.logging.Logger;

import org.mars.sim.mapdata.common.FileLocator;

/**
 * Gets elevation data by using memory mapped access to the select MEGDR file.
 */
public class MEGDRMapMemory extends MEGDRCachedReader
			implements Closeable {

    private static final Logger logger = Logger.getLogger(MEGDRMapMemory.class.getName());
    	
	private MappedByteBuffer memoryBuffer;

	private RandomAccessFile mapData;

    public MEGDRMapMemory(String imgName) throws IOException {
		super(1000, 5);

		prepareMemoryFile(FileLocator.locateFile(imgName));
	}

	
    protected synchronized short loadElevation(int index) {
		byte []data = new byte[2];

		memoryBuffer.get(index * 2, data, 0, 2);
		
		return (short)( // NOTE: type cast not necessary for int
				(data[0]) << 8  |
				(0xff & data[1])
				);
	}


	/**
	 * Opens the map file for memory mapped access.
	 * 
	 * @throws IOException
	 */
	private void prepareMemoryFile(File mapSource) throws IOException {
		// Open the map file
		mapData = new RandomAccessFile(mapSource, "r");
		long fileSize = mapData.length();

	    // Get file channel in read-only mode
      	FileChannel fileChannel = mapData.getChannel();
           
        // Get direct byte buffer access using channel.map() operation
        memoryBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY,
		  							0, fileChannel.size());
		
		short mapHeight = (short) Math.sqrt(fileSize / 4);
		short mapWidth = (short) (mapHeight * 2);
		
		logger.info("Reading elevation memory mapped to '" + mapSource.getName()
						+ "' (" + mapWidth + " by " + mapHeight + ").");

		setSize(mapWidth, mapHeight);
	}


	@Override
	public void close() throws IOException {
		mapData.close();
	}
}
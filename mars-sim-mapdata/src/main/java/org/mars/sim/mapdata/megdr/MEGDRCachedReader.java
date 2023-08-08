/*
 * Mars Simulation Project
 * MEGDRCachedReader.java
 * @date 2023-08-06
 * @author Barry Evans
 */
package org.mars.sim.mapdata.megdr;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * This is a reader where the selected elevation values are cached for later reuse.
 */
public abstract class MEGDRCachedReader extends MEGDRMapReader {

    private static final Logger logger = Logger.getLogger(MEGDRCachedReader.class.getName());
    	
	private Cache<Integer, Integer> cache;
	private int requests = 0;
	private int hits = 0;

    public MEGDRCachedReader(int maximumEntries, int maxIdleMinutes) throws IOException {
		
		cache = CacheBuilder.newBuilder()
								.maximumSize(maximumEntries)
								.expireAfterAccess(maxIdleMinutes, TimeUnit.MINUTES)
								.build();
	}

	/**
	 * Gets the elevation at a certain index into the map data. This will check the cached value first.
	 * 
	 * @param index Index to the data.
	 */
    protected short getElevation(int index) {
		short result = 0;
		requests++;
		Integer value = cache.getIfPresent(index);
		if (value != null) {
			result = value.shortValue();
			hits++;
		}
		else {
			result = loadElevation(index);
			cache.put(index, Integer.valueOf(result));
		}

		if (requests % 1000 == 0) {
			logger.info("MEGDR Cache hit rate " + (hits/requests)  + ", size=" + cache.size());
		}
		return result;
	}

	/**
	 * Loads a short value from the mapdata.
	 * 
	 * @param index
	 * @return
	 */
    protected abstract short loadElevation(int index);
}

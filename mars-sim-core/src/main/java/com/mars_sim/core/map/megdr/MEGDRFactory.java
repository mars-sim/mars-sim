/*
 * Mars Simulation Project
 * MEGDRFactory.java
 * @date 2024-09-29
 * @author Barry Evans
 */
package com.mars_sim.core.map.megdr;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;

import java.io.IOException;
import java.util.logging.Logger;

import com.mars_sim.core.tool.RandomUtil;

/**
 * Static helper class responsible for loading MEDGR data to support elevation.
 */
public class MEGDRFactory {

    // Package friendly for unit tests
    static final String SEPARATOR = ",";
	static final String ARRAY_READER = "array";
	static final String DIRECT_READER = "direct";
	static final String MEMORY_READER = "memory";

    private static final String ELEVATION_FOLDER = "/elevation/";
	
    private static Logger logger = Logger.getLogger(MEGDRFactory.class.getName());

	private static MEGDRMapReader reader;
    private static String defaultSpec = MEMORY_READER + SEPARATOR + MEGDRMapReader.DEFAULT_MEGDR_FILE;

    private MEGDRFactory() {
        // Stop creation of helper classes
    }

    /**
	 * Gets the elevation as a short integer at a given location.
	 * 
	 * @param phi   the phi location.
	 * @param theta the theta location.
	 * @return the elevation as an integer.
	 */
    public static short getElevation(double phi, double theta) {
        if (reader == null) {
            reader = createReader(defaultSpec);
        }
        return reader.getElevation(phi, theta);
	}

	/**
	 * Creates a MEGDRReader based on a spec that contains the "reader type, filename".
	 * 
	 * @param spec
	 * @return
	 */
	static MEGDRMapReader createReader(String spec) {
		String [] parts = spec.split(SEPARATOR);
		
		String reader = parts[0].trim().toLowerCase();
		String imageName = ELEVATION_FOLDER + parts[1].trim();

		logger.config("imageName: " + imageName);
		
		try {
			return switch(reader) {
				case ARRAY_READER -> new MEGDRMapArray(imageName);
				case DIRECT_READER -> new MEGDRMapDirect(imageName);
				case MEMORY_READER -> new MEGDRMapMemory(imageName);
				default -> throw new IllegalArgumentException("Unknown MEGDR reader called " + reader);
			};
		}
		catch(IOException ioe) {
			logger.severe("Problem creating MEGDRReader " + ioe.getMessage());
			throw new IllegalArgumentException("Problem loading MEGDRReader:" + ioe.getMessage(), ioe);
		}
	}

    public static void setSpec(String newSpec) {
        defaultSpec = newSpec;
    }


	public static void main(String[] args) {
		runPerfTest(DIRECT_READER + SEPARATOR + MEGDRMapReader.DEFAULT_MEGDR_FILE);
		runPerfTest(ARRAY_READER + SEPARATOR + MEGDRMapReader.DEFAULT_MEGDR_FILE);
		runPerfTest(MEMORY_READER + SEPARATOR + MEGDRMapReader.DEFAULT_MEGDR_FILE);
	}

	private static void runPerfTest(String spec) {
		DecimalFormat formatter = new DecimalFormat("###,###,###");

		long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		MEGDRMapReader reader = createReader(spec);
		int size = 10000;
		double pi2 = Math.PI * 2;
		Instant start = Instant.now();
		for(int i = 0; i < size; i++) {
			double phi = RandomUtil.getRandomDouble(Math.PI);
			double theta = RandomUtil.getRandomDouble(pi2);
			reader.getElevation(phi, theta);
		}
		Instant finish = Instant.now();
		long finishMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		System.out.println("Reader " + spec + " - Memory increase " + formatter.format(finishMemory - startMemory) + ".");
		System.out.println(size + " lookups in " + Duration.between(start, finish).toMillis() + " ms.");
	}
}

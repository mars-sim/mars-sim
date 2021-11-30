/*
 * Mars Simulation Project
 * App.java
 * @date 2021-11-29
 * @author Manny Kung
 */

package org.mars_sim.base;

import org.mars_sim.msp.core.logging.SimLogger;

/**
 * Hello world!
 *
 */
public class App {
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(App.class.getName());

    public static void main(String[] args) {
        logger.info(null, "Hello World!");
    }
}

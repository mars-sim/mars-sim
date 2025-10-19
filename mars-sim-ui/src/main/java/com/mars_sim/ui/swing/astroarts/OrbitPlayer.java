/* 
 * Mars Simulation Project
 * OrbitPlayer.java
 * @date 2022-07-10
 * @author Manny Kung
 * Original work by Osamu Ajiki and Ron Baalke (NASA/JPL)
 * http://www.astroarts.com/products/orbitviewer/
 * http://neo.jpl.nasa.gov/
 */

package com.mars_sim.ui.swing.astroarts;

import com.mars_sim.core.logging.SimLogger;

public class OrbitPlayer implements Runnable {
	
	private SimLogger logger = SimLogger.getLogger(OrbitPlayer.class.getName());
	private static final String THREAD_NAME = "OrbitViewer";

	private OrbitViewer	orbitViewer;
	private boolean active = false;
	private Thread runner = null;
	
	/**
	 * Constructor
	 */
	public OrbitPlayer(OrbitViewer orbitViewer) {
		this.orbitViewer = orbitViewer;
	}
	
	public void start() {
		active = true;
		if (runner == null) {
			runner = new Thread(this,  THREAD_NAME);
			runner.start();
		}
	}

	/**
	 * Stop the player
	 */
	public void stop() {
		active = false;
		runner = null;
	}
	
	/**
	 * Play forever
	 */
	public void run() {
		
		while (active) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				logger.warning("Sleep interrupted.");
			    // Restore interrupted state...
			    Thread.currentThread().interrupt();
			    break;
			}

			orbitViewer.advanceTime();
		}
	}
}


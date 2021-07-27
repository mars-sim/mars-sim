/* Mars Simulation Project
 * OrbitPlayer.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 * Original work by Osamu Ajiki and Ron Baalke (NASA/JPL)
 * http://www.astroarts.com/products/orbitviewer/
 * http://neo.jpl.nasa.gov/
 */

package org.mars_sim.msp.ui.astroarts;

import org.mars_sim.msp.core.astroarts.ATime;

public class OrbitPlayer implements Runnable {
		private OrbitViewer	orbitViewer;
		private boolean active = true;
		
		/**
		 * Constructor
		 */
		public OrbitPlayer(OrbitViewer orbitViewer) {
			this.orbitViewer = orbitViewer;
		}
		
		/**
		 * Stop the player
		 */
		public void stop() {
			active = false;
		}
		
		/**
		 * Play forever
		 */
		public void run() {
			active = true;
			
			while (active) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					break;
				}
				ATime atime = orbitViewer.getAtime();
				atime.changeDate(orbitViewer.timeStep, orbitViewer.playDirection);
				orbitViewer.setNewDate(atime);
			}
		}
	}


/* Mars Simulation Project
 * OrbitPlayer.java
 * @version 3.1.0 2019-09-20
 * @author Manny Kung
 * Original work by Osamu Ajiki and Ron Baalke (NASA/JPL)
 * http://www.astroarts.com/products/orbitviewer/
 * http://neo.jpl.nasa.gov/
 */

package org.mars_sim.msp.ui.astroarts;

import org.mars_sim.msp.core.astroarts.ATime;

public class OrbitPlayer implements Runnable {
		OrbitViewer	orbitViewer;
		
		/**
		 * Constructor
		 */
		public OrbitPlayer(OrbitViewer orbitViewer) {
			this.orbitViewer = orbitViewer;
		}
		
		/**
		 * Play forever
		 */
		public void run() {
			while (true) {
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


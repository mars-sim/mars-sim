///* Mars Simulation Project
// * OrbitPlayer.java
// * @version 3.1.0 2019-09-20
// * @author Manny Kung
// * Original work by Osamu Ajiki and Ron Baalke (NASA/JPL)
// * http://www.astroarts.com/products/orbitviewer/
// * http://neo.jpl.nasa.gov/
// */
//
//package org.mars_sim.msp.ui.astroarts;
//
//public class OrbitPlayerMain implements Runnable {
//		OrbitViewerMain	OrbitViewerMain;
//		
//		/**
//		 * Constructor
//		 */
//		public OrbitPlayerMain(OrbitViewerMain OrbitViewerMain) {
//			this.OrbitViewerMain = OrbitViewerMain;
//		}
//		
//		/**
//		 * Play forever
//		 */
//		public void run() {
//			while (true) {
//				try {
//					Thread.sleep(50);
//				} catch (InterruptedException e) {
//					break;
//				}
//				ATime atime = OrbitViewerMain.getAtime();
//				atime.changeDate(OrbitViewerMain.timeStep, OrbitViewerMain.playDirection);
//				OrbitViewerMain.setNewDate(atime);
//			}
//		}
//	}
//

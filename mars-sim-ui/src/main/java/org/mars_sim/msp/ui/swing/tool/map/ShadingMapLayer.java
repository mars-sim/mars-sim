/**
 * Mars Simulation Project
 * ShadingMapLayer.java
 * @version 3.1.0 2017-10-05
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.map;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.MemoryImageSource;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.ui.swing.tool.navigator.MarsMap;

/**
 * The ShadingMapLayer is a graphics layer to display twilight and night time
 * shading.
 */
public class ShadingMapLayer implements MapLayer {

//	private static String CLASS_NAME = "org.mars_sim.msp.ui.swing.tool.map.ShadingMapLayer";
//	private static Logger logger = Logger.getLogger(CLASS_NAME);
 	private static Logger logger = Logger.getLogger(ShadingMapLayer.class.getName());
 	
 	private static double rho = CannedMarsMap.PIXEL_RHO;

	// Domain data
	private int[] shadingArray;
	
	private static SurfaceFeatures surfaceFeatures;

	private Component displayComponent;

	/**
	 * Constructor
	 * 
	 * @param displayComponent the display component.
	 */
	public ShadingMapLayer(Component displayComponent) {
		surfaceFeatures = Simulation.instance().getMars().getSurfaceFeatures();
		
		this.displayComponent = displayComponent;
		
		shadingArray = new int[Map.MAP_VIS_WIDTH * Map.MAP_VIS_HEIGHT];
	}

	/**
	 * Displays the layer on the map image.
	 * 
	 * @param mapCenter the location of the center of the map.
	 * @param mapType   the type of map.
	 * @param g         graphics context of the map display.
	 */
	public void displayLayer(Coordinates mapCenter, String mapType, Graphics g) {

//		boolean nightTime = false;
	
		Coordinates location = new Coordinates(0D, 0D);
		
		int sunlightInt = (int) (127 * surfaceFeatures.getSurfaceSunlightRatio(mapCenter));

		if (sunlightInt < 36) {
//			nightTime = true;
//		}
//		
//		if (nightTime) {
//			g.setColor(new Color(0, 0, 0, 128));
//			g.fillRect(0, 0, Map.MAP_VIS_WIDTH, Map.MAP_VIS_HEIGHT);
//		}
//		
//		else {
			int centerX = MarsMap.MAP_W / 2;
			int centerY = centerX;

			// Coordinates sunDirection = orbitInfo.getSunDirection();

			int shadeColor = ((127 - sunlightInt) << 24) & 0xFF000000;
			
			for (int x = 0; x < Map.MAP_VIS_WIDTH; x += 2) {
				for (int y = 0; y < Map.MAP_VIS_HEIGHT; y += 2) {
					mapCenter.convertRectToSpherical(x - centerX, y - centerY, rho, location);

					shadingArray[x + (y * Map.MAP_VIS_WIDTH)] = shadeColor;
					shadingArray[x + 1 + (y * Map.MAP_VIS_WIDTH)] = shadeColor;
					if (y < Map.MAP_VIS_HEIGHT - 1) {
						shadingArray[x + ((y + 1) * Map.MAP_VIS_WIDTH)] = shadeColor;
						shadingArray[x + 1 + ((y + 1) * Map.MAP_VIS_WIDTH)] = shadeColor;
					}
				}
			}
			
			// Create shading image for map
			Image shadingMap = displayComponent.createImage(
					new MemoryImageSource(Map.MAP_VIS_WIDTH, Map.MAP_VIS_HEIGHT, shadingArray, 0, Map.MAP_VIS_WIDTH));

			MediaTracker mt = new MediaTracker(displayComponent);
			mt.addImage(shadingMap, 0);
			try {
				mt.waitForID(0);
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE, "ShadingMapLayer interrupted: " + e);
			}

			// Draw the shading image
			g.drawImage(shadingMap, 0, 0, displayComponent);
		}

	}
}
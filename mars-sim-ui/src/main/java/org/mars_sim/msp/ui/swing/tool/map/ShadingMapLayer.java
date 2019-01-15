/**
 * Mars Simulation Project
 * ShadingMapLayer.java
 * @version 3.1.0 2017-10-05
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.map;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.MemoryImageSource;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.Mars;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.ui.swing.tool.navigator.MarsGlobe;

/**
 * The ShadingMapLayer is a graphics layer to display twilight and night time
 * shading.
 */
public class ShadingMapLayer implements MapLayer {

	private static String CLASS_NAME = "org.mars_sim.msp.ui.swing.tool.map.ShadingMapLayer";

	private static Logger logger = Logger.getLogger(CLASS_NAME);

	// Domain data
	private SurfaceFeatures surfaceFeatures;
	private int[] shadingArray;
	private Component displayComponent;

	/**
	 * Constructor
	 * 
	 * @param displayComponent the display component.
	 */
	public ShadingMapLayer(Component displayComponent) {

		Mars mars = Simulation.instance().getMars();
		surfaceFeatures = mars.getSurfaceFeatures();
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

		int centerX = MarsGlobe.map_width / 2;
		int centerY = centerX;

		// Coordinates sunDirection = orbitInfo.getSunDirection();

		double rho = CannedMarsMap.PIXEL_RHO;

		boolean nightTime = true;
		boolean dayTime = true;
		Coordinates location = new Coordinates(0D, 0D);
		for (int x = 0; x < Map.MAP_VIS_WIDTH; x += 2) {
			for (int y = 0; y < Map.MAP_VIS_HEIGHT; y += 2) {
				mapCenter.convertRectToSpherical(x - centerX, y - centerY, rho, location);
				double sunlight = surfaceFeatures.getSurfaceSunlight(location);
				// double sunlight = surfaceFeatures.getSolarIrradiance(location) /
				// SurfaceFeatures.MEAN_SOLAR_IRRADIANCE;
				if (sunlight > 1D) {
					sunlight = 1D;
				}
				int sunlightInt = (int) (127 * sunlight);
				int shadeColor = ((127 - sunlightInt) << 24) & 0xFF000000;

				shadingArray[x + (y * Map.MAP_VIS_WIDTH)] = shadeColor;
				shadingArray[x + 1 + (y * Map.MAP_VIS_WIDTH)] = shadeColor;
				if (y < Map.MAP_VIS_HEIGHT - 1) {
					shadingArray[x + ((y + 1) * Map.MAP_VIS_WIDTH)] = shadeColor;
					shadingArray[x + 1 + ((y + 1) * Map.MAP_VIS_WIDTH)] = shadeColor;
				}

				if (sunlight > 0)
					nightTime = false;
				if (sunlight < 127)
					dayTime = false;
			}
		}

		if (nightTime) {
			g.setColor(new Color(0, 0, 0, 128));
			g.fillRect(0, 0, Map.MAP_VIS_WIDTH, Map.MAP_VIS_HEIGHT);
		} else if (!dayTime) {
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
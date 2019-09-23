/**
 * Mars Simulation Project
 * AreothermalMap.java
 * @version 3.1.0 2017-10-03
 * @author Scott Davis
 */
package org.mars_sim.msp.core.mars;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;

/**
 * A map of areothermal power generation potential on the Martian surface.
 */
//@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = As.PROPERTY, property = "@class")
public class AreothermalMap implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(AreothermalMap.class.getName());

	// Static members.
	private static final String VOLCANIC_IMG = Msg.getString("RandomMineralMap.image.volcanic"); //$NON-NLS-1$
	
	private static final int W = 300;
	private static final int H = 150;
	
	// Data members
	
	private Set<Coordinates> hotspots;
	private Map<Coordinates, Double> areothermalPotentialCache;

	/**
	 * Constructor.
	 */
	public AreothermalMap() {
		// Load the areothermal hot spots.
		loadHotspots();
	}

	/**
	 * Load areothermal hot spots from volcanic map image.
	 */
	private void loadHotspots() {
		hotspots = new HashSet<Coordinates>(700);
		URL imageMapURL = getClass().getResource("/images/" + VOLCANIC_IMG);
		ImageIcon mapIcon = new ImageIcon(imageMapURL);
		Image mapImage = mapIcon.getImage();

		int[] mapPixels = new int[W * H];
		PixelGrabber grabber = new PixelGrabber(mapImage, 0, 0, W, H, mapPixels, 0, W);
		try {
			grabber.grabPixels();
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "grabber error" + e);
		}
		if ((grabber.status() & ImageObserver.ABORT) != 0)
			logger.severe("grabber error");

		for (int x = 0; x < H; x++) {
			for (int y = 0; y < W; y++) {
				int pixel = mapPixels[(x * W) + y];
				Color color = new Color(pixel);
				if (Color.white.equals(color)) {
					double pixel_offset = (Math.PI / 150D) / 2D;
					double phi = (((double) x / 150D) * Math.PI) + pixel_offset;
					double theta = (((double) y / 150D) * Math.PI) + Math.PI + pixel_offset;
					if (theta > (2D * Math.PI))
						theta -= (2D * Math.PI);
					hotspots.add(new Coordinates(phi, theta));
				}
			}
		}
	}

	/**
	 * Gets the areothermal heat potential for a given location.
	 * 
	 * @param location the coordinate location.
	 * @return areothermal heat potential as percentage (0% - low, 100% - high).
	 */
	public double getAreothermalPotential(Coordinates location) {
		double result = 0D;

		// Load hotspots if not loaded already.
		if (hotspots == null)
			loadHotspots();

		// Initialize areothermal potential cache.
		if (areothermalPotentialCache == null)
			areothermalPotentialCache = new HashMap<Coordinates, Double>();

		// Check if location's areothermal potential has been cached.
		if (areothermalPotentialCache.containsKey(location)) {
			result = areothermalPotentialCache.get(location);
		} else {
			// Add heat contribution from each hot spot.
			Iterator<Coordinates> i = hotspots.iterator();
			while (i.hasNext()) {
				Coordinates hotspot = i.next();
				double distance = location.getDistance(hotspot);
				double pixelRadius = (Mars.MARS_CIRCUMFERENCE / W) / 2D;

				double a = 25D; // value at pixel radius.
				double b = 15D; // ratio max / ratio mid.
				double T = pixelRadius; // max distance - mid distance.
				double expo = (distance - pixelRadius) / T;
				double heat = 100D - (a * Math.pow(b, expo));
				if (heat < 0D)
					heat = 0D;
				result += heat;
			}

			// Maximum areothermal potential should be 100%.
			if (result > 100D)
				result = 100D;
			if (result < 0D)
				result = 0D;

			// Add location's areothermal potential to cache.
			areothermalPotentialCache.put(new Coordinates(location), result);
		}

		return result;
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		hotspots.clear();
		hotspots = null;
		if (areothermalPotentialCache != null) {

			areothermalPotentialCache.clear();
			areothermalPotentialCache = null;
		}
	}
}
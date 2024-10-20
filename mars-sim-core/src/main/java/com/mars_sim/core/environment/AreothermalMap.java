/*
 * Mars Simulation Project
 * AreothermalMap.java
 * @date 2023-06-14
 * @author Scott Davis
 */
package com.mars_sim.core.environment;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.mineral.RandomMineralFactory;
import com.mars_sim.core.tool.Msg;

/**
 * A map of areothermal power generation potential on the Martian surface.
 */
public class AreothermalMap implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

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
		hotspots = RandomMineralFactory.getTopoRegionSet(VOLCANIC_IMG, W, H);
	}

	/**
	 * Gets the areothermal heat potential for a given location.
	 * 
	 * @param location the coordinate location.
	 * @return areothermal heat potential as percentage (0% - low, 100% - high).
	 */
	public double getAreothermalPotential(Coordinates location) {
		double result = 0D;

		// Initialize areothermal potential cache.
		if (areothermalPotentialCache == null)
			areothermalPotentialCache = new HashMap<>();

		// Check if location's areothermal potential has been cached.
		if (areothermalPotentialCache.containsKey(location)) {
			result = areothermalPotentialCache.get(location);
		} else {
			// Add heat contribution from each hot spot.
			Iterator<Coordinates> i = hotspots.iterator();
			while (i.hasNext()) {
				Coordinates hotspot = i.next();
				double distance = location.getDistance(hotspot);
				double pixelRadius = (Coordinates.MARS_CIRCUMFERENCE / W) / 2D;

				double a = 25D; // value at pixel radius.
				double b = 15D; // ratio max / ratio mid.
				double t = pixelRadius; // max distance - mid distance.
				double expo = (distance - pixelRadius) / t;
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
			areothermalPotentialCache.put(location, result);
		}

		return result;
	}

	/**
	 * Prepares object for garbage collection.
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

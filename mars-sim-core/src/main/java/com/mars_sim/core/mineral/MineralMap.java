/*
 * Mars Simulation Project
 * MineralMap.java
 * @date 2024-08-30
 * @author Scott Davis
 */

package com.mars_sim.core.mineral;

import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.SurfaceManager;
import com.mars_sim.core.tool.RandomUtil;

/**
 * A randomly generated mineral map of Mars.
 */
public class MineralMap implements Serializable {

	/*
	 * Additional Map Resources
	 * 
	 * (A). Arizona State University (ASU) Mars Space FLight Facility
	 * 
	 * 1. GLobal Data Sets - THEMIS, MOLA, TES THermal Inertia, etc.
	 * 2. TES Mineral Maps - Hematite, K-Feldspar, Plagioclase, High/Low-Ca Pyroxene, Olivine, Si, Quartz, Amphibole
	 * 3. Viking IRTM
	 * 
	 * Reference : 
	 * 1. https://mars.asu.edu/data/
	 * 
	 * 
	 * 
	 * (B). ESA's 5 minerals maps 
	 * 
	 * 1. Ferric Oxide - https://www.esa.int/ESA_Multimedia/Images/2013/05/Mars_ferric_oxide_map
 	 * 2. Dust - https://www.esa.int/ESA_Multimedia/Images/2013/05/Mars_dust_map
	 * 3. Pyroxene - https://www.esa.int/ESA_Multimedia/Images/2013/05/Mars_pyroxene_map
	 * 4. Hydrated Mineral - https://www.esa.int/ESA_Multimedia/Images/2013/05/Mars_hydrated_mineral_map
	 * 5. Mineralogy - https://www.esa.int/ESA_Multimedia/Images/2013/05/Mars_mineralogy
	 * 
	 * Reference: 
	 * 1. https://www.esa.int/ESA_Multimedia/Videos/2013/05/Mars_mineral_globe
	 * 2. https://phys.org/news/2022-08-mars-invaluable-future-exploration.html
	 * 
	 * 
	 * 
	 * (C). Rainbow Map
	 * 
	 * A new, high-resolution mineral map of Mars was released in July 2022. Dubbed the “Rainbow Map,” it covers 86%
	 * of the Red Planet’s surface and reveals the distribution of dozens of key minerals. This 5.6-gigapixel map 
	 * provides a more detailed understanding of Mars’ geological composition.
	 * 
	 * Compact Reconnaissance Imaging Spectrometer for Mars (CRISM) instrument on NASA’s Mars Reconnaissance Orbiter (MRO) 
	 * spacecraft has a new near-global map conveying the mineral composition of the Martian surface.
	 * 
	 * Using detectors that see visible and infrared wavelengths, the CRISM team has previously produced high-resolution 
	 * mineral maps that provide a record of the formation of the Martian crust and where and how it was altered by water.
	 * 
	 * These maps have been crucial to helping scientists understand how lakes, streams, and groundwater shaped the planet. 
	 * 
	 * The different false-colors of the CRISM images show the presence of iron-oxides, iron-bearing minerals, important
	 * rock-forming minerals like pyroxene, water-altered minerals and carbonates on the planet's surface.
	 * 
	 * May use CRISM's map to deduce distribution of dozens of key minerals in future.
	 * 
	 * Reference: 
	 * 1. https://www.forbes.com/sites/davidbressan/2022/07/05/new-mineralogical-map-of-mars-online/
	 * 2. https://www.jhuapl.edu/news/news-releases/220621-crism-team-releases-new-global-map-of-mars-at-instruments-close
	 * 3. https://www.jhuapl.edu/news/news-releases/220621-crism-team-releases-new-global-map-of-mars-at-instruments-close
	 */

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	public static final double MIN_DISTANCE = 0.5;
	
	// A map of all mineral concentrations
	private SurfaceManager<MineralDeposit> allMinerals;
	
	private List<MineralType> types;
	
	/**
	 * Create a mineral map that is based on a configuration
	 * @param mineralMapConfig Defines details of map
	 */
	MineralMap(MineralMapConfig mineralMapConfig) {
		// Types are locked down when te map is created
		types = mineralMapConfig.getMineralTypes();

		allMinerals = new SurfaceManager<>();
	}

	/**
	 * Add a mineral concentration to a specific location
	 * @param locn
	 * @param mineral
	 * @param conc
	 */
	void addMineral(Coordinates locn, MineralType mineral, int conc) {
		var found = allMinerals.getFeature(locn);
		if (found == null) {
			found = new MineralDeposit(locn);
			allMinerals.addFeature(found);
		}
		found.adjustMineral(mineral.getName(), conc);
	}

	/**
	 * Gets all of the mineral concentrations at a given location.
	 * 
	 * @param location  the coordinate
	 * @param rho		the map scale
	 * @return map of mineral types and percentage concentration (0 to 100.0)
	 */
	public Map<String, Integer> getAllMineralConcentrations(Coordinates location) {
		var found = allMinerals.getFeature(location);
		if (found != null) {
			return found.getConcentrations();
		}
		return Collections.emptyMap();
	}

	/**
	 * Gets the mineral concentration at a given location.
	 * @note Called by SurfaceFeatures's addExploredLocation()
	 * and ExploreSite's improveSiteEstimates()
	 * 
	 * @param string the mineral type (see MineralMap.java)
	 * @param location   the coordinate location.
	 * @return percentage concentration (0 to 100.0)
	 */
	public double getMineralConcentration(String string, Coordinates location) {
		
		var details = allMinerals.getFeature(location);
		if (details == null) {
			return 0;
		}
		return details.getConcentration(string);
	}
	
	/**
	 * Get the mneral types in this map
	 * @return
	 */
	public List<MineralType> getTypes() {
		return types;
	}

	/**
	 * Generates a map of Mineral locations from a starting location.
	 * 
	 * @param startingLocation
	 * @param range
	 * @param foundLocations
	 * @return
	 */
	private Map<Coordinates, Double> generateMineralLocations(Coordinates startingLocation, double range, 
			Collection<Coordinates> foundLocations) {

		Map<Coordinates, Double> locales = new HashMap<>();
		double angle = range/Coordinates.KM_PER_RADIAN_AT_EQUATOR;
		var found = allMinerals.getFeatures(startingLocation, angle);

		// Exclude those alrready found
		for(var f : found) {
			var c = f.getLocation();
			if (!foundLocations.contains(c)) {
				double distance = startingLocation.getDistance(c);
				if (range >= distance && distance >= MIN_DISTANCE) {
					locales.put(c, distance);
				}
			}
		}
		return locales;
	}
	
	/**
	 * Finds a random location with mineral concentrations from a starting location
	 * and within a distance range.
	 * 
	 * @param startingLocation the starting location.
	 * @param range            the distance range (km).
	 * @param foundLocations
	 * @return location and distance pair
	 */
	public Map.Entry<Coordinates, Double> findRandomMineralLocation(Coordinates startingLocation, double range,
			Collection<Coordinates> foundLocations) {

		Map<Coordinates, Double> locales = generateMineralLocations(startingLocation, range, foundLocations);
		
		int size = locales.size();
		if (size <= 0) {
			return null;
		}
			
		Map<Coordinates, Double> weightedMap = new HashMap<>();
		
		for (var c: locales.entrySet()) {
			double distance = c.getValue();
			if (distance < MIN_DISTANCE) {
				continue;
			}
			double prob = 0;
			double delta = range - distance + 100;
			if (delta > 0) {
				prob = delta * delta / range / range;
			}
			
			if (distance >= MIN_DISTANCE && prob > 0) {
				// Fill up the weight map
				weightedMap.put(c.getKey(), prob);
			}
		}

		// Note: May use getRandomRegressionInteger to make the coordinates to be potentially closer

		// Choose one with weighted randomness 
		Coordinates chosen = RandomUtil.getWeightedRandomObject(weightedMap);

		if (weightedMap.isEmpty() || chosen == null) {
			return null;
		}
		
		double chosenDist = locales.get(chosen);
		return new SimpleEntry<>(chosen, chosenDist);
	}

	/**
	 * Get all the mineral deposit within a range of a specific center point. The deposits are filtered
	 * by a list of valid minerals
	 * @param center
	 * @param arcAngle
	 * @param minerals
	 * @return
	 */
    public List<MineralDeposit> getDeposits(Coordinates center, double arcAngle,
								Set<String> minerals) {
        return allMinerals.getFeatures(center, arcAngle).stream()
					.filter(f -> isPresent(f, minerals))
					.toList();
    }

	/**
	 * Is the specified mineral present in the deposit
	 * @param f
	 * @param minerals
	 * @return
	 */
	private static boolean isPresent(MineralDeposit f, Set<String> minerals) {
		return f.getConcentrations().keySet().stream()
				.anyMatch(minerals::contains);
	}
}

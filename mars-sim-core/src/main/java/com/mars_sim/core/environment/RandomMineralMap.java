/*
 * Mars Simulation Project
 * RandomMineralMap.java
 * @date 2023-06-14
 * @author Scott Davis
 */

package com.mars_sim.core.environment;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.net.URL;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.stream.Stream;

import javax.swing.ImageIcon;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.environment.MineralMapConfig.MineralType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.Direction;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;

/**
 * A randomly generated mineral map of Mars.
 */
public class RandomMineralMap implements MineralMap {

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

	private static final SimLogger logger = SimLogger.getLogger(RandomMineralMap.class.getName());
	
	public static final double MIN_DISTANCE = 0.5;
	 
	public static final String TOPO_MAP_FOLDER = "/topography/";
	
	private final int W = 300;
	private final int H = 150;
	
	private final int NUM_REGIONS = 100;

	private final int REGION_FACTOR = 1500;
	private final int NON_REGION_FACTOR = 50;
	
	// The resolution of each pixel is approximately 5.9219 m (or 0.00592 km) 
//	private final double PIXEL_RADIUS = Coordinates.KM_PER_4_DECIMAL_DEGREE_AT_EQUATOR; //(Coordinates.MARS_CIRCUMFERENCE / W) / 2D;
	
	// Topographical Region Strings
	private final String CRATER_IMG = Msg.getString("RandomMineralMap.image.crater"); //$NON-NLS-1$
	private final String VOLCANIC_IMG = Msg.getString("RandomMineralMap.image.volcanic"); //$NON-NLS-1$
	private final String SEDIMENTARY_IMG = Msg.getString("RandomMineralMap.image.sedimentary"); //$NON-NLS-1$

	private final String CRATER_REGION = "crater";
	private final String VOLCANIC_REGION = "volcanic";
	private final String SEDIMENTARY_REGION = "sedimentary";

	// Frequency Strings
	private final String COMMON_FREQUENCY = "common";
	private final String UNCOMMON_FREQUENCY = "uncommon";
	private final String RARE_FREQUENCY = "rare";
	private final String VERY_RARE_FREQUENCY = "very rare";

	private final double PHI_LIMIT = Math.PI / 7;
	private final double ANGLE_LIMIT = .01;
	
	private String[] mineralTypeNames;
	
	/** A map of the mineral name and its rgb color string. */
	private SortedMap<String, String> mineralColorMap;
	// A map of all mineral concentrations
	private Map<Coordinates, Map<String, Integer>> allMineralsByLoc;
	
	private transient Set<Coordinates> allLocations;
	
	private transient MineralMapConfig mineralMapConfig;
	
	private transient UnitManager unitManager;
	
	/**
	 * Constructor.
	 */
	RandomMineralMap() {
	
		allMineralsByLoc = new HashMap<>();
		
		mineralColorMap = new TreeMap<>();
		// Determine mineral concentrations.
		determineMineralConcentrations();
		
		allLocations = allMineralsByLoc.keySet();
	}

	/**
	 * Determines all mineral concentrations.
	 */
	private void determineMineralConcentrations() {
		// Load topographical regions.
		Set<Coordinates> craterRegionSet = getTopoRegionSet(CRATER_IMG);
		Set<Coordinates> volcanicRegionSet = getTopoRegionSet(VOLCANIC_IMG);
		Set<Coordinates> sedimentaryRegionSet = getTopoRegionSet(SEDIMENTARY_IMG);

		if (mineralMapConfig == null)
			mineralMapConfig = SimulationConfig.instance().getMineralMapConfiguration();
		
		List<MineralType> minerals = new ArrayList<>(mineralMapConfig.getMineralTypes());
		
		Collections.shuffle(minerals);

		Iterator<MineralType> i = minerals.iterator();
		while (i.hasNext()) {
			MineralType mineralType = i.next();
			
			// Save the color string
			mineralColorMap.put(mineralType.toString(), mineralType.getColorString());
			
			// Create super set of topographical regions.
			Set<Coordinates> regionSet = new HashSet<>(NUM_REGIONS);

			// Each mineral has unique abundance in each of the 3 regions
			Iterator<String> j = mineralType.getLocales().iterator();
			while (j.hasNext()) {
				String locale = j.next().trim();
				if (CRATER_REGION.equalsIgnoreCase(locale))
					regionSet.addAll(craterRegionSet);
				else if (VOLCANIC_REGION.equalsIgnoreCase(locale))
					regionSet.addAll(volcanicRegionSet);
				else if (SEDIMENTARY_REGION.equalsIgnoreCase(locale))
					regionSet.addAll(sedimentaryRegionSet);
			}
			
			int length = regionSet.size();
			Coordinates[] regionArray = regionSet.toArray(new Coordinates[length]);
		
			// Have one region array for each of 10 types of minerals 
			// regionArray between 850 and 3420 for each mineral
			
			// For now start with a random concentration between 0 to 100
			int conc = RandomUtil.getRandomInt(100);
			
			// Determine individual mineral iteration.
			int numIteration = calculateIteration(mineralType, true, length);
					
			// Get the new remainingConc
			double remainingConc = 1.0 * conc * numIteration;
		
			for (int x = 0; x < numIteration; x++) {
				
				// Determine individual mineral concentrations.
				remainingConc = createMinerals(remainingConc, 
						regionArray[RandomUtil.getRandomInt(length - 1)], x, numIteration, conc, mineralType.name);
								
				if (remainingConc <= 0.0) 
					break;
			}		
		} // end of iterating MineralType
	}

	/**
	 * Generates mineral concentrations.
	 * 
	 * @param remainingConc
	 * @param oldLocation
	 * @param size
	 * @param conc
	 * @param mineralName
	 */
	public double createMinerals(double remainingConc, Coordinates oldLocation, int x, int last, double conc, String mineralName) {
		Direction direction = new Direction(RandomUtil.getRandomDouble(Math.PI * 2D));
		// Spread it over a 10 km radius
		double distance = RandomUtil.getRandomDouble(1, 20);
		Coordinates newLocation = oldLocation.getNewLocation(direction, distance);
		double concentration = 0;

		if (x != last - 1)
			concentration = Math.round(RandomUtil.getRandomDouble(conc *.75, conc));
		else
			concentration = Math.round(remainingConc);

		remainingConc -= concentration;					

		if (concentration > 100)
			concentration = 100;
		if (concentration < 0) {
			concentration = 0;
		}
		
		Map<String, Integer> map = new HashMap<>();
		
		if (allMineralsByLoc.containsKey(newLocation)) {
			
			if (allMineralsByLoc.get(oldLocation).containsKey(mineralName)) {
				double oldConc = allMineralsByLoc.get(oldLocation).get(mineralName);
				map.put(mineralName, (int)Math.round(.5 * (oldConc + concentration)));
			}
			else {
				map.put(mineralName, (int)Math.round(concentration));
			}
		}
		else {
			// Save the mineral conc at oldLocation for the first time
			map.put(mineralName, (int)Math.round(concentration));
		}
		
		allMineralsByLoc.put(newLocation, map);

		return remainingConc;
	}
	
	/**
	 * Creates concentration at a specified location.
	 * 
	 * @param location
	 */
	public void createLocalConcentration(Coordinates location) {
			
		if (mineralMapConfig == null)
			mineralMapConfig = SimulationConfig.instance().getMineralMapConfiguration();
		
		List<MineralType> minerals = new ArrayList<>(mineralMapConfig.getMineralTypes());
		Collections.shuffle(minerals);
	
		Iterator<MineralType> i = minerals.iterator();
		while (i.hasNext()) {
			MineralType mineralType = i.next();

			// For now start with a random concentration between 0 to 25
			int conc = RandomUtil.getRandomInt(5, 25);
			// Determine individual mineral iteration.
			int numIteration = calculateIteration(mineralType, false, 1);

			// Get the new remainingConc
			double remainingConc = 1.0 * conc * numIteration;
			
			for (int x = 0; x < numIteration; x++) {
				// Determine individual mineral concentrations
				remainingConc = createMinerals(remainingConc, location, x, 
						numIteration, conc, mineralType.name);	
				
				if (remainingConc <= 0.0) 
					break;
			}
		}
	}
	
	/**
	 * Calculate the number of interaction in determining the mineral concentration.
	 * 
	 * @param mineralType
	 * @param isGlobal
	 * @param length
	 * @return
	 */
	private int calculateIteration(MineralType mineralType, boolean isGlobal, int length) {
		int num = 0;
		if ((isGlobal)) {
			num = (int)(Math.round(RandomUtil.getRandomDouble(.75, 1.25) 
					* REGION_FACTOR / 1800 * length
					/ getFrequencyModifier(mineralType.frequency)));
		}
		else {
			num = (int)(Math.round(RandomUtil.getRandomDouble(.75, 1.25) 
					* NON_REGION_FACTOR * 2 * length
					/ getFrequencyModifier(mineralType.frequency)));
		}
		return num;
	}
	
	/**
	 * Gets the dividend due to frequency of mineral type.
	 * 
	 * @param frequency the frequency ("common", "uncommon", "rare" or "very rare").
	 * @return frequency modifier.
	 */
	private float getFrequencyModifier(String frequency) {
		float result = 1F;
		if (COMMON_FREQUENCY.equalsIgnoreCase(frequency.trim()))
			result = 10; // 1F;
		else if (UNCOMMON_FREQUENCY.equalsIgnoreCase(frequency.trim()))
			result = 30; // 5F;
		else if (RARE_FREQUENCY.equalsIgnoreCase(frequency.trim()))
			result = 60; // 10F;
		else if (VERY_RARE_FREQUENCY.equalsIgnoreCase(frequency.trim()))
			result = 90; // 15F;
		return result;
	}

	/**
	 * Gets a set of location coordinates representing a topographical region.
	 * 
	 * @param imageMapName the topographical region map image.
	 * @return set of location coordinates.
	 */
	private Set<Coordinates> getTopoRegionSet(String imageMapName) {
		Set<Coordinates> result = new HashSet<>(3000);
		URL imageMapURL = getClass().getResource(TOPO_MAP_FOLDER + imageMapName);
		ImageIcon mapIcon = new ImageIcon(imageMapURL);
		Image mapImage = mapIcon.getImage();

		int[] mapPixels = new int[W * H];
		PixelGrabber topoGrabber = new PixelGrabber(mapImage, 0, 0, W, H, mapPixels, 0, W);
		try {
			topoGrabber.grabPixels();
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "grabber error" + e);
			// Restore interrupted state
		    Thread.currentThread().interrupt();
		}
		if ((topoGrabber.status() & ImageObserver.ABORT) != 0)
			logger.info("grabber error");

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
					result.add(new Coordinates(phi, theta));
				}
			}
		}

		return result;
	}

	/**
	 * Updates the set of minerals to be displayed.
	 * 
	 * @param availableMinerals
	 * @return
	 */
	private Set<String> updateMineralDisplay(Set<String> displayMinerals, Set<String> availableMinerals) {

		Set<String> intersection = new HashSet<>();
		availableMinerals.forEach((i) -> {
			if (displayMinerals.contains(i))
				intersection.add(i);
		});
		
		return intersection;
	}
	
	/**
	 * Gets some of the mineral concentrations at a given location.
	 * 
	 * @param mineralsDisplaySet 	a set of mineral strings.
	 * @param aLocation  a coordinate
	 * @param mag		 the map magnification factor
	 * @return map of mineral types and percentage concentration (0 to 100.0)
	 */
	public Map<String, Double> getSomeMineralConcentrations(Set<String> mineralsDisplaySet, Coordinates aLocation,
			double mag) {
		
		double angle = ANGLE_LIMIT;
		double magSQ = Math.sqrt(mag);
		
		Map<String, Double> newMap = new HashMap<>();
		
		boolean emptyMap = true;
		
		if (allLocations == null) {
			allLocations = allMineralsByLoc.keySet();
		}
		
		Iterator<Coordinates> i = allLocations.iterator();
		while (i.hasNext()) {
			Coordinates c = i.next();
	
			double phi = c.getPhi();
			double theta = c.getTheta();
			double phiDiff = Math.abs(aLocation.getPhi() - phi);
			double thetaDiff = Math.abs(aLocation.getTheta() - theta);
//			double halfPhiD = phiDiff / 2;
//			double halfThetaD = thetaDiff / 2;
//			double radius = Math.min(halfPhiD, halfThetaD);
//			double length = halfPhiD * halfPhiD + halfThetaD * halfThetaD;
//			boolean outsideCircle = length - radius * radius > 0;
				
			// Only take in what's within a certain boundary
			if (phi > PHI_LIMIT && phi < Math.PI - PHI_LIMIT
				&& phiDiff < angle && thetaDiff < angle) {
				
				Map<String, Integer> map = allMineralsByLoc.get(c);
				
				Set<String> mineralNames = updateMineralDisplay(mineralsDisplaySet, map.keySet());
				if (mineralNames.isEmpty()) {
					continue;
				}
		
				Iterator<String> j = mineralNames.iterator();
				while (j.hasNext()) {
					String mineralName = j.next();	
					
					double conc = 2 * map.get(mineralName);	
					// Tune the fuzzyRange to respond to the map magnification and mineral concentration
					double fuzzyRange = conc * magSQ;
									
					double deltaAngle = (phiDiff + thetaDiff) / 2;
					
					double radius = Math.max(phiDiff, thetaDiff);
				
					double limit = 1 - deltaAngle / radius;
					
					if (thetaDiff < angle && phiDiff < angle)	{
						
						double effect =  Math.sqrt(limit) * fuzzyRange;
					
						if (emptyMap) {
							newMap = new HashMap<>();
							emptyMap = false;
						}
						double newConc = 0D;
						if (newMap.containsKey(mineralName)) {
							// Load the total concentration
							newConc = newMap.get(mineralName);
						}
						
						newConc = newConc + effect;
						if (newConc > 100)
							newConc = 100;
						
						newMap.put(mineralName, newConc);
					}
				}
			}	
		}
		
		return newMap;
	}

	/**
	 * Gets all of the mineral concentrations at a given location.
	 * 
	 * @param location  the coordinate
	 * @param rho		the map scale
	 * @return map of mineral types and percentage concentration (0 to 100.0)
	 */
	public Map<String, Integer> getAllMineralConcentrations(Coordinates location) {
		if (allMineralsByLoc.isEmpty()
			|| !allMineralsByLoc.containsKey(location)) {
			
			return new HashMap<>();
		}
		else {
			return allMineralsByLoc.get(location);
		}
	}

	/**
	 * Gets the mineral concentration at a given location.
	 * @note Called by SurfaceFeatures's addExploredLocation()
	 * and ExploreSite's improveSiteEstimates()
	 * 
	 * @param mineralType the mineral type (see MineralMap.java)
	 * @param location   the coordinate location.
	 * @return percentage concentration (0 to 100.0)
	 */
	public double getMineralConcentration(String mineralType, Coordinates location) {
		
		Map<String, Integer> map = allMineralsByLoc.get(location);
		if (map == null || map.isEmpty()) {
			return 0;
		}
		else if (map.containsKey(mineralType)) {
			return map.get(mineralType);
		}
		else {
			return 0;
		}
	}

	/**
	 * Gets the color string of a mineral.
	 * 
	 * @param mineralName
	 * @return
	 */
	public String getColorString(String mineralName) {
		return mineralColorMap.get(mineralName);
	}
	
	/**
	 * Gets an array of all mineral type names.
	 * 
	 * @return array of name strings.
	 */
	public String[] getMineralTypeNames() {
		
		if (mineralTypeNames == null) {
		
			String[] result = new String[0];
			
			if (mineralMapConfig == null)
				mineralMapConfig = SimulationConfig.instance().getMineralMapConfiguration();
	
			try {
				List<MineralType> mineralTypes = mineralMapConfig.getMineralTypes();
				result = new String[mineralTypes.size()];
				for (int x = 0; x < mineralTypes.size(); x++)
					result[x] = mineralTypes.get(x).name;
			} catch (Exception e) {
				logger.severe("Error getting mineral types.", e);
			}
			
			Arrays.sort(result);
			
			mineralTypeNames = result;
			
			return result;
		}
		
		return mineralTypeNames;
	}

	/**
	 * Generates a map of Mineral locations from a starting location.
	 * 
	 * @param startingLocation
	 * @param range
	 * @param foundLocations
	 * @return
	 */
	public Map<Coordinates, Double> generateMineralLocations(Coordinates startingLocation, double range, 
			Collection<Coordinates> foundLocations) {

		Map<Coordinates, Double> locales = new HashMap<>();
		
		if (allLocations == null) {
			allLocations = allMineralsByLoc.keySet();
		}
		
		Iterator<Coordinates> i = allLocations.iterator();
		while (i.hasNext()) {
			Coordinates c = i.next();
			if (!foundLocations.contains(c)) {
				double distance = Coordinates.computeDistance(startingLocation, c);
				if (range >= distance && distance >= MIN_DISTANCE) {
					locales.put(c, distance);
				}
			}
		}

		int size = locales.size();
		
		if (unitManager == null)
			unitManager = Simulation.instance().getUnitManager();
		
		logger.info(unitManager.findSettlement(startingLocation), 30_000L, 
				"Found " + size 
				+ " potential mineral sites to explore within " + Math.round(range * 10.0)/10.0 + " km.");
	
		return locales;
	}
	
	public <K, V> Stream<K> keys(Map<K, V> map, V value) {
	    return map
	      .entrySet()
	      .stream()
	      .filter(entry -> value.equals(entry.getValue()))
	      .map(Map.Entry::getKey);
	}
	
	/**
	 * Finds a random location with mineral concentrations from a starting location
	 * and within a distance range.
	 * 
	 * @param startingLocation the starting location.
	 * @param range            the distance range (km).
	 * @param sol			the mission sol
	 * @param foundLocations
	 * @return location and distance pair
	 */
	public Map.Entry<Coordinates, Double> findRandomMineralLocation(Coordinates startingLocation, double range, int sol,
			Collection<Coordinates> foundLocations) {

		Map<Coordinates, Double> locales = generateMineralLocations(startingLocation, range, foundLocations);
		
		int size = locales.size();
		if (size <= 0) {
			return null;
		}
			
		Map<Coordinates, Double> weightedMap = new HashMap<>();
		
		for (Coordinates c: locales.keySet()) {
			double distance = locales.get(c);
			if (distance < MIN_DISTANCE) {
				continue;
			}
			double prob = 0;
			double delta = range - distance + Math.max(0, 100 - sol);
			if (delta > 0) {
				prob = delta * delta / range / range;
			}
			
			if (distance >= MIN_DISTANCE && prob > 0) {
				// Fill up the weight map
				weightedMap.put(c, prob);
//				System.out.println("c: " + c + "  d: " + distance);
			}
		}

		// Note: May use getRandomRegressionInteger to make the coordinates to be potentially closer

		// Choose one with weighted randomness 
		Coordinates chosen = RandomUtil.getWeightedRandomObject(weightedMap);

		if (weightedMap.isEmpty() || chosen == null) {
			logger.info(unitManager.findSettlement(startingLocation), "No site of interest found.");
			return null;
		}
		
		double chosenDist = locales.get(chosen);

		logger.info(unitManager.findSettlement(startingLocation), 30_000L, 
				"Located a mineral site at " + chosen + " (" + Math.round(chosenDist * 10.0)/10.0 + " km).");

//		System.out.println("#: " + weightedMap.size());
		
		Map.Entry<Coordinates, Double> result = new SimpleEntry<>(chosen, chosenDist);

		return result;
	}

	@Override
	public void destroy() {
		allMineralsByLoc.clear();
		allMineralsByLoc = null;
		allLocations.clear();
		allLocations = null;
	}
}

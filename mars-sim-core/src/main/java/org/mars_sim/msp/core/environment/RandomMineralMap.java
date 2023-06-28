/*
 * Mars Simulation Project
 * RandomMineralMap.java
 * @date 2023-06-14
 * @author Scott Davis
 */

package org.mars_sim.msp.core.environment;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.ImageIcon;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Direction;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.environment.MineralMapConfig.MineralType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * A randomly generated mineral map of Mars.
 */
public class RandomMineralMap implements Serializable, MineralMap {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final SimLogger logger = SimLogger.getLogger(RandomMineralMap.class.getName());

	private static final int W = 300;
	private static final int H = 150;
	
	private static final int NUM_REGIONS = 50;

	private static final int REGION_FACTOR = 1500;
	private static final int NON_REGION_FACTOR = 50;
	
	private static final double PIXEL_RADIUS = Coordinates.KM_PER_4_DECIMAL_DEGREE_AT_EQUATOR; //(Coordinates.MARS_CIRCUMFERENCE / W) / 2D;
	
	private static final String IMAGES_FOLDER = "/images/";
	
	// Topographical Region Strings
	private static final String CRATER_IMG = Msg.getString("RandomMineralMap.image.crater"); //$NON-NLS-1$
	private static final String VOLCANIC_IMG = Msg.getString("RandomMineralMap.image.volcanic"); //$NON-NLS-1$
	private static final String SEDIMENTARY_IMG = Msg.getString("RandomMineralMap.image.sedimentary"); //$NON-NLS-1$

	private static final String CRATER_REGION = "crater";
	private static final String VOLCANIC_REGION = "volcanic";
	private static final String SEDIMENTARY_REGION = "sedimentary";

	// Frequency Strings
	private static final String COMMON_FREQUENCY = "common";
	private static final String UNCOMMON_FREQUENCY = "uncommon";
	private static final String RARE_FREQUENCY = "rare";
	private static final String VERY_RARE_FREQUENCY = "very rare";

	private static final double LIMIT = Math.PI / 7;
	
//	private double angleCache;
	
	// A map of all mineral concentrations
	private Map<Coordinates, Map<String, Integer>> allMineralsByLocation;

	private String[] mineralTypeNames;
	
	private static Set<Coordinates> allLocations;
	
	private static MineralMapConfig mineralMapConfig = SimulationConfig.instance().getMineralMapConfiguration();
	
	/**
	 * Constructor.
	 */
	RandomMineralMap() {
	
		allMineralsByLocation = new HashMap<>();
		// Determine mineral concentrations.
		determineMineralConcentrations();
		
		allLocations = allMineralsByLocation.keySet();
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
		
			// Will have one region array for each of 10 types of minerals 
//				regionArray is between 850 and 3420 for each mineral
			
			// For now start with a random concentration between 0 to 100
			int conc = RandomUtil.getRandomInt(100);
			
			// Determine individual mineral concentrations.
			int concentrationNumber = calculateIteration(mineralType, true, length);
					
			// Get the new remainingConc by multiplying it with concentrationNumber; 
			double remainingConc = conc * concentrationNumber;
			
//				logger.info("regionArray size: " + length
//						+ "  concentrationNumber: " + concentrationNumber
//						+ "  remainingConc: " + remainingConc);
		
			for (int x = 0; x < concentrationNumber; x++) {

				remainingConc = createMinerals(remainingConc, 
						regionArray[RandomUtil.getRandomInt(length - 1)], x, concentrationNumber, conc, mineralType.name);
				
//				logger.info("  x: " + x + "  remainingConc: " + remainingConc);
				
				if (remainingConc <= 0.0) 
					break;
			}
				
			
		} // end of iterating MineralType

//		logger.info("Mineral Locations: " + allMineralsByLocation);
		logger.info("Global # of Mineral Locations: " + allMineralsByLocation.size());
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
		double distance = RandomUtil.getRandomDouble(PIXEL_RADIUS);
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
		
		if (allMineralsByLocation.containsKey(newLocation)) {
			
			if (allMineralsByLocation.get(oldLocation).containsKey(mineralName)) {
				double oldConc = allMineralsByLocation.get(oldLocation).get(mineralName);
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
		
		allMineralsByLocation.put(newLocation, map);

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

			// For now start with a random concentration between 0 to 100
			int conc = RandomUtil.getRandomInt(15);

			int concentrationNumber = calculateIteration(mineralType, false, 0);
			
			// Get the new remainingConc by multiplying it with concentrationNumber; 
			double remainingConc = conc * concentrationNumber;
			
			for (int x = 0; x < concentrationNumber; x++) {
				
				remainingConc = createMinerals(remainingConc, location, x, concentrationNumber, conc, mineralType.name);
				
//				logger.info(location +  " # of Local Mineral Locations: " + allMineralsByLocation.get(location).size());
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
	public int calculateIteration(MineralType mineralType, boolean isGlobal, int length) {
		int num = 0;
		if ((isGlobal)) {
			num = (int)(Math.round(RandomUtil.getRandomDouble(.75, 1.25) 
					* REGION_FACTOR / 1800 * length
					/ getFrequencyModifier(mineralType.frequency)));
		}
		else {
			num = (int)(Math.round(RandomUtil.getRandomDouble(.75, 1.25) 
					* NON_REGION_FACTOR * 2
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
//		[landrus, 26.11.09]: don't use the system classloader in a webstart env.
		URL imageMapURL = getClass().getResource(IMAGES_FOLDER + imageMapName);
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
	 * Gets all of the mineral concentrations at a given location.
	 * 
	 * @param mineralsDisplaySet 	a set of mineral strings.
	 * @param aLocation  a coordinate
	 * @param mag		 the map magnification factor
	 * @return map of mineral types and percentage concentration (0 to 100.0)
	 */
	public Map<String, Double> getSomeMineralConcentrations(Set<String> mineralsDisplaySet, Coordinates aLocation,
			double mag) {
		
		double angle = .025 / mag;
	
		Map<String, Double> newMap = new HashMap<>();
		
		boolean emptyMap = true;
		
		if (allLocations == null) {
			allLocations = allMineralsByLocation.keySet();
		}
		
		Iterator<Coordinates> i = allLocations.iterator();
		while (i.hasNext()) {
			Coordinates c = i.next();
	
			double concentrationPhi = c.getPhi();
			double concentrationTheta = c.getTheta();
			double phiDiff = Math.abs(aLocation.getPhi() - concentrationPhi);
			double thetaDiff = Math.abs(aLocation.getTheta() - concentrationTheta);

			// Only take in what's within a certain boundary
			if (concentrationPhi > LIMIT && concentrationPhi < Math.PI - LIMIT
				&& phiDiff < angle && thetaDiff < angle) {
				
				Map<String, Integer> map = allMineralsByLocation.get(c);
				
				Set<String> mineralNames = updateMineralDisplay(mineralsDisplaySet, map.keySet());
				if (mineralNames.isEmpty()) {
					continue;
				}
				
				double distance = aLocation.getDistance(c);
			
				Iterator<String> j = mineralNames.iterator();
				while (j.hasNext()) {
					String mineralName = j.next();	
					
					double effect = 0;
					double conc = map.get(mineralName);	
					// Tune the fuzzyRange to respond to the map magnification and mineral concentration
					double fuzzyRange = conc;
									
					if (distance < fuzzyRange)
						effect = (1D - (distance / fuzzyRange)) * conc;				
					
					if (effect > 0D) {
						
//						if (angleCache != angle) {
//							angleCache = angle;
//							logger.info("mag: " + Math.round(mag* 1000.0)/1000.0 
//									+ "  angle: "+ Math.round(angle * 1000.0)/1000.0
//									+ "  distance: " + Math.round(distance * 1000.0)/1000.0
//									+ "  conc: " + Math.round(conc * 1000.0)/1000.0
//									+ "  ratio: " + Math.round(distance / fuzzyRange * 1000.0)/1000.0
//									+ "  fuzzyRange: " + Math.round(fuzzyRange * 1000.0)/1000.0
//									+ "  effect: " + Math.round(effect * 1000.0)/1000.0) ;
//						}
						
						if (emptyMap) {
							newMap = new HashMap<>();
							emptyMap = false;
						}
						double totalConcentration = 0D;
						if (newMap.containsKey(mineralName)) {
							// Load the total concentration
							totalConcentration = newMap.get(mineralName);
						}
						
						totalConcentration += effect;
						if (totalConcentration > 100D)
							totalConcentration = 100D;
						
						newMap.put(mineralName, totalConcentration);
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
		if (allMineralsByLocation.isEmpty()
			|| !allMineralsByLocation.containsKey(location)) {
			
			return new HashMap<>();
		}
		else {
			return allMineralsByLocation.get(location);
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
		
		Map<String, Integer> map = allMineralsByLocation.get(location);
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
				logger.log(Level.SEVERE, "Error getting mineral types.", e);
			}
			
			mineralTypeNames = result;
			return result;
		}
		
		return mineralTypeNames;
	}

	/**
	 * Finds a random location with mineral concentrations from a starting location
	 * and within a distance range.
	 * 
	 * @param startingLocation the starting location.
	 * @param range            the distance range (km).
	 * @return location with one or more mineral concentrations or null if none
	 *         found.
	 */
	public Coordinates findRandomMineralLocation(Coordinates startingLocation, double range) {
		Coordinates chosen = null;

		List<Coordinates> locales = new ArrayList<>();
		
		if (allLocations == null) {
			allLocations = allMineralsByLocation.keySet();
		}
		
		Iterator<Coordinates> i = allLocations.iterator();
		while (i.hasNext()) {
			Coordinates c = i.next();
			double distance = Coordinates.computeDistance(startingLocation, c);
			if (range >= distance) {
				locales.add(c);
			}
		}

		int size = locales.size();
		
		logger.info(CollectionUtils.findSettlement(startingLocation), 30_000L, 
				"Found potentially " + size 
				+ " mineral sites to explore within " + Math.round(range * 10.0)/10.0 + " km.");
		
		if (size <= 0) {
			return null;
		}
		
		Map<Coordinates, Double> weightedMap = new HashMap<>();
		
		Coordinates closestC = null;
		double shortestD = range;
		
		for (int j = 0; j < size; j++) {
			Coordinates c = locales.get(j);
			double distance = Coordinates.computeDistance(startingLocation, c);

			// Fill up the weight map
			weightedMap.put(c, (range - distance) / range);
			
//			if (distance < shortestD) {
//				shortestD = distance;
//				closestC = c;
//			}
		}
		
//		logger.info(CollectionUtils.findSettlement(startingLocation), 30_000L, 
//				"Nearest mineral site: " + closestC + " (" + Math.round(shortestD * 10.0)/10.0 + " km).");
		
		// Choose one with weighted randomness 
		chosen = RandomUtil.getWeightedRandomObject(weightedMap);
		double chosenDist = weightedMap.get(chosen);
		
		logger.info(CollectionUtils.findSettlement(startingLocation), 30_000L, 
				"Chosen to investigate mineral site at " + chosen + " (" + Math.round(chosenDist * 10.0)/10.0 + " km).");

		return chosen;
	}

	@Override
	public void destroy() {
//		mineralConcentrations = null;
		allMineralsByLocation = null;
		mineralMapConfig = null;
	}
}

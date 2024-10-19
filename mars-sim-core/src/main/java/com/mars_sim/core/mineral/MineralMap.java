/*
 * Mars Simulation Project
 * MineralMap.java
 * @date 2024-08-30
 * @author Scott Davis
 */

package com.mars_sim.core.mineral;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.Serializable;
import java.net.URL;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.ImageIcon;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.MapPoint;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.Direction;
import com.mars_sim.core.map.location.SurfaceManager;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;

/**
 * A randomly generated mineral map of Mars.
 */
public class MineralMap implements Serializable{

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

	private static final SimLogger logger = SimLogger.getLogger(MineralMap.class.getName());
	
	public static final double MIN_DISTANCE = 0.5;
	 
	public static final String TOPO_MAP_FOLDER = "/topography/";
	
	private static final int W = 300;
	private static final int H = 150;

	private static final int REGION_FACTOR = 1500;
	private static final int NON_REGION_FACTOR = 50;

	// Topographical Region Strings
	private static final String CRATER_IMG = Msg.getString("RandomMineralMap.image.crater"); //$NON-NLS-1$
	private static final String VOLCANIC_IMG = Msg.getString("RandomMineralMap.image.volcanic"); //$NON-NLS-1$
	private static final String SEDIMENTARY_IMG = Msg.getString("RandomMineralMap.image.sedimentary"); //$NON-NLS-1$

	private static final String CRATER_REGION = "crater";
	private static final String VOLCANIC_REGION = "volcanic";
	private static final String SEDIMENTARY_REGION = "sedimentary";

	private static final double PHI_LIMIT = Math.PI / 7;
	private static final double ANGLE_LIMIT = .01;
	
	// A map of all mineral concentrations
	private SurfaceManager<MineralConcentration> allMinerals;
	
	private transient MineralMapConfig mineralMapConfig;
	
	/**
	 * Constructor.
	 */
	public MineralMap() {
	
		allMinerals = new SurfaceManager<>();
		
		// Determine mineral concentrations.
		determineMineralConcentrations();
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
		for(var mineralType : minerals) {
			
			// Create super set of topographical regions.
			List<Coordinates> potentialLocns = new ArrayList<>();

			// Each mineral has unique abundance in each of the 3 regions
			for(String locale : mineralType.getLocales()) {
				var newRegion = switch(locale) {
					case CRATER_REGION -> craterRegionSet;
					case VOLCANIC_REGION -> volcanicRegionSet;
					case SEDIMENTARY_REGION -> sedimentaryRegionSet;
					default -> null;
				};
				if (newRegion != null) {
					potentialLocns.addAll(newRegion);
				}
			}
		
			// Have one region array for each of 10 types of minerals 
			// regionArray between 850 and 3420 for each mineral
			
			// For now start with a random concentration between 0 to 100
			int conc = RandomUtil.getRandomInt(100);
			
			// Determine individual mineral iteration.
			int numIteration = calculateIteration(mineralType, true, potentialLocns.size());
					
			// Get the new remainingConc
			double remainingConc = 1.0 * conc * numIteration;
		
			for (int x = 0; x < numIteration; x++) {
				var c = RandomUtil.getRandomElement(potentialLocns);

				// Determine individual mineral concentrations.
				remainingConc = createMinerals(remainingConc, c, x, numIteration, conc,
						mineralType);
								
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
	 * @param mineral
	 */
	private double createMinerals(double remainingConc, Coordinates oldLocation, int x, int last, double conc,
								MineralType mineral) {
		Direction direction = new Direction(RandomUtil.getRandomDouble(Math.PI * 2D));
		// Spread it over a 10 km radius
		double distance = RandomUtil.getRandomDouble(1, 20);
		Coordinates newLocation = oldLocation.getNewLocation(direction, distance);
		int concentration = 0;

		if (x != last - 1)
			concentration = (int) Math.round(RandomUtil.getRandomDouble(conc *.75, conc));
		else
			concentration = (int) Math.round(remainingConc);

		remainingConc -= concentration;					

		if (concentration > 100)
			concentration = 100;
		if (concentration < 0) {
			concentration = 0;
		}
		
		addMineral(newLocation, mineral, concentration);
		return remainingConc;
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
			found = new MineralConcentration(locn);
			allMinerals.addFeature(found);
		}
		found.addMineral(mineral.getName(), conc);
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
						numIteration, conc, mineralType);	
				
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
			num = (int)Math.round(RandomUtil.getRandomDouble(.75, 1.25) 
					* REGION_FACTOR / 1800 * length
					/ mineralType.getFrequency());
		}
		else {
			num = (int)Math.round(RandomUtil.getRandomDouble(.75, 1.25) 
					* NON_REGION_FACTOR * 2 * length
					/ mineralType.getFrequency());
		}
		return num;
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
					double pixelOffset = (Math.PI / 150D) / 2D;
					double phi = (((double) x / 150D) * Math.PI) + pixelOffset;
					double theta = (((double) y / 150D) * Math.PI) + Math.PI + pixelOffset;
					if (theta > (2D * Math.PI))
						theta -= (2D * Math.PI);
					result.add(new Coordinates(phi, theta));
				}
			}
		}

		return result;
	}
	
	/**
	 * Gets some of the mineral concentrations at a given location.
	 * 
	 * @param mineralsDisplaySet 	a set of mineral strings.
	 * @param location
	 * @param mag		 the map magnification factor
	 * @return map of mineral types and percentage concentration (0 to 100.0)
	 */
	public Map<String, Integer> getSomeMineralConcentrations(Set<String> mineralsDisplaySet,
						MapPoint location, double mag) {
		
		double angle = ANGLE_LIMIT;
		double magSQ = Math.sqrt(mag);
		
		Map<String, Integer> newMap = new HashMap<>();
		
		double phi = location.phi();
		double theta = location.theta();

		// Not ideal creating a Coordinate but will resolve in a later commit
		for(var locnConc : allMinerals.getFeatures(new Coordinates(phi, theta), angle)) {
			
			Coordinates c = locnConc.getLocation();
			double phiLoc = c.getPhi();
			double thetaLoc = c.getTheta();

			double phiDiff = Math.abs(phi - phiLoc);
			double thetaDiff = Math.abs(theta - thetaLoc);
			
			// Only take in what's within a certain boundary
			if (phi > PHI_LIMIT && phi < Math.PI - PHI_LIMIT
				&& phiDiff < angle && thetaDiff < angle) {

				double deltaAngle = (phiDiff + thetaDiff) / 2;
				double radius = Math.max(phiDiff, thetaDiff);
				double limit = 1 - deltaAngle / radius;

				for(var displayed : mineralsDisplaySet) {
					
					int conc = 2 * locnConc.getConcentration(displayed);
					// Tune the fuzzyRange to respond to the map magnification and mineral concentration
					double fuzzyRange = conc * magSQ;	
					double effect =  Math.sqrt(limit) * fuzzyRange;
				
					int newConc = newMap.getOrDefault(displayed, 0);
					newConc = (int)(newConc + effect);
					if (newConc > 100)
						newConc = 100;
						
					newMap.put(displayed, newConc);
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
		return mineralMapConfig.getMineralTypes();
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
}

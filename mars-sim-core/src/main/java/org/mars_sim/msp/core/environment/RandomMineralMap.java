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
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Direction;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.environment.MineralMapConfig.MineralType;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * A randomly generated mineral map of Mars.
 */
public class RandomMineralMap implements Serializable, MineralMap {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(RandomMineralMap.class.getName());

	private static final int W = 300;
	private static final int H = 150;
	
	private static final int NUM_REGIONS = 50;

	private static final int REGION_FACTOR = 1000;
	private static final int NON_REGION_FACTOR = 20;
	
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
	
	// A map of all mineral concentrations
	private Map<Coordinates, Map<String, Integer>> allMineralsByLocation;

	private String[] mineralTypeNames;
	
	private static MineralMapConfig mineralMapConfig = SimulationConfig.instance().getMineralMapConfiguration();
	
	/**
	 * Constructor.
	 */
	RandomMineralMap() {
	
		allMineralsByLocation = new HashMap<>();
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
			int conc = RandomUtil.getRandomInt(80);
			
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
				
//				logger.info(
//						"  x: " + x
//						+ "  remainingConc: " + remainingConc);
				
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

//		logger.info(
//				"  oldLocation: " + oldLocation
//				+ "  mineralName: " + mineralName
//				+ "  concentration: " + (int)concentration
//				+ "  distance: " + (int)distance
//				);
		
//		map.clear();
//		
//		double effect = getMineralConcentrationEffect(oldLocation, newLocation, (int)Math.round(concentration));
//		if (effect > 0D) {
//			concentration += effect;
//			if (concentration > 100D)
//				concentration = 100D;
//
//			map.put(mineralName, (int)Math.round(concentration));
//			
////			logger.info(
////					"  newLocation: " + newLocation
////					+ "  mineralName: " + mineralName
////					+ "  concentration: " + (int)Math.round(concentration)
////					);
//			
//			
//			allMineralsByLocation.put(newLocation, map);
//		}

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
					* REGION_FACTOR / 2500 * length
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
	 * Gets all of the mineral concentrations at a given location.
	 * 
	 * @param location the coordinate location.
	 * @param baseMap
	 * @return map of mineral types and percentage concentration (0 to 100.0)
	 */
	public Map<String, Integer> getAllMineralConcentrations(Coordinates location, double rho) {

//		if (allMineralsByLocation.isEmpty()
//				|| !allMineralsByLocation.containsKey(location)) {
//				
//			return new HashMap<>();
//		}
//		else {
//			return allMineralsByLocation.get(location);
//		}
		
		double pixelHeight = rho * Math.PI;
		
		double pixelWidth = 2 * pixelHeight;
		
		double ratio = Coordinates.MARS_CIRCUMFERENCE /  pixelWidth;

		double H = pixelWidth / ratio; 
		
		double sinAngle = H / Coordinates.MARS_RADIUS_KM;
		
		double angle = Math.asin(sinAngle);
		
		Map<String, Integer> result = new HashMap<>();
		
		boolean emptyMap = true;
		
		Iterator<Coordinates> i = allMineralsByLocation.keySet().iterator();
		while (i.hasNext()) {
			Coordinates c = i.next();
			
			// Note : it merges with the code from getMineralConcentrationEffect() below
			
			double concentrationPhi = c.getPhi();
			double concentrationTheta = c.getTheta();
			double phiDiff = Math.abs(location.getPhi() - concentrationPhi);
			double thetaDiff = Math.abs(location.getTheta() - concentrationTheta);
//			double diffLimit = .04D;
//			
//			if (concentrationPhi < LIMIT || concentrationPhi > Math.PI - halfHeight)
//				diffLimit += Math.abs(Math.cos(concentrationPhi));
//			
//			if ((phiDiff < diffLimit) && (thetaDiff < diffLimit)) {
			
				// only take in what's within the limit
			if (phiDiff < angle && thetaDiff < angle)	{
				
				Iterator<String> j = allMineralsByLocation.get(c).keySet().iterator();
				while (j.hasNext()) {
					String mineralName = j.next();	
					double effect = 0;
					double conc = allMineralsByLocation.get(c).get(mineralName);

					double distance = location.getDistance(c);
					double concentrationRange = conc;
					if (distance < concentrationRange)
						effect = (1D - (distance / concentrationRange)) * conc;
					
					if (effect > 0D) {
						if (emptyMap) {
							result = new HashMap<>();
							emptyMap = false;
						}
						double totalConcentration = 0D;
						if (result.containsKey(mineralName))
							totalConcentration = result.get(mineralName);
						totalConcentration += effect;
						if (totalConcentration > 100D)
							totalConcentration = 100D;
						
						result.put(mineralName, (int)Math.round(totalConcentration));
					}
				}
			}	
		}
		
		return result;
		
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
	
//	/**
//	 * Gets the mineral concentration at a given location.
//	 * 
//	 * @param mineralType the mineral type (see MineralMap.java)
//	 * @param location    the coordinate location.
//	 * @return percentage concentration (0 to 100.0)
//	 */
//	public double getMineralConcentration(String mineralType, Coordinates location) {
//		double result = 0D;
//		Iterator<MineralConcentration> i = mineralConcentrations.iterator();
//		while (i.hasNext()) {
//			MineralConcentration mineralConcentration = i.next();
//			if (mineralConcentration.getMineralType().equalsIgnoreCase(mineralType)) {
//				result += getMineralConcentrationEffect(mineralConcentration, location);
//				if (result > 100D)
//					result = 100D;
//			}
//		}
//
//		return result;
//	}

//	/**
//	 * Gets the effect of a given mineral concentration on a location.
//	 * 
//	 * @param concentration the mineral concentration.
//	 * @param location      the location to affect.
//	 * @return concentration effect (0% - 100%).
//	 */
//	private double getMineralConcentrationEffect(MineralConcentration concentration, Coordinates location) {
//		double result = 0D;
//
//		double concentrationPhi = concentration.getLocation().getPhi();
//		double concentrationTheta = concentration.getLocation().getTheta();
//		double phiDiff = Math.abs(location.getPhi() - concentrationPhi);
//		double thetaDiff = Math.abs(location.getTheta() - concentrationTheta);
//		double diffLimit = .04D;
//		if ((concentrationPhi < Math.PI / 7D) || concentrationPhi > Math.PI - (Math.PI / 7D))
//			diffLimit += Math.abs(Math.cos(concentrationPhi));
//		if ((phiDiff < diffLimit) && (thetaDiff < diffLimit)) {
//			double distance = location.getDistance(concentration.getLocation());
//			double concentrationRange = concentration.getConcentration();
//			if (distance < concentrationRange)
//				result = (1D - (distance / concentrationRange)) * concentration.getConcentration();
//		}
//
//		return result;
//	}

//	/**
//	 * Adds the effect of a given mineral concentration on a location.
//	 * 
//	 * @param location the location.
//	 * @param concLocation  the mineral concentration location
//	 * @return concentration effect (0% - 100%).
//	 */
//	private double getMineralConcentrationEffect(Coordinates location, Coordinates concLocation, int conc) {
//		double result = 0D;
//
//		double concentrationPhi = concLocation.getPhi();
//		double concentrationTheta = concLocation.getTheta();
//		double phiDiff = Math.abs(location.getPhi() - concentrationPhi);
//		double thetaDiff = Math.abs(location.getTheta() - concentrationTheta);
//		double diffLimit = .04D;
//		if ((concentrationPhi < Math.PI / 7D) || concentrationPhi > Math.PI - (Math.PI / 7D))
//			diffLimit += Math.abs(Math.cos(concentrationPhi));
//		if ((phiDiff < diffLimit) && (thetaDiff < diffLimit)) {
//			double distance = location.getDistance(concLocation);
//			double concentrationRange = conc;
//			if (distance < concentrationRange)
//				result = (1D - (distance / concentrationRange)) * conc;
//		}
//
//		return result;
//	}
	
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
		Coordinates result = null;

		List<Coordinates> locales = new ArrayList<>();
		
		Iterator<Coordinates> i = allMineralsByLocation.keySet().iterator();
		while (i.hasNext()) {
			Coordinates c = i.next();
			double distance = Coordinates.computeDistance(startingLocation, c);
			if (range > distance) {
				locales.add(c);
			}
		}

		if (locales.size() > 0) {
			int index = RandomUtil.getRandomInt(locales.size() - 1);
			Coordinates c = locales.get(index);
			double distance = Coordinates.computeDistance(startingLocation, c);
			if (range < distance) {
				Direction direction = startingLocation.getDirectionToPoint(c);
				result = startingLocation.getNewLocation(direction, range);
			} else
				result = c;
		}

		return result;
	}

	@Override
	public void destroy() {
//		mineralConcentrations = null;
		allMineralsByLocation = null;
		mineralMapConfig = null;
	}
}

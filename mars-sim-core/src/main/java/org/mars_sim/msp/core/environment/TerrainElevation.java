/*
 * Mars Simulation Project
 * TerrainElevation.java
 * @date 2023-05-09
 * @author Scott Davis
 */

package org.mars_sim.msp.core.environment;

import java.awt.Color;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mars.sim.mapdata.MapDataUtil;
import org.mars.sim.mapdata.location.Coordinates;
import org.mars.sim.mapdata.location.Direction;
import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.structure.Settlement;

// Note: the newly surveyed ice deposit spans latitudes from 39 to 49 deg
// within the Utopia Planitia plains, as estimated by SHARAD, an subsurface
// sounding radar ice that penetrate below the surface. SHARAD was mounted
// on the Mars Reconnaissance Orbiter.

// See https://www.jpl.nasa.gov/news/news.php?feature=6680

/**
 * The TerrainElevation class represents the surface terrain of the virtual
 * Mars. It provides information about elevation and terrain ruggedness and
 * calculate ice collection rate at a location on its vast surface.
 */
public class TerrainElevation implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final SimLogger logger = SimLogger.getLogger(TerrainElevation.class.getName());
	
	public static final double STEP_KM = 2;
	
	private static final double DEG_TO_RAD = Math.PI/180;

	private static final double RATE = 1;

	private static final String TOPO_MAP_TYPE = "topo";
	
	private static MapDataUtil mapDataUtil = MapDataUtil.instance();

	private static Set<CollectionSite> sites;

	private transient Map<Coordinates, double[]> terrainCacheMap = new HashMap<>();
	
	/**
	 * Constructor.
	 */
	public TerrainElevation() {
		sites = new HashSet<>();
	}

	/**
	 * Returns terrain steepness angle (in radians) from location by sampling a step distance in given
	 * direction.
	 *
	 * @param currentLocation  the coordinates of the current location
	 * @param currentDirection the current direction (in radians)
	 * @return terrain steepness angle (in radians)
	 */
	public static double determineTerrainSteepness(Coordinates currentLocation, Direction currentDirection) {
		return determineTerrainSteepness(currentLocation, getMOLAElevation(currentLocation), currentDirection);
	}

	/**
	 * Determines the terrain steepness angle (in radians) from location by sampling a step distance in given
	 * direction and elevation.
	 *
	 * @param currentLocation
	 * @param elevation
	 * @param currentDirection
	 * @return
	 */
	public static double determineTerrainSteepness(Coordinates currentLocation, double elevation, Direction currentDirection) {
		double newY = - 1.5 * currentDirection.getCosDirection();
		double newX = 1.5 * currentDirection.getSinDirection();
		Coordinates sampleLocation = currentLocation.convertRectToSpherical(newX, newY);
		double elevationChange = getAverageElevation(sampleLocation) - elevation;
		double steepness = Math.atan(elevationChange / STEP_KM);
//		logger.config("elevation: " + elevation + "  1.  steepness: " + Math.round(steepness * 100.0)/100.0);
		return steepness;
	}

	/**
	 * Determines the terrain steepness angle (in radians) from location by sampling a random coordinate set and a step distance in given
	 * direction and elevation.
	 *
	 * @param currentLocation
	 * @param elevation
	 * @param currentDirection
	 * @return
	 */
	public static double determineTerrainSteepnessRandom(Coordinates currentLocation, double elevation, Direction currentDirection) {
		double newY = - RandomUtil.getRandomDouble(1.5) * currentDirection.getCosDirection();
		double newX = RandomUtil.getRandomDouble(1.5) * currentDirection.getSinDirection();
		Coordinates sampleLocation = currentLocation.convertRectToSpherical(newX, newY);
		double elevationChange = getAverageElevation(sampleLocation) - elevation;
		return Math.atan(elevationChange / STEP_KM);
	}

	/**
	 * Computes the terrain profile of a site at a coordinate
	 * direction and elevation.
	 *
	 * @param {@link Coordinates} currentLocation
	 * @return an array of two doubles, namely elevation and steepness
	 */
	public double[] computeTerrainProfile(Coordinates currentLocation) {
		
		if (!terrainCacheMap.containsKey(currentLocation)) {
			double steepness = 0;
			double elevation = getAverageElevation(currentLocation);
			for (int i=0 ; i <= 360 ; i++) {
				double rad = i * DEG_TO_RAD;
				steepness += Math.abs(determineTerrainSteepness(currentLocation, elevation, new Direction(rad)));
			}
		
			double[] terrain = {elevation, steepness};
			
			terrainCacheMap.put(currentLocation, terrain);
			
			return terrain;
		}
		
		return terrainCacheMap.get(currentLocation);
	}

	/**
	 * Gets the terrain profile of a location.
	 *
	 * @param {@link Coordinates} currentLocation
	 * @return an array of two doubles, namely elevation and steepness
	 */
	public double[] getTerrainProfile(Coordinates currentLocation) {
		return computeTerrainProfile(currentLocation);
	}


	/**
	 * Computes the regolith collection rate of a location.
	 *
	 * @param site
	 * @param currentLocation
	 * @return regolith collection rate
	 */
	public void computeRegolithCollectionRate(CollectionSite site, Coordinates currentLocation) {

		// Get the elevation and terrain gradient factor
		double[] terrainProfile = getTerrainProfile(currentLocation);

		double elevation = terrainProfile[0];
		double steepness = terrainProfile[1];
		double latitude = currentLocation.getLatitudeDouble();

		site.setElevation(elevation);
		site.setSteepness(steepness);

		double rate = RATE;

		// Note: Add seasonal variation for north and south hemisphere
		// Note: The collection rate may be increased by relevant scientific studies

		if (latitude < 60 && latitude > -60) {
			// The steeper the slope, the harder it is to retrieve the deposit
			rate *= RandomUtil.getRandomDouble(10) + (- 0.639 * elevation + 14.2492) / 5D  - Math.abs(steepness) / 10D;
		}

		else if ((latitude >= 60 && latitude < 75)
			|| (latitude <= -60 && latitude > -75)) {
			rate *= RandomUtil.getRandomDouble(5) + Math.abs(elevation) / 20.0  - Math.abs(latitude) / 100.0 - Math.abs(steepness) / 10D;
		}

		else if ((latitude >= 75 && latitude <= 90)
				|| (latitude <= -75 && latitude >= -90)) {
				rate *= Math.abs(elevation) / 50.0  - Math.abs(latitude) / 50.0;
		}

		if (rate > 200)
			rate = 200;

		if (rate < 1)
			rate = 1;

		site.setRegolithCollectionRate(rate);
	}

	/**
	 * Computes the ice collection rate of a location.
	 *
	 * @param site
	 * @param currentLocation
	 * @return ice collection rate
	 */
	public void computeIceCollectionRate(CollectionSite site, Coordinates currentLocation) {

		// Get the elevation and terrain gradient factor
		double[] terrainProfile = getTerrainProfile(currentLocation);

		double elevation = terrainProfile[0];
		double steepness = terrainProfile[1];
		double latitude = currentLocation.getLatitudeDouble();

		site.setElevation(elevation);
		site.setSteepness(steepness);

		double rate = RATE;

		// Note 1: Add seasonal variation for north and south hemisphere
		// Note 2: The collection rate may be increased by relevant scientific studies

		if (latitude < 60 && latitude > -60) {
			// The steeper the slope, the harder it is to retrieve the ice deposit
			rate *= (- 0.639 * elevation + 14.2492) / 20D + Math.abs(steepness) / 10D;
		}

		else if ((latitude >= 60 && latitude < 75)
			|| (latitude <= -60 && latitude > -75)) {
			rate *= RandomUtil.getRandomDouble(5) + Math.abs(elevation) / 2.0 + Math.abs(latitude) / 75.0 - Math.abs(steepness) / 10D;
		}

		else if ((latitude >= 75 && latitude <= 90)
				|| (latitude <= -75 && latitude >= -90)) {
				rate *= RandomUtil.getRandomDouble(10) + Math.abs(elevation) + Math.abs(latitude) / 75.0;
		}

		if (rate > 200)
			rate = 200;

		if (rate < 1)
			rate = 1;

		site.setIceCollectionRate(rate);
	}

	/**
	 * Obtains the ice collection rate of a location.
	 *
	 * @param loc
	 * @return the collection rate
	 */
	public double obtainIceCollectionRate(Coordinates loc) {
		CollectionSite site = getCollectionSite(loc);
		if (site.getIceCollectionRate() == -1)
			computeIceCollectionRate(site, loc);
		return site.getIceCollectionRate();
	}

	/**
	 * Obtains the regolith collection rate of a location.
	 *
	 * @param loc
	 * @return the collection rate
	 */
	public double obtainRegolithCollectionRate(Coordinates loc) {
		CollectionSite site = getCollectionSite(loc);
		if (site.getRegolithCollectionRate() == -1)
			computeRegolithCollectionRate(site, loc);
		return site.getRegolithCollectionRate();
	}

//	/**
//	 * Compute the RGB Topo map based Elevation.
//	 * 
//	 * @param location
//	 * @return
//	 */
//	public static int getRGBIntElevation(Coordinates location) {	
//		// Find hue and saturation color components at location.
//		int color = mapDataUtil.getMapData("topo").getRGBColorInt(location.getPhi(), location.getTheta());
//
//		// The peak of Olympus Mons is 21,229 meters (69,649 feet) above the Mars areoid (a reference datum similar to Earth's sea level). 
//		// The lowest point is within the Hellas Impact Crater (marked by a flag with the letter "L"). The lowest point in the Hellas Impact Crater is 8,200 meters (26,902 feet) below the Mars areoid. 
//		
//		double height = (color - 9000) / 10_000;
//
//		return (int)Math.round(height);
//	}
	
	/**
	 * Compute the RGB Topo map based Elevation.
	 * 
	 * @param location
	 * @return
	 */
	public static double getRGBElevation(Coordinates location) {	
		// Find hue and saturation color components at location.
		Color color = new Color(mapDataUtil.getMapData(TOPO_MAP_TYPE).getRGBColorInt(location.getPhi(), location.getTheta()));
		int red = color.getRed();
		int green = color.getGreen();
		int blue = color.getBlue();
		
		int[] rgb = {red, green, blue};
		
		// Returns the hue value only
		float[] hsb = getHSB(rgb);
		
		double height = hsb[0];

		return height;
	}
	
    /** 
     * Returns the average elevation using both the topo map and MOLA data set.
     * 
     *  @return elevation in km.
     */
    public static double getAverageElevation(Coordinates location) {
//    	double elevationTopo = getRGBElevation(location);
    	double elevationMOLA = getMOLAElevation(location);
//		logger.info(20_000L, "elevationTopo: " + elevationTopo + " km.  " + "elevationMOLA: " + (int)elevationMOLA + " km.");
    	return elevationMOLA;
    }
    
	
    /**
     * Returns the HSB array.
     * 
     * @param rgb
     * @return
     */
    public static float[] getHSB(int[] rgb) {
		float[] hsb = Color.RGBtoHSB(rgb[0], rgb[1], rgb[2], null);
		float hue = hsb[0];
		float saturation = hsb[1];
		float brightness = hsb[2];

//		String s1 = String.format(" %5.3f %5.3f %5.3f  ",
//				Math.round(hue*1000.0)/1000.0,
//				Math.round(saturation*1000.0)/1000.0,
//				Math.round(brightness*1000.0)/1000.0);
//		System.out.print(s1);

		return new float[] {hue, saturation, brightness};
	}

	/**
	 * Returns the elevation in km at the given location, based on MOLA's dataset.
	 *
	 * @param location the location in question
	 * @return the elevation at the location (in km)
	 */
	public static double getMOLAElevation(Coordinates location) {	
		// Check if this location is a settlement
		Settlement s = CollectionUtils.findSettlement(location);
		double MOLAHeight = 0;
		if (s != null) {
			MOLAHeight = s.getElevation();
		}
		else {
			MOLAHeight = getMOLAElevation(location.getPhi(), location.getTheta());	
		}
			
		return MOLAHeight;
	}

	/**
	 * Returns the elevation in km at the given location, based on MOLA's dataset.
	 *
	 * @param phi
	 * @param theta
	 * @return the elevation at the location (in km)
	 */
	public static double getMOLAElevation(double phi, double theta) {
		return mapDataUtil.getElevation(phi, theta)/1000.0;
	}

	
	public Set<CollectionSite> getCollectionSites() {
		return sites;
	}

	public static void addCollectionSite(CollectionSite site) {
		sites.add(site);
	}

	public static synchronized CollectionSite getCollectionSite(Coordinates newLocation) {
		// Create a shallow copy of sites to avoid ConcurrentModificationException
		for (CollectionSite s:  sites) {
			if (s.getLocation().equals(newLocation)) {
				return s;
			}
		}
		CollectionSite site = new CollectionSite(newLocation);
		addCollectionSite(site);
		return site;
	}

	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		//nothing
	}
}
